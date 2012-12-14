package com.nimbler.tp.dataobject;
/**
 * 
 * @author nIKUNJ
 *
 */
public class LegLiveFeed {

	private Leg leg;
	//private Long arrivalTime;
	private Long departureTime;
	private Long arrivalTime;
	private Integer timeDiffInMins;
	private int arrivalTimeFlag;


	public Leg getLeg() {
		return leg;
	}
	/**
	 * 
	 * @param leg
	 */
	public void setLeg(Leg leg) {
		Leg emptyLeg = new Leg();
		emptyLeg.setId(leg.getId());
		emptyLeg.setMode(leg.getMode()); 
		emptyLeg.setStartTime(leg.getStartTime()); 
		emptyLeg.setEndTime(leg.getEndTime()); 
		this.leg = emptyLeg;

	}	
	public int getArrivalTimeFlag() {
		return arrivalTimeFlag;
	}
	public void setArrivalTimeFlag(int arrivalTimeFlag) {
		this.arrivalTimeFlag = arrivalTimeFlag;
	}
	public Integer getTimeDiffInMins() {
		return timeDiffInMins;
	}
	public void setTimeDiffInMins(Integer timeDiffInMins) {
		this.timeDiffInMins = timeDiffInMins;
	}

	public Long getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(Long departureTime) {
		this.departureTime = departureTime;
	}

	public Long getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(Long arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	@Override
	public String toString() {
		return "LegLiveFeed [leg=" + leg + ", departureTime=" + departureTime
				+ ", arrivalTime=" + arrivalTime + ", timeDiffInMins="
				+ timeDiffInMins + ", arrivalTimeFlag=" + arrivalTimeFlag + "]";
	}
}