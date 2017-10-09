package com.infopae.model;

import java.io.Serializable;

public class SDDLObject implements Serializable {
	
	private static final long serialVersionUID = 43797L;
	private Object packageMessage;
	private String appPackageName;
	
	public SDDLObject()
	{
		packageMessage = null;
		appPackageName = "";
	}
	
	public void setPackageMessage(Object object){
		this.packageMessage = object;
	}
	
	public Object getPackageMessage(){
		return this.packageMessage;
	}
	
	public String getAppPackageName(){
		return this.appPackageName;
	}
	
	public void setAppPackageName(String name){
		this.appPackageName = name;
	}

}
