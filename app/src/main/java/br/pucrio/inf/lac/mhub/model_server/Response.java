package br.pucrio.inf.lac.mhub.model_server;

import java.util.ArrayList;

public class Response {

    private String message;
    private String uuid;
    private String sensorName;
    private String macAddress;
    private Double[] data;
    private ArrayList<Sensor> sensors;

    public ArrayList<Sensor> getSensors() {
        return sensors;
    }

    public String getMessage() {
        return message;
    }

    public String getUuid() {
        return uuid;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getSensorName() {
        return sensorName;
    }

    public Double[] getData() {
        return data;
    }
}
