package de.kennyhml.e4.abap_highlighter;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;

import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

public class AbapRegexWordRule extends AbapWordRule {

	public AbapRegexWordRule(IWordDetector detector) {
		super(detector);
	};

	@Override
	public IToken evaluate(ICharacterScanner scanner) {

		int c = scanner.read();
		if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)
				&& (fColumn == UNDEFINED || fColumn == scanner.getColumn() - 1)) {

			// read the full world or until EOF
			fBuffer.setLength(0);
			do {
				fBuffer.append((char) c);
				c = scanner.read();
			} while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
			scanner.unread();

			fLastWord = fBuffer.toString();

			// Check each pattern on the word.
			for (Map.Entry<Pattern, IToken> entry : fPatterns.entrySet()) {
				if (entry.getKey().matcher(fLastWord).matches()) {
					return entry.getValue();
				}
			}

			// return the scanner to its prior state
			if (fDefaultToken.isUndefined()) {
				unreadBuffer(scanner);
			}

			return fDefaultToken;
		}
		scanner.unread();
		return Token.UNDEFINED;
	}
	
	@Override
	public void addWord(String pattern, IToken token) {
		fPatterns.put(Pattern.compile(pattern), token);
	}

	protected Map<Pattern, IToken> fPatterns = new HashMap<>();
}
