package lac.cnet.model;

import java.io.Serializable;
import java.util.HashMap;

public class Location implements Serializable {
	
    //private static final long serialVersionUID = -2498851809585114353L;
    private static final long serialVersionUID = 201L;
	
    public int     nodeId;
    public double  latitude;
    public double  longitude;
    public String  datetime;
    public float   accuracy;
    public String  provider;
    public float   speed;
    public float   bearing;
    public double  altitude;
    public String  connectionType;
    public int     batteryPercent;
    public boolean isCharging;
    public int     areaId;
    public long    genTimestamp;

	
    public Location()
    {
        super();
    }

    public Location(int nodeId, double latitude, double longitude, String datetime, float accuracy, String provider, float speed, float bearing, double altitude, String connectionType, int batteryPercent, boolean isCharging, int areaId, long genTimestamp)
    {
        super();
        this.nodeId = nodeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.datetime = datetime;
        this.accuracy = accuracy;
        this.provider = provider;
        this.speed = speed;
        this.bearing = bearing;
        this.altitude = altitude;
        this.connectionType = connectionType;
        this.batteryPercent = batteryPercent;
        this.isCharging = isCharging;
        this.areaId = areaId;
        this.genTimestamp = genTimestamp;
    }
	
    public int getNodeId()
    {
        return nodeId;
    }
    
    public void setNodeId(int nodeId)
    {
        this.nodeId = nodeId;
    }
    public double getLatitude()
    {
        return latitude;
    }
    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }
    public double getLongitude()
    {
        return longitude;
    }
    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }
    public String getDatetime()
    {
        return datetime;
    }
    public void setDatetime(String datetime)
    {
        this.datetime = datetime;
    }
    public float getAccuracy()
    {
        return accuracy;
    }
    public void setAccuracy(float accuracy)
    {
        this.accuracy = accuracy;
    }
    public String getProvider()
    {
        return provider;
    }
    public void setProvider(String provider)
    {
        this.provider = provider;
    }
    public float getSpeed()
    {
        return speed;
    }
    public void setSpeed(float speed)
    {
        this.speed = speed;
    }
    public float getBearing()
    {
        return bearing;
    }
    public void setBearing(float bearing)
    {
        this.bearing = bearing;
    }
    public double getAltitude()
    {
        return altitude;
    }
    public void setAltitude(double altitude)
    {
        this.altitude = altitude;
    }
    public String getConnectionType()
    {
        return connectionType;
    }
    public void setConnectionType(String connectionType)
    {
        this.connectionType = connectionType;
    }
    public int getBatteryPercent()
    {
        return batteryPercent;
    }
    public void setBatteryPercent(int batteryPercent)
    {
        this.batteryPercent = batteryPercent;
    }
    public boolean isCharging()
    {
        return isCharging;
    }
    public void setCharging(boolean isCharging)
    {
        this.isCharging = isCharging;
    }
    public int getAreaId()
    {
        return areaId;
    }
    public void setAreaId(int area)
    {
        this.areaId = area;
    }
    public long getGenTimestamp()
    {
        return genTimestamp;
    }
    public void setGenTimestamp(long genTimestamp)
    {
        this.genTimestamp = genTimestamp;
    }
    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }
    @Override
    public String toString()
    {
        return "Location [nodeId=" + nodeId + ", latitude=" + latitude + ", longitude=" + longitude + ", datetime=" + datetime + ", accuracy=" + accuracy + ", provider=" + provider + ", speed=" + speed + ", bearing=" + bearing + ", altitude=" + altitude + ", connectionType=" + connectionType + ", batteryPercent=" + batteryPercent + ", isCharging=" + isCharging + ", area=" + areaId
                + ", genTimestamp=" + genTimestamp + "]";
    }
    
    public static HashMap<String,Object> getHashMapRepresentation(Location location)
    {
        HashMap<String,Object> newInstance = new HashMap<String, Object>();

        newInstance.put("nodeId",         location.nodeId);         //"int"
        newInstance.put("latitude",       location.latitude);       //"double",
        newInstance.put("longitude",      location.longitude);      //"double",
        newInstance.put("datetime",       location.datetime);       //String",
        newInstance.put("accuracy",       location.accuracy);       //"float",
        newInstance.put("provider",       location.provider);       //"String",
        newInstance.put("speed",          location.speed);          //"float",
        newInstance.put("bearing",        location.bearing);        //"float",
        newInstance.put("altitude",       location.altitude);       //"double",
        newInstance.put("connectionType", location.connectionType); //"String",
        newInstance.put("batteryPercent", location.batteryPercent); //"int",
        newInstance.put("isCharging",     location.isCharging);     //"boolean",
        newInstance.put("areaId",         location.areaId);           //"int"
        newInstance.put("genTimestamp",   location.genTimestamp);   //"long"

        return newInstance;
    }
    
    public static Location createSampleLocationEvent(int nodeId, double latitude, double longitude, int areaId, int batteryPercent)
    {
        String  datetime       = "14/04/2015";
        float   accuracy       = (float)3.5;
        String  provider       = "Test";
        float   speed          = (float)35.5;
        float   bearing        = (float)120.5;
        double  altitude       = (double)15.5;
        String  connectionType = "Test";
        boolean isCharging     = true;
        long    genTimestamp   = System.currentTimeMillis();

        Location newInstance = new Location(nodeId, latitude, longitude, datetime, accuracy, provider, speed, bearing, altitude, connectionType, batteryPercent, isCharging, areaId, genTimestamp);

        return newInstance;
    }
}
