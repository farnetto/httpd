package httpd;

public class HttpError extends Exception {
	private final StatusCode code;

	private String content;

	public HttpError(StatusCode code) {
		this.code = code;
	}

	public HttpError(StatusCode code, String content) {
		this(code);
		this.content = content;
	}

	public StatusCode getCode() {
		return code;
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return String.format("HttpError[code=%s, content=%s]", code, content);
	}
}
