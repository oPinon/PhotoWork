package gAPainter;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class Painter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		BufferedImage source = ImageIO.read( new File("monaLisa.png"));
		Sketch.isTriangle = false;
		
		Painter p = new Painter(source);
		
		Display d = new Display(source);
		int i=0;
		long t = System.currentTimeMillis();
		
		while(true) {
			p.mutate();
			d.setImage(p.getBest(1));
			//ImageIO.write(p.getBest(1), "png", new File("render.png"));
			i++;
			if(i%100==0) { System.out.println("fitness = "+p.bestFitness()+
					"; time = "+((System.currentTimeMillis()-t)/1000)); }
		}
	}

	private BufferedImage source;
	static int sketchPop = 200;
	private int width, height, pop = 5, birthFac = 10;
	private Sketch s;
	private int fitness;

	public Painter(BufferedImage source) {
		this.source = source;
		this.width = source.getWidth(); this.height = source.getHeight();

		s = new Sketch(width,height,sketchPop);
		fitness = getDiff(s.getIm());
	}

	public void mutate() {

		Sketch s2 = new Sketch(s);
		s2.mutate();
		s2.merge(s, 0.5);
		int fitness2 = getDiff(s2.getIm());
		if(fitness2<fitness) {
			fitness=fitness2;
			s = s2;
		}
	}

	public BufferedImage getBest(int scale) {
		return s.render(scale);
	}

	public int bestFitness() {
		return fitness/source.getWidth()/source.getHeight();
	}

	private int getDiff(BufferedImage img) {

		int diff=0;
		WritableRaster raster1 = source.getRaster();
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

}
