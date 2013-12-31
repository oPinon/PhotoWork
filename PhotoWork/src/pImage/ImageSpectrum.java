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
						double re = Math.cos(2*Math.PI*(fx*x*1.0/width+fy*y*1.0/height))*Math.pow(-1,x+y);
						double im = Math.sin(-2*Math.PI*(fx*x*1.0/width+fy*y*1.0/height))*Math.pow(-1,x+y);
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

	public PImage getTransform() {
		PImage img = new PImage(width,height);
		for(int fx=0;fx<width;fx++){
			for(int fy=0;fy<height;fy++){
				double max= getMaximum();
				double r= (RSpectrum.getComplex(fx, fy).getAbs())*(255.0/max);
				double g= (GSpectrum.getComplex(fx, fy).getAbs())*(255.0/max);
				double b= (BSpectrum.getComplex(fx, fy).getAbs())*(255.0/max);
				
				
//				//filtre log
//				double c= 255.0/Math.log10(1+max);
//				r=c*Math.log10(1+r);
//				g=c*Math.log10(1+g);
//				b=c*Math.log10(1+b);
				
				PColor col = new RGB((int)r,(int)g,(int)b);
				img.setCol(fx, fy, col);
			}
		}
		return img;
	}

	private double getMaximum() {
		double result= 0;
		for(int fx=0;fx<width;fx++){
			for(int fy=0;fy<height;fy++){
				if(RSpectrum.getComplex(fx, fy).getAbs()>result) result= RSpectrum.getComplex(fx, fy).getAbs();
				if(GSpectrum.getComplex(fx, fy).getAbs()>result) result= GSpectrum.getComplex(fx, fy).getAbs();
				if(BSpectrum.getComplex(fx, fy).getAbs()>result) result= BSpectrum.getComplex(fx, fy).getAbs();
			}
		}
		return result;	
	}

	public PImage getReverseTransform() {
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
				r = Math.max(0, Math.min(255, r));
				g = Math.max(0, Math.min(255, g));
				b = Math.max(0, Math.min(255, b));
				PColor col = new RGB((int)r,(int)g,(int)b);
				img.setCol(x, y, col);
			}
		}
		return img;
	}
	
	public Spectrum getRSpectrum() { return RSpectrum; }
	public Spectrum getGSpectrum() { return GSpectrum; }
	public Spectrum getBSpectrum() { return BSpectrum; }
}
