package httpd;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server
{
    private static final Logger LOGGER = LogManager.getLogger(Server.class);

    private final File docroot;
    
    private final int port;

    /**
     * constructor
     * 
     * @param docroot document root directory
     */
    public Server(int port, String docrootName)
    {
    	this.port = port;
        this.docroot = new File(docrootName);
        if (!docroot.exists() || !docroot.isDirectory())
        {
            throw new RuntimeException("docroot does not exist or is not a directory: " + docrootName);
        }
    }

    public static void main(String[] args)
    {
    	// defaults
    	int port = 80;
    	String docroot = ".";
    	if (args.length > 0)
    	{
    		try
    		{
    			port = Integer.parseInt(args[0]);
    		}
    		catch (NumberFormatException e)
    		{
    			LOGGER.warn(String.format("Could not parse port %d", args[0]));
    		}
    		if (args.length > 1)
    		{
    			File d = new File(args[1]);
    			if (!d.exists() || !d.isDirectory())
    			{
    				LOGGER.error(String.format("docroot does not exist or is not a directory %s", args[1]));
    				System.exit(1);
    			}
    			docroot = args[1];
    		}
    	}
        new Server(port, docroot).start();
    }

    public static void usage()
    {
    	System.out.println("Usage: Server [port [docroot]]");
    }
    public void start()
    {
    	LOGGER.info(String.format("Starting server on port %d with docroot %s", port, docroot));
        try (ServerSocket serverSocket = new ServerSocket(port))
        {
            LOGGER.debug("listening on port " + serverSocket.getLocalPort());
            while (true)
            {
                try
                {
                    new Thread(new Worker(docroot, serverSocket.accept())).start();
                }
                catch (IOException e)
                {
                    // TODO
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.error("could not start server socket", e);
        }
    }

}
