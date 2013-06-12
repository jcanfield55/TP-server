package com.nimbler.tp.dataobject.wmata;

import java.util.ArrayList;
import java.util.List;


/**
 * The Class WmataRouteDetails.
 *
 * @author nirmal
 * @since May 3, 2013
 */
public class WmataRouteDetails {
	WmataDirection Direction0;
	WmataDirection Direction1;
	WmataDirection Direction2;
	WmataDirection Direction3;
	String RouteID;
	String Name;
	List<WmataDirection> lstDirections = new ArrayList<WmataDirection>();
	public WmataDirection getDirection0() {
		return Direction0;
	}
	public void setDirection0(WmataDirection direction0) {
		Direction0 = direction0;
	}
	public WmataDirection getDirection1() {
		return Direction1;
	}
	public void setDirection1(WmataDirection direction1) {
		Direction1 = direction1;
	}
	public String getRouteID() {
		return RouteID;
	}
	public void setRouteID(String routeID) {
		RouteID = routeID;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public WmataDirection getDirection2() {
		return Direction2;
	}
	public void setDirection2(WmataDirection direction2) {
		Direction2 = direction2;
	}
	public WmataDirection getDirection3() {
		return Direction3;
	}
	public void setDirection3(WmataDirection direction3) {
		Direction3 = direction3;
	}
	@Override
	public String toString() {
		return "WmataRouteDetails [Direction0=" + Direction0 + ", Direction1="
				+ Direction1 + ", Direction2=" + Direction2 + ", Direction3="
				+ Direction3 + "]";
	}
	public int size() {
		return lstDirections.size();
	}

	public List<WmataDirection> getLstDirections() {
		return lstDirections;
	}
	public void setLstDirections(List<WmataDirection> lstDirections) {
		this.lstDirections = lstDirections;
	}
	public void addDirection(WmataDirection directions) {
		this.lstDirections.add(directions);
	}
	public void addAllDirection(List<WmataDirection> lstDirections) {
		this.lstDirections.addAll(lstDirections);
	}
	public void pack(String sortName) {
		lstDirections.clear();
		if(Direction0!=null)lstDirections.add(Direction0);
		if(Direction1!=null)lstDirections.add(Direction1);
		if(Direction2!=null)lstDirections.add(Direction2);
		if(Direction3!=null)lstDirections.add(Direction3);
		for (WmataDirection direction : lstDirections) {
			direction.setApiRouteId(RouteID);
			direction.setGtfsRouteId(sortName);
		}
	}
}
