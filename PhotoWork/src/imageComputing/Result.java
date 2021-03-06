package imageComputing;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Represente le resultat d'un traitement en cours ou fini: image traitee et son numero.
 *   
 */
public class Result {
	private BufferedImage image; // si progress n'est pas 100, devrait etre VOID_IMAGE.
	private int imageNumber;
	private double progress; // en pourcentage
	
	public static BufferedImage VOID_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

	public Result(BufferedImage image, int imageNumber, double progress) {
		this.image = image;
		this.imageNumber = imageNumber;
		this.progress = progress;
	}

	public BufferedImage getImage() {
		return image;
	}
	
	public int getImageNumber() {
		return imageNumber;
	}
	
	public double getProgress() {
		return progress;
	}

	/**
	 * Envoie les donnees d'un Result dans un outputStream.
	 * 
	 * @param toClient
	 * @throws IOException
	 */
	public void sendToStream(DataOutputStream toClient) throws IOException{
		ImageIO.write(image, "png", toClient);
		toClient.writeInt(imageNumber);
		toClient.writeDouble(progress);
	}
}
