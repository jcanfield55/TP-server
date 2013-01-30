package com.nimbler.tp.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.common.StopNotFoundException;
import com.nimbler.tp.dataobject.wmata.BusStop;
import com.nimbler.tp.dataobject.wmata.GtfsStop;
import com.nimbler.tp.dataobject.wmata.Point;
import com.nimbler.tp.dataobject.wmata.RailLine;
import com.nimbler.tp.dataobject.wmata.RailPrediction;
import com.nimbler.tp.dataobject.wmata.RailPrediction.RAIL_MIN_VALUES;
import com.nimbler.tp.dataobject.wmata.RailStation;
import com.nimbler.tp.dataobject.wmata.StopMapping;
import com.nimbler.tp.dataobject.wmata.WmataBusPrediction;
/**
 * 
 * @author nirmal
 *
 */
public class WmataUtil {

	/**
	 * Gets the line code by display name.
	 *
	 * @param railLines the rail lines
	 * @param displayName the display name
	 * @return the line code by display name
	 */
	public static String getLineCodeByDisplayName(Collection<RailLine> railLines,String displayName) {
		if(railLines!=null){
			for (RailLine railLine : railLines) {
				if(railLine.getDisplayName().equalsIgnoreCase(displayName))
					return railLine.getLineCode();
			}
		}
		return null;
	}

	/**
	 * Gets the closest estimation.
	 *
	 * @param lstPredictions the estimates
	 * @param scheduledTime the scheduled time
	 * @param index the index
	 * @return the Pair <match differance,Estimated time>, -ve differance means early
	 */
	public static MutablePair<Integer, Long> getClosestEstimationForBus(List<WmataBusPrediction> lstPredictions, long scheduledTime, int index,int clampEarly,int clampDelay) {
		if (lstPredictions==null || lstPredictions.size()==0)
			return null;
		Map<Integer, MutablePair<Integer, Long>> diffToMiutes = new TreeMap<Integer, MutablePair<Integer, Long>>();
		for (WmataBusPrediction p: lstPredictions) {
			int deviation = NumberUtils.toInt(p.getMinutes(), -1);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, deviation);
			Long estimatedDepartureTime = cal.getTimeInMillis();
			int diff = (int) (estimatedDepartureTime - scheduledTime);
			if(diff<0 && Math.abs(diff)>(clampEarly*DateUtils.MILLIS_PER_MINUTE))
				continue;
			else if(diff>0 && diff>(clampDelay*DateUtils.MILLIS_PER_MINUTE))
				continue;
			diffToMiutes.put(Math.abs(diff), new MutablePair<Integer, Long>(diff, estimatedDepartureTime)); //-ve diff --> early.
		}
		int currIndex = 0;
		for (Map.Entry<Integer, MutablePair<Integer, Long>> entry : diffToMiutes.entrySet()) {
			MutablePair<Integer, Long> value = entry.getValue();
			if(currIndex ==index){
				return value;
			}
		}
		return null;
	}
	public static MutablePair<Integer, Long> getClosestEstimationForRail(List<RailPrediction> estimates, long scheduledTime, int index,int clampEarly,int clampDelay) {
		if (estimates==null || estimates.size()==0)
			return null;
		Map<Integer, MutablePair<Integer, Long>> diffToMiutes = new TreeMap<Integer, MutablePair<Integer, Long>>();
		for (RailPrediction estimate: estimates) {
			int deviation = -1;
			if(RAIL_MIN_VALUES.ARR.name().equalsIgnoreCase(estimate.getMin())){
				deviation = 0; // arrived
			}else if(RAIL_MIN_VALUES.BRD.name().equalsIgnoreCase(estimate.getMin())){
				deviation = 0;// boarded
			}else{
				deviation = NumberUtils.toInt(estimate.getMin(), -1);
			}
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, deviation);
			Long estimatedDepartureTime = cal.getTimeInMillis();
			int diff = (int) (estimatedDepartureTime - scheduledTime);
			if(diff<0 && Math.abs(diff)>(clampEarly*DateUtils.MILLIS_PER_MINUTE))
				continue;
			else if(diff>0 && diff>(clampDelay*DateUtils.MILLIS_PER_MINUTE))
				continue;
			diffToMiutes.put(Math.abs(diff), new MutablePair<Integer, Long>(diff, estimatedDepartureTime)); //-ve diff --> early.
		}
		int currIndex = 0;
		for (Map.Entry<Integer, MutablePair<Integer, Long>> entry : diffToMiutes.entrySet()) {
			MutablePair<Integer, Long> value = entry.getValue();
			if(currIndex ==index){
				return value;
			}
		}
		return null;
	}

	/**
	 * Gets the wmata stop from gtfs stop and line.
	 *
	 * @param fromStopId - gtfs stop id
	 * @param routeTag- e.g Blue, Orange
	 * @param stopMapping the stop mapping
	 * @return the wmata stop from gtfs stops
	 * @throws RealTimeDataException
	 */
	public static  List<String> getWmataStopFromGtfsStopAndLine(String fromStopId,String routeTag, StopMapping stopMapping) throws StopNotFoundException {
		GtfsStop gtfsStop = stopMapping.getRailGtfsStopsById().get(fromStopId);
		if(gtfsStop==null || ComUtils.isEmptyList(gtfsStop.getLstRailStations()))
			throw new StopNotFoundException("Could not find gtfs stop id:"+fromStopId+" from mapping");
		Set<String> res = new HashSet<String>();
		String lineCode = getLineCodeByDisplayName(stopMapping.getRailStationByRailLine().keySet(), routeTag);
		for (RailStation rs : gtfsStop.getLstRailStations()) {
			if(rs.haveLine(lineCode,true))
				res.add(rs.getCode());
		}
		if(res.isEmpty()){
			//			List<String> allCodes = new ArrayList<String>();
			//			for (RailStation rs : gtfsStop.getLstRailStations()) {
			//				allCodes.addAll(rs.getAllStationLineCodes());
			//			}
			//			throw new StopNotFoundException("Could not find line:"+lineCode+" from gtfs stop id:"+fromStopId+" , Expected: "+StringUtils.join(allCodes,","));
			if(gtfsStop.getLstRailStations()!=null && ! gtfsStop.getLstRailStations().isEmpty())
				res.add(gtfsStop.getLstRailStations().get(0).getCode());//get first station
			else
				throw new StopNotFoundException("Could not stop for  gtfs stop id:"+fromStopId);
		}
		return new ArrayList<String>(res);
	}

	/**
	 * Gets the bus stop id from gtfs stop.
	 *
	 * @param gtfsFromStop the gtfs from stop
	 * @param routeTag the route tag
	 * @param stopMapping the stop mapping
	 * @return the bus stop id from gtfs stop
	 * @throws RealTimeDataException the real time data exception
	 */
	public static List<BusStop> getBusStopIdFromGtfsStop(String gtfsFromStop,String routeTag, StopMapping stopMapping) throws StopNotFoundException {
		Map<String, GtfsStop> stopIdMap = stopMapping.getBusGtfsStopsById();
		GtfsStop gtfsStop = stopIdMap.get(gtfsFromStop);
		if(gtfsStop==null)
			throw new StopNotFoundException("No Gtfs Stop Found :"+gtfsFromStop+" in mapping while finding for route: "+routeTag);

		List<BusStop> stops = gtfsStop.getBusStopsForRoute(routeTag);
		if(ComUtils.isEmptyList(stops))
			throw new StopNotFoundException("No NextBus Stop Found for gtfs stop:"+gtfsFromStop+", route:"+routeTag);
		return stops;
	}

	/**
	 * Filter if one stop name matched.
	 *
	 * @param gtfsStop the gtfs stop
	 * @param updateUbject the update ubject
	 * @return the list
	 */
	public static List<BusStop> filterIfOneStopNameMatched(GtfsStop gtfsStop,boolean updateUbject) {
		String gtfsStopName = gtfsStop.getStopName().replace(" & ", " + ");
		boolean match = false;
		for (BusStop busStop : gtfsStop.getLstBusStops()) {
			if(StringUtils.equalsIgnoreCase(busStop.getName(), gtfsStopName)){
				match = true;
				break;
			}
		}
		if(match){
			List<BusStop> filteredList = new ArrayList<BusStop>(gtfsStop.getLstBusStops());
			for (Iterator iterator = filteredList.iterator(); iterator.hasNext();) {
				BusStop busStop = (BusStop) iterator.next();
				if(!StringUtils.equalsIgnoreCase(busStop.getName(), gtfsStopName)){
					iterator.remove();
				}
			}
			if(updateUbject)
				gtfsStop.setLstBusStops(filteredList);
			return filteredList;
		}
		return gtfsStop.getLstBusStops();
	}

	/**
	 * Filter stops by distance.
	 *
	 * @param p the p
	 * @param lstStops the lst stops
	 * @param maxDistance the max distance
	 * @return the list
	 */
	public static List filterStopsByDistance(Point p, Set lstStops,double maxDistance) {
		TreeMap<Double, Set<Point>> sortedMap = new TreeMap<Double, Set<Point>>();
		for (Object obj : lstStops) {
			Point stop = (Point) obj;
			double distance = DistanceLibrary.distance(p.getX(),p.getY(),stop.getX(),stop.getY());
			Set lst = sortedMap.get(distance);
			if(lst==null){
				lst = new HashSet<Point>();
				sortedMap.put(distance, lst);
			}
			lst.add(stop);
			if(sortedMap.size()>5){
				double lastKey = sortedMap.lastKey();
				if(lastKey>maxDistance)
					sortedMap.remove(lastKey);
			}
		}
		List res = new ArrayList();
		for (Map.Entry<Double, Set<Point>> entry : sortedMap.entrySet()) {
			Double distance = entry.getKey();
			Set<Point> value = entry.getValue();
			if(distance<=maxDistance || res.size()==0)
				res.addAll(value);
			else
				break;
		}
		return res;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List filterOneStopByDistance(Point p, Set lstStops,double maxDistance) {
		double min = -1;
		Set<Point> set = new HashSet<Point>();
		for (Object obj : lstStops) {
			Point stop = (Point) obj;
			double distance = DistanceLibrary.distance(p.getX(),p.getY(),stop.getX(),stop.getY());
			if(min==-1 || distance<min){
				min=distance;
				set = new HashSet<Point>();
				set.add(stop);
			}else if(min == distance){
				set.add(stop);
				System.out.println("same....");
			}
		}
		return new ArrayList(set);
	}
}
