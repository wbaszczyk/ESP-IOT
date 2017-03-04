package com.iot.device.management.iot.device;

import java.io.Serializable;

/**
 * Created by sneez on 21.04.16.
 */
public class ProximityDetectorNotification implements Serializable {
    private String uuid;
    private Integer proximity;

    public ProximityDetectorNotification(Integer proximity, String uuid) {
        this.proximity = proximity;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getProximity() {
        return proximity;
    }

    public void setProximity(Integer proximity) {
        this.proximity = proximity;
    }

    @Override
    public String toString() {
        return "ProximityDetectorNotification{" +
                "uuid='" + uuid + '\'' +
                ", proximity=" + proximity +
                '}';
    }
}
