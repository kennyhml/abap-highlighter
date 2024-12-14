package de.kennyhml.e4.abap_highlighter;

import java.util.Set;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import de.kennyhml.e4.abap_highlighter.AbapContext.ContextFlag;
import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

public class AbapFunctionRule extends BaseAbapRule {

	private static class FunctionDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return Character.isLetter(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isLetterOrDigit(c) || c == '_';
		}
	}

	@Override
	public IToken evaluate(AbapScanner scanner) {
		AbapContext ctx = scanner.getContext();

		if (!functionAllowedInContext(ctx)) {
			return Token.UNDEFINED;
		}

		int c = scanner.read();
		if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)) {
			String text = scanner.readNext(c, fDetector);

			if (isFunctionDeclaration(scanner) || scanForCall(scanner)) {
				fSubroutineToken.setText(text);
				ctx.addToken(fSubroutineToken);
				return fSubroutineToken;
			}
		}
		return Token.UNDEFINED;
	}

	/**
	 * Check for some keywords that initiate a call-like sequence, such as Class
	 * initialization: new z_class( ). Type casts: conv z_custom_Type( lv_value ).
	 * Inline assignments: data(ls_struct) = value struct_type( field1 = 'X' ... ).
	 * 
	 * As their token must be a type instead.
	 * 
	 * @param ctx The context to check in.
	 * 
	 * @return Whether a function is allowed in the current context.
	 */
	private boolean functionAllowedInContext(AbapContext ctx) {
		return !ctx.lastTokenMatchesAny(TokenType.KEYWORD, fForbiddenContext) 
				&& !ctx.active(ContextFlag.CONTEXT_DATA_MULTI_DECL);
	}

	/**
	 * Checks if the current token is a function declaration based on the previous
	 * context. If the context is in a multi function declaration, it will
	 * scan the upcoming characters to check for a function signature.
	 * 
	 * @param scanner The scanner needed to perform scans for a multi declaration possibility.
	 * 
	 * @return Whether the token is a function declaration.
	 */
	private boolean isFunctionDeclaration(AbapScanner scanner) {
		AbapContext ctx = scanner.getContext();
		
		if (ctx.lastTokenMatchesAny(TokenType.KEYWORD, fCallDeclarations)) {
			return true;
		}
		
		if (!ctx.active(ContextFlag.CONTEXT_FUNC_MULTI_DECL)) {
			return false;
		}
		
		return checkIsMultiDeclaration(scanner);
	}
	
	
	/**
	 * Scans for a paranthesis opening to indicate that the token is being
	 * called, there are other situations where this may happen (e.g buffer size,
	 * type conversions..) which are filtered out beforehand.
	 * 
	 * @param scanner The scanner used to check for the call.
	 * 
	 * @return Whether a call is found.
	 */
	private boolean scanForCall(AbapScanner scanner) {
		int c = scanner.read();
		scanner.unread();
		return c == '(';
	}

	/**
	 * Scans ahead of the current word to check if the token is a function
	 * declaration that is part of a multi function declaration construct.
	 * 
	 * @param scanner The scanner used to perform the check, the offset is restored.
	 * 
	 * @return Whether the token is a function declaration of a multi declaration.
	 */
	private boolean checkIsMultiDeclaration(AbapScanner scanner) {
		StringBuilder buffer = new StringBuilder();
		int c = scanner.read();
		int times_read = 1;

		boolean ret = false;

		// Mutli declared method with no parameters terminating the methods statement
		// or initiating another one.
		if (c == '.' || c == ',') {
			scanner.unread();
			return true;
		}

		// If the following character is a whitespace or a newline, make sure to skip it
		while (Character.isWhitespace(c) || c == '\n') {
			c = scanner.read();
			times_read++;
		}

		// Read the term that follows and check if belongs to a function signature
		if (Character.isLetter(c)) {
			do {
				buffer.append((char) c);
				c = scanner.read();
				times_read++;
			} while (c != ICharacterScanner.EOF && Character.isLetter(c));

			String kw = buffer.toString().toLowerCase();
			ret = fSignatureInitiators.contains(kw);
		}

		// Unread the word since we dont want to tokenize it
		for (int i = 0; i < times_read; i++) {
			scanner.unread();
		}
		return ret;
	}

	private static Set<String> fCallDeclarations = Set.of("methods", "method", "class-methods");
	private static Set<String> fSignatureInitiators = Set.of("importing", "returning", "raising", "changing",
			"exporting", "exceptions");

	private static final Set<String> fForbiddenContext = 
			Set.of("new", "conv", "value", "cond", "type", "raising", "corresponding");
	
	private static final Color SUBROUTINE_COLOR = new Color(220, 220, 170);

	private AbapToken fSubroutineToken = new AbapToken(SUBROUTINE_COLOR, TokenType.FUNCTION_CALL);

	private IWordDetector fDetector = new FunctionDetector();
}
