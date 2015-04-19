package com.ddns.citisense.citisense_vesna;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
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
    String vesnaPass = "passphrase";
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
        String ssid = ((TextView)((ViewGroup)view).getChildAt(1)).getText().toString();
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
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            wifiList = mainWifi.getScanResults();
            refinedWifiList.clear();
            for(int i = 0; i < wifiList.size(); i++){
                refinedWifiList.add(wifiList.get(i).SSID);
                wifiListAdapter.notifyDataSetChanged();
            }
        }
    }
}