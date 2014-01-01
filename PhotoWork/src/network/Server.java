package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

public class Server extends Thread{

	private static ServerSocket socket;

	public Server() {
		try {
			socket = new ServerSocket(6789);
			System.out.println("serveur: création sur cet ordinateur");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run(){
		while(!socket.isClosed()){
			try {
				new ComputationThread(socket.accept()).start();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("serveur: fin de connection");
				return;
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void terminate(){
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


