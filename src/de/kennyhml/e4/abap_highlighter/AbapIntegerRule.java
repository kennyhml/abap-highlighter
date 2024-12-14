package de.kennyhml.e4.abap_highlighter;

import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

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

	@Override
	public IToken evaluate(AbapScanner scanner) {
		
		int c = scanner.read();
		if (c == AbapScanner.EOF || !fDetector.isWordStart((char)c)) {
			return Token.UNDEFINED;
		}
		
		String text = scanner.readNext(c, fDetector);
		
		fIntegerToken.setText(text);
		scanner.getContext().addToken(fIntegerToken);
		return fIntegerToken;
	}

	private static final Color LITERAL_COLOR = new Color(181, 206, 168);
	private AbapToken fIntegerToken = new AbapToken(LITERAL_COLOR, TokenType.LITERAL);
	private AbapIntegerDetector fDetector = new AbapIntegerDetector();
}
