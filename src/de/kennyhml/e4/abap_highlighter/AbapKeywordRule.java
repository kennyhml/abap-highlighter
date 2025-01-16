package de.kennyhml.e4.abap_highlighter;

import de.kennyhml.e4.abap_highlighter.context.ContextFlag;
import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

import java.util.Map;
import java.util.Set;
import java.util.*;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class AbapKeywordRule extends BaseAbapRule {

	private static class AbapKeywordDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return Character.isLetter(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return !fKeywordTerminators.contains((char) c);
		}

		private static final Set<Character> fKeywordTerminators = Set.of(' ', '\r', '\n', '.', '(', ':', '>');
	}

	@Override
	public boolean isPossibleInContext(AbapContext ctx) {
		// referencing a class or instance member, impossible to be a keyword.
		if (ctx.lastTokenMatchesAny(TokenType.OPERATOR, Set.of("=>", "->"))) {
			return false;
		}

		// Function implementation body, the upcoming token has to be a function name.
		if (ctx.lastTokenMatches(TokenType.KEYWORD, "method")) {
			return false;
		}

		return true;
	}

	@Override
	public TokenType getTokenType() {
		return fToken.getType();
	}

	@Override
	public IToken evaluate(AbapScanner scanner) {
		AbapContext ctx = scanner.getContext();

		int c = scanner.peek();
		if (c == AbapScanner.EOF || !fDetector.isWordStart((char) c)) {
			return Token.UNDEFINED;
		}

		// Remember that we dont need to manually unread the word because the
		// scanner will rollback the advanced characters if the token is undefined!
		String text = scanner.readNext(fDetector);

		text = checkFunctionSignature(text, scanner);

		if (text == null) {
			return Token.UNDEFINED;
		}

		fToken.setText(text);
		ctx.addToken(fToken);
		return fToken;

//		// Special case for handling 'me' keyword as the dash is part of alot of
//		// other keywords and cannot simply be filtered out.
//		if (text.equals("me-")) {
//			scanner.unread();
//			text = text.substring(0, 2); 
//		}
//		
//		if (!fKeywords.contains(text)) {
//			return Token.UNDEFINED;
//		}
//
//		if (ctx.hasWord("types") && text.equals("of")) {
//			if (ctx.lastTokenMatches(TokenType.KEYWORD, "begin")) {
//				ctx.activate(ContextFlag.STRUCT_DECL);
//			} else if (ctx.lastTokenMatches(TokenType.KEYWORD, "end")) {
//				ctx.deactivate(ContextFlag.STRUCT_DECL);
//			}
//		} else if (fDataContextActivators.contains(text)) {
//			// This may be multi declaration too, that flag will be set by delimiter rule.
//			ctx.activate(ContextFlag.DATA_DECL);
//		} else if (fFuncContextActivators.contains(text)) {
//			// Could also be multi decl
//			ctx.activate(ContextFlag.FN_DECL);
//		}
//
//		fToken.setText(text);
//		ctx.addToken(fKeywordToken);
//		return fKeywordToken;
	}

	private String checkClassSignature(String text, AbapScanner scanner) {
		AbapContext ctx = scanner.getContext();

		if (fVisibilityKeywords.contains(text)) {
			String next = scanner.peekNext(fDetector);
			if (ctx.isEmpty() && next.equals("section")) {
				// section declaration
				return text + next;
			}
		}

		if (fClassSignatureInitiators.contains(text)) {
			if (text.equals("class")) {
				ctx.setNextPossibleTokens(Set.of(TokenType.TYPE_IDENTIFIER));
			}

			return text;
		} else if (ctx.isEmpty()) {
			return text;
		}

		return text;
	}

	private String checkFunctionSignature(String text, AbapScanner scanner) {
		AbapContext ctx = scanner.getContext();

		// Not in a function declaration
		if (!ctx.isEmpty() && !ctx.active(ContextFlag.FN_DECL)) {
			return text;
		}

		// If its a function declaration the next keyword has to be the name of the
		// function
		if (fFunctionDecl.contains(text)) {
			ctx.setNextPossibleTokens(Set.of(TokenType.FUNCTION));
			return text;
		} else if (ctx.isEmpty()) {
			return text;
		} // Has to be a declaration if there is no context

		// The next token has to be an identifier after, for example, "importing" ...
		if (fParameterSections.contains(text)) {
			ctx.setNextPossibleTokens(Set.of(TokenType.IDENTIFIER));
			return text;
		}

		if (fParamMetadata.contains(text)) {
			return text;
		}

		return text;
	}

	private static final Set<String> fDataContextActivators = Set.of("data", "class-data", "parameters");
	private static final Set<String> fFuncContextActivators = Set.of("methods", "class-methods");

	private static final Set<String> fKeywords = Set.of("if", "else", "elseif", "endif", "class", "endclass", "method",
			"endmethod", "methods", "type", "types", "implementation", "definition", "data", "table", "of", "public",
			"private", "protected", "section", "begin", "end", "final", "create", "is", "not", "initial", "and", "or",
			"importing", "exporting", "changing", "raising", "receiving", "line", "range", "loop", "at", "endloop",
			"endwhile", "append", "appending", "fields", "to", "from", "select", "into", "for", "all", "entries", "in",
			"where", "single", "value", "standard", "ref", "when", "write", "inheriting", "returning", "class-methods",
			"class-data", "case", "others", "abstract", "assigning", "field-symbol", "new", "try", "catch", "endtry",
			"join", "inner", "outer", "left", " right", "like", "update", "set", "delete", "modify", "no-gaps",
			"condense", "concatenate", "on", "as", "raise", "exception", "constants", "optional", "default", "call",
			"with", "non-unique", "unique", "key", "occurrences", "replace", "then", "switch", "continue", "message",
			"corresponding", "sort", "by", "duplicates", "return", "function", "conv", "exceptions", "reference",
			"preferred", "parameter", "length", "decimals", "empty", "components", "sorted", "hashed", "separated",
			"character", "mode", "respecting", "blanks", "byte", "include", "initialization", "start-of-selection",
			"report", "selection-screen", "parameters", "lower", "obligatory", "select-options", "block", "frame",
			"title", "intervals", "no", "starting", "visible", "checkbox", "user-command", "radiobutton", "group",
			"listbox", "modif", "id", "screen", "split", "cond", "reduce", "init", "next", "move-corresponding",
			"supplied", "insert", "authority-check", "object", "field", "clear", "do", "enddo", "eq", "ne", "lt", "gt",
			"le", "ge", "co", "cn", "ca", "na", "cs", "ns", "cp", "np", "me", "endcase", "assign", "field-symbols",
			"base", "check", "get", "time", "stamp", "commit", "work", "search", "assigned", "exit", "move", "read",
			"transporting", "convert", "date", "zone", "times", "bypassing", "buffer", "component", "distinct",
			"requested", "testing", "duration", "short", "risk", "level", "harmless", "local", "global", "friends",
			"interfaces", "renaming", "suffix", "structure", "resumable", "read-only", "interface", "endinterface");

	private static final Set<String> fVisibilityKeywords = Set.of("public", "protected", "private");

	private static final Set<String> fClassSignatureInitiators = Set.of("class", "private", "public", "section",
			"global", "local", "friends");

	private static final Set<String> fParameterSections = Set.of("importing", "exporting", "changing", "returning",
			"raising", "receiving", "exceptions"

	);

	private static final Set<String> fFunctionDecl = Set.of("methods", "class-methods");

	private static final Set<String> fParamMetadata = Set.of("type", "like", "value", "reference", "ref", "to",
			"default", "optional", "preferred", "parameter");

	
	// Map each keyword to its possible continatuations, but only consider words that actually start the 
	// keyword chain. For example, the keyword "with" can chain with "with non-unique key" or "with default key"
	// etc. But the word "non-unique" can only chain with "non-unique key" which is already covered by "with".
	private static final Map<String, Set<String>> fKeywordChains = Map.ofEntries(
			Map.entry("ref", Set.of("to")),
			Map.entry("with", Set.of("default key", "unique key", "non-unique key"))
	);

	// Keywords that can exist on their own
	private static final Set<String> fullyQualifiedKeywords = Set.of(
			"if", "else", "elseif", "endif", "endloop", "do", "times", "enddo", "while", "endwhile");

	private static Color KEYWORD_COLOR = new Color(86, 156, 214);

	private AbapToken fToken = new AbapToken(KEYWORD_COLOR, AbapToken.TokenType.KEYWORD);

	private AbapKeywordDetector fDetector = new AbapKeywordDetector();

}
