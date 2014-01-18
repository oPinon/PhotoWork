package display;

import imageComputing.Buffer;
import imageComputing.Client;
import imageComputing.LocalClient;
import imageComputing.NetworkClient;
import imageComputing.Result;
import imageComputing.Task;

import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.wb.swt.SWTResourceManager;

import pImage.PImage;
import filter.ImageFunction;
import gAPainter.PainterMaster;

/**
 * Cette classe sert d'interface entre le GUI et les clients, en transmettant les donnees a traiter aux clients et 
 * en mettant a jour le GUI a partir des donnees recues.
 *
 */
public class DisplayUpdater extends Thread {
	private GUI g;

	//PARAMETRES DU GUI
	private ImageFunction selectedFunction;
	private boolean workOnAllFiles;
	private Image[] savedImages;
	private int selectedImageNumber;
	private String[] IPList;
	private int nbThreads;

	private int autoBalanceType, blurSize, HDRAlgorithm;
	private int DFTMode, scaleMethod, cutFrequency;
	private int[] scanPointsX, scanPointsY;
	private int scanFormat;
	private int nbTriangles, nbCircles;


	//PARAMETRES DE TRAITEMENT
	private List<Client> clients = new ArrayList<Client>();

	private double progress;
	private int number;
	private BufferedImage output;
	private Image convertedOutput;

	private Buffer<Task> tasksToDo;
	private Buffer<Result> tasksDone;

	public static AtomicInteger tasksCompleted;


	public DisplayUpdater(GUI g) {
		this.g = g;
		selectedFunction = g.selectedFunction;
		workOnAllFiles = g.workOnAllFiles;
		savedImages = g.savedImages;
		selectedImageNumber = g.selectedImageNumber;
		IPList = g.IPList;
		nbThreads = g.nbThreads;
		autoBalanceType = g.autoBalanceType;
		blurSize = g.blurSize;
		HDRAlgorithm = g.HDRAlgorithm;
		DFTMode = g.DFTMode;
		scaleMethod = g.scaleMethod;
		cutFrequency = g.cutFrequency;
		scanPointsX = g.scanPointsX;
		scanPointsY = g.scanPointsY;
		scanFormat = g.scanFormat;
		nbTriangles = g.nbTriangles;
		nbCircles = g.nbCircles;
	}

	public void run(){
		try{
			final long t1 = System.currentTimeMillis();

			final Image[] imagesToModify;
			int count;	
			if((selectedFunction.isApplicableOnAllFiles()) && workOnAllFiles){
				imagesToModify = savedImages;
				count = 0;
			}
			else{
				imagesToModify = new Image[1];
				count = selectedImageNumber;
				imagesToModify[0] = savedImages[count];
			}

			tasksCompleted = new AtomicInteger(0);
			tasksToDo = new Buffer<Task>();
			tasksDone = new Buffer<Result>();		
			createClients();

			g.getDisplay().syncExec(new Runnable() {  //necessaire car on met a jour le GUI depuis un thread externe
				public void run() {
					try{
						g.setGlobalProgressBarSelection(0);
						g.btnApply.setText("Busy...");
						g.btnApply.setBackground(SWTResourceManager.getColor(GUI.UNDO_COLOR));
					} catch(SWTException e){}
				}
			});	


			//Envoi
			for(Image i: imagesToModify){		
				ImageData id = i.getImageData();
				BufferedImage input = ImageUtilities.convertToAWT(id);

				Task task = null;

				switch(selectedFunction){
				case AUTO_BALANCE:
					task = new Task(input, selectedFunction, count, new int[]{nbThreads, autoBalanceType, blurSize});
					break;

				case BLUR:
					task = new Task(input, selectedFunction, count, new int[]{blurSize});
					break;

				case HDR_EQUALIZER:
					task = new Task(input, selectedFunction, count, new int[]{HDRAlgorithm, blurSize});
					break;

				case FOURIER_TRANSFORM:
					task = new Task(input, selectedFunction, count, new int[]{DFTMode, scaleMethod, cutFrequency});
					break;

				case SCAN:
					task = new Task(input, selectedFunction, count, new int[]{nbThreads,scanPointsX[0],scanPointsX[1],
							scanPointsX[2],scanPointsX[3],scanPointsY[0],scanPointsY[1],scanPointsY[2],scanPointsY[3],
							scanFormat});

					break;

				case GA_PAINTER:
					task = new Task(input, selectedFunction, count, new int[]{nbCircles, nbTriangles});

					g.getDisplay().syncExec(new Runnable() {
						public void run() {
							g.btnStop.addSelectionListener(new SelectionAdapter() {
								public void widgetSelected(SelectionEvent arg0) {
									try{
										interrupt();
										for(Client c: clients) c.interrupt();
										g.btnApply.setText("Apply !");
										g.btnApply.setBackground(SWTResourceManager.getColor(GUI.APPLY_COLOR));
									} catch(SWTException e){}
								}
							});
						}
					});	
					break;

				default:
					break;	
				}

				tasksToDo.put(task);
				count++;
			}

			//Reception
			while(tasksCompleted.intValue() < imagesToModify.length){

				do{
					Result r = tasksDone.take();

					output  = new PImage(r.getImage()).getImage(); //necessaire, sinon convertToSWT ne marche pas
					number = r.getImageNumber();
					progress = r.getProgress();

					if(output.getWidth() > 1){  //pour GAPainter et les images finies
						convertedOutput = new Image(g.getDisplay(), ImageUtilities.convertToSWT(output));
					}

					g.getDisplay().syncExec(new Runnable() {
						public void run() {
							try{
								if(selectedFunction != ImageFunction.GA_PAINTER){	
									g.setLocalProgressBarSelection(progress);  //images hors GAPainter
								}
								else{   //pour GA Painter, qui envoie des images non entierement traitees					
									g.updateImage(convertedOutput, number);
									g.refreshDisplay();
									g.print("GAPainter fitness: "+(int) progress, true);
								}
							} catch(SWTException e){}
						}
					});	

				} while(progress != 100);

				g.getDisplay().syncExec(new Runnable() {
					public void run() {
						try{
							if(output.getWidth()>1){ 
								g.updateImage(convertedOutput, number);
								g.print("\n"+selectedFunction.getName()+" done for image "+(number+1), false);
								g.setGlobalProgressBarSelection(tasksCompleted.intValue()*100/imagesToModify.length);
								g.setLocalProgressBarSelection(100);
							}
							else g.setLocalProgressBarSelection(progress);

						} catch(SWTException e){}
					}
				});	
			}

			final long t2 = System.currentTimeMillis();

			g.getDisplay().syncExec(new Runnable() {
				public void run() {
					try{
						g.refreshDisplay();
						g.print("\n"+selectedFunction.getName()+" finished",false);
						g.print("\n"+"Time spent: "+((t2-t1)/1000.0)+" second(s)",false);
						if(selectedFunction == ImageFunction.SCAN) g.clearScan(); 
						//pour eviter d'effectuer deux scans a la suite
					} catch(SWTException e){}
				}
			});	


		} catch (InterruptedException e) {
			System.err.println("DisplayUpdater: interruption");
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} finally{
			for(Client c: clients) c.interrupt(); //arret des Clients

			if(!g.isDisposed()){
				g.getDisplay().syncExec(new Runnable() {
					public void run() {
						try{
							g.btnApply.setText("Apply !");
							g.btnApply.setBackground(SWTResourceManager.getColor(GUI.APPLY_COLOR));
						} catch(SWTException e){}
					}
				});	
			}
		}

	}

	/**
	 * Cree les Clients en fonctions des parametres:
	 * -des LocalClients si toutes les IP entrees par l'utilisateur sont celles de la machine courante
	 * -des NetworkClients sinon, et si la fonction n'est pas GAPainter
	 * -des PainterMasters si on est en réseau et la fonction est GAPainter
	 * 
	 * @throws UnknownHostException
	 */
	private void createClients() throws UnknownHostException {
		final String localIP = InetAddress.getLocalHost().getHostAddress();

		for(String ip: IPList){
			if(!ip.equals(localIP)){   //dans ce cas on passe en mode reseau
				if(selectedFunction != ImageFunction.GA_PAINTER){
					for(String ip1: IPList){
						System.out.println("MODE RESEAU");
						NetworkClient nc = new NetworkClient(ip1, tasksToDo, tasksDone);
						clients.add(nc);
						nc.start();
					}
					return; 
				} 

				else{      //client particulier pour GA Painter
					System.out.println("MODE RESEAU - GA PAINTER");
					PainterMaster pMaster = new PainterMaster(IPList, tasksToDo, tasksDone);
					clients.add(pMaster);
					pMaster.start();
					return;
				}
			}
		}	

		//si toutes les IP sont identiques on passe en mode local
		System.out.println("MODE LOCAL");
		for(int i=0; i<nbThreads; i++){
			LocalClient lc = new LocalClient(tasksToDo, tasksDone, i);
			clients.add(lc);
			lc.start();
		}
	}

	public static void incrementTasks(){
		tasksCompleted.getAndIncrement();
	}

}
