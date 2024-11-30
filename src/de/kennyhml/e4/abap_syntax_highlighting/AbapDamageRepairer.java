package de.kennyhml.e4.abap_syntax_highlighting;

import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;

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
			int start = event.getOffset();
			int end = start;
			
			// Walk back until we hit a dot
			while (start > 0 && (fDocument.getChar(start) != '.' || start == end)) {
				start -= 1;
			}
			
			// walk forward until we hit the next dot or EOF
			try {				
				do {
					end += 1;
				} while (fDocument.getChar(end) != '.');
			} catch (BadLocationException e) { 
				end -= 1;
			}
			((AbapScanner)fScanner).resetCache();
			return new Region(start, end - start);
		} catch (Exception e) { }
		return super.getDamageRegion(partition, event, documentPartitioningChanged);
	}
}