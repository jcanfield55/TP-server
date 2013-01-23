/*
 * 
 */
package com.nimbler.tp.service.livefeeds;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.common.StopNotFoundException;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
import com.nimbler.tp.dataobject.TraverseMode;
import com.nimbler.tp.dataobject.wmata.BusStop;
import com.nimbler.tp.dataobject.wmata.Predictions;
import com.nimbler.tp.dataobject.wmata.RailPrediction;
import com.nimbler.tp.dataobject.wmata.StopMapping;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.livefeeds.cache.WmataCachedApiClient;
import com.nimbler.tp.service.livefeeds.stub.WmataApiClient;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.ETA_FLAG;
import com.nimbler.tp.util.WmataUtil;
/**
 * Implementation class for processing WMATA real time data for a specif OTP leg.
 * @author nirmal
 *
 */
public class WmataApiImpl implements RealTimeAPI {

	@Autowired
	private LoggingService logger;
	private String loggerName = "com.nimbler.tp.service.livefeeds";

	private static final String ESTERN_TIMEZONE = "US/Eastern";
	/**
	 * Max min for early that can be used in minutes,can be updated from bean
	 */
	private int maxEarlyThresold = 5;
	/**
	 * Max min for delay that can be used in minutes,can be updated from bean
	 */
	private int maxDelayThresold = 30;
	/**
	 * 
	 */
	private int maxValidForEarly = 1;
	/**
	 * 
	 */
	private int maxValidForDelay = 2;

	private String apiKey= "wateq3gxqzb9s597qky6khd7";

	private String stopMapFilePath= "C:/wmata_data/";
	private StopMapping stopMapping = null;

	private Map<String, String> busDirectionOverride;
	@Autowired
	private WmataApiClient apiClient;

	@Autowired
	private WmataCachedApiClient cachedApiClient;
	private boolean _verbose = true;

	@PostConstruct
	private void readStopMapping() {
		try {
			if(stopMapFilePath==null)
				System.out.println("No gtfs stop mapping file found...");
			ObjectInputStream outputStream = new ObjectInputStream(new FileInputStream(stopMapFilePath+"/StopMap.obj"));
			System.out.println("Reading WMATA stop mapping....");
			stopMapping =  (StopMapping) outputStream.readObject();
			System.out.println("WMATA Stop Read Complete.... ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public LegLiveFeed getLiveFeeds(Leg leg) throws FeedsNotFoundException {
		LegLiveFeed resp = null;
		try {
			Long scheduledTime = leg.getStartTime();
			if (scheduledTime < System.currentTimeMillis())
				throw new RealTimeDataException("Leg scheduled time is already passed. No estimates possible.");

			String agencyId = leg.getAgencyId();
			String gtfsFromStop = leg.getFrom().getStopId().getId();
			String toStopId = leg.getTo().getStopId().getId();
			String routeTag = leg.getRoute().trim();
			String gtfsTtipId = leg.getTripId().trim();
			String mode = leg.getMode();

			if(StringUtils.equalsIgnoreCase(mode,TraverseMode.BUS.name())){
				return getBusRealTimeData(gtfsFromStop, gtfsTtipId, routeTag, scheduledTime);
			}else if(StringUtils.equalsIgnoreCase(mode,TraverseMode.SUBWAY.name())){
				List<String> lstApiFromStop = WmataUtil.getWmataStopFromGtfsStopAndLine(gtfsFromStop,routeTag,stopMapping);
				if(ComUtils.isEmptyList(lstApiFromStop))
					throw new RealTimeDataException("No gtfs fomr stop found");
			}

		} catch (RealTimeDataException e) {
			throw new FeedsNotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new FeedsNotFoundException("Unknown Exception: "+e);
		}
		//		System.out.println(resp);
		return resp;
	}

	private LegLiveFeed testRailRealTime(String formGrfsStopId,String  routeLine,String  tripId,long scheduledTime) throws RealTimeDataException, StopNotFoundException, ExecutionException {
		LegLiveFeed resp = new LegLiveFeed();
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.setTimeZone(TimeZone.getTimeZone(ESTERN_TIMEZONE));
		if(_verbose){
			System.out.println("Current Time  : "+dateFormat.format(new Date()));
			System.out.println("Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		}
		List<String> lstApiFromStop = WmataUtil.getWmataStopFromGtfsStopAndLine(formGrfsStopId,routeLine,stopMapping);
		String apiFromStop = lstApiFromStop.get(0);
		String gtfsTripDestStop = stopMapping.getTripLastStopAndHead().get(tripId).getLeft();
		String tripDestStop = WmataUtil.getWmataStopFromGtfsStopAndLine(gtfsTripDestStop,routeLine,stopMapping).get(0);
		if(_verbose)
			System.out.println("Current/Destination: "+apiFromStop+"-"+tripDestStop);
		List<RailPrediction> estimates = cachedApiClient.getRailPrediction(apiFromStop);
		validateList(estimates, "No Valid Estimation found form API");
		for (Iterator iterator = estimates.iterator(); iterator.hasNext();) {
			RailPrediction railPrediction = (RailPrediction) iterator.next();
			if(!StringUtils.equalsIgnoreCase(railPrediction.getDestinationCode(),tripDestStop)){
				iterator.remove();
			}
		}
		validateList(estimates, "No Valid Estimation found after filtering");

		if(_verbose){
			System.out.println("======  ======  ====== filtered =====  ==========  ==========");
			for (RailPrediction rp : estimates) {
				System.out.println("Destination: "+rp.getDestinationName()+"("+rp.getDestination()+")" +
						", Line: "+rp.getLine()+", Min: "+rp.getMin());
			}
			System.out.println("============  =============== ================ =========");
		}

		MutablePair<Integer, Long> closestMatch = WmataUtil.getClosestEstimationForRail(estimates, scheduledTime, 0,5,30);

		if (closestMatch == null)
			throw new RealTimeDataException("Valid minutes to departure not found in response for, Stop Tag: "+formGrfsStopId+", Route Tag: "+routeLine);
		System.out.println("ClosestMatch: "+(closestMatch.getLeft()/DateUtils.MILLIS_PER_MINUTE)+","+dateFormat.format(closestMatch.getRight())+"\n");

		int arrivalFlag = ETA_FLAG.ON_TIME.ordinal();
		long estimatedDepartureTime = closestMatch.getRight();
		int diff = closestMatch.getLeft();
		if (diff>(maxValidForDelay *DateUtils.MILLIS_PER_MINUTE)){
			arrivalFlag = TpConstants.ETA_FLAG.DELAYED.ordinal();
		}else if (diff<0 && Math.abs(diff) >(maxValidForDelay*DateUtils.MILLIS_PER_MINUTE)){
			arrivalFlag = TpConstants.ETA_FLAG.EARLY.ordinal();
		}
		if(_verbose){
			System.out.println("Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
			System.out.println("Estimated time: "+dateFormat.format(new Date(estimatedDepartureTime))+"  "+ETA_FLAG.values()[arrivalFlag].name());
			System.out.println("Min : "+((estimatedDepartureTime-scheduledTime)/1000)/60);
		}
		resp = new LegLiveFeed();
		resp.setTimeDiffInMins(diff);
		resp.setDepartureTime(estimatedDepartureTime);
		resp.setArrivalTimeFlag(arrivalFlag);
		return resp;
	}


	/**
	 * Gets the bus real time data.
	 *
	 * @param gtfsFromStop the gtfs from stop
	 * @param gtfsTripId the gtfs trip id
	 * @param routeTag the route tag
	 * @param scheduledTime the scheduled time
	 * @return
	 * @return the bus real time data
	 * @throws RealTimeDataException the real time data exception
	 * @throws StopNotFoundException the stop not found exception
	 */
	private LegLiveFeed getBusRealTimeData(String gtfsFromStop,String gtfsTripId,String routeTag,long scheduledTime) throws RealTimeDataException, StopNotFoundException {
		LegLiveFeed resp = new LegLiveFeed();
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.setTimeZone(TimeZone.getTimeZone(ESTERN_TIMEZONE));

		if(_verbose){
			System.out.println("Current Time  : "+dateFormat.format(new Date()));
			System.out.println("Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		}
		MutablePair<String, String> stop_head = stopMapping.getTripLastStopAndHead().get(gtfsTripId);
		validateNull(stop_head,"No trip found id found for getting direction id for trip:"+gtfsTripId);

		String directionId = busDirectionOverride.get(stop_head.getRight());
		validateNull(directionId,"No Direction ID Found : "+stop_head.getRight());
		if(_verbose	)
			System.out.println("directionId: "+stop_head.getRight()+"->"+directionId);
		List<BusStop> lstBusStops =  WmataUtil.getBusStopIdFromGtfsStop(gtfsFromStop,routeTag,stopMapping);
		if(_verbose	)
			System.out.println("Total Stops: "+lstBusStops.size());
		List<Predictions> lstPredictions = getBusPredictions(lstBusStops);

		System.out.println("Total Prediction To filter: "+lstPredictions.size());
		for (Iterator<Predictions> iterator = lstPredictions.iterator(); iterator.hasNext();) {
			Predictions predictions = iterator.next();
			if(!StringUtils.equalsIgnoreCase(predictions.getRouteID(),routeTag) || !StringUtils.equalsIgnoreCase(predictions.getDirectionNum(),directionId))
				iterator.remove();
		}
		validateList(lstPredictions,"No Matching Prediction Found For GtfsStop:"+gtfsFromStop);
		if(_verbose ){
			System.out.println("Filtered---->");
			for (Predictions p : lstPredictions) {
				System.out.println("       Route: "+p.getRouteID()+", Direction:"+p.getDirectionText()+"("+p.getDirectionNum()+"), VehicleID: "+p.getVehicleID()+", Min: "+p.getMinutes());
			}
		}

		MutablePair<Integer, Long> closestMatch = WmataUtil.getClosestEstimationForBus(lstPredictions, scheduledTime, 0 ,maxEarlyThresold,maxDelayThresold);
		validateNull(closestMatch,"No Closest Match Found For GtfsStop:"+gtfsFromStop);

		System.out.println("ClosestMatch: "+(closestMatch.getLeft()/DateUtils.MILLIS_PER_MINUTE)+","+dateFormat.format(closestMatch.getRight())+"\n");

		int arrivalFlag = ETA_FLAG.ON_TIME.ordinal();
		long estimatedDepartureTime = closestMatch.getRight();
		int diff = closestMatch.getLeft();
		if (diff>(maxValidForDelay *DateUtils.MILLIS_PER_MINUTE)){
			arrivalFlag = TpConstants.ETA_FLAG.DELAYED.ordinal();
		}else if (diff<0 && Math.abs(diff) >(maxValidForEarly *DateUtils.MILLIS_PER_MINUTE)){
			arrivalFlag = TpConstants.ETA_FLAG.EARLY.ordinal();
		}
		if(_verbose){
			System.out.println("Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
			System.out.println("Estimated time: "+dateFormat.format(new Date(estimatedDepartureTime))+"  "+ETA_FLAG.values()[arrivalFlag].name());
			System.out.println("Min : "+((estimatedDepartureTime-scheduledTime)/1000)/60);
		}
		resp = new LegLiveFeed();
		resp.setTimeDiffInMins(diff);
		resp.setDepartureTime(estimatedDepartureTime);
		resp.setArrivalTimeFlag(arrivalFlag);
		return resp;
	}



	/**
	 * Gets the bus predictions.
	 *
	 * @param lstBusStops the lst bus stops
	 * @return the bus predictions
	 */
	private List<Predictions> getBusPredictions(List<BusStop> lstBusStops) {
		List<Predictions> lstPredictions =  new ArrayList<Predictions>();
		for (BusStop busStop : lstBusStops) {
			if(_verbose	)
				System.out.println("Getting Real time prediction for stop:"+busStop.getStopId()+" - "+busStop.getName());
			try {
				List<Predictions> lst = cachedApiClient.getBusPredictionAtStop(busStop.getStopId());
				lstPredictions.addAll(lst);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		return lstPredictions;
	}

	public List<LegLiveFeed> getLiveFeeds(List<Leg> leg) {
		return null;
	}
	public int getMaxEarlyThresold() {
		return maxEarlyThresold;
	}
	public void setMaxEarlyThresold(int maxEarlyThresold) {
		this.maxEarlyThresold = maxEarlyThresold;
	}
	public int getMaxDelayThresold() {
		return maxDelayThresold;
	}
	public void setMaxDelayThresold(int maxDelayThresold) {
		this.maxDelayThresold = maxDelayThresold;
	}
	public int getMaxValidForEarly() {
		return maxValidForEarly;
	}
	public void setMaxValidForEarly(int maxValidForEarly) {
		this.maxValidForEarly = maxValidForEarly;
	}
	public int getMaxValidForDelay() {
		return maxValidForDelay;
	}
	public void setMaxValidForDelay(int maxValidForDelay) {
		this.maxValidForDelay = maxValidForDelay;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getStopMapFilePath() {
		return stopMapFilePath;
	}
	public void setStopMapFilePath(String stopMapFilePath) {
		this.stopMapFilePath = stopMapFilePath;
	}
	public StopMapping getStopMapping() {
		return stopMapping;
	}
	public void setStopMapping(StopMapping stopMapping) {
		this.stopMapping = stopMapping;
	}
	public Map<String, String> getBusDirectionOverride() {
		return busDirectionOverride;
	}
	public void setBusDirectionOverride(Map<String, String> busDirectionOverride) {
		this.busDirectionOverride = busDirectionOverride;
	}
	public WmataApiClient getApiClient() {
		return apiClient;
	}
	public void setApiClient(WmataApiClient apiClient) {
		this.apiClient = apiClient;
	}
	public static String getEsternTimezone() {
		return ESTERN_TIMEZONE;
	}
	public boolean is_verbose() {
		return _verbose;
	}
	public void set_verbose(boolean _verbose) {
		this._verbose = _verbose;
	}
	private void validateNull(Object  obj ,String msg) throws RealTimeDataException {
		if(obj==null)
			throw new RealTimeDataException(msg);
	}
	public LoggingService getLogger() {
		return logger;
	}
	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}
	public String getLoggerName() {
		return loggerName;
	}
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	public WmataCachedApiClient getCachedApiClient() {
		return cachedApiClient;
	}

	public void setCachedApiClient(WmataCachedApiClient cachedApiClient) {
		this.cachedApiClient = cachedApiClient;
	}
	private void validateList(List lst, String msg) throws RealTimeDataException {
		if(lst!=null && lst.size()==0)
			throw new RealTimeDataException(msg);
	}

}