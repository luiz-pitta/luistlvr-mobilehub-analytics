package lac.cnet.model;

import java.io.Serializable;
import java.util.HashMap;

public class Type5 implements Serializable
{
    //private static final long serialVersionUID = -7035763611839163759L;
    private static final long serialVersionUID = 208L;
    
    public int    member1 = 0;
    public double member2 = 0;
    public double member3 = 0;
    
    public int     areaId;
    
    public Type5()
    {
        super();
    }
    
    public Type5(int member1, double member2, double member3)
    {
        super();
        this.member1 = member1;
        this.member2 = member2;
        this.member3 = member3;
    }


    public int getMember1()
    {
        return member1;
    }
    public void setMember1(int member1)
    {
        this.member1 = member1;
    }
    public double getMember2()
    {
        return member2;
    }
    public void setMember2(double member2)
    {
        this.member2 = member2;
    }
    public double getMember3()
    {
        return member3;
    }
    public void setMember3(double member3)
    {
        this.member3 = member3;
    }
    
    public int getAreaId()
    {
        return areaId;
    }
    public void setAreaId(int area)
    {
        this.areaId = area;
    }
    
    @Override
    public String toString()
    {
        return "Type5 [member1=" + member1 + ", member2=" + member2 + ", member3=" + member3 + "]";
    }
    
    public static HashMap<String,Object> getHashMapRepresentation(Type5 type5)
    {
        HashMap<String,Object> newInstance = new HashMap<String, Object>();
        
        newInstance.put("member1", type5.member1);
        newInstance.put("member2", type5.member2);
        newInstance.put("member3", type5.member3);

        return newInstance;
    }

    public static Type5 createSampleType5Event()
    {
        int     member1 = 1;
        double  member2 = 1;
        double  member3 = 1;

        Type5 newInstance = new Type5(member1, member2, member3);
        
        return newInstance;
    }
    
    public static Type5 createSampleType5Event(int areaId)
    {
        Type5 newInstance = createSampleType5Event();
        
        newInstance.areaId = areaId;
        
        return newInstance;
    }
}
