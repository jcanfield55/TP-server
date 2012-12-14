/*
 * 
 */
package com.nimbler.tp.dataobject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An Itinerary is one complete way of getting from the start location to the end location.
 */
public class Itinerary {

	/**
	 * Primary key
	 */
	private String id;
	/**
	 * Linking reference for plan.
	 */
	private String planId;

	/**
	 * Duration of the trip on this itinerary, in milliseconds.
	 */
	private long duration = 0;

	/**
	 * Time that the trip departs.
	 */
	private long startTime = 0;
	/**
	 * Time that the trip arrives.
	 */
	private long endTime = 0;

	/**
	 * How much time is spent walking, in seconds.
	 */
	private long walkTime = 0;
	/**
	 * How much time is spent on transit, in seconds.
	 */
	private Long transitTime = null;
	/**
	 * How much time is spent waiting for transit to arrive, in seconds.
	 */
	private Long waitingTime = null;

	/**
	 * How far the user has to walk, in meters.
	 */
	private Double walkDistance = 0.0;

	/**
	 * How much elevation is lost, in total, over the course of the trip, in meters. As an example,
	 * a trip that went from the top of Mount Everest straight down to sea level, then back up K2,
	 * then back down again would have an elevationLost of Everest + K2.
	 */
	private Double elevationLost = 0.0;
	/**
	 * How much elevation is gained, in total, over the course of the trip, in meters. See
	 * elevationLost.
	 */
	private Double elevationGained = 0.0;

	/**
	 * The number of transfers this trip has.
	 */
	private Integer transfers = 0;

	/**
	 * The cost of this trip
	 */
	//	private Fare fare = new Fare();//TOOD nirmal

	/**
	 * A list of Legs. Each Leg is either a walking (cycling, car) portion of the trip, or a transit
	 * trip on a particular vehicle. So a trip where the use walks to the Q train, transfers to the
	 * 6, then walks to their destination, has four legs.
	 */
	private List<Leg> legs = new ArrayList<Leg>();

	/**
	 * This itinerary has a greater slope than the user requested (but there are no possible 
	 * itineraries with a good slope). 
	 */
	private boolean tooSloped = false;



	/** 
	 * adds leg to array list
	 * @param leg
	 */
	public void addLeg(Leg leg) {
		if(leg==null)
			return;
		if(legs==null)
			legs=  new ArrayList<Leg>();
		legs.add(leg);
	}


	public long getDuration() {
		return duration;
	}


	public void setDuration(long duration) {
		this.duration = duration;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public long getEndTime() {
		return endTime;
	}


	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}


	public long getWalkTime() {
		return walkTime;
	}


	public void setWalkTime(long walkTime) {
		this.walkTime = walkTime;
	}


	public long getTransitTime() {
		return transitTime;
	}


	public void setTransitTime(long transitTime) {
		this.transitTime = transitTime;
	}


	public Long getWaitingTime() {
		return waitingTime;
	}



	public Double getWalkDistance() {
		return walkDistance;
	}


	public void setWalkDistance(Double walkDistance) {
		this.walkDistance = walkDistance;
	}


	public Double getElevationLost() {
		return elevationLost;
	}


	public void setElevationLost(Double elevationLost) {
		this.elevationLost = elevationLost;
	}


	public Double getElevationGained() {
		return elevationGained;
	}


	public void setElevationGained(Double elevationGained) {
		this.elevationGained = elevationGained;
	}


	public Integer getTransfers() {
		return transfers;
	}


	public void setTransfers(Integer transfers) {
		this.transfers = transfers;
	}


	public List<Leg> getLegs() {
		return legs;
	}


	public void setLegs(List<Leg> legs) {
		this.legs = legs;
	}


	public boolean isTooSloped() {
		return tooSloped;
	}


	public void setTooSloped(boolean tooSloped) {
		this.tooSloped = tooSloped;
	}


	/** 
	 * remove the leg from the list of legs 
	 * @param leg object to be removed
	 */
	public void removeLeg(Leg leg) {
		if(leg != null) {
			legs.remove(leg);
		}
	}

	public void removeBogusLegs() {
		Iterator<Leg> it = legs.iterator();
		while (it.hasNext()) {
			Leg leg = it.next();
			if (leg.isBogusNonTransitLeg()) {
				it.remove();
			}
		}
	}


	@Override
	public String toString() {
		return "Itinerary [duration=" + duration + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", walkTime=" + walkTime
				+ ", transitTime=" + transitTime + ", waitingTime="
				+ waitingTime + ", walkDistance=" + walkDistance
				+ ", elevationLost=" + elevationLost + ", elevationGained="
				+ elevationGained + ", transfers=" + transfers + ", legs="
				+ legs + ", tooSloped=" + tooSloped + "]";
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getPlanId() {
		return planId;
	}

	public void setWaitingTime(Long waitingTime) {
		this.waitingTime = waitingTime;
	}


	public void setPlanId(String planId) {
		this.planId = planId;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (duration ^ (duration >>> 32));
		result = prime * result + ((legs == null) ? 0 : legs.hashCode());
		result = prime * result
				+ ((transfers == null) ? 0 : transfers.hashCode());
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
		Itinerary other = (Itinerary) obj;
		if (duration != other.duration)
			return false;
		if (legs == null) {
			if (other.legs != null)
				return false;
		} else if (legs.size() != other.legs.size())
			return false;
		else if(!legs.equals(other.legs)){
			return false;
		}
		if (transfers == null) {
			if (other.transfers != null)
				return false;
		} else if (!transfers.equals(other.transfers))
			return false;
		return true;
	}

}