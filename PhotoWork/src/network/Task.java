package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

import pImage.PImage;
import filter.AutoBalance;
import filter.BlurFilter;

public class Task {

	Socket socket;

	Task(Socket socket) {
		this.socket = socket;
	}

	public void execute() {
		DataInputStream fromClient;
		DataOutputStream toClient;
		try {
			System.out.println("ServerTask: prêt à traiter");

			fromClient = new DataInputStream(socket.getInputStream());
			toClient = new DataOutputStream(socket.getOutputStream());

			PImage input= new PImage(ImageIO.read(fromClient));
			PImage output= new PImage(0,0);	
			fromClient.skip(16); //on saute deux octets qui ne servent à rien

			String extension= fromClient.readUTF();
			String function= fromClient.readUTF();
			int imageNumber= fromClient.readInt();	
			int nbThreads= fromClient.readInt();

			switch(function){
			case "Auto Balance":
				int type= fromClient.readInt();
				int blurSize= fromClient.readInt();

				switch(type){
				case 0: output= AutoBalance.balance(input, nbThreads);     break;
				case 1: output= AutoBalance.balanceColors(input);		   break;	
				case 2: output= AutoBalance.balanceColors(input,blurSize); break;
				}

				break;

			case "Blur":
				int blurSize2= fromClient.readInt();

				output= BlurFilter.blur(input,blurSize2);

				break;

			case "Scan":
				//			if(scanPointsX[0] == 0 || scanPointsY[0] == 0){
				//				MessageBox mb= new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
				//				mb.setText("Warning");
				//				mb.setMessage("Please select 4 scan points\nand hit save button");
				//				mb.open();
				//				return;
				//			}
				//
				//			ImageData id2= savedImages[selectedImageNumber].getImageData();
				//			PImage output;
				//			output= Scanner.scan(FormatConversion.convertToAWT(id2), scanPointsX,scanPointsY,scanFormat.getSelectionIndex());
				//
				//			savedImages[selectedImageNumber]= new Image(getDisplay(),FormatConversion.convertToSWT(output.getImage()));
				//			resizeImage(savedImages[selectedImageNumber]);
				//			infoLabel.setText(infoLabel.getText()+"Scanning done"+"\n");
				break;

			case "GA Painter":	
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
			}

			ImageIO.write(output.getImage(), extension, toClient);
			toClient.writeInt(imageNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
