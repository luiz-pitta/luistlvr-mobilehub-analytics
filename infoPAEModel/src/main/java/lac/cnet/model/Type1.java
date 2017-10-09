package lac.cnet.model;

import java.io.Serializable;
import java.util.HashMap;

public class Type1 implements Serializable
{
    //private static final long serialVersionUID = 7012800986488334161L;
    private static final long serialVersionUID = 204L;
    
    public int    member1 = 0;
    public double member2 = 0;
    public double member3 = 0;
    public String member4 = null;
    public float  member5 = 0;
    
    public int     areaId;
    
    public Type1()
    {
        super();
    }
    
    public Type1(int member1, double member2, double member3, String member4, float member5)
    {
        super();
        this.member1 = member1;
        this.member2 = member2;
        this.member3 = member3;
        this.member4 = member4;
        this.member5 = member5;
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
    public String getMember4()
    {
        return member4;
    }
    public void setMember4(String member4)
    {
        this.member4 = member4;
    }
    public float getMember5()
    {
        return member5;
    }
    public void setMember5(float member5)
    {
        this.member5 = member5;
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
        return "Type1 [member1=" + member1 + ", member2=" + member2 + ", member3=" + member3 + ", member4=" + member4 + ", member5=" + member5 + "]";
    }
    
    public static HashMap<String,Object> getHashMapRepresentation(Type1 type1)
    {
        HashMap<String,Object> newInstance = new HashMap<String, Object>();
        
        newInstance.put("member1", type1.member1);
        newInstance.put("member2", type1.member2);
        newInstance.put("member3", type1.member3);
        newInstance.put("member4", type1.member4);
        newInstance.put("member5", type1.member5);

        return newInstance;
    }
    
    public static Type1 createSampleType1Event()
    {
        int     member1 = 1;
        double  member2 = 1;
        double  member3 = 1;
        String  member4 = "23/04/2015";
        float   member5 = 1;

        Type1 newInstance = new Type1(member1, member2, member3, member4, member5);
        
        return newInstance;
    }

    public static Type1 createSampleType1Event(int areaId)
    {
        Type1 newInstance = createSampleType1Event();
        
        newInstance.areaId = areaId;
        
        return newInstance;
    }
}
