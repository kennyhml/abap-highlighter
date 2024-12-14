package de.kennyhml.e4.abap_highlighter;

import java.util.Set;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class AbapOperatorRule extends BaseAbapRule {

	@Override
	public IToken evaluate(AbapScanner scanner) {
		int c = scanner.read();
		if (c == AbapScanner.EOF || !fOperators.contains((char)c)) {
			return Token.UNDEFINED;
		}
		
		// The '/' symbol is technically valid in identifier contexts, but may also be used
		// for operator context during division or newlines on writing, in which case it should
		// have a whitespace coming up after it.
		if (c == '/') {
			if (!Character.isWhitespace(scanner.read())) {
				return Token.UNDEFINED;
			}
			scanner.unread();
		}
		
		fOperatorToken.setText(Character.toString(c));
		scanner.getContext().addToken(fOperatorToken);
		return fOperatorToken;
	}

	private static final Set<Character> fOperators = Set.of('=', '>', '<', '+', '-', '~', '/');
	private static AbapToken fOperatorToken = new AbapToken(new Color(255, 255, 255), AbapToken.TokenType.OPERATOR);
}
