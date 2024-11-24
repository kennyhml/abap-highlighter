package de.kennyhml.e4.abap_syntax_highlighting;
import java.util.Set;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.swt.graphics.Color;

public class AbapDelimiterRule extends AbapWordRule {

	private static class AbapDelimiterDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return DELIMITERS.contains(c);
		}

		@Override
		public boolean isWordPart(char c) {
			// delimiters cannot be longer than 1 character
			return false;
		}
	}

	public AbapDelimiterRule() {
		super(new AbapDelimiterDetector());

		for (char op : DELIMITERS) {
			this.addWord(Character.toString(op), token);
		}
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		IToken ret = super.evaluate(scanner);

		// Assign the last word we found to the token
		if (!ret.isUndefined()) {
			((AbapToken) ret).setAssigned(fLastWord);
			AbapRuleBasedScanner.previousToken = (AbapToken)ret;
		}
		return ret;
	}

	static final Set<Character> DELIMITERS = Set.of('(', ')', '{', '}', '[', ']');
	
	private static AbapToken token = new AbapToken(new Color(255, 255, 255), AbapToken.TokenType.DELIMITER);
}
