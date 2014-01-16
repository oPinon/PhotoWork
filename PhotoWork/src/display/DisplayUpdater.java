package display;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import network.Buffer;
import network.Client;
import network.Result;
import network.Task;
import network.UpdaterClient;

import org.eclipse.swt.SWT;
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
 * Cette classe sert d'interface entre le GUI et les clients, en transmettant les données à traiter aux clients et 
 * en mettant à jour le GUI à partir des données reçues.
 *
 */
public class DisplayUpdater extends Thread {
	private GUI g;

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

	private List<UpdaterClient> clients = new ArrayList<UpdaterClient>();

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

			Buffer<Task> tasksToDo = new Buffer<Task>();
			final Buffer<Result> tasksDone = new Buffer<Result>();

			if(selectedFunction != ImageFunction.GA_PAINTER){
				for(String ip: IPList){
					Client c = new Client(ip, tasksToDo, tasksDone);
					clients.add(c);
					c.start();
				}
			}

			g.getDisplay().syncExec(new Runnable() {  //necessaire car on met a jour le GUI depuis un thread externe
				public void run() {
					try{
						g.setGlobalProgressBarSelection(0);
						g.btnApply.setText("Busy...");
						g.btnApply.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
					} catch(SWTException e){}
				}
			});	


			//Envoi
			for(Image i: imagesToModify){		
				ImageData id = i.getImageData();
				BufferedImage input = FormatConversion.convertToAWT(id);

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

					PainterMaster pMaster = new PainterMaster(IPList, tasksToDo, tasksDone);
					clients.add(pMaster);
					pMaster.start();

					g.getDisplay().syncExec(new Runnable() {
						public void run() {
							g.btnStop.addSelectionListener(new SelectionAdapter() {
								public void widgetSelected(SelectionEvent arg0) {
									try{
										interrupt();
										for(UpdaterClient c: clients) c.interrupt();
										g.btnApply.setText("Apply !");
										g.btnApply.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
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
			while(Client.tasksCompleted.intValue() < imagesToModify.length){
				Result r = tasksDone.take();

				final BufferedImage output = new PImage(r.getResult()).getImage(); //necessaire, sinon convertToSWT ne marche pas
				final int number = r.getImageNumber();
				final double progress = r.getProgress();

				final Image i = new Image(g.getDisplay(), FormatConversion.convertToSWT(output));

				g.getDisplay().syncExec(new Runnable() {
					public void run(){
						try{
							if(progress == 100){
								final int done = Client.tasksCompleted.intValue();

								g.updateImage(i, number);
								g.print("\n"+selectedFunction.getName()+" done for image "+(number+1), false);
								g.setGlobalProgressBarSelection(done*100/imagesToModify.length);
								g.setLocalProgressBarSelection(100);
							}

							else{
								if(output.getWidth()>1){   //pour GA Painter, qui envoie des images non entierement traitees					
									g.updateImage(i, number);
									g.print("GAPainter fitness: "+(int) progress, true);
								}
								else g.setLocalProgressBarSelection(progress);
							}
						} catch(SWTException e) {return;}

					}
				});
			}

			final long t2 = System.currentTimeMillis();

			g.getDisplay().syncExec(new Runnable() {
				public void run() {
					try{
						g.print("\n"+selectedFunction.getName()+" finished",false);
						g.print("\n"+"Time spent: "+((t2-t1)/1000.0)+" second(s)",false);
						if(selectedFunction == ImageFunction.SCAN) g.createOptionsMenu(); //pour eviter d'effectuer deux scans à la suite
					} catch(SWTException e){}
				}
			});	


		} catch (InterruptedException e) {
			System.err.println("DisplayUpdater: interruption");
		} finally{
			for(UpdaterClient c: clients) c.interrupt();

			if(!g.isDisposed()){
				g.getDisplay().syncExec(new Runnable() {
					public void run() {
						try{
							g.btnApply.setText("Apply !");
							g.btnApply.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
						} catch(SWTException e){}
					}
				});	
			}
		}

	}

}
