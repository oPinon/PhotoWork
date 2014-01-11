package gAPainter;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Circle implements Shape{

	private int x, y, size;
	private Color color;
	static final int alpha=100;
	
	public Circle(int x, int y, int size) {
		this.x=x; this.y=y; this.size=size;
		int R = (int) (Math.random()*255);
		int G = (int) (Math.random()*255);
		int B = (int) (Math.random()*255);
		color = new Color(R,G,B,alpha);
	}
	
	public Circle(Circle other) {
		this.x = other.x;
		this.y = other.y;
		this.size = other.size;
		int R = other.color.getRed();
		int G = other.color.getGreen();
		int B = other.color.getBlue();
		this.color = new Color(R,G,B,alpha);
	}
	
	public void mutate() {
		double r = 6*Math.random();
		int value;
		if(Math.random()<0.5) { value=(int)(-1-9*Math.random()); } else { value=(int)(1+9*Math.random());}
		if(r<1) { x+=value; }
		else if(r<2) { y+=value; }
		else if(r<3) { size += value; if(size<1) { size=1; }}
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
		g.fillOval((x-size/2)*scale, (y-size/2)*scale, size*scale, size*scale);
	}
	
	public void paint(Graphics g) {
		g.setColor(color);
		((Graphics2D) g).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.fillOval(x-size/2, y-size/2, size, size);
	}
}
