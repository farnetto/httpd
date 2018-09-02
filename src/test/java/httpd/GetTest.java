package httpd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class GetTest extends HttpdTest {
	@Test
	public void getTextFile() throws IOException, InterruptedException {
		List<String> request = getRequest("/test.html");
		addStandardHeaders(request);
		List<String> response = getResponse(request);

		assertEquals("HTTP/1.1 200 OK", response.get(0));
		long expectedLength = new File(docroot, "test.html").length();
		Map<String, String> headers = getHeaders(response);
		assertTrue("header Content-Length not found in response", headers.containsKey("Content-Length"));
		String content = getContent(response);
		assertEquals(expectedLength, Integer.parseInt(headers.get("Content-Length")));
		assertEquals(expectedLength, content.toString().length());
	}
	
	@Test
	public void getBinaryFile() throws IOException, InterruptedException
	{
		String binaryFileName = "IMG_20180831_195639.jpg";
		List<String> request = getRequest("/" + binaryFileName);
		addStandardHeaders(request);
		List<String> response = getResponse(request);
		Map<String, String> headers = getHeaders(response);
		String content = getContent(response);
		// TODO read content as binary
//		assertEquals(content.length(), "" + headers.get("Content-Length"));
		// expect jpg file content bigger than 1MB
		assertTrue(content.length() > 1_000_000);
		
	}
	
	@Test
	public void getDirectory() throws IOException, InterruptedException {
		List<String> request = getRequest("/");
		addStandardHeaders(request);
		List<String> response = getResponse(request);
		assertEquals("HTTP/1.1 200 OK", response.get(0));
		Map<String, String> headers = getHeaders(response);
		assertTrue(headers.containsKey("Content-Length"));
		assertEquals(String.valueOf(getContent(response).length()), headers.get("Content-Length"));
	}
}
