package de.kennyhml.e4.abap_highlighter;

import java.util.Set;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import de.kennyhml.e4.abap_highlighter.AbapContext.ContextFlag;
import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

public class AbapKeywordRule extends BaseAbapRule {

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

	@Override
	public IToken evaluate(AbapScanner scanner) {
		AbapContext ctx = scanner.getContext();

		// Prevent mistaking an identifer for a keyword token when it is ambiguous, e.g
		// class=>create()
		if (ctx.tokenMatches(0, TokenType.OPERATOR)) {
			return Token.UNDEFINED;
		}

		int c = scanner.read();
		if (c == AbapScanner.EOF || !fDetector.isWordStart((char)c)) {
			return Token.UNDEFINED;
		}
		
		// Remember thatwe dont need to manually unread the word because the 
		// scanner will rollback the advanced characters if the token is undefined!
		String text = scanner.readNext(c, fDetector);
		if (!fKeywords.contains(text)) {
			return Token.UNDEFINED;
		}
		
		if (ctx.hasWord("types") && text.equals("of")) {
			if (ctx.lastTokenMatches(TokenType.KEYWORD, "begin")) {
				ctx.activate(ContextFlag.CONTEXT_STRUCT_DECL);
			} else if (ctx.lastTokenMatches(TokenType.KEYWORD, "end")) {
				ctx.deactivate(ContextFlag.CONTEXT_STRUCT_DECL);
			}
		} 
		else if (fDataContextActivators.contains(text)) {
			// This may be multi declaration too, that flag will be set by the delimiter rule.
			ctx.activate(ContextFlag.CONTEXT_DATA_DECL);
		}
		else if (fFuncContextActivators.contains(text)) {
			// Could also be multi decl
			ctx.activate(ContextFlag.CONTEXT_FUNC_DECL);
		}
		
		fKeywordToken.setText(text);
		ctx.addToken(fKeywordToken);
		return fKeywordToken;
	}

	private static final Set<String> fDataContextActivators = Set.of("data", "class-data", "parameters");
	private static final Set<String> fFuncContextActivators = Set.of("methods", "class-methods");

	private static final Set<String> fKeywords = Set.of("if", "else", "elseif", "endif", "class", "endclass", "method",
			"endmethod", "methods", "type", "types", "implementation", "definition", "data", "table", "of", "public",
			"private", "protected", "section", "begin", "end", "final", "create", "is", "not", "initial", "and", "or",
			"importing", "exporting", "changing", "returning value", "raising", "receiving", "line", "range", "loop",
			"at", "endloop", "endwhile", "append", "appending", "fields", "to", "from", "select", "into",
			"for", "all", "entries", "in", "where", "single", "value", "standard", "ref", "when", "write", "inheriting",
			"returning", "class-methods", "class-data", "case", "others", "abstract", "assigning", "field-symbol",
			"new", "try", "catch", "endtry", "join", "inner", "outer", "left", " right", "like", "update", "set",
			"delete", "modify", "no-gaps", "condense", "concatenate", "on", "as", "raise", "exception", "constants",
			"optional", "default", "call", "with", "non-unique", "unique", "key", "occurrences", "replace", "then",
			"switch", "continue", "message", "corresponding", "sort", "by", "duplicates", "return", "function", "conv",
			"exceptions", "reference", "preferred", "parameter", "length", "decimals", "empty", "components", "sorted",
			"hashed", "seperated", "character", "mode", "respecting", "blanks", "byte", "include", "initialization",
			"start-of-selection", "report", "selection-screen", "parameters", "lower", "obligatory", "select-options",
			"block", "frame", "title", "intervals", "no", "starting", "visible", "checkbox", "user-command", "radiobutton",
			"group", "listbox", "modif", "id", "screen");

	private static Color KEYWORD_COLOR = new Color(86, 156, 214);
	private AbapToken fKeywordToken = new AbapToken(KEYWORD_COLOR, AbapToken.TokenType.KEYWORD);
	
	private AbapKeywordDetector fDetector = new AbapKeywordDetector();
	}
