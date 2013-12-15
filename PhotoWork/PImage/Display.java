package PImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Display extends JPanel{

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		long t0 = System.currentTimeMillis();
		BufferedImage image = ImageIO.read(new File("test.jpg"));
		PImage pIm = new PImage(image);
		pIm = BlurFilter.blur(pIm,20);
		//pIm = new PImage(500,200);
		Display disp = new Display(pIm.getImage());
		BufferedImage image2 = ImageIO.read(new File("test.png"));
		PImage pIm2 = new PImage(image2);
		Display disp2 = new Display(pIm2.getImage());
		System.out.println("Done in "+(System.currentTimeMillis()-t0)+" ms.");
	}

	static int maxWidth = 800;
	static int maxHeight = 600;
	private Image image;
	private int width;
	private int height;
	
	public Display(Image image) {
		int w = image.getWidth(this);
		int h = image.getHeight(this);
		if(w*((maxHeight*1.0)/h)>maxWidth) {
			width = maxWidth;
			height = (h*maxWidth)/w;
		}
		else {
			height = maxHeight;
			width = (w*maxHeight)/h;
		}
		this.image = image;
		JFrame frame = new JFrame();
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

	}
	
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(AlphaComposite.Src);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawImage(image,0,0,width,height,this);
	}
}
