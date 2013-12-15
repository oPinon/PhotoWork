package PImage;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class PImage {

	PColor [] raster; // line1 + line2 + ...
	private int width, height;
	
	public PImage(int width, int height) {
		this.width = width;
		this.height = height;
		PColor black = new RGB(0,0,0); // default color of an image
		raster = new PColor[width*height];
		for(int i=0; i<width*height;i++) { raster[i] = black; }
	}
	
	public PImage(BufferedImage image) {
		width = image.getWidth();
		height = image.getHeight();
		raster = new PColor[width*height];
		WritableRaster raster = image.getRaster();
		int[] pixel = new int[4];
		for(int x=0; x<width; x++) {
			for(int y=0; y<height; y++) {
				pixel = raster.getPixel(x, y, pixel);
				setCol(x,y,new RGB(pixel[0],pixel[1],pixel[2]));
			}
		}
	}
	
	public BufferedImage getImage() {
		
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		WritableRaster imageRaster = image.getRaster();
		for(int x=0; x< width; x++) {
			for(int y=0; y<height; y++) {
				imageRaster.setPixel(x, y, new int[]{getCol(x,y).getR(),getCol(x,y).getG(),getCol(x,y).getB()});
			}
		}
		return image;
	}
	
	public PColor getCol(int x, int y) {
		return raster[y*width+x];
	}
	
	public void setCol(int x, int y, PColor col) {
		raster[y*width+x] = col;
	}
	
	public int width() { return width; }
	public int height() { return height; }

	static void main(String args[]) {
		
	}
}
