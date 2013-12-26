package gAPainter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import pImage.*;

public class Mesh {
	
	private BufferedImage source, render;
	private PImage src;
	private ArrayList<Vertex> vertices;
	private ArrayList<Face> faces;
	int width, height, N;
	
	public Mesh(BufferedImage source, int N){
		this.source = source;
		src = new PImage(source);
		this.width = source.getWidth(); this.height = source.getHeight(); this.N = N;
		render = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		vertices = new ArrayList<Vertex>();
		faces = new ArrayList<Face>();
		//creates a N*N grid of vertices
		for(double x=0;x<=width;x+=width/N){
			for(double y=0;y<=height;y+=height/N){
				vertices.add(new Vertex((int)x,(int)y));
			}
		}
		//fills the grid with faces
		for(int y=0;y<N;y++){
			for(int x=0;x<N;x++){
				Vertex v0 = vertices.get(x+y*(N+1));
				Vertex v1 = vertices.get(x+1+y*(N+1));
				Vertex v2 = vertices.get(x+(y+1)*(N+1));
				Vertex v3 = vertices.get(x+1+(y+1)*(N+1));
				faces.add(new Face(v0,v3,v2));
				faces.add(new Face(v0,v1,v3));
			}
		}
		mutate();
		updateRender();
	}
	
	public void updateRender() {
		Graphics g = render.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, width, height);
		for(Face f : faces){
			f.setColor(src);
			f.paint(g);
		}
		g.dispose();
	}
	
	public void mutate() {
		for(Vertex v:vertices){
			v.move((int)(10-20*Math.random()), (int)(10-20*Math.random()));
		}
	}
	
	public BufferedImage getImage() {return render;}
	
}
