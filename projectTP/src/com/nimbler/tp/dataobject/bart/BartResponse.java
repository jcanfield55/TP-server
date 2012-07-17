package com.nimbler.tp.dataobject.bart;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="root")
public class BartResponse {

	private String uri;
	private String date;
	private String time;
	private String sched_num;
	private List<Routes> routes;
	private List<Station> station;
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}


	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	//@XmlElementWrapper(name="stations")

	public void setSched_num(String sched_num) {
		this.sched_num = sched_num;
	}

	public String getSched_num() {
		return sched_num;
	}

	@XmlElement(name="routes")
	public void setRoutes(List<Routes> routes) {
		this.routes = routes;
	}

	public List<Routes> getRoutes() {
		return routes;
	}

	public void setStation(List<Station> station) {
		this.station = station;
	}
	@XmlElement(name="station")
	public List<Station> getStation() {
		return station;
	}

	@Override
	public String toString() {
		return "Root [date=" + date + ", routes=" + routes + ", sched_num="
				+ sched_num + ", station=" + station + ", time=" + time
				+ ", uri=" + uri + "]";
	}
	
}
