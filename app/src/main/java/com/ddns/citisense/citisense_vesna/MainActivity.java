package com.ddns.citisense.citisense_vesna;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();
    ListView wifiListView;
    List<String> refinedWifiList = new ArrayList<String>();
    ArrayAdapter<String> wifiListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiListView = (ListView) findViewById(R.id.vesna_wifi_list);
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiListAdapter = new ArrayAdapter<String>(
                this,
                R.layout.list_element,
                R.id.text,
                refinedWifiList
        );
        wifiListView.setAdapter(wifiListAdapter);
        mainWifi.startScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    public void wifi_element_clicked(View view) {
        view.setBackgroundColor(0xffaaaaaa);
        String ssid = ((TextView)((ViewGroup)view).getChildAt(1)).getText().toString();
        new ConnectAndCheck(ssid, this).execute();

    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            wifiList = mainWifi.getScanResults();
            refinedWifiList.clear();
            String ssid;
            for(int i = 0; i < wifiList.size(); i++){
                ssid = wifiList.get(i).SSID;
                if(ssid.contains("CITI_JSI")) {
                    refinedWifiList.add(ssid);
                    wifiListAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public class ConnectAndCheck extends AsyncTask<Void, Void, Void> {
        String vesnaPass = "passphrase";
        String ssid;
        Context context;
        ConnectAndCheck(String ssid, Context context) {
            this.ssid = ssid;
            this.context = context;

        }

        @Override
        protected Void doInBackground(Void... params) {
            if(mainWifi.getConnectionInfo().getSSID().contains(ssid)) {
                return null;
            }
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + ssid + "\"";
            conf.preSharedKey = "\""+ vesnaPass +"\"";
            mainWifi.addNetwork(conf);
            List<WifiConfiguration> list = mainWifi.getConfiguredNetworks();
            for( WifiConfiguration i : list ) {
                if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                    mainWifi.disconnect();
                    mainWifi.enableNetwork(i.networkId, true);
                    mainWifi.reconnect();
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Intent intent = new Intent(context, DataCollection.class);
            startActivity(intent);
        }
    }
}