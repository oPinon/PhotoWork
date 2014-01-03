package network;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

public class Client extends Thread{

	private String ip;

	private Socket socket;

	private DataOutputStream toServer;
	private DataInputStream fromServer;	

	private Buffer<Task> tasksToDo;
	private Buffer<Result> tasksDone;


	public Client(String ip, Buffer<Task> tasksToDo, Buffer<Result> tasksDone) {
		this.tasksToDo= tasksToDo;
		this.tasksDone= tasksDone;	
		this.ip= ip;	

		System.out.println("client "+ip+": client créé");	
	}

	private void newConnection() {
		try {
			socket= new Socket(ip, 6789);
			fromServer = new DataInputStream(socket.getInputStream());
			toServer= new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.err.println("client "+ip+": erreur lors de la création");
			interrupt();
		}
	}

	public void run(){
		while(true){
			try {
				sendImage();
				receiveImage();
			} catch (IOException | InterruptedException e) {
				break;
			}
		}
		System.out.println("client "+ip+": fin de connection");
		terminate();
	}

	private void sendImage() throws IOException, InterruptedException{
		Task toSend= tasksToDo.take();
		newConnection();	
		toSend.sendToStream(toServer);

		System.out.println("client "+ip+": image "+(toSend.imageNumber+1)+" envoyée");
	}

	private void receiveImage() throws IOException, InterruptedException{
		BufferedImage output = ImageIO.read(fromServer);
		fromServer.skip(16); //on saute deux octets qui ne servent à rien
		int imageNumber = fromServer.readInt();

		tasksDone.put(new Result(output,imageNumber));
		System.out.println("client "+ip+": image "+(imageNumber+1)+" reçue");


	}

	public void terminate(){
		try {
			if(socket!=null) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
