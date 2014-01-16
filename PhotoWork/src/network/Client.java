package network;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

/**
 * Le client envoie des Task a un serveur particulier, et convertit ce qu'il recoit en Result.
 *
 */
public class Client extends Thread implements UpdaterClient{

	protected String ip;
	protected Socket socket;

	protected DataOutputStream toServer;
	protected DataInputStream fromServer;	

	protected Buffer<Task> tasksToDo;
	protected Buffer<Result> tasksDone;

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

	public void sendImage() throws IOException, InterruptedException{
		Task toSend = tasksToDo.take();
		newConnection();	
		toSend.sendToStream(toServer);

		System.out.println("client "+ip+": image "+(toSend.getImageNumber()+1)+" envoyee");
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

		tasksCompleted.getAndIncrement();
		System.out.println("client "+ip+": image "+(imageNumber+1)+" recue");
	}

	public void newConnection() {
		try {
			socket = new Socket(ip, 6789);
			fromServer = new DataInputStream(socket.getInputStream());
			toServer = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.err.println("client "+ip+": erreur lors de la connection");
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
