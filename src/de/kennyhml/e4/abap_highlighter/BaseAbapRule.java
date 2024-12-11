package de.kennyhml.e4.abap_highlighter;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

public abstract class BaseAbapRule implements IRule {
	
	/**
	 * Thin wrapper for the evaluate method using the AbapScanner instead.
	 * 
	 * @param scanner The scanner managing the rules.
	 * 
	 * @return An AbapToken representing the scanned area.
	 */
	public abstract IToken evaluate(AbapScanner scanner);
	
	@Override
	public final IToken evaluate(ICharacterScanner scanner) {
		IToken res = evaluate((AbapScanner)scanner);
		// Dont need to commit here, the scanner will do that after finding a
		// matching token. The important thing is that we roll back advancements
		// of the rule to make sure all rules start at the same position.
		if (res.isUndefined()) {
			((AbapScanner)scanner).rollback();
		}
		return res;
	}

}
