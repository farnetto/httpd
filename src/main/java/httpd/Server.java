package httpd;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Server
{
    private static final LogManager logManager = LogManager.getLogManager();

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    static
    {
        try
        {
            logManager.readConfiguration(Thread.currentThread().getContextClassLoader().getResourceAsStream("httpd/logging.properties"));
        }
        catch (IOException exception)
        {
            LOGGER.log(Level.SEVERE, "Error in loading configuration", exception);
        }
    }

    private final File docroot;
    
    private final int port;

    private final Map<String,String> eTags = new ConcurrentHashMap<>();

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
    			LOGGER.log(Level.WARNING, String.format("Could not parse port %d", args[0]));
    		}
    		if (args.length > 1)
    		{
    			File d = new File(args[1]);
    			if (!d.exists() || !d.isDirectory())
    			{
    				LOGGER.log(Level.SEVERE, String.format("docroot does not exist or is not a directory %s", args[1]));
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
    	LOGGER.log(Level.INFO, String.format("Starting server on port %d with docroot %s", port, docroot));
        try (ServerSocket ss = new ServerSocket(port))
        {
            LOGGER.log(Level.FINE, "listening on port " + ss.getLocalPort());
            while (true)
            {
                try
                {
                    new Thread(new Worker(docroot, eTags, ss.accept())).start();
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
            LOGGER.log(Level.SEVERE, "could not start server socket", e);
        }
    }

}
