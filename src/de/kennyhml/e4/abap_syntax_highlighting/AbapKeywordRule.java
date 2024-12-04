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
		AbapScanner abapScanner = ((AbapScanner) scanner);

		IToken ret = super.evaluate(scanner);

		// Assign the last word we found to the token
		if (!ret.isUndefined()) {
			((AbapToken) ret).setAssigned(fLastWord);
			abapScanner.pushToken((AbapToken) ret);
		}

		return ret;
	}

	private static final String[] KEYWORDS = { "if", "else", "elseif", "endif", "class", "endclass", "method",
			"endmethod", "methods", "type", "types", "implementation", "definition", "data", "table", "of", "public",
			"private", "protected", "section", "begin", "end", "final", "create", "is", "not", "initial", "and", "or",
			"importing", "exporting", "changing", "returning value", "raising", "receiving", "line", "range", "loop",
			"at", "endloop", "endwhile", "append", "to", "modify", "from", "select", "into", "for", "all", "entries",
			"in", "where", "single", "value", "standard", "ref", "when", "write", "inheriting", "returning",
			"class-methods", "case", "others", "abstract", "assigning", "field-symbol", "new", "try", "catch", "endtry",
			"join", "inner", "outer", "left", " right", "like", "update", "set", "delete", "modify", "no-gaps",
			"condense", "concatenate", "on", "as", "raise", "exception", "constants", "optional", "default", "call",
			"with", "non-unique", "unique", "key", "occurrences", "replace", "then", "switch", "continue", "message",
			"corresponding", "sort", "by", "duplicates", "return", "function", "conv", "" };

	private AbapToken token = new AbapToken(new Color(86, 156, 214), AbapToken.TokenType.KEYWORD);
}
