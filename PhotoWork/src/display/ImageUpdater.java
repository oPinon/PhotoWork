package display;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import network.Buffer;
import network.Client;
import network.Result;
import network.Task;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import pImage.PImage;
import filter.ImageFunction;

/**
 * Cette classe sert d'interface entre le GUI et les clients, en transmettant les données à traiter aux clients et 
 * en mettant à jour le GUI à partir des données reçues.
 * 
 * @author Pierre-Alexandre Durand
 *
 */
public class ImageUpdater extends Thread {
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

	public ImageUpdater(GUI g) {
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
	}

	public void run(){
		final long t1= System.currentTimeMillis();

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				g.setGlobalProgressBarSelection(0);
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

		List<Client> clients = new ArrayList<Client>();
		for(String s: IPList){
			Client c = new Client(s, tasksToDo, tasksDone);
			clients.add(c);
			c.start();
		}
		int tasksLeft = imagesToModify.length;

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
				task= new Task(input, selectedFunction, count, new int[]{});
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

		while(tasksLeft != 0){
			Result r;
			try {
				r = tasksDone.take();
			} catch (InterruptedException e) {
				for(Client c: clients) c.interrupt();
				return;
			}

			final BufferedImage output = new PImage(r.getResult()).getImage(); //nécessaire, sinon convertToSWT ne marche pas
			final int number = r.getImageNumber();
			final double progress = r.getProgress();

			if(progress == 100){
				final Image i = new Image(g.getDisplay(), FormatConversion.convertToSWT(output));

				tasksLeft--;
				final int remaining = tasksLeft;

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						g.updateImage(i, number, selectedFunction);
						g.setGlobalProgressBarSelection(100-(remaining*100/imagesToModify.length));
						g.setLocalProgressBarSelection(100);
					}
				});	

			} else{
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {	
						if(output.getWidth()>1){   //pour GA Painter, qui envoie des images non entièrement traitées
							Image i = new Image(g.getDisplay(), FormatConversion.convertToSWT(output));
							g.updateImage(i, number, selectedFunction);
							g.refreshDisplay();
						}
						g.setLocalProgressBarSelection(progress);
					}
				});	
			}

		}

		for(Client c: clients) c.interrupt();

		final long t2 = System.currentTimeMillis();

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				g.print("\n"+selectedFunction.getName()+" finished",false);
				g.print("\n"+"Time spent: "+((t2-t1)/1000.0)+" second(s)",false);
				if(selectedFunction == ImageFunction.SCAN) g.createOptionsMenu(); //pour eviter d'effectuer deux traitements à la suite
				g.refreshDisplay();
			}
		});	

	}

}
