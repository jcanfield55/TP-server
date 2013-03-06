package com.nimbler.tp.dataobject.wmata;

import com.google.gson.annotations.SerializedName;

public  class RailPrediction {

	public enum RAIL_MIN_VALUES {
		BRD, ARR;
	}

	public RailPrediction() {
	}

	@SerializedName("Car")
	private String car; //Number of cars in a particular train (usually 6 or 8).

	@SerializedName("Destination")
	private String destination;

	@SerializedName("DestinationCode")
	private String destinationCode;//The ID of destination station.

	@SerializedName("DestinationName")
	private String destinationName;

	@SerializedName("Group")
	private String group;

	@SerializedName("Line")
	private String line;

	@SerializedName("LocationCode")
	private String locationCode; //ID of the station where the train is arriving.

	@SerializedName("LocationName")
	private String locationName;

	@SerializedName("Min")
	private String min;

	long scheduledTime;
	long estimatedTime;

	long createTime = System.currentTimeMillis();

	public String getCar() {
		return car;
	}


	public long getCreateTime() {
		return createTime;
	}


	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}


	public long getScheduledTime() {
		return scheduledTime;
	}
	public void setScheduledTime(long scheduledTime) {
		this.scheduledTime = scheduledTime;
	}



	public long getEstimatedTime() {
		return estimatedTime;
	}



	public void setEstimatedTime(long estimatedTime) {
		this.estimatedTime = estimatedTime;
	}



	public String getMin() {
		return min;
	}


	public void setMin(String min) {
		this.min = min;
	}


	public void setCar(String car) {
		this.car = car;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDestinationCode() {
		return destinationCode;
	}

	public void setDestinationCode(String destinationCode) {
		this.destinationCode = destinationCode;
	}

	public String getDestinationName() {
		return destinationName;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	@Override
	public String toString() {
		return "RailPrediction [car=" + car + ", destination=" + destination
				+ ", destinationCode=" + destinationCode + ", destinationName="
				+ destinationName + ", group=" + group + ", line=" + line
				+ ", locationCode=" + locationCode + ", locationName="
				+ locationName + ", min=" + min + "]";
	}
}