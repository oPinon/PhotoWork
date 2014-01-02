package network;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

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

			String extension= fromClient.readUTF();
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
				else output= HDREqualizer.filter2(input,blurSize3);
				
				output= AutoBalance.balanceColors(output);
				
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

			ImageIO.write(output.getImage(), extension, toClient);
			toClient.writeInt(imageNumber);
			System.out.println("computationThread: image traitée et renvoyée");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
