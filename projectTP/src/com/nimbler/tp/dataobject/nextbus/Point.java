package com.nimbler.tp.dataobject.nextbus;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="point")
public class Point {
	private String lat;
	private String lon;

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
	@Override
	public String toString() {
		return "Point [lat=" + lat + ", lon=" + lon + "]";
	}
}
