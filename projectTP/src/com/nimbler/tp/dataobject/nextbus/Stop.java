package com.nimbler.tp.dataobject.nextbus;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="stop")
public class Stop {

	private String tag;
	private String title;
	private String lat;
	private String lon;
	private String stopId;
	
	@XmlAttribute
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	@XmlAttribute
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	@XmlAttribute
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	@XmlAttribute
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}
	@XmlAttribute
	public String getStopId() {
		return stopId;
	}
	public void setStopId(String stopId) {
		this.stopId = stopId;
	}
	
	@Override
	public String toString() {
		return "Stop [lat=" + lat + ", lon=" + lon + ", stopId=" + stopId
				+ ", tag=" + tag + ", title=" + title + "]";
	}
	
	
}
