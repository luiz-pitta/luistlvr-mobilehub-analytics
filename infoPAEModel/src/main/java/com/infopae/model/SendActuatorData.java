package com.infopae.model;

import java.io.Serializable;

public class SendActuatorData implements Serializable {

    private static final long serialVersionUID = 21L;

    private byte[] COMMAND;
    private String uuidData, uuidHub;

    public SendActuatorData() {
    }

    public byte[] getCommand() {
        return this.COMMAND;
    }

    public String getUuidData() {
        return uuidData;
    }

    public String getUuidHub() {
        return uuidHub;
    }

    public void setCommand( byte[] COMMAND ) {
        this.COMMAND = COMMAND;
    }

    public void setUuidData(String uuidData) {
        this.uuidData = uuidData;
    }

    public void setUuidHub(String uuidHub) {
        this.uuidHub = uuidHub;
    }
}
