/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs.stopindex;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

import com.nimbler.tp.gtfs.GtfsBundle;
import com.nimbler.tp.gtfs.TripStopIndex;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;

/**
 * The Interface TripStopIndexBuilder.
 *
 * @author nirmal
 * 
 */
public abstract class TripStopIndexBuilder {
	List<Class<?>> lstClasses = new ArrayList<Class<?>>();
	AGENCY_TYPE agency;
	{
		lstClasses.add(Agency.class);
		lstClasses.add(Stop.class);
		lstClasses.add(Route.class);
		lstClasses.add(Trip.class);
		lstClasses.add(StopTime.class);
	}
	/**
	 * Builds the index.
	 *
	 * @param stopIndex the stop index
	 * @param bundle the bundle
	 * @throws  
	 */
	public abstract void buildIndex(TripStopIndex stopIndex,List<GtfsBundle> bundle) throws RuntimeException;

	/**
	 * Sets the agency.
	 *
	 * @param agency the new agency
	 */
	public void setAgency(AGENCY_TYPE age){;
	this.agency = age;
	}

	public void setClassesToLoad(List<Class<?>> classes){
		this.lstClasses = classes;
	}


}
