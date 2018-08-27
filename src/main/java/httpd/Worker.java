package httpd;

import java.net.Socket;

public class Worker implements Runnable {

	private final Socket s;

	public Worker(Socket s) {
		this.s = s;
	}

	public void run() {
		
	}

}
