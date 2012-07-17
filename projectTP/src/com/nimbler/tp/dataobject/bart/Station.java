package com.nimbler.tp.dataobject.bart;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="station")
public class Station {

	private String name;
	private String abbr;
	private List<EstimateTimeOfDiparture> etdTimeDeparture;
	private String station;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAbbr() {
		return abbr;
	}
	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}
	public void setStations(String station) {
		this.station = station;
	}
	public String getStations() {
		return station;
	}
	@XmlElement(name="etd")
	public void setEtdTimeDeparture(List<EstimateTimeOfDiparture> etdTimeDeparture) {
		this.etdTimeDeparture = etdTimeDeparture;
	}
	public List<EstimateTimeOfDiparture> getEtdTimeDeparture() {
		return etdTimeDeparture;
	}
	@Override
	public String toString() {
		return "Station [abbr=" + abbr + ", etdTimeDeparture="
		+ etdTimeDeparture + ", name=" + name + ", station=" + station
		+ "]";
	}


}