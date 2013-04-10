/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.SetMultimap;
import com.nimbler.tp.util.ComUtils;

/**
 * 
 * @author nirmal
 * Contains non repeating indexed stop sequence for trips of perticular route
 * Sunset trips will be eliminated
 */
public class RouteStopIndex {
	/**
	 * route id, list of stop id
	 */
	private ArrayListMultimap<String, List> routeStopIndexList = ArrayListMultimap.create();

	/**
	 * Save.
	 *
	 * @param route_trip the route_trip
	 * @param tripStopTimesArray the trip stop times array
	 */
	public void save(SetMultimap<String, String> route_trip,ArrayListMultimap<String, String> tripStopTimesArray) {
		for (String route : route_trip.keySet()) {
			Set<String> trips = route_trip.get(route);
			for (String trip : trips) {
				routeStopIndexList.put(route, tripStopTimesArray.get(trip));
			}
		}	
		List<String> itr = new ArrayList<String>(routeStopIndexList.keySet());
		for (String route : itr) {
			List[] lst = (List[]) routeStopIndexList.get(route).toArray(new List[0]);
			List[] temp =  ArrayUtils.clone(lst);
			OUTER: for (int i = 0; i < lst.length; i++) {
				List first = lst[i];
				if(first==null)
					continue;
				for (int j = 0; j < temp.length; j++) {
					List sec = temp[j];
					if(sec==null || i==j)
						continue;
					if(sec.containsAll(first)){
						lst[i] = null;
						temp[i] = null;
						continue OUTER;
					}
				}
			}
			routeStopIndexList.replaceValues(route,ComUtils.getListFromArray(lst));
		}
	}

	/**
	 * Checks if is inter mediate stop id.
	 *
	 * @param routeId the route id
	 * @param source the source
	 * @param destination the destination
	 * @param intermediate the intermediate
	 * @return true, if is inter mediate stop id
	 */
	public boolean isInterMediateStopId(String routeId,String source,String destination, String intermediate) {
		List<List> stopTimes = routeStopIndexList.get(routeId);
		for (List stopTime : stopTimes) {
			int fromIndex = stopTime.indexOf(source.toLowerCase());
			int toIndex = stopTime.indexOf(intermediate.toLowerCase());
			int dest = stopTime.indexOf(destination.toLowerCase());			
			if(fromIndex !=-1 && toIndex!=-1 && dest!=-1){
				if((fromIndex<toIndex && toIndex<=dest ) || 
						(toIndex<fromIndex && dest<=toIndex )){
					return true;
				}
			}
		}
		return false;
	}
}
