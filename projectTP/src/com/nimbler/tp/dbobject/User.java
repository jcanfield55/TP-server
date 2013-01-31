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

	/**
	 * 
	 */
	private static final long serialVersionUID = -8231065322203477262L;
	private String id;
	private String deviceId;
	private int numberOfAlert;
	private String deviceToken;
	private String maxWalkDistance;

	private Long createTime;
	private Long updateTime;
	//1 if standard notification sound is enabled
	private Integer enableStdNotifSound;
	//1 if urgent notification sound is enabled
	private Integer enableUrgntNotifSound;
	private Integer appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal() ;//nimbler app type

	/* Nimbler 1.2 */
	//	=====================================  optional   ============================================

	private Long lastAlertTime;
	private Long lastReadTimeCaltrain;
	private Long lastReadTimeBart;
	private Long lastReadTimeAct;
	private Long lastReadTimeSfMuni;
	private Long lastReadTimeWmata;

	private Long lastPushTime;//caltrain app
	private Long lastPushTimeCaltrain;
	private Long lastPushTimeBart;
	private Long lastPushTimeAct;
	private Long lastPushTimeSfMuni;

	private Long lastPushTimeWmata;

	private Integer enableSfMuniAdv = BOOLEAN_VAL.FALSE.ordinal();
	private Integer enableBartAdv = BOOLEAN_VAL.TRUE.ordinal();
	private Integer enableCaltrainAdv = BOOLEAN_VAL.TRUE.ordinal();
	private Integer enableAcTransitAdv = BOOLEAN_VAL.FALSE.ordinal();

	private Integer enableWmataAdv = BOOLEAN_VAL.FALSE.ordinal();
	//	=====================================  optional end  ============================================
	private Integer transitMode;
	private Double maxBikeDist;

	private Double bikeTriangleFlat;
	private Double bikeTriangleBikeFriendly;
	private Double bikeTriangleQuick;

	/**
	 * Weekday Morning (5 - 10:00 am)
	 */
	private Integer notifTimingMorning = BOOLEAN_VAL.TRUE.ordinal();
	/**
	 * Weekday Midday (10 - 3:00pm)
	 */
	private Integer notifTimingMidday= BOOLEAN_VAL.FALSE.ordinal();
	/**
	 *  Weekday Evening peak (3 - 7:30pm)
	 */
	private Integer notifTimingEvening= BOOLEAN_VAL.TRUE.ordinal();

	/**
	 *  Weekday Night (7:30 - 12:00)
	 */
	private Integer notifTimingNight= BOOLEAN_VAL.FALSE.ordinal();
	/**
	 *   Weekend
	 */
	private Integer notifTimingWeekend= BOOLEAN_VAL.FALSE.ordinal();

	public User() {

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
	public int getNumberOfAlert() {
		return numberOfAlert;
	}
	public void setNumberOfAlert(int numberOfAlert) {
		this.numberOfAlert = numberOfAlert;
	}
	public String getDeviceToken() {
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}
	public String getMaxWalkDistance() {
		return maxWalkDistance;
	}
	public void setMaxWalkDistance(String maxWalkDistance) {
		this.maxWalkDistance = maxWalkDistance;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	public Long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}
	public Integer getEnableStdNotifSound() {
		return enableStdNotifSound;
	}
	public void setEnableStdNotifSound(Integer enableStdNotifSound) {
		this.enableStdNotifSound = enableStdNotifSound;
	}
	public Integer getEnableUrgntNotifSound() {
		return enableUrgntNotifSound;
	}
	public void setEnableUrgntNotifSound(Integer enableUrgntNotifSound) {
		this.enableUrgntNotifSound = enableUrgntNotifSound;
	}
	public Integer getAppType() {
		return appType;
	}
	public void setAppType(Integer appType) {
		this.appType = appType;
	}
	public Long getLastAlertTime() {
		return lastAlertTime;
	}
	public void setLastAlertTime(Long lastAlertTime) {
		this.lastAlertTime = lastAlertTime;
	}
	public Long getLastReadTimeCaltrain() {
		return lastReadTimeCaltrain;
	}
	public void setLastReadTimeCaltrain(Long lastReadTimeCaltrain) {
		this.lastReadTimeCaltrain = lastReadTimeCaltrain;
	}
	public Long getLastReadTimeBart() {
		return lastReadTimeBart;
	}
	public void setLastReadTimeBart(Long lastReadTimeBart) {
		this.lastReadTimeBart = lastReadTimeBart;
	}
	public Long getLastReadTimeAct() {
		return lastReadTimeAct;
	}
	public void setLastReadTimeAct(Long lastReadTimeAct) {
		this.lastReadTimeAct = lastReadTimeAct;
	}
	public Long getLastReadTimeSfMuni() {
		return lastReadTimeSfMuni;
	}
	public void setLastReadTimeSfMuni(Long lastReadTimeSfMuni) {
		this.lastReadTimeSfMuni = lastReadTimeSfMuni;
	}
	public Long getLastReadTimeWmata() {
		return lastReadTimeWmata;
	}
	public void setLastReadTimeWmata(Long lastReadTimeWmata) {
		this.lastReadTimeWmata = lastReadTimeWmata;
	}
	public Long getLastPushTime() {
		return lastPushTime;
	}
	public void setLastPushTime(Long lastPushTime) {
		this.lastPushTime = lastPushTime;
	}
	public Long getLastPushTimeCaltrain() {
		return lastPushTimeCaltrain;
	}
	public void setLastPushTimeCaltrain(Long lastPushTimeCaltrain) {
		this.lastPushTimeCaltrain = lastPushTimeCaltrain;
	}
	public Long getLastPushTimeBart() {
		return lastPushTimeBart;
	}
	public void setLastPushTimeBart(Long lastPushTimeBart) {
		this.lastPushTimeBart = lastPushTimeBart;
	}
	public Long getLastPushTimeAct() {
		return lastPushTimeAct;
	}
	public void setLastPushTimeAct(Long lastPushTimeAct) {
		this.lastPushTimeAct = lastPushTimeAct;
	}
	public Long getLastPushTimeSfMuni() {
		return lastPushTimeSfMuni;
	}
	public void setLastPushTimeSfMuni(Long lastPushTimeSfMuni) {
		this.lastPushTimeSfMuni = lastPushTimeSfMuni;
	}
	public Long getLastPushTimeWmata() {
		return lastPushTimeWmata;
	}
	public void setLastPushTimeWmata(Long lastPushTimeWmata) {
		this.lastPushTimeWmata = lastPushTimeWmata;
	}
	public Integer getEnableSfMuniAdv() {
		return enableSfMuniAdv;
	}
	public void setEnableSfMuniAdv(Integer enableSfMuniAdv) {
		this.enableSfMuniAdv = enableSfMuniAdv;
	}
	public Integer getEnableBartAdv() {
		return enableBartAdv;
	}
	public void setEnableBartAdv(Integer enableBartAdv) {
		this.enableBartAdv = enableBartAdv;
	}
	public Integer getEnableCaltrainAdv() {
		return enableCaltrainAdv;
	}
	public void setEnableCaltrainAdv(Integer enableCaltrainAdv) {
		this.enableCaltrainAdv = enableCaltrainAdv;
	}
	public Integer getEnableAcTransitAdv() {
		return enableAcTransitAdv;
	}
	public void setEnableAcTransitAdv(Integer enableAcTransitAdv) {
		this.enableAcTransitAdv = enableAcTransitAdv;
	}
	public Integer getEnableWmataAdv() {
		return enableWmataAdv;
	}
	public void setEnableWmataAdv(Integer enableWmataAdv) {
		this.enableWmataAdv = enableWmataAdv;
	}
	public Integer getTransitMode() {
		return transitMode;
	}
	public void setTransitMode(Integer transitMode) {
		this.transitMode = transitMode;
	}
	public Double getMaxBikeDist() {
		return maxBikeDist;
	}
	public void setMaxBikeDist(Double maxBikeDist) {
		this.maxBikeDist = maxBikeDist;
	}
	public Double getBikeTriangleFlat() {
		return bikeTriangleFlat;
	}
	public void setBikeTriangleFlat(Double bikeTriangleFlat) {
		this.bikeTriangleFlat = bikeTriangleFlat;
	}
	public Double getBikeTriangleBikeFriendly() {
		return bikeTriangleBikeFriendly;
	}
	public void setBikeTriangleBikeFriendly(Double bikeTriangleBikeFriendly) {
		this.bikeTriangleBikeFriendly = bikeTriangleBikeFriendly;
	}
	public Double getBikeTriangleQuick() {
		return bikeTriangleQuick;
	}
	public void setBikeTriangleQuick(Double bikeTriangleQuick) {
		this.bikeTriangleQuick = bikeTriangleQuick;
	}
	public Integer getNotifTimingMorning() {
		return notifTimingMorning;
	}
	public void setNotifTimingMorning(Integer notifTimingMorning) {
		this.notifTimingMorning = notifTimingMorning;
	}
	public Integer getNotifTimingMidday() {
		return notifTimingMidday;
	}
	public void setNotifTimingMidday(Integer notifTimingMidday) {
		this.notifTimingMidday = notifTimingMidday;
	}
	public Integer getNotifTimingEvening() {
		return notifTimingEvening;
	}
	public void setNotifTimingEvening(Integer notifTimingEvening) {
		this.notifTimingEvening = notifTimingEvening;
	}
	public Integer getNotifTimingNight() {
		return notifTimingNight;
	}
	public void setNotifTimingNight(Integer notifTimingNight) {
		this.notifTimingNight = notifTimingNight;
	}
	public Integer getNotifTimingWeekend() {
		return notifTimingWeekend;
	}
	public void setNotifTimingWeekend(Integer notifTimingWeekend) {
		this.notifTimingWeekend = notifTimingWeekend;
	}


}