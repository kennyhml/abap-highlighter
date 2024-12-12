package de.kennyhml.e4.abap_highlighter;

import java.util.Set;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import de.kennyhml.e4.abap_highlighter.AbapContext.ContextFlag;
import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

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
		AbapContext ctx = abapScanner.getContext();
		
		// Prevent mistaking an identifer for a keyword token when it is ambiguous, e.g
		// class=>create()
		if (ctx.tokenMatches(0, TokenType.OPERATOR, "*")) {
			return Token.UNDEFINED;
		}
		
		IToken ret = super.evaluate(scanner);

		// Assign the last word we found to the token
		if (!ret.isUndefined()) {
			if (fLastWord.equals("of")) {
				if (ctx.tokenMatches(0, TokenType.KEYWORD, "begin")) {
					ctx.activate(ContextFlag.CONTEXT_STRUCT_DECL);
				} else if (ctx.tokenMatches(0, TokenType.KEYWORD, "end")) {
					ctx.deactivate(ContextFlag.CONTEXT_STRUCT_DECL);
				}
			} 
			
			// This may be multi decl too, that flag will be set by the delimiter rule.
			if (fLastWord.equals("data") || fLastWord.equals("class-data")) {
				ctx.activate(ContextFlag.CONTEXT_DATA_DECL);
			}
			
			((AbapToken) ret).setText(fLastWord);
			ctx.addToken((AbapToken) ret);
		}

		return ret;
	}

	private static final String[] KEYWORDS = { "if", "else", "elseif", "endif", "class", "endclass", "method",
			"endmethod", "methods", "type", "types", "implementation", "definition", "data", "table", "of", "public",
			"private", "protected", "section", "begin", "end", "final", "create", "is", "not", "initial", "and", "or",
			"importing", "exporting", "changing", "returning value", "raising", "receiving", "line", "range", "loop",
			"at", "endloop", "endwhile", "append", "appending", "fields", "to", "modify", "from", "select", "into", "for", "all", "entries",
			"in", "where", "single", "value", "standard", "ref", "when", "write", "inheriting", "returning",
			"class-methods", "class-data", "case", "others", "abstract", "assigning", "field-symbol", "new", "try",
			"catch", "endtry", "join", "inner", "outer", "left", " right", "like", "update", "set", "delete", "modify",
			"no-gaps", "condense", "concatenate", "on", "as", "raise", "exception", "constants", "optional", "default",
			"call", "with", "non-unique", "unique", "key", "occurrences", "replace", "then", "switch", "continue",
			"message", "corresponding", "sort", "by", "duplicates", "return", "function", "conv", "exceptions",
			"reference", "preferred", "parameter", "length", "decimals", "empty", "components", "sorted", "hashed",
			"seperated", "character", "mode", "respecting", "blanks", "byte"};

	private AbapToken token = new AbapToken(new Color(86, 156, 214), AbapToken.TokenType.KEYWORD);
}
