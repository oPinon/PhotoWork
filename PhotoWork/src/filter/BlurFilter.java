package filter;

import imageComputing.Buffer;
import imageComputing.Result;
import pImage.PImage;
import pImage.RGB;

public class BlurFilter {

	public static PImage blur(PImage image, int blurSize, Buffer<Result> streamBuffer) throws InterruptedException {

		PImage expanded = expand(image,blurSize);

		PImage blurred = new PImage(expanded.width(),expanded.height());
		int n = (2*blurSize+1)*(2*blurSize+1);
		for(int x = blurSize; x<image.width()+blurSize;x++){
			for(int y = blurSize; y<image.height()+blurSize;y++) {

				int R=0; int G=0; int B=0;
				for(int i=-blurSize;i<=blurSize;i++){
					for(int j=-blurSize;j<=blurSize;j++){
						R+=expanded.getCol(x+i, y+j).getR();
						G+=expanded.getCol(x+i, y+j).getG();
						B+=expanded.getCol(x+i, y+j).getB();
					}
				}
				blurred.setCol(x, y, new RGB(R/n,G/n,B/n));
			}

			if(streamBuffer != null) 
				streamBuffer.put( new Result(Result.VOID_IMAGE, 0, Math.min( (x*100.0)/image.width() , 99.99)) );

		}

		return cut(blurred,blurSize);
	}

	public static PImage expand(PImage image, int pixels) {

		int width = image.width(); int height = image.height();
		PImage toReturn = new PImage(width+2*pixels,height+2*pixels);
		for(int x = 0; x<width; x++) {
			for(int y = 0; y<height; y++) {
				toReturn.setCol(x+pixels, y+pixels, image.getCol(x, y));
			}
		}
		for(int x = 0; x<pixels; x++) {
			for(int y = 0; y<pixels; y++) {
				toReturn.setCol(x, y, image.getCol(0, 0));
				toReturn.setCol(width+pixels+x, y, image.getCol(width-1, 0));
				toReturn.setCol(x, height+pixels+y, image.getCol(0, height-1));
				toReturn.setCol(width+pixels+x, height+pixels+y, image.getCol(width-1, height-1));
			}
		}
		for(int x = 0; x<width; x++) {
			for(int y=0; y<pixels; y++) {
				toReturn.setCol(x+pixels, y, image.getCol(x, 0));
				toReturn.setCol(x+pixels, height+pixels+y, image.getCol(x, height-1));
			}
		}
		for(int y = 0; y<height; y++) {
			for(int x=0; x<pixels; x++) {
				toReturn.setCol(x, y+pixels, image.getCol(0, y));
				toReturn.setCol(x+width+pixels, y+pixels, image.getCol(width-1, y));
			}
		}
		return toReturn;
	}

	public static PImage cut(PImage image, int pixels) {
		int width = image.width(); int height = image.height();
		PImage toReturn = new PImage(width-2*pixels,height-2*pixels);
		for(int x = 0; x< toReturn.width(); x++) {
			for(int y=0; y< toReturn.height(); y++) {
				toReturn.setCol(x, y, image.getCol(x+pixels, y+pixels));
			}
		}
		return toReturn;
	}
}

