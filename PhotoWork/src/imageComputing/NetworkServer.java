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
			System.out.println("serveur: creation sur cet ordinateur");
		} catch (BindException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run(){
		while(true){
			try {
				new NetworkComputationThread(socket.accept()).start();
			} catch (IOException e) {
				System.out.println("serveur: fin de connection");
				return;
			}
		}
	}

	public void closeSocket(){
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
