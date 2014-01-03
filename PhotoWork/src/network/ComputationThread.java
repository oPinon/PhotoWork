package network;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

import display.ProgressBarHandler;
import pImage.ImageSpectrum;
import pImage.PImage;
import pImage.Scanner;
import filter.AutoBalance;
import filter.BlurFilter;
import filter.HDREqualizer;
import filter.ImageFunction;

public class ComputationThread extends Thread {

	Socket socket;

	ComputationThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		DataInputStream fromClient;
		DataOutputStream toClient;
		try {
			System.out.println("computationThread: prêt à traiter");

			fromClient = new DataInputStream(socket.getInputStream());
			toClient = new DataOutputStream(socket.getOutputStream());

			BufferedImage b= ImageIO.read(fromClient);
			if(b==null) {													//pour les tests de connection
				System.out.println("computationThread: test connection OK");
				return; 
			}
			PImage input= new PImage(b);

			PImage output= new PImage(0,0);	
			fromClient.skip(16); //on saute deux octets qui ne servent à rien

			ImageFunction function= ImageFunction.fromName(fromClient.readUTF());
			int imageNumber= fromClient.readInt();	

			switch(function){
			case AUTO_BALANCE:
				int nbThreads= fromClient.readInt();
				int type= fromClient.readInt();
				int blurSize= fromClient.readInt();

				switch(type){
				case 0: output= AutoBalance.balance(input, nbThreads);     break;
				case 1: output= AutoBalance.balanceColors(input);		   break;	
				case 2: output= AutoBalance.balanceColors(input,blurSize); break;
				}
				break;

			case BLUR:
				int blurSize2= fromClient.readInt();

				output= BlurFilter.blur(input,blurSize2);
				break;

			case HDR_EQUALIZER:
				int algorithm= fromClient.readInt();
				int blurSize3= fromClient.readInt();

				if(algorithm==0) output= HDREqualizer.filter(input,blurSize3);
				else output= HDREqualizer.filter2(input,blurSize3, new ProgressBarHandler());

				output= AutoBalance.balanceColors(output);

				break;

			case FOURIER_TRANSFORM:
				int isLowPassFilterOn= fromClient.readInt();
				int isScalingLog= fromClient.readInt();
				int cutFrequency= fromClient.readInt();

				BufferedImage source = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = source.createGraphics();
				g.drawImage(b, 0, 0, 64, 64, null);
				g.dispose();
				System.out.println("image loaded");
				//makes it a PImage object
				PImage img = new PImage(source);
				System.out.println("image converted to PImage");
				//does a Fourier Transform to create the image's spectrum
				ImageSpectrum spectrum = new ImageSpectrum(img);
				System.out.println("spectrum computed");

				if(isLowPassFilterOn==0){
					output= spectrum.getTransform(isScalingLog==1);
				} else{	
					spectrum.lowPassFilter(cutFrequency); // the width of the part we remove in the spectrum

					PImage result = spectrum.getReverseTransform();
					output = filter.AutoBalance.balanceColors(result);

					System.out.println("reverse transform done");
				}
				break;

			case SCAN:
				int nbThreads2= fromClient.readInt();
				int[] scanPointsX= {fromClient.readInt(),fromClient.readInt(),fromClient.readInt(),fromClient.readInt()};
				int[] scanPointsY= {fromClient.readInt(),fromClient.readInt(),fromClient.readInt(),fromClient.readInt()};
				int formatIndex= fromClient.readInt();

				output= Scanner.scan(input, scanPointsX, scanPointsY, formatIndex, nbThreads2);

				break;

			case GA_PAINTER:	
				//			ImageData id= savedImages[selectedImageNumber].getImageData();
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

			ImageIO.write(output.getImage(), "png", toClient);
			toClient.writeInt(imageNumber);
			System.out.println("computationThread: image traitée et renvoyée");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
