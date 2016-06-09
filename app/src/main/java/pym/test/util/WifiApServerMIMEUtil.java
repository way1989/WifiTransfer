package pym.test.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author pengyiming
 * @description MIME类型工具类
 * 
 */
public class WifiApServerMIMEUtil
{
    /* 数据段begin */
    private static final String TAG = "WifiApServerMIMEUtil";

    /**
     * Common MIME type for dynamic content: binary
     */
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";

    /**
     * HashMap mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>()
    {
        {
            put("css", "text/css");
            put("htm", "text/html");
            put("html", "text/html");
            put("xml", "text/xml");
            put("java", "text/x-java-source, text/java");
            put("md", "text/plain");
            put("txt", "text/plain");
            put("asc", "text/plain");
            put("gif", "image/gif");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
            put("png", "image/png");
            put("mp3", "audio/mpeg");
            put("m3u", "audio/mpeg-url");
            put("mp4", "video/mp4");
            put("ogv", "video/ogg");
            put("flv", "video/x-flv");
            put("mov", "video/quicktime");
            put("swf", "application/x-shockwave-flash");
            put("js", "application/javascript");
            put("pdf", "application/pdf");
            put("doc", "application/msword");
            put("ogg", "application/x-ogg");
            put("zip", "application/octet-stream");
            put("exe", "application/octet-stream");
            put("class", "application/octet-stream");
        }
    };

    // Get MIME type from file name extension, if possible
    public static String getMimeTypeForFile(String file)
    {
        int dot = file.lastIndexOf('.');
        String mime = null;
        if (dot >= 0)
        {
            mime = MIME_TYPES.get(file.substring(dot + 1).toLowerCase());
        }
        return mime == null ? MIME_DEFAULT_BINARY : mime;
    }
    /* 数据段end */

    /* 函数段begin */
    /* 函数段end */
}
