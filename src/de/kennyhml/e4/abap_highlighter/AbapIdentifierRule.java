package de.kennyhml.e4.abap_highlighter;
import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

import java.util.Set;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class AbapIdentifierRule extends BaseAbapRule {

	private static class IdentifierDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			// Identifiers can only start with letters, underscores or slashes
			return Character.isLetter(c) || c == '_' || c == '/';
		}

		@Override
		public boolean isWordPart(char c) {
			// Identifiers can only contain letters, digits, underscores and slashes.
			return Character.isLetterOrDigit(c) || c == '_' || c == '/';
		}

	}

	@Override
	public IToken evaluate(AbapScanner scanner) {
		AbapContext ctx = scanner.getContext();

		int c = scanner.read();
		if (c == ICharacterScanner.EOF || !fDetector.isWordStart((char) c)) {
			return Token.UNDEFINED;
		}

		String text = scanner.readNext(c, fDetector);

		if (inTypeContext(ctx) || staticAccessUpcoming(scanner)) {
			fTypeToken.setText(text);
			ctx.addToken(fTypeToken);
			return fTypeToken;
		}

		fIdentifierToken.setText(text);
		ctx.addToken(fIdentifierToken);
		return fIdentifierToken;
	}
	
	/**
	 * Checks if the current context requires a type at our position.
	 * 
	 * This could be because the previous keyword always precedes a type, such
	 * as conv, cond or new. But other contexts such as multi type declarations
	 * and exceptions are also accounted for.
	 * 
	 * @param ctx The context to check for the previous tokens.
	 * 
	 * @return Whether the context requires a type.
	 */
	private boolean inTypeContext(AbapContext ctx) {
		return isTypeReference(ctx) || isTypeInClass(ctx) || isTypeInMultiDeclContext(ctx)
				|| ctx.tokenMatchesAny(0, TokenType.KEYWORD, fTypeKeywords);
	}
	
	/**
	 * Checks for cases where the type is referenced through a class, for example
	 * cl_my_class=>gtys_my_struct.
	 * 
	 * @param ctx The context to check for a referenced type.
	 * @return
	 */
	private boolean isTypeInClass(AbapContext ctx) {
		return ctx.lastTokenMatches(TokenType.OPERATOR, "=>") && ctx.tokenMatches(2, TokenType.KEYWORD, "type");
	}

	private boolean isTypeReference(AbapContext ctx) {
		return (ctx.tokenMatches(1, TokenType.KEYWORD, "ref") && ctx.lastTokenMatches(TokenType.KEYWORD, "to"))
				|| (ctx.lastTokenMatches(TokenType.KEYWORD, "ref"));
	}
	
	/**
	 * Checks if the current word is a type in a multi declaration context, for
	 * example
	 * 
	 * types: begin of gtys_mystruct, var1 type string, var2 type char10, end of
	 * gtys_mystruct, begin of gtys_mystruct2, var1 type string, var2 type char10,
	 * end of gtys_mystruct2, cust_type type string. ---------
	 * 
	 * @param ctx The context to check for the multi declaration.
	 * 
	 * @return Whether the current word is a type or not.
	 */
	private boolean isTypeInMultiDeclContext(AbapContext ctx) {
		return (ctx.tokenMatches(-1, TokenType.KEYWORD, "types") && ctx.tokenMatches(-2, TokenType.DELIMITER, ":"))
				&& ctx.lastTokenMatches(TokenType.DELIMITER, ",");
	}

	/**
	 * Scan past the current word to check for a static class access, for example
	 * 
	 * cl_my_class=>some_static_method()
	 * -----------
	 * 
	 * @param scanner The scanner to scan with, CANNOT RELY ON ROLLBACK HERE!
	 * 
	 * @return Whether a static type access is upcoming.
	 */
	private boolean staticAccessUpcoming(AbapScanner scanner) {
		boolean ret = scanner.read() == '=';
		scanner.unread();
		return ret;
	}

	private AbapToken fIdentifierToken = new AbapToken(GENERIC_COLOR, AbapToken.TokenType.IDENTIFIER);
	private AbapToken fTypeToken = new AbapToken(TYPE_COLOR, AbapToken.TokenType.IDENTIFIER);

	private IdentifierDetector fDetector = new IdentifierDetector();

	private static final Color GENERIC_COLOR = new Color(156, 220, 254);
	private static final Color TYPE_COLOR = new Color(78, 201, 176);

	private static final Set<String> fTypeKeywords = 
			Set.of("type", "raising", "class", "new", "catch", "of", "conv", "value", "types", "cond", "corresponding");
}
