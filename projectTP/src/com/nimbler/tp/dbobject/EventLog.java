/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.dbobject;

import java.io.Serializable;
/**
 * 
 * @author nirmal
 *
 */
public class EventLog implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 245575327919021671L;
	public enum EVENT_TYPE{
		UNDEFINED,
		GENERATE_PLAN,
		SAVE_PLAN_WITH_GEO
	}
	public enum TO_FROM_TYPE{
		UNDEFINED,
		GEO,
		REVERSE_GEO,
		PREDEFINED_LIST
	}
	private String id;
	private int eventType;
	private long cerateTime;
	private String  deviceId;

	private String frmtdAddrTo;
	private String rawAddrTo;

	private String frmtdAddrFrom;
	private String rawAddrFrom;

	private Double latTo;
	private Double lonTo;

	private Double latFrom;
	private Double lonFrom;

	private String geoRespFrom;
	private String geoRespTo;

	private String planId;
	private int fromType;
	private int toType;

	private double timeFrom;
	private double timeTo;
	private double timeTripPlan;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public long getCerateTime() {
		return cerateTime;
	}

	public void setCerateTime(long cerateTime) {
		this.cerateTime = cerateTime;
	}

	public String getDeviceId() {
		return deviceId;
	}


	public void setTimeTripPlan(long timeTripPlan) {
		this.timeTripPlan = timeTripPlan;
	}

	public int getFromType() {
		return fromType;
	}

	public void setFromType(int fromType) {
		this.fromType = fromType;
	}

	public int getToType() {
		return toType;
	}

	public void setToType(int toType) {
		this.toType = toType;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getFrmtdAddrTo() {
		return frmtdAddrTo;
	}

	public void setFrmtdAddrTo(String frmtdAddrTo) {
		this.frmtdAddrTo = frmtdAddrTo;
	}

	public String getRawAddrTo() {
		return rawAddrTo;
	}

	public void setRawAddrTo(String rawAddrTo) {
		this.rawAddrTo = rawAddrTo;
	}

	public String getFrmtdAddrFrom() {
		return frmtdAddrFrom;
	}

	public void setFrmtdAddrFrom(String frmtdAddrFrom) {
		this.frmtdAddrFrom = frmtdAddrFrom;
	}

	public String getRawAddrFrom() {
		return rawAddrFrom;
	}

	public void setRawAddrFrom(String rawAddrFrom) {
		this.rawAddrFrom = rawAddrFrom;
	}

	public Double getLatTo() {
		return latTo;
	}

	public void setLatTo(Double latTo) {
		this.latTo = latTo;
	}

	public Double getLonTo() {
		return lonTo;
	}

	public void setLonTo(Double lonTo) {
		this.lonTo = lonTo;
	}

	public Double getLatFrom() {
		return latFrom;
	}

	public void setLatFrom(Double latFrom) {
		this.latFrom = latFrom;
	}

	public Double getLonFrom() {
		return lonFrom;
	}

	public void setLonFrom(Double lonFrom) {
		this.lonFrom = lonFrom;
	}

	public String getGeoRespFrom() {
		return geoRespFrom;
	}

	public void setGeoRespFrom(String geoRespFrom) {
		this.geoRespFrom = geoRespFrom;
	}

	public String getGeoRespTo() {
		return geoRespTo;
	}

	public void setGeoRespTo(String geoRespTo) {
		this.geoRespTo = geoRespTo;
	}

	public String getPlanId() {
		return planId;
	}

	public void setPlanId(String planId) {
		this.planId = planId;
	}
	public int getEventType() {
		return eventType;
	}

	public void setEventType(int eventType) {
		this.eventType = eventType;
	}

	public double getTimeFrom() {
		return timeFrom;
	}

	public void setTimeFrom(double timeFrom) {
		this.timeFrom = timeFrom;
	}

	public double getTimeTo() {
		return timeTo;
	}

	public void setTimeTo(double timeTo) {
		this.timeTo = timeTo;
	}

	public double getTimeTripPlan() {
		return timeTripPlan;
	}

	public void setTimeTripPlan(double timeTripPlan) {
		this.timeTripPlan = timeTripPlan;
	}

	@Override
	public String toString() {
		return "EventLog [id=" + id + ", eventType=" + eventType
				+ ", cerateTime=" + cerateTime + ", deviceId=" + deviceId
				+ ", frmtdAddrTo=" + frmtdAddrTo + ", rawAddrTo=" + rawAddrTo
				+ ", frmtdAddrFrom=" + frmtdAddrFrom + ", rawAddrFrom="
				+ rawAddrFrom + ", latTo=" + latTo + ", lonTo=" + lonTo
				+ ", latFrom=" + latFrom + ", lonFrom=" + lonFrom
				+ ", geoRespFrom=" + geoRespFrom + ", geoRespTo=" + geoRespTo
				+ ", planId=" + planId + ", fromType=" + fromType + ", toType="
				+ toType + ", timeFrom=" + timeFrom + ", timeTo=" + timeTo
				+ ", timeTripPlan=" + timeTripPlan + "]";
	}
}