package com.nimbler.tp.dataobject.my511;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Stop")
public class My511Stop {
	private String name;
	private String code;
	private List<Integer> departureTimes;

	@XmlAttribute(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@XmlElementWrapper(name="DepartureTimeList")
	@XmlElement(name="DepartureTime")
	public List<Integer> getDepartureTimes() {
		return departureTimes;
	}
	public void setDepartureTimes(List<Integer> departureTimes) {
		this.departureTimes = departureTimes;
	}
	@XmlAttribute(name="StopCode")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	@Override
	public String toString() {
		return "My511Stop [name=" + name + ", code=" + code
				+ ", departureTimes=" + departureTimes + "]";
	}

}
