package de.kennyhml.e4.abap_highlighter;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;

public class AbapScanner extends RuleBasedScanner {

	public AbapScanner() {
		setRules(fRules);
	}

	/**
	 * Commits the scanner position advancements that have been made since the 
	 * last commit, they can no longer be rolled back afterwards.
	 */
	public void commit() {
		fCommittedOffset = fOffset;
	}
	
	/**
	 * Rolls the scanner position back to what it was at the time of the previous
	 * commit.
	 */
	public void rollback() {
		fOffset = fCommittedOffset;
	}
	
	@Override
	public int read() {
		fReadCount++;
		return super.read();
	}
	
	@Override
	public IToken nextToken() {
		IToken ret = super.nextToken();
		commit();
		return ret;
	}
	
	@Override
	public void setRange(IDocument document, int offset, int length) {
		super.setRange(document, offset, length);
		fCommittedOffset = offset;
	}
	
	/**
	 * Gets the context of the scanner, this context may be used by rules in order
	 * to make highlighting decisions based on what has been scanned previously and
	 * is available in the context.
	 * 
	 * @return The current context of the scanner.
	 */
	public AbapContext getContext() {
		return fContext;
	}
	
	/**
	 * Reads the next word (if available), the scanner is rewinded back to it's original
	 * position afterwards. Commit and rollback are not used.
	 * 
	 * @return The next word if available.
	 */
	public String peekNext(IWordDetector detector) {
		fReadCount = 0;
		StringBuilder buffer = new StringBuilder();
		int c;
		
		// Skip whitespaces as needed
		do {
			c = read();
		} while (Character.isWhitespace(c));
		
		
		if (detector.isWordStart((char)c)) {
			do {
				buffer.append((char) c);
				c = read();
			} while (c != ICharacterScanner.EOF && detector.isWordPart((char) c));
		}
		
		
		String read = buffer.toString();
		for (int i = 0; i < fReadCount; i++) {
			unread();
		}
		return read;
	}
	
	public String readNext(int c, IWordDetector detector) {
		StringBuilder buffer = new StringBuilder();		
		do {
			buffer.append((char) c);
			c = read();
		} while (c != ICharacterScanner.EOF && detector.isWordPart((char)c));
		unread();
		return buffer.toString();
	}

	private AbapContext fContext = new AbapContext();

	// The order of the rule matters!!!
	private IRule[] fRules = new IRule[] { new AbapNonCharRule(), new AbapCommentRule(), new AbapStringRule(),
			new AbapKeywordRule(), new AbapOperatorRule(), new AbapDelimiterRule(), new AbapFunctionRule(),
			new AbapFieldRule(), new AbapIdentifierRule(), new AbapLiteralRule() };

	
	private int fCommittedOffset = 0;
	private int fReadCount = 0;
	
	
}
