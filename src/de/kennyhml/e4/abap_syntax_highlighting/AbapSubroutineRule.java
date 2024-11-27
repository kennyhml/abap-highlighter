package de.kennyhml.e4.abap_syntax_highlighting;

import de.kennyhml.e4.abap_syntax_highlighting.AbapToken.TokenType;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class AbapSubroutineRule implements IRule {

	private static class SubroutineDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return Character.isLetter(c) || Character.isWhitespace(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isLetterOrDigit(c) || c == '_';
		}
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		// Decls are not followed by parantheses, so we dont need to walk to them.
		boolean isDeclaration = false;

		// Check for previous `methods` keyword, for example:
		// methods foo.
		// Or for previous `method` keyword (from method, endmethod block), for example:
		// method foo.
		if (AbapScanner.tokenMatchesAny(0, TokenType.KEYWORD, callDeclarations)) {
			isDeclaration = true;
		}
		// Check if previous token is `:` or `,` for example:
		// methods: foo,
		//			bar,
		//			baz.
		else if (AbapScanner.tokenMatchesAny(0, TokenType.DELIMITER, callDelimiters)) {
			isDeclaration |= (AbapScanner.tokenMatches(1, TokenType.KEYWORD, "methods")
					|| AbapScanner.tokenMatches(1, TokenType.SUBROUTINE, "*"));
		}

		// Check if previous token is `->` or `=>` from non decl call, for example:
		// lo_app->foo( ).
		if (!isDeclaration && !AbapScanner.tokenMatchesAny(0, TokenType.OPERATOR, callOperators)) {
			return Token.UNDEFINED;
		}

		int c = scanner.read();
		if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)) {

			// read the full world or until EOF
			fBuffer.setLength(0);
			do {
				fBuffer.append((char) c);
				c = scanner.read();
			} while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
			scanner.unread();

			// call must have parantheses to differentiate from var / type access
			// declarations do not have the parantheses.
			if (c == '(' || isDeclaration) {
				subroutineToken.setAssigned(fBuffer.toString());
				AbapScanner.pushToken(subroutineToken);
				return subroutineToken;
			}

			// Not a function call, rewind the scanner
			for (int i = fBuffer.length() - 1; i >= 0; i--)
				scanner.unread();
			return Token.UNDEFINED;
		}
		scanner.unread();
		return Token.UNDEFINED;
	}

	private static String[] callDelimiters = new String[] { ":", "," };
	private static String[] callOperators = new String[] { "->", "=>" };
	private static String[] callDeclarations = new String[] { "methods", "method"};
	
	private static final Color SUBROUTINE_COLOR = new Color(220, 220, 170);
	private AbapToken subroutineToken = new AbapToken(SUBROUTINE_COLOR, AbapToken.TokenType.SUBROUTINE);

	private IWordDetector fDetector = new SubroutineDetector();
	private StringBuilder fBuffer = new StringBuilder();
}
