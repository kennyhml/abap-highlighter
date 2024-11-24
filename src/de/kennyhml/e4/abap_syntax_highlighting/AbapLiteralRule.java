package de.kennyhml.e4.abap_syntax_highlighting;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.swt.graphics.Color;

public class AbapLiteralRule extends AbapRegexWordRule {

	private static class AbapLiteralDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return Character.isDigit(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isDigit(c);
		}

	}

	public AbapLiteralRule() {
		super(new AbapLiteralDetector());
		
		this.addWord("^\\d+$", literalToken);
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


	private static final Color LITERAL_COLOR = new Color(181, 206, 168);
	private AbapToken literalToken = new AbapToken(LITERAL_COLOR, AbapToken.TokenType.LITERAL);
}
