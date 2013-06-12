package com.nimbler.tp.dataobject.wmata;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class RailStopSeqElement implements Serializable,Cloneable{

	private static final long serialVersionUID = -2401318822967026040L;

	@SerializedName("DistanceToPrev")
	private String distanceToPrev;

	@SerializedName("LineCode")
	private String lineCode;

	@SerializedName("SeqNum")
	private String seqNum;

	@SerializedName("StationCode")
	private String stationCode;

	@SerializedName("StationName")
	private String stationName;

	private boolean isRushHour = false;

	/**
	 * @return the distanceToPrev
	 */
	public String getDistanceToPrev() {
		return distanceToPrev;
	}


	public static RailStopSeqElement of(String lineCode, String seqNum,
			String stationCode, String stationName, boolean isRushHour) {
		RailStopSeqElement element = new RailStopSeqElement();
		element.setLineCode(lineCode);
		element.setSeqNum(seqNum);
		element.setStationCode(stationCode);
		element.setStationName(stationName);
		element.setRushHour(isRushHour);
		return element;
	}


	/**
	 * @param distanceToPrev the distanceToPrev to set
	 */
	public void setDistanceToPrev(String distanceToPrev) {
		this.distanceToPrev = distanceToPrev;
	}

	/**
	 * @return the isRushHour
	 */
	public boolean isRushHour() {
		return isRushHour;
	}

	/**
	 * @param isRushHour the isRushHour to set
	 */
	public void setRushHour(boolean isRushHour) {
		this.isRushHour = isRushHour;
	}

	/**
	 * @return the lineCode
	 */
	public String getLineCode() {
		return lineCode;
	}

	/**
	 * @param lineCode the lineCode to set
	 */
	public void setLineCode(String lineCode) {
		this.lineCode = lineCode;
	}

	/**
	 * @return the seqNum
	 */
	public String getSeqNum() {
		return seqNum;
	}

	/**
	 * @param seqNum the seqNum to set
	 */
	public void setSeqNum(String seqNum) {
		this.seqNum = seqNum;
	}

	/**
	 * @return the stationCode
	 */
	public String getStationCode() {
		return stationCode;
	}

	/**
	 * @param stationCode the stationCode to set
	 */
	public void setStationCode(String stationCode) {
		this.stationCode = stationCode;
	}

	/**
	 * @return the stationName
	 */
	public String getStationName() {
		return stationName;
	}

	/**
	 * @param stationName the stationName to set
	 */
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RailStopSeqElement ["
				+ (lineCode != null ? "lineCode=" + lineCode + ", " : "")
				+ (stationCode != null ? "stationCode=" + stationCode + ", "
						: "")
						+ (stationName != null ? "stationName=" + stationName + ", "
								: "")
								+ (seqNum != null ? "seqNum=" + seqNum + ", " : "")
								+ (distanceToPrev != null ? "distanceToPrev=" + distanceToPrev
										: "") + "]";
	}
	@Override
	public RailStopSeqElement clone() throws CloneNotSupportedException {
		return (RailStopSeqElement) super.clone();
	}
}