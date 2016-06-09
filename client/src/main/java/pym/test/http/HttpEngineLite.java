package pym.test.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.http.message.BasicNameValuePair;

import pym.test.env.AppEnv;
import android.content.Context;
import android.util.Log;

/**
 * @author pengyiming
 * @date 2013-7-17
 * @function 基于HttpURLConnection的封装类————HttpEngineLite，相对于基于HttpClient的封装类————HttpEngine而言，有以下优缺点
 * @note 优点：1，高效，使用GZIP压缩，比HttpClient传输速度快数倍 2， 每个实例为一条连接，避免HttpClient中连接池的开销 3，支持停止下载、断点续传操作
 * @note 去掉APN相关支持
 */
public class HttpEngineLite
{
    /* 数据段begin */
    private static final String TAG = "HttpEngineLite";
    private Context mContext;

    // 错误码
    private static final int RESULT_CONNECT_ERR = 1;
    private static final int RESULT_RESPONSE_ERR = 2;
    
    // 连接服务器超时5s
    private final int HTTP_ENGINE_LITE_CONNECT_TIMEOUT = 5000;
    // 读取response超时5s
    private final int HTTP_ENGINE_LITE_READ_TIMEOUT = 5000;
    // URL
    private URL mUrl = null;
    // HttpURLConnection对象
    private HttpURLConnection mHttpURLConnection = null;
    
    // 下载数据大小（字节）
    private long mDownloadSize = 0;
    // 当前下载数据大小（字节）
    private long mCurrentDownloadSize = 0;
    
    // 回调对象
    private IHttpEngineLiteHandler mHttpEngineLiteHandler = null;
    
    // 停止标记
    private boolean mIsStop = false;
    /* 数据段end */
    
    /* 函数段begin */
    /**
     * @function 生成一个HttpEngineLite实例，对应一条连接
     * @param url-不含参数的url
     * @note 不支持APN纠错
     */
    public HttpEngineLite(String url)
    {
        if (AppEnv.bAppdebug)
        {
            Log.d(TAG, "url = " + url);
        }
        
        try
        {
            mUrl = new URL(url);
            mHttpURLConnection = (HttpURLConnection) mUrl.openConnection();
            mHttpURLConnection.setConnectTimeout(HTTP_ENGINE_LITE_CONNECT_TIMEOUT);
            mHttpURLConnection.setReadTimeout(HTTP_ENGINE_LITE_READ_TIMEOUT);
        }
        catch (MalformedURLException e)
        {
            Log.e(TAG, "", e);
        }
        catch (IOException e)
        {
            Log.e(TAG, "", e);
        }
    }
    
    /**
     * @function 设置回调对象
     * @param IHttpEngineLiteHandler-回调对象
     */
    public void setCallback(IHttpEngineLiteHandler httpEngineLiteHandler)
    {
        mHttpEngineLiteHandler = httpEngineLiteHandler;
    }
    
    /**
     * @function get请求
     * @param response-响应报文的数据
     */
    public void get(OutputStream response)
    {
        get(response, 0);
    }
    
    /**
     * @function get请求（支持断点续传）
     * @param response-响应报文的数据
     * @param resumeFrom-已下载字节，即续传起始字节
     */
    public void get(OutputStream response, long resumeFrom)
    {
        if (mHttpURLConnection == null)
        {
            if (mHttpEngineLiteHandler != null)
            {
                mHttpEngineLiteHandler.onError(RESULT_RESPONSE_ERR);
            }
            
            return;
        }
        
        try
        {
            mHttpURLConnection.setRequestMethod("GET");
            if (resumeFrom > 0)
            {
                mHttpURLConnection.setRequestProperty("RANGE", "bytes=" + resumeFrom + "-");
                
                mCurrentDownloadSize = resumeFrom;
            }
        }
        catch (ProtocolException e)
        {
            Log.e(TAG, "", e);
        }
        
        sendRequest(response);
    }
    
    /**
     * @function post请求
     * @param BasicNameValuePair-键值对形式的参数
     * @param response-响应报文的数据
     */
    public void post(OutputStream response, BasicNameValuePair... params)
    {
        post(response, 0, params);
    }
    
    /**
     * @function post请求（支持断点续传）
     * @param response-响应报文的数据
     * @param resumeFrom-已下载字节，即续传起始字节
     */
    public void post(OutputStream response, long resumeFrom, BasicNameValuePair... params)
    {
        if (mHttpURLConnection == null)
        {
            if (mHttpEngineLiteHandler != null)
            {
                mHttpEngineLiteHandler.onError(RESULT_RESPONSE_ERR);
            }
            
            return;
        }
        
        OutputStream outputStream = null;
        
        try
        {
            mHttpURLConnection.setRequestMethod("POST");
            if (resumeFrom > 0)
            {
                mHttpURLConnection.setRequestProperty("RANGE", "bytes=" + resumeFrom + "-");
                
                mCurrentDownloadSize = resumeFrom;
            }
            
            mHttpURLConnection.setDoInput(true);
            // 数据写入实体
            outputStream = mHttpURLConnection.getOutputStream();
            for (int loopVal = 0; loopVal < params.length; loopVal++)
            {
                outputStream.write(params.toString().getBytes());
            }
            outputStream.flush();
        }
        catch (ProtocolException e)
        {
            Log.e(TAG, "", e);
        }
        catch (IOException e)
        {
            Log.e(TAG, "", e);
        }
        finally
        {
            try
            {
                if (outputStream != null)
                {
                    outputStream.close();
                }
            }
            catch (IOException e)
            {
                Log.e(TAG, "", e);
            }
        }
        
        sendRequest(response);
    }
    
    /**
     * @function 发送请求，并读取response
     * @param response-响应报文的数据
     */
    private void sendRequest(OutputStream response)
    {
        if (mHttpEngineLiteHandler != null)
        {
            mHttpEngineLiteHandler.onStart();
        }
        
        try
        {
            // 建立连接
            mHttpURLConnection.connect();
            
            // 获取响应码
            int responseCode = mHttpURLConnection.getResponseCode();
            if (AppEnv.bAppdebug)
            {
                Log.d(TAG, "responseCode = " + responseCode);
            }
            // [200, 300)区间内正常，否则连接失败
            if (responseCode < HttpURLConnection.HTTP_OK
                    || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE)
            {
                Log.e(TAG, "connect failed!");
                
                if (mHttpEngineLiteHandler != null)
                {
                    mHttpEngineLiteHandler.onError(RESULT_CONNECT_ERR);
                }
                
                return;
            }
            
            // 记录总数据大小 = 已下载的字节数 + 还需续传字节数
            mDownloadSize = mCurrentDownloadSize + mHttpURLConnection.getContentLength();
            if (AppEnv.bAppdebug)
            {
                Log.d(TAG, "total size = " + mDownloadSize);
            }
        }
        catch (IOException e)
        {
            Log.e(TAG, "", e);
            
            if (mHttpEngineLiteHandler != null)
            {
                mHttpEngineLiteHandler.onError(RESULT_CONNECT_ERR);
            }
            
            return;
        }
        
        // check-point是否停止下载
        if (mIsStop)
        {
            return;
        }
        
        try
        {
            // 发送请求并获取response输入流
            InputStream inputStream = mHttpURLConnection.getInputStream();
            // check-point是否停止下载
            if (mIsStop)
            {
                return;
            }
            
            // 重定向到输出流
            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            long lastCallbackTime = System.currentTimeMillis();
            long nowTime;
            long downloadSizePerSecond = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1)
            {
                // check-point是否停止下载
                if (mIsStop)
                {
                    return;
                }
                
                response.write(buffer, 0, bytesRead);
                
                // 记录当前下载数据大小
                mCurrentDownloadSize += bytesRead;
                
                // onProgress每1s返回一次进度
                nowTime = System.currentTimeMillis();
                // 每1s计算一次速度
                downloadSizePerSecond += bytesRead;
                if (mHttpEngineLiteHandler != null
                    && (nowTime - lastCallbackTime) >= 1000)
                {
                    mHttpEngineLiteHandler.onProgress((int) (mCurrentDownloadSize * 100.0f / mDownloadSize), mCurrentDownloadSize, downloadSizePerSecond);
                    if (AppEnv.bAppdebug)
                    {
                        Log.d(TAG, "current progress = " + (int) (mCurrentDownloadSize * 100.0f / mDownloadSize));
                    }
                    
                    lastCallbackTime = nowTime;
                    
                    downloadSizePerSecond = 0;
                }
                
                // check-point是否停止下载
                if (mIsStop)
                {
                    return;
                }
            }
            
            // 由于每1s返回一次进度，有可能在这1s中已经下载完毕，而未返回onProgress(100)，在onOk()之前先返回onProgress(100)
            if (mHttpEngineLiteHandler != null)
            {
                mHttpEngineLiteHandler.onProgress(100, mDownloadSize, 0);
                if (AppEnv.bAppdebug)
                {
                    Log.d(TAG, "current progress = 100");
                }
            }
        }
        catch (IOException e)
        {
            Log.e(TAG, "", e);
            
            if (mHttpEngineLiteHandler != null)
            {
                mHttpEngineLiteHandler.onError(RESULT_RESPONSE_ERR);
            }
            
            return;
        }
        finally
        {
            mHttpURLConnection.disconnect();
        }
        
        if (mHttpEngineLiteHandler != null)
        {
            mHttpEngineLiteHandler.onOk();
        }
    }
    
    /**
     * @function 停止下载
     */
    public void stop()
    {
        mIsStop = true;
        
        if (mHttpEngineLiteHandler != null)
        {
            mHttpEngineLiteHandler.onStop();
        }
    }
    /* 函数段end */
    
    /* 内部接口begin */
    public interface IHttpEngineLiteHandler
    {
        public void onStart();
        public void onProgress(int progress, long currentSize, long speed);
        public void onStop();
        public void onOk();
        public void onError(int errorCode);
    }
    /* 内部接口end */
}
