package display;

import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import network.Server;

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
import org.eclipse.swt.custom.CLabel;

import filter.ImageFunction;

/**
 * La classe principale. Base de l'IHM. En tant que classe centrale de l'affichage utilisant SWT,
 * elle contient aussi la méthode main().
 * 
 * @author Pierre-Alexandre Durand
 *
 */
public class GUI extends Composite  {

	//BASE DE LA FENETRE SWT
	//moniteur d'affichage
	private static Display display;
	//fenetre principale
	private static Shell shell;

	//RESEAU
	private static ImageUpdater updater;
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

	//Barres de progression
	PWProgressBar globalProgressBar;
	PWProgressBar localProgressBar;
	
	//PARAMETRES
	//Nom des fichiers chargés
	String[] fileNames;
	//Nom de la fonction sélectionnée
	ImageFunction selectedFunction;

	//Fonctions de filtre (Auto Balance, Blur, HDR)
	int autoBalanceType, blurSize, HDRAlgorithm; //Type d'autobalance: 0:simple, 1:équilibrage couleurs, 2: équilibrage avec flou

	//DFT
	int DFTMode, scaleMethod, cutFrequency;

	//Scan
	int[] scanPointsX= new int[4];
	int[] scanPointsY= new int[4];
	int scanFormat;

	//GA Painter
	Button btnStop;

	//Menu Préférences
	boolean workOnAllFiles= true;
	int nbThreads= PreferencesMenu.AVAILABLE_THREADS;
	String[] IPList;

	public GUI(Composite parent, int style) throws BindException {
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

					zoomRatio=1;
					zoomLabel.setText(("Zoom: "+100+"%"));
					print("Opened "+fileNames.length+" file(s)",true);	

					selectedImageNumber= 0;
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
				if(savedImages==null) return;
				if(selectedImageNumber==0) selectedImageNumber= savedImages.length-1;	
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
				if(selectedImageNumber==savedImages.length-1) selectedImageNumber= 0;	
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
				if(image == null){ 
					showWarningMessage("Please select a file to work on");
					return;
				}

				final Shell prefShell= new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				final PreferencesMenu preferences= new PreferencesMenu(fileNames, savedImages, workOnAllFiles, nbThreads, IPList, prefShell, SWT.NONE);
				prefShell.setText("Preferences");	
				prefShell.setLayout(new FillLayout());
				prefShell.setSize(700,400); 

				Rectangle screenSize = display.getPrimaryMonitor().getBounds();
				prefShell.setLocation((screenSize.width - prefShell.getBounds().width) / 2, (screenSize.height - prefShell.getBounds().height) / 2);
				prefShell.open();
				prefShell.forceActive();

				prefShell.addListener(SWT.Close, new Listener() {
					public void handleEvent(Event event) {
						print("\n"+"Preferences changes saved",false);
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

		Button button = new Button(grpAllImages, SWT.NONE);
		button.setText("?");
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));	
		button.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage("");
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

		Button button_1 = new Button(grpAllImages, SWT.NONE);
		button_1.setText("?");
		button_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		button_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		button_1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage("");
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

		Button button_2 = new Button(grpAllImages, SWT.NONE);
		button_2.setText("?");
		button_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		button_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		button_2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage("");
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

		Button button_3 = new Button(grpAllImages, SWT.NONE);
		button_3.setText("?");
		button_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		button_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		button_3.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage("");
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

		Button button_4 = new Button(grpCurrentImageOnly, SWT.NONE);
		button_4.setText("?");
		button_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		button_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		button_4.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage("");
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

		Button button_5 = new Button(grpCurrentImageOnly, SWT.NONE);
		button_5.setText("?");
		button_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		button_5.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		button_5.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showHelpMessage("");
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
			IPList= new String[]{InetAddress.getLocalHost().getHostAddress()};
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		try {
			server= new Server();
			server.start();
		} catch (BindException e1) {
			throw e1;
		}
	}

	/**
	 *  Crée un menu d'options dépendant de la fonction choisie.
	 */
	void createOptionsMenu(){
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
			blurSize= 10;

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
						autoBalanceType=0;
						blurCheckButton.setEnabled(false);
						size.setEnabled(false);
					}
				}
			});			
			btnNewButton_11.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					if(btnNewButton_11.getSelection()){
						autoBalanceType= 1;
						blurCheckButton.setEnabled(true);
						blurCheckButton.setSelection(false);
					}
				}
			});
			blurCheckButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					size.setEnabled(blurCheckButton.getSelection());
					if(blurCheckButton.getSelection()) autoBalanceType= 2;
					else autoBalanceType= 1;
				}
			});
			break;

		case BLUR:
			optionsComposite.setLayout(new GridLayout(1, true));
			blurSize= 10;

			Label lblNewLabel1 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel1.setText("Blur Size (px):");
			final Spinner size2 = new Spinner(optionsComposite, SWT.BORDER);

			size2.setSelection(blurSize);
			size2.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					blurSize= size2.getSelection();
				}
			});
			break;

		case HDR_EQUALIZER:
			optionsComposite.setLayout(new GridLayout(1, true));
			blurSize= 50;
			HDRAlgorithm= 0;

			Label lblNewLabel2 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel2.setText("Blur Size (px):");
			final Spinner size3 = new Spinner(optionsComposite, SWT.BORDER);	
			size3.setSelection(blurSize);
			size3.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					blurSize= size3.getSelection();
				}
			});

			Label lblNewLabel21 = new Label(optionsComposite, SWT.NONE);
			lblNewLabel21.setText("Algorithm:");
			final List algorithm= new List(optionsComposite, SWT.NONE);
			algorithm.add("Adaptive histogram equalization");
			algorithm.add("Bas-relief from depth map");
			algorithm.setSelection(HDRAlgorithm);
			algorithm.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					HDRAlgorithm= algorithm.getSelectionIndex();
				}
			});

			break;

		case FOURIER_TRANSFORM:
			optionsComposite.setLayout(new GridLayout(1, true));
			DFTMode= 0;
			scaleMethod= 1;
			cutFrequency= 8;
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
						scaleMethod= 1;
					}
				}
			});
			btnLin = new Button(optionsComposite, SWT.RADIO);
			btnLin.setText("Linear");
			btnLin.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					if(btnLin.getSelection()){
						scaleMethod= 0;
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
					cutFrequency= cutoff.getSelection();
				}
			});
			mode.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					DFTMode= mode.getSelectionIndex();
					cutoff.setEnabled(DFTMode==1);
					btnLog.setEnabled(DFTMode==0);
					btnLin.setEnabled(DFTMode==0);
				}
			});
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

			final List format = new List(optionsComposite, SWT.V_SCROLL);
			format.setItems(new String[] {"A0 (841*1189)", "A1 (594*841)", "A2 (420*594)", "A3 (297*420)",
					"A4 (210*297)", "A5 (148*210)", "A6 (105*148)"});
			format.setSelection(4);
			format.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					scanFormat= format.getSelectionIndex();
				}
			});	

			imageFrame.addMouseListener(new MouseAdapter() {
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
						print("\n"+"Scan points saved",false);
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

	/**
	 * Lance un traitement pour la fonction sélectionnée. Un seul traitement peut avoir lieu simultanément sur un même
	 * ordinateur.
	 */
	private void apply(){
		if(updater!=null && updater.isAlive()){ 
			showWarningMessage("Please wait for the current treatment to end");
			return;
		}
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

		updater= new ImageUpdater(this, selectedFunction, workOnAllFiles, savedImages, selectedImageNumber, IPList, 
				nbThreads, autoBalanceType, blurSize, HDRAlgorithm, DFTMode, scaleMethod, cutFrequency, 
				scanPointsX, scanPointsY, scanFormat);
		updater.start();
	}

	/**
	 * Rafraîchit l'image en train d'être visionnée.
	 */
	void refreshDisplay(){
		imageNumber.setText((selectedImageNumber+1)+"/"+savedImages.length);
		titleLabel.setText(fileNames[selectedImageNumber].substring(fileNames[selectedImageNumber].lastIndexOf('/') + 1));
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

	/**
	 * Enregistre une image traitée dans le tableau d'images.
	 * @param img l'image traitée
	 * @param number le numéro de l'image
	 * @param appliedFunction la fonction qui lui a été appliquée
	 */
	void updateImage(Image img, int number, ImageFunction appliedFunction){
		if(savedImages!=null){
			savedImages[number]= img;
			print("\n"+appliedFunction.getName()+" done for image "+(number+1), false);
		}
	}

	public void setGlobalProgressBarSelection(double selection){
		globalProgressBar.setProgress(selection);
	}
	
	public void setLocalProgressBarSelection(double selection){
		localProgressBar.setProgress(selection);
	}

	public void print(String s, boolean clear){
		if(clear) infoLabel.setText("");
		infoLabel.append(s);
		infoLabel.setTopIndex(infoLabel.getLineCount() - 1);
	}

	public static void showWarningMessage(String message){
		MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
		mb.setText("Warning");
		mb.setMessage(message);
		mb.open();
		return;
	}
	public static void showHelpMessage(String message){
		MessageBox mb= new MessageBox(shell, SWT.ICON_INFORMATION | SWT.ABORT);
		mb.setText("Help");
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
		System.out.println("PhotoWork closing");
		server.terminate();

		display.dispose();
		//	System.exit(0);
	}
}