package com.nimbler.tp.dataobject.wmata;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class RailLine implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2332285866267235730L;

	@SerializedName("DisplayName")
	private String displayName;

	@SerializedName("EndStationCode")
	private String endStationCode;

	@SerializedName("InternalDestination1")
	private String internalDestination1;

	@SerializedName("LineCode")
	private String lineCode;

	@SerializedName("StartStationCode")
	private String startStationCode;


	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEndStationCode() {
		return endStationCode;
	}

	public void setEndStationCode(String endStationCode) {
		this.endStationCode = endStationCode;
	}

	public String getInternalDestination1() {
		return internalDestination1;
	}


	public void setInternalDestination1(String internalDestination1) {
		this.internalDestination1 = internalDestination1;
	}

	public String getLineCode() {
		return lineCode;
	}

	public void setLineCode(String lineCode) {
		this.lineCode = lineCode;
	}

	public String getStartStationCode() {
		return startStationCode;
	}

	public void setStartStationCode(String startStationCode) {
		this.startStationCode = startStationCode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((lineCode == null) ? 0 : lineCode.hashCode());
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
		RailLine other = (RailLine) obj;
		if (lineCode == null) {
			if (other.lineCode != null)
				return false;
		} else if (!lineCode.equals(other.lineCode))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RailLine [displayName=" + displayName + ", endStationCode="
				+ endStationCode + ", internalDestination1="
				+ internalDestination1 + ", lineCode=" + lineCode
				+ ", startStationCode=" + startStationCode + "]";
	}
}

