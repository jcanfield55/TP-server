package com.nimbler.tp.dataobject.wmata;

import java.util.List;

import com.google.gson.annotations.SerializedName;


public class WmataStops {

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
		return "WmataStopsResponse [stops=" + stops + "]";
	}
}
