package gAPainter;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PainterMaster extends Thread{

	long bestFitness = Long.MAX_VALUE;
	Sketch bestSketch;
	Socket[] slaves;
	DataOutputStream[] outputs;
	DataInputStream[] inputs;

	public PainterMaster( Socket[] slaves) {
		this.slaves = slaves;
	}
	
	public BufferedImage getImage() { return bestSketch.getIm(); }
	
	public long getFitness() { return bestFitness; }

	public void run() {
		while(true) {
			for(int i=0;i<slaves.length;i++) {
				try {
					long slaveFitness = inputs[i].readLong();
					outputs[i].writeLong(bestFitness);
					if(slaveFitness<bestFitness) {
						bestSketch.sendSketch(outputs[i]);
					}
					else {
						bestSketch = new Sketch(inputs[i]);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
