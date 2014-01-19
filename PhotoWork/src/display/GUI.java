package display;

import filter.ImageFunction;
import imageComputing.NetworkServer;

import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
	private static NetworkServer server; //traite les taches envoyees par les PC

	//AFFICHAGE GRAPHIQUE
	//Barre d'options
	private Group optionsMenu;
	private Group optionsGroup;

	//Zone d'affichage d'image
	private Composite imageFrame;
	private static double zoomRatio = 1;
	private Image image; //image redimensionnee pour l'affichage
	private GC gc;	//Dessins sur l'affichage d'image

	//Zones d'affichage d'informations
	private Text titleLabel, infoLabel, zoomLabel;
	private CLabel imageNumber;

	//Boutons particuliers
	Button btnApply;
	Button btnClearScan; //Scan
	private Text scanULx, scanULy, scanURx, scanURy, scanLLx, scanLLy, scanLRx, scanLRy;
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
	int autoBalanceType, blurSize, HDRAlgorithm; 
	//Type d'autobalance: 0:simple, 1:equilibrage couleurs, 2: equilibrage avec flou

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


	//Couleurs
	static final int FUNCTION_COLOR = SWT.COLOR_WHITE;
	static final int HELP_COLOR = SWT.COLOR_WHITE;
	static final int APPLY_COLOR = SWT.COLOR_GREEN;
	static final int UNDO_COLOR = SWT.COLOR_RED;
	static final int BACKGROUND_COLOR = SWT.COLOR_DARK_CYAN;
	static final int MENU_COLOR = SWT.COLOR_GRAY;
	static final int WINDOW_COLOR = SWT.COLOR_DARK_GRAY;
	static final int TEXT_COLOR = SWT.COLOR_BLACK;


	public GUI(Composite parent, int style) throws BindException {
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 4));
		GridLayout gl = (new GridLayout(6, true));
		gl.makeColumnsEqualWidth= true;
		setLayout(gl);
		setBackground(SWTResourceManager.getColor(BACKGROUND_COLOR));

		Composite compositeTitle = new Composite(this, SWT.NONE);
		compositeTitle.setLayout(new GridLayout(3, false));
		compositeTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		titleLabel = new Text(compositeTitle, SWT.NONE);
		titleLabel.setEditable(false);
		titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		titleLabel.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		titleLabel.setText("No image selected");

		Button btnRefresh = new Button(compositeTitle, SWT.NONE);
		btnRefresh.setBackground(SWTResourceManager.getColor(APPLY_COLOR));
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
		btnUndo.setBackground(SWTResourceManager.getColor(UNDO_COLOR));
		btnUndo.setImage(new Image(display,"images/undo.png"));
		btnUndo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if(image == null){ 
					showWarningMessage("Please select a file to work on");
					return;
				}

				for(int i=0; i<savedImages.length; i++)
					savedImages[i]= new Image(display, originalImages[i], SWT.IMAGE_COPY);	
				refreshDisplay();
				if(selectedFunction == ImageFunction.SCAN) clearScan(); 	//force un reinit du scan
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

				FileBrowser browser = new FileBrowser(display, SWT.OPEN);
				if(browser.chosenFiles != null){
					fileNames = browser.chosenFiles;

					if(savedImages != null){  //liberation ressources
						for(int i=0; i< savedImages.length; i++){
							savedImages[i].dispose();
							originalImages[i].dispose();
						}
					}

					savedImages = new Image[fileNames.length];
					originalImages = new Image[fileNames.length];
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

				if(browser.chosenFiles[0] != null){
					String name= browser.chosenFiles[0];
					ImageLoader loader = new ImageLoader();

					if(savedImages.length == 1){  
						loader.data = new ImageData[] {savedImages[0].getImageData()};
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

		Group menu = new Group(this, SWT.NONE);
		menu.setForeground(SWTResourceManager.getColor(TEXT_COLOR));
		menu.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
		menu.setLayout(new GridLayout(1, false));
		menu.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		menu.setBackground(SWTResourceManager.getColor(MENU_COLOR));
		menu.setText("Menu");

		Group grpAllImages = new Group(menu, SWT.NONE);
		grpAllImages.setLayout(new GridLayout(2, false));
		grpAllImages.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpAllImages.setBackground(SWTResourceManager.getColor(WINDOW_COLOR));
		grpAllImages.setText("All images");

		Button btnAutoBalance = new Button(grpAllImages, SWT.NONE);
		btnAutoBalance.setAlignment(SWT.LEFT);
		btnAutoBalance.setBackground(SWTResourceManager.getColor(FUNCTION_COLOR));
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
		btnABHelp.setBackground(SWTResourceManager.getColor(HELP_COLOR));
		btnABHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage(new Image(display,"images/autoBalance.png"));
			}
		});

		Button btnBlur = new Button(grpAllImages, SWT.NONE);
		btnBlur.setAlignment(SWT.LEFT);
		btnBlur.setBackground(SWTResourceManager.getColor(FUNCTION_COLOR));
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
		btnBlurHelp.setBackground(SWTResourceManager.getColor(HELP_COLOR));
		btnBlurHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage(new Image(display,"images/blur.png"));
			}
		});

		Button btnHDR = new Button(grpAllImages, SWT.NONE);
		btnHDR.setAlignment(SWT.LEFT);
		btnHDR.setBackground(SWTResourceManager.getColor(FUNCTION_COLOR));
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
		btnHDRHelp.setBackground(SWTResourceManager.getColor(HELP_COLOR));
		btnHDRHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage(new Image(display,"images/hdr.png"));
			}
		});

		Button btnFourier = new Button(grpAllImages, SWT.NONE);
		btnFourier.setAlignment(SWT.LEFT);
		btnFourier.setBackground(SWTResourceManager.getColor(FUNCTION_COLOR));
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
		btnDFTHelp.setBackground(SWTResourceManager.getColor(HELP_COLOR));
		btnDFTHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage(new Image(display,"images/DFT.png"));
			}
		});

		Group grpCurrentImageOnly = new Group(menu, SWT.NONE);
		grpCurrentImageOnly.setLayout(new GridLayout(2, false));
		grpCurrentImageOnly.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpCurrentImageOnly.setBackground(SWTResourceManager.getColor(WINDOW_COLOR));
		grpCurrentImageOnly.setText("Current image only");


		Button btnScan = new Button(grpCurrentImageOnly, SWT.NONE);
		btnScan.setAlignment(SWT.LEFT);
		btnScan.setBackground(SWTResourceManager.getColor(FUNCTION_COLOR));
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
		btnScanHelp.setBackground(SWTResourceManager.getColor(HELP_COLOR));
		btnScanHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage(new Image(display,"images/scanner.png"));
			}
		});

		Button btnGAPainter = new Button(grpCurrentImageOnly, SWT.NONE);
		btnGAPainter.setAlignment(SWT.LEFT);
		btnGAPainter.setBackground(SWTResourceManager.getColor(FUNCTION_COLOR));
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
		btnGAHelp.setBackground(SWTResourceManager.getColor(HELP_COLOR));
		btnGAHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage(new Image(display,"images/GA.png"));
			}
		});

		imageFrame = new Composite(this, SWT.BORDER);
		imageFrame.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));	
		imageFrame.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e){
				refreshDisplay();
			}
		}); 

		optionsMenu = new Group(this, SWT.NONE);
		optionsMenu.setForeground(SWTResourceManager.getColor(TEXT_COLOR));
		optionsMenu.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
		optionsMenu.setLayout(new GridLayout(1, false));
		optionsMenu.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		optionsMenu.setBackground(SWTResourceManager.getColor(MENU_COLOR));
		optionsMenu.setText("Welcome !");

		optionsGroup = new Group(optionsMenu, SWT.FILL);
		optionsGroup.setVisible(false);

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
		btnApply.setBackground(SWTResourceManager.getColor(APPLY_COLOR));
		btnApply.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				apply();
			}
		});

		try {
			IPList = new String[]{InetAddress.getLocalHost().getHostAddress()};
			server = new NetworkServer();
			server.start();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

	}


	/**
	 *  Cree un menu d'options dependant de la fonction choisie.
	 */
	private void createOptionsMenu(){
		if(image == null){ 
			showWarningMessage("Please select a file to work on");
			return;
		}
		if(!optionsGroup.isDisposed()){
			if(optionsMenu.getText() == "Scan"){
				refreshDisplay(); //on enleve les points d'un scan non fini
				btnApply.setEnabled(true);	

				Listener[] listeners = imageFrame.getListeners(SWT.MouseDown);
				for(int i = 0 ; i< listeners.length; i++){
					imageFrame.removeListener(SWT.MouseDown, listeners[i]);
				}
			}

			optionsGroup.dispose();
			optionsGroup = new Group(optionsMenu, SWT.FILL);
			optionsGroup.setText("Parameters");
			optionsGroup.setBackground(SWTResourceManager.getColor(WINDOW_COLOR));
		}

		optionsMenu.setText(selectedFunction.getName());

		switch(selectedFunction){
		case AUTO_BALANCE:
			optionsGroup.setLayout(new GridLayout(1, true));
			optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			autoBalanceType = 0;	
			blurSize = 10;

			final Button btnNewButton_10 = new Button(optionsGroup, SWT.RADIO);
			btnNewButton_10.setText("Balance Brightness");
			btnNewButton_10.setSelection(true);

			final Button btnNewButton_11 = new Button(optionsGroup, SWT.RADIO);
			btnNewButton_11.setText("Balance Colors");

			final Button blurCheckButton = new Button(optionsGroup, SWT.CHECK);
			blurCheckButton.setText("Blur");
			blurCheckButton.setEnabled(false);

			Label lblNewLabel = new Label(optionsGroup, SWT.NONE);
			lblNewLabel.setText("Blur Size (px):");

			final Spinner size = new Spinner(optionsGroup, SWT.BORDER);
			size.setSelection(blurSize);
			size.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			size.setEnabled(false);
			size.setMaximum(1000);
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
			optionsGroup.setLayout(new GridLayout(1, true));
			optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			blurSize = 10;

			Label lblNewLabel1 = new Label(optionsGroup, SWT.NONE);
			lblNewLabel1.setText("Blur Size (px):");

			final Spinner size2 = new Spinner(optionsGroup, SWT.BORDER);
			size2.setMaximum(1000);
			size2.setSelection(blurSize);
			size2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			size2.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					blurSize = size2.getSelection();
				}
			});
			break;

		case HDR_EQUALIZER:
			optionsGroup.setLayout(new GridLayout(1, true));
			optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			blurSize = 30;
			HDRAlgorithm = 1;

			Label lblNewLabel2 = new Label(optionsGroup, SWT.NONE);
			lblNewLabel2.setText("Size (px):");

			final Spinner size3 = new Spinner(optionsGroup, SWT.BORDER);	
			size3.setSelection(blurSize);
			size3.setMaximum(1000);
			size3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			size3.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					blurSize = size3.getSelection();
				}
			});

			Label lblNewLabel21 = new Label(optionsGroup, SWT.NONE);
			lblNewLabel21.setText("Algorithm:");
			final List algorithm = new List(optionsGroup, SWT.NONE);
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
			optionsGroup.setLayout(new GridLayout(1, true));
			optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			DFTMode = 0;
			scaleMethod = 1;
			cutFrequency = 8;
			final Button btnLog,btnLin;

			Label lblNewLabel22 = new Label(optionsGroup, SWT.NONE);
			lblNewLabel22.setText("DFT Type:");
			final List mode = new List(optionsGroup, SWT.CHECK);
			mode.add("Image spectrum");
			mode.add("High-pass filter");
			mode.setSelection(DFTMode);		

			Label lblNewLabel23 = new Label(optionsGroup, SWT.NONE);
			lblNewLabel23.setText("Scaling:");	
			btnLog = new Button(optionsGroup, SWT.RADIO);
			btnLog.setText("Logarithmic");
			btnLog.setSelection(true);
			btnLog.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					if(btnLog.getSelection()){
						scaleMethod = 1;
					}
				}
			});
			btnLin = new Button(optionsGroup, SWT.RADIO);
			btnLin.setText("Linear");
			btnLin.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					if(btnLin.getSelection()){
						scaleMethod = 0;
					}
				}
			});	


			Label lblNewLabel24 = new Label(optionsGroup, SWT.NONE);
			lblNewLabel24.setText("Cutoff frequency");

			final Spinner cutoff = new Spinner(optionsGroup, SWT.BORDER);
			cutoff.setEnabled(false);
			cutoff.setSelection(cutFrequency);
			cutoff.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			cutoff.setMaximum(32);
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
			optionsGroup.setLayout(new GridLayout(2, true));
			optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			btnApply.setEnabled(false);
			scanFormat = 4;

			Label lblNewLabel3 = new Label(optionsGroup, SWT.NONE);	
			lblNewLabel3.setText("Select image points");		
			new Label(optionsGroup, SWT.NONE);

			Label lblNewLabel31 = new Label(optionsGroup, SWT.NONE);
			lblNewLabel31.setText("Point 1: Upper Left");
			new Label(optionsGroup, SWT.NONE);
			scanULx = new Text(optionsGroup, SWT.NONE); scanULx.setEditable(false);
			scanULy = new Text(optionsGroup, SWT.NONE); scanULy.setEditable(false);

			Label lblNewLabel41 = new Label(optionsGroup, SWT.NONE);
			lblNewLabel41.setText("Point 2: Upper Right");	
			new Label(optionsGroup, SWT.NONE);
			scanURx = new Text(optionsGroup, SWT.NONE); scanURx.setEditable(false);
			scanURy = new Text(optionsGroup, SWT.NONE); scanURy.setEditable(false);

			Label lblNewLabel51 = new Label(optionsGroup, SWT.NONE);
			lblNewLabel51.setText("Point 3: Lower Left");
			new Label(optionsGroup, SWT.NONE);
			scanLLx = new Text(optionsGroup, SWT.NONE); scanLLx.setEditable(false);
			scanLLy = new Text(optionsGroup, SWT.NONE); scanLLy.setEditable(false);

			Label lblNewLabel61 = new Label(optionsGroup, SWT.NONE);
			lblNewLabel61.setText("Point 4: Lower Right");
			new Label(optionsGroup, SWT.NONE);
			scanLRx = new Text(optionsGroup, SWT.NONE); scanLRx.setEditable(false);
			scanLRy = new Text(optionsGroup, SWT.NONE); scanLRy.setEditable(false);

			btnClearScan = new Button(optionsGroup, SWT.NONE);
			btnClearScan.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					clearScan();
				}
			});
			btnClearScan.setText("Clear");		
			new Label(optionsGroup, SWT.NONE);

			final List format = new List(optionsGroup, SWT.V_SCROLL);
			format.setItems(new String[] {"A0 (841*1189)", "A1 (594*841)", "A2 (420*594)", "A3 (297*420)",
					"A4 (210*297)", "A5 (148*210)", "A6 (105*148)"});
			format.setSelection(scanFormat);
			format.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					scanFormat = format.getSelectionIndex();
				}
			});	

			imageFrame.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent arg0) {
					if(gc == null || gc.isDisposed()) gc = new GC(imageFrame);
					gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_RED));

					int x= (int) Math.min(savedImages[selectedImageNumber].getBounds().width-1,(double) (arg0.x)/zoomRatio);
					int y= (int) Math.min(savedImages[selectedImageNumber].getBounds().height-1,(double) (arg0.y)/zoomRatio);
					if(scanULx.getText().length() == 0){		
						scanULx.setText(String.valueOf(x)); scanULy.setText(String.valueOf(y));
						scanPointsX[0] = x; scanPointsY[0] = y;
						gc.fillOval(arg0.x-5, arg0.y-5, 10, 10);
						return;
					}
					if(scanURx.getText().length() == 0){
						scanURx.setText(String.valueOf(x)); scanURy.setText(String.valueOf(y));
						scanPointsX[1] = x; scanPointsY[1] = y;
						gc.fillOval(arg0.x-5, arg0.y-5, 10, 10);
						return;
					}
					if(scanLLx.getText().length() == 0){
						scanLLx.setText(String.valueOf(x)); scanLLy.setText(String.valueOf(y));
						scanPointsX[2] = x; scanPointsY[2] = y;
						gc.fillOval(arg0.x-5, arg0.y-5, 10, 10);
						return;
					}
					if(scanLRx.getText().length() == 0){
						scanLRx.setText(String.valueOf(x)); scanLRy.setText(String.valueOf(y));
						scanPointsX[3] = x; scanPointsY[3] = y;
						gc.fillOval(arg0.x-5, arg0.y-5, 10, 10);
					}
					for(int i=0; i<4; i++){
						for(int j=0; j<4; j++){
							if(i!=j){
								if(Math.pow(scanPointsX[j]-scanPointsX[i],2)+Math.pow(scanPointsY[j]-scanPointsY[i],2)==0){
									showWarningMessage("All four points must be distinct");
									clearScan();
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
			optionsGroup.setLayout(new GridLayout(1, true));
			optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			btnStop = new Button(optionsGroup, SWT.NONE);
			btnStop.setText("Stop");

			nbTriangles = 100;
			nbCircles = 100;

			Label lblNewLabel7 = new Label(optionsGroup, SWT.NONE);
			lblNewLabel7.setText("Number of triangles");

			final Spinner triangles = new Spinner(optionsGroup, SWT.BORDER);
			triangles.setSelection(nbTriangles);
			triangles.setMaximum(1000);
			triangles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			triangles.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					nbTriangles = triangles.getSelection();
				}
			});

			Label lblNewLabel8 = new Label(optionsGroup, SWT.NONE);
			lblNewLabel8.setText("Number of circles");

			final Spinner circles = new Spinner(optionsGroup, SWT.BORDER);
			circles.setSelection(nbCircles);
			circles.setMaximum(1000);
			circles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			circles.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					nbCircles = circles.getSelection();
				}
			});

		}
		optionsMenu.layout();
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
	 *  Reinitialise le menu de Scan
	 */
	void clearScan() {
		refreshDisplay();
		scanULx.setText("");scanULy.setText("");
		scanURx.setText("");scanURy.setText("");
		scanLLx.setText("");scanLLy.setText("");
		scanLRx.setText("");scanLRy.setText("");
		btnApply.setEnabled(false);
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

		if(savedImages == null) return;
		if(image != null) image.dispose();  //liberation ressources

		imageNumber.setText((selectedImageNumber+1)+"/"+savedImages.length);
		titleLabel.setText(fileNames[selectedImageNumber].substring(fileNames[selectedImageNumber].lastIndexOf('/') + 1));
		Image img = savedImages[selectedImageNumber];

		int imgWidth = img.getBounds().width;
		int imgHeight = img.getBounds().height;

		double xratio = (double) (imageFrame.getClientArea().width)/imgWidth;
		double yratio = (double) (imageFrame.getClientArea().height)/imgHeight;

		zoomRatio = Math.min(xratio, yratio);
		zoomLabel.setText(("Zoom: "+(int) (zoomRatio*100)+"%")+"\n"+"Original size: "+imgWidth+"*"+imgHeight);	

		image = ImageUtilities.resize(img, (int)(img.getBounds().width*zoomRatio), (int)(img.getBounds().height*zoomRatio));

		if(gc == null || gc.isDisposed()) gc = new GC(imageFrame);
		gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRectangle(imageFrame.getClientArea());
		gc.drawImage(image, 0, 0);

		gc.dispose();
	}

	/**
	 * @return Le facteur de redimensionnement de l'image pour l'affichage
	 */
	public static double getZoomRatio() {
		return zoomRatio;
	}

	/**
	 * Enregistre une image traitee dans le tableau d'images.
	 * @param img l'image traitee
	 * @param number le numero de l'image
	 */
	void updateImage(Image img, int number){
		if(savedImages != null){
			savedImages[number].dispose();  //liberation ressources
			savedImages[number] = img;
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
			showWarningMessage("One instance of PhotoWorks is already running");
			return;
		}	
		shell.setText("PhotoWorks");	
		shell.setLayout(new FillLayout());
		shell.setSize(1250,700);
		shell.setMinimumSize(1000,560); 

		openShell(shell);
		System.out.println("PhotoWorks opened");
		
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				System.out.println("PhotoWorks closing");
				server.closeSocket();
				if(updater != null && updater.isAlive()) updater.interrupt();

				display.dispose();
			}
		});

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

	}
}