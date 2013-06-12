package com.nimbler.tp.dataobject.wmata;

import java.util.List;

public class WmataDirection {
	String DirectionNum;
	String DirectionText;
	String TripHeadsign;
	List<BusStop> Stops;
	String apiRouteId;
	String gtfsRouteId;

	public String getDirectionNum() {
		return DirectionNum;
	}
	public void setDirectionNum(String directionNum) {
		DirectionNum = directionNum;
	}
	public String getDirectionText() {
		return DirectionText;
	}
	public void setDirectionText(String directionText) {
		DirectionText = directionText;
	}
	public List<BusStop> getStops() {
		return Stops;
	}


	/**
	 * @return the apiRouteId
	 */
	public String getApiRouteId() {
		return apiRouteId;
	}
	/**
	 * @param apiRouteId the apiRouteId to set
	 */
	public void setApiRouteId(String apiRouteId) {
		this.apiRouteId = apiRouteId;
	}
	/**
	 * @return the gtfsRouteId
	 */
	public String getGtfsRouteId() {
		return gtfsRouteId;
	}
	/**
	 * @param gtfsRouteId the gtfsRouteId to set
	 */
	public void setGtfsRouteId(String gtfsRouteId) {
		this.gtfsRouteId = gtfsRouteId;
	}
	public void setStops(List<BusStop> stops) {
		Stops = stops;
	}

	public String getTripHeadsign() {
		return TripHeadsign;
	}
	public void setTripHeadsign(String tripHeadsign) {
		TripHeadsign = tripHeadsign;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WmataDirection [DirectionNum=" + DirectionNum
				+ ", DirectionText=" + DirectionText + ", TripHeadsign="
				+ TripHeadsign + ", Stops=" + Stops + ", apiRouteId="
				+ apiRouteId + ", gtfsRouteId=" + gtfsRouteId + "]";
	}
}
