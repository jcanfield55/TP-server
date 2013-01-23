package com.nimbler.tp.dataobject.wmata;

import java.util.List;

import com.google.gson.annotations.SerializedName;


public class WmataBusPredictions {

	public WmataBusPredictions() {
	}

	@SerializedName("Predictions")
	private List<Predictions> predictions;

	public List<Predictions> getPredictions() {
		return predictions;
	}

	public void setPredictions(List<Predictions> predictions) {
		this.predictions = predictions;
	}


}