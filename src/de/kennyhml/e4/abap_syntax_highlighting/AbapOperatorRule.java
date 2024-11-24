package de.kennyhml.e4.abap_syntax_highlighting;
import java.util.Set;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.swt.graphics.Color;

public class AbapOperatorRule extends AbapWordRule {

	private static class AbapOperatorDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return VALID_CHARS.contains(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return VALID_CHARS.contains(c);
		}

		static final Set<Character> VALID_CHARS = Set.of('=', '>', '<', '.', '+', '-');
	}

	public AbapOperatorRule() {
		super(new AbapOperatorDetector());

		for (String op : OPERATORS) {
			this.addWord(op, token);
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

	private static final String[] OPERATORS = { "=>", "->", ".", "-", "+", "-" };

	private static AbapToken token = new AbapToken(new Color(255, 255, 255), AbapToken.TokenType.OPERATOR);
}
