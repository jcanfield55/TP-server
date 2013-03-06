package com.nimbler.tp.dbobject;

import java.io.Serializable;

public class AdvisoryFetchLog implements Serializable{

	public enum ADVISORY_FETCH_EVENT {
		UNDEFINED,
		ALL,
		COUNT,
		LATEST
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2126361830006687219L;
	private String id;
	private String deviceToken;
	private String deviceId;
	private long createTime;
	private int appType ;
	private int type ;
	private String appVersion ;
	private Long lastTweetTime ;
	private String agencyIds;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDeviceToken() {
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}
	public String getDeviceId() {
		return deviceId;
	}

	public long getCreateTime() {
		return createTime;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public int getAppType() {
		return appType;
	}
	public void setAppType(int appType) {
		this.appType = appType;
	}
	public String getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public Long getLastTweetTime() {
		return lastTweetTime;
	}
	public void setLastTweetTime(Long lastTweetTime) {
		this.lastTweetTime = lastTweetTime;
	}
	public String getAgencyIds() {
		return agencyIds;
	}
	public void setAgencyIds(String agencyIds) {
		this.agencyIds = agencyIds;
	}
	@Override
	public String toString() {
		return "AdvisoryFetchLog [id=" + id + ", deviceToken=" + deviceToken
				+ ", deviceId=" + deviceId + ", createTime=" + createTime
				+ ", appType=" + appType + ", appVersion=" + appVersion
				+ ", lastTweetTime=" + lastTweetTime + ", agencyIds="
				+ agencyIds + "]";
	}

}
