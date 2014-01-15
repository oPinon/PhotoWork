package test;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;

import gAPainter.*;

public class GANetworkTestServer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Sketch sketch = new Sketch(200,200,100,100);		
		new Display(sketch.getIm());
		
		ServerSocket server = new ServerSocket(6897);
		
		Socket socket = server.accept();
		
		sketch.sendSketch(new DataOutputStream(socket.getOutputStream()));
	}

}
