/**
 * 
 */
package org.ietr.preesm.core.scenario.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.ietr.preesm.core.scenario.Scenario;

/**
 * @author mpelcat
 *
 */
public class ConstraintsPage extends FormPage {

	private Scenario scenario;

	@Override
	public Control getPartControl() {
		// TODO Auto-generated method stub
		return super.getPartControl();
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		// TODO Auto-generated method stub
		super.createFormContent(managedForm);
		
		ScrolledForm f = managedForm.getForm();
		f.setText("Formular:");
		f.getBody().setLayout(new GridLayout());
		
		managedForm.getToolkit().createLabel(f.getBody(), "Feld1:");
		managedForm.getToolkit().createText(f.getBody(), "Wert1");
		managedForm.getToolkit().createHyperlink(f.getBody(), "Dies ist der Text", 0);

		Composite treeCp = managedForm.getToolkit().createComposite(f.getBody());
		treeCp.setLayout(new GridLayout(2,true));
		SDFTreeSection sdfTreeSection = new SDFTreeSection(scenario, treeCp, managedForm.getToolkit(),Section.DESCRIPTION);
		
		
		managedForm.refresh();

	}

	public ConstraintsPage(Scenario scenario, FormEditor editor, String id, String title) {
		super(editor, id, title);
		this.scenario = scenario;
	}

	@Override
	public void createPartControl(Composite parent) {

		super.createPartControl(parent);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		super.doSave(monitor);
	}

}
