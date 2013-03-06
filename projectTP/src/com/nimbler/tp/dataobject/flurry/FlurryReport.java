package com.nimbler.tp.dataobject.flurry;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
/**
 * 
 * @author nirmal
 *
 */
public class FlurryReport implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8018534774725867997L;

	private String id;

	@SerializedName("query")
	FlurryQueryData queryData;

	@SerializedName("meta")
	private Map<String,String> metaData;

	@SerializedName("sessionEvents")
	private List<FlurrySessionEventData> lstFlurrySessionEvents;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public FlurryQueryData getQueryData() {
		return queryData;
	}
	public void setQueryData(FlurryQueryData queryData) {
		this.queryData = queryData;
	}

	public List<FlurrySessionEventData> getLstFlurrySessionEvents() {
		return lstFlurrySessionEvents;
	}
	public void setLstFlurrySessionEvents(
			List<FlurrySessionEventData> lstFlurrySessionEvents) {
		this.lstFlurrySessionEvents = lstFlurrySessionEvents;
	}
	public Map<String, String> getMetaData() {
		return metaData;
	}
	public void setMetaData(Map<String, String> metaData) {
		this.metaData = metaData;
	}
	@Override
	public String toString() {
		return "FlurryReport [queryData=" + queryData + ", metaData="
				+ metaData + ", lstFlurrySessionEvents="
				+ lstFlurrySessionEvents + "]";
	}
}
