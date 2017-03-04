package com.iot.device.management.iot.coap.client;

import android.os.AsyncTask;
import android.widget.TextView;

import com.iot.device.management.iot.R;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

/**
 * Created by sneez on 23.04.16.
 */
public class CoapPostTask extends AsyncTask<String, String, CoapResponse> {

    protected void onPreExecute() {
        // Loading...
    }

    protected CoapResponse doInBackground(String... args) {
        CoapClient client = new CoapClient(args[0]);
        return client.post(args[1], MediaTypeRegistry.TEXT_PLAIN);
    }

    protected void onPostExecute(CoapResponse response) {
        // OK
    }
}