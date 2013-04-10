/*
 * 
 */
package com.nimbler.tp.dataobject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class TripResponse implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6801948599073308011L;
	private TripPlan plan;
	private PlannerError error = null;
	private Map requestParameters;
	private Long planGenerateTime = 0l;
	private List<Itinerary> lstItineraries = null;

	public TripResponse(TripPlan plan) {
		this.plan = plan;
	}
	public Map getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(Map requestParameters) {
		this.requestParameters = requestParameters;
	}

	public TripPlan getPlan() {
		return plan;
	}

	public void setPlan(TripPlan plan) {
		this.plan = plan;
	}

	public PlannerError getError() {
		return error;
	}

	public void setError(PlannerError error) {
		this.error = error;
	}
	public Object getRuestParameter(String key){
		if(requestParameters !=null)
			return  requestParameters.get(key);
		return null;
	}
	public Long getPlanGenerateTime() {
		return planGenerateTime;
	}
	public void setPlanGenerateTime(long planGenerateTime) {
		this.planGenerateTime = planGenerateTime;
	}

	public TripResponse() {
	}
	public List<Itinerary> getLstItineraries() {
		return lstItineraries;
	}
	public void setLstItineraries(List<Itinerary> lstItineraries) {
		this.lstItineraries = lstItineraries;
	}
	public void setPlanGenerateTime(Long planGenerateTime) {
		this.planGenerateTime = planGenerateTime;
	}
	@Override
	public String toString() {
		return "TripResponse [plan=" + plan + ", error=" + error
				+ ", requestParameters=" + requestParameters
				+ ", planGenerateTime=" + planGenerateTime + "]";
	}
}
