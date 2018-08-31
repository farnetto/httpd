package httpd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

import org.junit.Test;

public class GetTest
{
    @Test
    public void simple() throws IOException, InterruptedException
    {
        PipedInputStream pipedIn = new PipedInputStream();
        PipedOutputStream pipedOut = new PipedOutputStream();
        PipedOutputStream serverOut = new PipedOutputStream(pipedIn);
        PipedInputStream serverIn = new PipedInputStream(pipedOut);
        Socket s = mock(Socket.class);
        when(s.getInputStream()).thenReturn(serverIn);
        when(s.getOutputStream()).thenReturn(serverOut);
        File docroot = new File("target/test-classes");
        Thread t = new Thread(new Worker(docroot, s));
        t.start();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(pipedOut));
        BufferedReader in = new BufferedReader(new InputStreamReader(pipedIn));
        out.write("GET /test.html HTTP/1.1\r\n");
        out.write("User-Agent: Mozilla/4.0\r\n");
        out.write("Host: junit\r\n");
        out.write("Accept-Language: en-us\r\n");
        out.write("Accept-Encoding: gzip, deflate\r\n");
        out.write("\r\n");
        out.flush();
        t.join(5000L);
        assertEquals("HTTP/1.1 200 OK", in.readLine());
        String line = null;
        boolean contentLengthFound = false;
        long expectedLength = new File(docroot, "test.html").length();
        while ((line = in.readLine()) != null)
        {
            if (line.contains("Content-Length:"))
            {
                contentLengthFound = true;
                assertEquals(expectedLength, Long.parseLong(line.substring("Content-Length: ".length())));
            }
            if (line.equals(""))
            {
                StringBuilder content = new StringBuilder();
                while ((line = in.readLine()) != null)
                {
                    content.append(line).append(Worker.CRLF);
                }
                assertEquals(expectedLength, content.toString().length());
            }
        }
        assertTrue("header Content-Length not found in response", contentLengthFound);
    }
}
