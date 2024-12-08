package de.kennyhml.e4.abap_syntax_highlighting;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import de.kennyhml.e4.abap_syntax_highlighting.AbapToken.TokenType;

public class AbapStringRule implements IRule {

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		AbapScanner abapScanner = ((AbapScanner)scanner);
		int c = scanner.read();
		if (c == ICharacterScanner.EOF) {
			scanner.unread();
			return Token.UNDEFINED;
		}
		char chr = (char)c;
		boolean stringStarting = false;
		boolean stringContinuing = false;
		
		if (previousTokenWasEmbeddedVariable(abapScanner)) {
			if ((char)c == getLastSymbol()) {
				((AbapToken) stringToken).setText(Character.toString(symbolStack.removeLast()));
				abapScanner.getContext().addToken((AbapToken) stringToken);
				return stringToken;
			}
			stringContinuing = true;
		} else if (isStringStart((char)c)) {
			stringStarting = true;
			symbolStack.add((char)c);
		}
		
		if (stringStarting || stringContinuing) {
			fBuffer.setLength(0);
			char previousChar = 0;
			do {
				fBuffer.append((char) c);
				previousChar = (char)c;
				c = scanner.read();
			} while (c != ICharacterScanner.EOF && (previousChar == '\\' || !isStringEndOrInterrupt((char) c) ));
			if (!isStringStart((char)c)) {
				scanner.unread();
			} else {
				fBuffer.append((char)c);
				symbolStack.removeLast();
			}
			
			((AbapToken) stringToken).setText(fBuffer.toString());
			abapScanner.getContext().addToken((AbapToken) stringToken);
			return stringToken;
		}

		scanner.unread();
		return Token.UNDEFINED;
	}

	
	char getLastSymbol() {
		try {
			return symbolStack.getLast();
		} catch (NoSuchElementException e) {
			return 0;
		}

	}
	
	protected boolean isStringStart(char c) {
		return c == '|' || c == '\'' || c == '`';
	}

	protected boolean isStringEndOrInterrupt(char c) {
		return c == '\n' || c == symbolStack.getLast() || c == '{';
	}

	protected boolean previousTokenWasEmbeddedVariable(AbapScanner scanner) {
		return scanner.getContext().tokenMatches(0, TokenType.DELIMITER, "}");
	}

	protected StringBuilder fBuffer = new StringBuilder();
	
	protected List<Character> symbolStack = new ArrayList<>();
	
	private static final Color STRING_COLOR = new Color(224, 122, 0);
	private AbapToken stringToken = new AbapToken(STRING_COLOR, AbapToken.TokenType.STRING);
}
