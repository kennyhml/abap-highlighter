package de.kennyhml.e4.abap_syntax_highlighting;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class AbapToken extends Token {

	public AbapToken(Color color, TokenType type) {
		super(new TextAttribute(color));
		fAbapType = type;

	}

	public AbapToken(AbapToken other) {
		super(other.getData());
		fAbapType = other.fAbapType;
		fLastAssignment = other.fLastAssignment;
	}

	public boolean matchesAny(TokenType type, String[] termsOrSymbols) {
		for (String termOrSymbol : termsOrSymbols) {
			if (matches(type, termOrSymbol)) {
				return true;
			}
		}
		return false;
	}

	public boolean matches(TokenType type, String termOrSymbol) {
		return type == fAbapType && (termOrSymbol == "*" || fLastAssignment.equals(termOrSymbol));
	}

	public TokenType getAbapType() {
		return fAbapType;
	}

	public String getLastAssignment() {
		return fLastAssignment;
	}

	public void setAssigned(String assignedTo) {
		fLastAssignment = assignedTo;
	}

	public static enum TokenType {
		IDENTIFIER, KEYWORD, OPERATOR, SEPERATOR, DELIMITER, COMMENT, STRING, LITERAL, FUNCTION_CALL
	};

	// The type of this token
	protected TokenType fAbapType;

	// The string this token was last assigned to
	protected String fLastAssignment = "";
}
