/*
 * @author nirmal
 */
package com.nimbler.tp.dataobject.trimet.vehicleposition;

import java.io.Serializable;

/**
 * The Class TrimetVehicle.
 *
 * @author nirmal
 */
public class TrimetVehicle implements Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6742369051541733179L;


	private Integer tripID;
	private Integer direction = -1;
	private Long time;
	private Long expires;
	private String signMessageLong;
	private double longitude;
	private double latitude;
	private Integer vehicleID;	
	private Integer routeNumber;
	public Integer getTripID() {
		return tripID;
	}
	public void setTripID(Integer tripID) {
		this.tripID = tripID;
	}
	public Integer getDirection() {
		return direction;
	}
	public void setDirection(Integer direction) {
		this.direction = direction;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public Long getExpires() {
		return expires;
	}
	public void setExpires(Long expires) {
		this.expires = expires;
	}
	public String getSignMessageLong() {
		return signMessageLong;
	}
	public void setSignMessageLong(String signMessageLong) {
		this.signMessageLong = signMessageLong;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public Integer getVehicleID() {
		return vehicleID;
	}
	public void setVehicleID(Integer vehicleID) {
		this.vehicleID = vehicleID;
	}
	public Integer getRouteNumber() {
		return routeNumber;
	}
	public void setRouteNumber(Integer routeNumber) {
		this.routeNumber = routeNumber;
	}
	@Override
	public String toString() {
		return "TrimetVehicle [tripId=" + tripID + ", direction=" + direction
				+ ", time=" + time + ", expires=" + expires
				+ ", signMessageLong=" + signMessageLong + ", longitude="
				+ longitude + ", latitude=" + latitude + ", vehicleID="
				+ vehicleID + ", routeNumber=" + routeNumber + "]";
	}
}
