package pym.test.httpc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import pym.test.http.HttpEngineLite;
import pym.test.http.HttpEngineLite.IHttpEngineLiteHandler;
import android.util.Log;

/**
 * 
 * @author pengyiming
 * @description WifiAp客户端-请求服务端传输列表
 *
 */
public class WifiApClientGetFileRequest extends WifiApClientRequest
{
    /* 数据段begin */
    private final String TAG = "WifiApClientGetFileRequest";
    
    /* 数据段end */
    
    /* 函数段begin */
    public WifiApClientGetFileRequest(String url)
    {
        mUrl = url;
        
        File recvFile = new File(RECV_DIR + "temp");
        File dir = recvFile.getParentFile();
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        
        try
        {
            mResponseOutputStream = new FileOutputStream(recvFile, true);
        }
        catch (FileNotFoundException e)
        {
        }
    }
    
    @Override
    public void run()
    {
        HttpEngineLite httpEngineLite = new HttpEngineLite(mUrl);
        httpEngineLite.setCallback(new IHttpEngineLiteHandler()
        {
            @Override
            public void onStart()
            {
            }

            @Override
            public void onProgress(int progress, long currentSize, long speed)
            {
            }

            @Override
            public void onStop()
            {
            }

            @Override
            public void onOk()
            {
                try
                {
                    if (mResponseOutputStream != null)
                    {
                        mResponseOutputStream.flush();
                        mResponseOutputStream.close();
                    }
                }
                catch (IOException e)
                {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onError(int errorCode)
            {
            }
        });
        httpEngineLite.get(mResponseOutputStream);
    }
    /* 函数段end */
}
