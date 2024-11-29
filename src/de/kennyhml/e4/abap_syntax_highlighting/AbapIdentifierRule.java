package de.kennyhml.e4.abap_syntax_highlighting;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.swt.graphics.Color;

import de.kennyhml.e4.abap_syntax_highlighting.AbapToken.TokenType;

public class AbapIdentifierRule extends AbapRegexWordRule {

	private static class AbapIdentifierDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			// Identifiers can only start with letters or underscore
			return Character.isLetter(c) || c == '_' || c == '/';
		}

		@Override
		public boolean isWordPart(char c) {
			// Identifiers can only contain letters, digit and underscores.
			return Character.isLetterOrDigit(c) || c == '_' || c == '/';
		}

	}

	public AbapIdentifierRule() {
		super(new AbapIdentifierDetector());

		this.addWord("^[a-zA-Z_\\/][a-zA-Z0-9_\\/]*$", genericToken);
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		IToken ret = super.evaluate(scanner);

		// Assign the last word we found to the token
		if (!ret.isUndefined()) {
			((AbapToken) ret).setAssigned(fLastWord);

			// Check if previous token leads to a type, `ref to` are two tokens and need
			// seperate checking. Only checking `to` would lead to mismatches.
			if (AbapScanner.tokenMatchesAny(0, TokenType.KEYWORD, fTypeReferences)
					|| (AbapScanner.tokenMatches(0, TokenType.KEYWORD, "to")
							&& AbapScanner.tokenMatches(1, TokenType.KEYWORD, "ref"))) {
				ret = typeToken;
			}

			AbapScanner.pushToken((AbapToken) ret);
		}
		return ret;
	}

	private static final Color GENERIC_COLOR = new Color(156, 220, 254);
	private static final Color TYPE_COLOR = new Color(78, 201, 176);

	private AbapToken genericToken = new AbapToken(GENERIC_COLOR, AbapToken.TokenType.IDENTIFIER);
	private AbapToken typeToken = new AbapToken(TYPE_COLOR, AbapToken.TokenType.IDENTIFIER);

	private static String[] fTypeReferences = new String[] { "type", "raising", "class", "new" };
}
