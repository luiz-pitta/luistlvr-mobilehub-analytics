package com.infopae.model;

import java.io.Serializable;
import java.util.ArrayList;

public class SendSensorData implements Serializable {

    private static final long serialVersionUID = 19L;

    private Double[] data;
    private ArrayList<Double[]> listData;
    private ArrayList<String> uuidClients;

    public SendSensorData() {
    }

    public Double[] getData() {
        return this.data;
    }

    public ArrayList<Double[]> getListData() {
        return this.listData;
    }

    public ArrayList<String> getUuidClients() {
        return this.uuidClients;
    }

    public void setData( Double[] data ) {
        this.data = data;
    }

    public void setListData( ArrayList<Double[]> listData ) {
        this.listData = listData;
    }

    public void setUuidClients( ArrayList<String> uuidClients ) {
        this.uuidClients = uuidClients;
    }
}
