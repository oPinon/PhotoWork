package network;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

/**
 * Le client envoie des Task à un serveur particulier, et convertit ce qu'il recoit en Result.
 * 
 * @author Pierre-Alexandre Durand
 *
 */
public class Client extends Thread{

	private String ip;

	private Socket socket;

	private DataOutputStream toServer;
	private DataInputStream fromServer;	

	private Buffer<Task> tasksToDo;
	private Buffer<Result> tasksDone;
	
	public static AtomicInteger tasksCompleted;

	public Client(String ip, Buffer<Task> tasksToDo, Buffer<Result> tasksDone) {
		this.tasksToDo = tasksToDo;
		this.tasksDone = tasksDone;	
		this.ip = ip;

		tasksCompleted = new AtomicInteger(0);
		System.out.println("client "+ip+": client cree");	
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
		Task toSend = tasksToDo.take();
		newConnection();	
		toSend.sendToStream(toServer);

		System.out.println("client "+ip+": image "+(toSend.imageNumber+1)+" envoyee");
	}

	private void receiveImage() throws IOException, InterruptedException{
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

		tasksCompleted.getAndIncrement();
		System.out.println("client "+ip+": image "+(imageNumber+1)+" recue");
	}

	private void newConnection() {
		try {
			socket = new Socket(ip, 6789);
			fromServer = new DataInputStream(socket.getInputStream());
			toServer = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.err.println("client "+ip+": erreur lors de la creation");
			interrupt();
		}
	}

	public void terminate(){
		try {
			if(socket !=null) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
