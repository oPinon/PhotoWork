package gAPainter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.Timer;

public class PainterSlave implements ActionListener {

	private Painter painter;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;

	private Timer t;

	public PainterSlave(BufferedImage sourceImg, int cirPop, int triPop, Socket masterSocket)throws IOException {
		inputStream = new DataInputStream(masterSocket.getInputStream());
		outputStream = new DataOutputStream(masterSocket.getOutputStream());

		painter = new Painter(sourceImg,cirPop,triPop);
		painter.start(); // painter is the computation thread while PainterSlave is the network thread
		t = new Timer(1000,this);
		t.start();
	}

	public long getFitness() {
		return painter.getFitness();
	}

	public BufferedImage getImage() {
		return painter.getSketch().getIm();
	}

	/*
	 * protocol is :
	 * 1) send localFitness
	 * 2) receive masterFitness
	 * 3) if local is better send sketch, else receive sketch
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			long localFitness = painter.getFitness();
			outputStream.writeLong(localFitness);
			long masterFitness = inputStream.readLong();

			if(localFitness<masterFitness) {      
				Sketch localSketch = painter.getSketch();
				synchronized(localSketch) {
					localSketch.sendSketch(outputStream);  
				}	
			} else{                          
				painter.setSketch(new Sketch(inputStream));  
			}

		} 
		catch (IOException c) {
			painter.interrupt();
			t.stop();
			System.out.println("PainterSlave: fin de connection");
		}
	}
}
