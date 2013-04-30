/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;

/**
 * 
 * @author nirmal
 * Contains non repeating indexed stop sequence for trips of perticular route
 * Sunset trips will be eliminated
 */
public class TripStopIndex {
	/**
	 * route id, list of stop id
	 */
	private Map<AGENCY_TYPE, Map<String,List<String>>> trip_stopTimes = new HashMap<AGENCY_TYPE, Map<String,List<String>>>();

	/**
	 * Save.
	 *
	 * @param route_trip the route_trip
	 * @param tripStopTimesArray the trip stop times array
	 */
	public void save(AGENCY_TYPE type,Map<String,List<String>> data) {
		trip_stopTimes.put(type, data);
	}

	/**
	 * Checks if is inter mediate stop id.
	 *
	 * @param routeId the route id
	 * @param from the source
	 * @param to the destination
	 * @param intermediate the intermediate
	 * @return true, if is inter mediate stop id
	 */
	public boolean isValidStopsForTrip(AGENCY_TYPE type,String tripId,String from,String to) {
		Map<String, List<String>> trip_st = trip_stopTimes.get(type);
		if(trip_st!=null){
			List<String> stops = trip_st.get(tripId);
			if(stops!=null){
				int fromIndex = stops.indexOf(from.toLowerCase());
				int toIndex = stops.indexOf(to.toLowerCase());						
				if(fromIndex !=-1 && toIndex!=-1 && fromIndex<toIndex){
					return true;
				}
			}
		}
		return false;
	}

	public void save(AGENCY_TYPE agancy, GtfsRelationalDaoImpl context, Transformer tripIdTransformer) {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (Trip trip : context.getAllTrips()) {
			List<StopTime>  stoptimes = context.getStopTimesForTrip(trip);
			if(stoptimes!=null){
				List<String> stops = new ArrayList<String>();
				for (StopTime stopTime : stoptimes) {
					stops.add(stopTime.getStop().getId().getId());
				}
				if(tripIdTransformer!=null)
					map.put((String) tripIdTransformer.transform(trip), stops);
				else
					map.put(trip.getId().getId(), stops);
			}
		}
		trip_stopTimes.put(agancy, map);
	}
}
