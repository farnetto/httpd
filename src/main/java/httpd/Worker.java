package httpd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker implements Runnable
{
    private static final Logger LOGGER = Logger.getLogger(Worker.class.getName());

    private static final String CRLF = "\r\n";

    private final String docroot;

    private final Socket s;

    public Worker(String docroot, Socket s)
    {
        this.docroot = docroot;
        this.s = s;
    }

    @Override
    public void run()
    {
        try
        {
            LOGGER.log(Level.FINE, "accepted connection " + s.getInetAddress() + ":" + s.getPort());
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            try
            {
                Request req = readRequest(in);
                if (req != null)
                {
                    writeResponse(req, out);
                }
            }
            catch (HttpError httpError)
            {
                writeError(httpError, out);
            }
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
     * @param httpError
     * @param out
     * @throws IOException
     */
    private void writeError(HttpError httpError, BufferedWriter out) throws IOException
    {
        LOGGER.log(Level.FINE, "returning " + httpError);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">");
        html.append("<html><head>");
        html.append("<title>404 Not Found</title>");
        html.append("</head><body>");
        html.append("<h1>Not Found</h1>");
        html.append(String.format("<p>The requested URL %s was not found on this server.</p>", httpError.getContent()));
        html.append("</body></html>");

        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 " + httpError.getCode().getId() + " " + httpError.getCode().getDescription() + CRLF);
        response.append("Date: " + LocalDateTime.now() + CRLF);
        response.append("Server: Farnetto" + CRLF);
        response.append("Content-Type: text/html; charset=UTF-8" + CRLF);
        response.append("Content-Length: " + html.length() + CRLF);
        // response.append("Keep-Alive: timeout=15, max=100" + CRLF);
        response.append("Connection: close" + CRLF);
        response.append(CRLF);

        out.write(response.toString());
        out.write(html.toString());
    }

    /**
     * @param in
     * @return
     * @throws IOException
     */
    private Request readRequest(BufferedReader in) throws IOException
    {
        List<String> lines = new ArrayList<>();
        String line = null;
        while ((line = in.readLine()) != null)
        {
            lines.add(line);
            if (line.trim().equals(""))
            {
                break;
            }
        }
        if (lines.isEmpty())
        {
            LOGGER.log(Level.WARNING, "request is empty");
            return null;
        }
        return new Request(lines);
    }

    /**
     * @param req
     * @param out
     * @throws IOException
     * @throws HttpError
     */
    private void writeResponse(Request req, BufferedWriter out) throws IOException, HttpError
    {
        File f = new File(docroot, req.getResource());
        LOGGER.log(Level.FINE, "getting " + f);
        if (!f.exists())
        {
            throw new HttpError(StatusCode.NOT_FOUND, req.getResource());
        }
        String content = "nixda";
        if (f.isDirectory())
        {
            content = new DirectoryList().list(f);
        }

        // everything ok, write response now
        out.write("HTTP/1.1 200 OK" + CRLF);
        out.write("Date: " + LocalDateTime.now() + CRLF); // Tue, 28 Aug 2018 11:48:17 GMT
        out.write("Server: Farnetto " + CRLF);
        // out.write("Last-Modified: Wed, 01 Apr 2015 10:26:11 GMT\n");
        // out.write("ETag: \"3ab-512a724ea7e20\"\n");
        out.write("Accept-Ranges: bytes" + CRLF);
        out.write("Connection: close" + CRLF);
        out.write(String.format("Content-Length: %d" + CRLF, content.length()));
        out.write("Vary: Accept-Encoding" + CRLF);
        out.write("Content-Type: text/html; charset=UTF-8" + CRLF);
        out.write(CRLF);
        out.write(content);
    }

}
