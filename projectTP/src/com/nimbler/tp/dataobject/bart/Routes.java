package com.nimbler.tp.dataobject.bart;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="routes")
public class Routes {
	
	private List<Route> route;

	public void setRoute(List<Route> route) {
		this.route = route;
	}

	public List<Route> getRoute() {
		return route;
	}

	@Override
	public String toString() {
		return "Routes [route=" + route + "]";
	}
	
}
