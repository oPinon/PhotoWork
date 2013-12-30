package network;

import java.io.IOException;
import java.net.ServerSocket;

public class PoolServer{

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

	public void handleTask(){
		try {
			pool.addTask(new Task(socket.accept()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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


