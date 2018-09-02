package httpd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Request
{
    private static final Logger LOGGER = Logger.getLogger(Request.class.getName());

    enum Method
    {
        GET, POST, HEAD
    }

    private final List<String> text;

    private Method method;

    private String resource;

    private String httpVersion;
    
    private Map<String, String> headers = new HashMap<>();

    public Request(List<String> lines) throws HttpError
    {
        this.text = lines;
        parse();
    }

    private void parse() throws HttpError
    {
        for (String line : text)
        {
            LOGGER.log(Level.FINER, "    " + line);
        }
        String requestLine = text.get(0);
        String[] requestLineTokens = requestLine.split("\\s");
        if (requestLineTokens.length != 3)
        {
            throw new HttpError(StatusCode.BAD_REQUEST);
        }
        method = Method.valueOf(requestLineTokens[0]);
        resource = requestLineTokens[1];
        if (resource.contains("..") || resource.contains("\\"))
        {
        	throw new HttpError(StatusCode.BAD_REQUEST, resource);
        }
        httpVersion = requestLineTokens[2];
        for (int i = 1; i < text.size(); i++) {
        	String line = text.get(i);
        	if (line.trim().equals(""))
        	{
        		break;
        	}
        	int colon = line.indexOf(':');
        	String key = line.substring(0, colon);
        	String value = line.substring(colon + 2);
        	headers.put(key, value);
        }
    }

    public Method getMethod()
    {
        return method;
    }

    public String getResource()
    {
        return resource;
    }
    
    public Map<String, String> getHeaders()
    {
    	return headers;
    }
}
