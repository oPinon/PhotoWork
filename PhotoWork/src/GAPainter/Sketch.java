package GAPainter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Sketch {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		Sketch s = new Sketch(200,200,100);
		ImageIO.write(s.getIm(), "jpg", new File("C:/Users/Olivier/Desktop/mutation/image"+0+".jpg"));
		
		for(int i=1; i<1000; i++) {
			s.mutate();
			ImageIO.write(s.getIm(), "jpg", new File("C:/Users/Olivier/Desktop/mutation/image"+i+".jpg"));
		}
		
	}
	
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
