package test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import pImage.PImage;

public class HDRTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		BufferedImage originalSource = ImageIO.read(new File("hdr.png"));

		PImage source = new PImage(originalSource);
		
		PImage toReturn = filter.HDREqualizer.filter2(source, 50, null);
		
		toReturn = filter.AutoBalance.balanceColors(toReturn, null);
		
		originalSource = filter.AutoBalance.balanceColors(new pImage.PImage(originalSource), null).getImage();
		
		new gAPainter.Display(originalSource).setImage(toReturn.getImage());
	}

}
