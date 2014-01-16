package display;

import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import network.Server;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import filter.ImageFunction;

/**
 * La classe principale. Base de l'IHM.
 *
 */
public class GUI extends Composite  {

	//BASE DE LA FENETRE SWT
	//moniteur d'affichage
	private static Display display;
	//fenetre principale
	private static Shell shell;

	//RESEAU
	private static DisplayUpdater updater;
	private static Server server; //traite les taches envoyees par les PC

	//AFFICHAGE GRAPHIQUE
	//Barre d'options
	private Group optionsBar;
	private Composite optionsComposite;

	//Zone d'affichage d'image
	private Composite imageFrame;
	private double zoomRatio = 1;
	private Image image; //image redimensionnee pour l'affichage
	private GC gc;	//Dessins sur l'affichage d'image

	//Zones d'affichage d'informations
	private Text titleLabel, infoLabel, zoomLabel;
	private CLabel imageNumber;

	//Boutons particuliers
	Button btnApply;
	Button btnStop;	//GA Painter

	//Barres de progression
	private PWProgressBar globalProgressBar;
	private PWProgressBar localProgressBar;


	//PARAMETRES
	//Donnees sur l'affichage d'image
	int selectedImageNumber;
	Image[] savedImages; //Images telles qu'elles sont actuellement (taille normale)
	Image[] originalImages; //Images telles qu'elles etaient lors du chargement

	//Nom des fichiers charges
	String[] fileNames;

	//Nom de la fonction selectionnee
	ImageFunction selectedFunction;

	//Fonctions de filtre (Auto Balance, Blur, HDR)
	int autoBalanceType, blurSize, HDRAlgorithm; //Type d'autobalance: 0:simple, 1:equilibrage couleurs, 2: equilibrage avec flou

	//DFT
	int DFTMode, scaleMethod, cutFrequency;

	//Scan
	int[] scanPointsX = new int[4];
	int[] scanPointsY = new int[4];
	int scanFormat;

	//GA Painter
	int nbTriangles;
	int nbCircles;

	//Definis avec le menu Preferences
	boolean workOnAllFiles = true;
	int nbThreads = PreferencesMenu.AVAILABLE_THREADS;
	String[] IPList;


	public GUI(Composite parent, int style) throws BindException {
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 4));
		GridLayout gl = (new GridLayout(6, true));
		gl.makeColumnsEqualWidth= true;
		setLayout(gl);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_SELECTION));

		Composite compositeTitle = new Composite(this, SWT.NONE);
		compositeTitle.setLayout(new GridLayout(3, false));
		compositeTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		titleLabel = new Text(compositeTitle, SWT.NONE);
		titleLabel.setEditable(false);
		titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		titleLabel.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		titleLabel.setText("No image selected");

		Button btnRefresh = new Button(compositeTitle, SWT.NONE);
		btnRefresh.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
		btnRefresh.setImage(new Image(display,"images/refresh.png"));
		btnRefresh.addSelectionListener(new SelectionAdapter() {
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
			public void widgetSelected(SelectionEvent arg0) {
				if(updater!=null && updater.isAlive()){ 
					showWarningMessage("Please wait for the current treatment to end");
					return;
				}

				FileBrowser browser= new FileBrowser(display, SWT.OPEN);
				if(browser.chosenFiles!=null){
					fileNames = browser.chosenFiles;
					savedImages= new Image[fileNames.length];
					originalImages= new Image[fileNames.length];
					for(int i=0; i< fileNames.length; i++){
						savedImages[i] = new Image(display, fileNames[i]);
						originalImages[i] = new Image(display, fileNames[i]);
					}

					zoomRatio = 1;
					zoomLabel.setText(("Zoom: "+100+"%"));
					print("Opened "+fileNames.length+" file(s)",true);	

					selectedImageNumber = 0;
					refreshDisplay();
				}
			}
		});

		Button btnSaveFiles = new Button(this, SWT.NONE);
		btnSaveFiles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnSaveFiles.setText("Save Files");
		btnSaveFiles.setImage(new Image(display,"images/save.png"));
		btnSaveFiles.addSelectionListener(new SelectionAdapter() {
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
					print("Saved in: "+name,false);
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
			public void widgetSelected(SelectionEvent arg0) {
				if(savedImages == null) return;
				if(selectedImageNumber == 0) selectedImageNumber= savedImages.length-1;	
				else selectedImageNumber--;
				refreshDisplay();
			}
		});

		imageNumber = new CLabel(compositeImageNumber, SWT.CENTER);
		imageNumber.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		imageNumber.setText("0/0");

		Button btnNext = new Button(compositeImageNumber, SWT.ARROW | SWT.RIGHT);
		btnNext.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {				
				if(savedImages==null) return;
				if(selectedImageNumber == savedImages.length-1) selectedImageNumber = 0;	
				else selectedImageNumber++;
				refreshDisplay();
			}
		});

		final Button btnPreferences = new Button(this, SWT.NONE);
		btnPreferences.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		btnPreferences.setImage(new Image(display,"images/prefs.png"));
		btnPreferences.setText("Preferences");
		btnPreferences.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				createPreferencesMenu();
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
		grpAllImages.setLayout(new GridLayout(2, false));
		grpAllImages.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpAllImages.setBackground(SWTResourceManager.getColor(SWT.COLOR_LINK_FOREGROUND));
		grpAllImages.setText("All images");

		Button btnAutoBalance = new Button(grpAllImages, SWT.NONE);
		btnAutoBalance.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnAutoBalance.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		btnAutoBalance.setImage(new Image(display,"images/autoBalance.png"));
		btnAutoBalance.setText("Auto Balance");	
		btnAutoBalance.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				selectedFunction= ImageFunction.AUTO_BALANCE;
				createOptionsMenu();
			}
		});

		Button btnABHelp = new Button(grpAllImages, SWT.NONE);
		btnABHelp.setText("?");
		btnABHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));	
		btnABHelp.setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
		btnABHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage(new Image(display,"images/autoBalance.png"));
			}
		});

		Button btnBlur = new Button(grpAllImages, SWT.NONE);
		btnBlur.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnBlur.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		btnBlur.setImage(new Image(display,"images/blur.png"));
		btnBlur.setText("Blur");
		btnBlur.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				selectedFunction= ImageFunction.BLUR;
				createOptionsMenu();
			}
		});

		Button btnBlurHelp = new Button(grpAllImages, SWT.NONE);
		btnBlurHelp.setText("?");
		btnBlurHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		btnBlurHelp.setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
		btnBlurHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage(new Image(display,"images/blur.png"));
			}
		});

		Button btnHDR = new Button(grpAllImages, SWT.NONE);
		btnHDR.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnHDR.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		btnHDR.setImage(new Image(display,"images/hdr.png"));
		btnHDR.setText("HDR Equalizer");
		btnHDR.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				selectedFunction= ImageFunction.HDR_EQUALIZER;
				createOptionsMenu();
			}
		});

		Button btnHDRHelp = new Button(grpAllImages, SWT.NONE);
		btnHDRHelp.setText("?");
		btnHDRHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		btnHDRHelp.setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
		btnHDRHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage(new Image(display,"images/hdr.png"));
			}
		});

		Button btnFourier = new Button(grpAllImages, SWT.NONE);
		btnFourier.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnFourier.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		btnFourier.setImage(new Image(display,"images/DFT.png"));
		btnFourier.setText("DFT");	
		btnFourier.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				selectedFunction= ImageFunction.FOURIER_TRANSFORM;
				createOptionsMenu();
			}
		});

		Button btnDFTHelp = new Button(grpAllImages, SWT.NONE);
		btnDFTHelp.setText("?");
		btnDFTHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		btnDFTHelp.setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
		btnDFTHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage(new Image(display,"images/DFT.png"));
			}
		});

		Group grpCurrentImageOnly = new Group(compositeMenu, SWT.NONE);
		grpCurrentImageOnly.setLayout(new GridLayout(2, false));
		grpCurrentImageOnly.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpCurrentImageOnly.setBackground(SWTResourceManager.getColor(SWT.COLOR_LINK_FOREGROUND));
		grpCurrentImageOnly.setText("Current image only");


		Button btnScan = new Button(grpCurrentImageOnly, SWT.NONE);
		btnScan.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnScan.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		btnScan.setImage(new Image(display,"images/scanner.png"));
		btnScan.setText("Scan");		
		btnScan.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				selectedFunction= ImageFunction.SCAN;
				createOptionsMenu();
			}
		});

		Button btnScanHelp = new Button(grpCurrentImageOnly, SWT.NONE);
		btnScanHelp.setText("?");
		btnScanHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		btnScanHelp.setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
		btnScanHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage(new Image(display,"images/scanner.png"));
			}
		});

		Button btnGAPainter = new Button(grpCurrentImageOnly, SWT.NONE);
		btnGAPainter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnGAPainter.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		btnGAPainter.setImage(new Image(display,"images/GA.png"));
		btnGAPainter.setText("GA Painter");		
		btnGAPainter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				selectedFunction= ImageFunction.GA_PAINTER;
				createOptionsMenu();
			}
		});

		Button btnGAHelp = new Button(grpCurrentImageOnly, SWT.NONE);
		btnGAHelp.setText("?");
		btnGAHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		btnGAHelp.setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
		btnGAHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage(new Image(display,"images/GA.png"));
			}
		});

		imageFrame = new Composite(this, SWT.BORDER | SWT.DOUBLE_BUFFERED);
		imageFrame.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		imageFrame.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				if(savedImages != null) refreshDisplay();
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
		print("Ready",true);

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.VERTICAL));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		globalProgressBar = new PWProgressBar(composite, "overall");
		localProgressBar = new PWProgressBar(composite, "current");

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
			public void widgetSelected(SelectionEvent arg0) {
				apply();
			}
		});

		try {
			IPList = new String[]{InetAddress.getLocalHost().getHostAddress()};
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		try {
			server = new Server();
			server.start();
		} catch (BindException e1) {
			throw e1;
		}
	}


	/**
	 *  Cree un menu d'options dependant de la fonction choisie.
	 */
	void createOptionsMenu(){
		if(image == null){ 
			showWarningMessage("Please select a file to work on");
			return;
		}
		if(!optionsComposite.isDisposed()){
			if(optionsBar.getText() == "Scan"){
				refreshDisplay(); //on enleve les points d'un scan non fini
				btnApply.setEnabled(true);	

				Listener[] listeners = imageFrame.getListeners(SWT.MouseDown);
				for(int i = 0 ; i< listeners.length; i++){
					imageFrame.removeListener(SWT.MouseDown, listeners[i]);
				}
			}

			optionsComposite.dispose();
			optionsComposite = new Composite(optionsBar, SWT.FILL);
			optionsComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		}

		optionsBar.setText(selectedFunction.getName());

		switch(selectedFunction){
		case AUTO_BALANCE:
			optionsComposite.setLayout(new GridLayout(1, true));

			autoBalanceType = 0;	
			blurSize = 10;

			final Button btnNewButton_10 = new Button(optionsComposite, SWT.RADIO);
			btnNewButton_10.setText("Balance Brightness");
			btnNewButton_10.setSelection(true);

			final Button btnNewButton_11 = new Button(optionsComposite, SWT.RADIO);
			btnNewButton_11.setText("Balance Colors");


			final Button blurCheckButton = new Button(optionsComposite, SWT.CHECK);
			blurCheckButton.setText("Blur");
			blurCheckButton.setEnabled(false);

			Label lblNewLabel = new Label(optionsComposite, SWT.NONE);
			lblNewLabel.setText("Blur Size (px):");
			final Spinner size = new Spinner(optionsComposite, SWT.BORDER);
			size.setSelection(blurSize);
			size.setEnabled(false);	

			size.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					blurSize= size.getSelection();
				}
			});
			btnNewButton_10.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					if(btnNewButton_10.getSelection()){
						autoBalanceType = 0;
						blurCheckButton.setEnabled(false);
						size.setEnabled(false);
					}
				}
			});			
			btnNewButton_11.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					if(btnNewButton_11.getSelection()){
						autoBalanceType = 1;
						blurCheckButton.setEnabled(true);
						blurCheckButton.setSelection(false);
					}
				}
			});
			blurCheckButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					size.setEnabled(blurCheckButton.getSelection());
					if(blurCheckButton.getSelection()) autoBalanceType = 2;
					else autoBalanceType = 1;
				}
			});
			break;

		case BLUR:
			optionsComposite.setLayout(new GridLayout(1, true));
			blurSize = 10;

			Label lblNewLabel1 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel1.setText("Blur Size (px):");
			final Spinner size2 = new Spinner(optionsComposite, SWT.BORDER);

			size2.setSelection(blurSize);
			size2.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					blurSize = size2.getSelection();
				}
			});
			break;

		case HDR_EQUALIZER:
			optionsComposite.setLayout(new GridLayout(1, true));
			blurSize = 10;
			HDRAlgorithm = 0;

			Label lblNewLabel2 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel2.setText("Blur Size (px):");
			final Spinner size3 = new Spinner(optionsComposite, SWT.BORDER);	
			size3.setSelection(blurSize);
			size3.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					blurSize = size3.getSelection();
				}
			});

			Label lblNewLabel21 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel21.setText("Algorithm:");
			final List algorithm = new List(optionsComposite, SWT.NONE);
			algorithm.add("Adaptive histogram equalization");
			algorithm.add("Bas-relief from depth map");
			algorithm.setSelection(HDRAlgorithm);
			algorithm.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					HDRAlgorithm = algorithm.getSelectionIndex();
				}
			});
			break;

		case FOURIER_TRANSFORM:
			optionsComposite.setLayout(new GridLayout(1, true));
			DFTMode = 0;
			scaleMethod = 1;
			cutFrequency = 8;
			final Button btnLog,btnLin;

			Label lblNewLabel22 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel22.setText("DFT Type:");
			final List mode = new List(optionsComposite, SWT.CHECK);
			mode.add("Image spectrum");
			mode.add("High-pass filter");
			mode.setSelection(DFTMode);		

			Label lblNewLabel23 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel23.setText("Scaling:");	
			btnLog = new Button(optionsComposite, SWT.RADIO);
			btnLog.setText("Logarithmic");
			btnLog.setSelection(true);
			btnLog.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					if(btnLog.getSelection()){
						scaleMethod = 1;
					}
				}
			});
			btnLin = new Button(optionsComposite, SWT.RADIO);
			btnLin.setText("Linear");
			btnLin.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					if(btnLin.getSelection()){
						scaleMethod = 0;
					}
				}
			});	


			Label lblNewLabel24 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel24.setText("Cutoff frequency");
			final Spinner cutoff = new Spinner(optionsComposite, SWT.BORDER);
			cutoff.setEnabled(false);
			cutoff.setSelection(cutFrequency);
			cutoff.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					cutFrequency = cutoff.getSelection();
				}
			});
			mode.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					DFTMode= mode.getSelectionIndex();
					cutoff.setEnabled(DFTMode == 1);
					btnLog.setEnabled(DFTMode == 0);
					btnLin.setEnabled(DFTMode == 0);
				}
			});
			break;

		case SCAN:
			optionsComposite.setLayout(new GridLayout(2, true));
			btnApply.setEnabled(false);

			Label lblNewLabel3 = new Label(optionsComposite, SWT.NONE);	
			lblNewLabel3.setText("Select image points");		
			new Label(optionsComposite, SWT.NONE);

			Label lblNewLabel31 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel31.setText("Point 1: Upper Left");
			new Label(optionsComposite, SWT.NONE);
			final Text lblNewLabel32 = new Text(optionsComposite, SWT.NONE); lblNewLabel32.setEditable(false);
			final Text lblNewLabel33 = new Text(optionsComposite, SWT.NONE); lblNewLabel33.setEditable(false);

			Label lblNewLabel41 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel41.setText("Point 2: Upper Right");	
			new Label(optionsComposite, SWT.NONE);
			final Text lblNewLabel42 = new Text(optionsComposite, SWT.NONE); lblNewLabel42.setEditable(false);
			final Text lblNewLabel43 = new Text(optionsComposite, SWT.NONE); lblNewLabel43.setEditable(false);

			Label lblNewLabel51 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel51.setText("Point 3: Lower Left");
			new Label(optionsComposite, SWT.NONE);
			final Text lblNewLabel52 = new Text(optionsComposite, SWT.NONE); lblNewLabel52.setEditable(false);
			final Text lblNewLabel53 = new Text(optionsComposite, SWT.NONE); lblNewLabel53.setEditable(false);

			Label lblNewLabel61 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel61.setText("Point 4: Lower Right");
			new Label(optionsComposite, SWT.NONE);
			final Text lblNewLabel62 = new Text(optionsComposite, SWT.NONE); lblNewLabel62.setEditable(false);
			final Text lblNewLabel63 = new Text(optionsComposite, SWT.NONE); lblNewLabel63.setEditable(false);

			Button btnClear = new Button(optionsComposite, SWT.NONE);
			btnClear.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					refreshDisplay();
					lblNewLabel32.setText("");lblNewLabel33.setText("");
					lblNewLabel42.setText("");lblNewLabel43.setText("");
					lblNewLabel52.setText("");lblNewLabel53.setText("");
					lblNewLabel62.setText("");lblNewLabel63.setText("");
					btnApply.setEnabled(false);
				}
			});
			btnClear.setText("Clear");		
			new Label(optionsComposite, SWT.NONE);

			final List format = new List(optionsComposite, SWT.V_SCROLL);
			format.setItems(new String[] {"A0 (841*1189)", "A1 (594*841)", "A2 (420*594)", "A3 (297*420)",
					"A4 (210*297)", "A5 (148*210)", "A6 (105*148)"});
			format.setSelection(4);
			format.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					scanFormat = format.getSelectionIndex();
				}
			});	

			imageFrame.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent arg0) {
					if(gc == null || gc.isDisposed()) gc= new GC(imageFrame);
					gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
					gc.fillOval(arg0.x-5, arg0.y-5, 10, 10);
					int x= (int) Math.min(savedImages[selectedImageNumber].getBounds().width-1, arg0.x/zoomRatio);
					int y= (int) Math.min(savedImages[selectedImageNumber].getBounds().height-1, arg0.y/zoomRatio);
					if(lblNewLabel32.getText().length() == 0){		
						lblNewLabel32.setText(String.valueOf(x)); lblNewLabel33.setText(String.valueOf(y));
						scanPointsX[0] = x; scanPointsY[0] = y;
						return;
					}
					if(lblNewLabel42.getText().length() == 0){
						lblNewLabel42.setText(String.valueOf(x)); lblNewLabel43.setText(String.valueOf(y));
						scanPointsX[1] = x; scanPointsY[1] = y;
						return;
					}
					if(lblNewLabel52.getText().length() == 0){
						lblNewLabel52.setText(String.valueOf(x)); lblNewLabel53.setText(String.valueOf(y));
						scanPointsX[2] = x; scanPointsY[2] = y;
						return;
					}
					if(lblNewLabel62.getText().length() == 0){
						lblNewLabel62.setText(String.valueOf(x)); lblNewLabel63.setText(String.valueOf(y));
						scanPointsX[3] = x; scanPointsY[3] = y;
					}
					for(int i=0; i<4; i++){
						for(int j=0; j<4; j++){
							if(i!=j){
								if(Math.pow(scanPointsX[j]-scanPointsX[i],2)+Math.pow(scanPointsY[j]-scanPointsY[i],2)==0){
									showWarningMessage("All four points must be distinct");
									refreshDisplay();
									lblNewLabel32.setText(""); lblNewLabel33.setText("");
									lblNewLabel42.setText(""); lblNewLabel43.setText("");
									lblNewLabel52.setText(""); lblNewLabel53.setText("");
									lblNewLabel62.setText(""); lblNewLabel63.setText("");
									return;
								}
							}
						}
					}
					print("\n"+"Scan points saved",false);
					btnApply.setEnabled(true);
				}
			});

			break;

		case GA_PAINTER:
			optionsBar.setText("GA Painter");
			optionsComposite.setLayout(new GridLayout(1, true));

			btnStop = new Button(optionsComposite, SWT.NONE);
			btnStop.setText("Stop");

			nbTriangles = 100;
			nbCircles = 100;

			Label lblNewLabel7 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel7.setText("Number of triangles");
			final Spinner triangles = new Spinner(optionsComposite, SWT.BORDER);

			triangles.setSelection(nbTriangles);
			triangles.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					nbTriangles = triangles.getSelection();
				}
			});

			Label lblNewLabel8 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel8.setText("Number of circles");
			final Spinner circles = new Spinner(optionsComposite, SWT.BORDER);

			circles.setSelection(nbCircles);
			circles.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					nbCircles = circles.getSelection();
				}
			});

		}
		optionsBar.layout();
	}

	/**
	 *  Cree le menu de preferences, et met a jour le GUI a sa fermeture.
	 */
	private void createPreferencesMenu() {
		if(image == null){ 
			showWarningMessage("Please select a file to work on");
			return;
		}

		final Shell prefShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		final PreferencesMenu preferences = new PreferencesMenu(this, prefShell, SWT.NONE);
		prefShell.setText("Preferences");	
		prefShell.setLayout(new FillLayout());
		prefShell.setSize(700,400); 
		openShell(prefShell);

		prefShell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				print("\n"+"Preferences changes saved",false);
				workOnAllFiles = preferences.areAllFilesSelected();
				nbThreads = preferences.getNbThreads();
				IPList = preferences.getIPList();
			}
		});

		while (!prefShell.isDisposed()) {
			if (!display.readAndDispatch())	display.sleep();
		}

	}

	/**
	 * Lance un traitement pour la fonction selectionnee. Un seul traitement peut avoir lieu simultanement sur un meme
	 * ordinateur.
	 */
	private void apply(){
		if(image == null){ 
			showWarningMessage("Please select a file to work on");
			return;
		}	
		if(updater != null && updater.isAlive()){ 
			showWarningMessage("Please wait for the current treatment to end");
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

		updater = new DisplayUpdater(this);
		updater.start();
	}

	/**
	 * Rafraichit l'image en train d'etre visionnee.
	 */
	void refreshDisplay(){
		imageNumber.setText((selectedImageNumber+1)+"/"+savedImages.length);
		titleLabel.setText(fileNames[selectedImageNumber].substring(fileNames[selectedImageNumber].lastIndexOf('/') + 1));
		Image img = savedImages[selectedImageNumber];
		if(img == null) return;

		int imgWidth = img.getBounds().width;
		int imgHeight = img.getBounds().height;

		double xratio=(double) (imageFrame.getClientArea().width)/imgWidth;
		double yratio=(double) (imageFrame.getClientArea().height)/imgHeight;

		zoomRatio = Math.min(xratio, yratio);	
		zoomLabel.setText(("Zoom: "+(int) (zoomRatio*100)+"%")+"\n"+"Original size: "+imgWidth+"*"+imgHeight);	

		image= new Image(display,img.getImageData().scaledTo( (int)(img.getBounds().width*zoomRatio), (int)(img.getBounds().height*zoomRatio)));

		if(gc == null || gc.isDisposed()) gc = new GC(imageFrame);
		gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRectangle(imageFrame.getClientArea());
		gc.drawImage(image, 0, 0);
		gc.dispose();

		//shell.layout();
	}

	/**
	 * Enregistre une image traitee dans le tableau d'images.
	 * @param img l'image traitee
	 * @param number le numero de l'image
	 */
	void updateImage(Image img, int number){
		if(savedImages != null){
			savedImages[number] = img;
			refreshDisplay();
		}
	}

	void setGlobalProgressBarSelection(double selection){
		globalProgressBar.setProgress(selection);
	}
	void setLocalProgressBarSelection(double selection){
		localProgressBar.setProgress(selection);
	}

	/**
	 * Ecrit un message dans la zone en bas a gauche de l'interface
	 * @param s
	 * @param clear indique si la zone d'affichage est reinitialisee
	 */
	void print(String s, boolean clear){
		if(clear) infoLabel.setText("");
		infoLabel.append(s);
		infoLabel.setTopIndex(infoLabel.getLineCount() - 1);
	}

	static void showWarningMessage(String message){
		MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
		mb.setText("Warning");
		mb.setMessage(message);
		mb.open();
		return;
	}
	static void showHelpMessage(Image description){	
		Shell helpShell= new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

		helpShell.setText("Help");	
		helpShell.setLayout(new FillLayout());
		helpShell.setSize(description.getBounds().width,description.getBounds().height); 

		Label l= new Label(helpShell, SWT.NONE);
		l.setImage(description);

		openShell(helpShell);

		while (!helpShell.isDisposed()) {
			if (!display.readAndDispatch())	display.sleep();
		}
	}

	/**
	 * Ouvre une fenetre en la centrant sur l'ecran
	 * @param shell
	 */
	static void openShell(Shell shell) {
		Rectangle screenSize = display.getPrimaryMonitor().getBounds();
		shell.setLocation((screenSize.width - shell.getBounds().width) / 2, (screenSize.height - shell.getBounds().height) / 2);

		shell.open();
		shell.forceActive();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT display
	}

	public static void createGUI(){
		display = new Display();
		shell = new Shell(display,SWT.SHELL_TRIM);

		GUI g= null;
		try {
			g = new GUI(shell,SWT.NONE);
		} catch (BindException e) {
			showWarningMessage("One instance of PhotoWork is already running");
			return;
		}	
		shell.setText("PhotoWork");	
		shell.setLayout(new FillLayout());
		shell.setSize(1250,700);
		shell.setMinimumSize(1000,560); 

		openShell(shell);

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
		System.out.println("PhotoWork closing");
		server.terminate();
		if(updater != null && updater.isAlive()) updater.interrupt();

		display.dispose();
		//	System.exit(0);
	}
}