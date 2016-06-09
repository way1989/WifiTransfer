package pym.test.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiApClientUtil
{
    /* 数据段begin */
    private static final String TAG = "WifiApClientUtil";

    public static final String DEFAULT_GATEWAY_IP = "192.168.43.1";

    /* 数据段end */

    /* 函数段begin */
    public static String getLocalIP()
    {
        try
        {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            if (networkInterfaces == null)
            {
                return "";
            }

            while (networkInterfaces.hasMoreElements())
            {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements())
                {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && (address instanceof Inet4Address))
                    {
                        return address.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException e)
        {
            Log.e(TAG, "", e);
        }

        return "";
    }

    public static String getGatewayIP(WifiManager wifiManager)
    {
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        return convertIPv4IntToStr(dhcpInfo.serverAddress);
    }

    private static String convertIPv4IntToStr(int ip)
    {
        if (ip <= 0)
        {
            return DEFAULT_GATEWAY_IP;
        }
        
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }
    /* 函数段end */
}
