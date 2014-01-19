package imageComputing;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.imageio.ImageIO;

import display.DisplayUpdater;

/**
 * Le NetworkClient envoie des Task a un serveur particulier, et convertit ce qu'il recoit en Result.
 *
 */
public class NetworkClient extends Thread implements Client{

	private String ip;
	private Socket socket;

	private DataOutputStream toServer;
	private DataInputStream fromServer;	

	private Buffer<Task> tasksToDo;
	private Buffer<Result> tasksDone;

	public NetworkClient(String ip, Buffer<Task> tasksToDo, Buffer<Result> tasksDone) {
		this.tasksToDo = tasksToDo;
		this.tasksDone = tasksDone;	
		this.ip = ip;

		System.out.println("client "+ip+": created");	
	}

	public void run(){
		while(true){
			try {
				sendImage();
				receiveImage();
			} catch (IOException | InterruptedException e) {
				System.out.println("client "+ip+": end of connection");
				break;
			}
		}
		endConnection();
	}

	public void sendImage() throws IOException, InterruptedException{
		Task toSend = tasksToDo.take();
		newConnection();	
		toSend.sendToStream(toServer);

		System.out.println("client "+ip+": image "+(toSend.getImageNumber()+1)+" sent");
	}

	public void receiveImage() throws IOException, InterruptedException{
		BufferedImage output; 
		int imageNumber;
		double progress;

		do{
			output = ImageIO.read(fromServer);

			fromServer.skip(16); //on saute deux octets a la fin de l'image, dus au format png
			imageNumber = fromServer.readInt();
			progress = fromServer.readDouble();
			tasksDone.put(new Result(output, imageNumber, progress));
		}
		while(progress != 100);  //image en cours de traitement


		DisplayUpdater.incrementTasks();
		System.out.println("client "+ip+": image "+(imageNumber+1)+" received");
	}

	public void newConnection(){
		try {
			socket = new Socket(ip, 6789);
			fromServer = new DataInputStream(socket.getInputStream());
			toServer = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.err.println("client "+ip+": error while connecting");
			interrupt();
		}
	}

	public void endConnection(){
		try {
			if(socket != null) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
