package com.iot.device.management.iot.coap;

/**
 * Created by sneez on 23.04.16.
 */
public class DeviceGetRequest {

    private String requestCoapIp;
    private String resopnse;

    public DeviceGetRequest(String requestCoapIp) {
        this.requestCoapIp = requestCoapIp;
    }

    public String getRequestCoapIp() {
        return requestCoapIp;
    }

    public void setRequestCoapIp(String requestCoapIp) {
        this.requestCoapIp = requestCoapIp;
    }

    public String getResopnse() {
        return resopnse;
    }

    public void setResopnse(String resopnse) {
        this.resopnse = resopnse;
    }
}
