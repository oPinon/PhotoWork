package gAPainter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

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
	
	public void paint(Graphics g, int scale) {
		g.setColor(color);
		((Graphics2D) g).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.fillPolygon(x,y, 3);
	}
}
