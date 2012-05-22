package com.apprika.otp.dbobject;

import java.io.Serializable;

public class Trip implements Serializable{
	/**
	 * 
	 */
	public enum TRIP_TYPE{
		UNDEFINED,
		WHOLE_TRIP,
		ITINERARY,
		LEG
	}
	private static final long serialVersionUID = -3929872578211356793L;
	private String id;
	private String tripDef;
	private int type;


	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTripDef() {
		return tripDef;
	}
	public void setTripDef(String tripDef) {
		this.tripDef = tripDef;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "Trip [id=" + id + ", tripDef=" + tripDef + ", type=" + type
				+ "]";
	}
}
