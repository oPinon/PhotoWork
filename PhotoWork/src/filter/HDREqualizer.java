package filter;

import java.io.DataOutputStream;
import java.io.IOException;

import network.Result;
import display.PWProgressBar;
import pImage.PImage;
import pImage.RGB;

public class HDREqualizer {

	/*
	 *  uses Algo : http://en.wikipedia.org/wiki/Adaptive_histogram_equalization
	 */
	public static PImage filter(PImage image, int size) {
		//long t0 = System.currentTimeMillis();
		PImage expanded = BlurFilter.expand(image,size);
		//System.out.println("Expanded in "+(System.currentTimeMillis()-t0)+" ms.");
		//t0 = System.currentTimeMillis();
		PImage toReturn = new PImage(expanded.width(),expanded.height());
		
		int n = (2*size+1)*(2*size+1);
		
		for(int x = size; x<image.width()+size;x++){
			for(int y = size; y<image.height()+size;y++) {

				int R=0; int G=0; int B=0;
				
				pImage.PColor col = expanded.getCol(x, y);
				int R0=col.getR(); int G0=col.getG(); int B0=col.getB();
				
				for(int i=-size;i<=size;i++){
					for(int j=-size;j<=size;j++){
						col = expanded.getCol(x+i, y+j);
						if(col.getR()<R0) { R+= R0-col.getR(); }
						if(col.getG()<G0) { G+= G0-col.getG(); }
						if(col.getB()<B0) { B+= B0-col.getB(); }
					}
				}
				toReturn.setCol(x, y, new RGB((R)/n,(G)/n,(B)/n));
			}
		}
		//System.out.println("Blurred in "+(System.currentTimeMillis()-t0)+" ms.");
		return BlurFilter.cut(toReturn,size);
	}
	
	/*
	 * Works really great for generating bas-relief from depth-map
	 */
	public static PImage filter2(PImage image, int size, DataOutputStream toClient) {
		
		//long t0 = System.currentTimeMillis();
		PImage expanded = BlurFilter.expand(image,size);
		//System.out.println("Expanded in "+(System.currentTimeMillis()-t0)+" ms.");
		//t0 = System.currentTimeMillis();
		PImage toReturn = new PImage(expanded.width(),expanded.height());
		
		int n = (2*size+1)*(2*size+1);
		
		for(int x = size; x<image.width()+size;x++){
			for(int y = size; y<image.height()+size;y++) {

				int R=0; int G=0; int B=0;
				
				pImage.PColor col = expanded.getCol(x, y);
				int R0=col.getR(); int G0=col.getG(); int B0=col.getB();
				
				for(int i=-size;i<=size;i++){
					for(int j=-size;j<=size;j++){
						col = expanded.getCol(x+i, y+j);
						if(col.getR()<R0) { R++; }
						if(col.getG()<G0) { G++; }
						if(col.getB()<B0) { B++; }
					}
				}
				toReturn.setCol(x, y, new RGB((R*255)/n,(G*255)/n,(B*255)/n));
			}
			
			try {
				Result.sendDataToStream(null, 0, Math.min( (x*100.0)/image.width() , 99.99), toClient );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//System.out.println("Blurred in "+(System.currentTimeMillis()-t0)+" ms.");
		return BlurFilter.cut(toReturn,size);
	}
	
	
}
