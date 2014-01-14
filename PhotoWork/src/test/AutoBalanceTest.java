package test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import pImage.PImage;
import filter.*;

public class AutoBalanceTest {

	public static void main(String[] args) throws IOException {
		
		BufferedImage source = ImageIO.read(new File("monaRaw.png"));
		PImage img = new PImage(source);
		int[] R = new int[256];
		int[] G = new int[256];
		int[] B = new int[256];
		int nbThreads = Runtime.getRuntime().availableProcessors();
		AutoBalance.getColors(img, R, G, B, nbThreads);
		System.out.println("nbThreads = "+nbThreads);
		for(int i=0;i<256;i++) {System.out.println("value "+i+" : R="+R[i]+" G="+G[i]+" B="+B[i]);}
		
		PImage result = AutoBalance.balance(img, nbThreads, null);
		new gAPainter.Display(result.getImage());
	}

}
