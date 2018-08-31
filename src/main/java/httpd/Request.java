package httpd;

import java.util.List;

public class Request
{
    enum Method
    {
        GET, POST, HEAD
    }

    private final List<String> text;

    private Method method;

    private String resource;

    public Request(List<String> lines)
    {
        this.text = lines;
        parse();
    }

    private void parse()
    {
        for (String line : text)
        {
            System.out.println("line: " + line);
        }
        System.out.println("text: " + text);
        String[] cmd = text.get(0).split("\\s");
        method = Method.valueOf(cmd[0]);
        resource = cmd[1];
    }

    public Method getMethod()
    {
        return method;
    }

    public String getResource()
    {
        return resource;
    }
}
