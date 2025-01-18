package de.kennyhml.e4.abap_highlighter;

import de.kennyhml.e4.abap_highlighter.context.ContextFlag;
import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

import java.util.Set;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class AbapFieldRule extends BaseAbapRule {

	private static class FieldDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return Character.isLetter(c) || c == '/';
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isLetterOrDigit(c) || c == '_' || c == '/';
		}
	}

	
	@Override
	public boolean isPossibleInContext(AbapContext ctx) {
		return (isAccessingField(ctx) || isDefiningField(ctx) || isDefiningKeyComponents(ctx));
	}
	
	
	@Override
	public TokenType getTokenType() {
		return fToken.getType();
	}
	
	
	@Override
	/*
	 * Check if previous token is a field initiator, for example
	 * 
	 * struct-field or table~field
	 * 
	 * Or if the previous token is part of a table key definition, for example
	 * 
	 * ... key connid. Or if its predecessor was part of a table key definition, for
	 * example
	 * 
	 * ... key connid carrid.
	 * 
	 * All the while making sure that its not the alias of a key that is defined
	 * using components, for example
	 * 
	 * ... with non-unique key primary_key components connid carrid ... with
	 * non-unique sorted key secondary_key components connid carrid
	 * 
	 */
	public IToken evaluate(AbapScanner scanner) {
		AbapContext ctx = scanner.getContext();

		int c = scanner.peek();
		if (c == AbapScanner.EOF || !fDetector.isWordStart((char) c)) {
			return Token.UNDEFINED;
		}

		// Read the full word, we know it must be a field.
		String currWord = scanner.readNext(fDetector);
		String nextWord = scanner.peekNext(fDetector);

		// Next word is components, rewind the scanner completely.
		if (nextWord.equals("components")) {
			fKeyToken.setText(currWord);
			ctx.addToken(fKeyToken);
			return fKeyToken;
		}

		fToken.setText(currWord);
		ctx.addToken(fToken);
		ctx.setNextPossibleTokens(Set.of());
		return fToken;
	}

	private boolean isAccessingField(AbapContext ctx) {
		return ctx.lastTokenMatchesAny(TokenType.OPERATOR, fFieldInitiators);
	}

	private boolean isDefiningKeyComponents(AbapContext ctx) {
		if (!(ctx.hasWord("key") || ctx.hasWord("components"))) {
			return false;
		}

		return ctx.lastTokenMatchesAny(TokenType.KEYWORD, fComponentInitiators)
				|| ctx.lastTokenMatches(TokenType.FIELD);
	}

	private boolean isDefiningField(AbapContext ctx) {
		return ctx.active(ContextFlag.STRUCT_DECL) && ctx.lastTokenMatches(TokenType.DELIMITER, ",");
	}

	private static final Color FIELD_COLOR = new Color(147, 115, 165);
	private static final Color KEY_COLOR = new Color(149, 98, 181);

	private AbapToken fToken = new AbapToken(FIELD_COLOR, TokenType.FIELD);
	private AbapToken fKeyToken = new AbapToken(KEY_COLOR, TokenType.FIELD);

	private Set<String> fFieldInitiators = Set.of("-", "~");
	private Set<String> fComponentInitiators = Set.of("key", "components");

	private IWordDetector fDetector = new FieldDetector();
}
