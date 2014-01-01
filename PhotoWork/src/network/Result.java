package network;

import java.awt.image.BufferedImage;

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
