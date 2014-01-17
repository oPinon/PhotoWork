package filter;

import imageComputing.Buffer;
import imageComputing.Result;
import pImage.PImage;
import pImage.RGB;

public class HDREqualizer {

	/*
	 *  uses Algo : http://en.wikipedia.org/wiki/Adaptive_histogram_equalization
	 */
	public static PImage filter(PImage image, int size, Buffer<Result> streamBuffer) throws InterruptedException {
		PImage expanded = BlurFilter.expand(image,size);
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

			if(streamBuffer != null) 
				streamBuffer.put( new Result(Result.VOID_IMAGE, 0, Math.min( (x*100.0)/image.width() , 99.99)) );

		}
		return BlurFilter.cut(toReturn,size);
	}

	/*
	 * Works really great for generating bas-relief from depth-map
	 */
	public static PImage filter2(PImage image, int size, Buffer<Result> streamBuffer) throws InterruptedException {
		PImage expanded = BlurFilter.expand(image,size);
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

			if(streamBuffer != null) 
				streamBuffer.put( new Result(Result.VOID_IMAGE, 0, Math.min( (x*100.0)/image.width() , 99.99)) );

		}
		return BlurFilter.cut(toReturn,size);
	}


}
