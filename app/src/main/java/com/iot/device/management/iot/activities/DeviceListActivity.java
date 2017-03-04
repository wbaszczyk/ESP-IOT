package com.iot.device.management.iot.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.iot.device.management.iot.DeviceUtil;
import com.iot.device.management.iot.R;
import com.iot.device.management.iot.adapters.DeviceAdapter;
import com.iot.device.management.iot.coap.client.CoapPostTask;
import com.iot.device.management.iot.coap.server.ServerService;
import com.iot.device.management.iot.device.DeviceProperties;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sneez on 22.04.16.
 */
public class DeviceListActivity extends Activity implements Button.OnClickListener {
    public static final String DEVICE_DETAIL_KEY = "device";
    private ListView lvDevices;
    private DeviceAdapter deviceAdapter;
    ArrayList<DeviceProperties> devices = new ArrayList<>();

    private Map<String, DeviceProperties> devicePropertiesMap = new HashMap<>();
    private Map<String, String> ipDevices = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        lvDevices = (ListView) findViewById(R.id.lvDevices);
        deviceAdapter = new DeviceAdapter(this, devices);
        lvDevices.setAdapter(deviceAdapter);
        setupDeviceSelectedListener();

        startService(new Intent(this, ServerService.class));
        registerReceiver(broadcastReceiver, new IntentFilter(ServerService.BROADCAST_ACTION));

        Button refreshbtn = (Button)findViewById(R.id.refreshbtn);
        refreshbtn.setOnClickListener(this);

        new Thread(new Runnable(){
            public void run(){
                while(true){
                    for (DeviceProperties deviceProperties : devicePropertiesMap.values()) {
                        new CoapGetTask().execute(deviceProperties.getDeviceVarCoapAddress() + "isActive", deviceProperties.getUuid());
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            }
        ).start();
//        registerDevice(new DeviceProperties("asd", "dsaa", "ver", "1.1.1.1", 123, "mac"));
//        registerDevice(new DeviceProperties("asd1", "dsaa1", "ver1", "1.1.1.2", 123, "mac1"));
    }

    private void unregisterDevice(String uuid){
        if(uuid != null) {
            DeviceProperties deviceProperties = devicePropertiesMap.get(uuid);
            if(deviceProperties != null) {
                if (ipDevices.containsKey(deviceProperties.getIpAddress())) {
                    devicePropertiesMap.remove(ipDevices.get(deviceProperties.getIpAddress()));
                    ipDevices.remove(deviceProperties.getIpAddress());
                }
                devices.clear();
                for (Map.Entry<String, DeviceProperties> entry : devicePropertiesMap.entrySet()) {
                    devices.add(entry.getValue());
                }
                deviceAdapter.notifyDataSetChanged();
            }
        }
    }
    private void registerDevice(DeviceProperties deviceProperties){
        if(deviceProperties != null) {
            if (ipDevices.containsKey(deviceProperties.getIpAddress())) {
                devicePropertiesMap.remove(ipDevices.get(deviceProperties.getIpAddress()));
            }
            String uuid = deviceProperties.getUuid();
            ipDevices.put(deviceProperties.getIpAddress(), uuid);
            devicePropertiesMap.put(uuid, deviceProperties);

            devices.clear();
            for(Map.Entry<String, DeviceProperties> entry: devicePropertiesMap.entrySet()) {
                devices.add(entry.getValue());
            }
            deviceAdapter.notifyDataSetChanged();
            String serverAddress = DeviceUtil.getLocalIpAddress();
            new CoapPostTask().execute(deviceProperties.getDeviceFunctionCoapAddress() + "serverAddress", "coap://" + serverAddress + ":5683/");
            new CoapPostTask().execute(deviceProperties.getDeviceFunctionCoapAddress() + "uuid", uuid);
        }
    }
    private void clearDeviceList(){
        ipDevices.clear();
        devicePropertiesMap.clear();
        devices.clear();
        deviceAdapter.notifyDataSetChanged();
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DeviceProperties deviceProperties = (DeviceProperties) intent.getExtras().getSerializable(ServerService.GET_DEVICE_DESCRIPTION_KEY);
            registerDevice(deviceProperties);
            String uuid = intent.getExtras().getString(ServerService.GET_DEVICE_ACTIVE_NOTIFICATION_KEY);
            if(uuid != null){
                deviceProperties = devicePropertiesMap.get(uuid);
                Integer position = deviceAdapter.getPosition(deviceProperties);
                View v = ((ListView) findViewById(R.id.lvDevices)).getChildAt(position);
                if(v != null) {
                    ((View) v.findViewById(R.id.vNotificator)).setBackgroundColor(Color.GREEN);
                    ((View) v.findViewById(R.id.vNotificator)).setEnabled(true);
                }
            }
        }
    };
    public void setupDeviceSelectedListener() {
        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                lvDevices.getChildAt(position).setEnabled(false);
            Intent intent = new Intent(DeviceListActivity.this, DeviceDetailActivity.class);
            intent.putExtra(DEVICE_DETAIL_KEY, deviceAdapter.getItem(position));
            startActivity(intent);
            }
        });
    }
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.refreshbtn){
            try {
                String serverAddress = DeviceUtil.getLocalIpAddress();
                String broadcast = DeviceUtil.getBroadcast();
                clearDeviceList();
                new CoapPostTask().execute("coap://" + broadcast + ":5683/v1/f/" + "requestRefersh", "coap://" + serverAddress + ":5683/");
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    public class CoapGetTask extends AsyncTask<String, String, CoapResponse> {

        String uuid;
        protected void onPreExecute() {
            // Loading...
        }

        protected CoapResponse doInBackground(String... args) {
            uuid = args[1];
            CoapClient client = new CoapClient(args[0]);
            return client.get();
        }

        protected void onPostExecute(CoapResponse response) {
            if(response == null) {
                unregisterDevice(uuid);
            }
        }
    }
}
