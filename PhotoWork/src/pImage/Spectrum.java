package pImage;

public class Spectrum {
	private Complex[] raster;
	private int width, height;
	
	public Spectrum(int width, int height){
		raster = new Complex[width*height];
	}
	public void setComplex(int x,int y,Complex c) {
		raster[y*width+x]=c;
	}
	
	public Complex getComplex(int x, int y) {
		return raster[y*width+x];
	}
	public int width() { return width; }
	public int height() { return height; }
}
