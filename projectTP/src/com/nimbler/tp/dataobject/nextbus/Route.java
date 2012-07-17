package com.nimbler.tp.dataobject.nextbus;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="route")
public class Route {
	private String tag;
	private String title;
	private String color;
	private String oppositeColor;
	private String latMin;
	private String latMax;
	private String lonMin;
	private String lonMax;
	private List<Stop> stop;
	private List<Path> path;
	private List<Direction> direction;
	
	@XmlAttribute
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	@XmlAttribute
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	@XmlAttribute
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	@XmlAttribute
	public String getOppositeColor() {
		return oppositeColor;
	}
	public void setOppositeColor(String oppositeColor) {
		this.oppositeColor = oppositeColor;
	}
	@XmlAttribute
	public String getLatMin() {
		return latMin;
	}
	public void setLatMin(String latMin) {
		this.latMin = latMin;
	}
	@XmlAttribute
	public String getLatMax() {
		return latMax;
	}
	public void setLatMax(String latMax) {
		this.latMax = latMax;
	}
	@XmlAttribute
	public String getLonMin() {
		return lonMin;
	}
	public void setLonMin(String lonMin) {
		this.lonMin = lonMin;
	}
	@XmlAttribute
	public String getLonMax() {
		return lonMax;
	}
	public void setLonMax(String lonMax) {
		this.lonMax = lonMax;
	}

	public void setStop(List<Stop> stop) {
		this.stop = stop;
	}
	public List<Stop> getStop() {
		return stop;
	}
	public void setPath(List<Path> path) {
		this.path = path;
	}
	public List<Path> getPath() {
		return path;
	}
	public void setDirection(List<Direction> direction) {
		this.direction = direction;
	}
	public List<Direction> getDirection() {
		return direction;
	}
	@Override
	public String toString() {
		return "Route [color=" + color + ", direction=" + direction
				+ ", latMax=" + latMax + ", latMin=" + latMin + ", lonMax="
				+ lonMax + ", lonMin=" + lonMin + ", oppositeColor="
				+ oppositeColor + ", path=" + path + ", stop=" + stop
				+ ", tag=" + tag + ", title=" + title + "]";
	}
	
}
