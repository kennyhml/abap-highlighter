package de.kennyhml.e4.abap_highlighter;

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

public class AbapOperatorRule extends BaseAbapRule {

	@Override
	public boolean isPossibleInContext(AbapContext ctx) {
		return ctx.lastTokenMatches(TokenType.KEYWORD) 
				|| ctx.lastTokenMatches(TokenType.TYPE_IDENTIFIER) 
				|| ctx.lastTokenMatches(TokenType.LITERAL) 
				|| ctx.lastTokenMatchesAny(TokenType.DELIMITER, Set.of("]", ")"));
	}
	
	
	@Override
	public TokenType getTokenType() {
		return fToken.getType();
	}
	
	@Override
	public IToken evaluate(AbapScanner scanner) {
		AbapContext ctx = scanner.getContext();
		int c = scanner.read();
		if (c == AbapScanner.EOF || !fOpCharacters.contains((char) c)) {
			return Token.UNDEFINED;
		}

		int next = scanner.read();

		// The '/' symbol is technically valid in identifier contexts, but may also be
		// used for operator context during division or newlines on writing, in which
		// case it should have a whitespace coming up after it.
		if (c == '/' && !Character.isWhitespace(next)) {
			return Token.UNDEFINED;
		}

		// Check if this operator forms a "bigger" operator
		if (fComplements.getOrDefault((char) c, (char)0) == next) {
			fToken.setText(Character.toString(c) + Character.toString(next));
		} else {
			scanner.unread();
			fToken.setText(Character.toString(c));
		}
		
		if (c == '!') {
			ctx.setNextPossibleTokens(Set.of(TokenType.IDENTIFIER));
		} else {
			ctx.setNextPossibleTokens(Set.of());
		}
		ctx.addToken(fToken);
		return fToken;
	}

	private static final Set<Character> fOpCharacters = Set.of('=', '>', '<', '+', '-', '~', '/', '!');

	// Complementary map of characters that can come after another character to form
	// after an operator
	// character to form an operator, e.g '=' and '>' are both operators but can
	// also form '=>'
	private static final Map<Character, Character> fComplements = Map.of(
			'<', '>', 
			'=', '>', 
			'-', '>');

	private static AbapToken fToken = new AbapToken(new Color(255, 255, 255), AbapToken.TokenType.OPERATOR);
}
