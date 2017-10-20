package com.infopae.model;

import java.io.Serializable;

public class SendAcknowledge implements Serializable {

    private static final long serialVersionUID = 21L;

    /** Attributes */
    private String uuidIoTrade, uuidProvider;

    /** Constructor */
    public SendAcknowledge() {
    }

    /** Getters */
    public String getUuidIoTrade() {
        return uuidIoTrade;
    }

    public String getUuidProvider() {
        return uuidProvider;
    }
    /** Getters */


    /** Setters */
    public void setUuidIoTrade(String uuidIoTrade) {
        this.uuidIoTrade = uuidIoTrade;
    }

    public void setUuidProvider(String uuidProvider) {
        this.uuidProvider = uuidProvider;
    }
    /** Setters */
}
