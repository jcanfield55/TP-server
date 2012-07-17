package com.nimbler.tp.dataobject.nextbus;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="prediction")
public class Prediction {
	private long epochTime;
	private long seconds;
	private long minutes;
	private String isDeparture;
	private String dirTag;
	private String vehicle;
	private boolean affectedByLayover;
	private String block;
	private String tripTag;
	
	@XmlAttribute
	public long getEpochTime() {
		return epochTime;
	}
	public void setEpochTime(long epochTime) {
		this.epochTime = epochTime;
	}
	@XmlAttribute
	public long getSeconds() {
		return seconds;
	}
	public void setSeconds(long seconds) {
		this.seconds = seconds;
	}
	@XmlAttribute
	public long getMinutes() {
		return minutes;
	}
	public void setMinutes(long minutes) {
		this.minutes = minutes;
	}
	@XmlAttribute
	public String getIsDeparture() {
		return isDeparture;
	}
	public void setIsDeparture(String isDeparture) {
		this.isDeparture = isDeparture;
	}
	@XmlAttribute
	public String getDirTag() {
		return dirTag;
	}
	public void setDirTag(String dirTag) {
		this.dirTag = dirTag;
	}
	@XmlAttribute
	public String getVehicle() {
		return vehicle;
	}
	public void setVehicle(String vehicle) {
		this.vehicle = vehicle;
	}
	@XmlAttribute
	public boolean isAffectedByLayover() {
		return affectedByLayover;
	}
	public void setAffectedByLayover(boolean affectedByLayover) {
		this.affectedByLayover = affectedByLayover;
	}
	@XmlAttribute
	public String getBlock() {
		return block;
	}
	public void setBlock(String block) {
		this.block = block;
	}
	@XmlAttribute
	public String getTripTag() {
		return tripTag;
	}
	public void setTripTag(String tripTag) {
		this.tripTag = tripTag;
	}
	@Override
	public String toString() {
		return "Prediction [affectedByLayover=" + affectedByLayover
				+ ", block=" + block + ", dirTag=" + dirTag + ", epochTime="
				+ epochTime + ", isDeparture=" + isDeparture + ", minutes="
				+ minutes + ", seconds=" + seconds + ", tripTag=" + tripTag
				+ ", vehicle=" + vehicle + "]";
	}
	
}
