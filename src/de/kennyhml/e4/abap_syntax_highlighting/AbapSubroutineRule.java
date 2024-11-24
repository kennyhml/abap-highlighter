package de.kennyhml.e4.abap_syntax_highlighting;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;

public class AbapSubroutineRule implements IRule {

	public AbapSubroutineRule(IToken token) {
		fToken = token;
	}

	private static class SubroutineDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return c == '=' || c == '-';
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isLetterOrDigit(c) || c == '_' || c == '>';
		}

	}
	
	@Override
	public IToken evaluate(ICharacterScanner scanner) {

		int c = scanner.read();
		if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)) {

			// read the full world or until EOF
			fBuffer.setLength(0);
			do {
				fBuffer.append((char) c);
				c = scanner.read();
			} while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
			scanner.unread();

			return fToken;
		}
		scanner.unread();
		return Token.UNDEFINED;
	}
	
	
	private IToken fToken;
	private IWordDetector fDetector = new SubroutineDetector();
	private StringBuilder fBuffer = new StringBuilder();
}

