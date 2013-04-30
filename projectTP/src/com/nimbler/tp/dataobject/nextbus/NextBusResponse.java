package com.nimbler.tp.dataobject.nextbus;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="body")
public class NextBusResponse {
	private List<Route> route;
	private List<Predictions> predictions;
	private List<NbVehicle> vehicles;
	private NextBusError error;
	private LastTime  lastTime;

	public void setRoute(List<Route> route) {
		this.route = route;
	}

	public List<Route> getRoute() {
		return route;
	}

	public void setPredictions(List<Predictions> predictions) {
		this.predictions = predictions;
	}
	@XmlElement(name="vehicle")
	public List<NbVehicle> getVehicles() {
		return vehicles;
	}


	public void setVehicles(List<NbVehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public LastTime getLastTime() {
		return lastTime;
	}

	public void setLastTime(LastTime lastTime) {
		this.lastTime = lastTime;
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
		return "NextBusResponse [route=" + route + ", predictions="
				+ predictions + ", vehicles=" + vehicles + ", error=" + error
				+ ", lastTime=" + lastTime + "]";
	}	
}