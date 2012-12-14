package com.nimbler.tp.gtfs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.nimbler.tp.dataobject.TripPlan;
import com.nimbler.tp.util.ComUtils;

/**
 * 
 * @author nirmal
 *
 */
public class PlanCompareResult {
	private String fileName; 
	private String url;
	private int oldItinerary;
	private int newItinerary;
	private boolean match = false;
	private List<String> lstError;
	private TripPlan oldPlan;
	private TripPlan newPlan;

	public PlanCompareResult() {
	}

	public PlanCompareResult(TripPlan oldPlan, TripPlan newPlan) {
		this.oldPlan = oldPlan;
		this.newPlan = newPlan;
		oldItinerary = oldPlan.getItineraries().size();
		newItinerary = newPlan.getItineraries().size();
	}
	public TripPlan getOldPlan() {
		return oldPlan;
	}

	public void setOldPlan(TripPlan oldPlan) {
		this.oldPlan = oldPlan;
	}

	public TripPlan getNewPlan() {
		return newPlan;
	}

	public void setNewPlan(TripPlan newPlan) {
		this.newPlan = newPlan;
	}
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isMatch() {
		return match;
	}
	public void setMatch(boolean match) {
		this.match = match;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getOldItinerary() {
		return oldItinerary;
	}
	public void setOldItinerary(int oldItinerary) {
		this.oldItinerary = oldItinerary;
	}
	public int getNewItinerary() {
		return newItinerary;
	}
	public void setNewItinerary(int newItinerary) {
		this.newItinerary = newItinerary;
	}
	public List<String> getLstError() {
		return lstError;
	}
	public void setLstError(List<String> lstError) {
		this.lstError = lstError;
	}
	public void addError(String error) {
		if (lstError == null) 
			lstError = new ArrayList<String>();
		lstError.add(error);
	}
	public String getErrorString() {
		if(ComUtils.isEmptyList(lstError))
			return "Unknown Error";
		else
			return StringUtils.join(lstError,",");

	}
	@Override
	public String toString() {
		return "PlanCompareResult [ match=" + match +", fileName=" + fileName + ", url="+url 
				+ ", oldItinerary=" + oldItinerary + ", newItinerary="
				+ newItinerary + ", lstError=" + lstError
				+ "]";
	}
}
