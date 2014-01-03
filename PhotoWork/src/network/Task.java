package network;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import filter.ImageFunction;

/**
 * Repr�sente une image � traiter, avec les param�tres du traitement.
 * 
 * @author Pierre-Alexandre Durand
 *
 */
public class Task {
	BufferedImage image;
	ImageFunction function;
	int imageNumber;
	int[] parameters;

	public Task(BufferedImage image, ImageFunction functionID,
			int imageNumber, int[] parameters) {
		super();
		this.image = image;
		this.function = functionID;
		this.imageNumber = imageNumber;
		this.parameters = parameters;
	}

	public void sendToStream(DataOutputStream toServer) throws IOException{
		ImageIO.write(image, "png", toServer);
		toServer.writeUTF(function.name());
		toServer.writeInt(imageNumber);
		for(int i: parameters){
			toServer.writeInt(i);
		}
	}

}
