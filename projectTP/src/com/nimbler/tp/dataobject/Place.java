/*
 * 
 */
package com.nimbler.tp.dataobject; 

import javax.xml.bind.annotation.XmlElement;

/** 
 * A Place is where a journey starts or ends, or a transit stop along the way.
 */ 
public class Place {
	public static final String GEO_JSON_POINT = "{\"type\": \"Point\", \"coordinates\": [";
	public static final String GEO_JSON_TAIL = "]}";

	/** 
	 * For transit stops, the name of the stop.  For points of interest, the name of the POI.
	 */
	private String name = null;

	/** 
	 * The ID of the stop. This is often something that users don't care about.
	 */
	private AgencyAndId stopId = null;

	/** 
	 * The "code" of the stop. Depending on the transit agency, this is often
	 * something that users care about.
	 */
	private String stopCode = null;

	/**
	 * The longitude of the place.
	 */
	private Double lon = null;

	/**
	 * The latitude of the place.
	 */
	private Double lat = null;

	/**
	 * The time the rider will arrive at the place.
	 */
	private Long arrival = null;

	/**
	 * The time the rider will depart the place.
	 */
	private Long departure = null;

	private String geometry = null;

	private String orig;

	private String zoneId;

	/**
	 * Returns the geometry in GeoJSON format
	 * @return
	 */
	@XmlElement
	String getGeometry() {
		if(lat!=null && lon!=null)
			return GEO_JSON_POINT + lon + "," + lat + GEO_JSON_TAIL;
		else
			return null;
	}

	public void setGeometry(String geometry) {
		this.geometry = null;
	}


	public Place() {
	}

	public Place(Double lon, Double lat, String name) {
		this.lon = lon;
		this.lat = lat;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AgencyAndId getStopId() {
		return stopId;
	}

	public void setStopId(AgencyAndId stopId) {
		this.stopId = stopId;
	}

	public String getStopCode() {
		return stopCode;
	}

	public void setStopCode(String stopCode) {
		this.stopCode = stopCode;
	}

	public Double getLon() {
		return lon;
	}

	public void setLon(Double lon) {
		this.lon = lon;
	}


	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Long getArrival() {
		return arrival;
	}

	public void setArrival(Long arrival) {
		this.arrival = arrival;
	}

	public Long getDeparture() {
		return departure;
	}

	public void setDeparture(Long departure) {
		this.departure = departure;
	}

	public String getOrig() {
		return orig;
	}

	public void setOrig(String orig) {
		this.orig = orig;
	}

	public String getZoneId() {
		return zoneId;
	}

	public void setZoneId(String zoneId) {
		this.zoneId = zoneId;
	}

	@Override
	public String toString() {
		return "Place [name=" + name + ", stopId=" + stopId + ", stopCode="
				+ stopCode + ", lon=" + lon + ", lat=" + lat + ", arrival="
				+ arrival + ", departure=" + departure + ", orig=" + orig
				+ ", zoneId=" + zoneId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lat == null) ? 0 : lat.hashCode());
		result = prime * result + ((lon == null) ? 0 : lon.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Place other = (Place) obj;
		if (lat == null) {
			if (other.lat != null)
				return false;
		} else if (!lat.equals(other.lat))
			return false;
		if (lon == null) {
			if (other.lon != null)
				return false;
		} else if (!lon.equals(other.lon))
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		return true;
	}

}
