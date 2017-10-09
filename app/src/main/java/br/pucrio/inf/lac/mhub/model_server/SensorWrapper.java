package br.pucrio.inf.lac.mhub.model_server;


import java.io.Serializable;
import java.util.ArrayList;

public class SensorWrapper implements Serializable {
    private static final long serialVersionUID = 63L;
    private ArrayList<Sensor> listSensor = new ArrayList<>();

    public SensorWrapper(ArrayList<Sensor> listSensor) {
        this.listSensor = listSensor;
    }

    public ArrayList<Sensor> getListSensor() {
        return listSensor;
    }

}