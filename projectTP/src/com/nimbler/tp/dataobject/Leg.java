/*
 * 
 */
package com.nimbler.tp.dataobject;

import java.util.List;

/**
 * One leg of a trip -- that is, a temporally continuous piece of the journey that takes place on a
 * particular vehicle (or on foot).
 */

public class Leg {
	/**
	 * Primary key.
	 */
	private String id;
	/**
	 * Linking reference for itinerary.
	 */
	private String itinId;


	/**
	 * The date and time this leg begins.
	 */
	private Long startTime = null;

	/**
	 * The date and time this leg ends.
	 */
	private Long endTime = null;

	/**
	 * The distance traveled while traversing the leg in meters.
	 */
	private Double distance = null;

	/**
	 * The mode (e.g., <code>Walk</code>) used when traversing this leg.
	 */
	private String mode = TraverseMode.WALK.toString();

	/**
	 * For transit legs, the route of the bus or train being used. For non-transit legs, the name of
	 * the street being traversed.
	 */
	private String route = "";

	/**
	 * For transit legs, the ID of the route.
	 * For non-transit legs, null.
	 */
	public String routeId = null;


	private String agencyName;

	private String agencyUrl;

	private Integer agencyTimeZoneOffset;

	/**
	 * For transit leg, the route's (background) color (if one exists). For non-transit legs, null.
	 */
	private String routeColor = null;

	/**
	 * For transit leg, the route's text color (if one exists). For non-transit legs, null.
	 */
	private String routeTextColor = null;

	/**
	 * For transit legs, if the rider should stay on the vehicle as it changes route names.
	 */
	private Boolean interlineWithPreviousLeg;


	/**
	 * For transit leg, the trip's short name (if one exists). For non-transit legs, null.
	 */
	private String tripShortName = null;

	/**
	 * For transit legs, the headsign of the bus or train being used. For non-transit legs, null.
	 */
	private String headsign = null;

	/**
	 * For transit legs, the ID of the transit agency that operates the service used for this leg.
	 * For non-transit legs, null.
	 */
	private String agencyId = null;

	/**
	 * For transit legs, the ID of the trip.
	 * For non-transit legs, null.
	 */
	private String tripId = null;

	/**
	 * The Place where the leg originates.
	 */
	private Place from = null;

	/**
	 * The Place where the leg begins.
	 */
	private Place to = null;

	/**
	 * For transit legs, intermediate stops between the Place where the leg originates and the Place where the leg ends.
	 * For non-transit legs, null.
	 * This field is optional i.e. it is always null unless "showIntermediateStops" parameter is set to "true" in the planner request.
	 */
	private List<Place> intermediateStops;

	/**
	 * The leg's geometry.
	 */
	private EncodedPolylineBean legGeometry;

	/**
	 * A series of turn by turn instructions used for walking, biking and driving. 
	 */
	private List<WalkStep> steps;

	/**
	 * Deprecated field formerly used for notes -- will be removed.  See
	 * alerts
	 */
	private List<Note> notes;

	//	@XmlElement
	//	private List<Alert> alerts;

	private String routeShortName;

	private String routeLongName;

	private String boardRule;

	private String alightRule;

	private Long duration;

	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * bogus walk/bike/car legs are those that have 0.0 distance, 
	 * and just one instruction
	 * 
	 * @return boolean true if the leg is bogus 
	 */
	public boolean isBogusNonTransitLeg() {
		boolean retVal = false;
		if( (TraverseMode.WALK.toString().equals(this.mode) ||
				TraverseMode.CAR.toString().equals(this.mode) ||
				TraverseMode.BICYCLE.toString().equals(this.mode)) &&
				(this.steps == null || this.steps.size() <= 1) && 
				this.distance == 0) {
			retVal = true;
		}
		return retVal;
	}

	public long getDuration() {
		return duration;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}


	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getAgencyName() {
		return agencyName;
	}

	public void setAgencyName(String agencyName) {
		this.agencyName = agencyName;
	}

	public String getAgencyUrl() {
		return agencyUrl;
	}

	public void setAgencyUrl(String agencyUrl) {
		this.agencyUrl = agencyUrl;
	}

	public int getAgencyTimeZoneOffset() {
		return agencyTimeZoneOffset;
	}

	public void setAgencyTimeZoneOffset(int agencyTimeZoneOffset) {
		this.agencyTimeZoneOffset = agencyTimeZoneOffset;
	}

	public String getRouteColor() {
		return routeColor;
	}

	public void setRouteColor(String routeColor) {
		this.routeColor = routeColor;
	}

	public String getRouteTextColor() {
		return routeTextColor;
	}

	public void setRouteTextColor(String routeTextColor) {
		this.routeTextColor = routeTextColor;
	}

	public Boolean getInterlineWithPreviousLeg() {
		return interlineWithPreviousLeg;
	}

	public void setInterlineWithPreviousLeg(Boolean interlineWithPreviousLeg) {
		this.interlineWithPreviousLeg = interlineWithPreviousLeg;
	}

	public String getTripShortName() {
		return tripShortName;
	}

	public void setTripShortName(String tripShortName) {
		this.tripShortName = tripShortName;
	}

	public String getHeadsign() {
		return headsign;
	}

	public void setHeadsign(String headsign) {
		this.headsign = headsign;
	}

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public Place getFrom() {
		return from;
	}

	public void setFrom(Place from) {
		this.from = from;
	}

	public Place getTo() {
		return to;
	}

	public void setTo(Place to) {
		this.to = to;
	}
	public List<Place> getIntermediateStops() {
		return intermediateStops;
	}

	public void setIntermediateStops(List<Place> intermediateStops) {
		this.intermediateStops = intermediateStops;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public EncodedPolylineBean getLegGeometry() {
		return legGeometry;
	}

	public void setLegGeometry(EncodedPolylineBean legGeometry) {
		this.legGeometry = legGeometry;
	}

	public List<WalkStep> getSteps() {
		return steps;
	}

	public void setSteps(List<WalkStep> steps) {
		this.steps = steps;
	}

	public List<Note> getNotes() {
		return notes;
	}

	public void setNotes(List<Note> notes) {
		this.notes = notes;
	}

	public String getRouteShortName() {
		return routeShortName;
	}

	public void setRouteShortName(String routeShortName) {
		this.routeShortName = routeShortName;
	}

	public String getRouteLongName() {
		return routeLongName;
	}

	public void setRouteLongName(String routeLongName) {
		this.routeLongName = routeLongName;
	}

	public String getBoardRule() {
		return boardRule;
	}

	public void setBoardRule(String boardRule) {
		this.boardRule = boardRule;
	}

	public String getAlightRule() {
		return alightRule;
	}

	public void setAlightRule(String alightRule) {
		this.alightRule = alightRule;
	}

	@Override
	public String toString() {
		return "Leg [startTime=" + startTime + ", endTime=" + endTime
				+ ", distance=" + distance + ", mode=" + mode + ", route="
				+ route + ", agencyName=" + agencyName + ", agencyUrl="
				+ agencyUrl + ", agencyTimeZoneOffset=" + agencyTimeZoneOffset
				+ ", routeColor=" + routeColor + ", routeTextColor="
				+ routeTextColor + ", interlineWithPreviousLeg="
				+ interlineWithPreviousLeg + ", tripShortName=" + tripShortName
				+ ", headsign=" + headsign + ", agencyId=" + agencyId
				+ ", tripId=" + tripId + ", from=" + from + ", to=" + to
				+ ", stop=" + intermediateStops + ", legGeometry=" + legGeometry
				+ ", walkSteps=" + steps + ", notes=" + notes
				+ ", routeShortName=" + routeShortName + ", routeLongName="
				+ routeLongName + ", boardRule=" + boardRule + ", alightRule="
				+ alightRule + "]\n";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getItinId() {
		return itinId;
	}

	public void setItinId(String itinId) {
		this.itinId = itinId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((agencyId == null) ? 0 : agencyId.hashCode());
		result = prime * result
				+ ((distance == null) ? 0 : distance.hashCode());
		result = prime * result + (int) (duration ^ (duration >>> 32));
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result
				+ ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		Leg other = (Leg) obj;
		if (agencyId == null) {
			if (other.agencyId != null)
				return false;
		} else if (!agencyId.equals(other.agencyId))
			return false;
		if (distance == null) {
			if (other.distance != null)
				return false;
		} else if (!distance.equals(other.distance))
			return false;
		if (duration != other.duration)
			return false;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	/*	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((agencyId == null) ? 0 : agencyId.hashCode());
		result = prime * result
				+ ((distance == null) ? 0 : distance.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		Leg other = (Leg) obj;
		if (agencyId == null) {
			if (other.agencyId != null)
				return false;
		} else if (!agencyId.equals(other.agencyId))
			return false;
		if (distance == null) {
			if (other.distance != null)
				return false;
		} else if (!distance.equals(other.distance))
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}*/


}