package httpd;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectoryList
{
    private static final Logger LOGGER = Logger.getLogger(DirectoryList.class.getName());

    public String list(File docroot, String dirName)
    {
        File dir = new File(docroot, dirName);
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\r\n");
        sb.append("<html>\r\n");
        sb.append(" <head>\r\n");
        sb.append(String.format("  <title>Index of %s</title>\r\n", dirName));
        sb.append(" </head>\r\n");
        sb.append(" <body>\r\n");
        sb.append(String.format("<h1>Index of %s</h1>\r\n", dirName));
        sb.append("<ul>\r\n");
        if (parentExists(dirName))
        {
            sb.append(String.format("<li><a href=\"%s\"> Parent Directory</a></li>\r\n", getParent(dirName)));
        }
        for (String f : dir.list())
        {
            sb.append(String.format("<li><a href=\"%s\"> %s</a></li>\r\n", join(dirName, f), f));
        }
        sb.append("</ul>\r\n");
        sb.append("</body></html>\r\n");
        System.out.println(sb);
        return sb.toString();
    }

    /**
     * @param dir
     * @param f
     * @return
     */
    private Object join(String dir, String f)
    {
        if (dir.equals("") || dir.equals("/"))
        {
            return "/" + f;
        }
        if (dir.endsWith("/"))
        {
            return dir + f;
        }
        return dir + "/" + f;
    }

    /**
     * @param dir
     * @return
     */
    private String getParent(String dir)
    {
        LOGGER.log(Level.FINE, "looking for parent of " + dir);
        if (!parentExists(dir))
        {
            return "/";
        }
        String[] dirs = split(dir);
        System.out.println(Arrays.toString(dirs));
        StringBuilder sb = new StringBuilder("/");
        for (int i = 0; i < dirs.length - 1; i++)
        {
            sb.append(dirs[i]);
            sb.append("/");
        }
        return sb.toString();
    }

    /**
     * @param dir
     * @return
     */
    private String[] split(String dir)
    {
        StringTokenizer st = new StringTokenizer(dir, "/");
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens())
        {
            tokens.add(st.nextToken());
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    /**
     * @param dir
     * @return true if dir is not docroot
     */
    private boolean parentExists(String dir)
    {
        LOGGER.log(Level.FINE, "checking for parent of " + dir);
        return !"/".equals(dir);
    }
}
