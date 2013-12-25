package gAPainter;

import java.util.ArrayList;

public class Vertex {

	private int x,y;
	private ArrayList<Face> neigh;

	public Vertex(int x,int y) {
		this.x=x; this.y=y;
		neigh = new ArrayList<Face>();
	}

	public void move(int dx, int dy) { this.x+=dx; this.y+=dy; }

	public void addNeigh(Face face) {
		neigh.add(face);
	}
	
	public ArrayList<Face> getNeigh() { return neigh; }

	public int getX() {return x;}
	public int getY() {return y;}
}
