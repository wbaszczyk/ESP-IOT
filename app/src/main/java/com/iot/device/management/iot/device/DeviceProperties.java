package com.iot.device.management.iot.device;

import java.io.Serializable;

/**
 * Created by sneez on 21.04.16.
 */
public class DeviceProperties implements Serializable {

    private String title;
    private String ipAddress;
    private Integer port;
    private String mac;
    private Integer proximity;
    private Boolean touchDetector;
    private String uuid;
    private String version;

    private String status;
    private String deviceFunctionCoapAddress;
    private String deviceVarCoapAddress;
    private Integer position;

    public DeviceProperties(String uuid, String title, String version, String ipAddress, Integer port, String mac) {
        this.uuid = uuid;
        this.title = title;
        this.version = version;
        this.ipAddress = ipAddress;
        this.port = port;
        this.mac = mac;

        this.touchDetector = false;
        this.proximity = 0;

        this.deviceFunctionCoapAddress = "coap://" + ipAddress + ":" + port + "/v1/f/";
        this.deviceVarCoapAddress = "coap://" + ipAddress + ":" + port + "/v1/v/";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getProximity() {
        return proximity;
    }

    public void setProximity(Integer proximity) {
        this.proximity = proximity;
    }

    public Boolean getTouchDetector() {
        return touchDetector;
    }

    public void setTouchDetector(Boolean touchDetector) {
        this.touchDetector = touchDetector;
    }

    public String getDeviceFunctionCoapAddress() {
        return deviceFunctionCoapAddress;
    }

    public String getDeviceVarCoapAddress() {
        return deviceVarCoapAddress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        String active = touchDetector?"active":"not active";
        return "IP:" + ipAddress + System.getProperty("line.separator") +
                " ----> detector " + active + " (" + proximity + ")";
    }
}
