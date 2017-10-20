package br.pucrio.inf.lac.mhub.model_server;


import java.io.Serializable;
import java.util.UUID;

/**
 * Model to store provider information got from server
 * @author Luiz Guilherme Pitta
 */
public class User implements Serializable {
    private static final long serialVersionUID = 2L;

    /** Attributes */
    private String name, device;
    private double batery;
    private int signal;
    private double lat, lng;
    private float accuracy;
    private UUID uuid;
    private boolean active = false;

    /** Getters */
    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public double getBatery() {
        return batery;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public int getSignal() {
        return signal;
    }
    /** Getters */

    /** Setters */
    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public void setBatery(double batery) {
        this.batery = batery;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public float setAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    /** Setters */
}
