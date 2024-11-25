package de.kennyhml.e4.abap_syntax_highlighting;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import java.util.List;

import org.eclipse.jface.text.IDocument;

import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.swt.widgets.Display;

import com.sap.adt.tools.abapsource.ui.sources.editors.IAbapSourceMultiPageEditor;
import com.sap.adt.tools.abapsource.ui.sources.editors.IAbapSourcePage;

public class PluginStartup implements IStartup {


    private PresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new AbapRuleBasedScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        return reconciler;
    }
	
	
	private IPartListener2 listener = new IPartListener2() {
		
		@Override
		public void partActivated(org.eclipse.ui.IWorkbenchPartReference partRef) {
	
			 Display.getDefault().asyncExec(() -> {
                 System.out.println("An editor has been opened: " + partRef.getId());
			 });
			 
			if (partRef.getId().startsWith("com.sap.adt.abapClassEditor")) {
                Display.getDefault().asyncExec(() -> {
                    System.out.println("An abap class editor has been opened: " + partRef.getPartName());
                    
                    IWorkbenchPart part = partRef.getPart(false);
                    if (part instanceof IAbapSourceMultiPageEditor) {
                    	IAbapSourceMultiPageEditor editor = (IAbapSourceMultiPageEditor) part;
                    	List<IAbapSourcePage> pages = editor.getLoadedPages();
                    	
                    	if (pages.size() > 0) {
                    		PresentationReconciler conc = pages.get(0).getExistingPresentationReconciler();
                    		
                    		IPresentationRepairer repairer = conc.getRepairer(IDocument.DEFAULT_CONTENT_TYPE);
                    		
                    		conc.uninstall();
                    
                    		ISourceViewer viewer = pages.get(0).getViewer();
                    		PresentationReconciler recon = getPresentationReconciler(viewer);
                    		recon.install(viewer);
                    		viewer.invalidateTextPresentation();
                    	}
                    }
                });
            }
			
		};
	};
	
	
	
    @Override
    public void earlyStartup() {
    	PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
    		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(listener);
        });
    }
}
