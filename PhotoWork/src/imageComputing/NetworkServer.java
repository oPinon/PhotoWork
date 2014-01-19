package imageComputing;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

/**
 * Le serveur recoit les demandes de traitement emanant des clients des ordinateurs utilisant l'application.
 *
 */
public class NetworkServer extends Thread{

	private static ServerSocket socket;

	public NetworkServer() throws BindException {
		try {
			socket = new ServerSocket(6789);
			System.out.println("server: created on this computer");
		} catch (BindException e) {
			throw e;
		} catch (IOException e) {
			System.err.println("server: I/O error");
		}
	}

	public void run(){
		while(true){
			try {
				new NetworkComputationThread(socket.accept()).start();
			} catch (IOException e) {
				System.out.println("server: end of connection");
				return;
			}
		}
	}

	public void closeSocket(){
		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("server: error while closing the socket");
		}
	}
}
