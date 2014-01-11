package network;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Repr�sente le r�sultat d'un traitement en cours ou fini: image trait�e et son num�ro.
 * 
 * @author Pierre-Alexandre Durand
 *   
 */
public class Result {
	BufferedImage result; // si progress n'est pas � 100, devrait �tre nul.
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

	public static void sendDataToStream(BufferedImage result, int imageNumber, double progress, DataOutputStream toClient) throws IOException{
		if(result==null) ImageIO.write(VOID, "png", toClient);
		else ImageIO.write(result, "png", toClient);
		toClient.writeInt(imageNumber);
		toClient.writeDouble(progress);
	}
}
