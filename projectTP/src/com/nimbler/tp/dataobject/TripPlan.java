/*
 * 
 */

package com.nimbler.tp.dataobject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;

/**
 * A TripPlan is a set of ways to get from point A to point B at time T.
 */
public class TripPlan implements Cloneable,Serializable{

	/**
	 * Primary key.
	 */
	private String id;
	/**
	 * IPhone device Id for referencing user.
	 */
	private String deviceId;

	private long createTime;

	/** 
	 * The time and date of travel 
	 */
	private long date = 0;
	/**
	 * The origin
	 */
	private Place from = null;
	/**
	 * The destination
	 */
	private Place to = null;

	/** 
	 * A list of possible itineraries. 
	 */
	private List<Itinerary> itineraries = new ArrayList<Itinerary>();

	private String planUrlParams;//paramaters

	private Map<String, String> requestParameters;

	private int appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal();
	/**
	 * Actual url that was called
	 */
	private String requestUrl;

	public TripPlan() {}

	//	public TripPlan(Place from, Place to, Date date) {
	//		this.from = from;
	//		this.to = to;
	//		this.date = date;
	//	}

	public long getDate() {
		return date;
	}

	//	public void setDate(Date date) {
	//		this.date = date;
	//	}
	public void setDate(long time) {
		this.date =time;
	}

	public String getPlanUrlParams() {
		return planUrlParams;
	}

	public void setPlanUrlParams(String planUrlParams) {
		this.planUrlParams = planUrlParams;
	}

	public Place getFrom() {
		return from;
	}

	public void setFrom(Place from) {
		this.from = from;
	}

	public Place getTo() {
		return to;
	}

	public void setTo(Place to) {
		this.to = to;
	}

	public List<Itinerary> getItineraries() {
		return itineraries;
	}

	public void setItineraries(List<Itinerary> itineraries) {
		this.itineraries = itineraries;
	}

	public void addItinerary(Itinerary itinerary) {
		this.itineraries.add(itinerary);
	}

	@Override
	public String toString() {
		return "TripPlan [date=" + date + ", from=" + from + ", to=" + to
				+ ", itineraries=" + itineraries + "]";
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
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

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public int getAppType() {
		return appType;
	}

	public void setAppType(int appType) {
		if(appType==0)
			return;
		this.appType = appType;
	}

	public Map<String, String> getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(Map<String, String> requestParameters) {
		this.requestParameters = requestParameters;
	}

}
