package com.nimbler.tp.dataobject.bart;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="route")
public class Route {

	private String name;
	private String abbr;
	private String routeID;
	private String number;
	private String color;
	private String origin;
	private String destination;
	private String direction;
	private String holidays;
	private String num_stns;
	private List<Config> config;

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
	public String getRouteID() {
		return routeID;
	}
	public void setRouteID(String routeID) {
		this.routeID = routeID;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public String getHolidays() {
		return holidays;
	}
	public void setHolidays(String holidays) {
		this.holidays = holidays;
	}
	public void setNum_stns(String num_stns) {
		this.num_stns = num_stns;
	}
	public String getNum_stns() {
		return num_stns;
	}

	@XmlElement(name="config")
	public void setConfig(List<Config> config) {
		this.config = config;
	}
	public List<Config> getConfig() {
		return config;
	}
	@Override
	public String toString() {
		return "Route [abbr=" + abbr + ", color=" + color + ", config="
				+ config + ", destination=" + destination + ", direction="
				+ direction + ", holidays=" + holidays + ", name=" + name
				+ ", num_stns=" + num_stns + ", number=" + number + ", origin="
				+ origin + ", routeID=" + routeID + "]";
	}

}
