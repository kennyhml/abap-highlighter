package de.kennyhml.e4.abap_highlighter;

import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

import java.util.Set;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;


public class AbapIntegerRule extends BaseAbapRule {

	private static class AbapIntegerDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return Character.isDigit(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isDigit(c);
		}
	}

	/** Check if an integer literal is possible in the current context.
	 * 
	 * Integer literals are pretty easy to distinguish since they start with
	 * a number and have only numbers in them, its not really necessary to
	 * base it on the context as of now. They also have a ton of things that
	 * could lead to an integer literals (assignments, comparisons, math operations..)
	 */
	@Override
	public boolean isPossibleInContext(AbapContext ctx) {
		return true;
	}
	
	
	@Override
	public IToken evaluate(AbapScanner scanner) {
		
		int c = scanner.peek();
		if (c == AbapScanner.EOF || !fDetector.isWordStart((char)c)) {
			return Token.UNDEFINED;
		}
		
		String text = scanner.readNext(fDetector);
		
		fIntegerToken.setText(text);
		scanner.getContext().addToken(fIntegerToken);
		return fIntegerToken;
	}

	private static final Color LITERAL_COLOR = new Color(181, 206, 168);
	private AbapToken fIntegerToken = new AbapToken(LITERAL_COLOR, TokenType.LITERAL);
	private AbapIntegerDetector fDetector = new AbapIntegerDetector();
}
