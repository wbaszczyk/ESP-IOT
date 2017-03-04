/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 ******************************************************************************/
package com.iot.device.management.iot.coap.server;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.iot.device.management.iot.device.DeviceProperties;
import com.iot.device.management.iot.device.ProximityDetectorNotification;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;

import java.util.UUID;

public class ServerService extends Service {
    private CoapServer server;
    public static final String BROADCAST_ACTION = "com.iot.device.management.iot.coap.server";
    public static final String GET_DEVICE_DESCRIPTION_KEY = "devicePropertiesKey";
    public static final String GET_PROXIMITY_DETECTOR_NOTIFICATION_KEY = "proximityNotificationKey";
    public static final String GET_DEVICE_ACTIVE_NOTIFICATION_KEY = "deviceActiveNotificationKey";

    @Override
    public void onCreate() {
        super.onCreate();
        NetworkConfig networkConfig = new NetworkConfig();
        this.server = new CoapServer(networkConfig);
        server.add(new RegisterDevice());
        server.add(new NotifyAboutProximityDetector());
        server.add(new NotifyAboutDeviceActive());
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        server.start();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        server.stop();
        server.destroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class RegisterDevice extends CoapResource {
        private static final String TAG_TITLE = "title";
        private static final String TAG_IP = "ip";
        private static final String TAG_PORT = "port";
        private static final String TAG_MAC = "mac";
        private static final String TAG_VERSION = "version";

        public RegisterDevice() {
            super("register");
            getAttributes().setTitle("Register device resource");
        }
        @Override
        public void handlePOST(CoapExchange exchange) {
            try {
                String request = exchange.getRequestText();
                JSONObject register = new JSONObject(request);
                String title = register.getString(TAG_TITLE);
                String ipAddress = register.getString(TAG_IP);
                Integer port = register.getInt(TAG_PORT);
                String macAddress = register.getString(TAG_MAC);
                String version = register.getString(TAG_VERSION);

                String uuid = UUID.randomUUID().toString();
                DeviceProperties deviceProperties = new DeviceProperties(uuid, title, version, ipAddress, port, macAddress);

                Intent intent = new Intent(BROADCAST_ACTION);
                intent.putExtra(GET_DEVICE_DESCRIPTION_KEY, deviceProperties);
                sendBroadcast(intent);

                exchange.accept();
                exchange.respond(CoAP.ResponseCode.CONTENT, uuid);
            } catch (Exception e) {
                exchange.reject();
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
            }
        }
    }
    class NotifyAboutProximityDetector extends CoapResource {
        private static final String TAG_UUID = "uuid";
        private static final String TAG_PROXIMITY = "proximity";

        public NotifyAboutProximityDetector() {
            super("proximity");
            getAttributes().setTitle("Proximity value");
        }
        @Override
        public void handlePOST(CoapExchange exchange) {
            try {
                String request = exchange.getRequestText();
                JSONObject register = new JSONObject(request);
                String uuid = register.getString(TAG_UUID);
                Integer proximity = register.getInt(TAG_PROXIMITY);

                ProximityDetectorNotification proximityDetectorNotification = new ProximityDetectorNotification(proximity, uuid);

                Intent intent = new Intent(BROADCAST_ACTION);
                intent.putExtra(GET_PROXIMITY_DETECTOR_NOTIFICATION_KEY, proximityDetectorNotification);
                sendBroadcast(intent);

                exchange.accept();
                exchange.respond(CoAP.ResponseCode.CONTENT);
            } catch (Exception e) {
                exchange.reject();
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
            }
        }
    }
    class NotifyAboutDeviceActive extends CoapResource {
        private static final String TAG_UUID = "uuid";

        public NotifyAboutDeviceActive() {
            super("active");
            getAttributes().setTitle("Device active");
        }
        @Override
        public void handlePOST(CoapExchange exchange) {
            try {
                String request = exchange.getRequestText();
                JSONObject register = new JSONObject(request);
                String uuid = register.getString(TAG_UUID);

                Intent intent = new Intent(BROADCAST_ACTION);
                intent.putExtra(GET_DEVICE_ACTIVE_NOTIFICATION_KEY, uuid);
                sendBroadcast(intent);

                exchange.accept();
                exchange.respond(CoAP.ResponseCode.CONTENT);
            } catch (Exception e) {
                exchange.reject();
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
            }
        }
    }
}
