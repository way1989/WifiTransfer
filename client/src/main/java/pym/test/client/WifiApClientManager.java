package pym.test.client;

import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

/**
 * 
 * @author pengyiming
 * @description Wifi客户端Ap管理者，实质上是WifiManager的代理
 * 
 */
public class WifiApClientManager
{
    /* 数据段begin */
    private final static String TAG = "WifiApClientManager";
    // 单例
    private static WifiApClientManager mWifiApClientManager;
    // WifiManager引用
    private WifiManager mWifiManager;

    // 认证加密类型
    public enum AuthenticationType
    {
        TYPE_NONE, TYPE_WEP, TYPE_WPA, TYPE_WPA2
    }

    /* 数据段end */

    /* 函数段begin */
    private WifiApClientManager(WifiManager wifiManager)
    {
        mWifiManager = wifiManager;
    }

    public synchronized static WifiApClientManager getInstance(WifiManager wifiManager)
    {
        if (mWifiApClientManager == null)
        {
            mWifiApClientManager = new WifiApClientManager(wifiManager);
        }

        return mWifiApClientManager;
    }

    public boolean setWifiEnabled(boolean enabled)
    {
        return mWifiManager.setWifiEnabled(enabled);
    }

    public boolean startScan()
    {
        return mWifiManager.startScan();
    }

    public List<ScanResult> getScanResults()
    {
        return mWifiManager.getScanResults();
    }

    public AuthenticationType getWifiAuthenticationType(String capabilities)
    {
        if (capabilities.indexOf("WPA2") != -1)
        {
            return AuthenticationType.TYPE_WPA2;
        }

        if (capabilities.indexOf("WPA") != -1)
        {
            return AuthenticationType.TYPE_WPA;
        }

        if (capabilities.indexOf("WEP") != -1)
        {
            return AuthenticationType.TYPE_WEP;
        }

        return AuthenticationType.TYPE_NONE;
    }

    public WifiConfiguration generateWifiConfiguration(AuthenticationType type, String ssid, String password)
    {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = String.format("\"%s\"", ssid);
        switch (type)
        {
            case TYPE_NONE:
            {
                config.wepKeys[0] = "\"\"";
                config.wepTxKeyIndex = 0;

                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                break;
            }
            case TYPE_WEP:
            {
                config.wepKeys[0] = String.format("\"%s\"", password);
                config.wepTxKeyIndex = 0;

                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                break;
            }
            case TYPE_WPA:
            {
                config.preSharedKey = String.format("\"%s\"", password);

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
                config.preSharedKey = String.format("\"%s\"", password);

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

    public boolean connect(WifiConfiguration config)
    {
        if (mWifiManager == null || config == null)
        {
            return false;
        }

        int networkID = mWifiManager.addNetwork(config);
        if (networkID == -1)
        {
            return false;
        }

        return mWifiManager.enableNetwork(networkID, true);
    }
    /* 函数段end */
}
