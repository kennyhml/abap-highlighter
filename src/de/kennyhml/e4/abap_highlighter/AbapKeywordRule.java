package de.kennyhml.e4.abap_highlighter;

import de.kennyhml.e4.abap_highlighter.context.ContextFlag;
import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

import java.util.Map;
import java.util.Set;

import static de.kennyhml.e4.abap_highlighter.AbapToken.TokenType.*;

import java.util.*;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

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

		private static final Set<Character> fKeywordTerminators = Set.of(' ', '\r', '\n', '.', '(', '>', '!');
	}

	/**
	 * @brief Defines a possible completion for a keyword
	 */
	private static class KeywordCompletion {
		public KeywordCompletion(String relation, Set<TokenType> upcomingTokenType) {
			if (relation == null) {
				this.text = null;
			} else {
				this.text = Arrays.asList(relation.split(" "));
			}
			this.upcomingTokenType = upcomingTokenType;
		}

		public KeywordCompletion(String relation, TokenType upcomingTokenType) {

			this(relation, Set.of(upcomingTokenType));
		}

		public KeywordCompletion(String relation) {
			this(relation, Set.of());
		}

		public List<String> text;
		public Set<TokenType> upcomingTokenType;
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

		final KeywordCompletion completed = resolveRelatedKeywords(scanner, text);
		if (completed != null) {
			if (completed.text != null) {
				fToken.setText(text + completed.text);
			} else {
				fToken.setText(text);
			}
			ctx.setNextPossibleTokens(completed.upcomingTokenType);
			
			final String test = text;
			Display.getDefault().asyncExec(() -> {
				System.out.println("Completion of  " + test + ": " + completed.text);
			});
		} else {
			fToken.setText(text);
			ctx.addToken(fToken);
			ctx.setNextPossibleTokens(Set.of());
		}

		ctx.addToken(fToken);
		checkContextChanged(ctx);
		// Set next possible token type based on the keyword (if known)
		return fToken;
	}

	private void checkContextChanged(AbapContext ctx) {

		String lastWord = ctx.getLastToken().getText();
		String withoutMod = lastWord.replaceAll(":", "");

		// Check for types: begin of ... end of.
		if (ctx.hasWord("types:") && lastWord.equals("of")) {
			if (ctx.tokenMatches(1, KEYWORD, "begin")) {
				ctx.activate(ContextFlag.STRUCT_DECL);
			} else if (ctx.tokenMatches(1, KEYWORD, "end")) {
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

	/**
	 * @brief Checks whether the upcoming tokens after the scanned keyword belong to
	 *        the same expression. For example, after scanning the keyword "ref", it
	 *        is possible for it to group with "ref to".
	 */
	private KeywordCompletion resolveRelatedKeywords(AbapScanner scanner, String text) {

		// We can already commit the current word here since we are sure this is
		// at least a keyword. That way we can easily roll back here.
		scanner.commit();
		String ret = text;
		
		List<KeywordCompletion> completions = null;
		
		for (Map.Entry<Set<String>, List<KeywordCompletion>> entry : fKeywordCompletions.entrySet()) {
			if (entry.getKey().contains(text)) {
				completions = entry.getValue();
				break;
			}
		}
		
		// No relations defined for this keyword.
		if (completions == null) {
			return null;
		}
		
		int maxWords = 0;
		int wordCount = 0;
		for (KeywordCompletion comp : completions) {
			if (comp.text != null && comp.text.size() > maxWords) {
				maxWords = comp.text.size();
			}
		}

		// there is no completion for this, only types are provided.
		if (maxWords == 0) {
			return completions.get(0);
		}
		
		// Keep track of each completion whether it has matched up so far
		List<Boolean> tracking = new ArrayList<>(Collections.nCopies(completions.size(), true));
		List<Integer> matched = new ArrayList<>(Collections.nCopies(completions.size(), 0));
		
		while (wordCount < maxWords && tracking.contains(true)) {
			// Skip whitespaces
			while (Character.isWhitespace(scanner.peek())) {
				scanner.read();
				ret += " ";
			}
			
			// check the different continuations
			String nextWord = scanner.peekNext(fDetector);
			
			// The word isnt a keyword at all, no relation found.
			if (!fKeywords.contains(nextWord)) {
				break;
			}
			scanner.readNext(fDetector);
			
			// Check each completion we are still tracking whether its next word
			// matches with the word we scanned. If it does remember how many times
			// that completion matched. The completion that matched fully and the longest
			// will be chosen in the end.
			for (int i = 0; i < completions.size(); i++) {
				if (!tracking.get(i) || completions.get(i).text == null) {continue;}
				
				// Get the word this completion would next expect
				String expecting = "";
				try {
					expecting = completions.get(i).text.get(wordCount);
				} catch (IndexOutOfBoundsException e) {
				}
				
				// Stop tracking if there is no next word for this completion or it didnt match
				if (expecting.isEmpty() || !expecting.equals(nextWord)) {
					tracking.set(i, false); // stop tracking the completion
					continue;
				}
				
				matched.set(i, wordCount + 1);
			}
			ret += nextWord;
			wordCount++;
		};
		
		Integer longest = 0;
		KeywordCompletion match = null;
		for (int i = 0; i < completions.size(); i++) {
			int matchedWords = matched.get(i);
			
			// words matched and full completion dont match up, cant be this one
			
			if (matchedWords == 0) {
				if (completions.get(i).text != null) {
					continue;
				}
			} else if(matchedWords != completions.get(i).text.size()) {
				continue;
			}
			
			if (matchedWords > longest || match == null) {
				match = completions.get(i);
				longest = matchedWords;
			}
		}
		
		if (match == null) {
			scanner.rollback();
		}
		return match;
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

	private static Map<Set<String>, List<KeywordCompletion>> fKeywordCompletions = Map.ofEntries(
			
			// could be followed by pretty much anything..
			Map.entry(Set.of("if", "and", "or", "not"),
					List.of(new KeywordCompletion(null))),
			
			// type ref to..., ref itself is also fully qualified to get an object
			// reference, e.g ref #( obj ).
			Map.entry(Set.of("ref"), 
					List.of(new KeywordCompletion("to", TYPE_IDENTIFIER))),

			
			Map.entry(Set.of("class", "interface"), 
					List.of(new KeywordCompletion(null, TYPE_IDENTIFIER))),
			
			// raising resumable(exception), optional, raising itself is fully qualified
			Map.entry(Set.of("raising"),
					List.of(new KeywordCompletion("resumable", DELIMITER),
							new KeywordCompletion(null, TYPE_IDENTIFIER))),

			// could be "type ref to", but also "types: begin of.."
			Map.entry(Set.of("type", "types:"),
					List.of(new KeywordCompletion(null, Set.of(TYPE_IDENTIFIER, KEYWORD)))),
			
			Map.entry(Set.of("types"),
					List.of(new KeywordCompletion(null, Set.of(TYPE_IDENTIFIER)))),
			
			// "begin of mytype" ... "end of mytype"..
			Map.entry(Set.of("begin", "end"), 
					List.of(new KeywordCompletion("of", Set.of(TYPE_IDENTIFIER)))),
			
			// Could be "value some_type( )" but also "data x type i value 123", so type or literal
 			Map.entry(Set.of("value"), 
					List.of(new KeywordCompletion(null, Set.of(TYPE_IDENTIFIER, STRING, LITERAL, IDENTIFIER, DELIMITER)))),
			
			
			// All of these keywords guarantee a type identifier following them
			Map.entry(Set.of("new", "catch", "conv", "cond", "corresponding", "reduce"), 
					List.of(new KeywordCompletion(null, Set.of(TYPE_IDENTIFIER)))),
			
			Map.entry(Set.of("preferred"), 
					List.of(new KeywordCompletion("parameter", IDENTIFIER))),
			
			Map.entry(Set.of("parameters", "parameters:"), 
					List.of(new KeywordCompletion(null, IDENTIFIER))),
			
			Map.entry(Set.of("methods", "methods:"), 
					List.of(new KeywordCompletion(null, FUNCTION))),

			
			Map.entry(Set.of("importing", "exporting", "returning", "changing"),
					List.of(new KeywordCompletion(null, Set.of(IDENTIFIER, OPERATOR, KEYWORD)))
					),
			
			Map.entry(Set.of("reference"),
					List.of(new KeywordCompletion(null, Set.of(KEYWORD, DELIMITER)))
					),
			// type table of..
			Map.entry(Set.of("table"), 
					List.of(new KeywordCompletion("of", TokenType.TYPE_IDENTIFIER))));

	private static Color KEYWORD_COLOR = new Color(86, 156, 214);

	private AbapToken fToken = new AbapToken(KEYWORD_COLOR, AbapToken.TokenType.KEYWORD);

	private AbapKeywordDetector fDetector = new AbapKeywordDetector();

}
