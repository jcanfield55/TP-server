package com.nimbler.tp.dataobject.nextbus;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * 
 */                    
@XmlRootElement(name="vehicle")
public class NbVehicle implements Serializable {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -8886301079725164582L;
	private String vehicleId;
	private String routeTag;
	private String dirTag;
	private String lat;
	private String lon;
	private Integer secsSinceReport;
	private Boolean predictable;
	private String heading;
	private String speedKmHr;

	private long createTime = System.currentTimeMillis();

	@XmlAttribute(name="id")
	public String getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}
	@XmlAttribute
	public String getRouteTag() {
		return routeTag;
	}
	public void setRouteTag(String routeTag) {
		this.routeTag = routeTag;
	}
	@XmlAttribute
	public String getDirTag() {
		return dirTag;
	}
	public void setDirTag(String dirTag) {
		this.dirTag = dirTag;
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
	public Integer getSecsSinceReport() {
		return secsSinceReport;
	}

	public long getCreateTime() {
		return createTime;
	}
	public void setSecsSinceReport(Integer secsSinceReport) {
		this.secsSinceReport = secsSinceReport;
	}
	@XmlAttribute
	public Boolean getPredictable() {
		return predictable;
	}
	public void setPredictable(Boolean predictable) {
		this.predictable = predictable;
	}
	@XmlAttribute
	public String getHeading() {
		return heading;
	}
	public void setHeading(String heading) {
		this.heading = heading;
	}
	@XmlAttribute
	public String getSpeedKmHr() {
		return speedKmHr;
	}
	public void setSpeedKmHr(String speedKmHr) {
		this.speedKmHr = speedKmHr;
	}
	@Override
	public String toString() {
		return "NbVehicle [vehicleId=" + vehicleId + ", routeTag=" + routeTag
				+ ", dirTag=" + dirTag + ", lat=" + lat + ", lon=" + lon
				+ ", secsSinceReport=" + secsSinceReport + ", predictable="
				+ predictable + ", heading=" + heading + ", speedKmHr="
				+ speedKmHr + "]\n";
	}
	public NbVehicle() {
	}
}
