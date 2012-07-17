/**
 * 
 * All rights reserved.
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 *
 */
package com.nimbler.tp.dbobject;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class User.
 *
 * @author nirmal
 */
@XmlRootElement
public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8231065322203477262L;
	private String id;	
	private String deviceId;
	private int numberOfAlert;
	private String deviceToken;
	private String maxWalkDistance;
	private long lastAlertTime;
	private long lastPushTime;

	public User() {

	}
	
	public long getLastPushTime() {
		return lastPushTime;
	}

	public void setLastPushTime(long lastPushTime) {
		this.lastPushTime = lastPushTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public User(String deviceId) {
		this.deviceId = deviceId;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setNumberOfAlert(int numberOfAlert) {
		this.numberOfAlert = numberOfAlert;
	}

	public int getNumberOfAlert() {
		return numberOfAlert;
	}

	public void setMaxWalkDistance(String maxWalkDistance) {
		this.maxWalkDistance = maxWalkDistance;
	}

	public String getMaxWalkDistance() {
		return maxWalkDistance;
	}

	public void setLastAlertTime(long lastAlertTime) {
		this.lastAlertTime = lastAlertTime;
	}

	public long getLastAlertTime() {
		return lastAlertTime;
	}

	@Override
	public String toString() {
		return "User [deviceId=" + deviceId + ", deviceToken=" + deviceToken
		+ ", id=" + id + ", lastAlertTime=" + lastAlertTime
		+ ", maxWalkDistance=" + maxWalkDistance + ", numberOfAlert="
		+ numberOfAlert + "]";
	}


}
