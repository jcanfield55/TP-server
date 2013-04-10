package com.nimbler.tp.dataobject;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;

public class StopTimeIdentifier implements Comparable<StopTimeIdentifier>{
	StopTime stopTime;
	public StopTimeIdentifier(StopTime stopTime) {
		this.stopTime = stopTime;
	}
	public int getIndex() {
		return stopTime.getProxy().getId();
	}

	public StopTime getStopTime() {
		return stopTime;
	}
	public void setStopTime(StopTime stopTime) {
		this.stopTime = stopTime;
	}
	public static StopTimeIdentifier create(String stopid){
		StopTime stopTime = new StopTime();
		Stop stop = new Stop();
		stop.setId(new AgencyAndId("",stopid));
		stopTime.setStop(stop);
		return new StopTimeIdentifier(stopTime);
	}
	public String getStopId() {
		return stopTime.getStop().getId().getId();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stopTime.getStop().getId().getId() == null) ? 0 : stopTime.getStop().getId().getId().hashCode());
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
		StopTimeIdentifier other = (StopTimeIdentifier) obj;
		if (stopTime.getStop().getId().getId() == null) {
			if (other.stopTime.getStop().getId().getId() != null)
				return false;
		} else if (!stopTime.getStop().getId().getId().equalsIgnoreCase(other.getStopId()))
			return false;
		return true;
	}
	@Override
	public int compareTo(StopTimeIdentifier o) {
		return stopTime.compareTo(o.stopTime);
	}
	@Override
	public String toString() {
		return "StopTimeIdentifier [" +stopTime.getStop().getId().getId() + "]";
	}


}
