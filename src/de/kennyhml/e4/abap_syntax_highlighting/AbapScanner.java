package de.kennyhml.e4.abap_syntax_highlighting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;

import de.kennyhml.e4.abap_syntax_highlighting.AbapToken.TokenType;

public class AbapScanner extends RuleBasedScanner {

	public AbapScanner() {

		setRules(new IRule[] { new AbapNonCharRule(),  new AbapCommentRule(), new AbapStringRule(), new AbapKeywordRule(),
				new AbapOperatorRule(), new AbapDelimiterRule(), new AbapFunctionRule(), new AbapFieldRule(),
				new AbapIdentifierRule(), new AbapLiteralRule() });
	}

	public boolean tokenMatches(int offsetFromEnd, TokenType type, String term) {
		AbapToken token = getPreviousToken(offsetFromEnd);
		return token != null && token.matches(type, term);
	}

	public boolean tokenMatchesAny(int offsetFromEnd, TokenType type, String[] terms) {
		AbapToken token = getPreviousToken(offsetFromEnd);
		return token != null && token.matchesAny(type, terms);
	}

	public  AbapToken getPreviousToken() {
		return getPreviousToken(0);

	}
	
	public boolean hasToken(String token) {
		return fPreviousTokenStrings.contains(token);
	}

	public AbapToken getPreviousToken(int offsetFromEnd) {
		try {
			return fPreviousTokens.get((fPreviousTokens.size() - 1) - offsetFromEnd);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public void resetCache() {
		fPreviousTokens.clear();
		fPreviousTokenStrings.clear();
	}

	public void pushToken(AbapToken token) {
		fPreviousTokens.add(new AbapToken(token));
		fPreviousTokenStrings.add(token.getLastAssignment());
	}

	private List<AbapToken>  fPreviousTokens = new ArrayList<>();
	private HashSet<String> fPreviousTokenStrings = new HashSet<String>();
}
