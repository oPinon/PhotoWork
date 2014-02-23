package imageComputing;

import display.DisplayUpdater;

/**
 * Le LocalClient effectue des operations en local, en divisant les images sur un nombre de threads
 * definis par l'utilisateur.
 *
 */
public class LocalClient extends Thread implements Client{

	private Buffer<Task> tasksToDo;
	private Buffer<Result> tasksDone;

	private Buffer<Result> streamBuffer; //rassemble les donnees du LocalComputationThread

	private int id;

	public LocalClient(Buffer<Task> tasksToDo, Buffer<Result> tasksDone, int id) {
		this.tasksToDo = tasksToDo;
		this.tasksDone = tasksDone;	

		streamBuffer = new Buffer<Result>();

		this.id = id;

		System.out.println("local client["+id+"]: created");	
	}

	public void run(){
		while(true){
			try {
				sendImage();
				receiveImage();
			} catch (InterruptedException e) {
				System.out.println("local client["+id+"]: closing");
				break;
			}
		}
		endConnection();
	}

	public void sendImage() throws InterruptedException{
		Task toSend = tasksToDo.take();	
		new LocalComputationThread(toSend, streamBuffer).start();

		System.out.println("local client["+id+"]: image "+(toSend.getImageNumber()+1)+" sent");
	}

	public synchronized void receiveImage() throws InterruptedException{
		int imageNumber;
		double progress;

		do{
			Result r = streamBuffer.take();
			imageNumber = r.getImageNumber();
			progress = r.getProgress();
			tasksDone.put(r);
		}
		while(progress != 100);  //image en cours de traitement


		System.out.println("local client["+id+"]: image "+(imageNumber+1)+" received");
	}

	public void newConnection(){}  //non applicable en local
	public void endConnection(){
		streamBuffer.close();
	}

}
