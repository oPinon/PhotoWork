package PImage;

public class RGB implements PColor{
	
	private int R, G, B;
	
	public RGB(int R, int G, int B) {
		this.R = R;
		this.G = G;
		this.B = B;
	}
	
	public int getR() {
		return R;
	}
	
	public int getG() {
		return G;
	}
	
	public int getB() {
		return B;
	}
	
	public int getA() {
		return 255;
	}

	@Override
	public int getH() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getL() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getV() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
