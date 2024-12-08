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
			
			((AbapScanner) fScanner).getContext().clear();
			return new Region(start, end - start);
		} catch (Exception e) {
		}
		return super.getDamageRegion(partition, event, documentPartitioningChanged);
	}
	
	@Override
	public void createPresentation(TextPresentation presentation, ITypedRegion region) {
		
		String str;
		try {
			str = fDocument.get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		super.createPresentation(presentation, region);
	}

	private int findPreviousStatementTerminator(int fromOffset) {
		return findStatementTerminator(fromOffset, true);
	}
	
	private int findNextStatementTerminator(int fromOffset) {
		return findStatementTerminator(fromOffset, false);
	}
	
	private int findStatementTerminator(int changeOffset, boolean previous) {

		int currLine;
		try {
			currLine = fDocument.getLineOfOffset(changeOffset);
		} catch (BadLocationException e) {
			return 0;
		}

		String currLineString = null;
		int lineOffset = 0;
		int length = 0;

		while (currLine > 0) {
			try {
				lineOffset = fDocument.getLineOffset(currLine);
				length = fDocument.getLineLength(currLine);
				currLineString = fDocument.get(lineOffset, length);
			} catch (BadLocationException e) {
				break;
			}

			int changePos = changeOffset - lineOffset;
			int loc = findDotInLine(currLineString, previous ? -1 : changePos, previous ? changePos : -1);
			if (loc != -1) {
				if (!previous) {
					try {
						do {
							loc++;
						} while (fDocument.getChar(lineOffset + loc) != '\n');
					} catch (BadLocationException e) { }
				}
				return lineOffset + loc;
			}
			
			if (previous) {
				currLine--;
			} else {
				currLine++;
			}
		}
		return 0;
	}
	
	private int findDotInLine(String line, int afterIndex, int beforeIndex) {
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
			if (c == '.' && (afterIndex == -1 || i > afterIndex) && (beforeIndex == -1 || i < beforeIndex)) { 
				return i; }
		}
		return -1;
	}

	private Set<Character> fStringCharacters = Set.of('`', '|', '\'');
}