package gAPainter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Sketch {

	private int width, height, triPop, cirPop;
	private Triangle[] triangles;
	private Circle[] circles;
	private BufferedImage image;

	public Sketch(int width, int height, int triPop, int cirPop) {
		this.width=width;
		this.height=height;
		this.triPop=triPop;
		this.cirPop=cirPop;
		this.triangles = new Triangle[triPop];
		this.circles = new Circle[cirPop];
		for(int i=0;i<triPop;i++) {
			int x = (int) (width*Math.random());
			int y = (int) (height*Math.random());
			int size = (int) ((height/10)*(1+3*Math.random()));
			triangles[i]=new Triangle(x,y,size);
		}
		for(int i=0;i<cirPop;i++) {
			int x = (int) (width*Math.random());
			int y = (int) (height*Math.random());
			int size = (int) ((height/10)*(1+3*Math.random()));
			circles[i]=new Circle(x,y,size);
		}
		image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		updateImage();
	}

	public Sketch(Sketch other) {
		this.width = other.width;
		this.height = other.height;
		this.triPop=other.triPop;
		this.cirPop=other.cirPop;
		this.triangles = new Triangle[triPop];
		this.circles = new Circle[cirPop];
		for(int i=0;i<triPop;i++) {
			this.triangles[i]= new Triangle(other.triangles[i]);
		}
		for(int i=0;i<cirPop;i++) {
			this.circles[i]= new Circle(other.circles[i]);
		}
		image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		updateImage();
	}

	public void mutate() {
		for(int i=0;i<triPop;i++) {
			if(Math.random()<0.4) { this.triangles[i].mutate(); }
		}
		for(int i=0;i<cirPop;i++) {
			if(Math.random()<0.4) { this.circles[i].mutate(); }
		}
		updateImage();
	}

	public BufferedImage render(double scale) {
		BufferedImage img = new BufferedImage((int)(width*scale),(int)(height*scale),BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		paint(g,scale);
		return img;
	}

	public BufferedImage getIm() {
		return image;
	}

	public void updateImage() {
		Graphics g = image.getGraphics();
		paint(g);
	}

	public void paint(Graphics g, double scale) {
		g.setColor(Color.black);
		g.fillRect(0, 0, width, height);
		for(int i=0;i<triPop;i++) { 
			triangles[i].paint(g, scale);
		}
		for(int i=0;i<cirPop;i++) { 
			circles[i].paint(g, scale);
		}
	}
	public void paint(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, width, height);
		for(int i=0;i<triPop;i++) { 
			triangles[i].paint(g);
		}
		for(int i=0;i<cirPop;i++) { 
			circles[i].paint(g);
		}
	}

	/*
	 * protocol is :
	 * 0) receives int width then int height
	 * 1) receives circles until "Int.maxValue" which means end of circles
	 * 2) receives triangles until Int.maxValue which means end of the protocol
	 */
	public Sketch(DataInputStream reader) throws IOException {
		
		this.width = reader.readInt();
		this.height = reader.readInt();
		
		int flag = reader.readInt();
		
		ArrayList<Circle> receivedCircles = new ArrayList<Circle>();
		
		while(flag != Integer.MAX_VALUE) {
			int x = flag;
			int y = reader.readInt();
			int size = reader.readInt();
			int R = reader.readInt();
			int G = reader.readInt();
			int B = reader.readInt();
			receivedCircles.add(new Circle(x,y,size,R,G,B));
			
			flag = reader.readInt();
		}
		
		ArrayList<Triangle> receivedTriangles = new ArrayList<Triangle>();
		
		flag = reader.readInt();
		
		while(flag != Integer.MAX_VALUE) {
			int x0 = flag; int y0 = reader.readInt();
			int x1 = reader.readInt(); int y1 = reader.readInt();
			int x2 = reader.readInt(); int y2 = reader.readInt();
			int R=reader.readInt(); int G=reader.readInt(); int B=reader.readInt();
			receivedTriangles.add(new Triangle(x0,y0,x1,y1,x2,y2,R,G,B));
			flag = reader.readInt();
		}
		
		this.cirPop = receivedCircles.size();
		this.circles = new Circle[cirPop];
		for(int i=0; i<cirPop; i++){this.circles[i]=receivedCircles.get(i);}
		this.triPop = receivedTriangles.size();
		this.triangles = new Triangle[triPop];
		for(int i=0; i<triPop; i++){this.triangles[i]=receivedTriangles.get(i);}
		this.image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		updateImage();
	}
	
	public void sendSketch(DataOutputStream out) throws IOException {
		out.writeInt(this.width);
		out.writeInt(this.height);
		for(int i=0;i<this.circles.length;i++){
			this.circles[i].write(out);
		}
		out.writeInt(Integer.MAX_VALUE);
		for(int i=0;i<this.triangles.length;i++){
			this.triangles[i].write(out);
		}
		out.writeInt(Integer.MAX_VALUE);
	}
}