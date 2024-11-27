package de.kennyhml.e4.abap_syntax_highlighting;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class AbapNonCharRule implements IRule {

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		
		int c = scanner.read();
		while (c != ICharacterScanner.EOF && (Character.isWhitespace(c) || c == '\n')) {
			c = scanner.read();
		}
		scanner.unread();
		return Token.UNDEFINED;
	}
}
