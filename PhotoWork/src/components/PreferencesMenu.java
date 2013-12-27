package components;

import java.util.Set;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Button;

public class PreferencesMenu extends Composite {
	public static final int AVAILABLE_THREADS= Runtime.getRuntime().availableProcessors();
	
	Composite parent;

	List list;
	Spinner threadsSpinner;

	boolean workOnAllFiles;
	int threads;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public PreferencesMenu(String[] fileNames, Image[] savedImages, boolean workOnEveryFiles, int nbThreads, final Composite parent, int style) {
		super(parent, style);
		this.parent= parent;
		setLayout(new GridLayout(1, false));

		Group grpFilesToWork = new Group(this, SWT.NONE);
		grpFilesToWork.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpFilesToWork.setText("Select files to work on");
		grpFilesToWork.setLayout(new GridLayout(2, false));

		Label lblNewLabel_1 = new Label(grpFilesToWork, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_1.setText("File List");

		Label lblSize = new Label(grpFilesToWork, SWT.NONE);
		lblSize.setText("Size");

		list = new List(grpFilesToWork, SWT.V_SCROLL);
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final List sizes = new List(grpFilesToWork, SWT.V_SCROLL);
		sizes.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		sizes.setEnabled(false);

		final Button btnCheckButton = new Button(grpFilesToWork, SWT.CHECK);
        workOnAllFiles= workOnEveryFiles;
		btnCheckButton.setSelection(workOnAllFiles);
		btnCheckButton.setText("Work on all files");
		
		final Label lblNewLabel = new Label(grpFilesToWork, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		Group grpNumberOfThreads = new Group(composite, SWT.NONE);
		grpNumberOfThreads.setText("Number of threads");
		grpNumberOfThreads.setLayout(new FillLayout(SWT.HORIZONTAL));

		threadsSpinner = new Spinner(grpNumberOfThreads, SWT.BORDER);
		threadsSpinner.setMinimum(1);
		threads = nbThreads;
		threadsSpinner.setSelection(threads);

		final ScrollBar vBar2 = sizes.getVerticalBar();
		final ScrollBar vBar1 = list.getVerticalBar();

		for(int i=0; i<fileNames.length; i++){
			list.add(fileNames[i].substring(fileNames[i].lastIndexOf('/') + 1));
			sizes.add(savedImages[i].getBounds().width+"*"+savedImages[i].getBounds().height+"\n");
		}

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				vBar2.setSelection(vBar1.getSelection());
				sizes.setTopIndex(vBar2.getSelection());
			}
		};

		vBar1.addSelectionListener(listener);
		
		Button btnRestoreDefaults = new Button(composite, SWT.NONE);
		btnRestoreDefaults.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				btnCheckButton.setSelection(true);
				threadsSpinner.setSelection(AVAILABLE_THREADS);
				lblNewLabel.setText("Reset");
			}
		});
		btnRestoreDefaults.setText("Restore defaults");
		
		Button btnApplyChanges = new Button(composite, SWT.NONE);
		btnApplyChanges.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				workOnAllFiles= btnCheckButton.getSelection();
				threads= threadsSpinner.getSelection();
				lblNewLabel.setText("Saved");
			}
		});
		btnApplyChanges.setText("Apply changes");


	}

	public boolean areAllFilesSelected(){
		return workOnAllFiles;
	}

	public int getNbThreads(){
		return threads;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
