package test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import gAPainter.*;

public class GANetworkTestClient {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		Socket socket = new Socket("localhost",6897);
		
		Sketch sketch = new Sketch(socket);
		
		new Display(sketch.getIm());
	}

}
