package com.infopae.model;

import java.io.Serializable;
import java.util.ArrayList;

public class SendAnalyticsData implements Serializable {

    private static final long serialVersionUID = 23L;

    /** Attributes */
    private Double[] data;
    private String macAddress;
    private String uuid;
    private long interval;
    private ArrayList<String> uuidClients;

    /** Constructor */
    public SendAnalyticsData() {
    }

    /** Getters */
    public Double[] getData() {
        return this.data;
    }

    public String getUuid() {
        return uuid;
    }

    public long getInterval() {
        return this.interval;
    }

    public String getMacAddress() {
        return macAddress;
    }
    /** Getters */


    /** Setters */
    public void setUuid( String uuid ) {
        this.uuid = uuid;
    }

    public void setMacAddress( String macAddress ) {
        this.macAddress = macAddress;
    }

    public ArrayList<String> getUuidClients() {
        return this.uuidClients;
    }

    public void setData( Double[] data ) {
        this.data = data;
    }

    public void setInterval( long interval ) {
        this.interval = interval;
    }

    public void setUuidClients( ArrayList<String> uuidClients ) {
        this.uuidClients = uuidClients;
    }
    /** Setters */
}
