package pym.test.httpc;

import java.io.OutputStream;
import java.net.URLEncoder;

import pym.test.env.AppEnv;
import android.util.Log;

/**
 * 
 * @author pengyiming
 * @description WifiAp客户端请求抽象类
 *
 */
public abstract class WifiApClientRequest
    implements Runnable
{
    /* 数据段begin */
    private static final String TAG = "WifiApClientRequest";
    
    // 协议
    private static final String PROTOCOL = "http://";
    
    // 网关默认port
    private static final int PORT = 36063;
    
    // 服务端URL
    protected String mUrl;
    // 服务端返回输出流
    protected OutputStream mResponseOutputStream;
    
    // 回调对象
    protected IRequestHandler mRequestHandler = null;
    
    // 默认存储目录
    protected final String RECV_DIR = "/data/data/pym.test.wifi_ap_client/recv/";
    /* 数据段end */
    
    /* 函数段begin */
    /**
     * @function 设置回调对象
     * @param requestHandler
     */
    public void setCallback(IRequestHandler requestHandler)
    {
        mRequestHandler = requestHandler;
    }
    
    public static String generateURL(String ip, String function, String[] param)
    {
        String url;
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(PROTOCOL);
        urlBuilder.append(ip);
        urlBuilder.append(':');
        urlBuilder.append(PORT);
        
        if (function != null)
        {
            urlBuilder.append('/');
            urlBuilder.append(function);
        }
 
        if (param != null)
        {
            urlBuilder.append('?');
            for (int loopVal = 0; loopVal < param.length; loopVal++)
            {
                urlBuilder.append(param[loopVal]);
                if (loopVal < param.length - 1)
                {
                    urlBuilder.append('&');
                }
            }
        }
 
        url = urlBuilder.toString();
        if (AppEnv.bAppdebug)
        {
            Log.d(TAG, "url = " + url);
        }
        
        return url;
    }
    /* 函数段end */
    
    /* 内部接口begin */
    public interface IRequestHandler
    {
        public void onStart();
        public void onProgress();
        public void onStop();
        public void onOk();
        public void onError();
    }
    /* 内部接口end */
}
