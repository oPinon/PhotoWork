package pImage;

public class Complex {

	private double Re, Im;
	public Complex(double Re, double Im){
		this.Re=Re; this.Im=Im;
	}
	public double getRe() { return Re; }
	public double getIm() { return Im; }
	public double getAbs() { return Math.sqrt(Re*Re+Im*Im); }
	public void add(Complex other) {
		this.Re+=other.Re; this.Im+=other.Im;
	}
	public void add(double Re,  double Im){
		this.Re+=Re; this.Im+=Im;
	}
	public Complex multiply(Complex other){
		return new Complex(Re*other.Re-Im*other.Im,Re*other.Im+Im*other.Re);
	}
}
