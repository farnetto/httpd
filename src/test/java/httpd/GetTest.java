package httpd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class GetTest extends HttpTest
{
    @Test
    public void simple() throws IOException, InterruptedException
    {
        List<String> request = new ArrayList<>();
        request.add("GET /test.html HTTP/1.1");
        request.add("User-Agent: Mozilla/4.0");
        request.add("Host: junit");
        request.add("Accept-Language: en-us");
        request.add("Accept-Encoding: gzip, deflate");
        List<String> response = getResponse(request);

        assertEquals("HTTP/1.1 200 OK", response.get(0));
        long expectedLength = new File(docroot, "test.html").length();
        Map<String,String> headers = getHeaders(response);
        boolean contentLengthFound = headers.containsKey("Content-Length");
        String content = getContent(response);
        assertEquals(expectedLength, Integer.parseInt(headers.get("Content-Length")));
        assertEquals(expectedLength, content.toString().length());
        assertTrue("header Content-Length not found in response", contentLengthFound);
    }
}
