package com.nimbler.tp.dataobject.nextbus;

import java.util.Date;

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
	private String  dirTitleBecauseNoPredictions;

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
	@XmlAttribute
	public String getDirTitleBecauseNoPredictions() {
		return dirTitleBecauseNoPredictions;
	}
	public void setDirTitleBecauseNoPredictions(String dirTitleBecauseNoPredictions) {
		this.dirTitleBecauseNoPredictions = dirTitleBecauseNoPredictions;
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

	public String finePrint() {
		return "Prediction [dirTag=" + dirTag + ", tripTag=" + tripTag
				+ ", minutes=" + minutes + ", seconds=" + seconds
				+ ", vehicle=" + vehicle + ", date=" + new Date(epochTime)
		+ ", dirTitleBecauseNoPredictions="
		+ dirTitleBecauseNoPredictions + "]";
	}
	@Override
	public String toString() {
		return "Prediction [epochTime=" + epochTime + ", seconds=" + seconds
				+ ", minutes=" + minutes + ", isDeparture=" + isDeparture
				+ ", dirTag=" + dirTag + ", vehicle=" + vehicle
				+ ", affectedByLayover=" + affectedByLayover + ", block="
				+ block + ", tripTag=" + tripTag
				+ ", dirTitleBecauseNoPredictions="
				+ dirTitleBecauseNoPredictions + "]";
	}

}
