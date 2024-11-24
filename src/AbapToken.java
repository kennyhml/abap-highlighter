import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.Token;

public class AbapToken extends Token {

	public AbapToken(TextAttribute text, TokenType type) {
		super(text);
		fType = type;

	}

	public TokenType getType() {
		return fType;
	}
	
	public String getLastAssignment() {
		return fLastAssignment;
	}

	public void setAssigned(String assignedTo) {
		fLastAssignment = assignedTo;
	}
	
	public static enum TokenType {
		IDENTIFIER, KEYWORD, OPERATOR, SEPERATOR, DELIMITER
	};

	// The type of this token
	protected TokenType fType;
	
	// The string this token was last assigned to
	protected String fLastAssignment;
}
