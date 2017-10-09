package lac.cnet.model;

import java.io.Serializable;
import java.util.HashMap;

public class Type3 implements Serializable
{
    //private static final long serialVersionUID = -2491927973235252071L;
    private static final long serialVersionUID = 206L;
    
    public int    member1 = 0;
    public double member2 = 0;
    public double member3 = 0;
    public String member4 = null;
    public float  member5 = 0;
    public String member6 = null;
    public float  member7 = 0;
    public int    member8 = 0;
    public double member9 = 0;
    
    public int     areaId;
    
    
    public Type3()
    {
        super();
    }
    
    public Type3(int member1, double member2, double member3, String member4, float member5, String member6, float member7, int member8, double member9)
    {
        super();
        this.member1 = member1;
        this.member2 = member2;
        this.member3 = member3;
        this.member4 = member4;
        this.member5 = member5;
        this.member6 = member6;
        this.member7 = member7;
        this.member8 = member8;
        this.member9 = member9;
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
    public String getMember6()
    {
        return member6;
    }
    public void setMember6(String member6)
    {
        this.member6 = member6;
    }
    public float getMember7()
    {
        return member7;
    }
    public void setMember7(float member7)
    {
        this.member7 = member7;
    }
    public int getMember8()
    {
        return member8;
    }
    public void setMember8(int member8)
    {
        this.member8 = member8;
    }
    public double getMember9()
    {
        return member9;
    }
    public void setMember9(double member9)
    {
        this.member9 = member9;
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
        return "Type3 [member1=" + member1 + ", member2=" + member2 + ", member3=" + member3 + ", member4=" + member4 + ", member5=" + member5 + ", member6=" + member6 + ", member7=" + member7 + ", member8=" + member8 + ", member9=" + member9 + "]";
    }

    public static HashMap<String,Object> getHashMapRepresentation(Type3 type3)
    {
        HashMap<String,Object> newInstance   = new HashMap<String, Object>();
        
        newInstance.put("member1",  type3.member1);
        newInstance.put("member2",  type3.member2);
        newInstance.put("member3",  type3.member3);
        newInstance.put("member4",  type3.member4);
        newInstance.put("member5",  type3.member5);
        newInstance.put("member6",  type3.member6);
        newInstance.put("member7",  type3.member7);
        newInstance.put("member8",  type3.member8);
        newInstance.put("member9",  type3.member9);

        return newInstance;
    }
    
    public static Type3 createSampleType3Event()
    {
        int    member1 = 1;
        double member2 = 1;
        double member3 = 1;
        String member4 = "23/04/2015";
        float  member5 = 1;
        String member6 = "23/04/2015";
        float  member7 = 1;
        int    member8 = 1;
        double member9 = 1;

        Type3 newInstance   = new Type3(member1, member2, member3, member4, member5, member6, member7, member8, member9);

        return newInstance;
    }
    
    public static Type3 createSampleType3Event(int areaId)
    {
        Type3 newInstance = createSampleType3Event();
        
        newInstance.areaId = areaId;
        
        return newInstance;
    }

}
