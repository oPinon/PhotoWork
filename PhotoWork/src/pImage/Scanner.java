package pImage;

import filter.AutoBalance;
import imageComputing.Buffer;
import imageComputing.Result;

public class Scanner{

	public static PImage scan(PImage input, int[] x, int[] y, int format, int nbThreads, Buffer<Result> streamBuffer) throws InterruptedException {
		int outputHeight;
		int outputWidth;

		switch(format){
		case 0: outputHeight= 841 ; outputWidth= 1189 ;break;
		case 1: outputHeight= 594 ; outputWidth=  841 ;break;
		case 2: outputHeight= 420 ; outputWidth=  594 ;break;
		case 3: outputHeight= 297 ; outputWidth=  420 ;break;
		case 4: outputHeight= 210 ; outputWidth=  297 ;break;
		case 5: outputHeight= 148 ; outputWidth=  210 ;break;
		case 6: outputHeight= 105 ; outputWidth=  148 ;break;
		default: outputHeight= 210 ; outputWidth= 297 ;
		}

		PImage transformed = Scanner.transform(input,x,y,outputHeight,outputWidth, streamBuffer);

		transformed = AutoBalance.balance(transformed, nbThreads, null);

		return transformed;

	}

	static PImage transform(PImage img, int[] x5, int[] y5, int width, int height, Buffer<Result> streamBuffer) throws InterruptedException {
		// x and y are int[4] and M1 M2 M4 M3 are clockwise points
		double x1 = x5[0]; double x2 = x5[1]; double x3 = x5[2]; double x4 = x5[3];
		double y1 = y5[0]; double y2 = y5[1]; double y3 = y5[2]; double y4 = y5[3];

		double Ax = (x1-x2-x3+x4)/(width*height);
		double Bx = (x2-x1)/width;
		double Cx = (x3-x1)/height;
		double Dx = x1;

		double Ay = (y1-y2-y3+y4)/(width*height);
		double By = (y2-y1)/width;
		double Cy = (y3-y1)/height;
		double Dy = y1;

		PImage toReturn = new PImage(width,height);

		for(int i = 0; i<width; i++) {
			for(int j = 0; j<height; j++) {
				double X = Ax*i*j + Bx*i + Cx*j + Dx;
				double Y = Ay*i*j + By*i + Cy*j + Dy;
				toReturn.setCol(i, j, bilinear(img,X,Y));
			}

			if(streamBuffer != null) 
				streamBuffer.put( new Result(Result.VOID_IMAGE, 0, Math.min( (i*100.0)/img.width() , 99.99)) );

		}
		return toReturn;
	}

	static PColor bilinear(PImage img, double x, double y) {		
		int x0 = (int) x;
		int y0 = (int) y;
		double p1 = (x0+1-x)*(y0+1-y);
		double p2 = (x-x0)*(y0+1-y);
		double p3 = (x0+1-x)*(y-y0);
		double p4 = (x-x0)*(y-y0);
		PColor c1 = img.getCol(x0, y0);
		PColor c2 = img.getCol(x0+1, y0);
		PColor c3 = img.getCol(x0, y0+1);
		PColor c4 = img.getCol(x0+1, y0+1);
		int R = (int) (p1*c1.getR()+p2*c2.getR()+p3*c3.getR()+p4*c4.getR());
		int G = (int) (p1*c1.getG()+p2*c2.getG()+p3*c3.getG()+p4*c4.getG());
		int B = (int) (p1*c1.getB()+p2*c2.getB()+p3*c3.getB()+p4*c4.getB());

		return new RGB(R,G,B);
	}
}
