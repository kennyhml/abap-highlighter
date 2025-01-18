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

		private static final Set<Character> fKeywordTerminators = Set.of(' ', '\r', '\n', '.', '(', '>');
	}

	@Override
	public boolean isPossibleInContext(AbapContext ctx) {
		// referencing a class or instance member, impossible to be a keyword.
		if (ctx.lastTokenMatchesAny(TokenType.OPERATOR, Set.of("=>", "->"))) {
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

		if (text == null) {
			return Token.UNDEFINED;
		}

		// Special case for handling 'me' keyword as the dash is part of alot of
		// other keywords and cannot simply be filtered out.
		if (text.equals("me-")) {
			scanner.unread();
			text = text.replace("-", "");
		}

		if (!fKeywords.contains(text) && !fKeywords.contains(text.replace(":", ""))) {
			return Token.UNDEFINED;
		}

		fToken.setText(text);
		ctx.addToken(fToken);
		checkContextChanged(ctx);
		
		// Set next possible token type based on the keyword (if known)
		
		boolean found = false;
		for (Map.Entry<Set<String>, Set<TokenType>> entry : fSuccessorTypes.entrySet()) {
			if (entry.getKey().contains(text)) {
				ctx.setNextPossibleTokens(entry.getValue());
				found = true;
				break;
			}
		}
		if (!found) {
			ctx.setNextPossibleTokens(Set.of());
		}
		return fToken;
	}

	public void checkContextChanged(AbapContext ctx) {

		String lastWord = ctx.getLastToken().getText();
		String withoutMod = lastWord.replaceAll(":", "");

		// Check for types: begin of ... end of.
		if (ctx.hasWord("types:") && lastWord.equals("of")) {
			if (ctx.tokenMatches(1, TokenType.KEYWORD, "begin")) {
				ctx.activate(ContextFlag.STRUCT_DECL);
			} else if (ctx.tokenMatches(1, TokenType.KEYWORD, "end")) {
				ctx.deactivate(ContextFlag.STRUCT_DECL);
			}
		} else if (fDataContextActivators.contains(lastWord)) {
			ctx.activate(ContextFlag.DATA_DECL);
		} else if (fFuncContextActivators.contains(lastWord)) {
			ctx.activate(ContextFlag.FN_DECL);
		}
		// Also check if the last word with the ':' removed fits.
		else if (fDataContextActivators.contains(withoutMod)) {
			ctx.activate(ContextFlag.DATA_MULTI_DECL);
		} else if (fFuncContextActivators.contains(withoutMod)) {
			ctx.activate(ContextFlag.FN_MULTI_DECL);
		}
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

	
	private static Set<TokenType> fNotKeyword = Set.of(TokenType.LITERAL, TokenType.STRING, TokenType.KEYWORD, TokenType.IDENTIFIER, TokenType.TYPE_IDENTIFIER, TokenType.FUNCTION);
	
	
	// Map each keyword to the type of token that could follow afterwards
	private static Map<Set<String>, Set<TokenType>> fSuccessorTypes = Map.ofEntries(
			Map.entry(Set.of("if", "and", "or", "not"), Set.of(TokenType.KEYWORD, TokenType.IDENTIFIER, TokenType.TYPE_IDENTIFIER)),
			
			// Comparisons
			Map.entry(Set.of("eq", "ne", "lt", "gt", "le", "ge", "co", "cn", "ca", "na", "cs", "ns", "cp", "np"), fNotKeyword),
			
			Map.entry(Set.of("raising"), Set.of(TokenType.TYPE_IDENTIFIER)),
			Map.entry(Set.of("type", "raising", "class", "new", "catch", "of", "conv", "value", "types", "cond", "corresponding", "reduce"), Set.of(TokenType.TYPE_IDENTIFIER, TokenType.KEYWORD)),
			Map.entry(Set.of("class"), Set.of(TokenType.TYPE_IDENTIFIER)), 
			Map.entry(Set.of("parameters", "parameters:"), Set.of(TokenType.TYPE_IDENTIFIER)),
			Map.entry(Set.of("methods", "methods:"), Set.of(TokenType.FUNCTION)));

	private static Color KEYWORD_COLOR = new Color(86, 156, 214);

	private AbapToken fToken = new AbapToken(KEYWORD_COLOR, AbapToken.TokenType.KEYWORD);

	private AbapKeywordDetector fDetector = new AbapKeywordDetector();

}
