package com.nimbler.tp.dataobject.wmata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;


public class GtfsStop extends Point implements Serializable{

	public enum GTFS_STOP_TYPE{
		UNDEFINED,
		BUS,
		RAIL
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -3741131131032567594L;

	private String stopId;
	private String stopName;
	private String lat;
	private String lon;

	private List<BusStop> lstBusStops;
	private List<RailStation> lstRailStations;

	private int type = 0;;

	public GtfsStop(String gtfsStopId, String gtfsStopName,String lat,String lon,int type) {
		super(NumberUtils.toDouble(lat),NumberUtils.toDouble(lon));
		this.lat = lat;
		this.lon = lon;
		this.stopId = gtfsStopId;
		this.stopName = gtfsStopName;
		this.type = type;
	}
	public String getStopId() {
		return stopId;
	}
	public void addStop(BusStop stop) {
		if(lstBusStops==null)
			lstBusStops = new ArrayList<BusStop>();
		lstBusStops.add(stop);
	}

	public void addRailStop(RailStation stop) {
		if(lstRailStations==null)
			lstRailStations = new ArrayList<RailStation>();
		if(!lstRailStations.contains(stop))
			lstRailStations.add(stop);
	}
	public Set<String> getRailStopIds() {
		Set<String> res = new HashSet<String>();
		if(lstRailStations!=null){
			for (RailStation railStation : lstRailStations) {
				res.addAll(railStation.getAllStationCodes());
			}
		}
		return res;
	}

	public void setStopId(String stopId) {
		this.stopId = stopId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public List<BusStop> getBusStopsForRoute(String routeShortName) {
		List<BusStop> res = null;
		if(lstBusStops!=null){
			for (BusStop stop : lstBusStops) {
				if(stop.haveRoute(routeShortName,true)){
					if(res==null)
						res = new ArrayList<BusStop>();
					res.add(stop);
				}
			}
		}
		return res;
	}
	public List<String> getBusStopIdsForRoute(String routeShortName) {
		List<String> res = null;
		if(lstBusStops!=null){
			for (BusStop stop : lstBusStops) {
				if(stop.haveRoute(routeShortName,true)){
					if(res==null)
						res = new ArrayList<String>();
					res.add(stop.getStopId());
				}
			}
		}
		return res;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result = prime * result
				+ ((stopName == null) ? 0 : stopName.hashCode());
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
		GtfsStop other = (GtfsStop) obj;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		if (stopName == null) {
			if (other.stopName != null)
				return false;
		} else if (!stopName.equals(other.stopName))
			return false;
		return true;
	}
	public String getStopName() {
		return stopName;
	}

	public void setStopName(String stopName) {
		this.stopName = stopName;
	}

	public String getLat() {
		return lat;
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

	public double getY() {
		if(Double.isNaN(y) && lon!=null)
			setY(NumberUtils.toDouble(lon));
		return super.getY();
	}
	public String getLon() {
		return lon;
	}
	public List<BusStop> getLstBusStops() {
		return lstBusStops;
	}
	public void setLstBusStops(List<BusStop> lstStops) {
		this.lstBusStops = lstStops;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}
	public List<RailStation> getLstRailStations() {
		return lstRailStations;
	}
	public void setLstRailStations(List<RailStation> lstRailStations) {
		this.lstRailStations = lstRailStations;
	}
	@Override
	public String toString() {
		return "GtfsStop ["
				+ (stopId != null ? "stopId=" + stopId + ", " : "")
				+ (stopName != null ? "stopName=" + stopName + ", " : "")
				+ (lat != null ? "lat=" + lat + ", " : "")
				+ (lon != null ? "lon=" + lon + ", " : "")
				+ (lstBusStops != null ? "lstBusStops=" + lstBusStops + ", "
						: "")
						+ (lstRailStations != null ? "lstRailStations="
								+ lstRailStations + ", " : "") + "type=" + type + "]";
	}
}
