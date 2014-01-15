package display;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import network.Buffer;
import network.Client;
import network.Result;
import network.Task;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.wb.swt.SWTResourceManager;

import pImage.PImage;
import filter.ImageFunction;

/**
 * Cette classe sert d'interface entre le GUI et les clients, en transmettant les données à traiter aux clients et 
 * en mettant à jour le GUI à partir des données reçues.
 * 
 * @author Pierre-Alexandre Durand
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
		final long t1 = System.currentTimeMillis();

		g.getDisplay().syncExec(new Runnable() {  //necessaire car on met a jour le GUI depuis un thread externe
			public void run() {
				g.setGlobalProgressBarSelection(0);
				g.btnApply.setText("Busy...");
				g.btnApply.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
			}
		});	

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
		Buffer<Result> tasksDone = new Buffer<Result>();

		final List<Client> clients = new ArrayList<Client>();
		for(String s: IPList){
			Client c = new Client(s, tasksToDo, tasksDone);
			clients.add(c);
			c.start();
		}


		//Envoi
		for(Image i: imagesToModify){		
			ImageData id = i.getImageData();
			BufferedImage input = FormatConversion.convertToAWT(id);

			Task task = null;

			switch(selectedFunction){
			case AUTO_BALANCE:
				task= new Task(input, selectedFunction, count, new int[]{nbThreads, autoBalanceType, blurSize});
				break;

			case BLUR:
				task= new Task(input, selectedFunction, count, new int[]{blurSize});
				break;

			case HDR_EQUALIZER:
				task= new Task(input, selectedFunction, count, new int[]{HDRAlgorithm, blurSize});
				break;

			case FOURIER_TRANSFORM:
				task= new Task(input, selectedFunction, count, new int[]{DFTMode, scaleMethod, cutFrequency});
				break;

			case SCAN:
				task= new Task(input, selectedFunction, count, new int[]{nbThreads,scanPointsX[0],scanPointsX[1],
						scanPointsX[2],scanPointsX[3],scanPointsY[0],scanPointsY[1],scanPointsY[2],scanPointsY[3],
						scanFormat});
				break;

			case GA_PAINTER:	
				task= new Task(input, selectedFunction, count, new int[]{nbTriangles, nbCircles});

				g.getDisplay().syncExec(new Runnable() {
					public void run() {
						g.btnStop.addSelectionListener(new SelectionAdapter() {
							public void widgetSelected(SelectionEvent arg0) {
								interrupt();
								for(Client c: clients) c.interrupt();
							}
						});
					}
				});	

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

		//Reception
		while(Client.tasksCompleted.intValue() < imagesToModify.length){
			Result r;
			try {
				r = tasksDone.take();
			} catch (InterruptedException e) {
				for(Client c: clients) c.interrupt();
				g.btnApply.setText("Apply !");
				g.btnApply.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
				return;
			}

			final BufferedImage output = new PImage(r.getResult()).getImage(); //nécessaire, sinon convertToSWT ne marche pas
			final int number = r.getImageNumber();
			final double progress = r.getProgress();
			
			final Image i = new Image(g.getDisplay(), FormatConversion.convertToSWT(output));


			if(progress == 100){
				final int done = Client.tasksCompleted.intValue();
				g.getDisplay().syncExec(new Runnable() {
					public void run() {
						g.updateImage(i, number);
						g.print("\n"+selectedFunction.getName()+" done for image "+(number+1), false);
						g.setGlobalProgressBarSelection(done*100/imagesToModify.length);
						g.setLocalProgressBarSelection(100);
					}
				});	

			} else{
				g.getDisplay().syncExec(new Runnable() {
					public void run() {	
						if(output.getWidth()>1){   //pour GA Painter, qui envoie des images non entierement traitees					
							g.updateImage(i, number);
							g.print("GAPainter fitness: "+(int) progress, true);
						}
						else g.setLocalProgressBarSelection(progress);
					}
				});	
			}

		}

		for(Client c: clients) c.interrupt();

		final long t2 = System.currentTimeMillis();

		g.getDisplay().syncExec(new Runnable() {
			public void run() {
				g.print("\n"+selectedFunction.getName()+" finished",false);
				g.print("\n"+"Time spent: "+((t2-t1)/1000.0)+" second(s)",false);
				if(selectedFunction == ImageFunction.SCAN) g.createOptionsMenu(); //pour eviter d'effectuer deux scans à la suite
				g.btnApply.setText("Apply !");
				g.btnApply.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
			}
		});	

	}

}
