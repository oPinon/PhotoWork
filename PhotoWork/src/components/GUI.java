package components;

import gAPainter.Painter;

import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.custom.ScrolledComposite;

import filter.AutoBalance;
import filter.BlurFilter;
import pImage.PImage;
import pImage.Scanner;

import org.eclipse.swt.custom.CLabel;

public class GUI extends Composite {

	//moniteur d'affichage
	private static Display display;
	//fenetre principale
	private static Shell shell;

	//Barre d'options
	Group optionsBar;
	Composite optionsComposite;

	//Zone d'affichage d'image
	ScrolledComposite scrolledComposite;

	//Zones d'affichage d'informations
	Label titleLabel;
	Label infoLabel;
	Label zoomLabel;

	CLabel imageNumber;
	int selectedImageNumber;

	//Données sur l'affichage d'image
	double zoomRatio= 1;
	Image image; //image redimensionnée pour l'affichage
	Image[] originalImages; //Images telles qu'elles étaient lors du chargement
	Image[] savedImages; //Images telles qu'elles sont actuellement (taille normale)

	//Dessins sur l'affichage d'image
	GC gc;


	//PARAMETRES
	//Nom des fichiers chargés
	String[] fileNames;
	//Nom de la fonction sélectionnée
	String selectedFunction;


	//Auto-Balance et Blur
	int autoBalanceType;//Type d'autobalance: 0:simple, 1:équilibrage couleurs, 2: équilibrage avec flou
	Spinner blurSize;
	Button blurCheckButton;

	//Scan
	int[] scanPointsX= new int[4];
	int[] scanPointsY= new int[4];
	List scanFormat;

	//GA Painter
	Button btnStop;

	//Menu Préférences
	PreferencesMenu preferences;
	boolean workOnAllFiles= true;
	int nbThreads= PreferencesMenu.AVAILABLE_THREADS;


	public GUI(Composite parent, int style) {
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 4));
		setLayout(new GridLayout(6, true));
		setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_SELECTION));

		Composite composite_2 = new Composite(this, SWT.NONE);
		composite_2.setLayout(new GridLayout(2, false));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));


		titleLabel= new Label(composite_2, SWT.NONE);
		titleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		titleLabel.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		titleLabel.setText("No image selected");

		Button btnNewButton_4 = new Button(composite_2, SWT.NONE);
		GridData gd_btnNewButton_4 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton_4.heightHint = 29;
		btnNewButton_4.setLayoutData(gd_btnNewButton_4);
		btnNewButton_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		btnNewButton_4.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));
		btnNewButton_4.setImage(new Image(display,"images/undo.png"));
		btnNewButton_4.setText("Undo");
		btnNewButton_4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(image == null){ 
					MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
					mb.setText("Warning");
					mb.setMessage("Please select a file to work on");
					mb.open();
					return;
				}

				if(workOnAllFiles){
					for(int i=0; i<savedImages.length; i++) savedImages[i]= new Image(display, originalImages[i], SWT.IMAGE_COPY);	
				}
				else savedImages[selectedImageNumber]= new Image(display, originalImages[selectedImageNumber], SWT.IMAGE_COPY);
				resizeImage(savedImages[selectedImageNumber]);
			}
		});


		Button btnLoadFiles = new Button(this, SWT.NONE);
		GridData gd_btnLoadFiles = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_btnLoadFiles.heightHint = 25;
		gd_btnLoadFiles.widthHint = 100;
		btnLoadFiles.setLayoutData(gd_btnLoadFiles);
		btnLoadFiles.setText("Load File(s)");
		btnLoadFiles.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileBrowser browser= new FileBrowser(display, SWT.OPEN);
				if(browser.chosenFiles!=null){
					fileNames = browser.chosenFiles;
					savedImages= new Image[fileNames.length];
					originalImages= new Image[fileNames.length];
					for(int i=0; i< fileNames.length; i++){
						savedImages[i] = new Image(display, fileNames[i]);
						originalImages[i] = new Image(display, fileNames[i]);
					}

					zoomRatio=1;
					zoomLabel.setText(("Zoom: "+100+"%"));
					infoLabel.setText("Opened "+fileNames.length+" file(s)");	

					selectedImageNumber= 0;
					updateImageNumber();
				}
			}


		});

		Button btnSaveFiles = new Button(this, SWT.NONE);
		btnSaveFiles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnSaveFiles.setText("Save File(s)");
		btnSaveFiles.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(image == null){ 
					MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
					mb.setText("Warning");
					mb.setMessage("There is no file to save");
					mb.open();
					return;
				}
				FileBrowser browser= new FileBrowser(display, SWT.SAVE);
				if(browser.chosenFiles!=null){
					ImageLoader loader = new ImageLoader();
					loader.data = new ImageData[] {image.getImageData()};
					if(browser.chosenFiles[0]==null) return;
					loader.save(browser.chosenFiles[0], browser.extension);
					infoLabel.setText("Saved in: "+browser.chosenFiles[0]);
				}
			}
		});

		Composite composite_1 = new Composite(this, SWT.NONE);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_1.widthHint = 150;
		composite_1.setLayoutData(gd_composite_1);

		Button btnNewButton_5 = new Button(composite_1, SWT.ARROW | SWT.LEFT);
		btnNewButton_5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(savedImages==null) return;
				if(selectedImageNumber==0) selectedImageNumber= savedImages.length-1;	
				else selectedImageNumber--;
				updateImageNumber();
			}
		});

		imageNumber = new CLabel(composite_1, SWT.CENTER);
		imageNumber.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		imageNumber.setText("0/0");

		Button btnNewButton_6 = new Button(composite_1, SWT.ARROW | SWT.RIGHT);
		btnNewButton_6.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {				
				if(savedImages==null) return;
				if(selectedImageNumber==savedImages.length-1) selectedImageNumber= 0;	
				else selectedImageNumber++;
				updateImageNumber();
			}
		});

		final Button btnPreferences = new Button(this, SWT.NONE);
		btnPreferences.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnPreferences.setText("Preferences");
		btnPreferences.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(image == null){ 
					MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
					mb.setText("Warning");
					mb.setMessage("Please select a file to work on");
					mb.open();
					return;
				}

				final Shell prefShell= new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				preferences= new PreferencesMenu(fileNames, savedImages, workOnAllFiles, nbThreads, prefShell, SWT.NONE);
				prefShell.setText("Preferences");	
				prefShell.setLayout(new FillLayout());
				prefShell.setSize(400,400); 

				Rectangle screenSize = display.getPrimaryMonitor().getBounds();
				prefShell.setLocation((screenSize.width - prefShell.getBounds().width) / 2, (screenSize.height - prefShell.getBounds().height) / 2);
				prefShell.open();
				prefShell.forceActive();

				prefShell.addListener(SWT.Close, new Listener() {
					public void handleEvent(Event event) {
						infoLabel.setText("Preferences changes saved");
						workOnAllFiles= preferences.areAllFilesSelected();
						nbThreads= preferences.getNbThreads();
					}
				});

				while (!prefShell.isDisposed()) {
					if (!display.readAndDispatch())	display.sleep();
				}

				// disposes all associated windows and their components
				//	prefShell.dispose();
			}
		});

		Group composite = new Group(this, SWT.NONE);
		composite.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		composite.setLayout(new FillLayout(SWT.VERTICAL));
		composite.setText("Menu");

		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
		btnNewButton.setImage(new Image(display,"images/autoBalance.png"));
		btnNewButton.setText("Auto Balance");
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(image == null){ 
					MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
					mb.setText("Warning");
					mb.setMessage("Please select a file to work on");
					mb.open();
					return;
				}
				selectedFunction= "Auto Balance";
				createOptionsMenu();
			}
		});


		Button btnNewButton_1 = new Button(composite, SWT.NONE);
		btnNewButton_1.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
		btnNewButton_1.setImage(new Image(display,"images/blur.png"));
		btnNewButton_1.setText("Blur");
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(image == null){ 
					MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
					mb.setText("Warning");
					mb.setMessage("Please select a file to work on");
					mb.open();
					return;
				}
				selectedFunction= "Blur";
				createOptionsMenu();
			}
		});


		Button btnNewButton_2 = new Button(composite, SWT.NONE);
		btnNewButton_2.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
		btnNewButton_2.setImage(new Image(display,"images/scanner.png"));
		btnNewButton_2.setText("Scan");
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(image == null){ 
					MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
					mb.setText("Warning");
					mb.setMessage("Please select a file to work on");
					mb.open();
					return;
				}
				selectedFunction= "Scan";
				createOptionsMenu();
			}
		});

		Button btnNewButton_3 = new Button(composite, SWT.NONE);
		btnNewButton_3.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
		btnNewButton_3.setImage(new Image(display,"images/GA.jpg"));
		btnNewButton_3.setText("GA Painter");
		btnNewButton_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(image == null){ 
					MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
					mb.setText("Warning");
					mb.setMessage("Please select a file to work on");
					mb.open();
					return;
				}
				selectedFunction= "GA Painter";
				createOptionsMenu();
			}
		});

		scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.DOUBLE_BUFFERED |SWT.NO_REDRAW_RESIZE);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.addPaintListener(new PaintListener() { 
			public void paintControl(PaintEvent e) { 
				if(savedImages!=null) resizeImage(savedImages[selectedImageNumber]);
			} 
		}); 
		//scrolledComposite.setRedraw(false);

		optionsBar = new Group(this, SWT.NONE);
		optionsBar.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
		optionsBar.setLayout(new FillLayout(SWT.HORIZONTAL));
		optionsBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		optionsBar.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		optionsBar.setText("Welcome !");
		optionsComposite = new Composite(optionsBar, SWT.FILL);
		optionsComposite.setVisible(false);



		infoLabel= new Label(this, SWT.NONE);
		infoLabel.setText("Ready");
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		final ProgressBar progressBar = new ProgressBar(this, SWT.SMOOTH);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);	
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_progressBar.widthHint = 400;
		gd_progressBar.heightHint = 40;
		progressBar.setLayoutData(gd_progressBar);	

		progressBar.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				String string =
						(progressBar.getSelection() * 1.0 /
								(progressBar.getMaximum()-progressBar.getMinimum()) * 100)
								+ "%";
				Point point = progressBar.getSize();

				FontMetrics fontMetrics = e.gc.getFontMetrics();
				int width = fontMetrics.getAverageCharWidth() * string.length();
				int height = fontMetrics.getHeight();
				e.gc.setForeground
				(shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				e.gc.drawString
				(string, (point.x-width)/2 , (point.y-height)/2, true);
			}
		});

		zoomLabel= new Label(this, SWT.NONE);
		zoomLabel.setText(("Zoom: "+(int) (zoomRatio*100)+"%"));
		zoomLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		Button btnApply = new Button(this, SWT.NONE);
		btnApply.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD | SWT.ITALIC));
		GridData gd_btnApply = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_btnApply.heightHint = 40;
		gd_btnApply.widthHint = 100;
		btnApply.setLayoutData(gd_btnApply);
		btnApply.setText("Apply !");
		btnApply.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
		btnApply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				apply();
			}
		});

		//		ProgressBarHandler pbh = new ProgressBarHandler(progressBar);
		//		pbh.start();

	}

	private void updateImageNumber() {
		imageNumber.setText((selectedImageNumber+1)+"/"+savedImages.length);
		titleLabel.setText(fileNames[selectedImageNumber].substring(fileNames[selectedImageNumber].lastIndexOf('/') + 1));
		resizeImage(savedImages[selectedImageNumber]);

	}

	private void createOptionsMenu(){
		if(!optionsComposite.isDisposed()){
			if(optionsBar.getText()=="Scan") resizeImage(savedImages[selectedImageNumber]); //on enleve les points d'un scan non fini
			optionsComposite.dispose();
			optionsComposite = new Composite(optionsBar, SWT.FILL);
			optionsComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));

			Listener[] listeners = scrolledComposite.getListeners(SWT.MouseDown); 
			for(int i = 0 ; i< listeners.length; i++){
				scrolledComposite.removeListener(SWT.MouseDown, listeners[i]);
			}
		}

		switch(selectedFunction){
		case "Auto Balance":
			autoBalanceType=0;

			optionsBar.setText("Auto Balance");
			optionsComposite.setLayout(new GridLayout(1, true));

			final Button btnNewButton_10 = new Button(optionsComposite, SWT.RADIO);
			btnNewButton_10.setText("Balance Brightness");
			btnNewButton_10.setSelection(true);
			btnNewButton_10.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if(btnNewButton_10.getSelection()){
						autoBalanceType=0;
						blurCheckButton.setEnabled(false);
						blurSize.setEnabled(false);
					}
				}
			});

			final Button btnNewButton_11 = new Button(optionsComposite, SWT.RADIO);
			btnNewButton_11.setText("Balance Colors");
			btnNewButton_11.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if(btnNewButton_11.getSelection()){
						autoBalanceType=1;
						blurCheckButton.setEnabled(true);
						blurCheckButton.setSelection(false);
					}
				}
			});

			blurCheckButton = new Button(optionsComposite, SWT.CHECK);
			blurCheckButton.setText("Blur");
			blurCheckButton.setEnabled(false);
			blurCheckButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if(!blurCheckButton.getSelection()){
						blurSize.setEnabled(false);
					}
					else blurSize.setEnabled(true);
				}
			});


			Label lblNewLabel = new Label(optionsComposite, SWT.NONE);
			lblNewLabel.setText("Blur Size (px):");
			blurSize = new Spinner(optionsComposite, SWT.BORDER);
			blurSize.setSelection(10);
			blurSize.setEnabled(false);
			break;

		case "Blur":
			optionsBar.setText("Blur");
			optionsComposite.setLayout(new GridLayout(1, true));

			Label lblNewLabel1 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel1.setText("Blur Size (px):");
			blurSize = new Spinner(optionsComposite, SWT.BORDER);
			blurSize.setSelection(10);
			break;

		case "Scan":
			optionsBar.setText("Scan");
			optionsComposite.setLayout(new GridLayout(2, true));

			final Image imageWithoutPoints = new Image(display, savedImages[selectedImageNumber], SWT.IMAGE_COPY);

			Label lblNewLabel2 = new Label(optionsComposite, SWT.NONE);	
			if(image == null){ 
				lblNewLabel2.setText("Please open the file to scan");
				break;
			}
			lblNewLabel2.setText("Select image points");		
			new Label(optionsComposite, SWT.NONE);

			Label lblNewLabel31 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel31.setText("Point 1: Upper Left");
			new Label(optionsComposite, SWT.NONE);
			final Text lblNewLabel32 = new Text(optionsComposite, SWT.NONE);
			final Text lblNewLabel33 = new Text(optionsComposite, SWT.NONE);

			Label lblNewLabel41 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel41.setText("Point 2: Upper Right");	
			new Label(optionsComposite, SWT.NONE);
			final Text lblNewLabel42 = new Text(optionsComposite, SWT.NONE);
			final Text lblNewLabel43 = new Text(optionsComposite, SWT.NONE);

			Label lblNewLabel51 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel51.setText("Point 3: Lower Left");
			new Label(optionsComposite, SWT.NONE);
			final Text lblNewLabel52 = new Text(optionsComposite, SWT.NONE);
			final Text lblNewLabel53 = new Text(optionsComposite, SWT.NONE);

			Label lblNewLabel61 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel61.setText("Point 4: Lower Right");
			new Label(optionsComposite, SWT.NONE);
			final Text lblNewLabel62 = new Text(optionsComposite, SWT.NONE);
			final Text lblNewLabel63 = new Text(optionsComposite, SWT.NONE);

			final Button btnNewButton = new Button(optionsComposite, SWT.NONE);
			btnNewButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					scanPointsX[0]=Integer.parseInt(lblNewLabel32.getText());
					scanPointsY[0]=Integer.parseInt(lblNewLabel33.getText());
					scanPointsX[1]=Integer.parseInt(lblNewLabel42.getText());
					scanPointsY[1]=Integer.parseInt(lblNewLabel43.getText());
					scanPointsX[2]=Integer.parseInt(lblNewLabel52.getText());
					scanPointsY[2]=Integer.parseInt(lblNewLabel53.getText());
					scanPointsX[3]=Integer.parseInt(lblNewLabel62.getText());
					scanPointsY[3]=Integer.parseInt(lblNewLabel63.getText());
					infoLabel.setText("Scan points saved");
				}
			});
			btnNewButton.setText("Save Points");
			btnNewButton.setEnabled(false);

			Button btnNewButton1 = new Button(optionsComposite, SWT.NONE);
			btnNewButton1.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					savedImages[selectedImageNumber] = new Image(display, imageWithoutPoints, SWT.IMAGE_COPY);
					resizeImage(savedImages[selectedImageNumber]);
					lblNewLabel32.setText("");
					lblNewLabel33.setText("");
					lblNewLabel42.setText("");
					lblNewLabel43.setText("");
					lblNewLabel52.setText("");
					lblNewLabel53.setText("");
					lblNewLabel62.setText("");
					lblNewLabel63.setText("");
					btnNewButton.setEnabled(false);
				}
			});
			btnNewButton1.setText("Clear");

			scanFormat = new List(optionsComposite, SWT.V_SCROLL);
			scanFormat.setItems(new String[] {"A0 (841*1189)", "A1 (594*841)", "A2 (420*594)", "A3 (297*420)",
					"A4 (210*297)", "A5 (148*210)", "A6 (105*148)"});
			scanFormat.setSelection(4);


			scrolledComposite.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent arg0) {
					if(gc==null || gc.isDisposed()) gc= new GC(scrolledComposite);
					gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
					gc.fillOval(arg0.x-5, arg0.y-5, 10, 10);
					if(lblNewLabel32.getText().length()==0){		
						lblNewLabel32.setText(String.valueOf((int) (arg0.x/zoomRatio)));
						lblNewLabel33.setText(String.valueOf((int) (arg0.y/zoomRatio)));
						return;
					}
					if(lblNewLabel42.getText().length()==0){
						lblNewLabel42.setText(String.valueOf((int) (arg0.x/zoomRatio)));
						lblNewLabel43.setText(String.valueOf((int) (arg0.y/zoomRatio)));
						return;
					}
					if(lblNewLabel52.getText().length()==0){
						lblNewLabel52.setText(String.valueOf((int) (arg0.x/zoomRatio)));
						lblNewLabel53.setText(String.valueOf((int) (arg0.y/zoomRatio)));
						return;
					}
					if(lblNewLabel62.getText().length()==0){
						lblNewLabel62.setText(String.valueOf((int) (arg0.x/zoomRatio)));
						lblNewLabel63.setText(String.valueOf((int) (arg0.y/zoomRatio)));
					}
					if(lblNewLabel32.getText().length()!=0 && lblNewLabel33.getText().length()!=0 && lblNewLabel42.getText().length()!=0 && 
							lblNewLabel43.getText().length()!=0 && lblNewLabel52.getText().length()!=0 && lblNewLabel53.getText().length()!=0 &&
							lblNewLabel62.getText().length()!=0 && lblNewLabel63.getText().length()!=0){
						btnNewButton.setEnabled(true);
					}
				}
			});

			break;

		case "GA Painter":
			optionsBar.setText("GA Painter");
			optionsComposite.setLayout(new GridLayout(1, true));

			btnStop = new Button(optionsComposite, SWT.NONE);

			btnStop.setText("Stop");

		}
		optionsBar.layout();
	}

	private void apply(){
		if(image == null){ 
			MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
			mb.setText("Warning");
			mb.setMessage("Please select a file to work on");
			mb.open();
			return;
		}
		if(selectedFunction == null){ 
			MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
			mb.setText("Warning");
			mb.setMessage("Please select a function");
			mb.open();
			return;
		}

		Image[] imagesToModify;
		int count;
		if(!workOnAllFiles){
			imagesToModify= new Image[1];
			count= selectedImageNumber;
			imagesToModify[0]= savedImages[count];
		}
		else {
			imagesToModify= savedImages;
			count= 0;
		}

		switch(selectedFunction){
		case "Auto Balance":
			for(Image i: imagesToModify){
				ImageData id= i.getImageData();
				if(autoBalanceType==1 && blurSize.isEnabled()) autoBalanceType= 2;
				PImage output;

				switch(autoBalanceType){
				case 0: output=AutoBalance.balance(new PImage(FormatConversion.convertToAWT(id)), nbThreads);                    break;
				case 1: output=AutoBalance.balanceColors(new PImage(FormatConversion.convertToAWT(id)));						 break;	
				case 2: output=AutoBalance.balanceColors(new PImage(FormatConversion.convertToAWT(id)),blurSize.getSelection()); break;
				default: output= new PImage(0,0);
				}

				i= new Image(getDisplay(),FormatConversion.convertToSWT(output.getImage()));
				savedImages[count]= i;
				infoLabel.setText(infoLabel.getText()+"Done for image"+count+"\n");
				count++;
			}
			resizeImage(savedImages[selectedImageNumber]);
			break;

		case "Blur":
			for(Image i: imagesToModify){

				ImageData id= i.getImageData();
				PImage output= BlurFilter.blur(new PImage(FormatConversion.convertToAWT(id)),blurSize.getSelection());

				i= new Image(getDisplay(),FormatConversion.convertToSWT(output.getImage()));
				savedImages[count]= i;
				infoLabel.setText(infoLabel.getText()+"Done for image "+count+"\n");
				count++;
			}
			resizeImage(savedImages[selectedImageNumber]);
			break;

		case "Scan":
			//			if(scanPointsX[0] == 0 || scanPointsY[0] == 0){
			//				MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
			//				mb.setText("Warning");
			//				mb.setMessage("Please select 4 scan points\nand hit save button");
			//				mb.open();
			//				return;
			//			}
			//
			//			ImageData id2= savedImages[selectedImageNumber].getImageData();
			//			PImage output;
			//			output= Scanner.scan(FormatConversion.convertToAWT(id2), scanPointsX,scanPointsY,scanFormat.getSelectionIndex());
			//
			//			savedImages[selectedImageNumber]= new Image(getDisplay(),FormatConversion.convertToSWT(output.getImage()));
			//			resizeImage(savedImages[selectedImageNumber]);
			//			infoLabel.setText(infoLabel.getText()+"Scanning done"+"\n");
			break;

		case "GA Painter":	
			//			ImageData id= savedImages[selectedImageNumber].getImageData();
			//			final Painter p= new Painter(FormatConversion.convertToAWT(id));		
			//			p.start();
			//
			//
			//			final Timer t = new Timer();
			//			t.scheduleAtFixedRate(new TimerTask(){ public void run(){	
			//				GC GAgc=null;	
			//				if(p.output!=null){
			//					final Image output= new Image(display,savedImages[selectedImageNumber].getBounds().width*2,savedImages[selectedImageNumber].getBounds().height);
			//
			//					if(GAgc==null || GAgc.isDisposed()){
			//						GAgc= new GC(output);
			//					}
			//					GAgc.drawImage(savedImages[selectedImageNumber],0, 0);
			//					GAgc.drawImage(new Image(display,FormatConversion.convertToSWT(p.output)),savedImages[selectedImageNumber].getBounds().width, 0);
			//					GAgc.dispose();			
			//					Display.getDefault().asyncExec(new Runnable() {
			//						public void run() {
			//							resizeImage(output);
			//						}
			//					});
			//				}
			//			}}
			//			,0,1000l);
			//
			//			btnStop.addSelectionListener(new SelectionAdapter() {
			//				@Override
			//				public void widgetSelected(SelectionEvent arg0) {
			//					t.cancel();
			//					p.interrupt();
			//				}
			//			});
			break;
		}

	}

	public void resizeImage(Image img){
		if(img==null) return;

		double xratio=(double) (scrolledComposite.getClientArea().width)/img.getBounds().width;
		double yratio=(double) (scrolledComposite.getClientArea().height)/img.getBounds().height;

		zoomRatio=Math.min(xratio, yratio);	
		zoomLabel.setText(("Zoom: "+(int) (zoomRatio*100)+"%"));	

		image= new Image(display,img.getImageData().scaledTo( (int)(img.getBounds().width*zoomRatio), (int)(img.getBounds().height*zoomRatio)));

		if(gc==null || gc.isDisposed()) gc= new GC(scrolledComposite);
		gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(scrolledComposite.getClientArea());
		gc.drawImage(image, 0, 0);
		gc.dispose();

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public static void main(String[] args){
		display = new Display();
		shell = new Shell(display,SWT.SHELL_TRIM);

		GUI g=new GUI(shell,SWT.NONE);	
		shell.setText("Photowork");	
		shell.setLayout(new FillLayout());
		shell.setSize(1250,700);
		shell.setMinimumSize(1250,700); 

		shell.open();
		shell.forceActive();


		// run the event loop as long as the window is open
		while (!g.isDisposed()) {
			// read the next OS event queue and transfer it to a SWT event 
			if (!display.readAndDispatch())
			{
				// if there are currently no other OS event to process
				// sleep until the next OS event is available 
				display.sleep();
			}
		}
		// disposes all associated windows and their components
		display.dispose();
		System.exit(0);
	}
}