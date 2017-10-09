package com.infopae.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;


import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SDDLLocation implements Serializable {

	private static final long serialVersionUID = 199L;

	private String  uuid;
	private double  latitude;
	private double  longitude;
	private Date    datetime;
	private float   accuracy;
	private String  provider;
	private float   speed;
	private float   bearing;
	private double  altitude;
	private String  connectionType;
	private int     batteryPercent;
	private boolean isCharging;
	private String  content;

	public boolean isCharging() {
		return isCharging;
	}

	public void setCharging(boolean isCharging) {
		this.isCharging = isCharging;
	}

	public int getBatteryPercent() {
		return batteryPercent;
	}

	public void setBatteryPercent(int batteryPercent) {
		this.batteryPercent = batteryPercent;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public Date getDatetime() {
		return datetime;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getBearing() {
		return bearing;
	}

	public void setBearing(float bearing) {
		this.bearing = bearing;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public String getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {

		return "SDDLLocation [uuid=" + uuid + ", latitude=" + latitude
				+ ", longitude=" + longitude + ", datetime=" + datetime + ", accuracy="
				+ accuracy + ", provider=" + provider + ", speed=" + speed
				+ ", bearing=" + bearing + ", altitude=" + altitude
				+ ", connectionType=" + connectionType
				+ ", batteryPercent=" + batteryPercent
				+ ", isCharging=" + isCharging
				+ ", content=" + content
				+ "]";
	}

	public void constructJSON() {
		File jfile = new File("c:\\user.json"); //this will be done on the client, so it will have to be done some other way
		JsonGenerator jGenerator;
		JsonFactory jfactory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper();

		try {
			jGenerator = jfactory.createGenerator(jfile, JsonEncoding.UTF8);
			mapper.writeValue(jGenerator, getUuid());
			mapper.writeValue(jGenerator, getLatitude());
			mapper.writeValue(jGenerator, getLongitude());
			mapper.writeValue(jGenerator, getAccuracy());
			mapper.writeValue(jGenerator, getDatetime());
			mapper.writeValue(jGenerator, getBearing());
			mapper.writeValue(jGenerator, getProvider());
			mapper.writeValue(jGenerator, getSpeed());
			mapper.writeValue(jGenerator, getAltitude());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String JSONTest()
	{
		File jfile = new File("c:\\user.json"); //this will be done on the client, so it will have to be done some other way
		JsonFactory jfactory = new JsonFactory();
		JsonGenerator jGenerator;
		JsonParser jParser;
		ObjectMapper mapper = new ObjectMapper();
		String returnValue = "";

		try {
			jGenerator = jfactory.createGenerator(jfile, JsonEncoding.UTF8);
			jParser = jfactory.createParser(jfile);

			mapper.writeValue(jGenerator, "oi");
			returnValue = mapper.readValue(jParser, String.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return returnValue;
	}
}
