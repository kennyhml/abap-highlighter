import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Display;

import com.sap.rnd.ui.syntaxcoloring.*;
import com.sap.adt.programs.ui.internal.programs.editors.*;
import com.sap.adt.tools.abapsource.ui.sources.editors.IAbapSourceMultiPageEditor;
import com.sap.adt.tools.abapsource.ui.sources.editors.IAbapSourcePage;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.jface.text.presentation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginStartup implements IStartup {

	
	public class CustomScanner extends RuleBasedScanner {
	    public CustomScanner() {
	        // Define token categories
	    	
	        IToken keywordToken = new Token(new TextAttribute(new Color(86, 156, 214)));
	        
	        IToken identifierToken = new Token(new TextAttribute(new Color(156, 220, 254)));

	        
	        setRules(new IRule[] { new AbapKeywordRule() });

	    }
	}
	
	
    private PresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();

        // Configure syntax highlighting for different content types
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new CustomScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        // Add more damagers/repairers for other content types if needed
        return reconciler;
    }
	
	
	private IPartListener2 listener = new IPartListener2() {
		
		@Override
		public void partActivated(org.eclipse.ui.IWorkbenchPartReference partRef) {
	
			 Display.getDefault().asyncExec(() -> {
                 // You can replace this with any action, e.g., a pop-up dialog or a log entry
                 System.out.println("An editor has been opened: " + partRef.getId());
			 });
			 
			if (partRef.getId().startsWith("com.sap.adt.abapClassEditor")) {
                // Display a text message
				
                Display.getDefault().asyncExec(() -> {
                    // You can replace this with any action, e.g., a pop-up dialog or a log entry
                    System.out.println("An abap class editor has been opened: " + partRef.getPartName());
                    
                    // Get the editor instance
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
        // Register your listener during startup
        // ResourcesPlugin.getWorkspace().addResourceChangeListener(new ABAPAnnotationListener());
    }
}
