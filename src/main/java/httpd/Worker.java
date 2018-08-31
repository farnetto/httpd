package httpd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import httpd.Request.Method;

public class Worker implements Runnable
{
    private static final Logger LOGGER = Logger.getLogger(Worker.class.getName());

    public static final String CRLF = "\r\n";

    private final File docroot;

    private final Socket s;

    private final Map<String,String> eTags;

    public Worker(File docroot, Map<String,String> eTags, Socket s)
    {
        this.docroot = docroot;
        this.s = s;
        this.eTags = eTags;
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
        html.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">").append(CRLF);
        html.append("<html><head>").append(CRLF);
        html.append(String.format("<title>%s %s</title>", httpError.getCode().getId(), httpError.getCode().getDescription())).append(CRLF);
        html.append("</head><body>").append(CRLF);
        html.append("<h1>Not Found</h1>").append(CRLF);
        html.append(String.format("<p>The requested URL %s was not found on this server.</p>", httpError.getContent())).append(CRLF);
        html.append("</body></html>").append(CRLF);

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
     * @throws HttpError
     */
    private Request readRequest(BufferedReader in) throws IOException, HttpError
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
        String resource = req.getResource();

        LOGGER.log(Level.FINE, "getting " + resource);

        if (req.getMethod() != Method.GET && req.getMethod() != Method.HEAD)
        {
            throw new HttpError(StatusCode.NOT_IMPLEMENTED);
        }

        // exists?
        File f = new File(docroot, resource);
        if (!f.exists())
        {
            throw new HttpError(StatusCode.NOT_FOUND, resource);
        }

        // determine content type
        String contentType = "";
        if (f.isDirectory())
        {
            contentType = "text/html; charset=UTF-8";
        }
        else
        {
            contentType = getContentType(f);
        }

        long contentLength = f.length();

        // retrieve content
        char[] content = null;
        if (req.getMethod() == Method.GET)
        {
            if (f.isDirectory())
            {
                content = new DirectoryList().list(docroot, resource).toCharArray();
            }
            else
            {
                // TODO handle large files
                content = new char[(int) contentLength];
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8).read(content, 0, (int) contentLength);
            }
        }

        // everything ok, write response
        out.write("HTTP/1.1 200 OK" + CRLF);
        out.write("Date: " + now() + CRLF);
        out.write("Server: Farnetto " + CRLF);
        out.write("Last-Modified: " + getLastModified(f) + CRLF);
        out.write(String.format("ETag: \"%s\"" + CRLF, getETag(f, resource)));
        out.write("Accept-Ranges: bytes" + CRLF);
        out.write("Connection: close" + CRLF);
        out.write(String.format("Content-Length: %d" + CRLF, contentLength));
        out.write("Vary: Accept-Encoding" + CRLF);
        out.write("Content-Type: " + contentType + CRLF);
        out.write(CRLF);
        if (req.getMethod() == Method.GET)
        {
            out.write(content);
        }
    }

    /**
     * @param f
     * @return
     */
    private String getLastModified(File f)
    {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(new Date(f.lastModified()).toInstant(), ZoneId.of("GMT")));
    }

    /**
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types
     * 
     * TODO guess encoding for text files
     */
    private String getContentType(File file) throws IOException
    {

        String name = file.getName().toLowerCase();
        String ct = Files.probeContentType(file.toPath());
        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.log(Level.FINE, String.format("name=%s type=%s", name, ct));
        }

        if (ct != null)
        {
            return ct;
        }
        if (name.endsWith(".xml"))
        {
            return "text/xml; charset=ISO-8859-1";
        }
        if (name.endsWith(".java"))
        {
            return "text/plain; charset=UTF-8";
        }
        if (name.endsWith(".jar"))
        {
            return "application/x-java-archive";
        }
        return "application/octet-stream";
    }

    private String now()
    {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
    }

    /**
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/ETag
     * 
     * 
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Conditional_requests
     * 
     * @return
     */
    private String getETag(File file, String resource)
    {
        return String.valueOf((resource + file.lastModified()).hashCode());
    }
}
