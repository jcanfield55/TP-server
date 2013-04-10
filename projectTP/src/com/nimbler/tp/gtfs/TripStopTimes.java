package com.nimbler.tp.gtfs;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.onebusaway.gtfs.model.StopTime;

import com.google.common.collect.TreeMultimap;
import com.nimbler.tp.dataobject.StopTimeIdentifier;
/**
 * 
 * @author nirmal
 *
 */
public class TripStopTimes {

	TreeMultimap<String, StopTimeIdentifier> trip_stopTimeIndex = TreeMultimap.create();

	public void addStopTimeForTrip(String tripId,StopTime stopTime) {
		trip_stopTimeIndex.put(tripId, new StopTimeIdentifier(stopTime));
	}
	public void addStopTime(StopTime stopTime) {
		trip_stopTimeIndex.put(stopTime.getTrip().getId().getId(), new StopTimeIdentifier(stopTime));
	}
	public boolean canTraverse(String trip,String fromStopId, String toStopId) {
		StopTimeIdentifier formStop = StopTimeIdentifier.create(fromStopId);
		StopTimeIdentifier toStop = StopTimeIdentifier.create(toStopId);
		SortedSet<StopTimeIdentifier> setOfIdentifiers = trip_stopTimeIndex.get(trip);
		if(setOfIdentifiers==null)
			return false;
		List<StopTimeIdentifier> lstIdentifiers = new ArrayList<StopTimeIdentifier>(setOfIdentifiers); 
		int fromIndex = lstIdentifiers.indexOf(formStop);
		int toIndex = lstIdentifiers.indexOf(toStop);
		if(fromIndex!=-1 && toIndex!=-1 && fromIndex<toIndex){
			return true;
		}
		return false;
	}
	public StopTime traverse(String trip,String fromStopId, String toStopId) {
		StopTimeIdentifier formStop = StopTimeIdentifier.create(fromStopId);
		StopTimeIdentifier toStop = StopTimeIdentifier.create(toStopId);
		SortedSet<StopTimeIdentifier> setOfIdentifiers = trip_stopTimeIndex.get(trip);
		if(setOfIdentifiers==null)
			return null;
		List<StopTimeIdentifier> lstIdentifiers = new ArrayList<StopTimeIdentifier>(setOfIdentifiers); 
		int fromIndex = lstIdentifiers.indexOf(formStop);
		int toIndex = lstIdentifiers.indexOf(toStop);
		if(fromIndex!=-1 && toIndex!=-1 && fromIndex<toIndex){
			return  lstIdentifiers.get(fromIndex).getStopTime();
		}
		return null;
	}
	public TreeMultimap<String, StopTimeIdentifier> getTrip_stopTimeIndex() {
		return trip_stopTimeIndex;
	}
	public void setTrip_stopTimeIndex(
			TreeMultimap<String, StopTimeIdentifier> trip_stopTimeIndex) {
		this.trip_stopTimeIndex = trip_stopTimeIndex;
	}
}
