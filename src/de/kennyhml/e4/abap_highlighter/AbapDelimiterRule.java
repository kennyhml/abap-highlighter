package de.kennyhml.e4.abap_highlighter;

import java.util.Set;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import de.kennyhml.e4.abap_highlighter.AbapContext.ContextFlag;
import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

public class AbapDelimiterRule extends BaseAbapRule {

	@Override
	public IToken evaluate(AbapScanner scanner) {
		AbapContext ctx = scanner.getContext();

		int c = scanner.read();
		if (c == ICharacterScanner.EOF || !DELIMITERS.contains((char) c)) {
			return Token.UNDEFINED;
		}

		if (c == ':' && ctx.lastTokenMatches(TokenType.KEYWORD)) {
			if (ctx.active(ContextFlag.CONTEXT_FUNC_DECL)) {
				ctx.activate(ContextFlag.CONTEXT_FUNC_MULTI_DECL);
			} else if (ctx.active(ContextFlag.CONTEXT_DATA_DECL)) {
				ctx.activate(ContextFlag.CONTEXT_DATA_MULTI_DECL);
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
			fDelimiterToken.setData(new TextAttribute(NO_COLOR));
		} else {
			fDelimiterToken.setData(new TextAttribute(fColors[stack % fColors.length]));
		}

		fDelimiterToken.setText(Character.toString(c));
		ctx.addToken(fDelimiterToken);
		return fDelimiterToken;
	}
	
	private static final Color NO_COLOR = new Color(255, 255, 255);
	
	private static final Color STACK_YELLOW = new Color(255, 215, 16);	
	private static final Color STACK_PURPLE = new Color(206, 112, 203);	
	private static final Color STACK_BLUE = new Color(26, 159, 219);
	
	private static final Color[] fColors = { STACK_YELLOW, STACK_PURPLE, STACK_BLUE };
	
	private AbapToken fDelimiterToken = new AbapToken(NO_COLOR, TokenType.DELIMITER);

	static final Set<Character> DELIMITERS = Set.of('(', ')', '{', '}', '[', ']', ':', '.', ',');
	static final Set<Character> OPENING_PARENS = Set.of('(', '{', '[');
	static final Set<Character> CLOSING_PARENS = Set.of(')', '}', ']');
}
