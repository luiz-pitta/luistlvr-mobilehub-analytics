package lac.cnet.model;

import java.io.Serializable;
import java.util.HashMap;

public class LowBattery implements Serializable
{
    //private static final long serialVersionUID = 8297317865245774685L;
    private static final long serialVersionUID = 202L;

    private Location nodeLocation = null;
    
    
    public LowBattery()
    {
        super();
    }

    public LowBattery(Location nodeLocation)
    {
        super();
        this.nodeLocation = nodeLocation;
    }


    public Location getNodeLocation()
    {
        return nodeLocation;
    }

    public void setNodeLocation(Location nodeLocation)
    {
        this.nodeLocation = nodeLocation;
    }

    @Override
    public String toString()
    {
        return "LowBattery [nodeLocation=" + nodeLocation + "]";
    }
    
    public static HashMap<String,Object> getHashMapRepresentation(LowBattery lowBattery)
    {
        HashMap<String,Object> newInstance = new HashMap<String, Object>();

        newInstance.put("nodeLocation", lowBattery.nodeLocation);

        return newInstance;
    }
    
    public static LowBattery createSampleLowBatteryEvent(int nodeId, double latitude, double longitude, int areaId, int batteryPercent)
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

        Location nodeLocation  = new Location(nodeId, latitude, longitude, datetime, accuracy, provider, speed, bearing, altitude, connectionType, batteryPercent, isCharging, areaId, genTimestamp);
        LowBattery newInstance = new LowBattery(nodeLocation);

        return newInstance;
    }
}
