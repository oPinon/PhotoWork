package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

public class PoolServer extends Thread{

	private static ServerSocket socket;
	private Pool pool;

	public PoolServer() {
		try {
			socket = new ServerSocket(6789);
			pool = new Pool(5, 5);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run(){
		while(!socket.isClosed()){
			try {
				pool.addTask(new Task(socket.accept()));
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


