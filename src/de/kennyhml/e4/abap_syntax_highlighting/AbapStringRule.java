package de.kennyhml.e4.abap_syntax_highlighting;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import de.kennyhml.e4.abap_syntax_highlighting.AbapToken.TokenType;

public class AbapStringRule implements IRule {

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int c = scanner.read();

		if (c != ICharacterScanner.EOF && (isStringStart((char) c) || previousTokenWasEmbeddedVariable())) {
			fBuffer.setLength(0);
			do {
				fBuffer.append((char) c);
				if (!Character.isWhitespace(c)) {
					previousChar = (char) c;
				}
				c = scanner.read();
			} while (c != ICharacterScanner.EOF && !isStringEndOrInterrupt((char) c));
			scanner.unread();
			String read = fBuffer.toString();

			((AbapToken) stringToken).setAssigned(fBuffer.toString());
			AbapScanner.pushToken((AbapToken) stringToken);
			return stringToken;
		}

		scanner.unread();
		return Token.UNDEFINED;
	}

	protected boolean isStringStart(char c) {
		return c == '|' || c == '\'';
	}

	protected boolean isStringEndOrInterrupt(char c) {
		return c == '\n' || ((previousChar == '\'' || previousChar == '|') && (c == '.' || c == ')')) || c == '{';
	}

	protected boolean previousTokenWasEmbeddedVariable() {
		return AbapScanner.tokenMatches(0, TokenType.DELIMITER, "}");
	}

	protected StringBuilder fBuffer = new StringBuilder();
	protected char previousChar = ' ';

	private static final Color STRING_COLOR = new Color(224, 122, 0);
	private AbapToken stringToken = new AbapToken(STRING_COLOR, AbapToken.TokenType.STRING);
}
