package gAPainter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

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
	
	public BufferedImage render(int scale) {
		BufferedImage img = new BufferedImage(width*scale,height*scale,BufferedImage.TYPE_INT_RGB);
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

	public void paint(Graphics g, int scale) {
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
}
