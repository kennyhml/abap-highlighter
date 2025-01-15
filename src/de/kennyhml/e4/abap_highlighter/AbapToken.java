package de.kennyhml.e4.abap_highlighter;

import java.util.Set;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class AbapToken extends Token {
	
	public static enum TokenType {
		IDENTIFIER, 
		KEYWORD, 
		OPERATOR, 
		SEPERATOR, 
		DELIMITER, 
		COMMENT, 
		STRING, 
		LITERAL, 
		FUNCTION, 
		FIELD,
		KEY,
		TYPE_IDENTIFIER
	};
	
	/**
	 * Constructs a token with a color of a certain type.
	 * 
	 * @param color The color to display this token with
	 * @param type The type this token represents.
	 */
	public AbapToken(Color color, TokenType type) {
		super(new TextAttribute(color));
		fType = type;

	}

	/**
	 * Copy constructor
	 * 
	 * @param other The token to copy
	 */
	public AbapToken(AbapToken other) {
		super(other.getData());
		fType = other.fType;
		fText = other.fText;
	}
	/**
	 * Checks whether the token matches the given type and terms
	 * 
	 * @param type The type the token should match
	 * @param terms The terms of which any should match the token
	 * 
	 * @return Whether a match exists
	 */
	public boolean matchesAny(TokenType type, Set<String> terms) {
	    return fType == type && terms.contains(fText);
	}

	/**
	 * Checks whetheer the given type and term match the token
	 * 
	 * @param type The type the token should match
	 * @param term The term the token should match
	 * 
	 * @return Whether the data matched
	 */
	public boolean matches(TokenType type, String term) {
		
		return type == fType && (term == null || fText.equalsIgnoreCase(term));
	}

	/**
	 * @return The type of the token (e.g Delimiter, Operator, Keyword..)
	 */
	public TokenType getType() {
		return fType;
	}

	/**
	 * @return The text of the token (e.g "DATA", "Z_MY_CLASS", "|An abap string|") 
	 */
	public String getText() {
		return fText;
	}

	/**
	 * Sets the text of the token to the given string
	 * 
	 * @param string The text this token was assigned to.
	 */
	public void setText(String string) {
		fText = string;
	}

	// The type of this token
	protected TokenType fType;

	// The string this token was last assigned to
	protected String fText;
}
