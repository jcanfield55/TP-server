/*
 * @author nirmal
 */
package com.nimbler.tp.service.livefeeds;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
import com.nimbler.tp.dataobject.RealTimePrediction;
import com.nimbler.tp.dataobject.nextbus.VehiclePosition;
import com.nimbler.tp.dataobject.trimet.arrivals.ArrivalType;
import com.nimbler.tp.dataobject.trimet.arrivals.BlockPositionType;
import com.nimbler.tp.dataobject.trimet.arrivals.ResultSet;
import com.nimbler.tp.dataobject.trimet.arrivals.TrimetRealTimePrediction;
import com.nimbler.tp.dataobject.trimet.arrivals.TripType;
import com.nimbler.tp.dataobject.trimet.vehicleposition.TrimetVehicle;
import com.nimbler.tp.gtfs.GtfsDataService;
import com.nimbler.tp.gtfs.TripStopIndex;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.livefeeds.cache.TrimetCachedApiClient;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;
/**
 * Implementation for getting real time data from NextBus real time API for specific leg.
 * @author nIKUNJ
 *
 */
public class TrimetApiImpl implements RealTimeAPI{


	@Autowired 
	private LoggingService logger;


	private String loggerName = "com.nimbler.tp.service.livefeeds";

	private boolean _verbose = false;

	@Autowired
	private TrimetCachedApiClient cachedApiClient;
	@Autowired
	private GtfsDataService gtfsDataService;

	TripStopIndex tripStopIndex; 

	List<String> suportedAencies = null;

	@PostConstruct
	private void init() {
		tripStopIndex = gtfsDataService.getTripStopIndex();
		if(suportedAencies==null){
			suportedAencies = new ArrayList<String>();
			/*	Map<String, RealTimeAPI> map = nimblerApps.getRealTimeApiByAgency();
			for (Map.Entry<String, RealTimeAPI> entry : map.entrySet()) {
				String key = entry.getKey();
				RealTimeAPI value = entry.getValue();
				if(value.getClass().equals(this.getClass())){
					suportedAencies.add(key);
				}
			}*/
			suportedAencies.add("TriMet");
		}
	}

	public enum TrimetArrivalStatus{
		undefined,
		estimated,
		scheduled,
		delayed, 
		canceled;
		public boolean is(String status) {
			return this.toString().equalsIgnoreCase(status);
		}
	}

	@Override
	public LegLiveFeed getLiveFeeds(Leg leg) throws FeedsNotFoundException {
		throw new FeedsNotFoundException("Unsupported request");
	}

	/* (non-Javadoc)
	 * @see com.nimbler.tp.service.livefeeds.RealTimeAPI#getRealTimeFeeds(com.nimbler.tp.dataobject.Leg)
	 */
	@Override
	public LegLiveFeed getAllRealTimeFeeds(Leg leg) throws FeedsNotFoundException {
		LegLiveFeed resp = new LegLiveFeed();
		List<RealTimePrediction> lstRealTimePredictions = new ArrayList<RealTimePrediction>();
		try {			
			Long scheduledTime = leg.getStartTime();
			String routeId = leg.getRouteId();
			String fromStopId = leg.getFrom().getStopId().getId();
			String toStopId = leg.getTo().getStopId().getId();
			String tripId = leg.getTripId();

			if(_verbose){
				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				System.out.println("From Stop: "+leg.getFrom().getStopId()+" -- To Stop: "+leg.getTo().getStopId()+" -- Route: "+routeId+", Trip: "+tripId);
				System.out.println("   Scheduled: "+ scheduledTime+" --> "+getPrettyDate(scheduledTime)+", "+leg.getHeadsign());
			}
			ResultSet resultSet = cachedApiClient.getPredictionAtStop(fromStopId);
			for (ArrivalType  arrival: resultSet.getArrival()) {
				if(!equalsIgnoreCase(String.valueOf(arrival.getRoute()),routeId))//ignore if different route
					continue;
				if(!equalsIgnoreCase(String.valueOf(arrival.getLocid()),fromStopId))//ignore if different stop, possible when queried in bulk
					continue;

				TrimetArrivalStatus status = TrimetArrivalStatus.valueOf(arrival.getStatus());
				TrimetRealTimePrediction prediction = null;
				switch (status) {
				case scheduled:
					prediction = TrimetRealTimePrediction.of(arrival);
					prediction.setEpochTime(arrival.getScheduled());
					prediction.setStatus(status.ordinal());
					lstRealTimePredictions.add(prediction);
					if(_verbose)
						System.out.println(String.format("   on schdul: %s --> %s,Route: %s, Sign: %s ",arrival.getScheduled(),getPrettyDate(arrival.getScheduled()),
								arrival.getRoute(),arrival.getFullSign()));
					break;
				case delayed:
					//fallback to estimated
				case estimated:
					for (BlockPositionType blockPos : arrival.getBlockPosition()) {
						for (TripType trip : blockPos.getTrip()) {
							boolean valid = false;
							if(tripId.equalsIgnoreCase(trip.getTripNum()+"")) // use id same trip id
								valid=true;
							else
								valid = tripStopIndex.isValidStopsForTrip(AGENCY_TYPE.TRIMET, trip.getTripNum()+"", fromStopId, toStopId);
							if(_verbose)
								printPrediction(arrival,leg,trip,valid,status);
							if(valid){
								prediction = TrimetRealTimePrediction.of(arrival);
								prediction.setEpochTime(arrival.getEstimated());
								prediction.setTripId(trip.getTripNum()+"");
								prediction.setDirection(trip.getDir()+"");
								prediction.setStatus(status.ordinal());
								prediction.setScheduleTime(arrival.getScheduled()+"");
								lstRealTimePredictions.add(prediction);	
								break;
							}
						}
					}
					break;
				case canceled:
					//do nothing
					continue;
				default:
					logger.warn(loggerName, "Invalid status of arrival: "+status);
					break;
				} 

			}
			if(_verbose)
				System.out.println("");
			if(lstRealTimePredictions.isEmpty()){
				throw new FeedsNotFoundException("No Valid feeds found in response");
			}
			resp.setEmptyLeg(leg);
			resp.setLstPredictions(lstRealTimePredictions);			
			return resp;
		} catch (Exception e) {
			throw new FeedsNotFoundException("Unknown Exception: "+e);
		}
	}

	@Override
	public List<LegLiveFeed> getAllRealTimeFeeds(List<Leg> legs) throws FeedsNotFoundException {
		List<String> stops = new ArrayList<String>();
		for (Leg leg : legs) {
			if(suportedAencies.contains(leg.getAgencyName())){
				stops.add(leg.getFrom().getStopId().getId());
			}
		}
		try {
			cachedApiClient.getPredictionAtStop(stops);
		} catch (RealTimeDataException e1) {
			logger.info(loggerName, e1.getMessage());
		} catch (ExecutionException e1) {
			logger.info(loggerName, e1.getMessage());
		}
		List<LegLiveFeed> lstRes = new ArrayList<LegLiveFeed>();
		for (Leg leg : legs) {
			try {
				LegLiveFeed legFeed = getAllRealTimeFeeds(leg);
				if (legFeed!=null) 
					lstRes.add(legFeed);
			} catch (FeedsNotFoundException e) {
				logger.info(loggerName, e.getMessage());
			}
		}
		return lstRes;
	}

	/**
	 * Prints the prediction.
	 * Used for debug only
	 */
	private void printPrediction(ArrivalType arrival, Leg leg,TripType trip, boolean valid, TrimetArrivalStatus status) {
		String data = String.format(status+" -  Predicted: %s ,Scheduled: %s , Route:%s, Trip:%s ,diff: %s",
				getPrettyDate(arrival.getEstimated()),getPrettyDate(arrival.getScheduled()),trip.getRoute(),trip.getTripNum(),
				getTimeDiff(arrival));
		if(valid){			
			System.out.println("   "+data);
		}else{
			System.out.println(" X-"+data);
		}
	}

	private String getTimeDiff(ArrivalType arrival) {
		Long est = arrival.getEstimated();
		long scedule = arrival.getScheduled();
		if(est==null)
			return "-";
		return DurationFormatUtils.formatDurationWords(est-scedule, true, true);
	}

	/* (non-Javadoc)
	 * @see com.nimbler.tp.service.livefeeds.RealTimeAPI#getVehiclePosition(com.nimbler.tp.dataobject.Leg)
	 */
	@Override
	public VehiclePosition getVehiclePosition(Leg leg)	throws FeedsNotFoundException {
		try {
			String tripId = leg.getTripId();
			TrimetVehicle vehicle = cachedApiClient.getVehiclePositionForTrip(tripId);
			VehiclePosition position = new VehiclePosition();
			position.setLat(vehicle.getLatitude()+"");
			position.setLon(vehicle.getLongitude()+"");
			position.setDirTag(vehicle.getDirection()+"");
			position.setRouteTag(vehicle.getRouteNumber()+"");
			position.setVehicleId(vehicle.getVehicleID()+"");
			position.setHeadsign(vehicle.getSignMessageLong()+"");
			return position;
		} catch (RealTimeDataException e) {
			throw new FeedsNotFoundException(e.getMessage());
		}
	}




	@Override
	public LegLiveFeed getLegArrivalTime(Leg leg) throws FeedsNotFoundException {
		throw new FeedsNotFoundException("Unsupported request");
	}
	private String getPrettyDate(Long date){
		if(date==null)
			return "-";
		return getPrettyDate(new Date(date));
	}
	private String getPrettyDate(Date date){
		if(date==null)
			return "-";
		SimpleDateFormat dateFormat = new SimpleDateFormat(TpConstants.OTP_DATE_FORMAT+" z");
		return dateFormat.format(date);
	}

	public boolean isVerbose() {
		return _verbose;
	}

	public void setVerbose(boolean _verbose) {
		this._verbose = _verbose;
	}

	public List<String> getSuportedAencies() {
		return suportedAencies;
	}

	public void setSuportedAencies(List<String> suportedAencies) {
		this.suportedAencies = suportedAencies;
	}
}