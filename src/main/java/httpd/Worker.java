package httpd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker implements Runnable
{
    private static final Logger LOGGER = Logger.getLogger(Worker.class.getName());

    private final Socket s;

    public Worker(Socket s)
    {
        this.s = s;
    }

    @Override
    public void run()
    {
        try
        {
            LOGGER.log(Level.FINE, "accepted connection " + s.getInetAddress() + ":" + s.getPort());
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            Request req = readRequest(in);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            writeResponse(req, out);
            out.flush();
            out.close();
        }
        catch (IOException e)
        {
            LOGGER.log(Level.SEVERE, "could not handle request", e);
        }
        finally
        {
            try
            {
                s.close();
            }
            catch (IOException e)
            {
                LOGGER.log(Level.FINE, "could not close socket", e);
            }
        }
        LOGGER.log(Level.FINE, "finished handling");
    }

    /**
     * @param in
     * @return
     * @throws IOException
     */
    private Request readRequest(BufferedReader in) throws IOException
    {
        java.util.List<String> lines = new ArrayList<>();
        String line = null;
        while ((line = in.readLine()) != null)
        {
            lines.add(line);
            if (line.trim().equals(""))
            {
                break;
            }
        }
        return new Request(lines);
    }

    /**
     * @param req
     * @param out
     * @throws IOException
     */
    private void writeResponse(Request req, BufferedWriter out) throws IOException
    {
        out.write("HTTP/1.1 200 OK\r\n");
        out.write("Date: " + LocalDateTime.now() + "\r\n"); // Tue, 28 Aug 2018 11:48:17 GMT
        out.write("Server: Farnetto \r\n");
        // out.write("Last-Modified: Wed, 01 Apr 2015 10:26:11 GMT\n");
        // out.write("ETag: \"3ab-512a724ea7e20\"\n");
        out.write("Accept-Ranges: bytes\r\n");
        out.write("Connection: close\r\n");
        String content = new DirectoryList().list(req.getResource());
        out.write(String.format("Content-Length: %d\r\n", content.length()));
        out.write("Vary: Accept-Encoding\r\n");
        out.write("Content-Type: text/html; charset=UTF-8\r\n");
        out.write('\n');
        out.write('\n');
        out.write(content);
    }

}
