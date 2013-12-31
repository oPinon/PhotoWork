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
			ImageIO.write(image, extension, toServer);
			toServer.writeUTF(extension);
			toServer.writeUTF(function);
			for(int i: parameters){
				toServer.writeInt(i);
			}
			System.out.println("client: image "+(parameters[0]+1)+" envoyée");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void newConnection(String ip) {
		try {
			socket = new Socket(ip, 6789);
			System.out.println("client: L'image sera envoyée à: "+ ip);

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

			System.out.println("client: image "+(imageNumber+1)+" reçue");
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
