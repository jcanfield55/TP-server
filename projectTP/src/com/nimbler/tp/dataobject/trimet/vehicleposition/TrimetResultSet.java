package com.nimbler.tp.dataobject.trimet.vehicleposition;

import java.io.Serializable;
import java.util.List;

public class TrimetResultSet implements Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -653851031206382083L;

	List<TrimetVehicle> vehicle;
	long queryTime;

	public List<TrimetVehicle> getVehicle() {
		return vehicle;
	}

	public void setVehicle(List<TrimetVehicle> vehicle) {
		this.vehicle = vehicle;
	}

	public long getQueryTime() {
		return queryTime;
	}

	public void setQueryTime(long queryTime) {
		this.queryTime = queryTime;
	}

	@Override
	public String toString() {
		return "TrimetResultSet [vehicle=" + vehicle + ", queryTime="
				+ queryTime + "]";
	}

}