/*
 * @author nirmal
 */
package com.nimbler.tp.dataobject.my511;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class My511Agency.
 *
 * @author nirmal
 */
@XmlRootElement(name="Agency")
public class My511Agency implements Serializable{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -8684713085418048013L;
	private String name;
	private String hasDirection;
	private String mode;

	private List<My511Route> routes;


	@XmlElementWrapper(name="RouteList")
	@XmlElement(name="Route")
	public List<My511Route> getRoutes() {
		return routes;
	}


	public void setRoutes(List<My511Route> routes) {
		this.routes = routes;
	}


	@XmlAttribute(name="Name")
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(name="HasDirection")
	public String getHasDirection() {
		return hasDirection;
	}
	public void setHasDirection(String hasDirection) {
		this.hasDirection = hasDirection;
	}
	@XmlAttribute(name="Mode")
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}

	@Override
	public String toString() {
		return "My511Agency [name=" + name + ", hasDirection=" + hasDirection
				+ ", mode=" + mode + ", routes=" + routes + "]";
	}


}
