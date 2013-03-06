package com.nimbler.tp.dataobject.flurry;

import java.io.Serializable;

public class FlurryQueryData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3753891086958170065L;
	private String id;
	private String project;
	private long startTime;
	private long endTime;
	private long requestTime;
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public long getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(long requestTime) {
		this.requestTime = requestTime;
	}
	@Override
	public String toString() {
		return "FlurryQueryData [project=" + project + ", startTime="
				+ startTime + ", endTime=" + endTime + ", requestTime="
				+ requestTime + "]";
	}
}

