/*
 * @author nirmal
 */
package com.nimbler.tp.dataobject;

import java.io.Serializable;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.nimbler.tp.dataobject.bart.Estimate;
import com.nimbler.tp.dataobject.nextbus.Prediction;

/**
 * The Class RealTimePrediction.
 *
 * @author nirmal
 */
public class RealTimePrediction implements Serializable{

	private String tripId;
	private String routeTag;
	private Long epochTime;
	private Long seconds;
	private String vehicleId;
	private String direction;

	/**
	 * 
	 */
	private static final long serialVersionUID = -5685192102963893821L;

	public RealTimePrediction(Prediction prediction) {
		this.epochTime = prediction.getEpochTime();
		this.tripId = prediction.getTripTag();
		this.seconds = prediction.getSeconds();
		this.vehicleId = prediction.getVehicle();
	}
	public RealTimePrediction(Estimate estimate) {
		this.seconds = NumberUtils.toLong(estimate.getMinutes())*60;				
		this.epochTime =  estimate.getCreateTime()+(seconds*1000);				
	}
	public String getTripId() {
		return tripId;
	}
	public void setTripId(String tripId) {
		this.tripId = tripId;
	}
	public String getRouteTag() {
		return routeTag;
	}
	public void setRouteTag(String routeTag) {
		this.routeTag = routeTag;
	}
	public Long getEpochTime() {
		return epochTime;
	}
	public void setEpochTime(Long epochTime) {
		this.epochTime = epochTime;
	}
	public Long getSeconds() {
		return seconds;
	}
	public void setSeconds(Long seconds) {
		this.seconds = seconds;
	}
	public String getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}


}
