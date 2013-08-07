package com.nimbler.tp.dataobject.my511;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="RouteDirection")
public class My511RouteDirection {
	private String name;
	private String code;

	List<My511Stop> stops;

	@XmlAttribute(name="Name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@XmlElementWrapper(name="StopList")
	@XmlElement(name="Stop")
	public List<My511Stop> getStops() {
		return stops;
	}
	public void setStops(List<My511Stop> stops) {
		this.stops = stops;
	}
	@XmlAttribute(name="Code")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	@Override
	public String toString() {
		return "My511RouteDirection [name=" + name + ", code=" + code
				+ ", stops=" + stops + "]";
	}


}
