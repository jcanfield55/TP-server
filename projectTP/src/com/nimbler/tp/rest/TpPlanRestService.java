/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd.
 * All rights reserved.
 *
 */
package com.nimbler.tp.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.dataobject.Itinerary;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.dataobject.TripPlan;
import com.nimbler.tp.dataobject.TripResponse;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.TpEventLoggingService;
import com.nimbler.tp.service.TpPlanService;
import com.nimbler.tp.service.livefeeds.WmataApiImpl;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.ResponseUtil;
import com.nimbler.tp.util.TpException;

/**
 * The TripPlan WebService for saving trip.
 *
 * @author nIKUNJ
 */
@Path("/plan/")
public class TpPlanRestService {

	private LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private String loggerName;

	/**
	 * 
	 * @param deviceid
	 * @param planJsonString
	 * @return
	 */
	@POST
	@Path("/new/")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.TEXT_PLAIN})
	@Deprecated
	public String savePlan(@Context HttpServletRequest request) {
		Map<String,String> reqMap = new HashMap<String, String>();
		TPResponse response = null;
		try {
			ComUtils.parseMultipartRequest(request, reqMap, null, null);
			String deviceid =reqMap.get(RequestParam.DEVICE_ID) ;
			String planJsonString = reqMap.get(RequestParam.PLAN_JSON);

			TpPlanService service = BeanUtil.getPlanService();
			TripPlan plan = service.savePlan(deviceid, planJsonString);
			TpEventLoggingService tpLoggingService = BeanUtil.getTpEventLoggingService();
			tpLoggingService.savePlan(reqMap,plan!=null?plan.getId():null);
			response = ResponseUtil.createResponse(plan!=null?TP_CODES.SUCESS:TP_CODES.FAIL);
		} catch (TpException e) {
			logger.error(loggerName, e.getErrMsg());
			response = ResponseUtil.createResponse(TP_CODES.FAIL);
		} catch (Exception e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		return JSONUtil.getResponseJSON(response);
	}
	@GET
	@Path("/get/")
	@Deprecated
	public String getPlanDetails(@QueryParam(RequestParam.DEVICE_ID) String deviceId) {
		TPResponse response = null;
		try{
			TpPlanService planService = BeanUtil.getPlanService();
			TripPlan plan = planService.getLatestPlanOfDevice(deviceId);
			if (plan==null)
				throw new TpException(TP_CODES.DATA_NOT_EXIST);
			plan = planService.getFullPlanFromDB(plan.getId());
			plan = planService.getResponsePlanFromFullPlan(plan);
			return JSONUtil.getJsonFromObj(new TripResponse(plan));
		}catch (TpException e) {
			logger.debug(loggerName, e.getErrMsg());
			response = ResponseUtil.createResponse(e);
		} catch (Exception e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		return JSONUtil.getResponseJSON(response);
	}

	/**
	 * Generate plan.
	 *
	 * @param httpRequest the http request
	 * @return the tP response
	 * @throws TpException
	 */
	@POST
	@Path("/generate/")
	@Produces({MediaType.APPLICATION_JSON})
	public String generatePlan(@Context HttpServletRequest httpRequest){
		TPResponse response = ResponseUtil.createResponse(TP_CODES.FAIL);
		String reqID = "";
		//		long time = System.currentTimeMillis();
		try {
			Map<String,String> reqParams  = ComUtils.parseMultipartRequest(httpRequest);
			if(reqParams==null || reqParams.size()==0)
				throw new TpException(TP_CODES.INVALID_REQUEST);
			//			System.out.println(">============================================================");
			//			System.out.println(reqParams.get("time")+"::"+reqParams.get("date"));

			reqID = reqParams.get(RequestParam.REQ_ID);
			TpPlanService service = BeanUtil.getPlanService();
			TripResponse tripResponse = service.genearetePlan(reqParams);
			//System.out.println(tripResponse);
			String planId = null;
			if (tripResponse==null)
				throw new TpException("TripResponse is null");
			if (tripResponse.getPlan()!=null)
				planId = tripResponse.getPlan().getId();
			//			printWMataDetails(tripResponse.getPlan());

			TpEventLoggingService tpLoggingService = BeanUtil.getTpEventLoggingService();
			reqParams.put(RequestParam.TIME_TRIP_PLAN, tripResponse.getPlanGenerateTime()+"");
			tpLoggingService.savePlan(reqParams,planId);

			response = new TPResponse(TP_CODES.SUCESS);
			response.setPlan(tripResponse.getPlan());
			response.setError(tripResponse.getError());
			response.setPlanGenerateTime(tripResponse.getPlanGenerateTime());
			//			long total = System.currentTimeMillis() - time;
			//			System.out.println("Plan + overhead : "+tripResponse.getPlanGenerateTime()+", "+(total-tripResponse.getPlanGenerateTime()));
		} catch (TpException e) {
			logger.info(loggerName, e.getErrMsg());
			response =ResponseUtil.createResponse(e);
		}catch (Exception e) {
			logger.error(loggerName, e);
			response =ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}finally{
			response.setReqId(reqID);
		}
		String res = JSONUtil.getResponseJSON(response);
		System.out.println(res);
		//		System.out.println("============================================================");
		return res;
	}

	private void printWMataDetails(TripPlan plan) {
		try {
			List<Itinerary> lstItineraries = plan.getItineraries();
			for (Itinerary itinerary : lstItineraries) {
				System.out.println("Itineraty ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				List<Leg> legs = itinerary.getLegs();
				for (Leg leg : legs) {
					getPrediction(legs);
					if(leg.getFrom()!=null){
						//						System.out.println("     Start Time: "+leg.getStartTime());
						//						System.out.println("     Trip id   : "+leg.getTripId());
						//						System.out.println("     Stop Id   : "+leg.getFrom().getStopId().getId());
						//						System.out.println("     Route     : "+leg.getRoute());
						System.out.println("     From  : "+leg.getFrom().getName());
						System.out.println("     To    : "+leg.getTo().getName());
						System.out.println("     Head  : "+leg.getHeadsign());
					}
					//					"1305","Red","35229",1358771160000l
					String arg = "";
					if(StringUtils.equalsIgnoreCase("SUBWAY", leg.getMode())){
						arg=leg.getFrom().getStopId().getId()+","+leg.getRoute()+","+leg.getTripId()+","+leg.getStartTime();
					}
					if(StringUtils.equalsIgnoreCase("BUS", leg.getMode())){
						String ar="BUS:------>  "+leg.getFrom().getStopId().getId()+","+leg.getTripId()+","+leg.getRoute()+","+leg.getStartTime();
						System.out.println(ar);
					}
					System.out.println("---------------------"+arg);
				}
				System.out.println("</Itineraty.............................................................\n");
			}
		} catch (Exception e) {
			System.out.println("can't print:");
			e.printStackTrace();
		}

	}
	private void getPrediction(final List<Leg> legs) {
		Thread thread =  new Thread(){
			@Override
			public void run() {
				for (Leg leg : legs) {
					try {
						WmataApiImpl apiImpl = BeanUtil.getWMATAApiImpl();
						apiImpl.getLiveFeeds(leg);
					} catch (FeedsNotFoundException e) {
						System.out.println(e.getMessage());
					}
				}
			}
		};
		//		thread.start();

	}
	/**
	 * 
	 * @return
	 */
	public String getLoggerName() {
		return loggerName;
	}
	/**
	 * 
	 * @param loggerName
	 */
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
}