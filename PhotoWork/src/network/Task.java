package network;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import filter.ImageFunction;

/**
 * Représente une image à traiter, avec les paramètres du traitement.
 *
 */
public class Task {
	private BufferedImage image;
	private ImageFunction function;
	private int imageNumber;
	private int[] parameters;

	public Task(BufferedImage image, ImageFunction functionID,
			int imageNumber, int[] parameters) {
		super();
		this.image = image;
		this.function = functionID;
		this.imageNumber = imageNumber;
		this.parameters = parameters;
	}

	public int getImageNumber() {
		return imageNumber;
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
