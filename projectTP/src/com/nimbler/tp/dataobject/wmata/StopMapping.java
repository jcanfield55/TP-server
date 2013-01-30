package com.nimbler.tp.dataobject.wmata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;

/**
 * The Class StopMapping.
 * @author nirmal
 */
public class StopMapping implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6917209651167560128L;
	private Map<String, GtfsStop> busGtfsStopsById;
	private Map<String, BusStop> busApiStopsById;

	/**
	 * @return <Gtfs Stop ID,GtfsStop>
	 */
	private Map<String, GtfsStop> railGtfsStopsById;
	private Map<RailLine, List<RailStation>> railStationByRailLine;

	private Map<String, String> busStopFinalMap = new Hashtable<String, String>();

	/**
	 * <Trip,LastStop id, head>
	 */
	private Map<String, MutablePair<String, String>> tripLastStopAndHead;

	public StopMapping() {
	}

	public List<RailStation> getAllRailStations() {
		List<RailStation> res = null;
		if(railStationByRailLine!=null){
			res = new ArrayList<RailStation>();
			Collection<List<RailStation>> lists =railStationByRailLine.values();
			for (List<RailStation> list : lists) {
				res.addAll(list);
			}
		}
		return res;
	}

	public Map<String, GtfsStop> getBusGtfsStopsById() {
		return busGtfsStopsById;
	}

	public void setBusGtfsStopsById(Map<String, GtfsStop> busGtfsStopsById) {
		this.busGtfsStopsById = busGtfsStopsById;
	}

	public Map<String, BusStop> getBusApiStopsById() {
		return busApiStopsById;
	}

	public void setBusApiStopsById(Map<String, BusStop> busApiStopsById) {
		this.busApiStopsById = busApiStopsById;
	}

	public Map<String, GtfsStop> getRailGtfsStopsById() {
		return railGtfsStopsById;
	}

	public void setRailGtfsStopsById(Map<String, GtfsStop> railGtfsStopsById) {
		this.railGtfsStopsById = railGtfsStopsById;
	}

	public Map<RailLine, List<RailStation>> getRailStationByRailLine() {
		return railStationByRailLine;
	}
	public void addFinalStopMappingForBus(String gtfsStop,String apiStop) {
		busStopFinalMap.put(gtfsStop, apiStop);
	}

	public void setRailStationByRailLine(
			Map<RailLine, List<RailStation>> railStationByRailLine) {
		this.railStationByRailLine = railStationByRailLine;
	}

	public Map<String, MutablePair<String, String>> getTripLastStopAndHead() {
		return tripLastStopAndHead;
	}

	public void setTripLastStopAndHead(
			Map<String, MutablePair<String, String>> tripLastStopAndHead) {
		this.tripLastStopAndHead = tripLastStopAndHead;
	}

	public Map<String, String> getBusStopFinalMap() {
		return busStopFinalMap;
	}

	public void setBusStopFinalMap(Map<String, String> busStopFinalMap) {
		this.busStopFinalMap = busStopFinalMap;
	}



}
