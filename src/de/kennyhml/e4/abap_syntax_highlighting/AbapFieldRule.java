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
	/*
	 * Check if previous token is a field initiator, for example
	 * 
	 * struct-field or table~field
	 * 
	 * Or if the previous token is part of a table key definition, for example
	 * 
	 * ... key connid. Or if its predecessor was part of a table key definition, for
	 * example
	 * 
	 * ... key connid carrid.
	 * 
	 * All the while making sure that its not the alias of a key that is defined
	 * using components, for example
	 * 
	 * ... with non-unique key primary_key components connid carrid ... with
	 * non-unique sorted key secondary_key components connid carrid
	 * 
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		AbapScanner sc = ((AbapScanner) scanner);

		// Yes we are losing performance splitting up the conditions like this but it is
		// honestly
		// negligible and makes it alot less confusing.
		final boolean fieldAccess = sc.tokenMatchesAny(0, TokenType.OPERATOR, fFieldInitiators);
		final boolean isTableDef = sc.hasToken("key") || sc.hasToken("components");
		final boolean keyComponents = isTableDef && (sc.tokenMatchesAny(0, TokenType.KEYWORD, fComponentInitiators)
				|| sc.tokenMatches(0, TokenType.FIELD, "*"));

		if (!fieldAccess && !keyComponents) {
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
			String wordRead = fBuffer.toString();
			
			// read the next word, if it is 'components' then what we just scanned
			// is the alias for the key, not its components.
			int timesRead = 1;
			c = scanner.read();
			while (Character.isWhitespace(c)) {
				c = scanner.read();
				timesRead++;
			}
			fBuffer.setLength(0);
			do {
				fBuffer.append((char) c);
				c = scanner.read();
				timesRead++;
			} while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
			
			String secondRead = fBuffer.toString();
			for (int i = 0; i < timesRead; i++) {
				scanner.unread();
			}
			
			// Next word is components, rewind the scanner completely.
			if (secondRead.equals("components")) {
				((AbapToken) fKeyToken).setAssigned(fBuffer.toString());
				sc.pushToken((AbapToken) fKeyToken);
				return fKeyToken;
			}
			((AbapToken) fFieldToken).setAssigned(fBuffer.toString());
			sc.pushToken((AbapToken) fFieldToken);
			return fFieldToken;
		}
		scanner.unread();
		return Token.UNDEFINED;
	}

	private static final Color FIELD_COLOR = new Color(147, 115, 165);
	private static final Color KEY_COLOR = new Color(149, 98, 181);
	
	private AbapToken fFieldToken = new AbapToken(FIELD_COLOR, TokenType.FIELD);
	private AbapToken fKeyToken = new AbapToken(KEY_COLOR, TokenType.FIELD);
	
	private String[] fFieldInitiators = new String[] { "-", "~" };
	private String[] fComponentInitiators = new String[] { "key", "components" };

	private IWordDetector fDetector = new FieldDetector();
	private StringBuilder fBuffer = new StringBuilder();
}
