package com.nimbler.tp.dataobject.wmata;

import com.google.gson.annotations.SerializedName;

/**
 * The Class Predictions.
 * @author nirmal
 */
public class Predictions {

	@SerializedName("DirectionNum")
	private String directionNum;
	@SerializedName("DirectionText")
	private String directionText;
	@SerializedName("Minutes")
	private String minutes;
	@SerializedName("RouteID")
	private String routeID;
	@SerializedName("VehicleID")
	private String vehicleID;

	private BusStop busStop;

	public String getDirectionNum() {
		return directionNum;
	}

	public BusStop getBusStop() {
		return busStop;
	}

	public void setBusStop(BusStop busStop) {
		this.busStop = busStop;
	}

	public void setDirectionNum(String directionNum) {
		this.directionNum = directionNum;
	}
	public String getDirectionText() {
		return directionText;
	}
	public void setDirectionText(String directionText) {
		this.directionText = directionText;
	}
	public String getMinutes() {
		return minutes;
	}
	public void setMinutes(String minutes) {
		this.minutes = minutes;
	}
	public String getRouteID() {
		return routeID;
	}
	public void setRouteID(String routeID) {
		this.routeID = routeID;
	}
	public String getVehicleID() {
		return vehicleID;
	}
	public void setVehicleID(String vehicleID) {
		this.vehicleID = vehicleID;
	}
	@Override
	public String toString() {
		return "Predictions [directionNum=" + directionNum + ", directionText="
				+ directionText + ", minutes=" + minutes + ", routeID="
				+ routeID + ", vehicleID=" + vehicleID + "]";
	}
}
