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

	private Buffer<Result> streamBuffer; //rassemble les donnees du ComputationStream

	private int id;

	public LocalClient(Buffer<Task> tasksToDo, Buffer<Result> tasksDone, int id) {
		this.tasksToDo = tasksToDo;
		this.tasksDone = tasksDone;	

		streamBuffer = new Buffer<Result>();

		this.id = id;

		System.out.println("client local["+id+"]: client cree");	
	}

	public void run(){
		while(true){
			try {
				sendImage();
				receiveImage();
			} catch (InterruptedException e) {;
			System.out.println("client local["+id+"]: termine");
			endConnection();
			break;
			}
		}
	}

	public void sendImage() throws InterruptedException{
		Task toSend = tasksToDo.take();	
		new LocalComputationThread(toSend, streamBuffer).start();

		System.out.println("client local["+id+"]: image "+(toSend.getImageNumber()+1)+" envoyee");
	}

	public void receiveImage() throws InterruptedException{
		int imageNumber;
		double progress;

		do{
			Result r = streamBuffer.take();
			imageNumber = r.getImageNumber();
			progress = r.getProgress();
			tasksDone.put(r);
		}
		while(progress != 100);  //image en cours de traitement

		DisplayUpdater.incrementTasks();
		System.out.println("client local["+id+"]: image "+(imageNumber+1)+" recue");
	}

	public void newConnection(){}  //non applicable en local
	public void endConnection(){
		streamBuffer.close();
	}

}
