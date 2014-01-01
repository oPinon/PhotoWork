package network;

import java.io.IOException;
import java.net.ServerSocket;

public class Server extends Thread{

	private static ServerSocket socket;

	public Server() {
		try {
			socket = new ServerSocket(6789);
			System.out.println("serveur: cr�ation sur cet ordinateur");
		} catch (IOException e) {
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


