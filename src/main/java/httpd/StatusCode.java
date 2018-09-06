package httpd;

/**
 * HTTP Status Codes.
 */
public enum StatusCode {

    NOT_FOUND(404, "Not Found"),

    BAD_REQUEST(400, "Bad Request"),

    NOT_IMPLEMENTED(501, "Not Implemented"),

    PRECONDITION_FAILED(412, "Precondition Failed"),

    NOT_MODIFIED(304, "Not Modified");

    private final int id;

    private final String description;

    private StatusCode(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

}
