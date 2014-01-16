package gAPainter;

import java.awt.Color;
import java.awt.Graphics;

import pImage.PImage;

public class Face {

	//is a triangle, with its 3 vertices
	private Vertex v1,v2,v3;
	private Color color;
	private int nbPixels;
	private int rSum, gSum, bSum;
	private PImage source;

	public Face(Vertex v1, Vertex v2, Vertex v3) {
		this.v1=v1;this.v2=v2;this.v3=v3;
		v1.addNeigh(this); v2.addNeigh(this); v3.addNeigh(this);
		color = new Color((int)(255*Math.random()),(int)(255*Math.random()),(int)(255*Math.random()));
	}

	public void paint(Graphics g) {
		g.setColor(color);
		g.fillPolygon(new int[]{v1.getX(),v2.getX(),v3.getX()},new int[]{v1.getY(),v2.getY(),v3.getY()}, 3);
	}

	public void setColor(PImage source) {
		int xMin = Math.min(v1.getX(), Math.min(v2.getX(), v3.getX()));
		int xMax = Math.max(v1.getX(), Math.max(v2.getX(), v3.getX()));
		int yMin = Math.min(v1.getY(), Math.min(v2.getY(), v3.getY()));
		int yMax = Math.max(v1.getY(), Math.max(v2.getY(), v3.getY()));
		nbPixels=0;rSum=0;gSum=0;bSum=0;
		for(int x=xMin;x<=xMax;x++){
			for(int y=yMin;y<=yMax;y++){
				if(x>=0&&y>=0&&x<source.width()&&y<source.height()){
					nbPixels++;
					pImage.PColor col = source.getCol(x, y);
					rSum += col.getR();
					gSum += col.getG();
					bSum += col.getB();
				}
			}
		}
		if(nbPixels!=0){
			this.color = new Color(rSum/nbPixels,gSum/nbPixels,bSum/nbPixels);
		}
	}

	//calculates the tone of the source's part corresponding to the face, and makes it the face's color
	//uses http://www.sunshine2k.de/coding/java/TriangleRasterization/TriangleRasterization.html
	public void setColor2 (PImage source){
		nbPixels=0;
		rSum=0; gSum=0; bSum=0;
		this.source = source;
		sortVerticesAscendingByY();
		if (v2.getY() == v3.getY()){ fillBottomFlatTriangle(v1, v2, v3); }
		else if(v1.getY()==v2.getY()){ fillTopFlatTriangle(v1,v2,v3); }
		else {
			Vertex v4 = new Vertex((int)(v1.getX()+((float)(v2.getY()-v1.getY())/(float)(v3.getY()-v1.getY())))*(v3.getX()-v1.getX()),v2.getY());
			fillBottomFlatTriangle(v1,v2,v4);
			fillTopFlatTriangle(v2, v4, v3);
		}
		if(nbPixels!=0) {
			color = new Color(rSum/nbPixels,gSum/nbPixels,bSum/nbPixels);
		}
	}

	private void fillBottomFlatTriangle(Vertex v1, Vertex v2, Vertex v3)
	{
		float invslope1 = (v2.getX() - v1.getX()) / (v2.getY() - v1.getY());
		float invslope2 = (v3.getX() - v1.getX()) / (v3.getY() - v1.getY());

		float curx1 = v1.getX();
		float curx2 = v1.getX();

		for (int scanlineY = v1.getY(); scanlineY <= v2.getY(); scanlineY++)
		{
			drawLine((int)curx1, (int)curx2, scanlineY);
			curx1 += invslope1;
			curx2 += invslope2;
		}
	}
	private void drawLine(int x0, int x1, int y) {
		for(int x=x0;x<=x1;x++){
			if(x>=0&&y>=0&&x<source.width()&&y<source.height()){
				nbPixels++;
				pImage.PColor col = source.getCol(x, y);
				rSum += col.getR();
				gSum += col.getG();
				bSum += col.getB();
			}
		}
	}
	private void fillTopFlatTriangle(Vertex v1, Vertex v2, Vertex v3)
	{
		float invslope1 = (v3.getX() - v1.getX()) / (v3.getY() - v1.getY());
		float invslope2 = (v3.getX() - v2.getX()) / (v3.getY() - v2.getY());

		float curx1 = v3.getX();
		float curx2 = v3.getX();

		for (int scanlineY = v3.getY(); scanlineY > v1.getY(); scanlineY--)
		{
			curx1 -= invslope1;
			curx2 -= invslope2;
			drawLine((int)curx1, (int)curx2, scanlineY);
		}
	}
	// goal is v1.y <= v2.y <= v3.y
	private void sortVerticesAscendingByY() {
		Vertex vt3, vt2, vt1;
		if(v3.getY()>v2.getY()&&v3.getY()>v1.getY()) {
			vt3 = v3;
			if(v2.getY()>v1.getY()) {vt2=v2;vt1=v1;}
			else {vt2=v1;vt1=v2;}
		}
		else if(v2.getY()>v1.getY()&&v2.getY()>v3.getY()) {
			vt3 = v2;
			if(v3.getY()>v1.getY()) {vt2=v3;vt1=v1;}
			else {vt2=v1;vt1=v3;}
		}
		else {
			vt3 = v1;
			if(v3.getY()>v2.getY()) {vt2=v3;vt1=v2;}
			else {vt2=v2;vt1=v3;}
		}
		v1=vt1;v2=vt2;v3=vt3;
	}
}
