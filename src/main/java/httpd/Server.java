package httpd;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {

	public static void main(String[] args) {
		new Server().start();
	}

	public void start() {
		try (ServerSocket ss = new ServerSocket(8888)) {
			while (true) {
				try {
					new Thread(new Worker(ss.accept())).start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		catch (IOException e)
		{
			
		}
	}

}
