package imageComputing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

import pImage.ImageSpectrum;
import pImage.PImage;
import pImage.Scanner;
import filter.AutoBalance;
import filter.BlurFilter;
import filter.HDREqualizer;
import filter.ImageFunction;
import gAPainter.PainterSlave;

/**
 * Realise le traitement d'une image en reseau.
 *
 */
public class NetworkComputationThread extends Thread implements ComputationThread{

	private Socket socket;

	private DataInputStream fromClient;
	private DataOutputStream toClient;

	private Buffer<Result> temporaryBuffer;
	private Thread senderThread;

	NetworkComputationThread(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try {
			System.out.println("computationThread: ready");
			computeImage();

		} catch (IOException | InterruptedException e) {
			System.err.println("computationThread: I/O error");
		}
	}

	public void computeImage() throws IOException, InterruptedException {

		fromClient = new DataInputStream(socket.getInputStream());
		toClient = new DataOutputStream(socket.getOutputStream());

		BufferedImage b = ImageIO.read(fromClient);
		if(b == null) {										//pour les tests de connection du menu Preferences
			System.out.println("computationThread: connection test OK");
			return; 
		}
		PImage input = new PImage(b);

		PImage output= new PImage(0,0);	
		fromClient.skip(16); //on saute deux octets a la fin de l'image, dus au format png

		ImageFunction function = ImageFunction.valueOf(fromClient.readUTF());
		int imageNumber = fromClient.readInt();	

		temporaryBuffer = new Buffer<Result>();

		if(function != ImageFunction.GA_PAINTER){   
			//ce Thread envoie les images intermediaires au Client (barre de progression)
			senderThread = new Thread(){
				public void run(){
					Result r = null;
					do{
						try {
							r = temporaryBuffer.take();
							r.sendToStream(toClient);
						} catch (IOException | InterruptedException e) {
							System.err.println("computationThread: senderThread interrupted");
							temporaryBuffer.close();
							break;
						}
					}
					while(r.getProgress () != 100);  //image en cours de traitement
				}
			};
			senderThread.start();
		}

		switch(function){
		case AUTO_BALANCE:
			int nbThreads = fromClient.readInt();
			int type = fromClient.readInt();
			int blurSize = fromClient.readInt();

			switch(type){
			case 0: output = AutoBalance.balance(input, nbThreads, temporaryBuffer);     break;
			case 1: output = AutoBalance.balanceColors(input, temporaryBuffer);		   break;	
			case 2: output = AutoBalance.balanceColors(input, blurSize, temporaryBuffer); break;
			}
			break;

		case BLUR:
			int blurSize2 = fromClient.readInt();

			output= BlurFilter.blur(input, blurSize2, temporaryBuffer);
			break;

		case HDR_EQUALIZER:
			int algorithm = fromClient.readInt();
			int blurSize3 = fromClient.readInt();

			if(algorithm == 0) output = HDREqualizer.filter(input, blurSize3, temporaryBuffer);
			else output = HDREqualizer.filter2(input, blurSize3, temporaryBuffer);

			output = AutoBalance.balanceColors(output, null);

			break;

		case FOURIER_TRANSFORM:
			int isHighPassFilterOn = fromClient.readInt();
			int isScalingLog = fromClient.readInt();
			int cutFrequency = fromClient.readInt();

			BufferedImage source = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = source.createGraphics();
			g.drawImage(b, 0, 0, 64, 64, null);
			g.dispose();

			//makes it a PImage object
			PImage img = new PImage(source);

			//does a Fourier Transform to create the image's spectrum
			ImageSpectrum spectrum = new ImageSpectrum(img, temporaryBuffer);

			if(isHighPassFilterOn == 0){
				output = spectrum.getTransform(isScalingLog == 1);
			} else{	
				spectrum.highPassFilter(cutFrequency);

				PImage result = spectrum.getReverseTransform();
				output = filter.AutoBalance.balanceColors(result, null);
			}
			break;

		case SCAN:
			int nbThreads2 = fromClient.readInt();
			int[] scanPointsX = {fromClient.readInt(),fromClient.readInt(),fromClient.readInt(),fromClient.readInt()};
			int[] scanPointsY = {fromClient.readInt(),fromClient.readInt(),fromClient.readInt(),fromClient.readInt()};
			int formatIndex = fromClient.readInt();

			output = Scanner.scan(input, scanPointsX, scanPointsY, formatIndex, nbThreads2, temporaryBuffer);
			break;

		case GA_PAINTER:
			int cirPop = fromClient.readInt();	
			int triPop = fromClient.readInt();

			new PainterSlave(input.getImage(), cirPop, triPop, socket);

			return;

		default: 
			break;
		}

		temporaryBuffer.put( new Result(output.getImage(), imageNumber, 100) );
		System.out.println("computationThread: image computed and sent back");
	}
}
