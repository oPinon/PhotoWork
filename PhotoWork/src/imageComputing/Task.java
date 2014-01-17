package imageComputing;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import filter.ImageFunction;

/**
 * Represente une image a traiter, avec les parametres du traitement.
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
	
	public BufferedImage getImage() {
		return image;
	}

	public ImageFunction getFunction() {
		return function;
	}

	public int getImageNumber() {
		return imageNumber;
	}

	public int[] getParameters() {
		return parameters;
	}

	/**
	 * Envoie les donnees d'un Task dans un outputStream.
	 * 
	 * @param toServer
	 * @throws IOException
	 */
	public void sendToStream(DataOutputStream toServer) throws IOException{
		ImageIO.write(image, "png", toServer);
		toServer.writeUTF(function.name());
		toServer.writeInt(imageNumber);
		for(int i: parameters){
			toServer.writeInt(i);
		}
	}

}
