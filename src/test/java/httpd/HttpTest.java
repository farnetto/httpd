package httpd;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpTest
{
    protected final File docroot = new File("target/test-classes");

    public List<String> getResponse(List<String> request) throws IOException, InterruptedException
    {
        PipedInputStream pipedIn = new PipedInputStream();
        PipedOutputStream pipedOut = new PipedOutputStream();
        PipedOutputStream serverOut = new PipedOutputStream(pipedIn);
        PipedInputStream serverIn = new PipedInputStream(pipedOut);
        Socket s = mock(Socket.class);
        when(s.getInputStream()).thenReturn(serverIn);
        when(s.getOutputStream()).thenReturn(serverOut);

        Thread t = new Thread(new Worker(docroot, new HashMap<String,String>(), s));
        t.start();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(pipedOut));
        BufferedReader in = new BufferedReader(new InputStreamReader(pipedIn));
        for (String requestLine : request)
        {
            out.write(requestLine);
            out.write(Worker.CRLF);
        }
        out.write("\r\n");
        out.flush();
        t.join(5000L);
        List<String> response = new ArrayList<>();
        String responseLine = null;
        while ((responseLine = in.readLine()) != null)
        {
            response.add(responseLine);
        }
        return response;
    }

    public Map<String,String> getHeaders(List<String> response)
    {
        Map<String,String> headers = new HashMap<>();
        for (String s : response.subList(1, response.size()))
        {
            if (s.equals(""))
            {
                break;
            }
            int colon = s.indexOf(":");
            headers.put(s.substring(0, colon), s.substring(colon + 2));
        }
        return headers;
    }

    public String getContent(List<String> response)
    {
        StringBuilder content = new StringBuilder();
        boolean inHeaders = true;
        for (String s : response)
        {
            if (!inHeaders)
            {
                content.append(s).append(Worker.CRLF);
            }
            if (s.equals(""))
            {
                inHeaders = false;
            }
        }
        return content.toString();
    }
}
