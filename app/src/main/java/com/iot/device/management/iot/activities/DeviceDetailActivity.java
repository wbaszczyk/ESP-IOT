package com.iot.device.management.iot.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.iot.device.management.iot.R;
import com.iot.device.management.iot.coap.client.CoapPostTask;
import com.iot.device.management.iot.coap.server.ServerService;
import com.iot.device.management.iot.device.DeviceProperties;
import com.iot.device.management.iot.device.ProximityDetectorNotification;
import com.squareup.picasso.Picasso;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by sneez on 22.04.16.
 */
public class DeviceDetailActivity  extends Activity implements SeekBar.OnSeekBarChangeListener {
    private String uuid;
    private Integer progressValue = 0;
    private String deviceFunctionCoapAddress;

    private ImageView ivDeviceCover;
    private TextView tvTitle;
    private TextView tvVersion;
    private TextView tvCoapAddress;
    private TextView tvUuid;
    private TextView tvProximity;
    private TextView tvRGB;
    private CheckBox checkBoxColor;

    private SeekBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);


        ivDeviceCover = (ImageView) findViewById(R.id.ivDeviceCover);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvVersion = (TextView) findViewById(R.id.tvVersion);
        tvUuid = (TextView) findViewById(R.id.tvUuid);
        tvCoapAddress = (TextView) findViewById(R.id.tvCoapAddress);
        tvProximity = (TextView) findViewById(R.id.tvProximity);
        checkBoxColor = (CheckBox) findViewById(R.id.checkBoxColor);

        DeviceProperties device = (DeviceProperties) getIntent().getSerializableExtra(DeviceListActivity.DEVICE_DETAIL_KEY);
        new CoapStatusTask().execute(device.getDeviceFunctionCoapAddress() + "status", "empty");
        uuid = device.getUuid();
        deviceFunctionCoapAddress = device.getDeviceFunctionCoapAddress();

        this.setTitle(device.getTitle());
        Picasso.with(this).load(R.mipmap.device).into(ivDeviceCover);
        tvTitle.setText(device.getTitle());
        tvVersion.setText(device.getVersion());
        tvUuid.setText(uuid);
        tvCoapAddress.setText(deviceFunctionCoapAddress);
        tvProximity.setText("Proximity: " + device.getProximity().toString());


        registerReceiver(broadcastReceiver, new IntentFilter(ServerService.BROADCAST_ACTION));

        final ToggleButton proximitybtn= (ToggleButton) findViewById(R.id.proximitybtn);
        final ToggleButton touchbtn= (ToggleButton) findViewById(R.id.touchbtn);
        proximitybtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    touchbtn.setChecked(false);
                    new CoapPostTask().execute(deviceFunctionCoapAddress + "proximity_start", "notify");
                    proximitybtn.setText("ON");
                }
                else {
                    new CoapPostTask().execute(deviceFunctionCoapAddress + "proximity_stop", "notify");
                    proximitybtn.setText("OFF");
                }
            }
        });
        touchbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    proximitybtn.setChecked(false);
                    new CoapPostTask().execute(deviceFunctionCoapAddress + "touch_start", "notify");
                    touchbtn.setText("ON");
                }
                else {
                    new CoapPostTask().execute(deviceFunctionCoapAddress + "touch_stop", "notify");
                    touchbtn.setText("OFF");
                }
            }
        });
        checkBoxColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    new CoapPostTask().execute(deviceFunctionCoapAddress + "proximity_color_active", "1");
                }
                else {
                    new CoapPostTask().execute(deviceFunctionCoapAddress + "proximity_color_active", "0");
                }
            }
        });
        Button calibratebtn = (Button)findViewById(R.id.calibratebtn);
        calibratebtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CoapPostTask().execute(deviceFunctionCoapAddress + "calibrate", "notify");
            }
        });
        bar = (SeekBar) findViewById(R.id.seekBarGreen);
        bar.setOnSeekBarChangeListener(this);
        bar = (SeekBar) findViewById(R.id.seekBarBlue);
        bar.setOnSeekBarChangeListener(this);
        bar = (SeekBar) findViewById(R.id.seekBarRed);
        bar.setOnSeekBarChangeListener(this);
        bar = (SeekBar) findViewById(R.id.seekBarSensitivity);
        bar.setOnSeekBarChangeListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_detail, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_share) {
//            setShareIntent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ProximityDetectorNotification proximityDetectorNotification =
                    (ProximityDetectorNotification) intent.getExtras().getSerializable(ServerService.GET_PROXIMITY_DETECTOR_NOTIFICATION_KEY);
            if(proximityDetectorNotification != null){
                if(uuid.equals(proximityDetectorNotification.getUuid())) {
                    tvProximity.setText("Proximity: " + proximityDetectorNotification.getProximity().toString());
                }
            }
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        progressValue = i;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Integer val = (progressValue * 1023) / 100;
        if (seekBar.getId() == R.id.seekBarGreen)
            new CoapPostTask().execute(deviceFunctionCoapAddress + "set_g", val.toString());
        else if (seekBar.getId() == R.id.seekBarBlue)
            new CoapPostTask().execute(deviceFunctionCoapAddress + "set_b", val.toString());
        else if (seekBar.getId() == R.id.seekBarRed)
            new CoapPostTask().execute(deviceFunctionCoapAddress + "set_r", val.toString());
        else if (seekBar.getId() == R.id.seekBarSensitivity)
            new CoapPostTask().execute(deviceFunctionCoapAddress + "sensitivity", progressValue.toString());
//                Toast.makeText(getActivity(), "Filed to get server handler", Toast.LENGTH_SHORT).show();

    }
    private void changeEnabled(Boolean enable){

        (findViewById(R.id.seekBarRed)).setEnabled(enable);
        (findViewById(R.id.seekBarBlue)).setEnabled(enable);
        (findViewById(R.id.seekBarGreen)).setEnabled(enable);
        (findViewById(R.id.proximitybtn)).setEnabled(enable);
        (findViewById(R.id.calibratebtn)).setEnabled(enable);
    }
    public class CoapStatusTask extends AsyncTask<String, String, CoapResponse> {

        private static final String TAG_R = "r";
        private static final String TAG_G = "g";
        private static final String TAG_B = "b";
        private static final String TAG_PROXIMITY_DETECTOR_STATUS = "detector";
        private static final String TAG_PROXIMITY_DETECTOR_SENSITIVITY = "sensitivity";

        protected void onPreExecute() {
            changeEnabled(false);
            ((TextView) findViewById(R.id.tvStatus)).setText("Loading...");
            ((TextView) findViewById(R.id.tvStatus)).setTextColor(Color.BLUE);
        }
        protected CoapResponse doInBackground(String... args) {
            CoapClient client = new CoapClient(args[0]);
            return client.post(args[1], MediaTypeRegistry.TEXT_PLAIN);
        }
        protected void onPostExecute(CoapResponse response) {
            if(response != null) {
                try {
                    JSONObject register = new JSONObject(response.getResponseText());
                    Boolean detector = register.getBoolean(TAG_PROXIMITY_DETECTOR_STATUS);
                    Integer sensitivity = register.getInt(TAG_PROXIMITY_DETECTOR_SENSITIVITY);
                    ((ToggleButton) findViewById(R.id.proximitybtn)).setChecked(detector);
                    ((SeekBar) findViewById(R.id.seekBarSensitivity)).setProgress(sensitivity);
                    Integer r = register.getInt(TAG_R);
                    Integer g = register.getInt(TAG_G);
                    Integer b = register.getInt(TAG_B);
                    ((SeekBar) findViewById(R.id.seekBarRed)).setProgress((r*100)/1023);
                    ((SeekBar) findViewById(R.id.seekBarBlue)).setProgress((b*100)/1023);
                    ((SeekBar) findViewById(R.id.seekBarGreen)).setProgress((g*100)/1023);
                    ((TextView) findViewById(R.id.tvStatus)).setTextColor(Color.GREEN);
                    ((TextView) findViewById(R.id.tvStatus)).setText("Device is active");
                    changeEnabled(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                changeEnabled(false);
                ((TextView) findViewById(R.id.tvStatus)).setTextColor(Color.RED);
                ((TextView) findViewById(R.id.tvStatus)).setText("Device is not active");
            }
        }
    }

}
