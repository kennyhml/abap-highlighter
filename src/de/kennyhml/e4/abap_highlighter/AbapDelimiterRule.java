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
		if (c != ICharacterScanner.EOF && DELIMITERS.contains((char)c)) {
			if (c == ':') {
				if (ctx.lastTokenMatchesAny(TokenType.KEYWORD, Set.of("methods", "class-methods"))) {
					ctx.activate(ContextFlag.CONTEXT_FUNC_MULTI_DECL);				
				}
				else if (ctx.lastTokenMatchesAny(TokenType.KEYWORD, Set.of("data", "class-data"))) {
					ctx.activate(ContextFlag.CONTEXT_DATA_MULTI_DECL);
				}
			}
			fDelimiterToken.setText(Character.toString(c));
			ctx.addToken(fDelimiterToken);
			return fDelimiterToken;
		}
		return Token.UNDEFINED;
	}


	private static final Color DELIMITER_COLOR = new Color(255, 255, 255);
	private AbapToken fDelimiterToken = new AbapToken(DELIMITER_COLOR, TokenType.DELIMITER);
	
	static final Set<Character> DELIMITERS = Set.of('(', ')', '{', '}', '[', ']', ':', '.', ',');
}
