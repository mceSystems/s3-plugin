package hudson.plugins.s3;

import org.junit.Test;

import java.net.URLDecoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class FingerprintRecordTest {

    @Test
    public void testGetLinkFromWindowsPath() throws Exception {
        String windowsPath = "path\\to\\windows\\test.txt";
        FingerprintRecord windowsRecord = new FingerprintRecord(true, "test", windowsPath, "us-eat-1", "xxxx", null, null, null);
        String link = windowsRecord.getLink();
        String linkDecoded = URLDecoder.decode(link, "utf-8");
        assertNotEquals("link is encoded", windowsPath, link);
        assertEquals("should match file name", "download/" + windowsPath, linkDecoded);
    }

    @Test
    public void testGetLinkFromUnixPath() throws Exception {
        String unixPath = "path/tmp/abc";
        FingerprintRecord unixRecord = new FingerprintRecord(true, "test", unixPath, "us-eat-1", "xxxx", null, null, null);
        assertEquals("should match file name", "download/" + unixPath, unixRecord.getLink());
    }
}
