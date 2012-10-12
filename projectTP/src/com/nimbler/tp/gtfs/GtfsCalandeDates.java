package com.nimbler.tp.gtfs;


/**
 * The Class GtfsCalandeDates.
 *
 * @author nirmal
 */
public class GtfsCalandeDates {
	private enum SERVICE_EXCEPTION_TYPE{
		UNDEFINED,
		ADDED,
		REMOVED
	}
	String serviceName;
	String serviceType;
	String date;

	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceType() {
		return serviceType;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "GtfsCalandeDates [serviceName=" + serviceName
				+ ", ServiceType=" + serviceType + ", date=" + date + "]\n";
	}


}