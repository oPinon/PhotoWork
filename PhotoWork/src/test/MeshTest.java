package test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import gAPainter.*;

import javax.imageio.ImageIO;


public class MeshTest {

	public static void main(String[] args) throws IOException {
	
		BufferedImage source = ImageIO.read(new File("monaRaw.png"));
		Mesh mesh = new Mesh(source,10);
		new gAPainter.Display(mesh.getImage());
	}

}
