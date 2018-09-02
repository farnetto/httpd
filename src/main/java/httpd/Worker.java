package httpd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import httpd.Request.Method;

public class Worker implements Runnable {
	private static final Logger LOGGER = LogManager.getLogger(Worker.class.getName());

	public static final String CRLF = "\r\n";

	private final File docroot;

	private final Socket s;

	private final Map<String, String> eTags;

	public Worker(File docroot, Map<String, String> eTags, Socket s) {
		this.docroot = docroot;
		this.s = s;
		this.eTags = eTags;
	}

	@Override
	public void run() {
		LOGGER.debug("accepted connection " + s.getInetAddress() + ":" + s.getPort());
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
			OutputStream out = s.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
			while (true) {
				LOGGER.debug("listening for request...");
				try {
					Request req = readRequest(in);
					if (req != null) {
						writeResponse(req, writer, out);
						writer.flush();
					}
					else
					{
						// client closed connection
						break;
					}
				} catch (HttpError httpError) {
					writeError(httpError, writer);
				}
			}
		} catch (IOException e) {
			LOGGER.error("could not handle request", e);
		} finally {
			try {
				s.close();
			} catch (IOException e) {
				LOGGER.debug("could not close socket", e);
			}
		}
		LOGGER.debug("connection closed");
	}

	/**
	 * @param httpError
	 * @param out
	 * @throws IOException
	 */
	private void writeError(HttpError httpError, BufferedWriter out) throws IOException {
		LOGGER.debug("returning " + httpError);

		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">").append(CRLF);
		html.append("<html><head>").append(CRLF);
		html.append(String.format("<title>%s %s</title>", httpError.getCode().getId(),
				httpError.getCode().getDescription())).append(CRLF);
		html.append("</head><body>").append(CRLF);
		html.append("<h1>Not Found</h1>").append(CRLF);
		html.append(String.format("<p>The requested URL %s was not found on this server.</p>", httpError.getContent()))
				.append(CRLF);
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
	private Request readRequest(BufferedReader in) throws IOException, HttpError {
		List<String> lines = new ArrayList<>();
		String line = null;
		while ((line = in.readLine()) != null) {
			System.out.println("request line: " + line);
			lines.add(line);
			if (line.trim().equals("")) {
				break;
			}
		}
		if (lines.isEmpty()) {
			LOGGER.warn("request is empty");
			return null;
		}
		return new Request(lines);
	}

	/**
	 * @param request
	 * @param writer
	 * @param out
	 * @throws IOException
	 * @throws HttpError
	 */
	private void writeResponse(Request request, BufferedWriter writer, OutputStream out) throws IOException, HttpError {
		String resource = request.getResource();

		LOGGER.debug("getting " + resource);

		if (request.getMethod() != Method.GET && request.getMethod() != Method.HEAD) {
			throw new HttpError(StatusCode.NOT_IMPLEMENTED);
		}

		// exists?
		File resourceFile = new File(docroot, resource);
		if (!resourceFile.exists()) {
			throw new HttpError(StatusCode.NOT_FOUND, resource);
		}

		// determine content type
		String contentType = "";
		long contentLength = -1L;
		if (resourceFile.isDirectory()) {
			contentType = "text/html; charset=UTF-8";
		} else {
			contentType = getContentType(resourceFile);
			contentLength = resourceFile.length();
		}

		String eTag = getETag(resourceFile, resource);
		if (request.getHeaders().containsKey("If-Match"))
		{
			if (!request.getHeaders().get("If-Match").equals("\""+ eTag + "\""))
			{
				throw new HttpError(StatusCode.PRECONDITION_FAILED, resource);
			}
		}
		// retrieve content
		char[] textContent = null;
		byte[] binaryContent = null;
		if (request.getMethod() == Method.GET) {
			if (resourceFile.isDirectory()) {
				textContent = new DirectoryList().list(docroot, resource).toCharArray();
				contentLength = textContent.length;
			} else {
				// TODO handle large files
				if (isText(contentType)) {
					textContent = new char[(int) contentLength];
					// TODO read using appropriate encoding
					new InputStreamReader(new FileInputStream(resourceFile), StandardCharsets.UTF_8).read(textContent, 0,
							(int) contentLength);
				} else {
					binaryContent = new byte[(int) contentLength];
					new FileInputStream(resourceFile).read(binaryContent);
				}
			}
		}
		// everything ok, write response
		writer.write("HTTP/1.1 200 OK" + CRLF);
		writer.write("Date: " + now() + CRLF);
		writer.write("Server: Farnetto " + CRLF);
		writer.write("Last-Modified: " + getLastModified(resourceFile) + CRLF);
		writer.write("Connection: keep-alive" + CRLF);
		writer.write(String.format("ETag: \"%s\"" + CRLF, eTag));
		writer.write("Accept-Ranges: bytes" + CRLF);
		writer.write(String.format("Content-Length: %d" + CRLF, contentLength));
		writer.write("Vary: Accept-Encoding" + CRLF);
		writer.write("Content-Type: " + contentType + CRLF);
		writer.write(CRLF);
		writer.flush();
		if (request.getMethod() == Method.GET) {
			if (isText(contentType)) {
				writer.write(textContent);
				writer.flush();
			} else {
				out.write(binaryContent);
				out.flush();
			}
		}
	}

	private boolean isText(String contentType) {
		return contentType.startsWith("text") || contentType.endsWith("xml");
	}

	/**
	 * @param f
	 * @return
	 */
	private String getLastModified(File f) {
		return DateTimeFormatter.RFC_1123_DATE_TIME
				.format(ZonedDateTime.ofInstant(new Date(f.lastModified()).toInstant(), ZoneId.of("GMT")));
	}

	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types
	 * 
	 * TODO guess encoding for text files
	 */
	private String getContentType(File file) throws IOException {

		String name = file.getName().toLowerCase();
		String ct = Files.probeContentType(file.toPath());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("name=%s type=%s", name, ct));
		}

		if (ct != null) {
			return ct;
		}
		if (name.endsWith(".xml")) {
			return "text/xml; charset=ISO-8859-1";
		}
		if (name.endsWith(".java") || name.endsWith(".md")) {
			return "text/plain; charset=UTF-8";
		}
		if (name.endsWith(".jar")) {
			return "application/x-java-archive";
		}
		return "application/octet-stream";
	}

	private String now() {
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
	private String getETag(File file, String resource) {
		return String.valueOf((resource + file.lastModified()).hashCode());
	}
}
