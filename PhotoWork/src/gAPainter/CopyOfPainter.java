package gAPainter;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class CopyOfPainter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int scale = 1;
		
		BufferedImage source = ImageIO.read( new File("mutation.png"));
		BufferedImage sourceHQ = new BufferedImage(source.getWidth()*scale,source.getHeight()*scale,BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics g = sourceHQ.getGraphics();
		g.drawImage(source, 0, 0, source.getWidth()*scale, source.getHeight()*scale, null);
		
		CopyOfPainter p = new CopyOfPainter(source);
		
		Display d = new Display(sourceHQ);
		int i=0;
		long t = System.currentTimeMillis();
		
		while(true) {
			p.mutate();
			//ImageIO.write(p.getBest(1), "png", new File("render.png"));
			i++;
			if(i%100==0) {
				d.setImage(p.getBest(scale));
				System.out.println("fitness = "+p.bestFitness()+
					"; time = "+((System.currentTimeMillis()-t)/1000)); }
		}
	}

	private BufferedImage source;
	static int triPop = 100;
	static int cirPop = 100;
	private int width, height, pop = 5, birthFac = 10;
	private Sketch s;
	private int fitness;

	public CopyOfPainter(BufferedImage source) {
		this.source = source;
		this.width = source.getWidth(); this.height = source.getHeight();

		s = new Sketch(width,height,triPop,cirPop);
		fitness = getDiff(s.getIm());
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
