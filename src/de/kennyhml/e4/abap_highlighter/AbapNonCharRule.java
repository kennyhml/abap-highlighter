package de.kennyhml.e4.abap_highlighter;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

public class AbapNonCharRule extends BaseAbapRule {
	
	private static class NopDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			// Identifiers can only start with letters, underscores or slashes
			return Character.isWhitespace(c) || c == '\n' || c == '\r';
		}

		@Override
		public boolean isWordPart(char c) {
			// Identifiers can only contain letters, digits, underscores and slashes.
			return Character.isWhitespace(c) || c == '\n' || c == '\r';
		}

	}
	
	
	@Override
	public TokenType getTokenType() {
		return fToken.getType();
	}
	
	
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
		fToken.setText(text);
		return fToken;
	}
	
	private AbapToken fToken = new AbapToken(TYPE_COLOR, AbapToken.TokenType.NOP);
	private NopDetector fDetector = new NopDetector();
	private static final Color TYPE_COLOR = new Color(0, 0, 0);
}
