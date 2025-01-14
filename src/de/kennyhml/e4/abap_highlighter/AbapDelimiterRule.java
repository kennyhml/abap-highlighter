package de.kennyhml.e4.abap_highlighter;

import de.kennyhml.e4.abap_highlighter.context.ContextFlag;
import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

import java.util.Set;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;


public class AbapDelimiterRule extends BaseAbapRule {

	@Override
	public boolean isPossibleInContext(AbapContext ctx) {
		// Delimiters cannot exist as the first token in a context
		return !ctx.isEmpty();
	}
	
	
	@Override
	public TokenType getTokenType() {
		return fToken.getType();
	}
	
	@Override
	public IToken evaluate(AbapScanner scanner) {
		AbapContext ctx = scanner.getContext();

		int c = scanner.read();
		if (c == ICharacterScanner.EOF || !DELIMITERS.contains((char) c)) {
			return Token.UNDEFINED;
		}

		if (c == ':' && ctx.lastTokenMatches(TokenType.KEYWORD)) {
			if (ctx.active(ContextFlag.FN_DECL)) {
				ctx.activate(ContextFlag.FN_MULTI_DECL);
			} else if (ctx.active(ContextFlag.DATA_DECL)) {
				ctx.activate(ContextFlag.DATA_MULTI_DECL);
			}
		}
		
		int stack = -1;
		if (OPENING_PARENS.contains((char)c)) {
			stack = ctx.pushBracket();
		} else if (CLOSING_PARENS.contains((char)c)) {
			stack = ctx.popBracket();
		}
		
		// No color
		if (stack == -1) {
			fToken.setData(new TextAttribute(NO_COLOR));
		} else {
			// Set the color based on the current bracket stack
			fToken.setData(new TextAttribute(fColors[stack % fColors.length]));
		}

		fToken.setText(Character.toString(c));
		ctx.addToken(fToken);
		return fToken;
	}
	
	private static final Color NO_COLOR = new Color(255, 255, 255);
	
	private static final Color STACK_YELLOW = new Color(255, 215, 16);	
	private static final Color STACK_PURPLE = new Color(206, 112, 203);	
	private static final Color STACK_BLUE = new Color(26, 159, 219);
	
	private static final Color[] fColors = { STACK_YELLOW, STACK_PURPLE, STACK_BLUE };
	
	private AbapToken fToken = new AbapToken(NO_COLOR, TokenType.DELIMITER);

	static final Set<Character> DELIMITERS = Set.of('(', ')', '{', '}', '[', ']', ':', '.', ',');
	static final Set<Character> OPENING_PARENS = Set.of('(', '{', '[');
	static final Set<Character> CLOSING_PARENS = Set.of(')', '}', ']');
}
