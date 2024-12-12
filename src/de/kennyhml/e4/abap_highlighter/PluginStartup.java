package de.kennyhml.e4.abap_highlighter;

import com.sap.adt.tools.abapsource.ui.sources.editors.IAbapSourceMultiPageEditor;
import com.sap.adt.tools.abapsource.ui.sources.editors.IAbapSourcePage;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.presentation.PresentationReconciler;

public class PluginStartup implements IStartup {

	@Override
	public void earlyStartup() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {

			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			for (IEditorReference ref : window.getActivePage().getEditorReferences()) {
				addPageListener(ref.getPart(false));
			}

			window.getPartService().addPartListener(partListener);
		});
	}

	@SuppressWarnings("restriction")
	private void addPageListener(IWorkbenchPart part) {
		if (!(part instanceof IAbapSourceMultiPageEditor)) {
			return;
		}

		IAbapSourceMultiPageEditor editor = (IAbapSourceMultiPageEditor) part;
		editor.addPageChangedListener(pageChangedListener);
	}

	@SuppressWarnings("restriction")
	private void modifyReconciler(IAbapSourcePage page) {
		PresentationReconciler reconciler = page.getExistingPresentationReconciler();

		// We may have already modified this reconcilers repairer
		IPresentationRepairer rep = reconciler.getRepairer(IDocument.DEFAULT_CONTENT_TYPE);
		if (rep instanceof AbapDamageRepairer) {
			return;
		}

		// Modify the repairer to use our custom damager and repairer instead.
		AbapDamageRepairer dr = new AbapDamageRepairer(new AbapScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.install(page.getViewer());
		page.getViewer().invalidateTextPresentation();
	}

	private IPartListener2 partListener = new IPartListener2() {

		@SuppressWarnings("restriction")
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);

			addPageListener(part);
			if (part instanceof IAbapSourceMultiPageEditor) {
				IAbapSourceMultiPageEditor editor = (IAbapSourceMultiPageEditor) part;
				for (IAbapSourcePage page : editor.getLoadedPages()) {
					modifyReconciler(page);
				}
			}

		};
	};

	private IPageChangedListener pageChangedListener = new IPageChangedListener() {

		@SuppressWarnings("restriction")
		@Override
		public void pageChanged(PageChangedEvent event) {
			Object genericPage = event.getSelectedPage();

			if (genericPage instanceof IAbapSourcePage) {
				modifyReconciler((IAbapSourcePage) genericPage);
			}
		}
	};

}
