package com.nimbler.tp.dataobject.nextbus;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="body")
public class NextBusResponse {
	private List<Route> route;
	private List<Predictions> predictions;
	private NextBusError error;

	public void setRoute(List<Route> route) {
		this.route = route;
	}

	public List<Route> getRoute() {
		return route;
	}

	public void setPredictions(List<Predictions> predictions) {
		this.predictions = predictions;
	}

	@XmlElement(name="Error")
	public NextBusError getError() {
		return error;
	}

	public void setError(NextBusError error) {
		this.error = error;
	}

	public List<Predictions> getPredictions() {
		return predictions;
	}

	@Override
	public String toString() {
		return "Body [predictions=" + predictions + ", route=" + route + "]";
	}	
}