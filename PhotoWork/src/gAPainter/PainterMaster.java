package gAPainter;

import imageComputing.Buffer;
import imageComputing.Client;
import imageComputing.Result;
import imageComputing.Task;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import display.GUI;

public class PainterMaster extends Thread implements Client{

	private String[] IPList;
	private Socket[] sockets;

	private DataOutputStream[] outputs;
	private DataInputStream[] inputs;	

	private Buffer<Task> tasksToDo;
	private Buffer<Result> tasksDone;

	long bestFitness = Long.MAX_VALUE;
	Sketch bestSketch;

	private int imageNumber;
	private double renderRatio = 1;

	public PainterMaster(String[] IPList, Buffer<Task> tasksToDo, Buffer<Result> tasksDone) {
		this.tasksToDo = tasksToDo;
		this.tasksDone = tasksDone;	
		this.IPList = IPList;

		sockets = new Socket[IPList.length];
		outputs = new DataOutputStream[IPList.length];
		inputs = new DataInputStream[IPList.length];	

		System.out.println("PainterMaster cree");
	}


	public BufferedImage getImage(double scale) {
		if(bestSketch != null) return bestSketch.render(scale);
		else return null;
	}

	public long getFitness() { return bestFitness; }

	public void run() {
		try {
			newConnection();
			while(true) {
				sendImage();
				receiveImage();
			}
		} catch (IOException | InterruptedException e) {}
		System.out.println("PainterMaster: fin de connection");
		endConnection();
	}


	public void newConnection() throws IOException, InterruptedException {
		Task toSend = tasksToDo.take();
		imageNumber = toSend.getImageNumber();

		for(int i=0;i<IPList.length;i++){
			sockets[i] = new Socket(IPList[i], 6789);
			inputs[i] = new DataInputStream(sockets[i].getInputStream());
			outputs[i] = new DataOutputStream(sockets[i].getOutputStream());
			toSend.sendToStream(outputs[i]);
		}
	}

	public void sendImage() throws IOException {
		for(int i=0;i<sockets.length;i++) {
			long slaveFitness = inputs[i].readLong();
			outputs[i].writeLong(bestFitness);

			if(slaveFitness<bestFitness) {  
				bestFitness = slaveFitness;
				bestSketch = new Sketch(inputs[i]); 
			}
			else {                              
				bestSketch.sendSketch(outputs[i]); 
			}
		}
	}

	public void receiveImage() throws InterruptedException{
		renderRatio = GUI.getZoomRatio()*renderRatio;
		BufferedImage output = getImage(renderRatio);

		if(output != null){
			Result r = new Result(output, imageNumber, getFitness());
			tasksDone.put(r);
		}
	}

	public void endConnection(){
		try {
			if(sockets != null) {
				for(Socket s: sockets) s.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
