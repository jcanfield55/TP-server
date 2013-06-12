/*
 * 
 */
package com.nimbler.tp.service.livefeeds;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
import com.nimbler.tp.dataobject.nextbus.VehiclePosition;
import com.nimbler.tp.dataobject.wmata.BusStop;
import com.nimbler.tp.dataobject.wmata.GtfsStop;
import com.nimbler.tp.dataobject.wmata.RailPrediction;
import com.nimbler.tp.dataobject.wmata.RailStation;
import com.nimbler.tp.dataobject.wmata.RailStopSequence;
import com.nimbler.tp.dataobject.wmata.StopMapping;
import com.nimbler.tp.dataobject.wmata.WmataBusPrediction;
import com.nimbler.tp.dataobject.wmata.WmataRealTimePrediction;
import com.nimbler.tp.gtfs.GtfsDataService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.livefeeds.cache.WmataCachedApiClient;
import com.nimbler.tp.service.livefeeds.stub.WmataApiClient;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;
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

	//	private int useCountForBusStopToBeFinal = 5;

	private String apiKey= "wateq3gxqzb9s597qky6khd7";

	private String stopMapFilePath= "E:/nimbler/wmata-mapping";
	private StopMapping stopMapping = null;

	@Autowired
	private WmataApiClient apiClient;

	@Autowired
	private WmataCachedApiClient cachedApiClient;
	private boolean _verbose = false;

	@Autowired
	private GtfsDataService gtfsDataService;
	private long maxRealTimeSupportLimitMin = NumberUtils.toLong(TpConstants.MAX_REALTIME_LIMIT_MIN,90);

	@PostConstruct
	@SuppressWarnings("unused")
	private void init() {
		try {
			String file = stopMapFilePath+"/StopMap.obj";
			System.out.println("Reading WMATA Stop Mapping....");
			Object[] dataObj = WmataUtil.read(file, 1);
			stopMapping =  (StopMapping) dataObj[0];
			System.out.println("    Version:"+stopMapping.getVersion());
			if(stopMapping.getDate()!=null)
				System.out.println("    Date   :"+new Date(stopMapping.getDate()));
			System.out.println("    Note   :"+stopMapping.getNote());

			//			Map<String, RailStopSequence> s = stopMapping.getRailStopSequence();
			//			for (Map.Entry<String, RailStopSequence> entry : s.entrySet()) {
			//				String key = entry.getKey();
			//				RailStopSequence value = entry.getValue();
			//				System.out.println("-------------------------------");
			//				System.out.println(key+" - "+ToStringBuilder.reflectionToString(value.getStopCodeSequence(),ToStringStyle.SHORT_PREFIX_STYLE));
			//				for (RailStopSeqElement element : value.getPath()) {
			//					System.out.println("      "+element.getLineCode()+" - "+element.getSeqNum()+"-"+element.getStationCode()+"-"+element.getStationName());
			//				}
			//			}
			System.out.println("WMATA Stop Read Complete....");

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
			String tripHeadSign = leg.getHeadsign();
			if(TraverseMode.BUS.is(mode)){
				LegLiveFeed liveFeed = getBusRealTimeData(gtfsFromStop, gtfsTtipId, routeTag, scheduledTime,tripHeadSign);
				liveFeed.setLeg(leg);
				return  liveFeed;

			}else if(TraverseMode.SUBWAY.is(mode)){
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
		try {
			if(_verbose)
				logger.debug(loggerName, "-------------------------------------------------------------------------------"+leg.getMode());
			String mode = leg.getMode();
			LegLiveFeed liveFeed = null;
			if(TraverseMode.BUS.is(mode))
				liveFeed = getAllBusRealTimeData(leg);
			else if(TraverseMode.SUBWAY.is(mode))
				liveFeed =  getAllRailRealTimeData(leg);
			else 
				throw new RealTimeDataException("Unsupported Mode for Wmata: "+mode);
			liveFeed.setEmptyLeg(leg);
			if(_verbose){
				if(liveFeed.getLstPredictions()!=null){
					logger.debug(loggerName, "RealTime count: "+liveFeed.getLstPredictions().size());
				}
			}
			return liveFeed;
		} catch (StopNotFoundException e) {
			throw new FeedsNotFoundException(e.getMessage());
		} catch (RealTimeDataException e) {
			throw new FeedsNotFoundException(e.getMessage());
		} catch (ExecutionException e) {
			throw new FeedsNotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new FeedsNotFoundException("Unknown Exception: "+e);
		}finally{
			if(_verbose)
				logger.debug(loggerName, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		}
	}

	/**
	 * Gets the all bus real time data.
	 *
	 * @param gtfsFromStop the gtfs from stop
	 * @param gtfsTripId the gtfs trip id
	 * @param routeTag the route tag
	 * @param scheduledTime the scheduled time
	 * @param tripHeadSign
	 * @return the all bus real time data
	 * @throws RealTimeDataException the real time data exception
	 * @throws StopNotFoundException the stop not found exception
	 * @throws ExecutionException the execution exception
	 */
	private LegLiveFeed getAllBusRealTimeData(Leg leg) throws RealTimeDataException, StopNotFoundException, ExecutionException {
		Long scheduledTime = leg.getStartTime();
		String gtfsFromStop = leg.getFrom().getStopId().getId();
		String routeTag = leg.getRoute().trim();
		String gtfsTripId = leg.getTripId().trim();
		String tripHeadSign = leg.getHeadsign();

		List<RealTimePrediction> lstRealTimePredictions = new ArrayList<RealTimePrediction>();
		String data = "gtfsFromStop: "+ gtfsFromStop + ", gtfsTripId: " + gtfsTripId+ ", routeTag: " + routeTag+", HeadSign:"+tripHeadSign;
		logger.debug(loggerName,data);
		if(_verbose){
			SimpleDateFormat dateFormat = new SimpleDateFormat();
			dateFormat.setTimeZone(TimeZone.getTimeZone(ESTERN_TIMEZONE));
			logger.debug(loggerName,"Current Time  : "+dateFormat.format(new Date()));
			logger.debug(loggerName,"Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		}

		String apiDirectionId = stopMapping.getBusApiHeadSign().get(routeTag, tripHeadSign);
		Collection<String> apiRoutes = stopMapping.getBusRouteIdMapping().get(routeTag);
		validateNull(apiDirectionId,"No Direction ID Found "+data);
		if(apiDirectionId.equals("-"))
			throw new RealTimeDataException("Invalid Direction - found, possibly ambiguous in api: "+data);

		if(_verbose	)
			logger.debug(loggerName,"directionId: "+tripHeadSign+"->"+apiDirectionId);

		List<WmataBusPrediction> lstPredictions = getBusPredictions(gtfsFromStop,routeTag,tripHeadSign);
		if(_verbose	)
			logger.debug(loggerName,"Total Prediction To filter: "+lstPredictions.size());
		for (Iterator<WmataBusPrediction> iterator = lstPredictions.iterator(); iterator.hasNext();) {
			WmataBusPrediction p = iterator.next();
			boolean isValid = isValidRouteAndDirection(p,apiRoutes,apiDirectionId);
			if(isValid){
				lstRealTimePredictions.add(new RealTimePrediction(p));
			}
			if(_verbose){
				String prediction = "Route: "+p.getRouteID()+",HeadSign: "+tripHeadSign+", TripID:"+gtfsTripId+", Direction:"+p.getDirectionText()+"("+p.getDirectionNum()+"), VehicleID: "+p.getVehicleID()+", Min: "+p.getMinutes();
				if(isValid)
					logger.debug(loggerName,"          "+prediction);
				else
					logger.debug(loggerName,"      [X] "+prediction);
			}
		}
		validateList(lstRealTimePredictions,"No Matching Prediction Found For GtfsStop:"+gtfsFromStop);
		setScheduleTimesInPrediction(lstRealTimePredictions, leg, AGENCY_TYPE.WMATA);
		LegLiveFeed resp = new LegLiveFeed();
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

		String gtfsTripDestStop = null;
		//		String gtfsTripDestStop = stopMapping.getTripLastStopAndHead().get(tripId).getLeft();
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
	 * @param gtfsFromStopId the form grfs stop id
	 * @param routeLine the route line
	 * @param tripId the trip id
	 * @param scheduledTime the scheduled time
	 * @return the all rail real time data
	 * @throws RealTimeDataException the real time data exception
	 * @throws StopNotFoundException the stop not found exception
	 * @throws ExecutionException the execution exception
	 */
	private LegLiveFeed getAllRailRealTimeData(Leg leg) throws RealTimeDataException, StopNotFoundException, ExecutionException {
		Long scheduledTime = leg.getStartTime();
		String gtfsFromStopId = leg.getFrom().getStopId().getId();
		String gtfsToStopId = leg.getTo().getStopId().getId();
		String routeLine = leg.getRoute().trim();
		String tripId = leg.getTripId().trim();

		String data = "formGrfsStopId: "+ gtfsFromStopId+ ", routeLine: "+ routeLine	+ ", tripId: "+ tripId+ ", scheduledTime: "+ scheduledTime;
		logger.debug(loggerName,data);
		if(_verbose){
			SimpleDateFormat dateFormat = new SimpleDateFormat();
			dateFormat.setTimeZone(TimeZone.getTimeZone(ESTERN_TIMEZONE));
			logger.debug(loggerName,"Current Time  : "+dateFormat.format(new Date()));
			logger.debug(loggerName,"Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		}

		GtfsStop gtfsFromStop = stopMapping.getRailGtfsStopsById().get(gtfsFromStopId);
		validateNull(gtfsFromStop, "No valid Gtfs From stop found ->"+data);

		GtfsStop gtfsToStop = stopMapping.getRailGtfsStopsById().get(gtfsToStopId);
		validateNull(gtfsToStop, "No valid Gtfs To stop found ->"+data);

		String gtfsTripDestStop = stopMapping.getRailTripLastStop().get(tripId);
		validateNull(gtfsTripDestStop, "No valid destination gtfs stop found for trip: "+data);


		List<RailStation> railStations = gtfsFromStop.getLstRailStations();
		validateList(railStations, "No  rail station found for gtfs stop: "+gtfsFromStop);

		List<RealTimePrediction> lstRealTimePredictions = new ArrayList<RealTimePrediction>();
		String lineCode = stopMapping.getRailLineCodeByName(routeLine);
		for (RailStation apiFromStop : railStations) {
			if(!apiFromStop.haveLine(lineCode)){
				if(_verbose)
					logger.debug(loggerName, "skipped station : "+apiFromStop.getCode()+",Line: "+apiFromStop.getAllStationLineCodes());
				continue;
			}
			if(_verbose)
				logger.debug(loggerName,"Current/Destination[Gtfs(API)] : %s(%s) - %s(%s)",gtfsFromStop.getStopId(),apiFromStop.getAllStationCodes().toString(),
						gtfsToStop.getStopId(),gtfsToStop.getRailStopIds().toString());
			List<RailPrediction> apiEstimates = cachedApiClient.getRailPrediction(apiFromStop.getCode());
			validateList(apiEstimates, "No Valid Estimation found form API");
			for (Iterator<RailPrediction> iterator = apiEstimates.iterator(); iterator.hasNext();) {
				RailPrediction rp =  iterator.next();
				boolean isValid  = isValidRailPrediction(rp,apiFromStop,lineCode,gtfsToStop.getLstRailStations());
				if(isValid)
					lstRealTimePredictions.add(WmataRealTimePrediction.of(rp));
				if(_verbose){
					String predictionData = String.format("Line:%s, Loc:%s, Dest:%s, Min:%s", rp.getLine(),rp.getLocationCode(),	rp.getDestinationCode(),rp.getMin());
					if(isValid)
						logger.debug(loggerName,"          "+predictionData);
					else
						logger.debug(loggerName,"      [X] "+predictionData);

				}
			}
		}
		if(lstRealTimePredictions.isEmpty())
			throw new RealTimeDataException("No valid estimates found");
		setScheduleTimesInPrediction(lstRealTimePredictions, leg, AGENCY_TYPE.WMATA);
		LegLiveFeed resp = new LegLiveFeed();
		resp.setLstPredictions(lstRealTimePredictions);
		return resp;
	}



	/**
	 * Checks if is valid rail prediction. compares sequence for <b> from stop->to stop->prediction destination</b>
	 * @param rp RailPrediction
	 * @param apiFromStop the api stop
	 * @param lastApiStops - the last api stop list - may multiple
	 * @param routeLine the route line, e.g RD
	 * @param apiToStops
	 * @return true, if is valid rail prediction
	 * @throws RealTimeDataException
	 */
	private boolean isValidRailPrediction(RailPrediction rp, RailStation apiFromStop, String lineCode, List<RailStation> apiToStops) throws RealTimeDataException {
		if(!apiFromStop.haveLine(lineCode, true) || rp.getDestinationCode()==null){
			return false;
		}
		RailStopSequence lineStopSequence = stopMapping.getRailStopSequence().get(lineCode);
		validateNull(lineStopSequence, "No RailStopSequence found from mapping for line: "+lineCode);
		for (RailStation toStop : apiToStops) { // if multiple stops found for gtfs stop
			if(toStop.haveLine(lineCode)){
				boolean res = lineStopSequence.isValidSequence(apiFromStop.getAllStationCodes(), toStop.getAllStationCodes(), rp.getDestinationCode());
				if(res)
					return res;
			}
		}
		return false;
	}

	/**
	 * Gets the bus real time data.
	 *
	 * @param gtfsFromStop the gtfs from stop
	 * @param gtfsTripId the gtfs trip id
	 * @param routeTag the route tag
	 * @param scheduledTime the scheduled time
	 * @param tripHeadSign
	 * @return
	 * @return the bus real time data
	 * @throws RealTimeDataException the real time data exception
	 * @throws StopNotFoundException the stop not found exception
	 * @throws ExecutionException
	 */
	private LegLiveFeed getBusRealTimeData(String gtfsFromStop,String gtfsTripId,String routeTag,long scheduledTime, String tripHeadSign) throws RealTimeDataException, StopNotFoundException, ExecutionException {
		String data = "gtfsFromStop: "+ gtfsFromStop + ", gtfsTripId: " + gtfsTripId+ ", routeTag: " + routeTag+", HeadSign:"+tripHeadSign;
		logger.debug(loggerName,data);
		LegLiveFeed resp = new LegLiveFeed();
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.setTimeZone(TimeZone.getTimeZone(ESTERN_TIMEZONE));

		if(_verbose){
			logger.debug(loggerName,"Current Time  : "+dateFormat.format(new Date()));
			logger.debug(loggerName,"Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		}

		String apiDirectionId = stopMapping.getBusApiHeadSign().get(routeTag, tripHeadSign);
		Collection<String> apiRoutes = stopMapping.getBusRouteIdMapping().get(routeTag);
		validateNull(apiDirectionId,"No Direction ID Found "+data);
		if(apiDirectionId.equals("-"))
			throw new RealTimeDataException("Invalid Direction - found, possibly ambiguous in api: "+data);

		if(_verbose	)
			logger.debug(loggerName,"directionId: "+tripHeadSign+"->"+apiDirectionId);

		List<WmataBusPrediction> lstPredictions = getBusPredictions(gtfsFromStop,routeTag,tripHeadSign);
		if(_verbose	)
			logger.debug(loggerName,"Total Prediction To filter: "+lstPredictions.size());
		for (Iterator<WmataBusPrediction> iterator = lstPredictions.iterator(); iterator.hasNext();) {
			WmataBusPrediction p = iterator.next();
			if(!isValidRouteAndDirection(p,apiRoutes,apiDirectionId))
				iterator.remove();
		}
		validateList(lstPredictions,"No Matching Prediction Found For GtfsStop:"+gtfsFromStop);
		if(_verbose ){
			logger.debug(loggerName,"Filtered---->");
			for (WmataBusPrediction p : lstPredictions) {
				logger.debug(loggerName,"       Route: "+p.getRouteID()+", Direction:"+p.getDirectionText()+"("+p.getDirectionNum()+"), VehicleID: "+p.getVehicleID()+", Min: "+p.getMinutes());
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
	 * Checks if is valid route and direction.
	 *
	 * @param p the p
	 * @param apiRoutes the api routes
	 * @param apiDirectionId the api direction id
	 * @return true, if is valid route and direction
	 */
	private boolean isValidRouteAndDirection(WmataBusPrediction p,Collection<String> apiRoutes, String apiDirectionId) {
		String apiRoute = p.getRouteID();
		String direction = p.getDirectionNum();
		for (String route : apiRoutes) {
			if(equalsIgnoreCase(apiRoute, route) && equalsIgnoreCase(direction, apiDirectionId) ){
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the bus predictions.
	 * @param routeTag
	 * @param gtfsFromStop
	 * @param gtfsFromStop
	 * @param tripHeadSign
	 *
	 * @param lstBusStops the lst bus stops
	 * @return the bus predictions
	 * @throws StopNotFoundException
	 * @throws ExecutionException
	 * @throws RealTimeDataException
	 */
	private  List<WmataBusPrediction> getBusPredictions(String gtfsFromStop, String routeTag, String tripHeadSign) throws StopNotFoundException, RealTimeDataException, ExecutionException {
		GtfsStop gtfsStop = stopMapping.getGtfsBusStopById(gtfsFromStop);
		validateNull(gtfsStop, "No gtfs Stop Found from mapping :"+gtfsFromStop);

		List<BusStop> lstBusStops = gtfsStop.getLstBusStops();
		if(lstBusStops.size()>1){ // check route in case of multiple api stops
			List<BusStop> lstfilteredBusStops = new ArrayList<BusStop>();
			Collection<String> apiRoutes = stopMapping.getBusRouteIdMapping().get(routeTag);
			for (BusStop busStop : lstBusStops) {
				for (String apiRoute : apiRoutes) {
					if(busStop.haveRoute(apiRoute,true))
						lstfilteredBusStops.add(busStop);
					break;
				}
			}
			lstBusStops = lstfilteredBusStops;
		}
		validateList(lstBusStops, "No api stops found for route : "+routeTag);
		if(lstBusStops.size()>1){ // use matching headsign to break tie
			List<BusStop> lstfilteredBusStops = new ArrayList<BusStop>();
			for (BusStop busStop : lstBusStops) {
				if(equalsIgnoreCase(busStop.getHeadSign(),tripHeadSign)){
					lstfilteredBusStops.add(busStop);
				}
			}
			lstBusStops = lstfilteredBusStops;
		}
		validateList(lstBusStops, "No api stops found for route : "+routeTag);
		if(lstBusStops.size()!=1)
			throw new RealTimeDataException("empty or more then one stop mapping found.:"+lstBusStops);

		BusStop busStop = lstBusStops.get(0);
		logger.debug(loggerName,"Getting Real time prediction for stop:"+busStop.getStopId()+" - "+busStop.getName());
		List<WmataBusPrediction> lst= cachedApiClient.getBusPredictionAtStop(busStop.getStopId());
		List<WmataBusPrediction> lstPredictions =  new ArrayList<WmataBusPrediction>();
		if(!ComUtils.isEmptyList(lst)){
			lstPredictions.addAll(lst);
			for (WmataBusPrediction predictions : lst) {
				predictions.setBusStop(busStop);
			}
		}
		return lstPredictions;
	}

	private void setScheduleTimesInPrediction(List<RealTimePrediction> lstRealTimePredictions, Leg leg, AGENCY_TYPE type) {
		for (RealTimePrediction realTimePrediction : lstRealTimePredictions) {
			gtfsDataService.getMatchingScheduleForRealTime(leg, type,realTimePrediction);
		}
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
	public WmataApiClient getApiClient() {
		return apiClient;
	}

	public void setApiClient(WmataApiClient apiClient) {
		this.apiClient = apiClient;
	}


	public void setMaxEarlyThresold(int maxEarlyThresold) {
		this.maxEarlyThresold = maxEarlyThresold;
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
	private void validateList(Collection<?> lst, String msg) throws RealTimeDataException {
		if(lst==null || lst.size()==0)
			throw new RealTimeDataException(msg);
	}

	@Override
	public LegLiveFeed getLegArrivalTime(Leg leg) throws FeedsNotFoundException {
		return null;
	}


	/**
	 * @return the gtfsDataService
	 */
	public GtfsDataService getGtfsDataService() {
		return gtfsDataService;
	}

	/**
	 * @param gtfsDataService the gtfsDataService to set
	 */
	public void setGtfsDataService(GtfsDataService gtfsDataService) {
		this.gtfsDataService = gtfsDataService;
	}

	@Override
	public VehiclePosition getVehiclePosition(Leg leg)	throws FeedsNotFoundException {
		return null;
	}
}