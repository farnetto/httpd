package httpd;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class MaliciousRequestTest extends HttpdTest
{
    @Test
    public void testDotDot() throws IOException
    {
    	List<String> request = getRequest("/..");
		addStandardHeaders(request);
		List<String> response = getResponse(request);
		assertEquals("HTTP/1.1 400 Bad Request", response.get(0));
    }
}
