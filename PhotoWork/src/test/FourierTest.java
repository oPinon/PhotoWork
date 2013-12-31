package test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import pImage.*;
import filter.*;

import javax.imageio.ImageIO;

public class FourierTest {

	public static void main(String[] args) throws IOException {
		
		//loads Image from file
		BufferedImage originalSource = ImageIO.read(new File("cln1.gif"));
		BufferedImage source = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = source.createGraphics();
		g.drawImage(originalSource, 0, 0, 64, 64, null);
		g.dispose();
		System.out.println("image loaded");
		//makes it a PImage object
		PImage img = new PImage(source);
		System.out.println("image converted to PImage");
		//does a Fourier Transform to create the image's spectrum
		ImageSpectrum spectrum = new ImageSpectrum(img);
		System.out.println("spectrum computed");
		//converts it back to image with reverse Fourier Transform

		PImage result = spectrum.getTransform();

		System.out.println("reverse transform done");
		
		new gAPainter.Display(result.getImage());
		
		//ImageIO.write(result.getImage(), "png", new File("result.png"));
	}

}
