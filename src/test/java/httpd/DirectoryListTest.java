package httpd;

import org.junit.Test;

public class DirectoryListTest
{
    @Test
    public void simple()
    {
        String s = new DirectoryList().list("/foo/bar");
        System.out.println(s);
    }
}
