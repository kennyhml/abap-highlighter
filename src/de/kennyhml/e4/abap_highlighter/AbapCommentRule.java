package de.kennyhml.e4.abap_highlighter;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import de.kennyhml.e4.abap_highlighter.AbapToken.TokenType;
import de.kennyhml.e4.abap_highlighter.context.ContextFlag;

public class AbapCommentRule extends BaseAbapRule {

	private static class CommentDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return c == '"';
		}

		@Override
		public boolean isWordPart(char c) {
			return c != '\n';
		}
	}
	
	
	@Override
	public boolean isPossibleInContext(AbapContext ctx) {
		return !ctx.active(ContextFlag.FMT_STRING);
	}
	
	@Override
	public IToken evaluate(AbapScanner scanner) {
		
		int c = scanner.read();
		if (c != ICharacterScanner.EOF && isCommentStart(scanner.getColumn(), c)) {
			// Read until end of line, it is not possible for a comment to end before that.
			String text = scanner.readNext(c, fDetector);
			commentToken.setText(text);
			return commentToken;
		}
		return Token.UNDEFINED;
	}

	
	private boolean isCommentStart(int column, int c) {
		return ((column == 1 && c == '*') || c == '"');
	}
	
	private static final Color COMMENT_COLOR = new Color(87, 166, 74);

	private AbapToken commentToken = new AbapToken(COMMENT_COLOR, TokenType.COMMENT);
	CommentDetector fDetector = new CommentDetector();
}
