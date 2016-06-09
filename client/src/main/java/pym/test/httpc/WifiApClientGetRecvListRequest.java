package pym.test.httpc;

import java.io.ByteArrayOutputStream;

import android.util.Log;

import pym.test.env.AppEnv;
import pym.test.http.HttpEngineLite;
import pym.test.http.HttpEngineLite.IHttpEngineLiteHandler;

/**
 * 
 * @author pengyiming
 * @description WifiAp客户端-请求文件
 *
 */
public class WifiApClientGetRecvListRequest extends WifiApClientRequest
{
    /* 数据段begin */
    private final String TAG = "WifiApClientGetRecvListRequest";
    
    /* 数据段end */
    
    /* 函数段begin */
    public WifiApClientGetRecvListRequest(String url)
    {
        mUrl = url;
        mResponseOutputStream = new ByteArrayOutputStream();
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
                if (AppEnv.bAppdebug)
                {
                    Log.d(TAG, "response = " + mResponseOutputStream.toString());
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
