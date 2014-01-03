package display;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

public class PreferencesMenu extends Composite {
	public static final int AVAILABLE_THREADS= Runtime.getRuntime().availableProcessors();

	Composite parent;

	List fileList, serverList;
	Spinner threadsSpinner;	
	
	Button btnRemoveServer;

	boolean workOnAllFiles;
	int threads;
	String[] IPs;
	Text text;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	PreferencesMenu(String[] fileNames, Image[] savedImages, boolean workOnEveryFiles, int nbThreads, String[] IPList, final Composite parent, int style) {
		super(parent, style);
		this.parent= parent;
		setLayout(new GridLayout(2, false));

		Group grpNetwork = new Group(this, SWT.NONE);
		grpNetwork.setLayout(new GridLayout(1, false));
		grpNetwork.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
		grpNetwork.setText("Network");

		serverList = new List(grpNetwork, SWT.BORDER | SWT.V_SCROLL);
		GridData gd_serverList = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_serverList.widthHint = 170;
		serverList.setLayoutData(gd_serverList);
		serverList.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				btnRemoveServer.setEnabled(serverList.getSelectionCount()!=0);
			}
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});		
		IPs= IPList;
		for(String s: IPs){
			serverList.add(s);
		}

		Composite composite_1 = new Composite(grpNetwork, SWT.NONE);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		text = new Text(composite_1, SWT.BORDER);

		Button btnAddServer = new Button(composite_1, SWT.NONE);
		btnAddServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String ip= text.getText();			
				if(ip.length()!=0){
					try {
						InetAddress.getByName(ip);	
						Socket socket= new Socket();	
						socket.connect(new InetSocketAddress(ip, 6789), 1000);
						socket.close();

						serverList.add(text.getText());
						btnRemoveServer.setEnabled(true);
					} catch (IOException e) {
						GUI.showWarningMessage("Can't reach entered IP address");
					} 
					finally{
						text.setText("");	
					}													
				}
			}
		});
		btnAddServer.setText("Add");

		btnRemoveServer = new Button(composite_1, SWT.NONE);
		btnRemoveServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				serverList.remove(serverList.getSelectionIndex());
				btnRemoveServer.setEnabled(false);
			}
		});
		btnRemoveServer.setText("Remove");
		btnRemoveServer.setEnabled(false);

		Group grpFilesToWork = new Group(this, SWT.NONE);
		grpFilesToWork.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpFilesToWork.setText("Select files to work on");
		grpFilesToWork.setLayout(new GridLayout(2, false));

		Label lblNewLabel_1 = new Label(grpFilesToWork, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_1.setText("File List");

		Label lblSize = new Label(grpFilesToWork, SWT.NONE);
		lblSize.setText("Size");

		fileList = new List(grpFilesToWork, SWT.V_SCROLL);
		fileList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

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
		final ScrollBar vBar1 = fileList.getVerticalBar();

		for(int i=0; i<fileNames.length; i++){
			fileList.add(fileNames[i].substring(fileNames[i].lastIndexOf('/') + 1));
			sizes.add(savedImages[i].getBounds().width+"*"+savedImages[i].getBounds().height);
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
				serverList.removeAll();
				try {
					serverList.add(InetAddress.getLocalHost().getHostAddress());
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		});
		btnRestoreDefaults.setText("Restore defaults");

		Button btnApplyChanges = new Button(composite, SWT.NONE);
		btnApplyChanges.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				workOnAllFiles= btnCheckButton.getSelection();
				threads= threadsSpinner.getSelection();
				IPs= serverList.getItems();
				lblNewLabel.setText("Saved");
			}
		});
		btnApplyChanges.setText("Apply changes");
	}

	boolean areAllFilesSelected(){
		return workOnAllFiles;
	}

	int getNbThreads(){
		return threads;
	}

	String[] getIPList() {
		return IPs;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT display
	}
}
