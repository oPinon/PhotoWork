package filter;

import java.io.DataOutputStream;
import java.io.IOException;

import network.Result;
import pImage.PColor;
import pImage.PImage;
import pImage.RGB;

public class AutoBalance {

	public static PImage balance(PImage img, int nbThreads, DataOutputStream toClient) throws IOException {
		int width = img.width(); int height = img.height();
		PImage toReturn = new PImage(width, height);
		int[] temp = getMinMax(img, 0, nbThreads);
		int minV = temp[0]; int maxV = temp[1];
		for(int x=0; x<width; x++) {
			for(int y=0; y<height; y++) {
				PColor c0 = img.getCol(x, y);
				int R = transform(c0.getR(),minV,maxV);
				int G = transform(c0.getG(),minV,maxV);
				int B = transform(c0.getB(),minV,maxV);
				toReturn.setCol(x, y, new RGB(R,G,B));
			}
				Result.sendDataToStream(null, 0, Math.min( (x*100.0)/img.width() , 99.99), toClient );
		}
		return toReturn;
	}

	public static PImage balanceColors(PImage img, DataOutputStream toClient) throws IOException {
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

				Result.sendDataToStream(null, 0, Math.min( (x*100.0)/img.width() , 99.99), toClient );

		}
		return toReturn;
	}
	
	public static PImage balanceColors(PImage img, int blurSize, DataOutputStream toClient) throws IOException {
		int width = img.width(); int height = img.height();
		PImage toReturn = new PImage(width, height);
		PImage blurred = BlurFilter.blur(img, blurSize, null);
		
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

				Result.sendDataToStream(null, 0, Math.min( (x*100.0)/img.width() , 99.99), toClient );
		}
		return toReturn;
	}

	private static int transform (int value, int min, int max) {
		
		int pure = (int) (128+(255.0/(max-min))*(value-(max+min)/2.0));
		return Math.max(Math.min(pure, 255), 0);
	}
	
	//threshold is 0 by default, or higher if the image is noisy
	public static int[] getMinMax(PImage img, int threshold, int nbThreads) {
		
		int minV = Integer.MAX_VALUE; int maxV = Integer.MIN_VALUE;
		int[] R = new int[256];
		int[] G = new int[256];
		int[] B = new int[256];
		
		getColors(img,R,G,B,nbThreads);
		
		for(int min=0;min<256;min++){
			if(Math.max(R[min], Math.max(G[min], B[min]))>threshold) {
				minV = min;
	//			System.out.println("min = "+min);
				 break;
			}
		}
		for(int max=255;max>=0;max--){
			if(Math.max(R[max], Math.max(G[max], B[max]))>threshold) {
				maxV = max;
	//			System.out.println("max = "+max);
				break;
			}
		}
		return new int[]{ minV, maxV };
	}
	
	private static void getColors(PImage img, int[] R, int[] G, int[] B, int minX, int maxX, int minY, int maxY) {
		//these arrays will count of pixels of each color
		int[] rR = new int[256];
		int[] rG = new int[256];
		int[] rB = new int[256];
		//does the count for each pixel
		for(int x=minX; x<maxX; x++) {
			for(int y=minY; y<maxY; y++) {
				PColor c = img.getCol(x, y);
				rR[(int)c.getR()]++;
				rG[(int)c.getG()]++;
				rB[(int)c.getB()]++;
			}
		}
		//put the results in the output arrays
		synchronized(R){ for(int i=0;i<256;i++) { R[i]+=rR[i]; } }
		synchronized(G){ for(int i=0;i<256;i++) { G[i]+=rG[i]; } }
		synchronized(B){ for(int i=0;i<256;i++) { B[i]+=rB[i]; } }
	}
	
	public static void getColors(final PImage img,final int[] R,final int[] G,final int[] B, final int nbThreads) {
		Thread[] threads = new Thread[nbThreads];
		final int step = img.width()/nbThreads;
		for (int i = 0; i < threads.length-1; i++) {
			final int threadNumber = i;
			threads[i]= new Thread(){
				public void run() {
					getColors(img, R, G, B, threadNumber*step, (threadNumber+1)*step, 0, img.height());
				}
			};
			threads[i].start();
		}
		threads[nbThreads-1] = new Thread(){
			public void run() {
				getColors(img, R, G, B, (nbThreads-1)*step, img.width(), 0, img.height());
			}
		};
		threads[nbThreads-1].start();
				
		for (int i = 0; i < threads.length; i++) {
			try { threads[i].join(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
}
