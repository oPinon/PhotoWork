package network;

import java.io.IOException;

public interface UpdaterClient {
	void sendImage() throws IOException, InterruptedException;
	void receiveImage() throws IOException, InterruptedException;
	void newConnection() throws IOException, InterruptedException;
	void terminate();
	void interrupt();
}

