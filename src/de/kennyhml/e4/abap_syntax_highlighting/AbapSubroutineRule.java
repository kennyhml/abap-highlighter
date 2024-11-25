package de.kennyhml.e4.abap_syntax_highlighting;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import de.kennyhml.e4.abap_syntax_highlighting.AbapToken.TokenType;

public class AbapSubroutineRule implements IRule {

	private static class SubroutineDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return Character.isLetter(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isLetterOrDigit(c) || c == '_';
		}

	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {

		AbapToken prevToken = AbapRuleBasedScanner.previousToken;
		// The previous token must either be ->, => or a method/methods keyword.
		// Technically this currently doesnt allow for methods defined using methods:
		// (which is inherently bad practice) as the : will be considered the last
		// token.
		// For this reason it may be useful to keep track of the previous x tokens
		// instead.
		if (prevToken != null) {
			AbapToken.TokenType prevType = prevToken.getAbapType();
			String prevWord = prevToken.getLastAssignment();

			if (prevType == TokenType.OPERATOR || prevType == TokenType.KEYWORD
					&& (prevWord.equals("->") || prevWord.equals("=>") || prevWord.contains("method"))) {

				int c = scanner.read();
				if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)) {

					// read the full world or until EOF
					fBuffer.setLength(0);
					do {
						fBuffer.append((char) c);
						c = scanner.read();
					} while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
					scanner.unread();

					// function call must end identifier with (, otherwise it could be type /
					// variable access.
					if (c == '(' || prevWord.contains("method")) {
						return subroutineToken;
					}

					// Not a function call, rewind the scanner
					for (int i = fBuffer.length() - 1; i >= 0; i--)
						scanner.unread();
					return Token.UNDEFINED;
				}
				scanner.unread();
			}
		}
		return Token.UNDEFINED;
	}

	private static final Color SUBROUTINE_COLOR = new Color(220, 220, 170);
	private AbapToken subroutineToken = new AbapToken(SUBROUTINE_COLOR, AbapToken.TokenType.SUBROUTINE);

	private IWordDetector fDetector = new SubroutineDetector();
	private StringBuilder fBuffer = new StringBuilder();
}
