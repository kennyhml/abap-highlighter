package de.kennyhml.e4.abap_highlighter;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

public abstract class BaseAbapRule implements IRule {
	
	/**
	 * Thin wrapper for the evaluate method using the AbapScanner instead.
	 * 
	 * @param scanner The scanner managing the rules.
	 * 
	 * @return An AbapToken representing the scanned area.
	 */
	public abstract IToken evaluate(AbapScanner scanner);
	
	/**
	 * Abstract method a rule must implement to check whether the token type
	 * the rule determines is even possible in the current context.
	 * 
	 * For example, if the previous token is an '=>', then the only tokens
	 * that could follow are function call and identifier tokens.
	 * 
	 * @param ctx The current context
	 * 
	 * @return Whether the rules token can occur based on the context.
	 */
	public abstract boolean isPossibleInContext(AbapContext ctx);
	
	/**
	 * @return The type of token that this scanner can return.
	 */
	public abstract TokenType getTokenType();
	
	@Override
	public final IToken evaluate(ICharacterScanner scanner) {
		if (!isPossibleInContext(((AbapScanner)scanner).getContext())) {
			return Token.UNDEFINED;
		}
		
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
