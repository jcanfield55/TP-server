package com.nimbler.tp.gtfs;

import java.util.Date;
import java.util.Map;

import com.nimbler.tp.util.HtmlUtil.GtfsSummery;
import com.nimbler.tp.util.HtmlUtil.TableRow;

/**
 * 
 * @author nirmal
 *
 */
public class GtfsMonitorResult {

	private GtfsBundle gtfsBundle;
	private Map<String,Date> oldData;
	private Map<String,Date> newData;
	private Map<String,Date> crackedData;
	private Map<String,TableRow> merged;
	private GtfsSummery gtfsSummury;
	private String error;
	public boolean ignoreErrorInSummery = false;

	public GtfsBundle getGtfsBundle() {
		return gtfsBundle;
	}
	public void setGtfsBundle(GtfsBundle gtfsBundle) {
		this.gtfsBundle = gtfsBundle;
	}
	public Map<String, Date> getOldData() {
		return oldData;
	}
	public void setOldData(Map<String, Date> oldData) {
		this.oldData = oldData;
	}
	public Map<String, TableRow> getMerged() {
		return merged;
	}
	public GtfsSummery getGtfsSummury() {
		return gtfsSummury;
	}
	public void setGtfsSummury(GtfsSummery gtfsSummury) {
		this.gtfsSummury = gtfsSummury;
	}
	public void setMerged(Map<String, TableRow> merged) {
		this.merged = merged;
	}
	public Map<String, Date> getCrackedData() {
		return crackedData;
	}
	public void setCrackedData(Map<String, Date> crackedData) {
		this.crackedData = crackedData;
	}
	public Map<String, Date> getNewData() {
		return newData;
	}
	public void setNewData(Map<String, Date> newData) {
		this.newData = newData;
	}
	public GtfsMonitorResult(GtfsBundle gtfsBundle) {
		this.gtfsBundle = gtfsBundle;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public void addError(String err) {
		if(error == null)
			error = err;
		else
			error = error+", "+err;
	}
	@Override
	public String toString() {
		return "GtfsMonitorResult [gtfsBundle=" + gtfsBundle + ", oldData="
				+ oldData + ", newData=" + newData + ", crakedData="
				+ crackedData + ", merged=" + merged + ", gtfsSummury="
				+ gtfsSummury + ", error=" + error + "]";
	}
}
