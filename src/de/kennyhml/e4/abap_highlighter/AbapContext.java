package de.kennyhml.e4.abap_highlighter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

/**
 * Stores information about the current context.
 * 
 * @warning The scope of context is limited to any context that may occur within
 *          a single statement inside abap. In other words, is is not possible
 *          to track, for example, a class context, as it consists of multiple
 *          abap statements.
 * 
 *          A valid context exists between no more than two statement
 *          terminators (.).
 * 
 *          When leaving a context, the clear() method will be called by the
 *          document scanner.
 */
public class AbapContext {

	public enum ContextFlag {
		CONTEXT_NONE(0), 
		CONTEXT_STRUCT_DECL(2), // begin of # [..context..] end of #.
		CONTEXT_FUNC_DECL(3), // methods [context].
		CONTEXT_FUNC_MULTI_DECL(4), // methods: [context].
		CONTEXT_FMT_STRING(5), // | [context] |.
		CONTEXT_DATA_DECL(6), // data [context].
		CONTEXT_DATA_MULTI_DECL(7); // data: [context]
		public final int flag;

		ContextFlag(int id) {
			this.flag = 1 << id;
		}
	}

	/**
	 * Clears the context, i.e resets all tokens and flags to the initial values.
	 * Should be called after a code block finished repairing.
	 */
	public void clear() {
		fTokens.clear();
		fTokenWords.clear();
		fCtxFlags = 0;
	}

	/**
	 * Checks whether a given context is currently active.
	 * 
	 * @param flag The flag of the context to check.
	 */
	public boolean active(ContextFlag flag) {
		return (fCtxFlags & flag.flag) != 0;
	}

	/**
	 * Activates a given context flag.
	 * 
	 * @param flag The flag to activate because the context was entered.
	 */
	public void activate(ContextFlag flag) {
		fCtxFlags |= flag.flag;
	}

	/**
	 * Deactivates a given context flag.
	 * 
	 * @param flag The flag to deactivate because the context was left.
	 */
	public void deactivate(ContextFlag flag) {
		fCtxFlags &= (~flag.flag);
	}

	/**
	 * Adds a Token to the current context.
	 * 
	 * @param token The token (that was just scanned) to append to the context.
	 */
	public void addToken(AbapToken token) {
		if (token.matches(TokenType.DELIMITER, ".")) {
			clear();
		} else {
			fTokens.add(new AbapToken(token)); // COPY the token, we dont want a reference!!
			fTokenWords.add(token.getText());
		}
	}

	/**
	 * Checks if the current context contains the given word.
	 * 
	 * @param word The word that may occur in the context.
	 * 
	 * @return Whether the given word was found.
	 */
	public boolean hasWord(String word) {
		return fTokenWords.contains(word);
	}

	/**
	 * Checks if the current context contains any of the given words
	 * 
	 * @param words A HashSet of words that may occur in the context.
	 * 
	 * @return Whether any of the given words were found.
	 */
	public boolean hasAnyWord(Set<String> words) {
		for (String word : words) {
			if (fTokenWords.contains(word)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves the requested Token from the current context.
	 * 
	 * @param offsetFromBack offset of the token from the previous word
	 * 
	 * @return The token at the position or null if the position is invalid.
	 */
	public AbapToken getToken(int offsetFromBack) {
		int offset;

		if (offsetFromBack < 0) {
			offset = Math.abs(offsetFromBack) - 1;
		} else {
			offset = ((fTokens.size() - 1) - offsetFromBack);
		}

		try {
			return fTokens.get(offset);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * @return The last token of the context, or null if there are no tokens.
	 */
	public AbapToken getLastToken() {
		return getToken(0);
	}

	/**
	 * Checks if the token at the given offset matches the criteria.
	 * 
	 * @param offsetFromBack Offset from the last token (0 is last).
	 * @param type           The type the token should match.
	 * @param term           The term the token string should match or null.
	 * 
	 * @return Whether the token was matched.
	 */
	public boolean tokenMatches(int offsetFromBack, TokenType type, String term) {
		AbapToken token = getToken(offsetFromBack);
		return token != null && token.matches(type, term);
	}

	/**
	 * Checks if the token at the given offset matches the type.
	 * 
	 * @param offsetFromBack Offset from the last token (0 is last).
	 * @param type           The type the token should match.
	 * 
	 * @return Whether the token was matched.
	 */
	public boolean tokenMatches(int offsetFromBack, TokenType type) {
		return tokenMatches(offsetFromBack, type, null);
	}

	/**
	 * Checks if the last token matches the given type.
	 * 
	 * @param type The type the token should match.
	 * 
	 * @return Whether the token was matched.
	 */
	public boolean lastTokenMatches(TokenType type) {
		return tokenMatches(0, type, null);
	}

	/**
	 * Checks if the last token matches the given type and term.
	 * 
	 * @param type The type the token should match.
	 * @param term The term the token should match
	 * 
	 * @return Whether the token was matched.
	 */
	public boolean lastTokenMatches(TokenType type, String term) {
		return tokenMatches(0, type, term);
	}

	/**
	 * Checks if the token at the given offset matches a given type and any term
	 * 
	 * @param offsetFromBack Offset from the last token (0 is last).
	 * @param type           The type the token should match.
	 * @param terms          A set of terms of which any must match the token.
	 * 
	 * @return Whether the token was matched.
	 */
	public boolean tokenMatchesAny(int offsetFromBack, TokenType type, Set<String> terms) {
		AbapToken token = getToken(offsetFromBack);
		return token != null && token.matchesAny(type, terms);
	}

	/**
	 * Checks if the last token matches a given type and any term
	 * 
	 * @param type  The type the token should match.
	 * @param terms A set of terms of which any must match the token.
	 * 
	 * @return Whether the token was matched.
	 */
	public boolean lastTokenMatchesAny(TokenType type, Set<String> terms) {
		return tokenMatchesAny(0, type, terms);
	}

	private List<AbapToken> fTokens = new ArrayList<>();
	private Set<String> fTokenWords = new HashSet<String>();
	private int fCtxFlags = 0;
}
