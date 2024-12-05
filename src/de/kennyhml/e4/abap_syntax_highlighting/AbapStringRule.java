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
		AbapScanner abapScanner = ((AbapScanner)scanner);
		int c = scanner.read();

		if (c != ICharacterScanner.EOF && (isStringStart((char) c) || previousTokenWasEmbeddedVariable(abapScanner))) {
			if (isStringStart((char)c)) {
				startCharacter = (char)c;
			}

			// Consider a string like |{ iv_value }|. For the parser it looks like both a start and
			// a continuation when in reality we should be terminating the string here.
			if (isStringStart((char)c) && previousTokenWasEmbeddedVariable(abapScanner)) {
				((AbapToken) stringToken).setAssigned("|");
				abapScanner.pushToken((AbapToken) stringToken);
				return stringToken;
			}
			
			fBuffer.setLength(0);
			do {
				fBuffer.append((char) c);
				if (!Character.isWhitespace(c)) {
					previousChar = (char) c;
				}
				c = scanner.read();
			} while (c != ICharacterScanner.EOF && !isStringEndOrInterrupt((char) c, startCharacter));
			if (c != '\'' && c != '|') {
				scanner.unread();
			} else {
				fBuffer.append((char)c);
			}
			
			((AbapToken) stringToken).setAssigned(fBuffer.toString());
			abapScanner.pushToken((AbapToken) stringToken);
			return stringToken;
		}

		scanner.unread();
		return Token.UNDEFINED;
	}

	protected boolean isStringStart(char c) {
		return c == '|' || c == '\'';
	}

	protected boolean isStringEndOrInterrupt(char c, char startWith) {
		return c == '\n' || c == startWith || c == '{';
	}

	protected boolean previousTokenWasEmbeddedVariable(AbapScanner scanner) {
		return scanner.tokenMatches(0, TokenType.DELIMITER, "}");
	}

	protected StringBuilder fBuffer = new StringBuilder();
	protected char previousChar = ' ';
	protected char startCharacter = 0;
	
	private static final Color STRING_COLOR = new Color(224, 122, 0);
	private AbapToken stringToken = new AbapToken(STRING_COLOR, AbapToken.TokenType.STRING);
}
