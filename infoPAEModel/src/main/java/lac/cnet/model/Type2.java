package lac.cnet.model;

import java.io.Serializable;
import java.util.HashMap;

public class Type2 implements Serializable
{
    //private static final long serialVersionUID = 5394414049324921630L;
    private static final long serialVersionUID = 205L;
    
    public int    member1  = 0;
    public double member2  = 0;
    public double member3  = 0;
    public String member4  = null;
    public float  member5  = 0;
    public int    member6  = 0;
    public double member7  = 0;
    public double member8  = 0;
    public String member9  = null;
    public float  member10 = 0;
    
    public int     areaId;
        
    public Type2()
    {
        super();
    }
    
    public Type2(int member1, double member2, double member3, String member4, float member5, int member6, double member7, double member8, String member9, float member10)
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
        this.member10 = member10;
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
    public int getMember6()
    {
        return member6;
    }
    public void setMember6(int member6)
    {
        this.member6 = member6;
    }
    public double getMember7()
    {
        return member7;
    }
    public void setMember7(double member7)
    {
        this.member7 = member7;
    }
    public double getMember8()
    {
        return member8;
    }
    public void setMember8(double member8)
    {
        this.member8 = member8;
    }
    public String getMember9()
    {
        return member9;
    }
    public void setMember9(String member9)
    {
        this.member9 = member9;
    }
    public float getMember10()
    {
        return member10;
    }
    public void setMember10(float member10)
    {
        this.member10 = member10;
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
        return "Type2 [member1=" + member1 + ", member2=" + member2 + ", member3=" + member3 + ", member4=" + member4 + ", member5=" + member5 + ", member6=" + member6 + ", member7=" + member7 + ", member8=" + member8 + ", member9=" + member9 + ", member10=" + member10 + "]";
    }
    
    public static HashMap<String,Object> getHashMapRepresentation(Type2 type2)
    {
        HashMap<String,Object> newInstance   = new HashMap<String, Object>();
        
        newInstance.put("member1",  type2.member1);
        newInstance.put("member2",  type2.member2);
        newInstance.put("member3",  type2.member3);
        newInstance.put("member4",  type2.member4);
        newInstance.put("member5",  type2.member5);
        newInstance.put("member6",  type2.member6);
        newInstance.put("member7",  type2.member7);
        newInstance.put("member8",  type2.member8);
        newInstance.put("member9",  type2.member9);
        newInstance.put("member10", type2.member10);

        return newInstance;
    }
    
    public static Type2 createSampleType2Event()
    {
        int     member1  = 1;
        double  member2  = 1;
        double  member3  = 1;
        String  member4  = "23/04/2015";
        float   member5  = 1;
        int     member6  = 1;
        double  member7  = 1;
        double  member8  = 1;
        String  member9  = "23/04/2015";
        float   member10 = 1;

        Type2 newInstance = new Type2(member1, member2, member3, member4, member5, member6, member7, member8, member9, member10);
        
        return newInstance;
    }
    
    public static Type2 createSampleType2Event(int areaId)
    {
        Type2 newInstance = createSampleType2Event();
        
        newInstance.areaId = areaId;
        
        return newInstance;
    }

}
