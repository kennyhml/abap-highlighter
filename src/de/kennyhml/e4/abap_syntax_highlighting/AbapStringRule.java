package de.kennyhml.e4.abap_syntax_highlighting;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class AbapStringRule implements IRule {

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int c = scanner.read();

		if (c != ICharacterScanner.EOF && (isStringStart((char) c) || previousTokenWasEmbeddedVariable())) {
			fBuffer.setLength(0);
			do {
				fBuffer.append((char) c);
				c = scanner.read();
			} while (c != ICharacterScanner.EOF && !isStringEndOrInterrupt((char) c));
			scanner.unread();

			((AbapToken) stringToken).setAssigned(fBuffer.toString());
			return stringToken;
		}

		scanner.unread();
		return Token.UNDEFINED;
	}

	protected boolean isStringStart(char c) {
		return c == '|' || c == '\'';
	}

	protected boolean isStringEndOrInterrupt(char c) {
		return c == '\n' || c == '.' || c == '{';
	}

	protected boolean previousTokenWasEmbeddedVariable() {
		AbapToken prev = AbapRuleBasedScanner.previousToken;
		return prev != null && prev.getAbapType() == AbapToken.TokenType.DELIMITER
				&& prev.getLastAssignment().equals("}");

	}

	protected StringBuilder fBuffer = new StringBuilder();

	private static final Color STRING_COLOR = new Color(224, 122, 0);
	private AbapToken stringToken = new AbapToken(STRING_COLOR, AbapToken.TokenType.STRING);
}
