package com.nimbler.tp.dataobject.nextbus;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="path")
public class Path {
	private List<Point> point;

	public void setPoint(List<Point> point) {
		this.point = point;
	}

	public List<Point> getPoint() {
		return point;
	}

	@Override
	public String toString() {
		return "Path [point=" + point + "]";
	}
}
