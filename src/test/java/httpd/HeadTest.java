package httpd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Tests for the HEAD method.
 */
public class HeadTest extends HttpdTest {

    /**
     * Simple HEAD test requesting a file.
     */
    @Test
    public void simpleTest() throws IOException {
        List<String> request = new ArrayList<>();
        request.add("HEAD /test.html HTTP/1.1");
        addStandardHeaders(request);
        List<String> response = getResponse(request);

        assertEquals("HTTP/1.1 200 OK", response.get(0));
        long expectedContentLength = new File(docroot, "test.html").length();
        Map<String,String> headers = getHeaders(response);
        assertTrue("header Content-Length not found in response", headers.containsKey("Content-Length"));
        String content = getContent(response);
        assertEquals(expectedContentLength, Integer.parseInt(headers.get("Content-Length")));
        // content is empty with HEAD
        assertEquals(0, content.toString().length());
    }
}
