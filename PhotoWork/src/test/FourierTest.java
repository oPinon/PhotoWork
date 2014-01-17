package test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import pImage.*;

import javax.imageio.ImageIO;

public class FourierTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		//loads Image from file
		BufferedImage originalSource = ImageIO.read(new File("monaRaw.png"));
		BufferedImage source = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = source.createGraphics();
		g.drawImage(originalSource, 0, 0, 64, 64, null);
		g.dispose();
		System.out.println("image loaded");
		//makes it a PImage object
		PImage img = new PImage(source);
		System.out.println("image converted to PImage");
		//does a Fourier Transform to create the image's spectrum
		ImageSpectrum spectrum = new ImageSpectrum(img, null);
		System.out.println("spectrum computed");
		//converts it back to image with reverse Fourier Transform
		
		int f = 8; // the width of the part we remove in the spectrum
		for(int x = -f; x <= f; x++){
			for(int y = -f; y <= f; y++){
				int x1 = source.getWidth()/2+x;
				int y1 = source.getHeight()/2+y;
				
				if(x!=0&&y!=0){ // musn't remove the constant part of the spectrum
				spectrum.getRSpectrum().setComplex(x1, y1, new Complex(0,0));
				spectrum.getGSpectrum().setComplex(x1, y1, new Complex(0,0));
				spectrum.getBSpectrum().setComplex(x1, y1, new Complex(0,0));
				}
			}
		}
		
		PImage result = spectrum.getReverseTransform();
		result = filter.AutoBalance.balanceColors(result, null);

		System.out.println("reverse transform done");
		
		BufferedImage toReturn = new BufferedImage(originalSource.getWidth(), originalSource.getHeight(), BufferedImage.TYPE_INT_RGB);
		g = toReturn.createGraphics();
		g.drawImage(result.getImage(), 0, 0, originalSource.getWidth(), originalSource.getHeight(), null);
		g.dispose();
		
		new gAPainter.Display(toReturn);
		
		//ImageIO.write(calculated.getImage(), "png", new File("calculated.png"));
	}

}
