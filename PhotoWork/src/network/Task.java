package network;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import filter.ImageFunction;

/**
 * Représente une image à traiter, avec les paramètres du traitement
 * 
 * @author Pierre-Alexandre Durand
 *
 */
public class Task {
	BufferedImage image;
	String extension;
	ImageFunction function;
	int imageNumber;
	int[] parameters;

	public Task(BufferedImage image, String extension, ImageFunction functionID,
			int imageNumber, int[] parameters) {
		super();
		this.image = image;
		this.extension = extension;
		this.function = functionID;
		this.imageNumber = imageNumber;
		this.parameters = parameters;
	}

	public void sendToStream(DataOutputStream toServer) throws IOException{
		ImageIO.write(image, extension, toServer);
		toServer.writeUTF(extension);
		toServer.writeUTF(function.name());
		toServer.writeInt(imageNumber);
		for(int i: parameters){
			toServer.writeInt(i);
		}
	}

}
