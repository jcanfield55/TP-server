/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.document.mongodb.query.Order;

import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.AgencyAndId;
import com.nimbler.tp.dataobject.Itinerary;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.StopTimeType;
import com.nimbler.tp.dataobject.TripPlan;
import com.nimbler.tp.dataobject.TripResponse;
import com.nimbler.tp.gtfs.PlanCompareTask;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.GtfsUtils;
import com.nimbler.tp.util.HttpUtils;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
import com.nimbler.tp.util.TpException;

/**
 * 
 * @author nIKUNJ
 *
 */
public class TpPlanService {

	@Autowired
	private LoggingService logger; 
	@Autowired
	private PersistenceService persistenceService;
	/**
	 * 
	 */
	private String loggerName;

	public static String OTP_DATE_FORMAT="MM/dd/yyyy";
	public static String OTP_TIME_FORMAT="hh:mm a";

	/**
	 * 
	 * @param deviceId
	 * @param planJsonString
	 * @return
	 */
	public TripPlan savePlan(String deviceId, String planJsonString) {
		try {
			TripResponse tripResponse = JSONUtil.getFullPlanObjFromJson(planJsonString);
			if(tripResponse== null || tripResponse.getPlan()==null)
				throw new TpException("Error while getting Planb object from JSON string.");
			TripPlan plan = tripResponse.getPlan();

			setPlanUrl(tripResponse);

			plan.setDeviceId(deviceId);
			plan.setCreateTime(System.currentTimeMillis()); 
			List<Itinerary> itineraries = plan.getItineraries();
			plan.setItineraries(null);//set null as we don't want embedded reference in mongo db.


			persistenceService.addObject(TpConstants.MONGO_TABLES.plan.name(), plan);

			if (itineraries == null || itineraries.size()==0) {
				logger.debug(loggerName, "No itineraries to save for plan Id: "+plan.getId()); 
				return getResponsePlanFromFullPlan(plan);
			}
			plan.setItineraries(itineraries);//reset for response purpose
			for (Itinerary itin: itineraries) {
				List<Leg> legs = itin.getLegs();
				itin.setLegs(null);//set null as we don't want embedded reference in mongo db.
				itin.setPlanId(plan.getId());

				persistenceService.addObject(TpConstants.MONGO_TABLES.itinerary.name(), itin);
				if (legs==null || legs.size()==0) {
					logger.debug(loggerName, "No legs to save for itinerary id: "+itin.getId());
					continue;
				}
				for (Leg leg: legs) {
					leg.setItinId(itin.getId());
				}
				persistenceService.addObjects(TpConstants.MONGO_TABLES.leg.name(), legs); 
				itin.setLegs(legs); //reset for response purpose
			}
			return getResponsePlanFromFullPlan(plan);
		} catch (TpException e) {
			logger.error(loggerName, "Error while saving trip in DB: "+e.getMessage());
		} catch (DBException e) {
			logger.error(loggerName, "Error while saving trip in DB: "+e.getMessage());
		}
		return null;
	}

	/**
	 * Sets the plan url.
	 *
	 * @param tripResponse the new plan url
	 */
	private void setPlanUrl(TripResponse tripResponse) {
		try {			
			String url = GtfsUtils.getPlanUrlFromResponse("",tripResponse,PlanCompareTask.arrParams,null);
			tripResponse.getPlan().setPlanUrlParams(url);
		} catch (TpException e) {
			logger.error(loggerName, e.getErrMsg());				
		}


	}
	/**
	 * Genearete plan.
	 *
	 * @param reqMap the req map
	 * @return the trip response
	 * @throws MalformedURLException the malformed url exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws CloneNotSupportedException the clone not supported exception
	 * @throws TpException the tp exception
	 */
	public TripResponse genearetePlan(Map<String, String> reqMap) throws TpException {
		TripResponse response = null;
		try{
			String deviceId = reqMap.get(RequestParam.DEVICE_ID);
			List<String> lstOtpParams = new ArrayList<String>();
			for (int i = 0; i <  TpConstants.OTP_PARAMETERS.length; i++) {
				String val = reqMap.get( TpConstants.OTP_PARAMETERS[i]);
				if(val!=null)
					lstOtpParams.add( TpConstants.OTP_PARAMETERS[i]+"="+URLEncoder.encode(val));
			}			
			String url = TpConstants.SERVER_URL+"ws/plan?"+StringUtils.join(lstOtpParams, "&");
			long start = System.currentTimeMillis();
			//			System.out.println(url);
			String planJsonString = HttpUtils.getHttpResponse(url);
			//			System.out.println(planJsonString);
			long planGenerationTime = System.currentTimeMillis()-start;
			reqMap.put(RequestParam.TIME_TRIP_PLAN, planGenerationTime+"");
			response= JSONUtil.getFullPlanObjFromJson(planJsonString);			
			response.setPlanGenerateTime(planGenerationTime);
			if(response.getPlan()==null){
				logger.debug(loggerName, "No plan found, possoble error:"+response.getError());
				return response;
			}
			TripPlan plan = response.getPlan();
			plan.setDeviceId(deviceId);
			plan.setAppType(NumberUtils.toInt(reqMap.get(TpConstants.APP_TYPE)));
			plan.setCreateTime(System.currentTimeMillis()); 
			List<Itinerary> itineraries = plan.getItineraries();

			String strSavePlan = StringUtils.defaultString(reqMap.get(RequestParam.SAVE_PLAN),"true");
			boolean savePlan = Boolean.parseBoolean(strSavePlan);
			if(savePlan){
				plan.setItineraries(null);//set null as we don't want embedded reference in mongo db.
				persistenceService.addObject(TpConstants.MONGO_TABLES.plan.name(), plan);
				if (itineraries == null || itineraries.size()==0) {
					logger.debug(loggerName, "No itineraries to save for plan Id: "+plan.getId()); 
					return response;
				}
				plan.setItineraries(itineraries);//reset for response purpose
				for (Itinerary itin: itineraries) {
					List<Leg> legs = itin.getLegs();
					itin.setLegs(null);//set null as we don't want embedded reference in mongo db.
					itin.setPlanId(plan.getId());
					persistenceService.addObject(TpConstants.MONGO_TABLES.itinerary.name(), itin);
					if (legs==null || legs.size()==0) {
						logger.debug(loggerName, "No legs to save for itinerary id: "+itin.getId());
						continue;
					}
					for (Leg leg: legs) {
						leg.setItinId(itin.getId());
					}
					persistenceService.addObjects(TpConstants.MONGO_TABLES.leg.name(), legs); 
					itin.setLegs(legs); //reset for response purpose 
				}
			}
		}  catch (DBException e) {
			logger.error(loggerName, "Error while saving trip in DB: "+e.getMessage());
		}catch (Exception e) {
			logger.error(loggerName, e);
		}
		return response;	
	}

	/**
	 * Gets the next legs.
	 *
	 * @param lstLegs the lst legs
	 * @return the next legs
	 */
	public TripResponse getNextLegs(List<Leg> lstLegs) {
		TripResponse tripResponse = new TripResponse();
		long time = System.currentTimeMillis();
		List<Itinerary> lstResLegs = new ArrayList<Itinerary>();
		for (Leg leg : lstLegs) {
			Itinerary itinerary =  getNextLegs(leg);
			//printLeg(leg,itinerary);
			lstResLegs.add(itinerary);
		}		
		tripResponse.setLstItineraries(lstResLegs);
		tripResponse.setPlanGenerateTime(System.currentTimeMillis()-time);		
		return tripResponse;
	}

	private void printLeg(Leg leg, Itinerary itinerary) {		 
		System.out.println("===========================================================");
		System.out.println("Request Time: "+new Date(leg.getStartTime())+", from:"+leg.getFrom().getName()+", to:"+leg.getTo().getName());
		System.out.println("size to Iterate: "+itinerary.getLegs().size());
		for (Leg l : itinerary.getLegs()) {
			System.out.println("      Time: "+new Date(l.getStartTime())+", Trip:"+l.getTripId());
		}
		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n\n");

	}

	/**
	 * Gets the next leg.
	 *
	 * @param leg the leg
	 * @return the next leg
	 */
	public Itinerary getNextLegs(Leg leg) {		
		try {
			String baseUrl = TpConstants.SERVER_URL+"ws/nimbler/nextTripFromStop";
			List<String> lstOtpParams = new ArrayList<String>();
			lstOtpParams.add("agency="+URLEncoder.encode(leg.getAgencyId()));
			lstOtpParams.add("fromStopId="+URLEncoder.encode(leg.getFrom().getStopId().getId()));
			lstOtpParams.add("toStopId="+URLEncoder.encode(leg.getTo().getStopId().getId()));
			lstOtpParams.add("startTime="+leg.getStartTime());
			Long size = leg.getSize();
			if(size==null || size == 0L)
				size = 15L;			
			lstOtpParams.add("size="+ size);
			if(leg.getEndTime()!=null)
				lstOtpParams.add("endTime="+leg.getEndTime());
			String url = baseUrl+"?"+StringUtils.join(lstOtpParams, "&");			
			String planJsonString = HttpUtils.getHttpResponse(url);
			Itinerary itinerary = (Itinerary) JSONUtil.getObjectFromJson(planJsonString,Itinerary.class);	
			trimData(itinerary);
			itinerary.setId(leg.getId());
			return itinerary;
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
		return null;
	}

	public StopTimeType getClosestTime(Leg leg,long time) {		
		try {
			String baseUrl = TpConstants.SERVER_URL+"ws/nimbler/closestSchedule";
			//			String baseUrl = "http://localhost:7070/opentripplanner-api-webapp/ws/nimbler/closestSchedule";
			List<String> lstOtpParams = new ArrayList<String>();
			lstOtpParams.add("agency="+URLEncoder.encode(leg.getAgencyId()));
			lstOtpParams.add("fromStopId="+URLEncoder.encode(leg.getFrom().getStopId().getId()));
			lstOtpParams.add("toStopId="+URLEncoder.encode(leg.getTo().getStopId().getId()));
			lstOtpParams.add("startTime="+leg.getStartTime());
			lstOtpParams.add("time="+time);			
			lstOtpParams.add("endTime="+leg.getEndTime());
			String url = baseUrl+"?"+StringUtils.join(lstOtpParams, "&");			
			String planJsonString = HttpUtils.getHttpResponse(url);			
			StopTimeType stopTimeList = (StopTimeType) JSONUtil.getObjectFromJson(planJsonString,StopTimeType.class);
			return stopTimeList;
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
		return null;
	}
	private void trimData(Itinerary itinerary) {
		itinerary.setDuration(null);
		itinerary.setElevationGained(null);
		itinerary.setElevationLost(null);		
		itinerary.setWalkTime(null);		
		itinerary.setTransitTime(null);		
		itinerary.setWaitingTime(null);		
		itinerary.setWalkDistance(null);		
		itinerary.setTransfers(null);		
		for (Leg leg : itinerary.getLegs()) {
			leg.setAgencyName(null);
			leg.setAgencyUrl(null);
			leg.setAgencyId(null);
			leg.getFrom().setLat(null);
			leg.getFrom().setLon(null);
			leg.getFrom().setName(null);
			leg.getFrom().setZoneId(null);			

			leg.getTo().setLat(null);
			leg.getTo().setLon(null);
			leg.getTo().setName(null);
			leg.getTo().setZoneId(null);
			leg.setAgencyTimeZoneOffset(null);
			leg.setRoute(null);
		}
	}

	/**
	 * Gets the next leg.
	 *
	 * @param leg the leg
	 * @return the next leg
	 * @deprecated
	 */
	private Leg getNextLegFromPlan(Leg leg) {		
		try {
			Date date = new Date(leg.getStartTime()); 
			List<String> lstOtpParams = new ArrayList<String>();
			String from = leg.getFrom().getLat()+","+leg.getFrom().getLon();
			lstOtpParams.add("fromPlace="+URLEncoder.encode(from));
			String to = leg.getTo().getLat()+","+leg.getFrom().getLon();
			lstOtpParams.add("toPlace="+URLEncoder.encode(to));
			lstOtpParams.add("arriveBy=false");
			lstOtpParams.add("transferPenalty=2000");
			lstOtpParams.add("date="+URLEncoder.encode(DateFormatUtils.format(date, OTP_DATE_FORMAT)));
			lstOtpParams.add("time="+URLEncoder.encode(DateFormatUtils.format(date, OTP_TIME_FORMAT)));
			String url = TpConstants.SERVER_URL+"ws/plan?"+StringUtils.join(lstOtpParams, "&");
			long start = System.currentTimeMillis();
			//System.out.println(url);
			String planJsonString = HttpUtils.getHttpResponse(url);
			//System.out.println(planJsonString);
			long planGenerationTime = System.currentTimeMillis()-start;

			TripResponse response = JSONUtil.getFullPlanObjFromJson(planJsonString);			
			response.setPlanGenerateTime(planGenerationTime);
			if(response.getPlan()==null){
				return null;
			}
			List<Itinerary> itis = response.getPlan().getItineraries();
			if(!ComUtils.isEmptyList(itis)){
				for (Itinerary itinerary : itis) {
					Leg l=  getValidLeg(itinerary,leg.getFrom().getStopId(),leg.getTo().getStopId());
					if(l!=null)
						return l;

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Leg getValidLeg(Itinerary itinerary, AgencyAndId from,AgencyAndId to) {
		if(itinerary!=null && itinerary.getLegs()!=null && itinerary.getLegs().size()>0){
			for (Leg leg : itinerary.getLegs()) {
				if("WALK".equalsIgnoreCase(leg.getMode()))
					continue;
				if(from.equals(leg.getFrom().getStopId()) && to.equals(leg.getTo().getStopId())){
					return leg;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param deviceId
	 * @return
	 */
	public TripPlan getLatestPlanOfDevice(String deviceId) {
		try {
			List list = persistenceService.find(TpConstants.MONGO_TABLES.plan.name(), "deviceId", deviceId, "createTime", Order.DESCENDING, 1, TripPlan.class);
			if (list!=null && list.size()>0)
				return (TripPlan)list.get(0);
			else
				logger.warn(loggerName, "Trip plan for DeviceID: "+deviceId+" not found in DB."); 
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage()); 
		}
		return null;
	}
	/**
	 * 
	 * @param plan
	 * @return
	 */
	public TripPlan getResponsePlanFromFullPlan (TripPlan plan){		
		TripPlan responsePlan = new TripPlan();
		responsePlan.setId(plan.getId());
		if (plan.getItineraries()==null)
			return responsePlan;	

		List<Itinerary> newItinis = new ArrayList<Itinerary>();
		for (Itinerary itin: plan.getItineraries()) {
			Itinerary newItin = new Itinerary();
			newItin.setId(itin.getId());
			newItin.setPlanId(itin.getPlanId());
			newItin.setDuration(itin.getDuration());
			newItin.setStartTime(itin.getStartTime()); 

			if (itin.getLegs() == null || itin.getLegs().size()==0)
				continue;

			List<Leg> legs = itin.getLegs();
			List<Leg> newLegs  = new ArrayList<Leg>();
			for (Leg leg: legs) {
				Leg newLeg = new Leg();
				newLeg.setId(leg.getId());
				newLeg.setItinId(leg.getItinId());
				newLeg.setDistance(leg.getDistance());
				newLeg.setStartTime(leg.getStartTime());
				newLegs.add(newLeg); 
			}
			newItin.setLegs(newLegs);
			newItinis.add(newItin);
		}
		responsePlan.setItineraries(newItinis); 
		return responsePlan; 
	}
	/**
	 * 
	 * @param planId
	 * @return
	 */
	public TripPlan getFullPlanFromDB(String planId) {
		try {
			TripPlan plan = getPlanFromDB(planId);
			if (plan==null)
				return null;
			List resultSetItin = persistenceService.find(TpConstants.MONGO_TABLES.itinerary.name(), "planId", planId, Itinerary.class);
			if (resultSetItin!=null && resultSetItin.size()>0) {
				List<Itinerary> itineraries = new ArrayList<Itinerary>(resultSetItin);
				for (Itinerary itin: itineraries) {
					List resultSetLegs = persistenceService.find(TpConstants.MONGO_TABLES.leg.name(), "itinId", itin.getId(), Leg.class);
					if (resultSetLegs!=null && resultSetLegs.size()>0) {
						itin.setLegs(new ArrayList<Leg>(resultSetLegs)); 
					}
				}
				plan.setItineraries(itineraries); 
			}
			return plan;
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage()); 
		}
		return null;
	}
	/**
	 * 
	 * @param legId
	 * @return
	 */
	public TripPlan getFullPlanOfLeg(String legId) {
		try {
			Leg leg = (Leg) persistenceService.findOne(MONGO_TABLES.leg.name(), "id", legId, Leg.class);
			if (leg == null)
				return null;
			return getFullPlanOfItinerary(leg.getItinId());
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage()); 
		}
		return null;
	}
	/**
	 * #############################################################################################
	 * 							HELPER METHODS
	 * #############################################################################################
	 */
	/**
	 * 
	 * @param planId
	 * @return
	 */
	private TripPlan getPlanFromDB(String planId) throws DBException {
		TripPlan plan = (TripPlan) persistenceService.findOne(MONGO_TABLES.plan.name(), "id", planId, TripPlan.class);
		return plan;
	}
	/**
	 * 
	 * @param itinId
	 * @return
	 */
	public TripPlan getFullPlanOfItinerary(String itinId) throws DBException {
		Itinerary itinerary = (Itinerary)persistenceService.findOne(TpConstants.MONGO_TABLES.itinerary.name(), "id", itinId, Itinerary.class);
		if (itinerary==null)
			return null;
		TripPlan plan = getPlanFromDB(itinerary.getPlanId());
		if (plan==null)
			return null;
		List<Itinerary> itineraries = getFullItinerariesOfPlan(itinerary.getPlanId()); 
		plan.setItineraries(itineraries);
		return plan;
	}
	/**
	 * 
	 * @param planId
	 * @return
	 */
	private List<Itinerary> getFullItinerariesOfPlan(String planId) throws DBException {
		List resultSet = persistenceService.find(TpConstants.MONGO_TABLES.itinerary.name(), "planId", planId, Itinerary.class);
		if (resultSet!=null && resultSet.size()>0) {
			List<Itinerary> itineraries = new ArrayList<Itinerary>(resultSet);
			for (Itinerary itin: itineraries) {
				List resultSetLegs = persistenceService.find(TpConstants.MONGO_TABLES.leg.name(), "itinId", itin.getId(), Leg.class);
				if (resultSetLegs!=null && resultSetLegs.size()>0) {
					itin.setLegs(new ArrayList<Leg>(resultSetLegs)); 
				}
			}
			return itineraries;
		}
		return null;
	}

	/**
	 * Gets the full itineraries by ids.
	 *
	 * @param itineraryIds the itinerary ids
	 * @return the full itineraries by ids
	 * @author nirmal
	 * @since 30Aug2012
	 * @throws DBException the dB exception
	 */
	public List<Itinerary> getFullItinerariesByIds(String[] itineraryIds) throws DBException {
		List resultSet = persistenceService.findByIn(MONGO_TABLES.itinerary.name(), "id", itineraryIds, Itinerary.class);
		if (resultSet!=null && resultSet.size()>0) {
			List<Itinerary> itineraries = new ArrayList<Itinerary>(resultSet);
			Map<String, Itinerary> map = new HashMap<String, Itinerary>();
			for (Itinerary itin: itineraries) 
				map.put(itin.getId(), itin);
					List<Leg> resultSetLegs = persistenceService.findByIn(MONGO_TABLES.leg.name(), "itinId", itineraryIds, Leg.class);
					if (resultSetLegs!=null && resultSetLegs.size()>0) {
						for (Leg leg : resultSetLegs) {
							Itinerary it = map.get(leg.getItinId());
							if(it!=null)
								it.addLeg(leg); 
						}
					}
					return itineraries;
		}
		return null;
	}
	public LoggingService getLogger() {
		return logger;
	}
	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}
	public PersistenceService getPersistenceService() {
		return persistenceService;
	}
	public void setPersistenceService(PersistenceService persistenceService) {
		this.persistenceService = persistenceService;
	}
	public String getLoggerName() {
		return loggerName;
	}
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}


}