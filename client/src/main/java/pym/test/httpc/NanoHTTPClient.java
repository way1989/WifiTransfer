package pym.test.httpc;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 
 * @author pengyiming
 * @description 遵循Http协议的客户端，Http请求的管理者，可以并发执行多个Http请求
 * 
 */
public class NanoHTTPClient
{
    /* 数据段begin */
    private final String TAG = "NanoHTTPClient";

    // 单例
    private static NanoHTTPClient mNanoHTTPClient;

    // 线程池
    private Executor mExecutor;

    /* 数据段end */

    /* 函数段begin */
    private NanoHTTPClient()
    {
        mExecutor = Executors.newSingleThreadExecutor();
    }

    public synchronized static NanoHTTPClient getInstance()
    {
        if (mNanoHTTPClient == null)
        {
            mNanoHTTPClient = new NanoHTTPClient();
        }

        return mNanoHTTPClient;
    }
    
    /**
     * @function 获取服务端发送列表
     * @param ip
     * @param function
     * @param param
     */
    public void requestGetRecvList(String ip, String function, String[] param)
    {
        mExecutor.execute(new WifiApClientGetRecvListRequest(WifiApClientRequest.generateURL(ip, function, param)));
    }
    
    /**
     * @function 获取文件
     * @param ip
     * @param function
     * @param param
     */
    public void requestGetFile(String ip, String function, String[] param)
    {
        mExecutor.execute(new WifiApClientGetFileRequest(WifiApClientRequest.generateURL(ip, function, param)));
    }
    
    /**
     * @function 获取通讯录
     * @param ip
     * @param function
     * @param param
     */
    public void requestGetContacts(String ip, String function, String[] param)
    {
    }
    /* 函数段end */
}
