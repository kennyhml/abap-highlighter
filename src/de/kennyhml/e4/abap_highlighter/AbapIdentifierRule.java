package de.kennyhml.e4.abap_highlighter;
import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;

import java.util.Set;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class AbapIdentifierRule extends BaseAbapRule {

	private static class IdentifierDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			// Identifiers can only start with letters, underscores or slashes
			return Character.isLetter(c) || c == '_' || c == '/';
		}

		@Override
		public boolean isWordPart(char c) {
			// Identifiers can only contain letters, digits, underscores and slashes.
			return Character.isLetterOrDigit(c) || c == '_' || c == '/';
		}

	}

	@Override
	public boolean isPossibleInContext(AbapContext ctx) {
		// identifiers are pretty much valid in every context as we also dont
		// currently differentiate between type and variable identifiers..
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
		if (c == ICharacterScanner.EOF || !fDetector.isWordStart((char) c)) {
			return Token.UNDEFINED;
		}

		String text = scanner.readNext(fDetector);

		fToken.setText(text);
		ctx.addToken(fToken);
		ctx.setNextPossibleTokens(Set.of());
		return fToken;
	}


	private AbapToken fToken = new AbapToken(GENERIC_COLOR, AbapToken.TokenType.IDENTIFIER);

	private IdentifierDetector fDetector = new IdentifierDetector();

	private static final Color GENERIC_COLOR = new Color(156, 220, 254);
}
