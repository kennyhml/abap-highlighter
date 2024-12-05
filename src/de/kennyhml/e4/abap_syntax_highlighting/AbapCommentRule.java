package de.kennyhml.e4.abap_syntax_highlighting;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import de.kennyhml.e4.abap_syntax_highlighting.AbapToken.TokenType;

public class AbapCommentRule implements IRule {

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		if (((AbapScanner)scanner).tokenMatches(0, TokenType.DELIMITER, "}")) {
			return Token.UNDEFINED;
		}
		int c = scanner.read();
		if (c != ICharacterScanner.EOF && ((scanner.getColumn() == 1 && c == '*') || c == '"')) {

			fBuffer.setLength(0);
			do {
				fBuffer.append((char) c);
				c = scanner.read();
			} while (c != ICharacterScanner.EOF && c != '\n');
			scanner.unread();

			((AbapToken) commentToken).setAssigned(fBuffer.toString());
			
			return commentToken;

		}

		scanner.unread();
		return Token.UNDEFINED;
	}

	protected StringBuilder fBuffer = new StringBuilder();

	private static final Color COMMENT_COLOR = new Color(87, 166, 74);

	private AbapToken commentToken = new AbapToken(COMMENT_COLOR, AbapToken.TokenType.COMMENT);
}
