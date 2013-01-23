package com.nimbler.tp.dataobject.wmata;

import java.io.Serializable;

/**
 * The Class Point.
 * @author nirmal
 */
public class Point implements Serializable{
	private static final long serialVersionUID = -9218298428626406746L;
	protected double x = Double.NaN;
	protected double y=Double.NaN;


	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public double fastdistance(Point p){
		double dx = x - p.x;
		double dy = y - p.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public double getX() {
		return x;
	}


	public void setX(double x) {
		this.x = x;
	}


	public double getY() {
		return y;
	}


	public void setY(double y) {
		this.y = y;
	}


	public Point() {
	}
	@Override
	public String toString() {
		return "Point <"+ x + ", " + y+ ">";
	}
}
