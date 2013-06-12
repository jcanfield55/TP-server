/*
 * @author nirmal
 */
package com.nimbler.tp.dataobject.wmata;

import org.apache.commons.lang3.math.NumberUtils;

import com.nimbler.tp.dataobject.RealTimePrediction;
/**
 * 
 * @author nirmal
 *
 */
public class WmataRealTimePrediction extends RealTimePrediction{
	private String cars;
	private String group;
	private String destination;
	/**
	 * 
	 */
	private static final long serialVersionUID = -5104871459162412142L;

	/**
	 * Seconds
	 * -1 : ARR
	 * -2 : BRD
	 * -3 : unknown
	 *
	 * @param rp the rp
	 * @return the wmata real time prediction
	 */
	public static WmataRealTimePrediction of(RailPrediction rp) {
		WmataRealTimePrediction prediction = new WmataRealTimePrediction();
		String strMin = rp.getMin();
		long seconds = 0;
		if("BRD".equalsIgnoreCase(strMin))
			seconds = -2;
		else if("ARR".equalsIgnoreCase(strMin))
			seconds = -1;
		else {
			long min = NumberUtils.toLong(strMin,-1);
			seconds = (min == -1)?-3:(min*60);
		}
		prediction.setSeconds(seconds);
		prediction.setEpochTime(rp.getCreateTime()+(seconds*1000));
		prediction.setCars(rp.getCar());
		prediction.setGroup(rp.getGroup());
		prediction.setDestination(rp.getDestinationName());
		return prediction;
	}
	/**
	 * @return the cars
	 */
	public String getCars() {
		return cars;
	}
	/**
	 * @param cars the cars to set
	 */
	public void setCars(String cars) {
		this.cars = cars;
	}
	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}
	/**
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}
	/**
	 * @return the destination
	 */
	public String getDestination() {
		return destination;
	}
	/**
	 * @param destination the destination to set
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WmataRealTimePrediction ["
				+ (cars != null ? "cars=" + cars + ", " : "")
				+ (group != null ? "group=" + group + ", " : "")
				+ (destination != null ? "destination=" + destination : "")
				+ "]";
	}
}
