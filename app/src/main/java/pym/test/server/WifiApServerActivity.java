package pym.test.server;

import pym.test.env.AppEnv;
import pym.test.httpd.NanoHTTPServer;
import pym.test.wifi_ap_server.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * 
 * @author pengyiming
 * @description WifiAp服务端Activity
 * 
 */
public class WifiApServerActivity extends Activity implements View.OnClickListener
{
    /* 数据段begin */
    private final String TAG = "WifiApServerActivity";

    private WifiManager mWifiManager;
    private WifiApServerManager mWifiApServerManager;
    // NanoHTTPServer
    private NanoHTTPServer mNanoHTTPServer; 

    private WifiStateReceiver mWifiStateReceiver;
    private WifiApStateReceiver mWifiApStateReceiver;

    // 服务端MAC（本机）
    private String mMAC;

    // 控件
    private EditText mSetPasswordEt;
    private Button mOpenWifiApBtn;
    private Button mCloseWifiApBtn;
    private ListView mClientListView;

    /* 数据段end */

    /* 函数段begin */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_ap_server_activity_layout);

        initView();
        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !Settings.System.canWrite(this)){
            requestWriteSettings();
        }else {
            initData();
            registerReceiver();
        }
    }
    private static final int REQUEST_CODE_WRITE_SETTINGS = 2;
    private void requestWriteSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS );
    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (Settings.System.canWrite(this)) {
                Log.i(TAG, "onActivityResult write settings granted" );
                initData();
                registerReceiver();
            }
        }
    }
    private void initView()
    {
        mSetPasswordEt = (EditText) findViewById(R.id.set_password);
        mOpenWifiApBtn = (Button) findViewById(R.id.open_wifi_ap);
        mOpenWifiApBtn.setOnClickListener(this);
        mCloseWifiApBtn = (Button) findViewById(R.id.close_wifi_ap);
        mCloseWifiApBtn.setOnClickListener(this);
        mClientListView = (ListView) findViewById(R.id.client_list);
    }

    private void initData()
    {
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiApServerManager = WifiApServerManager.getInstance(mWifiManager);
        mNanoHTTPServer = NanoHTTPServer.getInstance();

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getMacAddress() == null)
        {
            // 打开Wifi开关以便Wifi上报MAC
            mWifiManager.setWifiEnabled(true);
        }
        else
        {
            mMAC = wifiInfo.getMacAddress();
        }
    }

    private void registerReceiver()
    {
        mWifiStateReceiver = new WifiStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiStateReceiver, intentFilter);

        mWifiApStateReceiver = new WifiApStateReceiver();
        IntentFilter intentFilterForAp = new IntentFilter();
        intentFilterForAp.addAction(WifiApServerManager.WIFI_AP_STATE_CHANGED_ACTION);
        registerReceiver(mWifiApStateReceiver, intentFilterForAp);
    }

    private void unregisterReceiver()
    {
        unregisterReceiver(mWifiStateReceiver);
        unregisterReceiver(mWifiApStateReceiver);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        unregisterReceiver();
    }

    @Override
    public void onClick(View v)
    {
        if (v.equals(mOpenWifiApBtn))
        {
            if (mMAC == null)
            {
                return;
            }

            if (mWifiApServerManager.isWifiApEnabled())
            {
                return;
            }

            String password = mSetPasswordEt.getText().toString();
            if (TextUtils.isEmpty(password))
            {
                mWifiApServerManager.setWifiApEnabled(mWifiApServerManager.generateWifiConfiguration(
                                WifiApServerManager.AuthenticationType.TYPE_NONE, "pengyiming", mMAC, null), true);
            }
            else
            {
                mWifiApServerManager.setWifiApEnabled(mWifiApServerManager.generateWifiConfiguration(
                                WifiApServerManager.AuthenticationType.TYPE_WPA, "pengyiming", mMAC, password), true);
            }

            return;
        }

        if (v.equals(mCloseWifiApBtn))
        {
            mWifiApServerManager.setWifiApEnabled(null, false);

            return;
        }
    }

    /* 函数段end */

    /* 内部类begin */
    private class WifiApStateReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int state = intent.getIntExtra(WifiApServerManager.EXTRA_WIFI_AP_STATE, -1);
            // 判断Wifi AP状态（由于在不同的API level中，值的定义不一致，反射获取较麻烦，此处直接用magic number判断）
            switch (state)
            {
                case 0:
                case 10:
                {
                    if (AppEnv.bAppdebug)
                    {
                        Log.d(TAG, "wifi ap disabling");
                    }
                    break;
                }
                case 1:
                case 11:
                {
                    if (AppEnv.bAppdebug)
                    {
                        Log.d(TAG, "wifi ap disabled");
                    }
                    
                    // 关闭NanoHTTPServer
                    mNanoHTTPServer.stop();
                    
                    break;
                }
                case 2:
                case 12:
                {
                    if (AppEnv.bAppdebug)
                    {
                        Log.d(TAG, "wifi ap enabling");
                    }
                    break;
                }
                case 3:
                case 13:
                {
                    if (AppEnv.bAppdebug)
                    {
                        Log.d(TAG, "wifi ap enabled");
                    }
                    
                    // 开启NanoHTTPServer
                    mNanoHTTPServer.start();
                    
                    break;
                }
                case 4:
                case 14:
                {
                    if (AppEnv.bAppdebug)
                    {
                        Log.d(TAG, "wifi ap failed");
                    }
                    break;
                }
                default:
                {
                    if (AppEnv.bAppdebug)
                    {
                        Log.e(TAG, "wifi ap state = " + state);
                    }
                    break;
                }
            }
        }
    }

    private class WifiStateReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action))
            {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                switch (state)
                {
                    case WifiManager.WIFI_STATE_DISABLING:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.d(TAG, "wifi disabling");
                        }
                        break;
                    }
                    case WifiManager.WIFI_STATE_DISABLED:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.d(TAG, "wifi disabled");
                        }
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLING:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.d(TAG, "venabling");
                        }
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLED:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.d(TAG, "wifi enabled");
                        }

                        mMAC = mWifiManager.getConnectionInfo().getMacAddress();
                        // Wifi已上报MAC，关闭Wifi开关
                        mWifiManager.setWifiEnabled(false);

                        break;
                    }
                    case WifiManager.WIFI_STATE_UNKNOWN:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.d(TAG, "wifi unknown");
                        }
                        break;
                    }
                    default:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.e(TAG, "wifi state = " + state);
                        }
                        break;
                    }
                }
            }
        }
    }
    /* 内部类end */
}
