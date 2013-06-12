package com.nimbler.tp.dataobject.wmata;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;



public class Stops  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8775871379709651434L;
	/**
	 * 
	 */

	@SerializedName("Stops")
	private List<BusStop> stops;

	public List<BusStop> getStops() {
		return stops;
	}

	public void setStops(List<BusStop> stops) {
		this.stops = stops;
	}

	@Override
	public String toString() {
		return "Stops [stops=" + stops + "]";
	}
}
