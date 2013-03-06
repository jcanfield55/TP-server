/*
 * 
 */
package com.nimbler.tp.service.livefeeds;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.common.StopNotFoundException;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
import com.nimbler.tp.dataobject.RealTimePrediction;
import com.nimbler.tp.dataobject.TraverseMode;
import com.nimbler.tp.dataobject.wmata.BusStop;
import com.nimbler.tp.dataobject.wmata.RailPrediction;
import com.nimbler.tp.dataobject.wmata.StopMapping;
import com.nimbler.tp.dataobject.wmata.WmataBusPrediction;
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
	private int maxEligibleEarly = 1;
	/**
	 * 
	 */
	private int maxEligibleForDelay = 2;

	private int useCountForBusStopToBeFinal = 5;

	private String apiKey= "wateq3gxqzb9s597qky6khd7";

	private String stopMapFilePath= "C:/wmata_data/";
	private StopMapping stopMapping = null;

	private Map<String, String> busDirectionOverride;
	@Autowired
	private WmataApiClient apiClient;

	@Autowired
	private WmataCachedApiClient cachedApiClient;
	private boolean _verbose = true;
	private long maxRealTimeSupportLimitMin = NumberUtils.toLong(TpConstants.MAX_REALTIME_LIMIT_MIN,90);

	@PostConstruct
	private void init() {
		try {
			if(stopMapFilePath==null)
				System.out.println("No gtfs stop mapping file found...");
			ObjectInputStream outputStream = new ObjectInputStream(new FileInputStream(stopMapFilePath+"/StopMap.obj"));
			System.out.println("Reading WMATA Stop Mapping....");
			stopMapping =  (StopMapping) outputStream.readObject();
			System.out.println("WMATA Stop Read Complete.... ");
			if(busDirectionOverride == null){
				busDirectionOverride = new HashMap<String, String>();
				busDirectionOverride.put("0", "1");
				busDirectionOverride.put("1", "0");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.nimbler.tp.service.livefeeds.RealTimeAPI#getLiveFeeds(com.nimbler.tp.dataobject.Leg)
	 */
	@Override
	public LegLiveFeed getLiveFeeds(Leg leg) throws FeedsNotFoundException {
		LegLiveFeed resp = null;
		try {
			if(_verbose)
				logger.debug(loggerName, "-------------------------------------------------------------------------------");
			Long scheduledTime = leg.getStartTime();

			validateTimeLimit(scheduledTime);
			String gtfsFromStop = leg.getFrom().getStopId().getId();
			String routeTag = leg.getRoute().trim();
			String gtfsTtipId = leg.getTripId().trim();
			String mode = leg.getMode();
			if(TraverseMode.BUS.isSame(mode)){
				LegLiveFeed liveFeed = getBusRealTimeData(gtfsFromStop, gtfsTtipId, routeTag, scheduledTime);
				liveFeed.setLeg(leg);
				return  liveFeed;

			}else if(TraverseMode.SUBWAY.isSame(mode)){
				LegLiveFeed liveFeed =  getRailRealTimeData(gtfsFromStop, routeTag, gtfsTtipId, scheduledTime);
				liveFeed.setLeg(leg);
				return  liveFeed;
			}
		} catch (StopNotFoundException e) {
			throw new FeedsNotFoundException(e.getMessage());
		} catch (RealTimeDataException e) {
			throw new FeedsNotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new FeedsNotFoundException("Unknown Exception: "+e);
		}finally{
			if(_verbose)
				logger.debug(loggerName, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		}
		return resp;
	}

	/* (non-Javadoc)
	 * @see com.nimbler.tp.service.livefeeds.RealTimeAPI#getAllRealTimeFeeds(com.nimbler.tp.dataobject.Leg)
	 */
	@Override
	public LegLiveFeed getAllRealTimeFeeds(Leg leg)	throws FeedsNotFoundException {
		LegLiveFeed resp = null;
		try {
			if(_verbose)
				logger.debug(loggerName, "-------------------------------------------------------------------------------");
			Long scheduledTime = leg.getStartTime();
			if (scheduledTime < System.currentTimeMillis())
				throw new RealTimeDataException("Leg scheduled time is already passed. No estimates possible.");
			validateTimeLimit(scheduledTime);
			String gtfsFromStop = leg.getFrom().getStopId().getId();
			String routeTag = leg.getRoute().trim();
			String gtfsTtipId = leg.getTripId().trim();
			String mode = leg.getMode();
			if(TraverseMode.BUS.isSame(mode)){
				LegLiveFeed liveFeed = getAllBusRealTimeData(gtfsFromStop, gtfsTtipId, routeTag, scheduledTime);
				liveFeed.setEmptyLeg(leg);
				return  liveFeed;
			}else if(TraverseMode.SUBWAY.isSame(mode)){
				LegLiveFeed liveFeed =  getAllRailRealTimeData(gtfsFromStop, routeTag, gtfsTtipId, scheduledTime);
				liveFeed.setEmptyLeg(leg);
				return  liveFeed;
			}
		} catch (StopNotFoundException e) {
			throw new FeedsNotFoundException(e.getMessage());
		} catch (RealTimeDataException e) {
			throw new FeedsNotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new FeedsNotFoundException("Unknown Exception: "+e);
		}finally{
			if(_verbose)
				logger.debug(loggerName, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		}
		return resp;
	}

	/**
	 * Gets the all bus real time data.
	 *
	 * @param gtfsFromStop the gtfs from stop
	 * @param gtfsTripId the gtfs trip id
	 * @param routeTag the route tag
	 * @param scheduledTime the scheduled time
	 * @return the all bus real time data
	 * @throws RealTimeDataException the real time data exception
	 * @throws StopNotFoundException the stop not found exception
	 * @throws ExecutionException the execution exception
	 */
	private LegLiveFeed getAllBusRealTimeData(String gtfsFromStop,String gtfsTripId, String routeTag, Long scheduledTime) throws RealTimeDataException, StopNotFoundException, ExecutionException {
		List<RealTimePrediction> lstRealTimePredictions = new ArrayList<RealTimePrediction>();
		logger.debug(loggerName,"gtfsFromStop: "+ gtfsFromStop + ", gtfsTripId: " + gtfsTripId
				+ ", routeTag: " + routeTag);
		LegLiveFeed resp = new LegLiveFeed();
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.setTimeZone(TimeZone.getTimeZone(ESTERN_TIMEZONE));
		if(_verbose){
			logger.debug(loggerName,"Current Time  : "+dateFormat.format(new Date()));
			logger.debug(loggerName,"Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		}
		MutablePair<String, String> stop_head = stopMapping.getTripLastStopAndHead().get(gtfsTripId);
		validateNull(stop_head,"No trip  found while getting direction id for trip:"+gtfsTripId);

		String directionId = busDirectionOverride.get(stop_head.getRight());
		validateNull(directionId,"No Direction ID Found : "+stop_head.getRight());
		if(_verbose	)
			logger.debug(loggerName,"directionId: "+stop_head.getRight()+"->"+directionId);

		List<WmataBusPrediction> lstPredictions = getBusPredictions(gtfsFromStop,routeTag);
		if(_verbose	)
			logger.debug(loggerName,"Total Prediction To filter: "+lstPredictions.size());
		for (Iterator<WmataBusPrediction> iterator = lstPredictions.iterator(); iterator.hasNext();) {
			WmataBusPrediction p = iterator.next();
			if(!equalsIgnoreCase(p.getRouteID(),routeTag) || !equalsIgnoreCase(p.getDirectionNum(),directionId))
				iterator.remove();
		}

		validateList(lstPredictions,"No Matching Prediction Found For GtfsStop:"+gtfsFromStop);
		if(_verbose )
			logger.debug(loggerName,"Filtered---->");
		Set<BusStop> stopsToMark = new HashSet<BusStop>();
		for (WmataBusPrediction p : lstPredictions) {
			if(_verbose )
				logger.debug(loggerName,"       Route: "+p.getRouteID()+", Direction:"+p.getDirectionText()+"("+p.getDirectionNum()+"), VehicleID: "+p.getVehicleID()+", Min: "+p.getMinutes());
			if(p.getBusStop()!=null){
				stopsToMark.add(p.getBusStop());
			}
			lstRealTimePredictions.add(new RealTimePrediction(p));
		}
		Map<String, String> busStopFinalMap = stopMapping.getBusStopFinalMap();
		for (BusStop busStop : stopsToMark) {
			busStop.markUsed();
			if(!busStopFinalMap.containsKey(gtfsFromStop) && busStop.getUsedCount()>useCountForBusStopToBeFinal ){
				if(_verbose)
					System.out.println("stop marked gtfsFromStop:"+gtfsFromStop+", "+busStop);
				stopMapping.addFinalStopMappingForBus(gtfsFromStop, busStop.getStopId());
			}
		}

		resp = new LegLiveFeed();
		resp.setLstPredictions(lstRealTimePredictions);
		return resp;
	}



	/**
	 * Gets the rail real time data.
	 *
	 * @param formGrfsStopId the form grfs stop id
	 * @param routeLine the route line
	 * @param tripId the trip id
	 * @param scheduledTime the scheduled time
	 * @return the rail real time data
	 * @throws RealTimeDataException the real time data exception
	 * @throws StopNotFoundException the stop not found exception
	 * @throws ExecutionException the execution exception
	 */
	private LegLiveFeed getRailRealTimeData(String formGrfsStopId,String  routeLine,String  tripId,long scheduledTime) throws RealTimeDataException, StopNotFoundException, ExecutionException {
		logger.debug(loggerName,"formGrfsStopId: "+ formGrfsStopId+ ", routeLine: "
				+ routeLine	+ ", tripId: "+ tripId);
		LegLiveFeed resp = new LegLiveFeed();
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.setTimeZone(TimeZone.getTimeZone(ESTERN_TIMEZONE));
		if(_verbose){
			logger.debug(loggerName,"Current Time  : "+dateFormat.format(new Date()));
			logger.debug(loggerName,"Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		}
		List<String> lstApiFromStop = WmataUtil.getWmataStopFromGtfsStopAndLine(formGrfsStopId,routeLine,stopMapping);
		String apiFromStop = lstApiFromStop.get(0);
		String gtfsTripDestStop = stopMapping.getTripLastStopAndHead().get(tripId).getLeft();
		String tripDestStop = WmataUtil.getWmataStopFromGtfsStopAndLine(gtfsTripDestStop,routeLine,stopMapping).get(0);
		if(_verbose)
			logger.debug(loggerName,"Current/Destination: "+apiFromStop+"-"+tripDestStop);

		List<RailPrediction> estimates = cachedApiClient.getRailPrediction(apiFromStop);
		validateList(estimates, "No Valid Estimation found form API");
		estimates = new ArrayList<RailPrediction>(estimates);
		for (Iterator iterator = estimates.iterator(); iterator.hasNext();) {
			RailPrediction railPrediction = (RailPrediction) iterator.next();
			if(!StringUtils.equalsIgnoreCase(railPrediction.getDestinationCode(),tripDestStop)){
				iterator.remove();
			}
		}
		validateList(estimates, "No Valid Estimation found after filtering");

		if(_verbose){
			logger.debug(loggerName,"Filtered---->");
			for (RailPrediction rp : estimates) {
				logger.debug(loggerName,"        Destination: "+rp.getDestinationName()+"("+rp.getDestination()+")" +
						", Line: "+rp.getLine()+", Min: "+rp.getMin());
			}
		}

		MutablePair<Integer, Long> closestMatch = WmataUtil.getClosestEstimationForRail(estimates, scheduledTime, 0,maxEarlyThresold,maxDelayThresold);
		if (closestMatch == null)
			throw new RealTimeDataException("Valid minutes to departure not found in response for, Stop Tag: "+formGrfsStopId+", Route Tag: "+routeLine);

		int arrivalFlag = ETA_FLAG.ON_TIME.ordinal();
		long estimatedDepartureTime = closestMatch.getRight();
		int diff = closestMatch.getLeft();
		if (diff>(maxEligibleForDelay *DateUtils.MILLIS_PER_MINUTE)){
			arrivalFlag = TpConstants.ETA_FLAG.DELAYED.ordinal();
		}else if (diff<0 && Math.abs(diff) >(maxEligibleEarly*DateUtils.MILLIS_PER_MINUTE)){
			arrivalFlag = TpConstants.ETA_FLAG.EARLY.ordinal();
		}
		logger.debug(loggerName,"ClosestMatch: "+(closestMatch.getLeft()/DateUtils.MILLIS_PER_MINUTE)+", "+dateFormat.format(closestMatch.getRight())+"  "+ETA_FLAG.values()[arrivalFlag].name());

		resp = new LegLiveFeed();
		resp.setTimeDiffInMills(diff);
		resp.setDepartureTime(estimatedDepartureTime);
		resp.setArrivalTimeFlag(arrivalFlag);
		return resp;
	}

	/**
	 * Gets the all rail real time data.
	 *
	 * @param formGrfsStopId the form grfs stop id
	 * @param routeLine the route line
	 * @param tripId the trip id
	 * @param scheduledTime the scheduled time
	 * @return the all rail real time data
	 * @throws RealTimeDataException the real time data exception
	 * @throws StopNotFoundException the stop not found exception
	 * @throws ExecutionException the execution exception
	 */
	private LegLiveFeed getAllRailRealTimeData(String formGrfsStopId,String  routeLine,String  tripId,long scheduledTime) throws RealTimeDataException, StopNotFoundException, ExecutionException {
		List<RealTimePrediction> lstRealTimePredictions = new ArrayList<RealTimePrediction>();
		logger.debug(loggerName,"formGrfsStopId: "+ formGrfsStopId+ ", routeLine: "
				+ routeLine	+ ", tripId: "+ tripId+ ", scheduledTime: "+ scheduledTime);
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.setTimeZone(TimeZone.getTimeZone(ESTERN_TIMEZONE));
		if(_verbose){
			logger.debug(loggerName,"Current Time  : "+dateFormat.format(new Date()));
			logger.debug(loggerName,"Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		}
		List<String> lstApiFromStop = WmataUtil.getWmataStopFromGtfsStopAndLine(formGrfsStopId,routeLine,stopMapping);
		String apiFromStop = lstApiFromStop.get(0);
		String gtfsTripDestStop = stopMapping.getTripLastStopAndHead().get(tripId).getLeft();
		String tripDestStop = WmataUtil.getWmataStopFromGtfsStopAndLine(gtfsTripDestStop,routeLine,stopMapping).get(0);
		if(_verbose)
			logger.debug(loggerName,"Current/Destination: "+apiFromStop+"-"+tripDestStop);
		List<RailPrediction> apiEstimates = cachedApiClient.getRailPrediction(apiFromStop);
		validateList(apiEstimates, "No Valid Estimation found form API");
		List<RailPrediction> estimates = new ArrayList<RailPrediction>();
		for (Iterator iterator = estimates.iterator(); iterator.hasNext();) {
			RailPrediction railPrediction = (RailPrediction) iterator.next();
			if(StringUtils.equalsIgnoreCase(railPrediction.getDestinationCode(),tripDestStop)){
				estimates.add(railPrediction);
			}
		}
		validateList(estimates, "No Valid Estimation found after filtering");
		if(_verbose)
			logger.debug(loggerName,"Filtered---->");
		for (RailPrediction rp : estimates) {
			if(_verbose)
				logger.debug(loggerName,"     Destination: "+rp.getDestinationName()+"("+rp.getDestination()+")" +
						", Line: "+rp.getLine()+", Min: "+rp.getMin());
			lstRealTimePredictions.add(new RealTimePrediction(rp));
		}


		LegLiveFeed resp = new LegLiveFeed();
		resp.setLstPredictions(lstRealTimePredictions);
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
	 * @throws ExecutionException
	 */
	private LegLiveFeed getBusRealTimeData(String gtfsFromStop,String gtfsTripId,String routeTag,long scheduledTime) throws RealTimeDataException, StopNotFoundException, ExecutionException {
		logger.debug(loggerName,"gtfsFromStop: "+ gtfsFromStop + ", gtfsTripId: " + gtfsTripId
				+ ", routeTag: " + routeTag);
		LegLiveFeed resp = new LegLiveFeed();
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.setTimeZone(TimeZone.getTimeZone(ESTERN_TIMEZONE));

		if(_verbose){
			logger.debug(loggerName,"Current Time  : "+dateFormat.format(new Date()));
			logger.debug(loggerName,"Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		}
		MutablePair<String, String> stop_head = stopMapping.getTripLastStopAndHead().get(gtfsTripId);
		validateNull(stop_head,"No trip  found while getting direction id for trip:"+gtfsTripId);

		String directionId = busDirectionOverride.get(stop_head.getRight());
		validateNull(directionId,"No Direction ID Found : "+stop_head.getRight());
		if(_verbose	)
			logger.debug(loggerName,"directionId: "+stop_head.getRight()+"->"+directionId);

		List<WmataBusPrediction> lstPredictions = getBusPredictions(gtfsFromStop,routeTag);
		if(_verbose	)
			logger.debug(loggerName,"Total Prediction To filter: "+lstPredictions.size());
		for (Iterator<WmataBusPrediction> iterator = lstPredictions.iterator(); iterator.hasNext();) {
			WmataBusPrediction p = iterator.next();
			if(!StringUtils.equalsIgnoreCase(p.getRouteID(),routeTag) || !StringUtils.equalsIgnoreCase(p.getDirectionNum(),directionId))
				iterator.remove();
		}

		validateList(lstPredictions,"No Matching Prediction Found For GtfsStop:"+gtfsFromStop);
		if(_verbose )
			logger.debug(loggerName,"Filtered---->");
		Set<BusStop> stopsToMark = new HashSet<BusStop>();
		for (WmataBusPrediction p : lstPredictions) {
			if(_verbose )
				logger.debug(loggerName,"       Route: "+p.getRouteID()+", Direction:"+p.getDirectionText()+"("+p.getDirectionNum()+"), VehicleID: "+p.getVehicleID()+", Min: "+p.getMinutes());
			if(p.getBusStop()!=null){
				stopsToMark.add(p.getBusStop());
			}
		}
		Map<String, String> busStopFinalMap = stopMapping.getBusStopFinalMap();
		for (BusStop busStop : stopsToMark) {
			busStop.markUsed();
			if(!busStopFinalMap.containsKey(gtfsFromStop) && busStop.getUsedCount()>useCountForBusStopToBeFinal ){
				if(_verbose)
					logger.info(loggerName,"stop marked gtfsFromStop:"+gtfsFromStop+", "+busStop);
				stopMapping.addFinalStopMappingForBus(gtfsFromStop, busStop.getStopId());
			}
		}
		MutablePair<Integer, Long> closestMatch = WmataUtil.getClosestEstimationForBus(lstPredictions, scheduledTime, 0 ,maxEarlyThresold,maxDelayThresold);
		validateNull(closestMatch,"No Closest Match Found For GtfsStop:"+gtfsFromStop);

		int arrivalFlag = ETA_FLAG.ON_TIME.ordinal();
		long estimatedDepartureTime = closestMatch.getRight();
		int diff = closestMatch.getLeft();
		if (diff>(maxEligibleForDelay *DateUtils.MILLIS_PER_MINUTE)){
			arrivalFlag = TpConstants.ETA_FLAG.DELAYED.ordinal();
		}else if (diff<0 && Math.abs(diff) >(maxEligibleEarly *DateUtils.MILLIS_PER_MINUTE)){
			arrivalFlag = TpConstants.ETA_FLAG.EARLY.ordinal();
		}
		if(_verbose	)
			logger.debug(loggerName,"ClosestMatch: "+(closestMatch.getLeft()/DateUtils.MILLIS_PER_MINUTE)+", "+dateFormat.format(closestMatch.getRight())+"  "+ETA_FLAG.values()[arrivalFlag].name());
		resp = new LegLiveFeed();
		resp.setTimeDiffInMills(diff);
		resp.setDepartureTime(estimatedDepartureTime);
		resp.setArrivalTimeFlag(arrivalFlag);
		return resp;
	}



	/**
	 * Gets the bus predictions.
	 * @param routeTag
	 * @param gtfsFromStop
	 * @param gtfsFromStop
	 *
	 * @param lstBusStops the lst bus stops
	 * @return the bus predictions
	 * @throws StopNotFoundException
	 * @throws ExecutionException
	 * @throws RealTimeDataException
	 */
	private  List<WmataBusPrediction> getBusPredictions(String gtfsFromStop, String routeTag) throws StopNotFoundException, RealTimeDataException, ExecutionException {
		List<WmataBusPrediction> lstPredictions =  new ArrayList<WmataBusPrediction>();
		String apiStop = stopMapping.getBusStopFinalMap().get(gtfsFromStop);
		if(!ComUtils.isEmptyString(apiStop)){
			logger.debug(loggerName,"Got stop from final mapping for gtfs stop: "+gtfsFromStop);
			List<WmataBusPrediction> lst = cachedApiClient.getBusPredictionAtStop(apiStop);
			lstPredictions.addAll(lst);
		}else{
			List<BusStop> lstBusStops =  WmataUtil.getBusStopIdFromGtfsStop(gtfsFromStop,routeTag,stopMapping);
			if(_verbose	)
				logger.debug(loggerName,"Total Stops: "+lstBusStops.size());

			for (BusStop busStop : lstBusStops) {
				logger.debug(loggerName,"Getting Real time prediction for stop:"+busStop.getStopId()+" - "+busStop.getName());
				try {
					List<WmataBusPrediction> lst= cachedApiClient.getBusPredictionAtStop(apiStop);
					if(!ComUtils.isEmptyList(lst)){
						lstPredictions.addAll(lst);
						for (WmataBusPrediction predictions : lst) {
							predictions.setBusStop(busStop);
						}
					}
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
		return lstPredictions;
	}

	public int getMaxEarlyThresold() {
		return maxEarlyThresold;
	}

	public int getMaxDelayThresold() {
		return maxDelayThresold;
	}

	public void setMaxDelayThresold(int maxDelayThresold) {
		this.maxDelayThresold = maxDelayThresold;
	}

	public int getMaxEligibleEarly() {
		return maxEligibleEarly;
	}

	public void setMaxEligibleEarly(int maxEligibleEarly) {
		this.maxEligibleEarly = maxEligibleEarly;
	}

	public int getMaxEligibleForDelay() {
		return maxEligibleForDelay;
	}

	public void setMaxEligibleForDelay(int maxEligibleForDelay) {
		this.maxEligibleForDelay = maxEligibleForDelay;
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

	public boolean is_verbose() {
		return _verbose;
	}

	public void setMaxEarlyThresold(int maxEarlyThresold) {
		this.maxEarlyThresold = maxEarlyThresold;
	}

	public void set_verbose(boolean _verbose) {
		this._verbose = _verbose;
	}
	private void validateNull(Object  obj ,String msg) throws RealTimeDataException {
		if(obj==null)
			throw new RealTimeDataException(msg);
	}
	private void validateTimeLimit(Long scheduledTime) throws RealTimeDataException {
		long curruntTime =System.currentTimeMillis();
		if (scheduledTime < curruntTime)
			throw new RealTimeDataException("Leg scheduled time is already passed. No estimates possible.");
		long maxTime = curruntTime +(maxRealTimeSupportLimitMin*DateUtils.MILLIS_PER_MINUTE);
		if(scheduledTime>maxTime)
			throw new RealTimeDataException("ScheduleTime out Of range to query realtime for time:"+new Date(scheduledTime)+",currunt time:"+new Date(curruntTime));
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
		if(ComUtils.isEmptyList(lst))
			throw new RealTimeDataException(msg);
	}

	public int getUseCountForBusStopToBeFinal() {
		return useCountForBusStopToBeFinal;
	}

	public void setUseCountForBusStopToBeFinal(int useCountForBusStopToBeFinal) {
		this.useCountForBusStopToBeFinal = useCountForBusStopToBeFinal;
	}
	public void saveStopMapping() {
		logger.debug(loggerName, "Saving stop Mapping.....");
		try {
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(stopMapFilePath+"/StopMap.obj"));
			stream.writeObject(stopMapping);
			logger.debug(loggerName, "Mapping Saved...");
		} catch (Exception e) {
			logger.error(loggerName, e);
		}

	}

}