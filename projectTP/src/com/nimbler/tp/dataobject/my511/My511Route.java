package com.nimbler.tp.dataobject.my511;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Route")
public class My511Route {
	private String name;
	private String code;

	private List<My511RouteDirection> roureDirection;

	@XmlAttribute(name="Name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@XmlAttribute(name="Code")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}


	@XmlElementWrapper(name="RouteDirectionList")
	@XmlElement(name="RouteDirection")
	public List<My511RouteDirection> getRoureDirection() {
		return roureDirection;
	}
	public void setRoureDirection(List<My511RouteDirection> roureDirection) {
		this.roureDirection = roureDirection;
	}
	@Override
	public String toString() {
		return "My511Route [name=" + name + ", code=" + code
				+ ", roureDirection=" + roureDirection + "]";
	}

}
