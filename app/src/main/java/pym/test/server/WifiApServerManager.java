package pym.test.server;

import java.lang.reflect.Method;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * 
 * @author pengyiming
 * @description Wifi服务端Ap管理者，实质上是WifiManager的代理
 * 
 */
public class WifiApServerManager
{
    /* 数据段begin */
    private final static String TAG = "WifiApServerManager";
    // 单例
    private static WifiApServerManager mWifiApServerManager;
    // WifiManager引用
    private WifiManager mWifiManager;

    // Wifi AP广播action（本应该用反射获取，但为了减少不必要的代码，在这里定义，并与源码保持一致）
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";

    // 认证加密类型
    public enum AuthenticationType
    {
        TYPE_NONE, TYPE_WPA, TYPE_WPA2
    }

    /* 数据段end */

    /* 函数段begin */
    private WifiApServerManager(WifiManager wifiManager)
    {
        mWifiManager = wifiManager;
    }

    public synchronized static WifiApServerManager getInstance(WifiManager wifiManager)
    {
        if (mWifiApServerManager == null)
        {
            mWifiApServerManager = new WifiApServerManager(wifiManager);
        }

        return mWifiApServerManager;
    }

    public WifiConfiguration getWifiApConfiguration()
    {
        WifiConfiguration config = null;
        try
        {
            Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            config = (WifiConfiguration) method.invoke(mWifiManager, (Object[]) null);
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }

        return config;
    }

    public boolean setWifiApConfiguration(WifiConfiguration config)
    {
        boolean ret = false;
        try
        {
            Method method = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            ret = (Boolean) method.invoke(mWifiManager, config);
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }

        return ret;
    }

    public boolean isWifiApEnabled()
    {
        boolean ret = false;
        try
        {
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            ret = (Boolean) method.invoke(mWifiManager, (Object[]) null);
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }

        return ret;
    }

    public boolean setWifiApEnabled(WifiConfiguration config, boolean enabled)
    {
        boolean ret = false;
        try
        {
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            ret = (Boolean) method.invoke(mWifiManager, config, enabled);
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }

        return ret;
    }

    public WifiConfiguration generateWifiConfiguration(AuthenticationType type, String ssid, String MAC, String password)
    {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = ssid;
        config.BSSID = MAC;
        Log.d(TAG, "MAC = " + config.BSSID);
        switch (type)
        {
            case TYPE_NONE:
            {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                break;
            }
            case TYPE_WPA:
            {
                config.preSharedKey = password;

                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

                break;
            }
            case TYPE_WPA2:
            {
                config.preSharedKey = password;

                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

                break;
            }
            default:
            {
                break;
            }
        }

        return config;
    }
    /* 函数段end */
}
