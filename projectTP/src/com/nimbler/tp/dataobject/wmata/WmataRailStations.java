package com.nimbler.tp.dataobject.wmata;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;


/**
 * The Class WmataRailStations.
 */
public class WmataRailStations implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4932991201248096481L;

	/**
	 * 
	 */
	@SerializedName("Stations")
	private List<RailStation> lstStations;

	public List<RailStation> getLstStations() {
		return lstStations;
	}

	public void setLstStations(List<RailStation> lstStations) {
		this.lstStations = lstStations;
	}

	public WmataRailStations() {
	}

	@Override
	public String toString() {
		return "WmataRailStations [lstStations=" + lstStations + "]";
	}
}
