package gAPainter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;

public class Triangle implements Shape {

	private int[] x,y;
	private Color color;
	static final int alpha=100;
	
	public Triangle(int x0, int y0, int size) {
		this.x = new int[3]; x[0]=x0;
		x[1]=(int) (x0+size*(2*Math.random()-1));
		x[2]=(int) (x0+size*(2*Math.random()-1));
		this.y = new int[3]; y[0]=y0;
		y[1]=(int) (y0+size*(2*Math.random()-1));
		y[2]=(int) (y0+size*(2*Math.random()-1));
		int R = (int) (Math.random()*255);
		int G = (int) (Math.random()*255);
		int B = (int) (Math.random()*255);
		color = new Color(R,G,B,alpha);
	}
	
	public void write(java.io.DataOutputStream out) throws IOException {
		out.writeInt(x[0]); out.writeInt(y[0]);
		out.writeInt(x[1]); out.writeInt(y[1]);
		out.writeInt(x[2]); out.writeInt(y[2]);
		out.writeInt(color.getRed()); out.writeInt(color.getGreen()); out.writeInt(color.getBlue());
	}
	
	public Triangle(int x0, int y0, int x1, int y1, int x2, int y2, int R, int G, int B) {
		this.x = new int[] {x0,x1,x2};
		this.y = new int[] {y0,y1,y2};
		this.color = new Color(R,G,B,alpha);
	}
	
	public Triangle(Triangle other) {
		x = new int[3]; y = new int[3];
		for(int i=0; i<3; i++) {
			x[i]=other.x[i];
			y[i]=other.y[i];
		}
		int R = other.color.getRed();
		int G = other.color.getGreen();
		int B = other.color.getBlue();
		this.color = new Color(R,G,B,alpha);
	}
	
	public void mutate() {
		double r = 6*Math.random();
		int value;
		if(Math.random()<0.5) { value=(int)(-1-9*Math.random()); } else { value=(int)(1+9*Math.random());}
		if(r<0.5) { x[0]+=value; }
		else if(r<1) { y[0]+=value; }
		else if(r<1.5) { x[1]+=value;}
		else if(r<2) { y[1]+=value;}
		else if(r<2.5) { x[2]+=value;}
		else if(r<3) { y[2]+=value;}
		else {
			int R = color.getRed();
			int G = color.getGreen();
			int B = color.getBlue();
			if(r<4) { R = Math.max(0, Math.min(255, R+value)); }
			else if(r<5) { G = Math.max(0, Math.min(255, G+value)); }
			else { B = Math.max(0, Math.min(255, B+value)); }
			color = new Color(R,G,B,alpha);
		}
	}
	
	public void paint(Graphics g, double scale) {
		g.setColor(color);
		((Graphics2D) g).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int[] x2 = new int[3];
		int[] y2 = new int[3];
		for(int i=0; i<3; i++) {
			x2[i]=(int)(x[i]*scale);
			y2[i]=(int)(y[i]*scale);
		}
		g.fillPolygon(x2,y2,3);
	}
	
	public void paint(Graphics g) {
		g.setColor(color);
		((Graphics2D) g).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.fillPolygon(x,y, 3);
	}
}
