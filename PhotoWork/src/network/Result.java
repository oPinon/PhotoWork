package network;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Represente le resultat d'un traitement en cours ou fini: image traitee et son numero.
 * 
 * @author Pierre-Alexandre Durand
 *   
 */
public class Result {
	BufferedImage result; // si progress n'est pas à 100, devrait être nul.
	int imageNumber;
	double progress; // en pourcentage
	
	private static BufferedImage VOID= new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);

	public Result(BufferedImage result, int imageNumber, double progress) {
		this.result = result;
		this.imageNumber = imageNumber;
		this.progress = progress;
	}

	public BufferedImage getResult() {
		return result;
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
	 * @param result
	 * @param imageNumber
	 * @param progress
	 * @param toClient S'il vaut null, la methode quitte immediatement sans rien faire
	 * @throws IOException
	 */
	public static void sendDataToStream(BufferedImage result, int imageNumber, double progress, DataOutputStream toClient) throws IOException{
		if(toClient == null) return;
		if(result == null) ImageIO.write(VOID, "png", toClient);
		else ImageIO.write(result, "png", toClient);
		toClient.writeInt(imageNumber);
		toClient.writeDouble(progress);
	}
}
