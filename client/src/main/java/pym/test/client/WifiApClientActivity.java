package pym.test.client;

import java.util.List;

import pym.test.client.WifiApClientManager.AuthenticationType;
import pym.test.env.AppEnv;
import pym.test.http.HttpEngineLite;
import pym.test.http.HttpEngineLite.IHttpEngineLiteHandler;
import pym.test.httpc.NanoHTTPClient;
import pym.test.util.WifiApClientUtil;
import pym.test.wifi_ap_client.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author pengyiming
 * @description WifiAp客户端Activity
 * 
 */
public class WifiApClientActivity extends Activity implements View.OnClickListener, OnItemClickListener
{
    /* 数据段begin */
    private final String TAG = "WifiApClientActivity";

    private WifiManager mWifiManager;
    private WifiApClientManager mWifiApClientManager;
    // NanoHTTPClient
    private NanoHTTPClient mNanoHTTPClient;
    
    private WifiStateReceiver mWifiStateReceiver;
    private List<ScanResult> mScanResult;
    // 网关ip
    private String mGatewayIP;

    // 控件
    private Button mOpenWifiBtn;
    private Button mSendHttpRequestBtn;
    private Button mCloseWifiBtn;
    private ListView mApListView;

    // 对话框
    private AlertDialog mEnterPasswordDlg;

    /* 数据段end */

    /* 函数段begin */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wifi_ap_client_activity_layout);

        initView();
        initData();
        registerReceiver();
    }

    private void initView()
    {
        mOpenWifiBtn = (Button) findViewById(R.id.open_wifi);
        mOpenWifiBtn.setOnClickListener(this);
        mSendHttpRequestBtn = (Button) findViewById(R.id.send_http_request);
        mSendHttpRequestBtn.setOnClickListener(this);
        mCloseWifiBtn = (Button) findViewById(R.id.close_wifi);
        mCloseWifiBtn.setOnClickListener(this);
        mApListView = (ListView) findViewById(R.id.ap_list);
        mApListView.setOnItemClickListener(this);
    }

    private void initData()
    {
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiApClientManager = WifiApClientManager.getInstance(mWifiManager);
        mNanoHTTPClient = NanoHTTPClient.getInstance();
    }

    private void registerReceiver()
    {
        mWifiStateReceiver = new WifiStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWifiStateReceiver, intentFilter);
    }

    private void unregisterReceiver()
    {
        unregisterReceiver(mWifiStateReceiver);
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
        if (v.equals(mOpenWifiBtn))
        {
            mWifiApClientManager.setWifiEnabled(true);
            mWifiApClientManager.startScan();

            return;
        }
        
        if (v.equals(mSendHttpRequestBtn))
        {
//            mNanoHTTPClient.requestGetRecvList(mGatewayIP, "getRecvList", null);
            mNanoHTTPClient.requestGetFile(mGatewayIP, "getFile", new String[] {"path=/mnt/sdcard/index.html"});
            
            return;
        }

        if (v.equals(mCloseWifiBtn))
        {
            mWifiApClientManager.setWifiEnabled(false);

            return;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        ScanResult scanResult = mScanResult.get(position);
        String capabilities = scanResult.capabilities;
        String ssid = scanResult.SSID;

        AuthenticationType type = mWifiApClientManager.getWifiAuthenticationType(capabilities);
        switch (type)
        {
            case TYPE_NONE:
            {
                mWifiApClientManager.connect(mWifiApClientManager.generateWifiConfiguration(type, ssid, null));
                break;
            }
            case TYPE_WEP:
            {
                showEnterPasswordDlg(type, ssid);

                break;
            }
            case TYPE_WPA:
            {
                showEnterPasswordDlg(type, ssid);

                break;
            }
            case TYPE_WPA2:
            {
                showEnterPasswordDlg(type, ssid);

                break;
            }
            default:
            {
                assert (false);
                break;
            }
        }
    }

    private void showEnterPasswordDlg(final AuthenticationType type, final String ssid)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enter_password_dlg_title);
        View contentView = getLayoutInflater().inflate(R.layout.enter_password_dialog_layout, null);
        builder.setView(contentView);
        final EditText enterPassword = (EditText) contentView.findViewById(R.id.enter_password);
        final CheckBox showPassword = (CheckBox) contentView.findViewById(R.id.show_password);
        showPassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!showPassword.isChecked())
                {
                    enterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    Editable editable = enterPassword.getText();
                    Selection.setSelection(editable, editable.length());
                }
                else
                {
                    enterPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    Editable editable = enterPassword.getText();
                    Selection.setSelection(editable, editable.length());
                }
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String password = enterPassword.getText().toString();
                mWifiApClientManager.connect(mWifiApClientManager.generateWifiConfiguration(type, ssid, password));
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });
        mEnterPasswordDlg = builder.create();
        mEnterPasswordDlg.show();
    }

    /* 函数段end */

    /* 内部类begin */
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
                            Log.d(TAG, "wifi enabling");
                        }
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLED:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.d(TAG, "wifi enabled");
                        }
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
                        assert(false);
                        break;
                    }
                }
            }
            else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action))
            {
                if (AppEnv.bAppdebug)
                {
                    Log.d(TAG, "wifi scanned");
                }

                mScanResult = mWifiApClientManager.getScanResults();

                mApListView.setAdapter(new ApListViewAdapter());
            }
            else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action))
            {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                NetworkInfo.State state = networkInfo.getState();
                switch (state)
                {
                    case CONNECTED:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.d(TAG, "wifi connected");
                        }
                        
                        mGatewayIP = WifiApClientUtil.getGatewayIP(mWifiManager);
                        
                        break;
                    }
                    case CONNECTING:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.d(TAG, "wifi connecting");
                        }
                        break;
                    }
                    case DISCONNECTED:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.d(TAG, "wifi disconnected");
                        }
                        break;
                    }
                    case DISCONNECTING:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.d(TAG, "wifi disconnecting");
                        }
                        break;
                    }
                    case SUSPENDED:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.d(TAG, "wifi suspended");
                        }
                        break;
                    }
                    case UNKNOWN:
                    {
                        if (AppEnv.bAppdebug)
                        {
                            Log.d(TAG, "wifi unknown");
                        }
                        break;
                    }
                    default:
                    {
                        assert(false);
                        break;
                    }
                }
            }
            else
            {
                assert (false);
            }
        }
    }

    private class ApListViewAdapter extends BaseAdapter
    {
        private LayoutInflater mLayoutInflater;

        public ApListViewAdapter()
        {
            mLayoutInflater = LayoutInflater.from(WifiApClientActivity.this);
        }

        @Override
        public int getCount()
        {
            if (mScanResult == null)
            {
                return 0;
            }

            return mScanResult.size();
        }

        @Override
        public Object getItem(int position)
        {
            if (mScanResult == null)
            {
                return null;
            }

            return mScanResult.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            if (mScanResult == null)
            {
                return 0;
            }

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (mScanResult == null)
            {
                return null;
            }

            ViewHolder viewHolder;
            if (convertView == null)
            {
                viewHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.wifi_ap_list_item_layout, null);
                viewHolder.ssid = (TextView) convertView.findViewById(R.id.ap_ssid);
                viewHolder.bssid = (TextView) convertView.findViewById(R.id.ap_bssid);
                viewHolder.capabilities = (TextView) convertView.findViewById(R.id.ap_capabilities);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ScanResult scanResult = mScanResult.get(position);
            viewHolder.ssid.setText(scanResult.SSID);
            viewHolder.bssid.setText(scanResult.BSSID);
            viewHolder.capabilities.setText(scanResult.capabilities);

            return convertView;
        }

        private class ViewHolder
        {
            TextView ssid;
            TextView bssid;
            TextView capabilities;
        }
    }
    /* 内部类end */
}
