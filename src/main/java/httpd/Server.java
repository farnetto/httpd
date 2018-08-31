package httpd;

import java.io.IOException;
import java.net.ServerSocket;
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
            logManager.readConfiguration(Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties"));
        }
        catch (IOException exception)
        {
            LOGGER.log(Level.SEVERE, "Error in loading configuration", exception);
        }
    }

    private final String docroot;

    /**
     * constructor
     * 
     * @param docroot document root directory
     */
    public Server(String docroot)
    {
        this.docroot = docroot;
    }

    public static void main(String[] args)
    {
        new Server(".").start();
    }

    public void start()
    {
        try (ServerSocket ss = new ServerSocket(8888))
        {
            LOGGER.log(Level.FINE, "listening on port " + ss.getLocalPort());
            while (true)
            {
                try
                {
                    new Thread(new Worker(docroot, ss.accept())).start();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
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
