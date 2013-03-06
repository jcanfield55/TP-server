package com.nimbler.tp.dataobject.nextbus;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="direction")
public class Direction {
	private String title;
	private String name;
	private List<Prediction> prediction;
	private List<Stop> stop;

	public void setStop(List<Stop> stop) {
		this.stop = stop;
	}

	public List<Stop> getStop() {
		return stop;
	}

	public void setPrediction(List<Prediction> prediction) {
		this.prediction = prediction;
	}

	public List<Prediction> getPrediction() {
		return prediction;
	}


	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	@XmlAttribute
	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return "Direction [prediction=" + prediction + ", stop=" + stop
				+ ", title=" + title + "]";
	}

}
