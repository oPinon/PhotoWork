package network;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;

public class PoolClient{

	private Socket socket;
	private DataOutputStream toServer;
	private DataInputStream fromServer;	

	public BufferedImage output;
	public int imageNumber;


	public void sendImage(BufferedImage image, String extension, String function, int[] parameters){
		try {
			newConnection();
			
			ImageIO.write(image, extension, toServer);
			toServer.writeUTF(extension);
			toServer.writeUTF(function);
			for(int i: parameters){
				toServer.writeInt(i);
			}
			System.out.println("Envoyé");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void newConnection() {
		try {
			socket = new Socket(InetAddress.getLocalHost(), 6789);

			fromServer = new DataInputStream(socket.getInputStream());
			toServer= new DataOutputStream(socket.getOutputStream());		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void receiveImage(){
		try {
			output= ImageIO.read(fromServer);
			
			fromServer.skip(16); //on saute deux octets qui ne servent à rien
			imageNumber = fromServer.readInt();

			System.out.println("Reçu: "+imageNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void terminate(){
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
