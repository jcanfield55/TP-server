/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.util.Arrays;
import java.util.Date;


public class GtfsCalander {

	//	public static SimpleDateFormat gtfsDateFormat = new SimpleDateFormat("yyyyMMdd");
	public String serviceName;
	public Date startDate;
	public Date endDate;
	/**
	 * start from synday
	 */
	private int[] weeklyStatusForService;

	public String getServiceName() {		
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}


	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int[] getWeeklyStatusForService() {
		return weeklyStatusForService;
	}
	public void setWeeklyStatusForService(int[] weeklyStatusForService) {
		this.weeklyStatusForService = weeklyStatusForService;
	}
	public boolean isServiceEnabled(Date date){
		return (startDate.before(date) || startDate.equals(date)) && (date.before(endDate) || endDate.equals(date));
	}

	@Override
	public String toString() {
		return "GtfsCalander [serviceName=" + serviceName + ", startDate="
				+ startDate + ", endDate=" + endDate
				+ ", weeklyStatusForService="
				+ Arrays.toString(weeklyStatusForService) + "]";
	}
}