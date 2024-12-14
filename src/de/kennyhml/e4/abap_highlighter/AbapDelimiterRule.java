package de.kennyhml.e4.abap_highlighter;

import java.util.Set;

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
		fDelimiterToken.setText(Character.toString(c));
		ctx.addToken(fDelimiterToken);
		return fDelimiterToken;
	}

	private static final Color DELIMITER_COLOR = new Color(255, 255, 255);
	private AbapToken fDelimiterToken = new AbapToken(DELIMITER_COLOR, TokenType.DELIMITER);

	static final Set<Character> DELIMITERS = Set.of('(', ')', '{', '}', '[', ']', ':', '.', ',');
}
