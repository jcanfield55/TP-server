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
	private static final boolean DEFAULT_SOUND_FOR_STANDARD_NOTIFICATION =false;
	private static final boolean DEFAULT_SOUND_FOR_URGENT_NOTIFICATION =true;
	public enum BOOLEAN_VAL{
		UNDEFINED,
		TRUE,
		FALSE
	}

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

	private long createTime;
	private long updateTime;
	//1 if standard notification sound is enabled
	private int enableStdNotifSound;
	//1 if urgent notification sound is enabled
	private int enableUrgntNotifSound;


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
	
	public int getEnableStdNotifSound() {
		return enableStdNotifSound;
	}

	public void setEnableStdNotifSound(int enableStdNotifSound) {
		this.enableStdNotifSound = enableStdNotifSound;
	}

	public int getEnableUrgntNotifSound() {
		return enableUrgntNotifSound;
	}

	public void setEnableUrgntNotifSound(int enableUrgntNotifSound) {
		this.enableUrgntNotifSound = enableUrgntNotifSound;
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

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * Checks if is standard notification enable.
	 *
	 * @return true, if is standard notification enable
	 */
	public boolean isStandardNotifSoundEnable() {  
		if(enableStdNotifSound == BOOLEAN_VAL.TRUE.ordinal())
			return true;
		else if(enableStdNotifSound == BOOLEAN_VAL.FALSE.ordinal())
			return false;
		else
			return DEFAULT_SOUND_FOR_STANDARD_NOTIFICATION;

	}

	/**
	 * Checks if is urgent notification enable.
	 *
	 * @return true, if is urgent notification enable
	 */
	public boolean isUrgentNotifSoundEnable() {
		if(enableUrgntNotifSound == BOOLEAN_VAL.TRUE.ordinal())
			return true;
		else if(enableUrgntNotifSound == BOOLEAN_VAL.FALSE.ordinal())
			return false;
		else
			return DEFAULT_SOUND_FOR_URGENT_NOTIFICATION;
	}
}