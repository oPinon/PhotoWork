package gAPainter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Face {

	//is a triangle, with its 3 vertices
	Vertex v1,v2,v3;
	Color color;
	
	public Face(Vertex v1, Vertex v2, Vertex v3) {
		this.v1=v1;this.v2=v2;this.v3=v3;
		v1.addNeigh(this); v2.addNeigh(this); v3.addNeigh(this);
		color = new Color((int)(255*Math.random()),(int)(255*Math.random()),(int)(255*Math.random()));
	}
	
	public void paint(Graphics g) {
		g.setColor(color);
		g.fillPolygon(new int[]{v1.getX(),v2.getX(),v3.getX()},new int[]{v1.getY(),v2.getY(),v3.getY()}, 3);
	}
}
