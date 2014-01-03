package display;

import gAPainter.Painter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import network.Buffer;
import network.Client;
import network.FormatConversion;
import network.Result;
import network.Server;
import network.Task;

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
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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
import filter.ImageFunction;
import pImage.PImage;
import pImage.Scanner;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.RowLayout;

public class GUI extends Composite  {

	//BASE DE LA FENETRE SWT
	//moniteur d'affichage
	private static Display display;
	//fenetre principale
	private static Shell shell;

	//RESEAU
	private static Server server; //traite les taches envoyées par les PC

	//AFFICHAGE GRAPHIQUE
	//Barre d'options
	Group optionsBar;
	Composite optionsComposite;

	//Zone d'affichage d'image
	Composite imageFrame;

	//Zones d'affichage d'informations
	Text titleLabel, infoLabel, zoomLabel;

	CLabel imageNumber;
	int selectedImageNumber;

	Button btnApply;

	//Données sur l'affichage d'image
	double zoomRatio= 1;
	Image image; //image redimensionnée pour l'affichage
	Image[] originalImages; //Images telles qu'elles étaient lors du chargement
	Image[] savedImages; //Images telles qu'elles sont actuellement (taille normale)

	//Dessins sur l'affichage d'image
	GC gc;

	//Barre de progression
	ProgressBar progressBar;


	//PARAMETRES
	//Nom des fichiers chargés
	String[] fileNames;
	//Nom de la fonction sélectionnée
	ImageFunction selectedFunction;

	//Fonctions de filtre (Auto Balance, Blur, HDR)
	int autoBalanceType;//Type d'autobalance: 0:simple, 1:équilibrage couleurs, 2: équilibrage avec flou
	Spinner blurSize;
	Button blurCheckButton;
	List HDRAlgorithm;
	
	//DFT
	int scaleMethod;
	List DFTMode;
	Spinner cutFrequency;
	
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
	String[] IPList;


	public GUI(Composite parent, int style) {
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 4));
		GridLayout gl= (new GridLayout(6, true));
		gl.makeColumnsEqualWidth= true;
		setLayout(gl);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_SELECTION));

		Composite compositeTitle = new Composite(this, SWT.NONE);
		compositeTitle.setLayout(new GridLayout(3, false));
		compositeTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		titleLabel= new Text(compositeTitle, SWT.NONE);
		titleLabel.setEditable(false);
		titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		titleLabel.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		titleLabel.setText("No image selected");

		Button btnRefresh = new Button(compositeTitle, SWT.NONE);
		btnRefresh.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		btnRefresh.setImage(new Image(display,"images/refresh.png"));
		btnRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(image == null){ 
					showWarningMessage("Please select a file to work on");
					return;
				}
				refreshDisplay();
			}
		});

		Button btnUndo = new Button(compositeTitle, SWT.NONE);
		btnUndo.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		btnUndo.setImage(new Image(display,"images/undo.png"));
		btnUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(image == null){ 
					showWarningMessage("Please select a file to work on");
					return;
				}

				if(workOnAllFiles){
					for(int i=0; i<savedImages.length; i++) savedImages[i]= new Image(display, originalImages[i], SWT.IMAGE_COPY);	
				}
				else savedImages[selectedImageNumber]= new Image(display, originalImages[selectedImageNumber], SWT.IMAGE_COPY);
				refreshDisplay();
			}
		});

		Button btnLoadFiles = new Button(this, SWT.NONE);
		GridData gd_btnLoadFiles = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_btnLoadFiles.heightHint = 25;
		gd_btnLoadFiles.widthHint = 100;
		btnLoadFiles.setLayoutData(gd_btnLoadFiles);
		btnLoadFiles.setText("Load Files");
		btnLoadFiles.setImage(new Image(display,"images/load.png"));
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
		btnSaveFiles.setText("Save Files");
		btnSaveFiles.setImage(new Image(display,"images/save.png"));
		btnSaveFiles.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(image == null){ 
					showWarningMessage("There is no file to save");
					return;
				}
				FileBrowser browser= new FileBrowser(display, SWT.SAVE);

				if(browser.chosenFiles!=null){
					String name= browser.chosenFiles[0];
					ImageLoader loader = new ImageLoader();

					if(!workOnAllFiles || savedImages.length==1){
						loader.data = new ImageData[] {savedImages[selectedImageNumber].getImageData()};
						loader.save(name, browser.extension);
					}
					else
						for(int i=0; i<savedImages.length; i++) {
							loader.data = new ImageData[] {savedImages[i].getImageData()};
							loader.save((name.substring(0,name.lastIndexOf(".")))+"["+i+"]"+(name.substring(name.lastIndexOf("."))), browser.extension);
						}
					infoLabel.setText("Saved in: "+name);
				}
			}
		});

		Composite compositeImageNumber = new Composite(this, SWT.NONE);
		compositeImageNumber.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_compositeImageNumber = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_compositeImageNumber.widthHint = 150;
		compositeImageNumber.setLayoutData(gd_compositeImageNumber);

		Button btnPrevious = new Button(compositeImageNumber, SWT.ARROW | SWT.LEFT);
		btnPrevious.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(savedImages==null) return;
				if(selectedImageNumber==0) selectedImageNumber= savedImages.length-1;	
				else selectedImageNumber--;
				updateImageNumber();
			}
		});

		imageNumber = new CLabel(compositeImageNumber, SWT.CENTER);
		imageNumber.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		imageNumber.setText("0/0");

		Button btnNext = new Button(compositeImageNumber, SWT.ARROW | SWT.RIGHT);
		btnNext.addSelectionListener(new SelectionAdapter() {
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
		btnPreferences.setImage(new Image(display,"images/prefs.png"));
		btnPreferences.setText("Preferences");
		btnPreferences.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(image == null){ 
					showWarningMessage("Please select a file to work on");
					return;
				}

				final Shell prefShell= new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				preferences= new PreferencesMenu(fileNames, savedImages, workOnAllFiles, nbThreads, IPList, prefShell, SWT.NONE);
				prefShell.setText("Preferences");	
				prefShell.setLayout(new FillLayout());
				prefShell.setSize(700,400); 

				Rectangle screenSize = display.getPrimaryMonitor().getBounds();
				prefShell.setLocation((screenSize.width - prefShell.getBounds().width) / 2, (screenSize.height - prefShell.getBounds().height) / 2);
				prefShell.open();
				prefShell.forceActive();

				prefShell.addListener(SWT.Close, new Listener() {
					public void handleEvent(Event event) {
						infoLabel.setText("Preferences changes saved");
						workOnAllFiles= preferences.areAllFilesSelected();
						nbThreads= preferences.getNbThreads();
						IPList= preferences.getIPList();
					}
				});

				while (!prefShell.isDisposed()) {
					if (!display.readAndDispatch())	display.sleep();
				}

			}
		});

		Group compositeMenu = new Group(this, SWT.NONE);
		compositeMenu.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		compositeMenu.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
		compositeMenu.setLayout(new GridLayout(1, false));
		compositeMenu.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeMenu.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		compositeMenu.setText("Menu");

		Group grpAllImages = new Group(compositeMenu, SWT.NONE);
		grpAllImages.setBackground(SWTResourceManager.getColor(SWT.COLOR_LINK_FOREGROUND));
		grpAllImages.setLayout(new FillLayout(SWT.VERTICAL));
		GridData gd_grpAllImages = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_grpAllImages.heightHint = 163;
		grpAllImages.setLayoutData(gd_grpAllImages);
		grpAllImages.setText("All images");

		Button btnAutoBalance = new Button(grpAllImages, SWT.NONE);
		btnAutoBalance.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		btnAutoBalance.setImage(new Image(display,"images/autoBalance.png"));
		btnAutoBalance.setText("Auto Balance");	
		btnAutoBalance.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				selectedFunction= ImageFunction.AUTO_BALANCE;
				createOptionsMenu();
			}
		});

		Button btnBlur = new Button(grpAllImages, SWT.NONE);
		btnBlur.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		btnBlur.setImage(new Image(display,"images/blur.png"));
		btnBlur.setText("Blur");
		btnBlur.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				selectedFunction= ImageFunction.BLUR;
				createOptionsMenu();
			}
		});

		Button btnHDR = new Button(grpAllImages, SWT.NONE);
		btnHDR.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		btnHDR.setImage(new Image(display,"images/hdr.png"));
		btnHDR.setText("HDR Equalizer");
		btnHDR.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				selectedFunction= ImageFunction.HDR_EQUALIZER;
				createOptionsMenu();
			}
		});

		Button btnFourier = new Button(grpAllImages, SWT.NONE);
		btnFourier.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		btnFourier.setImage(new Image(display,"images/DFT.png"));
		btnFourier.setText("DFT");
		btnFourier.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				selectedFunction= ImageFunction.FOURIER_TRANSFORM;
				createOptionsMenu();
			}
		});

		Group grpCurrentImageOnly = new Group(compositeMenu, SWT.NONE);
		grpCurrentImageOnly.setBackground(SWTResourceManager.getColor(SWT.COLOR_LINK_FOREGROUND));
		grpCurrentImageOnly.setLayout(new FillLayout(SWT.VERTICAL));
		grpCurrentImageOnly.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpCurrentImageOnly.setText("Current image only");


		Button btnScan = new Button(grpCurrentImageOnly, SWT.NONE);
		btnScan.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		btnScan.setImage(new Image(display,"images/scanner.png"));
		btnScan.setText("Scan");		
		btnScan.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				selectedFunction= ImageFunction.SCAN;
				createOptionsMenu();
			}
		});

		Button btnGAPainter = new Button(grpCurrentImageOnly, SWT.NONE);
		btnGAPainter.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		btnGAPainter.setImage(new Image(display,"images/GA.png"));
		btnGAPainter.setText("GA Painter");
		btnGAPainter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				selectedFunction= ImageFunction.GA_PAINTER;
				createOptionsMenu();
			}
		});

		imageFrame = new Composite(this, SWT.BORDER | SWT.DOUBLE_BUFFERED);
		imageFrame.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		imageFrame.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				if(savedImages!=null) refreshDisplay();
			}
		});

		optionsBar = new Group(this, SWT.NONE);
		optionsBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		optionsBar.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
		optionsBar.setLayout(new FillLayout(SWT.HORIZONTAL));
		optionsBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		optionsBar.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		optionsBar.setText("Welcome !");
		optionsComposite = new Composite(optionsBar, SWT.FILL);
		optionsComposite.setVisible(false);

		infoLabel= new Text(this, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_infoLabel = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		gd_infoLabel.heightHint = 40;
		infoLabel.setLayoutData(gd_infoLabel);
		infoLabel.setEditable(false);
		infoLabel.setText("Ready");

		progressBar = new ProgressBar(this, SWT.SMOOTH);
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

		zoomLabel= new Text(this, SWT.WRAP);
		zoomLabel.setEditable(false);
		zoomLabel.setText(("Zoom: "+(int) (zoomRatio*100)+"%"));
		zoomLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		btnApply = new Button(this, SWT.NONE);
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

		try {
			IPList= new String[]{InetAddress.getLocalHost().getHostAddress()};
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		server= new Server();
		server.start();
	}

	private void createOptionsMenu(){
		if(image == null){ 
			showWarningMessage("Please select a file to work on");
			return;
		}
		if(!optionsComposite.isDisposed()){
			if(optionsBar.getText()=="Scan"){
				refreshDisplay(); //on enleve les points d'un scan non fini
				btnApply.setEnabled(true);
			}
			optionsComposite.dispose();
			optionsComposite = new Composite(optionsBar, SWT.FILL);
			optionsComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));

			Listener[] listeners = imageFrame.getListeners(SWT.MouseDown); 
			for(int i = 0 ; i< listeners.length; i++){
				imageFrame.removeListener(SWT.MouseDown, listeners[i]);
			}
		}

		optionsBar.setText(selectedFunction.getName());

		switch(selectedFunction){
		case AUTO_BALANCE:
			optionsComposite.setLayout(new GridLayout(1, true));

			autoBalanceType= 0;

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
						autoBalanceType= 1;
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
					blurSize.setEnabled(blurCheckButton.getSelection());
					if(blurCheckButton.getSelection()) autoBalanceType= 2;
					else autoBalanceType= 1;
				}
			});


			Label lblNewLabel = new Label(optionsComposite, SWT.NONE);
			lblNewLabel.setText("Blur Size (px):");
			blurSize = new Spinner(optionsComposite, SWT.BORDER);
			blurSize.setSelection(10);
			blurSize.setEnabled(false);
			break;

		case BLUR:
			optionsComposite.setLayout(new GridLayout(1, true));

			Label lblNewLabel1 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel1.setText("Blur Size (px):");
			blurSize = new Spinner(optionsComposite, SWT.BORDER);
			blurSize.setSelection(10);
			break;

		case HDR_EQUALIZER:
			optionsComposite.setLayout(new GridLayout(1, true));

			Label lblNewLabel2 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel2.setText("Blur Size (px):");
			blurSize = new Spinner(optionsComposite, SWT.BORDER);
			blurSize.setSelection(50);

			Label lblNewLabel21 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel21.setText("Algorithm:");
			HDRAlgorithm= new List(optionsComposite, SWT.NONE);
			HDRAlgorithm.add("Adaptive histogram equalization");
			HDRAlgorithm.add("Bas-relief from depth map");
			HDRAlgorithm.setSelection(0);
			break;
			
		case FOURIER_TRANSFORM:
			optionsComposite.setLayout(new GridLayout(1, true));
			
			scaleMethod= 1;
			final Button btnLog,btnLin;
			
			
			Label lblNewLabel22 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel22.setText("DFT Type:");
			DFTMode = new List(optionsComposite, SWT.CHECK);
			DFTMode.add("Image spectrum");
			DFTMode.add("Low-pass filter");
			DFTMode.setSelection(0);

			Label lblNewLabel23 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel23.setText("Scaling:");	
			btnLog = new Button(optionsComposite, SWT.RADIO);
			btnLog.setText("Logarithmic");
			btnLog.setSelection(true);
			btnLog.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if(btnLog.getSelection()){
						scaleMethod= 1;
					}
				}
			});
			btnLin = new Button(optionsComposite, SWT.RADIO);
			btnLin.setText("Linear");
			btnLin.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if(btnLin.getSelection()){
						scaleMethod= 0;
					}
				}
			});	
			
			DFTMode.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					cutFrequency.setEnabled(DFTMode.getSelectionIndex()==1);
					btnLog.setEnabled(DFTMode.getSelectionIndex()==0);
					btnLin.setEnabled(DFTMode.getSelectionIndex()==0);
				}
			});
			
			Label lblNewLabel24 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel24.setText("Cutoff frequency");
			cutFrequency = new Spinner(optionsComposite, SWT.BORDER);
			cutFrequency.setEnabled(false);
			cutFrequency.setSelection(8);
			break;

		case SCAN:
			optionsComposite.setLayout(new GridLayout(2, true));

			btnApply.setEnabled(false);

			final Image imageWithoutPoints = new Image(display, savedImages[selectedImageNumber], SWT.IMAGE_COPY);

			Label lblNewLabel3 = new Label(optionsComposite, SWT.NONE);	
			if(image == null){ 
				lblNewLabel3.setText("Please open the file to scan");
				break;
			}
			lblNewLabel3.setText("Select image points");		
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

			Button btnClear = new Button(optionsComposite, SWT.NONE);
			btnClear.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					savedImages[selectedImageNumber] = new Image(display, imageWithoutPoints, SWT.IMAGE_COPY);
					refreshDisplay();
					lblNewLabel32.setText("");
					lblNewLabel33.setText("");
					lblNewLabel42.setText("");
					lblNewLabel43.setText("");
					lblNewLabel52.setText("");
					lblNewLabel53.setText("");
					lblNewLabel62.setText("");
					lblNewLabel63.setText("");
					btnApply.setEnabled(false);
				}
			});
			btnClear.setText("Clear");		
			new Label(optionsComposite, SWT.NONE);

			scanFormat = new List(optionsComposite, SWT.V_SCROLL);
			scanFormat.setItems(new String[] {"A0 (841*1189)", "A1 (594*841)", "A2 (420*594)", "A3 (297*420)",
					"A4 (210*297)", "A5 (148*210)", "A6 (105*148)"});
			scanFormat.setSelection(4);


			imageFrame.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent arg0) {
					if(gc==null || gc.isDisposed()) gc= new GC(imageFrame);
					gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
					gc.fillOval(arg0.x-5, arg0.y-5, 10, 10);
					if(lblNewLabel32.getText().length()==0){		
						lblNewLabel32.setText(String.valueOf((int) (arg0.x/zoomRatio)));
						lblNewLabel33.setText(String.valueOf((int) (arg0.y/zoomRatio)));
						scanPointsX[0]=Integer.parseInt(lblNewLabel32.getText());
						scanPointsY[0]=Integer.parseInt(lblNewLabel33.getText());
						return;
					}
					if(lblNewLabel42.getText().length()==0){
						lblNewLabel42.setText(String.valueOf((int) (arg0.x/zoomRatio)));
						lblNewLabel43.setText(String.valueOf((int) (arg0.y/zoomRatio)));
						scanPointsX[1]=Integer.parseInt(lblNewLabel42.getText());
						scanPointsY[1]=Integer.parseInt(lblNewLabel43.getText());
						return;
					}
					if(lblNewLabel52.getText().length()==0){
						lblNewLabel52.setText(String.valueOf((int) (arg0.x/zoomRatio)));
						lblNewLabel53.setText(String.valueOf((int) (arg0.y/zoomRatio)));
						scanPointsX[2]=Integer.parseInt(lblNewLabel52.getText());
						scanPointsY[2]=Integer.parseInt(lblNewLabel53.getText());
						return;
					}
					if(lblNewLabel62.getText().length()==0){
						lblNewLabel62.setText(String.valueOf((int) (arg0.x/zoomRatio)));
						lblNewLabel63.setText(String.valueOf((int) (arg0.y/zoomRatio)));
						scanPointsX[3]=Integer.parseInt(lblNewLabel62.getText());
						scanPointsY[3]=Integer.parseInt(lblNewLabel63.getText());
					}
					if(lblNewLabel32.getText().length()!=0 && lblNewLabel33.getText().length()!=0 && lblNewLabel42.getText().length()!=0 && 
							lblNewLabel43.getText().length()!=0 && lblNewLabel52.getText().length()!=0 && lblNewLabel53.getText().length()!=0 &&
							lblNewLabel62.getText().length()!=0 && lblNewLabel63.getText().length()!=0){
						infoLabel.setText("Scan points saved");
						btnApply.setEnabled(true);
					}
				}
			});

			break;

		case GA_PAINTER:
			optionsBar.setText("GA Painter");
			optionsComposite.setLayout(new GridLayout(1, true));

			btnStop = new Button(optionsComposite, SWT.NONE);
			btnStop.setText("Stop");

		}
		optionsBar.layout();
	}

	private void apply(){
		if(image == null){ 
			showWarningMessage("Please select a file to work on");
			return;
		}
		if(selectedFunction == null){ 
			showWarningMessage("Please select a function");
			return;
		}
		if(IPList.length == 0){ 
			showWarningMessage("Please connect to at least one PC");
			return;
		}

		long t1= System.currentTimeMillis();
		progressBar.setSelection(0);

		Image[] imagesToModify;
		int count;	
		if((selectedFunction.isApplicableOnAllFiles()) && workOnAllFiles){
			imagesToModify= savedImages;
			count= 0;
		}
		else{
			imagesToModify= new Image[1];
			count= selectedImageNumber;
			imagesToModify[0]= savedImages[count];
		}

		int tasksLeft= imagesToModify.length;
		Buffer<Task> tasksToDo= new Buffer<Task>();
		Buffer<Result> tasksDone= new Buffer<Result>();

		java.util.List<Client> clients= new ArrayList<Client>();
		for(String s: IPList){
			Client c= new Client(s, tasksToDo, tasksDone);
			clients.add(c);
			c.start();
		}

		for(Image i: imagesToModify){		
			ImageData id= i.getImageData();
			BufferedImage input= FormatConversion.convertToAWT(id);

			Task task = null;

			switch(selectedFunction){
			case AUTO_BALANCE:
				task= new Task(input, selectedFunction, count, new int[]{nbThreads, autoBalanceType, blurSize.getSelection()});
				break;

			case BLUR:
				task= new Task(input, selectedFunction, count, new int[]{blurSize.getSelection()});
				break;

			case HDR_EQUALIZER:
				task= new Task(input, selectedFunction, count, new int[]{HDRAlgorithm.getSelectionIndex(), blurSize.getSelection()});
				break;
				
			case FOURIER_TRANSFORM:
				task= new Task(input, selectedFunction, count, new int[]{DFTMode.getSelectionIndex(), scaleMethod, cutFrequency.getSelection()});
				break;

			case SCAN:
				task= new Task(input, selectedFunction, count, new int[]{nbThreads,scanPointsX[0],scanPointsX[1],
						scanPointsX[2],scanPointsX[3],scanPointsY[0],scanPointsY[1],scanPointsY[2],scanPointsY[3],
						scanFormat.getSelectionIndex()});
				break;

			case GA_PAINTER:	
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
			default:
				break;	
			}

			try {
				tasksToDo.put(task);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
			count++;
		}

		while(tasksLeft>0){
			Result r;
			try {
				r = tasksDone.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
			BufferedImage output= new PImage(r.getResult()).getImage(); //nécessaire, sinon convertToSWT ne marche pas
			int number= r.getImageNumber();

			Image i= new Image(getDisplay(), FormatConversion.convertToSWT(output));
			savedImages[number]= i;	

			infoLabel.append("\n"+selectedFunction.getName()+" done for image "+(number+1));
			infoLabel.setTopIndex(infoLabel.getLineCount() - 1);

			tasksLeft--;
			progressBar.setSelection(100-(tasksLeft*100/imagesToModify.length));
		}

		for(Client c: clients) c.interrupt();

		if(selectedFunction==ImageFunction.SCAN) createOptionsMenu(); //réinitialisation du scan
		long t2= System.currentTimeMillis();
		infoLabel.append("\n"+"Time spent: "+((t2-t1)/1000.0)+" second(s)");
		refreshDisplay();
	}

	private void updateImageNumber() {
		imageNumber.setText((selectedImageNumber+1)+"/"+savedImages.length);
		titleLabel.setText(fileNames[selectedImageNumber].substring(fileNames[selectedImageNumber].lastIndexOf('/') + 1));
		refreshDisplay();
	}

	private void refreshDisplay(){
		Image img= savedImages[selectedImageNumber];
		if(img==null) return;
		
		int imgWidth=img.getBounds().width;
		int imgHeight=img.getBounds().height;

		double xratio=(double) (imageFrame.getClientArea().width)/imgWidth;
		double yratio=(double) (imageFrame.getClientArea().height)/imgHeight;

		zoomRatio=Math.min(xratio, yratio);	
		zoomLabel.setText(("Zoom: "+(int) (zoomRatio*100)+"%")+"\n"+"Original size: "+imgWidth+"*"+imgHeight);	

		image= new Image(display,img.getImageData().scaledTo( (int)(img.getBounds().width*zoomRatio), (int)(img.getBounds().height*zoomRatio)));

		if(gc==null || gc.isDisposed()) gc= new GC(imageFrame);
		gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRectangle(imageFrame.getClientArea());
		gc.drawImage(image, 0, 0);
		gc.dispose();
	}

	public static void showWarningMessage(String message){
		MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
		mb.setText("Warning");
		mb.setMessage(message);
		mb.open();
		return;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT display
	}

	public static void main(String[] args){
		display = new Display();
		shell = new Shell(display,SWT.SHELL_TRIM);

		GUI g=new GUI(shell,SWT.NONE);	
		shell.setText("Photowork");	
		shell.setLayout(new FillLayout());
		shell.setSize(1250,700);
		shell.setMinimumSize(1000,560); 

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
		// disposes all associated windows and their display
		System.out.println("Photowork closing");
		server.terminate();

		display.dispose();
		//	System.exit(0);
	}
}