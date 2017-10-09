package lac.cnet.model;

import java.io.Serializable;
import java.util.HashMap;

public class TooClose implements Serializable
{
    //private static final long serialVersionUID = -8874304329694402700L;
    private static final long serialVersionUID = 203L;
    
    private Location nodeALocation;
    private Location nodeBLocation;
    
    
    public TooClose()
    {
        super();
    }
    
    public TooClose(Location nodeALocation, Location nodeBLocation)
    {
        super();
        this.nodeALocation = nodeALocation;
        this.nodeBLocation = nodeBLocation;
    }


    public Location getNodeALocation()
    {
        return nodeALocation;
    }
    public void setNodeALocation(Location nodeALocation)
    {
        this.nodeALocation = nodeALocation;
    }
    public Location getNodeBLocation()
    {
        return nodeBLocation;
    }
    public void setNodeBLocation(Location nodeBLocation)
    {
        this.nodeBLocation = nodeBLocation;
    }
    
    @Override
    public String toString()
    {
        return "TooClose [nodeALocation=" + nodeALocation + ", nodeBLocation=" + nodeBLocation + "]";
    }
    
    public static HashMap<String,Object> getHashMapRepresentation(TooClose tooClose)
    {
        HashMap<String,Object> newInstance = new HashMap<String, Object>();

        newInstance.put("nodeALocation", tooClose.nodeALocation);
        newInstance.put("nodeBLocation", tooClose.nodeBLocation);

        return newInstance;
    }
    
}
