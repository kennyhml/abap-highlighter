import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class AbapKeywordRule extends AbapWordRule {

	private static class AbapKeywordDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return Character.isLetter(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isLetter(c);
		}

	}

	public AbapKeywordRule() {
		super(new AbapKeywordDetector(), Token.UNDEFINED, true);

		for (String keyword : KEYWORDS) {
			this.addWord(keyword, token);
		}
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		IToken ret = super.evaluate(scanner);

		// Assign the last word we found to the token
		if (!ret.isUndefined()) {
			((AbapToken) ret).setAssigned(fLastWord);
		}
		
		return ret;
	}

	private static final String[] KEYWORDS = { "if", "else", "elseif", "endif", "class", "endclass", "method",
			"endmethod", "methods", "type", "implementation", "definition", "data" };

	private AbapToken token = new AbapToken(new TextAttribute(new Color(86, 156, 214)), AbapToken.TokenType.KEYWORD);
}
