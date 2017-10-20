package com.infopae.model;

import java.io.Serializable;

public class BuyAnalyticsData implements Serializable {

    private static final long serialVersionUID = 25L;

    /** Attributes */
    private int option;
    private double value;
    private String macAddress;
    private String uuidData, uuidIotrade, uuidAnalyticsHub;

    /** Constructor */
    public BuyAnalyticsData() {
    }

    /** Getters */
    public int getOption() {
        return this.option;
    }

    public String getUuidData() {
        return uuidData;
    }

    public String getUuidAnalyticsHub() {
        return uuidAnalyticsHub;
    }

    public String getUuidIotrade() {
        return uuidIotrade;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public double getValue() {
        return value;
    }
    /** Getters */

    /** Setters */
    public void setUuidData( String uuidData ) {
        this.uuidData = uuidData;
    }

    public void setUuidIotrade( String uuidIotrade ) {
        this.uuidIotrade = uuidIotrade;
    }

    public void setUuidAnalyticsHub( String uuidAnalyticsHub ) {
        this.uuidAnalyticsHub = uuidAnalyticsHub;
    }

    public void setMacAddress( String macAddress ) {
        this.macAddress = macAddress;
    }

    public void setOption( int option ) {
        this.option = option;
    }

    public void setValue( double value ) {
        this.value = value;
    }
    /** Setters */

}
