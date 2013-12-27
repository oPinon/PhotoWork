package components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ScrollBar;

public class PreferencesMenu extends Composite {
	List list;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public PreferencesMenu(String[] fileNames, Image[] savedImages, Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(4, false));

		Label lblNewLabel = new Label(this, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		lblNewLabel.setText("Select files to work on");

		Label label = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));

		Label lblNewLabel_1 = new Label(this, SWT.NONE);
		lblNewLabel_1.setText("File List");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		Label lblSize = new Label(this, SWT.NONE);
		lblSize.setText("Size");

		list = new List(this, SWT.MULTI | SWT.V_SCROLL);
		GridData gd_list = new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1);
		gd_list.widthHint = 260;
		list.setLayoutData(gd_list);

		final List sizes = new List(this, SWT.V_SCROLL);
		GridData gd_sizes = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_sizes.widthHint = 65;
		sizes.setLayoutData(gd_sizes);
		sizes.setEnabled(false);

		if(fileNames!=null){
			for(int i=0; i<fileNames.length; i++){
				list.add(fileNames[i].substring(fileNames[i].lastIndexOf('/') + 1));
				sizes.add(savedImages[i].getBounds().width+"*"+savedImages[i].getBounds().height+"\n");
			}
		}
		list.setSelection(0);

		final ScrollBar vBar1 = list.getVerticalBar();
		final ScrollBar vBar2 = sizes.getVerticalBar();
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				vBar2.setSelection(vBar1.getSelection());
				sizes.setTopIndex(vBar2.getSelection());
			}
		};
		vBar1.addSelectionListener(listener);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
