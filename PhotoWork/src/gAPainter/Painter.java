package gAPainter;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Painter extends Thread{

	/**
	 * @param args
	 * @throws IOException 
	 */
	private BufferedImage output;
	private BufferedImage input;
	
	static int triPop;
	static int cirPop;
	private int width, height, pop = 5, birthFac = 10;
	private Sketch s;
	private int fitness;
	
	public Painter(BufferedImage source, int nbTriangles, int nbCircles) {
		this.input = source;
		this.width = source.getWidth(); this.height = source.getHeight();
		
		triPop = nbTriangles;
		cirPop = nbCircles;

		s = new Sketch(width,height,triPop,cirPop);
		fitness = getDiff(s.getIm());
	}

	public void run() {
		int scale = 1;

		output = new BufferedImage(input.getWidth()*2,input.getHeight(),BufferedImage.TYPE_INT_RGB);

		int i=0;
		long t = System.currentTimeMillis();

		while(!isInterrupted()) {
			mutate();
			i++;
			if(i%100==0) {
				setOutput(getBest(scale));	
				
				System.out.println("fitness = "+(int) bestFitness()+
						"; time = "+((System.currentTimeMillis()-t)/1000)); }
		}
	}

	public void mutate() {

		Sketch s2 = new Sketch(s);
		s2.mutate();
		int fitness2 = getDiff(s2.getIm());
		if(fitness2<fitness) {
			fitness=fitness2;
			s = s2;
		}
	}

	public BufferedImage getBest(int scale) {
		return s.render(scale);
	}

	public synchronized double bestFitness() {
		return fitness/input.getWidth()/input.getHeight();
	}

	private int getDiff(BufferedImage img) {

		int diff=0;
		WritableRaster raster1 = input.getRaster();
		WritableRaster raster2 = img.getRaster();
		for(int x=0;x<width;x++) {
			for(int y=0;y<height;y++) {
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

	/**
	 * @return the output
	 */
	public synchronized BufferedImage getOutput() {
		return output;
	}

	/**
	 * @param output the output to set
	 */
	private synchronized void setOutput(BufferedImage calculated) {
		Graphics g = output.getGraphics();
		g.drawImage(input,0,0,null);
		g.drawImage(calculated,input.getWidth(),0,null);
		Toolkit.getDefaultToolkit().sync();
	}

}
