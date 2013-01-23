package com.nimbler.tp.dataobject.wmata;

import java.util.List;

import com.google.gson.annotations.SerializedName;


public class WmataRailPredictions {

	public WmataRailPredictions() {
	}

	@SerializedName("Trains")
	private List<RailPrediction> trains;

	public List<RailPrediction> getTrains() {
		return trains;
	}

	public void setTrains(List<RailPrediction> trains) {
		this.trains = trains;
	}


}