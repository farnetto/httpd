package httpd;

public enum Header {
	IF_MODIFIED_SINCE("If-Modified-Since"), IF_NONE_MATCH("If-None-Match"), CONNECTION("Connection"), KEEP_ALIVE("Keep-Alive");
	
	private final String text;
	
	private Header(String text)
	{
		this.text = text;
	}
	
	public String getText()
	{
		return text;
	}
}
