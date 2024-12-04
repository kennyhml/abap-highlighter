package de.kennyhml.e4.abap_syntax_highlighting;

import de.kennyhml.e4.abap_syntax_highlighting.AbapToken.TokenType;

import java.util.Set;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class AbapFunctionRule implements IRule {

	private static class FunctionDetector implements IWordDetector {

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
		AbapScanner abapScanner = ((AbapScanner) scanner);

		// Decls are not followed by parantheses, so we dont need to walk to them.
		boolean isDeclaration = false;
		boolean mightBeDeclaration = false;

		// Check for some keywords that initiate a call-like sequence, such as
		// Class initialization: 
		//		new z_class( ).
		// Type casts: 
		// 		conv z_custom_Type( lv_value ).
		// Inline assignments: 
		//		data(ls_struct) = value struct_type( field1 = 'X' ... ).
		// As their token must be a type instead.
		if (abapScanner.tokenMatchesAny(0, TokenType.KEYWORD, fTypeInitiators)) {
			return Token.UNDEFINED;
		}

		// Check for previous `methods` keyword, for example:
		// methods foo.
		// Or for previous `method` keyword (from method, endmethod block), for example:
		// method foo.
		if (abapScanner.tokenMatchesAny(0, TokenType.KEYWORD, fCallDeclarations)) {
			isDeclaration = true;
		}
		// Check if previous token is `:` or `,` for example:
		// methods: foo,
		// bar,
		// baz.
		else if (abapScanner.tokenMatchesAny(0, TokenType.DELIMITER, fCallDelimiters)) {
			mightBeDeclaration = true;
			// Check if the token before `:` or `,` is a METHODS keyword or a function token
			// This wont work if the functions defined also have parameters though.
			isDeclaration |= (abapScanner.tokenMatchesAny(1, TokenType.KEYWORD, fCallDeclarations)
					|| abapScanner.tokenMatches(1, TokenType.FUNCTION_CALL, "*"));
		}

		// If its not a declaration we must walk to the end of the word in either cause
		// as it
		// could either be `lo_app->foo()` or just `foo()` if called internally.

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
			String read = fBuffer.toString();
			if (c == '(' || isDeclaration || (mightBeDeclaration && checkIsMultiDeclaration(scanner))) {
				fSubroutineToken.setAssigned(read);
				abapScanner.pushToken(fSubroutineToken);
				return fSubroutineToken;
			}

			// Not a function call, rewind the scanner
			for (int i = fBuffer.length() - 1; i >= 0; i--)
				scanner.unread();
			return Token.UNDEFINED;
		}
		scanner.unread();
		return Token.UNDEFINED;
	}

	private boolean checkIsMultiDeclaration(ICharacterScanner scanner) {

		if (!((AbapScanner) scanner).hasToken("methods")) {
			return false;
		}

		StringBuilder buffer = new StringBuilder();
		int c = scanner.read();
		int times_read = 1;

		boolean ret = false;

		// Mutli declared method with no parameters terminating the methods statement
		// or initiating another one.
		if (c == '.' || c == ',') {
			scanner.unread();
			return true;
		}

		// If the following character is a whitespace or a newline, make sure to skip it
		while (Character.isWhitespace(c) || c == '\n') {
			c = scanner.read();
			times_read++;
		}

		// Read the term that follows and check if belongs to a function signature
		if (Character.isLetter(c)) {
			do {
				buffer.append((char) c);
				c = scanner.read();
				times_read++;
			} while (c != ICharacterScanner.EOF && Character.isLetter(c));

			String kw = buffer.toString();
			ret = fSignatureInitiators.contains(kw);
		}

		// Unread the word since we dont want to tokenize it
		for (int i = 0; i < times_read; i++) {
			scanner.unread();
		}
		return ret;
	}

	private static String[] fCallDelimiters = new String[] { ":", "," };
	private static String[] fTypeInitiators = new String[] { "new", "value", "conv" };
	private static String[] fCallDeclarations = new String[] { "methods", "method", "class-methods" };

	private static Set<String> fSignatureInitiators = Set.of("importing", "returning", "raising", "changing",
			"exporting");

	private static final Color SUBROUTINE_COLOR = new Color(220, 220, 170);

	private AbapToken fSubroutineToken = new AbapToken(SUBROUTINE_COLOR, TokenType.FUNCTION_CALL);

	private IWordDetector fDetector = new FunctionDetector();
	private StringBuilder fBuffer = new StringBuilder();
}
