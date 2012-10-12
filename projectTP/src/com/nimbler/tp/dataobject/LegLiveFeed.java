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
	@Override
	public String toString() {
		return "LegLiveFeed [leg=" + leg + ", timeDiffInMins=" + timeDiffInMins
				+ ", arrivalTimeFlag=" + arrivalTimeFlag + "]";
	}
}