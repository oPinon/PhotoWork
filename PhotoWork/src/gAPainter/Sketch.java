package gAPainter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Sketch {
	
	static boolean isTriangle = false;;
	
	private int width, height, pop;
	private Shape[] shapes;
	private BufferedImage image;
	
	public Sketch(int width, int height, int pop) {
		this.width=width;
		this.height=height;
		this.pop=pop;
		shapes = new Shape[pop];
		for(int i=0;i<pop;i++) {
			int x = (int) (width*Math.random());
			int y = (int) (height*Math.random());
			int size = (int) ((height/10)*(1+3*Math.random()));
			if(isTriangle) {shapes[i]=new Triangle(x,y,size); }
			else {shapes[i]=new Circle(x,y,size);}
		}
		image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		//updateImage();
	}
	
	public Sketch(Sketch other) {
		this.width = other.width;
		this.height = other.height;
		this.pop = other.pop;
		this.shapes = new Shape[pop];
		for(int i=0;i<pop;i++) {
			if(isTriangle) {this.shapes[i]=new Triangle((Triangle)other.shapes[i]);}
			else {this.shapes[i]=new Circle((Circle)other.shapes[i]);}
		}
		image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		//updateImage();
	}
	
	public void mutate() {
		for(int i=0;i<pop;i++) {
			if(Math.random()<0.4) { this.shapes[i].mutate(); }
		}
		//updateImage();
	}
	
	/*
	 * ratio is the percentage of each parent's DNA you keep. A ratio of 0.5 will keep the DNA's size.
	 */
	public void merge(Sketch other, double ratio) {
		
		int newSize = (int)(2*ratio*pop);
		Shape[] newShapes = new Shape[newSize];
		int k = 0;
		for(int i=0; i<newSize;i++){
			if(k>=pop){k=0;}
			if(Math.random()<0.5){ newShapes[i]=this.shapes[k]; }
			else { newShapes[i]=other.shapes[k]; }
			k++;
		}
		this.shapes = newShapes;
	}
	
	public BufferedImage render(int scale) {
		BufferedImage img = new BufferedImage(width*scale,height*scale,BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		paint(g,scale);
		return img;
	}
	
	public BufferedImage getIm() {
		updateImage();
		return image;
	}
	
	public void updateImage() {
		Graphics g = image.getGraphics();
		paint(g,1);
	}

	public void paint(Graphics g, int scale) {
		g.setColor(Color.black);
		g.fillRect(0, 0, width, height);
		for(int i=0;i<pop;i++) { 
			shapes[i].paint(g, scale);
		}
	}
}
