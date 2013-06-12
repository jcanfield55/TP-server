package com.nimbler.tp.dataobject;

import java.io.Serializable;
import java.util.HashSet;
/**
 * 
 * @author nirmal
 *
 */
public class GraphMetaData implements Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6709044122170062335L;
	private double lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude;
	private HashSet<TraverseMode> transitModes = new HashSet<TraverseMode>();

	private double centerLatitude;

	private double centerLongitude;

	public double getLowerLeftLatitude() {
		return lowerLeftLatitude;
	}

	public void setLowerLeftLatitude(double lowerLeftLatitude) {
		this.lowerLeftLatitude = lowerLeftLatitude;
	}

	public double getLowerLeftLongitude() {
		return lowerLeftLongitude;
	}

	public void setLowerLeftLongitude(double lowerLeftLongitude) {
		this.lowerLeftLongitude = lowerLeftLongitude;
	}

	public double getUpperRightLatitude() {
		return upperRightLatitude;
	}

	public void setUpperRightLatitude(double upperRightLatitude) {
		this.upperRightLatitude = upperRightLatitude;
	}

	public double getUpperRightLongitude() {
		return upperRightLongitude;
	}

	public void setUpperRightLongitude(double upperRightLongitude) {
		this.upperRightLongitude = upperRightLongitude;
	}

	public HashSet<TraverseMode> getTransitModes() {
		return transitModes;
	}

	public void setTransitModes(HashSet<TraverseMode> transitModes) {
		this.transitModes = transitModes;
	}

	public double getCenterLatitude() {
		return centerLatitude;
	}

	public void setCenterLatitude(double centerLatitude) {
		this.centerLatitude = centerLatitude;
	}

	public double getCenterLongitude() {
		return centerLongitude;
	}

	public void setCenterLongitude(double centerLongitude) {
		this.centerLongitude = centerLongitude;
	}

	@Override
	public String toString() {
		return "GraphMetaData [lowerLeftLatitude=" + lowerLeftLatitude
				+ ", lowerLeftLongitude=" + lowerLeftLongitude
				+ ", upperRightLatitude=" + upperRightLatitude
				+ ", upperRightLongitude=" + upperRightLongitude
				+ ", transitModes=" + transitModes + ", centerLatitude="
				+ centerLatitude + ", centerLongitude=" + centerLongitude + "]";
	}
}
