package imageComputing;

import java.io.IOException;

public interface Client {
	void sendImage() throws IOException, InterruptedException;
	void receiveImage() throws IOException, InterruptedException;
	void newConnection() throws IOException, InterruptedException;
	void endConnection();
	void interrupt();
}

