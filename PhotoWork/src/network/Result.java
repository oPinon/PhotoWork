package network;

import java.awt.image.BufferedImage;

/**
 * Représente le résultat d'un traitement: image traitée et son numéro.
 * 
 * @author Pierre-Alexandre Durand
 *   
 */
public class Result {
	BufferedImage result;
	int imageNumber;
	
	public Result(BufferedImage result, int imageNumber) {
		super();
		this.result = result;
		this.imageNumber = imageNumber;
	}

	public BufferedImage getResult() {
		return result;
	}

	public int getImageNumber() {
		return imageNumber;
	}
}
