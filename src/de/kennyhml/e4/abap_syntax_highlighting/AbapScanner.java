package de.kennyhml.e4.abap_syntax_highlighting;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;

public class AbapScanner extends RuleBasedScanner {


	public AbapScanner() {

		setRules(new IRule[] { new AbapNonCharRule(), new AbapCommentRule(), new AbapKeywordRule(), new AbapOperatorRule(),
				new AbapDelimiterRule(), new AbapSubroutineRule(), new AbapIdentifierRule(), new AbapStringRule(), new AbapLiteralRule() });
	}
	
	
	public static boolean tokenMatches(int offsetFromEnd, AbapToken.TokenType type, String term) {
		AbapToken token = getPreviousToken(offsetFromEnd);
		return token != null && token.matches(type, term);
	}
	
	public static boolean tokenMatchesAny(int offsetFromEnd, AbapToken.TokenType type, String[] terms) {
		AbapToken token = getPreviousToken(offsetFromEnd);
		return token != null && token.matchesAny(type, terms);
	}
	
	public static AbapToken getPreviousToken() {
		return getPreviousToken(0);
		
	}
	
	public static AbapToken getPreviousToken(int offsetFromEnd) {
		return previousTokens[4 - offsetFromEnd];
	}
	
	public static void pushToken(AbapToken token) {
		
		// Move all token (besides the first) one to the left
		for (int i = 0; i < previousTokens.length - 1; i++) {
			previousTokens[i] = previousTokens[i + 1];
		}
		
		// Push the new token
		previousTokens[4] = new AbapToken(token);
	}
	
	
	// Store the last 5 tokens
	private static AbapToken[] previousTokens = new AbapToken[5];
	
}
