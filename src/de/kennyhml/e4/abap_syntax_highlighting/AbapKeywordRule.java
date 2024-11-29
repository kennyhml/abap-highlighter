package de.kennyhml.e4.abap_syntax_highlighting;

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
			return !Character.isWhitespace(c) && c != '\n' && c != '.' && c != '(' && c != ':';
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
			AbapScanner.pushToken((AbapToken) ret);
		}

		return ret;
	}

	private static final String[] KEYWORDS = { "if", "else", "elseif", "endif", "class", "endclass", "method",
			"endmethod", "methods", "type", "types", "implementation", "definition", "data", "table", "of", "public",
			"private", "protected", "section", "begin", "end", "final", "create", "is", "not", "initial", "and", "or",
			"importing", "exporting", "changing", "returning value", "raising", "receiving", "line", "range", "loop",
			"at", "endloop", "endwhile", "append", "to", "modify", "from", "select", "into", "for", "all", "entries",
			"in", "where", "single", "value", "standard", "ref", "when", "write", "inheriting", "returning",
			"class-methods", "case", "others", "abstract", "assigning", "field-symbol", "new", "try", "catch", "endtry" };

	private AbapToken token = new AbapToken(new Color(86, 156, 214), AbapToken.TokenType.KEYWORD);
}
