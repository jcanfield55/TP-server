/*
 * @author nirmal
 */
package com.nimbler.tp.rest;

import static org.apache.commons.lang3.StringUtils.replaceOnce;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.Itinerary;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
import com.nimbler.tp.dataobject.LiveFeedResponse;
import com.nimbler.tp.dataobject.PlanLiveFeeds;
import com.nimbler.tp.dataobject.TraverseMode;
import com.nimbler.tp.dataobject.TripPlan;
import com.nimbler.tp.dataobject.nextbus.VehiclePosition;
import com.nimbler.tp.dataobject.wmata.GtfsStop;
import com.nimbler.tp.dataobject.wmata.RailLine;
import com.nimbler.tp.dataobject.wmata.RailStation;
import com.nimbler.tp.dataobject.wmata.StopMapping;
import com.nimbler.tp.dataobject.wmata.WmataRouteDetails;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.TpPlanService;
import com.nimbler.tp.service.livefeeds.NextBusApiImpl;
import com.nimbler.tp.service.livefeeds.RealTimeAPI;
import com.nimbler.tp.service.livefeeds.RealTimeAPIFactory;
import com.nimbler.tp.service.livefeeds.WmataApiImpl;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.PlanUtil;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.ETA_FLAG;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;
import com.nimbler.tp.util.TpException;
/**
 * 
 * @author nIKUNJ
 *
 */
@Path("/livefeeds/")
public class RealTimePredictionService {

	@Autowired
	private LoggingService logger;

	@Autowired
	TpPlanService planService;

	@Autowired
	NextBusApiImpl nextBusApiImpl;

	private String loggerName;

	@GET
	@Path("/itinerary/")
	@Deprecated
	public String predictWholeItinerary(@QueryParam(RequestParam.ITINERARY_ID) String itineraryId) {
		LiveFeedResponse response = new LiveFeedResponse();
		try {
			Itinerary itin = getFullItinerary(itineraryId);
			response.setItineraryId(itineraryId);
			if (itin == null) {
				logger.error(loggerName, "Itinerary not found in DB: "+itineraryId); 
				response.setError(TP_CODES.FAIL.getCode());
				throw new TpException(TP_CODES.FAIL.getCode());
			}

			List<Leg> legs = getApplicableLegs(itin.getLegs());
			if (legs == null || legs.size()==0) {
				response.setError(TP_CODES.DATA_NOT_EXIST.getCode());
				logger.warn(loggerName, "Live feeds not applicable for any of the legs of Itinerary: "+itineraryId); 
				throw new TpException(TP_CODES.DATA_NOT_EXIST.getCode());
			}
			for (Leg leg : legs) {
				try {
					RealTimeAPI liveFeedAPI = RealTimeAPIFactory.getInstance().getLiveFeedAPI(leg);
					LegLiveFeed legFeed = liveFeedAPI.getLiveFeeds(leg);
					if (legFeed!=null)
						response.addLegLiveFeed(legFeed);
				} catch (FeedsNotFoundException e) {
					logger.warn(loggerName, e.getMessage());
				}
			}
			if (response.getLegLiveFeeds()==null || response.getLegLiveFeeds().size()==0)
				response.setError(TP_CODES.DATA_NOT_EXIST.getCode());
		} catch (TpException tpe) {
			logger.error(loggerName, tpe.getErrMsg());
			response.setError(tpe.getErrCode());
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setError(TP_CODES.FAIL.ordinal());
		}
		return getJsonResponse(response);
	}

	/**
	 * Predict itineraries.
	 *
	 * @param itineraryId the comma seprated itinerary ids
	 * @return the string
	 */
	@GET
	@Path("/itineraries/")
	public String predictItineraries (@QueryParam(RequestParam.ITINERARY_ID) String itineraryId,
			@DefaultValue("false") @QueryParam(RequestParam.FOR_TODAY) Boolean forToday
			) {
		PlanLiveFeeds response = new PlanLiveFeeds();
		try {
			List<Itinerary> itineraries =planService.getFullItinerariesByIds(itineraryId.split(","));
			if ( ComUtils.isEmptyList(itineraries) ) 
				throw new TpException(TP_CODES.DATA_NOT_EXIST.getCode());
			if (forToday)
				updateLegTimeToToday(itineraries);

			for (Itinerary itin: itineraries) {
				//				System.out.println("Itinerary Start Time:---------->> "+new Date(itin.getStartTime()));
				LiveFeedResponse itinFeeds = getRealTimeLegData(itin);
				if(itinFeeds!=null && !ComUtils.isEmptyList(itinFeeds.getLegLiveFeeds())){
					adjustLegOverLap(itin,itinFeeds);
					updateItineraryTimeFlag(itinFeeds); 
					response.addItinLiveFeeds(itinFeeds); 
				}
			}
			if (response.getItinLiveFeeds()==null || response.getItinLiveFeeds().size()==0)
				response.setError(TP_CODES.DATA_NOT_EXIST.getCode());
		} catch (TpException tpe) {
			logger.error(loggerName, tpe.getErrMsg());
			response.setError(tpe.getErrCode());
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setError(TP_CODES.FAIL.getCode());
		}
		return getJsonResponse(response);
	}

	/**
	 * Predict real time by legs- all required detailed of legs will be as input.
	 *
	 * @param strLegs the str legs
	 * @return the string
	 */
	@POST
	@Path("/bylegs/")
	public String predictRealTimeByLegs(@Context HttpServletRequest httpRequest) {
		PlanLiveFeeds response = new PlanLiveFeeds();
		try {
			Map<String,String> reqParam = ComUtils.parseMultipartRequest(httpRequest);
			return predictRealTimeByLegs(reqParam);
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setError(TP_CODES.FAIL.getCode());
		}
		String res =  getJsonResponse(response);
		return res;
	}
	@POST
	@Path("/vehiclePosition/")
	@SuppressWarnings("unchecked")
	public String getVehiclePosition(@Context HttpServletRequest httpRequest) {
		PlanLiveFeeds response = new PlanLiveFeeds();
		try {
			Map<String,String> reqParam = ComUtils.parseMultipartRequest(httpRequest);
			String strLegs = reqParam.get(RequestParam.LEGS);	
			List<Leg> lstLegs =  JSONUtil.getLegsJson(strLegs);
			int  appType =  NumberUtils.toInt(reqParam.get(TpConstants.APP_TYPE),-1);  
			if(ComUtils.isEmptyList(lstLegs))
				throw new TpException(TP_CODES.INVALID_REQUEST.getCode(),"Error while getting JSON String from plan object.");

			LegLiveFeed legLiveFeeds = getVehiclePositionsForLegs(lstLegs,appType);
			if (legLiveFeeds!=null && legLiveFeeds.getLstVehiclePositions()!=null && legLiveFeeds.getLstVehiclePositions().size()>0) 
				response.addLegLiveFeeds(legLiveFeeds); 
			else
				response.setError(TP_CODES.DATA_NOT_EXIST.getCode());
		} catch (TpException tpe) {
			logger.error(loggerName, tpe.getErrMsg());
			response.setError(tpe.getErrCode());
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setError(TP_CODES.FAIL.getCode());
		}
		String res =  getJsonResponse(response);
		//System.out.println(res);
		return res;
	}

	/**
	 * Predict real time by legs.
	 *
	 * @param reqParam the req param
	 * @return the string
	 */
	private String predictRealTimeByLegs(Map<String, String> reqParam) {
		PlanLiveFeeds response = new PlanLiveFeeds();
		try {
			String strLegs = reqParam.get(RequestParam.LEGS);			
			strLegs = replaceOnce(strLegs, "(","[");
			strLegs = replaceOnce(strLegs, ")","]");			
			List lstLegs =  JSONUtil.getLegsJson(strLegs);
			if(ComUtils.isEmptyList(lstLegs))
				throw new TpException(TP_CODES.INVALID_REQUEST.getCode(),"Error while getting JSON String from plan object.");

			long start = System.currentTimeMillis();
			List<LegLiveFeed> legLiveFeeds = getAllRealTimeFeedsForLegs(lstLegs);
			long end = System.currentTimeMillis();
			logger.debug(loggerName,"Operation took " + (end - start)  + " msec");

			if (!ComUtils.isEmptyList(legLiveFeeds)) 
				response.setLegLiveFeeds(legLiveFeeds); 
			else
				response.setError(TP_CODES.DATA_NOT_EXIST.getCode());
		} catch (TpException tpe) {
			logger.error(loggerName, tpe.getErrMsg());
			response.setError(tpe.getErrCode());
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setError(TP_CODES.FAIL.getCode());
		}
		String res =  getJsonResponse(response);
		//System.out.println("Realtime--> "+res);
		return res;
	}

	/**
	 * Gets the real time leg data.
	 *
	 * @param itin the itin
	 * @return the real time leg data
	 */
	private LiveFeedResponse getRealTimeLegData(Itinerary itin) {
		LiveFeedResponse itinFeeds = new LiveFeedResponse();
		itinFeeds.setItineraryId(itin.getId());
		List<Leg> legs = getApplicableLegs(itin.getLegs());	
		if(ComUtils.isEmptyList(legs)){
			logger.debug(loggerName, "Live feeds not applicable for any of the legs of Itinerary: "+itin.getId()); 
			return null;
		}
		for (Leg leg : legs) {
			try {
				RealTimeAPI liveFeedAPI = RealTimeAPIFactory.getInstance().getLiveFeedAPI(leg);
				LegLiveFeed legFeed = liveFeedAPI.getLiveFeeds(leg);
				if (legFeed!=null) {
					itinFeeds.addLegLiveFeed(legFeed);
				}
			} catch (FeedsNotFoundException e) {
				logger.info(loggerName, e.getMessage());
			}
		}
		return itinFeeds;
	}

	/**
	 * Gets the real time leg data legs.
	 *
	 * @param legs the legs
	 * @return the real time leg data legs
	 */
	private List<LegLiveFeed> getAllRealTimeFeedsForLegs(List<Leg> legs) {
		List<LegLiveFeed> lstRes = new ArrayList<LegLiveFeed>();
		legs = getApplicableLegs(legs);	
		if(ComUtils.isEmptyList(legs)){
			logger.debug(loggerName, "Live feeds not applicable for any of the legs: "+legs); 
			return null;
		}
		for (Leg leg : legs) {
			try {
				RealTimeAPI liveFeedAPI = RealTimeAPIFactory.getInstance().getLiveFeedAPI(leg);
				LegLiveFeed legFeed = liveFeedAPI.getAllRealTimeFeeds(leg);
				if (legFeed!=null) {
					lstRes.add(legFeed);
				}
			} catch (FeedsNotFoundException e) {
				logger.info(loggerName, e.getMessage());
			}
		}
		return lstRes;
	}

	/**
	 * Gets the vehicle positions for legs.
	 *
	 * @param legs the legs
	 * @param appType 
	 * @return the vehicle positions for legs
	 */
	private LegLiveFeed getVehiclePositionsForLegs(List<Leg> legs, int appType) {
		LegLiveFeed legLiveFeed = new LegLiveFeed();
		//legs = getApplicableLegs(legs);	
		if(ComUtils.isEmptyList(legs)){
			//logger.debug(loggerName, "Live feeds not applicable for any of the legs: "+legs); 
			return null;
		}
		// for backward compability.
		boolean isSfBayApp = (appType==NIMBLER_APP_TYPE.SF_BAY_AREA.ordinal()) ||  (appType==NIMBLER_APP_TYPE.CALTRAIN.ordinal());
		for (Leg leg : legs) {
			try {
				RealTimeAPI liveFeedAPI = isSfBayApp?nextBusApiImpl:RealTimeAPIFactory.getInstance().getLiveFeedAPI(leg);
				VehiclePosition vehiclePosition = liveFeedAPI.getVehiclePosition(leg);
				if (vehiclePosition!=null) {
					legLiveFeed.addVehiclePosition(vehiclePosition);
				}
			} catch (FeedsNotFoundException e) {
				logger.info(loggerName, e.getMessage());
			}
		}
		return legLiveFeed;
	}

	/**
	 * Update leg time to today.
	 *
	 * @param itineraries the itineraries
	 */
	private void updateLegTimeToToday(List<Itinerary> itineraries) {
		for (Itinerary itinerary : itineraries) {
			List<Leg> legs = itinerary.getLegs();
			if(legs==null){
				logger.debug(loggerName, "No legs found in itinerary: "+itinerary.getId());
				continue;
			}
			for (Leg leg : legs) {
				long time =ComUtils.getTodayDateTime(leg.getStartTime());				
				leg.setStartTime(time);
			}
		}

	}
	Map<String, WmataRouteDetails> wmataCache = new HashMap<String, WmataRouteDetails>();
	@GET
	@Path("/wmata/")
	public String getWmataStops(@QueryParam("name") String sortname) {
		try {
			WmataRouteDetails details = wmataCache.get(sortname);
			if(details==null){
				details = BeanUtil.getWMATAApiImpl().getApiClient().getBusRouteDetails("wateq3gxqzb9s597qky6khd7", sortname);
				if(details!=null)
					wmataCache.put(sortname, details);
			}
			return JSONUtil.getJsonFromObj(details);
		} catch (RealTimeDataException e) {
			e.printStackTrace();
		} catch (TpException e) {
			e.printStackTrace();
		}
		return "Not Found";
	}
	@GET
	@Path("/busstop/")
	public String getStops(@QueryParam("s") String stopID,@QueryParam("r") String routeId) {
		try {
			WmataApiImpl apiImpl =  (WmataApiImpl) TPApplicationContext.getInstance().getBean("wmataApiImpl");
			StopMapping stopMapping =  apiImpl.getStopMapping();
			Map<String, GtfsStop> stopIdMap = stopMapping.getBusGtfsStopsById();
			GtfsStop gtfsStop = stopIdMap.get(stopID);
			if(gtfsStop==null){
				return " No gtfs Stop";
			}
			return JSONUtil.getJsonFromObj(gtfsStop.getBusStopsForRoute(routeId));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Error";
	}
	@GET
	@Path("/railstop/")
	public String getRailStops(@QueryParam("gtfsStopId") String gtfsStopId,@QueryParam("line") String line) {
		try {
			WmataApiImpl apiImpl =  (WmataApiImpl) TPApplicationContext.getInstance().getBean("wmataApiImpl");
			StopMapping stopMapping =  apiImpl.getStopMapping();
			List<RailStation> res = new ArrayList<RailStation>();
			GtfsStop gtfsStop = stopMapping.getRailGtfsStopsById().get(gtfsStopId);
			Map<RailLine, List<RailStation>> lstGtfsStops = stopMapping.getRailStationByRailLine();
			for (Map.Entry<RailLine, List<com.nimbler.tp.dataobject.wmata.RailStation>> entry : lstGtfsStops.entrySet()) {
				List<RailStation> railStations = entry.getValue();
				RailLine railLine = entry.getKey();
				if (StringUtils.equalsIgnoreCase(railLine.getDisplayName(),line)){
					for (RailStation rs : railStations) {
						if(!ComUtils.isEmptyList(gtfsStop.getLstRailStations()) && gtfsStop.getLstRailStations().get(0).haveStationCode(rs.getCode())){
							res.add(rs);
						}
					}
					break;
				}

			}

			return JSONUtil.getJsonFromObj(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Error";
	}
	@GET
	@Path("/getgtfsstop/")
	public String getGtfsStops(@QueryParam("s") String stopID,@QueryParam("c") String code) {
		try {
			WmataApiImpl apiImpl =  (WmataApiImpl) TPApplicationContext.getInstance().getBean("wmataApiImpl");
			StopMapping stopMapping =  apiImpl.getStopMapping();
			Collection<RailStation> gtfsStop = stopMapping.getAllRailStations();
			List<GtfsStop> res = new ArrayList<GtfsStop>();



			return JSONUtil.getJsonFromObj(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Error";
	}
	/*	@GET
	@Path("/plan/")
	@Deprecated
	public String predictWholePlan(@QueryParam(RequestParam.PLAN_ID) String planId) {
		PlanLiveFeeds response = new PlanLiveFeeds(planId);
		try {
			TripPlan plan = getPlanWithItineraries(planId);
			if (plan == null) {
				logger.warn(loggerName, "Plan not found in DB: "+planId); 
				response.setError(TP_CODES.FAIL.getCode());
				throw new TpException(TP_CODES.FAIL.getCode());
			}

			List<Itinerary> itineraries = plan.getItineraries();
			if ( itineraries == null || itineraries.size()==0) {
				logger.warn(loggerName, "No itineraries found for Plan in DB: "+planId); 
				response.setError(TP_CODES.FAIL.getCode());
				throw new TpException(TP_CODES.FAIL.getCode());
			}

			for (Itinerary itin: itineraries) {
				LiveFeedResponse itinFeeds = new LiveFeedResponse();
				itinFeeds.setItineraryId(itin.getId());
				List<Leg> legs = getApplicableLegs(itin.getLegs());
				if (legs == null || legs.size()==0) {
					logger.debug(loggerName, "Live feeds not applicable for any of the legs of Itinerary: "+itin.getId()); 
					continue;
				}
				for (Leg leg : legs) {
					try {
						RealTimeAPI liveFeedAPI = RealTimeAPIFactory.getInstance().getLiveFeedAPI(leg.getMode());
						LegLiveFeed legFeed = liveFeedAPI.getLiveFeeds(leg);
						if (legFeed!=null) {
							itinFeeds.addLegLiveFeed(legFeed);
						}
					} catch (FeedsNotFoundException e) {
						logger.error(loggerName, e.getMessage());
					}
				}
				if (itinFeeds.getLegLiveFeeds()!=null && itinFeeds.getLegLiveFeeds().size()>0) {
					updateItineraryTimeFlag(itinFeeds); 
					response.addItinLiveFeeds(itinFeeds); 
				}
			}
			if (response.getItinLiveFeeds()==null || response.getItinLiveFeeds().size()==0)
				response.setError(TP_CODES.DATA_NOT_EXIST.getCode());
		} catch (TpException tpe) {
			logger.error(loggerName, tpe.getErrMsg());
			response.setError(tpe.getErrCode());
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setError(TP_CODES.FAIL.getCode());
		}
		return getJsonResponse(response);
	}*/
	/**
	 * ######################################   PRIVATE API ##############################################################
	 */
	/**
	 * 
	 * @param planId
	 * @return
	 */
	private TripPlan getPlanWithItineraries(String planId) {
		TpPlanService planService = BeanUtil.getPlanService();
		return planService.getFullPlanFromDB(planId); 
	}
	/**
	 * 
	 * @param itinId
	 * @return
	 * @throws DBException
	 */
	private Itinerary getFullItinerary(String itinId) {
		PersistenceService persistenceService = BeanUtil.getPersistanceService();
		try {
			List resultSet = persistenceService.find(TpConstants.MONGO_TABLES.itinerary.name(), "id", itinId, Itinerary.class);
			if (resultSet!=null && resultSet.size()>0) {
				Itinerary itin = (Itinerary)resultSet.get(0);
				List resultSetLegs = persistenceService.find(TpConstants.MONGO_TABLES.leg.name(), "itinId", itin.getId(), Leg.class);
				if (resultSetLegs!=null && resultSetLegs.size()>0) {
					itin.setLegs(new ArrayList<Leg>(resultSetLegs)); 
				}
				return itin;
			}
		} catch (DBException e) {
			logger.error(loggerName, "Error while getting Itineray from DB: "+e.getMessage()); 
		}
		return null;
	}
	/**
	 * Get legs for which live feeds are available. 
	 * @param legs
	 * @return
	 */
	private List<Leg> getApplicableLegs(List<Leg> legs) {
		List<Leg> applicableLegs = new ArrayList<Leg>();
		for (Leg leg: legs) {
			if (leg.getMode().equals(TpConstants.LIVE_FEED_MODES.BUS.name()) || leg.getMode().equals(TpConstants.LIVE_FEED_MODES.SUBWAY.name())
					|| leg.getMode().equals(TpConstants.LIVE_FEED_MODES.TRAM.name()) || leg.getMode().equals(TpConstants.LIVE_FEED_MODES.CABLE_CAR.name())) {
				applicableLegs.add(leg);
			}
		}
		return applicableLegs;
	}
	/**
	 * 
	 * @param itinFeeds
	 */
	private void updateItineraryTimeFlag(LiveFeedResponse itinFeeds) {
		boolean delayed = false; boolean early = false;
		for (LegLiveFeed legFeed: itinFeeds.getLegLiveFeeds()) {
			if (legFeed.getArrivalTimeFlag() == ETA_FLAG.DELAYED.ordinal())
				delayed = true;
			else if (legFeed.getArrivalTimeFlag() == ETA_FLAG.EARLY.ordinal())
				early = true;
		}
		if (delayed && early)
			itinFeeds.setArrivalTimeFlag(ETA_FLAG.ITINERARY_TIME_SLIPPAGE.ordinal()); 
		else if (delayed)
			itinFeeds.setArrivalTimeFlag(ETA_FLAG.DELAYED.ordinal());
		else if (early)
			itinFeeds.setArrivalTimeFlag(ETA_FLAG.EARLY.ordinal());
		else
			itinFeeds.setArrivalTimeFlag(ETA_FLAG.ON_TIME.ordinal());		
	}
	/**
	 * Adjust leg over lap.
	 *
	 * @param itin the itin
	 * @param itinFeeds the itin feeds
.	 */
	private void adjustLegOverLap(Itinerary itin, LiveFeedResponse itinFeeds) {
		try {
			List<Leg> legs = itin.getLegs();
			if(itinFeeds==null || ComUtils.isEmptyList(itinFeeds.getLegLiveFeeds())){				
				//				System.out.println("no life feeds.....");
				return;
			}
			List<LegLiveFeed> lstLegLiveFeeds =  itinFeeds.getLegLiveFeeds();
			List<LegLiveFeed> lstFeedsToAdd = new ArrayList<LegLiveFeed>();
			for (LegLiveFeed legLiveFeed : lstLegLiveFeeds) {
				LegLiveFeed feed = PlanUtil.shiftLeg(legs, legLiveFeed);
				if(feed!=null){
					lstFeedsToAdd.add(feed);
					logger.debug(loggerName, "Shifted Walk leg: "+feed);
					//					System.out.println("Shifted Walk leg: "+feed);
				}
			}
			if(lstFeedsToAdd.size()>0)
				lstLegLiveFeeds.addAll(lstFeedsToAdd);
			PlanUtil.validateLegConflict(legs,itinFeeds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param response
	 * @return
	 */
	private String getJsonResponse(Object response) {
		try {
			return JSONUtil.getJsonFromObj(response);
		} catch (TpException e) {
			logger.error(loggerName, e.getMessage());  
		}
		return "";
	}
	public String getLoggerName() {
		return loggerName;
	}
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	public static void main(String[] args) {
		try {
			Leg fistWalk = new Leg();
			fistWalk.setId("fw");
			fistWalk.setMode(TraverseMode.WALK.toString());
			fistWalk.setStartTime(10l);
			fistWalk.setEndTime(20l);

			Leg transit = new Leg();
			transit.setId("transit");
			transit.setMode(TraverseMode.SUBWAY.toString());
			transit.setStartTime(23l);
			transit.setEndTime(28l);


			Leg lastWalk = new Leg();
			lastWalk.setId("lw");
			lastWalk.setMode(TraverseMode.WALK.toString());
			lastWalk.setStartTime(30l);
			lastWalk.setEndTime(40l);

			Leg transit1 = new Leg();
			transit1.setId("transit1");
			transit1.setMode(TraverseMode.SUBWAY.toString());
			transit1.setStartTime(1l);
			transit1.setEndTime(5l);

			Itinerary itinerary = new Itinerary();
			itinerary.addLeg(transit1);
			itinerary.addLeg(fistWalk);
			itinerary.addLeg(transit);
			itinerary.addLeg(lastWalk);

			//			System.out.println(JSONUtil.getJsonFromObj(itinerary.getLegs()));
			LegLiveFeed legFeed = new LegLiveFeed();
			legFeed.setLeg(transit);
			legFeed.setTimeDiffInMins(6);
			legFeed.setDepartureTime(transit.getStartTime()-6);
			legFeed.setArrivalTimeFlag(ETA_FLAG.EARLY.ordinal());

			LiveFeedResponse feedResponse = new LiveFeedResponse();
			feedResponse.addLegLiveFeed(legFeed);
			RealTimePredictionService service = new RealTimePredictionService();
			service.adjustLegOverLap(itinerary, feedResponse);
			//		System.out.println(getLegAtOffset(itinerary.getLegs(), fistWalk,3));
			System.out.println(feedResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public TpPlanService getPlanService() {
		return planService;
	}
	public void setPlanService(TpPlanService planService) {
		this.planService = planService;
	}
	public NextBusApiImpl getNextBusApiImpl() {
		return nextBusApiImpl;
	}
	public void setNextBusApiImpl(NextBusApiImpl nextBusApiImpl) {
		this.nextBusApiImpl = nextBusApiImpl;
	}

}