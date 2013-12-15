package filter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import pImage.PColor;
import pImage.PImage;
import pImage.RGB;

public class AutoBalance {
	
	public static void main(String[] args) throws IOException {
		BufferedImage image_source = ImageIO.read(new File("test.jpg"));
		PImage image_work = new PImage(image_source);
		
		long t0 = System.currentTimeMillis();
		getMinMax(image_work);
		System.out.println("MinMax done in "+(System.currentTimeMillis()-t0)+" ms");
	}

	public static PImage balance(PImage img) {
		int width = img.width(); int height = img.height();
		PImage toReturn = new PImage(width, height);
		int[] temp = getMinMax(img);
		int minV = temp[0]; int maxV = temp[1];
		for(int x=0; x<width; x++) {
			for(int y=0; y<height; y++) {
				PColor c0 = img.getCol(x, y);
				int R = transform(c0.getR(),minV,maxV);
				int G = transform(c0.getG(),minV,maxV);
				int B = transform(c0.getB(),minV,maxV);
				toReturn.setCol(x, y, new RGB(R,G,B));
			}
		}
		return toReturn;
	}

	public static PImage balanceColors(PImage img) {
		int width = img.width(); int height = img.height();
		PImage toReturn = new PImage(width, height);
		
		int minR = Integer.MAX_VALUE; int maxR = Integer.MIN_VALUE;
		int minG = Integer.MAX_VALUE; int maxG = Integer.MIN_VALUE;
		int minB = Integer.MAX_VALUE; int maxB = Integer.MIN_VALUE;
		
		for(int x=0; x<width; x++) {
			for(int y=0; y<height; y++) {
				PColor c = img.getCol(x, y);
				if(c.getR()>maxR) {maxR = c.getR();}
				if(c.getR()<minR) {minR = c.getR();}
				if(c.getG()>maxG) {maxG = c.getG();}
				if(c.getG()<minG) {minG = c.getG();}
				if(c.getB()>maxB) {maxB = c.getB();}
				if(c.getB()<minB) {minB = c.getB();}
			}
		}
		
		for(int x=0; x<width; x++) {
			for(int y=0; y<height; y++) {
				PColor c0 = img.getCol(x, y);
				int R = transform(c0.getR(),minR,maxR);
				int G = transform(c0.getG(),minG,maxG);
				int B = transform(c0.getB(),minB,maxB);
				toReturn.setCol(x, y, new RGB(R,G,B));
			}
		}
		return toReturn;
	}
	
	public static PImage balanceColors(PImage img, int blurSize) {
		int width = img.width(); int height = img.height();
		PImage toReturn = new PImage(width, height);
		PImage blurred = BlurFilter.blur(img, blurSize);
		
		int minR = Integer.MAX_VALUE; int maxR = Integer.MIN_VALUE;
		int minG = Integer.MAX_VALUE; int maxG = Integer.MIN_VALUE;
		int minB = Integer.MAX_VALUE; int maxB = Integer.MIN_VALUE;
		
		for(int x=0; x<width; x++) {
			for(int y=0; y<height; y++) {
				PColor c = blurred.getCol(x, y);
				if(c.getR()>maxR) {maxR = c.getR();}
				if(c.getR()<minR) {minR = c.getR();}
				if(c.getG()>maxG) {maxG = c.getG();}
				if(c.getG()<minG) {minG = c.getG();}
				if(c.getB()>maxB) {maxB = c.getB();}
				if(c.getB()<minB) {minB = c.getB();}
			}
		}
		
		for(int x=0; x<width; x++) {
			for(int y=0; y<height; y++) {
				PColor c0 = img.getCol(x, y);
				int R = transform(c0.getR(),minR,maxR);
				int G = transform(c0.getG(),minG,maxG);
				int B = transform(c0.getB(),minB,maxB);
				toReturn.setCol(x, y, new RGB(R,G,B));
			}
		}
		return toReturn;
	}

	private static int transform (int value, int min, int max) {
		
		int pure = (int) (128+(255.0/(max-min))*(value-(max+min)/2.0));
		return Math.max(Math.min(pure, 255), 0);
	}
	
	public static int[] getMinMax(PImage img) {
		int width = img.width(); int height = img.height();
		PImage toReturn = new PImage(width, height);
		int minV = Integer.MAX_VALUE; int maxV = Integer.MIN_VALUE;
		for(int x=0; x<width; x++) {
			for(int y=0; y<height; y++) {
				PColor c = img.getCol(x, y);
				int max = Math.max(c.getR(), Math.max(c.getG(),c.getB()));
				int min = Math.min(c.getR(), Math.min(c.getG(),c.getB()));
				if(max>maxV) {maxV = max;}
				if(min<minV) {minV = min;}
			}
		}
		System.out.println("min = "+minV+" and max = "+maxV);
		return new int[]{ minV, maxV };
	}
}
