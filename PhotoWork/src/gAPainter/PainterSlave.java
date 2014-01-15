package gAPainter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.Timer;

public class PainterSlave implements ActionListener {

	private Painter painter;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;

	public PainterSlave(Socket masterSocket)throws IOException {
		inputStream = new DataInputStream(masterSocket.getInputStream());
		outputStream = new DataOutputStream(masterSocket.getOutputStream());
		int cirPop = inputStream.readInt(); //get Int
		int triPop = inputStream.readInt(); //get Int
		BufferedImage sourceImg = ImageIO.read(masterSocket.getInputStream()); //get Image
		painter = new Painter(sourceImg,cirPop,triPop);
		painter.start(); // painter is the computation thread while PainterSlave is the network thread
		(new Timer(1000,this)).start();
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
		while(true) {
			try {
				long localFitness = painter.getFitness();
				long masterFitness = inputStream.readLong();
				outputStream.writeLong(localFitness);
				if(localFitness<masterFitness) {
					Sketch localSketch = painter.getSketch();
					synchronized(localSketch) {
						localSketch.sendSketch(outputStream);
					}
				}
				else {
					painter.setSketch(new Sketch(inputStream));
				}
			} catch (IOException c) {
				// TODO Auto-generated catch block
				c.printStackTrace();
			}
		}
	}
}
