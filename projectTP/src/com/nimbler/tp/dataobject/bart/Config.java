package com.nimbler.tp.dataobject.bart;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="config")
public class Config {
	private List<String> station;

	public void setStation(List<String> station) {
		this.station = station;
	}

	@XmlElement
	public List<String> getStation() {
		return station;
	}

	@Override
	public String toString() {
		return "Config [station=" + station + "]";
	}	
	
}
