package de.kennyhml.e4.abap_syntax_highlighting;

import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.swt.widgets.Display;

import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;

/*
 * Repairer override to choose the region to repair in a way to ensure that enough
 * context is provided while repairing, as the tokenization largely relies on cached token context.
 */
public class AbapDamageRepairer extends DefaultDamagerRepairer {

	public AbapDamageRepairer(AbapScanner scanner) {
		super(scanner);
	}

	@Override
	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event, boolean documentPartitioningChanged) {
		if (documentPartitioningChanged) {
			return partition;
		}

		try {
			// The change occurred at this offset
			int start = findPreviousStatementTerminator(event.getOffset());
			int end = findNextStatementTerminator(event.getOffset());

			Display.getDefault().asyncExec(() -> {
				System.out.println("Change at " + event.getOffset() + ": Repairing from " + start + " to " + end);
			});
			
			((AbapScanner) fScanner).resetCache();
			return new Region(start, end - start);
		} catch (Exception e) {
		}
		return super.getDamageRegion(partition, event, documentPartitioningChanged);
	}
	
	@Override
	public void createPresentation(TextPresentation presentation, ITypedRegion region) {
		super.createPresentation(presentation, region);
	}

	private int findPreviousStatementTerminator(int fromOffset) {
		return findStatementTerminator(fromOffset, true);
	}
	
	private int findNextStatementTerminator(int fromOffset) {
		return findStatementTerminator(fromOffset, false);
	}
	
	private int findStatementTerminator(int fromOffset, boolean previous) {

		int currLine;
		try {
			currLine = fDocument.getLineOfOffset(fromOffset);
		} catch (BadLocationException e) {
			return 0;
		}

		String currLineString = null;
		int offset = 0;
		int length = 0;

		while (currLine > 0) {
			try {
				offset = fDocument.getLineOffset(currLine);
				length = fDocument.getLineLength(currLine);

				// Cut off everything before the change for the first line
				if (!previous && currLineString == null) {
					length = fromOffset - offset;
					offset = fromOffset + 1;
				}
				// The line we also made the change in, dont check past the change.
				else if (previous && fromOffset < offset + length) {
					length = (fromOffset - offset) - 1;
				}
				currLineString = fDocument.get(offset, length);
			} catch (BadLocationException e) {
				break;
			}

			int loc = findDotInLine(currLineString);
			if (loc != -1) {
				return offset + loc;
			}
			
			if (previous) {
				currLine--;
			} else {
				currLine++;
			}
		}
		return 0;
	}
	
	private int findDotInLine(String line) {
		if (line.startsWith("*")) {
			return -1;
		}
		
		char stringStartCharacter = 0;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);

			if (fStringCharacters.contains(c)) {
				if (stringStartCharacter == 0) { 
					stringStartCharacter = c; // String started
					continue;
				}
				if (stringStartCharacter == c) {
					stringStartCharacter = 0; // String ended
					continue;
				}
			} else if (stringStartCharacter != 0) {
				continue; // Inside a string
			}

			// Comments continue for the rest of the line, no point going further
			if (c == '"') { break; }
			if (c == '.') { return i; }
		}
		return -1;
	}

	private Set<Character> fStringCharacters = Set.of('`', '|', '\'');
}