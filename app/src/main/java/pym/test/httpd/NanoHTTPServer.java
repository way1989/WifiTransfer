package pym.test.httpd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import pym.test.env.AppEnv;
import pym.test.util.WifiApServerMIMEUtil;
import android.util.Log;

/**
 * 
 * @author pengyiming
 * @description 遵循Http协议的服务器，NanoHTTPD实现类
 * 
 */
public class NanoHTTPServer extends NanoHTTPD
{
    /* 数据段begin */
    private final String TAG = "NanoHTTPServer";

    // 默认port
    private static final int PORT = 36063;

    // 单例
    private static NanoHTTPServer mNanoHTTPServer;
    
    // 运行标志
    private boolean mIsRunning = false;

    /* 数据段end */

    /* 函数段begin */
    private NanoHTTPServer(int port)
    {
        super(port);
    }

    public synchronized static NanoHTTPServer getInstance()
    {
        if (mNanoHTTPServer == null)
        {
            mNanoHTTPServer = new NanoHTTPServer(PORT);
        }

        return mNanoHTTPServer;
    }
    
    public synchronized void start()
    {
        if (mIsRunning)
        {
            return;
        }
        else
        {
            mIsRunning = true;
        }
        
        try
        {
            super.start();
        }
        catch (IOException e)
        {
            Log.e(TAG, "", e);
        }
    }
    
    public synchronized void stop()
    {
        if (mIsRunning)
        {
            super.stop();
            
            mIsRunning = false;
        }
    }
    
    @Override
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files)
    {
        if (AppEnv.bAppdebug)
        {
            Log.d(TAG, "uri = " + uri);
            Log.d(TAG, "method = " + method);
            Log.d(TAG, "headers" + headers);
            Log.d(TAG, "parms" + parms);
            Log.d(TAG, "files" + files);
        }
        
        // 根据function生成不同的响应
        if ("/getRecvList".equals(uri))
        {
            return serverRecvList();
        }
        else if ("/getFile".equals(uri))
        {
            return serverFile(headers, parms);
        }
        
        return null;
    }
    
    private Response serverRecvList()
    {
        return new Response("getRecvList");
    }
    
    private Response serverFile(Map<String, String> headers, Map<String, String> parms)
    {
        // 判断是否需要断点续传
        long resumeFrom = 0;
        String range = headers.get("range");
        if (range != null)
        {
            if (range.startsWith("bytes="))
            {
                range = range.substring("bytes=".length());
                int minusPosition = range.indexOf('-');
                try
                {
                    if (minusPosition > 0)
                    {
                        resumeFrom = Long.parseLong(range.substring(0, minusPosition));
                    }
                }
                catch (NumberFormatException ignored)
                {
                }
            }
        }
        
        // 获取文件路径
        String path = parms.get("path");
        if (path == null)
        {
            return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "");
        }
        
        File file = new File(path);
        if (file.isDirectory() || !file.exists())
        {
            return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "");
        }

        FileInputStream fileInputStream = null;
        try
        {
            fileInputStream = new FileInputStream(file);
            fileInputStream.skip(resumeFrom);
        }
        catch (FileNotFoundException e)
        {
            return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "");
        }
        catch (IOException e)
        {
            return new Response(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
        }
        String mimeType = WifiApServerMIMEUtil.getMimeTypeForFile(path);
        long length = file.length();
        // 正常流程
        if (resumeFrom == 0)
        {
            Response response = new Response(Response.Status.OK, mimeType, fileInputStream);
            response.addHeader("Content-Length", String.valueOf(length));
            
            return response;
        }
        // 断点续传
        else if (resumeFrom > 0 && resumeFrom < length)
        {
            Response response = new Response(Response.Status.PARTIAL_CONTENT, mimeType, fileInputStream);
            response.addHeader("Content-Length", String.valueOf(length - resumeFrom));
            
            return response;
        }
        // resumeFrom < 0 || resumeFrom >= length
        else
        {
            return new Response(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
        }
    }
    /* 函数段end */
}
