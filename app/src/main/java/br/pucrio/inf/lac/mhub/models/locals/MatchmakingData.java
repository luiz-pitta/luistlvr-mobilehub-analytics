package br.pucrio.inf.lac.mhub.models.locals;

import org.json.JSONException;
import org.json.JSONObject;

import br.pucrio.inf.lac.mhub.models.base.LocalMessage;


/**
 * Contains the sensor data received from the
 * Mobile Objects
 */
public class MatchmakingData extends LocalMessage {
    /** DEBUG */
    private static final String TAG = MatchmakingData.class.getSimpleName();


    /** Message Data */
    public static final int START = 0;
    public static final int STOP = 1;

    private String uuidMatch, uuidClient, uuidAnalyticsClient;
    private String macAddress, uuidData;
    private int startStop;


    public MatchmakingData() {
        super( TAG );
    }

    /** Getters */
    public String getUuidMatch() {
        return this.uuidMatch;
    }

    public String getUuidAnalyticsClient() {
        return this.uuidAnalyticsClient;
    }

    public String getUuidClient() {
        return this.uuidClient;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public String getUuidData() {
        return this.uuidData;
    }

    public int getStartStop() {
        return this.startStop;
    }
    /** Getters */

    /** Setters */
    public void setUuidClient( String uuidClient ) {
        this.uuidClient = uuidClient;
    }

    public void setUuidAnalyticsClient( String uuidAnalyticsClient ) {
        this.uuidAnalyticsClient = uuidAnalyticsClient;
    }

    public void setUuidMatch( String uuidMatch ) {
        this.uuidMatch = uuidMatch;
    }

    public void setMacAddress( String macAddress ) {
        this.macAddress = macAddress;
    }

    public void setUuidData( String uuidData ) {
        this.uuidData = uuidData;
    }

    public void setStartStop( int startStop ) {
        this.startStop = startStop;
    }
    /** Setters */

    @Override
    public String getID() {
        return getUuidClient() + SEPARATOR + getUuidMatch();
    }

    @Override
    public String toJSON() throws JSONException {
        JSONObject data = new JSONObject();

        data.put( UUID_MATCH,   getUuidMatch() );
        data.put( UUID_CLIENT, getUuidClient() );
        data.put( UUID_DATA,   getUuidData() );
        data.put( MAC_ADDRESS, getMacAddress() );
        data.put( START_STOP, getStartStop() );
        data.put( UUID_ANALYTICS_CLIENT, getUuidAnalyticsClient() );

        // Parent
        data.put( FUNCTION,  getTag() );

        return data.toString();
    }

    @Override
    public String toString() {
        return TAG + " [uuid_match=" + getUuidMatch() + ", uuid_client=" + getUuidClient() + "]";
    }
}
