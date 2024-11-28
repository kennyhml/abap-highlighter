package de.kennyhml.e4.abap_syntax_highlighting;

import de.kennyhml.e4.abap_syntax_highlighting.AbapToken.TokenType;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class AbapFieldRule implements IRule {

	private static class FieldDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return Character.isLetter(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isLetterOrDigit(c) || c == '_';
		}
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		
		// previous token must be - (assuming structure field access)
		// Consider using this for access to class fields too? :think:
		// Only makes sense with a way to differentiate them inside classes though
		if (!AbapScanner.tokenMatches(0, TokenType.OPERATOR, "-")) {
			return Token.UNDEFINED;
		}
		
		int c = scanner.read();
		if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)) {

			// read the full world or until EOF
			fBuffer.setLength(0);
			do {
				fBuffer.append((char) c);
				c = scanner.read();
			} while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
			scanner.unread();

			return fFieldToken;
		}
		scanner.unread();
		return Token.UNDEFINED;
	}

	private static final Color FIELD_COLOR = new Color(147, 115, 165);
	
	private AbapToken fFieldToken = new AbapToken(FIELD_COLOR, TokenType.FIELD);

	private IWordDetector fDetector = new FieldDetector();
	private StringBuilder fBuffer = new StringBuilder();
}
