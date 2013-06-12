/**
 * 
 * All rights reserved.
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 *
 */
package com.nimbler.tp.dbobject;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;

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

	public enum TRANSIT_MODE {
		UNDEFINED,
		WALK,
		TRANSIT,
		TRANSIT_WALK,
		BIKE,
		BIKE_TRANSIT
	}

	public enum USER_STATE{
		PUSH_DISABLED(-1),
		UNINSTALLED(-2),
		INVALID_TOKEN_FOR_PUSH(-3);
		int code;
		private USER_STATE(int code) {
			this.code = code;
		}
		public int code() {
			return code;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8231065322203477262L;
	private String id; 
	private transient String randomId; 
	private String deviceId;
	private int numberOfAlert;
	private String deviceToken;
	private String maxWalkDistance;

	private long lastAlertTime;
	private long lastReadTimeCaltrain;
	private long lastReadTimeBart;
	private long lastReadTimeAct;
	private long lastReadTimeSfMuni;
	private long lastReadTimeWmata;

	private long lastPushTime;//caltrain app
	private long lastPushTimeCaltrain;
	private long lastPushTimeBart;
	private long lastPushTimeAct;
	private long lastPushTimeSfMuni;

	private long createTime;
	private long updateTime;
	//1 if standard notification sound is enabled
	private int enableStdNotifSound;
	//1 if urgent notification sound is enabled
	private int enableUrgntNotifSound;
	private int appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal() ;//nimbler app type

	private String appVer;

	/* Nimbler 1.2 */

	private int enableSfMuniAdv = BOOLEAN_VAL.FALSE.ordinal();
	private int enableBartAdv = BOOLEAN_VAL.TRUE.ordinal();
	private int enableCaltrainAdv = BOOLEAN_VAL.TRUE.ordinal();
	private int enableAcTransitAdv = BOOLEAN_VAL.FALSE.ordinal();
	private Integer enableWmataAdv = BOOLEAN_VAL.TRUE.ordinal();

	private Long lastPushTimeWmata;


	private int transitMode;
	private double maxBikeDist;

	private double bikeTriangleFlat;
	private double bikeTriangleBikeFriendly;
	private double bikeTriangleQuick;

	/**
	 * Weekday Morning (5 - 10:00 am)
	 */
	private int notifTimingMorning = BOOLEAN_VAL.TRUE.ordinal();
	/**
	 * Weekday Midday (10 - 3:00pm)
	 */
	private int notifTimingMidday= BOOLEAN_VAL.FALSE.ordinal();
	/**
	 *  Weekday Evening peak (3 - 7:30pm)
	 */
	private int notifTimingEvening= BOOLEAN_VAL.TRUE.ordinal(); 

	/**
	 *  Weekday Night (7:30 - 12:00)
	 */
	private int notifTimingNight= BOOLEAN_VAL.FALSE.ordinal(); 
	/**
	 *   Weekend 
	 */
	private int notifTimingWeekend= BOOLEAN_VAL.FALSE.ordinal(); 

	public User() {

	}


	public long getLastPushTimeCaltrain() {
		return lastPushTimeCaltrain;
	}


	public void setLastPushTimeCaltrain(long lastPushTimeCaltrain) {
		this.lastPushTimeCaltrain = lastPushTimeCaltrain;
	}


	public Long getLastPushTimeWmata() {
		return lastPushTimeWmata;
	}


	public void setLastPushTimeWmata(Long lastPushTimeWmata) {
		this.lastPushTimeWmata = lastPushTimeWmata;
	}


	public Integer getEnableWmataAdv() {
		return enableWmataAdv;
	}

	public long getLastReadTimeWmata() {
		return lastReadTimeWmata;
	}


	public void setLastReadTimeWmata(long lastReadTimeWmata) {
		this.lastReadTimeWmata = lastReadTimeWmata;
	}


	public void setEnableWmataAdv(Integer enableWmataAdv) {
		this.enableWmataAdv = enableWmataAdv;
	}


	public double getBikeTriangleQuick() {
		return bikeTriangleQuick;
	}
	public void setBikeTriangleQuick(double bikeTriangleQuick) {
		this.bikeTriangleQuick = bikeTriangleQuick;
	}

	public long getLastPushTimeBart() {
		return lastPushTimeBart;
	}
	public void setLastPushTimeBart(long lastPushTimeBart) {
		this.lastPushTimeBart = lastPushTimeBart;
	}
	public long getLastPushTimeAct() {
		return lastPushTimeAct;
	}
	public void setLastPushTimeAct(long lastPushTimeAct) {
		this.lastPushTimeAct = lastPushTimeAct;
	}


	public long getLastPushTimeSfMuni() {
		return lastPushTimeSfMuni;
	}
	public void setLastPushTimeSfMuni(long lastPushTimeSfMuni) {
		this.lastPushTimeSfMuni = lastPushTimeSfMuni;
	}

	public int getNotifTimingMorning() {
		return notifTimingMorning;
	}


	public String getAppVer() {
		return appVer;
	}

	public void setAppVer(String appVer) {
		this.appVer = appVer;
	}

	public void setNotifTimingMorning(int notifTimingMorning) {
		this.notifTimingMorning = notifTimingMorning;
	}

	public String getRandomId() {
		return randomId;
	}
	public void setRandomId(String randomId) {
		this.randomId = randomId;
	}
	public int getNotifTimingMidday() {
		return notifTimingMidday;
	}
	public void setNotifTimingMidday(int notifTimingMidday) {
		this.notifTimingMidday = notifTimingMidday;
	}
	public int getNotifTimingEvening() {
		return notifTimingEvening;
	}


	public void setNotifTimingEvening(int notifTimingEvening) {
		this.notifTimingEvening = notifTimingEvening;
	}


	public int getNotifTimingNight() {
		return notifTimingNight;
	}


	public void setNotifTimingNight(int notifTimingNight) {
		this.notifTimingNight = notifTimingNight;
	}


	public int getNotifTimingWeekend() {
		return notifTimingWeekend;
	}


	public void setNotifTimingWeekend(int notifTimingWeekend) {
		this.notifTimingWeekend = notifTimingWeekend;
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
		return "User [id=" + id + ", deviceId=" + deviceId + ", numberOfAlert="
				+ numberOfAlert + ", deviceToken=" + deviceToken
				+ ", maxWalkDistance=" + maxWalkDistance + ", lastAlertTime="
				+ lastAlertTime + ", lastPushTime=" + lastPushTime
				+ ", lastPushTimeCaltrain=" + lastPushTimeCaltrain
				+ ", lastPushTimeBart=" + lastPushTimeBart
				+ ", lastPushTimeAct=" + lastPushTimeAct
				+ ", lastPushTimeSfMuni=" + lastPushTimeSfMuni
				+ ", createTime=" + createTime + ", updateTime=" + updateTime
				+ ", enableStdNotifSound=" + enableStdNotifSound
				+ ", enableUrgntNotifSound=" + enableUrgntNotifSound
				+ ", appType=" + appType + ", enableSfMuniAdv="
				+ enableSfMuniAdv + ", enableBartAdv=" + enableBartAdv
				+ ", enableCaltrainAdv=" + enableCaltrainAdv
				+ ", enableAcTransitAdv=" + enableAcTransitAdv
				+ ", transitMode=" + transitMode + ", maxBikeDist="
				+ maxBikeDist + ", bikeTriangleFlat=" + bikeTriangleFlat
				+ ", bikeTriangleBikeFriendly=" + bikeTriangleBikeFriendly
				+ ", bikeTriangleQuick=" + bikeTriangleQuick
				+ ", notifTimingMorning=" + notifTimingMorning
				+ ", notifTimingMidday=" + notifTimingMidday
				+ ", notifTimingEvening=" + notifTimingEvening
				+ ", notifTimingNight=" + notifTimingNight
				+ ", notifTimingWeekend=" + notifTimingWeekend + "]";
	}

	public int getEnableSfMuniAdv() {
		return enableSfMuniAdv;
	}

	public void setEnableSfMuniAdv(int enableSfMuniAdv) {
		this.enableSfMuniAdv = enableSfMuniAdv;
	}

	public int getEnableBartAdv() {
		return enableBartAdv;
	}

	public void setEnableBartAdv(int enableBartAdv) {
		this.enableBartAdv = enableBartAdv;
	}

	public int getEnableCaltrainAdv() {
		return enableCaltrainAdv;
	}

	public void setEnableCaltrainAdv(int enableCaltrainAdv) {
		this.enableCaltrainAdv = enableCaltrainAdv;
	}

	public int getEnableAcTransitAdv() {
		return enableAcTransitAdv;
	}

	public void setEnableAcTransitAdv(int enableAcTransitAdv) {
		this.enableAcTransitAdv = enableAcTransitAdv;
	}

	public int getTransitMode() {
		return transitMode;
	}

	public void setTransitMode(int transitMode) {
		this.transitMode = transitMode;
	}

	public double getMaxBikeDist() {
		return maxBikeDist;
	}

	public void setMaxBikeDist(double maxBikeDist) {
		this.maxBikeDist = maxBikeDist;
	}

	public double getBikeTriangleFlat() {
		return bikeTriangleFlat;
	}

	public void setBikeTriangleFlat(double bikeTriangleFlat) {
		this.bikeTriangleFlat = bikeTriangleFlat;
	}

	public double getBikeTriangleBikeFriendly() {
		return bikeTriangleBikeFriendly;
	}

	public void setBikeTriangleBikeFriendly(double bikeTriangleBikeFriendly) {
		this.bikeTriangleBikeFriendly = bikeTriangleBikeFriendly;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	/*public long getLastPushTimeCaltrain() {
		return lastPushTimeCaltrain;
	}*/

	/*public void setLastPushTimeCaltrain(long lastPushTimeCaltrain) {
		this.lastPushTimeCaltrain = lastPushTimeCaltrain;
	}*/

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public long getLastReadTimeCaltrain() {
		return lastReadTimeCaltrain;
	}



	public void setLastReadTimeCaltrain(long lastReadTimeCaltrain) {
		this.lastReadTimeCaltrain = lastReadTimeCaltrain;
	}



	public long getLastReadTimeBart() {
		return lastReadTimeBart;
	}



	public void setLastReadTimeBart(long lastReadTimeBart) {
		this.lastReadTimeBart = lastReadTimeBart;
	}



	public long getLastReadTimeAct() {
		return lastReadTimeAct;
	}



	public void setLastReadTimeAct(long lastReadTimeAct) {
		this.lastReadTimeAct = lastReadTimeAct;
	}



	public long getLastReadTimeSfMuni() {
		return lastReadTimeSfMuni;
	}



	public void setLastReadTimeSfMuni(long lastReadTimeSfMuni) {
		this.lastReadTimeSfMuni = lastReadTimeSfMuni;
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

	public int getAppType() {
		return appType;
	}

	public void setAppType(int appType) {
		this.appType = appType;
	}
}