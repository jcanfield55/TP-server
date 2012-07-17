package com.nimbler.tp.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.dataobject.Itinerary;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
import com.nimbler.tp.dataobject.LiveFeedResponse;
import com.nimbler.tp.dataobject.PlanLiveFeeds;
import com.nimbler.tp.dataobject.TripPlan;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.TpPlanService;
import com.nimbler.tp.service.livefeeds.RealTimeAPI;
import com.nimbler.tp.service.livefeeds.RealTimeAPIFactory;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.ETA_FLAG;
import com.nimbler.tp.util.TpException;
/**
 * 
 * @author nIKUNJ
 *
 */
@Path("/livefeeds/")
public class RealTimePredictionService {

	private LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private String loggerName;

	@GET
	@Path("/itinerary/")
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
					RealTimeAPI liveFeedAPI = RealTimeAPIFactory.getInstance().getLiveFeedAPI(leg.getMode());
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

	@GET
	@Path("/plan/")
	public String predictWholePlan(@QueryParam(RequestParam.PLAN_ID) String planId) {
		PlanLiveFeeds response = new PlanLiveFeeds(planId);
		try {
			TripPlan plan = getPlanWithItineraries(planId);
			if (plan == null) {
				logger.error(loggerName, "Plan not found in DB: "+planId); 
				response.setError(TP_CODES.FAIL.getCode());
				throw new TpException(TP_CODES.FAIL.getCode());
			}

			List<Itinerary> itineraries = plan.getItineraries();
			if ( itineraries == null || itineraries.size()==0) {
				logger.error(loggerName, "No itineraries found for Plan in DB: "+planId); 
				response.setError(TP_CODES.FAIL.getCode());
				throw new TpException(TP_CODES.FAIL.getCode());
			}

			for (Itinerary itin: itineraries) {
				LiveFeedResponse itinFeeds = new LiveFeedResponse();
				itinFeeds.setItineraryId(itin.getId());
				List<Leg> legs = getApplicableLegs(itin.getLegs());
				if (legs == null || legs.size()==0) {
					logger.warn(loggerName, "Live feeds not applicable for any of the legs of Itinerary: "+itin.getId()); 
					continue;
				}
				for (Leg leg : legs) {
					try {
						RealTimeAPI liveFeedAPI = RealTimeAPIFactory.getInstance().getLiveFeedAPI(leg.getMode());
						LegLiveFeed legFeed = liveFeedAPI.getLiveFeeds(leg);
						if (legFeed!=null) {
							if (legFeed.getArrivalTimeFlag() == ETA_FLAG.DELAYED.ordinal() 
									|| legFeed.getArrivalTimeFlag() == ETA_FLAG.EARLY.ordinal()) {
								itinFeeds.setArrivalTimeFlag(ETA_FLAG.ITINERARY_TIME_SLIPPAGE.ordinal());
							}
							itinFeeds.addLegLiveFeed(legFeed);
						}
					} catch (FeedsNotFoundException e) {
						logger.error(loggerName, e.getMessage());
					}
				}
				if (itinFeeds.getLegLiveFeeds()!=null && itinFeeds.getLegLiveFeeds().size()>0)
					response.addItinLiveFeeds(itinFeeds); 
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
					|| leg.getMode().equals(TpConstants.LIVE_FEED_MODES.TRAM.name())) {
				applicableLegs.add(leg);
			}
		}
		return applicableLegs;
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
}