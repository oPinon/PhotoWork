package network;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import pImage.PImage;

public class Task {
	BufferedImage image;
	String extension;
	String function;
	int imageNumber;
	int[] parameters;

	public Task(BufferedImage image, String extension, String function,
			int imageNumber, int[] parameters) {
		super();
		this.image = image;
		this.extension = extension;
		this.function = function;
		this.imageNumber = imageNumber;
		this.parameters = parameters;
	}

	public void sendToStream(DataOutputStream toServer) throws IOException{
		ImageIO.write(image, extension, toServer);
		toServer.writeUTF(extension);
		toServer.writeUTF(function);
		toServer.writeInt(imageNumber);
		for(int i: parameters){
			toServer.writeInt(i);
		}
	}

}
