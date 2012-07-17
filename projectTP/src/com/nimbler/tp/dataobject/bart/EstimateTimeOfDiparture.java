package com.nimbler.tp.dataobject.bart;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="etd")
public class EstimateTimeOfDiparture {
	
	private String destination;
	private String abbreviation;
	private List<Estimate> estimate;
	
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getAbbreviation() {
		return abbreviation;
	}
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}
	public void setEstimate(List<Estimate> estimate) {
		this.estimate = estimate;
	}
	@XmlElement(name="estimate")
	public List<Estimate> getEstimate() {
		return estimate;
	}
	@Override
	public String toString() {
		return "Etd [abbreviation=" + abbreviation + ", destination="
				+ destination + ", estimate=" + estimate + "]";
	}
	
}
