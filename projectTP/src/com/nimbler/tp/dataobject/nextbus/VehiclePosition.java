package com.nimbler.tp.dataobject.nextbus;

import java.io.Serializable;

public class VehiclePosition implements Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -8555227236429495118L;
	private String id;
	private String vehicleId;
	private String routeTag;
	private String dirTag;
	private String lat;
	private String lon;
	private Integer secsSinceReport;
	private Boolean predictable;
	private String heading;
	private String speedKmHr;
	private Long reportTime;

	private String headsign;
	private String routeShortName;
	private String mode;

	public Long getReportTime() {
		return reportTime;
	}
	public String getId() {
		return id;
	}

	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getHeadsign() {
		return headsign;
	}

	public void setHeadsign(String headsign) {
		this.headsign = headsign;
	}

	public String getRouteShortName() {
		return routeShortName;
	}

	public void setRouteShortName(String routeShortName) {
		this.routeShortName = routeShortName;
	}

	public void setReportTime(Long reportTime) {
		this.reportTime = reportTime;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}

	public String getRouteTag() {
		return routeTag;
	}

	public void setRouteTag(String routeTag) {
		this.routeTag = routeTag;
	}

	public String getDirTag() {
		return dirTag;
	}

	public void setDirTag(String dirTag) {
		this.dirTag = dirTag;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public Integer getSecsSinceReport() {
		return secsSinceReport;
	}

	public void setSecsSinceReport(Integer secsSinceReport) {
		this.secsSinceReport = secsSinceReport;
	}

	public Boolean getPredictable() {
		return predictable;
	}

	public void setPredictable(Boolean predictable) {
		this.predictable = predictable;
	}

	public String getHeading() {
		return heading;
	}

	public void setHeading(String heading) {
		this.heading = heading;
	}

	public String getSpeedKmHr() {
		return speedKmHr;
	}

	public void setSpeedKmHr(String speedKmHr) {
		this.speedKmHr = speedKmHr;
	}

	@Override
	public String toString() {
		return "VehiclePosition [vehicleId=" + vehicleId + ", routeTag="
				+ routeTag + ", dirTag=" + dirTag + ", lat=" + lat + ", lon="
				+ lon + ", secsSinceReport=" + secsSinceReport
				+ ", predictable=" + predictable + ", heading=" + heading
				+ ", speedKmHr=" + speedKmHr + "]";
	}

	public static VehiclePosition of(NbVehicle vehicle) {
		VehiclePosition vp = new VehiclePosition();
		vp.setReportTime(vehicle.getCreateTime());
		vp.setDirTag(vehicle.getDirTag());
		vp.setHeading(vehicle.getHeading());
		vp.setLat(vehicle.getLat());
		vp.setLon(vehicle.getLon());
		vp.setSpeedKmHr(vehicle.getSpeedKmHr());
		vp.setSecsSinceReport(vehicle.getSecsSinceReport());
		vp.setPredictable(vehicle.getPredictable());
		vp.setVehicleId(vehicle.getVehicleId());
		return vp;
	}
}
