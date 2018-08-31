package httpd;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class DirectoryListTest
{
    @Test
    public void simple()
    {
        File dir = new File("target");
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        String s = new DirectoryList().list(dir);
        assertTrue("unexpected content: " + s, s.contains("<html>"));
    }
}
