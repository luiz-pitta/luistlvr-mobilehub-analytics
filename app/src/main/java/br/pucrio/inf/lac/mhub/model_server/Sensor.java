package br.pucrio.inf.lac.mhub.model_server;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Sensor implements Serializable {
    private static final long serialVersionUID = 63L;

    private String name, macAddress;
    private String uuidSer, uuidData;
    private String uuidCali, uuidConf;
    private int rssi;
    private boolean actuator;
    private byte[] calibrationData, value;
    private ArrayList<String> option_description;
    private ArrayList<String> option_bytes;
    private ArrayList<Byte> enable;

    public String getUuidSer() {
        return uuidSer;
    }

    public String getUuidData() {
        return uuidData;
    }

    public String getUuidCali() {
        return uuidCali;
    }

    public String getUuidConf() {
        return uuidConf;
    }

    public ArrayList<Byte> getEnable() {
        return enable;
    }

    public ArrayList<String> getOptionBytes() {
        return option_bytes;
    }

    public ArrayList<String> getOptionDescription() {
        return option_description;
    }

    public boolean isActuator() {
        return actuator;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getName() {
        return name;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUuidSer(String uuidSer) {
        this.uuidSer = uuidSer;
    }

    public void setUuidData(String uuidData) {
        this.uuidData = uuidData;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public void setCalibrationData(byte[] calibrationData) {
        this.calibrationData = calibrationData;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}
