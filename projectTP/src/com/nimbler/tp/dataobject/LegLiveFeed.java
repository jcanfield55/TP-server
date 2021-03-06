package com.nimbler.tp.dataobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import com.nimbler.tp.dataobject.nextbus.VehiclePosition;

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
	private Integer arrivalTimeFlag;	
	private List<RealTimePrediction> lstPredictions;
	private List<VehiclePosition> lstVehiclePositions ;


	public Leg getLeg() {
		return leg;
	}

	public List<RealTimePrediction> getLstPredictions() {
		return lstPredictions;
	}

	public void setLstPredictions(List<RealTimePrediction> lstPredictions) {
		this.lstPredictions = lstPredictions;
	}

	public List<VehiclePosition> getLstVehiclePositions() {
		return lstVehiclePositions;
	}

	public void setLstVehiclePositions(List<VehiclePosition> lstVehiclePositions) {
		this.lstVehiclePositions = lstVehiclePositions;
	}
	public void addVehiclePosition(VehiclePosition vehiclePosition) {
		if(lstVehiclePositions == null)
			lstVehiclePositions = new ArrayList<VehiclePosition>();
		this.lstVehiclePositions.add(vehiclePosition);
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
		emptyLeg.setRouteId(leg.getRouteId()); 
		emptyLeg.setRoute(leg.getRoute()); 
		this.leg = emptyLeg;

	}	
	public void setEmptyLeg(Leg leg) {
		Leg emptyLeg = new Leg();
		emptyLeg.setId(leg.getId());
		emptyLeg.setMode(null);
		emptyLeg.setRoute(null);
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
	public void setTimeDiffInMills(int diff) {
		this.timeDiffInMins = (int) (diff/DateUtils.MILLIS_PER_MINUTE);
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
		return "LegLiveFeed ["
				+ (leg != null ? "leg=" + leg + ", " : "")
				+ (departureTime != null ? "departureTime=" + departureTime
						+ ", " : "")
						+ (arrivalTime != null ? "arrivalTime=" + arrivalTime + ", "
								: "")
								+ (timeDiffInMins != null ? "timeDiffInMins=" + timeDiffInMins
										+ ", " : "")
										+ (arrivalTimeFlag != null ? "arrivalTimeFlag="
												+ arrivalTimeFlag + ", " : "")
												+ (lstPredictions != null ? "lstPredictions=" + lstPredictions
														+ ", " : "")
														+ (lstVehiclePositions != null ? "lstVehiclePositions="
																+ lstVehiclePositions : "") + "]";
	}
}