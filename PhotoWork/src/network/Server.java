package network;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

/**
 * Le serveur re�oit les demandes de traitement �manant des clients des ordinateurs utilisant l'application.
 * 
 * @author Pierre-Alexandre Durand
 *
 */
public class Server extends Thread{

	private static ServerSocket socket;

	public Server() throws BindException {
		try {
			socket = new ServerSocket(6789);
			System.out.println("serveur: cr�ation sur cet ordinateur");
		} catch (BindException e) {
			throw e;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run(){
		while(true){
			try {
				new ComputationThread(socket.accept()).start();
			} catch (IOException e) {
				System.out.println("serveur: fin de connection");
				return;
			}
		}
	}

	public void terminate(){
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


