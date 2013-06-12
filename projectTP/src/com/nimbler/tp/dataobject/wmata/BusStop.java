package com.nimbler.tp.dataobject.wmata;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.annotations.SerializedName;


@XmlRootElement(name="Stop")
public class BusStop extends Point implements Serializable{

	/**
	 * 
	 */

	private static final long serialVersionUID = -2719346337897471149L;
	public BusStop() {
	}

	@SerializedName("Lat")
	private String lat;
	@SerializedName("Lon")
	private String lon;
	@SerializedName("Name")
	private String name;

	@SerializedName("StopID")
	private String stopId;

	@SerializedName("Routes")
	private List<String> routes;

	private List<String> gtfsRoutesId;

	private double gtfsDistance;
	private int usedCount = 0;
	private String headSign;
	private String route;


	public String getLat() {
		return lat;
	}


	public String getRoute() {
		return route;
	}

	public List<String> getGtfsRoutesId() {
		return gtfsRoutesId;
	}


	public void setGtfsRoutesId(List<String> gtfsRoutesId) {
		this.gtfsRoutesId = gtfsRoutesId;
	}


	public void setRoute(String route) {
		this.route = route;
	}


	public double getGtfsDistance() {
		return gtfsDistance;
	}



	public String getHeadSign() {
		return headSign;
	}

	public void setHeadSign(String headSign) {
		this.headSign = headSign;
	}

	public BusStop(BusStop s) {
	}

	public void setGtfsDistance(double gtfsDistance) {
		this.gtfsDistance = gtfsDistance;
	}


	public boolean idMapped() {
		return stopId!=null;
	}


	public void setLat(String lat) {
		this.lat = lat;
	}
	@Override
	public double getX() {
		if(Double.isNaN(x) && lat!=null)
			setX(NumberUtils.toDouble(lat));
		return super.getX();
	}

	@Override
	public double getY() {
		if(Double.isNaN(y) && lon!=null)
			setY(NumberUtils.toDouble(lon));
		return super.getY();
	}


	public String getLon() {
		return lon;
	}


	public void setLon(String lon) {
		this.lon = lon;
	}


	public String getName() {
		return name;
	}
	public boolean haveRoute(String route,boolean ignoreCase) {
		if(routes!=null){
			if(!ignoreCase){
				return route.contains(route);
			}else{
				for (String r : routes) {
					if(r.equalsIgnoreCase(route))
						return true;
				}
			}
		}
		return false;
	}

	public boolean haveRoute(String route) {
		return haveRoute(route, false);
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getStopId() {
		return stopId;
	}
	public synchronized void markUsed() {
		usedCount++;
	}


	public void setStopId(String stopId) {
		this.stopId = stopId;
	}

	public int getUsedCount() {
		return usedCount;
	}

	public List<String> getRoutes() {
		return routes;
	}


	public void setRoutes(List<String> routes) {
		this.routes = routes;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BusStop other = (BusStop) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BusStop [lat=" + lat + ", lon=" + lon + ", name=" + name
				+ ", stopId=" + stopId + ", routes=" + routes
				+ ", gtfsDistance=" + gtfsDistance + ", usedCount=" + usedCount
				+ ", headSign=" + headSign + ", route=" + route + "]";
	}
}
