package test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import pImage.PImage;

public class HDRTest {

	public static void main(String[] args) throws IOException {
		
		BufferedImage originalSource = ImageIO.read(new File("monaRaw.png"));

		PImage source = new PImage(originalSource);
		
		PImage toReturn = filter.HDREqualizer.filter(source, 50);
		
		toReturn = filter.AutoBalance.balanceColors(toReturn);
		
		originalSource = filter.AutoBalance.balanceColors(new pImage.PImage(originalSource)).getImage();
		
		new gAPainter.Display(originalSource).setImage(toReturn.getImage());
	}

}
