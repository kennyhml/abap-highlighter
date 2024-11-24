package de.kennyhml.e4.abap_syntax_highlighting;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;

public class AbapRuleBasedScanner extends RuleBasedScanner {

	public static AbapToken previousToken;
	
	public AbapRuleBasedScanner() {
		
        setRules(new IRule[] { new AbapCommentRule(), new AbapKeywordRule(), new AbapOperatorRule(), new AbapIdentifierRule(), new AbapStringRule() });
	} 
	
}
