package com.infopae.model;

import java.io.Serializable;
import java.util.ArrayList;

public class SendSensorData implements Serializable {

    private static final long serialVersionUID = 19L;

    /** Message Data */
    public static final int MOBILE_HUB = 0;
    public static final int ANALYTICS_HUB = 1;
    /** Message Data */

    /** Attributes */
    private Double[] data;
    private int source;
    private long interval;
    private ArrayList<Double[]> listData;
    private ArrayList<String> uuidClients;

    /** Constructor */
    public SendSensorData() {
    }

    /** Getters */
    public Double[] getData() {
        return this.data;
    }

    public int getSource() {
        return this.source;
    }

    public long getInterval() {
        return this.interval;
    }

    public ArrayList<Double[]> getListData() {
        return this.listData;
    }

    public ArrayList<String> getUuidClients() {
        return this.uuidClients;
    }
    /** Getters */


    /** Setters */
    public void setData( Double[] data ) {
        this.data = data;
    }

    public void setSource( int source ) {
        this.source = source;
    }

    public void setInterval( long interval ) {
        this.interval = interval;
    }

    public void setListData( ArrayList<Double[]> listData ) {
        this.listData = listData;
    }

    public void setUuidClients( ArrayList<String> uuidClients ) {
        this.uuidClients = uuidClients;
    }
    /** Setters */
}
