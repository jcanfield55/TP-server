package com.nimbler.tp.dataobject.nextbus;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="predictions")
public class Predictions {
	private String agencyTitle;
	private String routeTitle;
	private String routeTag;
	private String stopTitle;
	private String stopTag;
	private String dirTitleBecauseNoPredictions;
	private List<Direction> direction;
	
	@XmlAttribute
	public String getAgencyTitle() {
		return agencyTitle;
	}
	public void setAgencyTitle(String agencyTitle) {
		this.agencyTitle = agencyTitle;
	}
	@XmlAttribute
	public String getRouteTitle() {
		return routeTitle;
	}
	public void setRouteTitle(String routeTitle) {
		this.routeTitle = routeTitle;
	}
	@XmlAttribute
	public String getRouteTag() {
		return routeTag;
	}
	public void setRouteTag(String routeTag) {
		this.routeTag = routeTag;
	}
	@XmlAttribute
	public String getStopTitle() {
		return stopTitle;
	}
	public void setStopTitle(String stopTitle) {
		this.stopTitle = stopTitle;
	}
	@XmlAttribute
	public String getStopTag() {
		return stopTag;
	}
	public void setStopTag(String stopTag) {
		this.stopTag = stopTag;
	}
	@XmlAttribute
	public String getDirTitleBecauseNoPredictions() {
		return dirTitleBecauseNoPredictions;
	}
	public void setDirTitleBecauseNoPredictions(String dirTitleBecauseNoPredictions) {
		this.dirTitleBecauseNoPredictions = dirTitleBecauseNoPredictions;
	}
	@Override
	public String toString() {
		return "Predictions [agencyTitle=" + agencyTitle
				+ ", dirTitleBecauseNoPredictions="
				+ dirTitleBecauseNoPredictions + ", routeTag=" + routeTag
				+ ", routeTitle=" + routeTitle + ", stopTag=" + stopTag
				+ ", stopTitle=" + stopTitle + "]";
	}
	public void setDirection(List<Direction> direction) {
		this.direction = direction;
	}
	public List<Direction> getDirection() {
		return direction;
	}
	
}
