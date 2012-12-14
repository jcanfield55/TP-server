/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.dbobject;

import java.io.Serializable;

import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;

/**
 * The Class FeedBack.
 * @author nirmal
 */
public class FeedBack implements Serializable {

	public enum FEEDBACK_FORMAT_TYPE{
		UNDEFINED,
		TEXT,
		AUDIO,
		TEXT_AUDIO;
	}
	public static enum FEEDBACK_SOURCE_TYPE {
		DEFAULT,
		PLAN,
		ITINERARY,
		LEG,
		OVERVIEW
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -1989283767936507352L;
	private String id;
	private String deviceId;
	private String emailId;
	private int source;
	private int formatType;
	private String fbText;
	private String audioFileUrl;
	private float rating;	
	private long createTime;
	private String date;

	private String planID;
	private String itineraryID;
	private String legID;
	private String addFrom;
	private String addTo;
	private int appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal();

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getAddFrom() {
		return addFrom;
	}
	public void setAddFrom(String addFrom) {
		this.addFrom = addFrom;
	}
	public String getAddTo() {
		return addTo;
	}
	public void setAddTo(String addTo) {
		this.addTo = addTo;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public int getFormatType() {
		return formatType;
	}
	public void setFormatType(int formatType) {
		this.formatType = formatType;
	}
	public float getRating() {
		return rating;
	}
	public String getAudioFileUrl() {
		return audioFileUrl;
	}
	public void setAudioFileUrl(String audioFileUrl) {
		this.audioFileUrl = audioFileUrl;
	}
	public void setRating(float rating) {
		this.rating = rating;
	}
	public String getFbText() {
		return fbText;
	}
	public void setFbText(String fbText) {
		this.fbText = fbText;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public String getPlanID() {
		return planID;
	}
	public void setPlanID(String planID) {
		this.planID = planID;
	}
	public String getItineraryID() {
		return itineraryID;
	}
	public void setItineraryID(String itineraryID) {
		this.itineraryID = itineraryID;
	}
	public String getLegID() {
		return legID;
	}
	public void setLegID(String legID) {
		this.legID = legID;
	}
	@Override
	public String toString() {
		return "FeedBack [id=" + id + ", deviceId=" + deviceId + ", emailId="
				+ emailId + ", source=" + source + ", formatType=" + formatType
				+ ", fbText=" + fbText + ", audioFileUrl=" + audioFileUrl
				+ ", rating=" + rating + ", createTime=" + createTime
				+ ", date=" + date + ", planID=" + planID + ", itineraryID="
				+ itineraryID + ", legID=" + legID + ", addFrom=" + addFrom
				+ ", addTo=" + addTo + ", appType=" + appType + "]";
	}
	public int getAppType() {
		return appType;
	}
	public void setAppType(int at) {
		if(at==0)
			return;
		this.appType = at;
	}


}