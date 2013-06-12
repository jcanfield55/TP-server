package com.nimbler.tp.dataobject.wmata;

import java.util.List;

import com.google.gson.annotations.SerializedName;


public class WmataBusPredictions {

	public WmataBusPredictions() {
	}

	@SerializedName("Predictions")
	private List<WmataBusPrediction> predictions;

	public List<WmataBusPrediction> getPredictions() {
		return predictions;
	}

	public void setPredictions(List<WmataBusPrediction> predictions) {
		this.predictions = predictions;
	}


}