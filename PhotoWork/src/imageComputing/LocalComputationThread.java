package imageComputing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import pImage.ImageSpectrum;
import pImage.PImage;
import pImage.Scanner;
import display.GUI;
import filter.AutoBalance;
import filter.BlurFilter;
import filter.HDREqualizer;
import filter.ImageFunction;
import gAPainter.Painter;

/**
 * Realise le traitement d'une image en local.
 *
 */
public class LocalComputationThread extends Thread implements ComputationThread {

	private Task task;
	private Buffer<Result> streamBuffer;

	private double painterRenderRatio = 1; //GAPainter uniquement

	LocalComputationThread(Task task, Buffer<Result> streamBuffer) {
		this.task = task;
		this.streamBuffer = streamBuffer;
	}

	public void run() {
		try {
			System.out.println("computationThread: pret a traiter");
			computeImage();
			System.out.println("computationThread: image traitee et renvoyee");

		} catch (InterruptedException e) {
			System.err.println("computationThread: interrupted");
		}
	}

	public void computeImage() throws InterruptedException {

		BufferedImage b = task.getImage();
		PImage input = new PImage(b);

		PImage output = new PImage(0,0);	

		ImageFunction function = task.getFunction();
		final int imageNumber = task.getImageNumber();	
		int[] parameters = task.getParameters();

		switch(function){
		case AUTO_BALANCE:
			int nbThreads = parameters[0];
			int type = parameters[1];
			int blurSize = parameters[2];

			switch(type){
			case 0: output = AutoBalance.balance(input, nbThreads, streamBuffer);     break;
			case 1: output = AutoBalance.balanceColors(input, streamBuffer);		   break;	
			case 2: output = AutoBalance.balanceColors(input, blurSize, streamBuffer); break;
			}
			break;

		case BLUR:
			int blurSize2 = parameters[0];

			output= BlurFilter.blur(input, blurSize2, streamBuffer);
			break;

		case HDR_EQUALIZER:
			int algorithm = parameters[0];
			int blurSize3 = parameters[1];

			if(algorithm == 0) output = HDREqualizer.filter(input, blurSize3, streamBuffer);
			else output = HDREqualizer.filter2(input, blurSize3, streamBuffer);

			output = AutoBalance.balanceColors(output, null);
			break;

		case FOURIER_TRANSFORM:
			int isHighPassFilterOn = parameters[0];
			int isScalingLog = parameters[1];
			int cutFrequency = parameters[2];

			BufferedImage source = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = source.createGraphics();
			g.drawImage(b, 0, 0, 64, 64, null);
			g.dispose();

			//makes it a PImage object
			PImage img = new PImage(source);

			//does a Fourier Transform to create the image's spectrum
			ImageSpectrum spectrum = new ImageSpectrum(img, streamBuffer);

			if(isHighPassFilterOn == 0){
				output = spectrum.getTransform(isScalingLog==1);
			} else{	
				spectrum.highPassFilter(cutFrequency);

				PImage result = spectrum.getReverseTransform();
				output = filter.AutoBalance.balanceColors(result, null);
			}
			break;

		case SCAN:
			int nbThreads2 = parameters[0];
			int[] scanPointsX = {parameters[1],parameters[2],parameters[3],parameters[4]};
			int[] scanPointsY = {parameters[5],parameters[6],parameters[7],parameters[8]};
			int formatIndex = parameters[9];

			output = Scanner.scan(input, scanPointsX, scanPointsY, formatIndex, nbThreads2, streamBuffer);

			break;

		case GA_PAINTER:
			int cirPop = parameters[0];
			int triPop = parameters[1];

			final Painter painter = new Painter(b,cirPop,triPop);
			painter.start();

			final Timer t = new Timer();

			t.scheduleAtFixedRate(new TimerTask(){
				public void run(){
					painterRenderRatio = GUI.getZoomRatio()*painterRenderRatio;
					
					if(painter.getSketch() != null){
						BufferedImage result = painter.getSketch().render(painterRenderRatio);
						try {
							streamBuffer.put(new Result(result, imageNumber, painter.getFitness()));
						} catch (InterruptedException e) {
							System.out.println("painter: fin");
							painter.interrupt();
							t.cancel();
						}	
					}
				}
			}, 0, 1000);
			return;

		default: 
			break;
		}

		streamBuffer.put(new Result(output.getImage(), imageNumber, 100));	
	}
}
