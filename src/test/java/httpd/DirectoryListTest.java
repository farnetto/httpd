package httpd;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class DirectoryListTest
{
    @Test
    public void simple()
    {
        String fName = "target";
        File dir = new File(fName);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        String s = new DirectoryList().list(new File("."), fName);
        assertTrue("unexpected content: " + s, s.contains("<html>"));
    }
}
