package lac.cnet.model;

import java.io.Serializable;
import java.util.HashMap;

public class ClosePOI implements Serializable
{
    //private static final long serialVersionUID = -6373467463108322689L;
    private static final long serialVersionUID = 200L;
    
    public Location nodeLocation = null;

    public String   poiName      = null;
    public double   poiLatitude  = 0;
    public double   poiLongitude = 0;
    
    
    public ClosePOI()
    {
        super();
    }
    
    public ClosePOI(Location nodeLocation, String poiName, double poiLatitude, double poiLongitude)
    {
        super();
        this.nodeLocation = nodeLocation;
        this.poiName = poiName;
        this.poiLatitude = poiLatitude;
        this.poiLongitude = poiLongitude;
    }


    public Location getNodeLocation()
    {
        return nodeLocation;
    }
    public void setNodeLocation(Location nodeLocation)
    {
        this.nodeLocation = nodeLocation;
    }
    public String getPoiName()
    {
        return poiName;
    }
    public void setPoiName(String poiName)
    {
        this.poiName = poiName;
    }
    public double getPoiLatitude()
    {
        return poiLatitude;
    }
    public void setPoiLatitude(double poiLatitude)
    {
        this.poiLatitude = poiLatitude;
    }
    public double getPoiLongitude()
    {
        return poiLongitude;
    }
    public void setPoiLongitude(double poiLongitude)
    {
        this.poiLongitude = poiLongitude;
    }
    
    @Override
    public String toString()
    {
        return "ClosePOI [nodeLocation=" + nodeLocation + ", poiName=" + poiName + ", poiLatitude=" + poiLatitude + ", poiLongitude=" + poiLongitude + "]";
    }
    
    public static HashMap<String,Object> getHashMapRepresentation(ClosePOI closePOI)
    {
        HashMap<String,Object> newInstance = Location.getHashMapRepresentation(closePOI.nodeLocation);
        
        newInstance.put("poiName", closePOI.poiName);
        newInstance.put("poiLatitude", closePOI.poiLatitude);
        newInstance.put("poiLongitude", closePOI.poiLongitude);

        return newInstance;
    }
        
}
