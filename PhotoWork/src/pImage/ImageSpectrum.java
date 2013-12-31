package pImage;

public class ImageSpectrum {
	
	private Spectrum RSpectrum;
	private Spectrum GSpectrum;
	private Spectrum BSpectrum;
	private int width, height;
	
	public ImageSpectrum(PImage source) {
		this.width=source.width(); this.height=source.height();
		RSpectrum = new Spectrum(width,height);
		GSpectrum = new Spectrum(width,height);
		BSpectrum = new Spectrum(width,height);
		for(int fx=0;fx<width;fx++) {
			for(int fy=0;fy<height;fy++){
				Complex R = new Complex(0,0);
				Complex G = new Complex(0,0);
				Complex B = new Complex(0,0);
				for(int x=0;x<width;x++){
					for(int y=0;y<height;y++){
						PColor col = source.getCol(x, y);
						double re = Math.cos(2*Math.PI*(fx*x*1.0/width+fy*y*1.0/height));
						double im = Math.sin(-2*Math.PI*(fx*x*1.0/width+fy*y*1.0/height));
						R.add(col.getR()*re, col.getR()*im);
						G.add(col.getG()*re, col.getG()*im);
						B.add(col.getB()*re, col.getB()*im);
					}
				}
				RSpectrum.setComplex(fx, fy, R);
				GSpectrum.setComplex(fx, fy, G);
				BSpectrum.setComplex(fx, fy, B);
			}
		}
	}
	
	public PImage getImage() {
		PImage img = new PImage(width,height);
		for(int x=0;x<width;x++){
			for(int y=0;y<height;y++){
				Complex R = new Complex(0,0);
				Complex G = new Complex(0,0);
				Complex B = new Complex(0,0);
				for(int fx=0;fx<width;fx++){
					for(int fy=0;fy<height;fy++){
						double re = Math.cos(2*Math.PI*(fx*x*1.0/width+fy*y*1.0/height));
						double im = Math.sin(2*Math.PI*(fx*x*1.0/width+fy*y*1.0/height));
						Complex c = new Complex(re,im);
						R.add(RSpectrum.getComplex(fx, fy).multiply(c));
						G.add(GSpectrum.getComplex(fx, fy).multiply(c));
						B.add(BSpectrum.getComplex(fx, fy).multiply(c));
					}
				}
				double r = R.getAbs()/(width*height);
				double g = G.getAbs()/(width*height);
				double b = B.getAbs()/(width*height);
				System.out.println(r);
				r = Math.max(0, Math.min(255, r));
				g = Math.max(0, Math.min(255, g));
				b = Math.max(0, Math.min(255, b));
				PColor col = new RGB((int)r,(int)g,(int)b);
				img.setCol(x, y, col);
			}
		}
		return img;
	}
}
