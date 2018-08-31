package httpd;

public enum StatusCode
{
    NOT_FOUND(404, "Not Found");

    private final int id;

    private final String description;

    private StatusCode(int id, String description)
    {
        this.id = id;
        this.description = description;
    }

    public int getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }
}
