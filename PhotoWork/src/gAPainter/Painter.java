package gAPainter;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Painter extends Thread{

	private Sketch bestSketch;
	private long fitness;
	private int cirPop;
	private int triPop;
	private BufferedImage sourceImg;
	
	public Painter(BufferedImage sourceImg, int cirPop, int triPop) {
		this.sourceImg=sourceImg; this.cirPop=cirPop; this.triPop=triPop;
		bestSketch = new Sketch(sourceImg.getWidth(),sourceImg.getHeight(),this.triPop,this.cirPop);
		fitness = getFitness(sourceImg,bestSketch.getIm());
	}
	
	public void run() {
		while(!isInterrupted()) {
			Sketch newSketch;
			synchronized(bestSketch) {
				newSketch = new Sketch(bestSketch);
			}
			newSketch.mutate();
			long newFitness = getFitness(sourceImg,newSketch.getIm());
			synchronized(bestSketch) {
				if(newFitness<fitness) {
					bestSketch = newSketch;
					fitness = newFitness;
				}
			}
		}
	}

	public long getFitness() { return fitness; }
	
	public Sketch getSketch() {
		synchronized(bestSketch) {
			return bestSketch;
		}
	}
	
	public void setSketch(Sketch sketch) {
		synchronized(this.bestSketch) {
			this.bestSketch = sketch;
		}
	}
	
	static int getFitness(BufferedImage sourceImg, BufferedImage img) {

		int diff=0;
		WritableRaster raster1 = sourceImg.getRaster();
		WritableRaster raster2 = img.getRaster();
		for(int x=0;x<img.getWidth();x++) {
			for(int y=0;y<img.getHeight();y++) {
				int[] pix1 = new int[4];
				int[] pix2 = new int[4];
				pix1 = raster1.getPixel(x, y, pix1);
				pix2 = raster2.getPixel(x, y, pix2);

				int r = pix1[0]-pix2[0];
				int g = pix1[1]-pix2[1];
				int b = pix1[2]-pix2[2];
				diff += r*r + g*g + b*b;
			}
		}
		if(diff<0) { diff = Integer.MAX_VALUE; }
		return diff;
	}

}
