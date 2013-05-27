/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.rest;

import java.util.Date;
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

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.dataobject.Itinerary;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.dataobject.TripPlan;
import com.nimbler.tp.dataobject.TripResponse;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.TpEventLoggingService;
import com.nimbler.tp.service.TpPlanService;
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

	@Autowired
	private LoggingService logger;
	private String loggerName;

	@Autowired
	TpPlanService planService;

	@Autowired
	TpEventLoggingService eventLogService;

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


			TripPlan plan = planService.savePlan(deviceid, planJsonString);			
			eventLogService.savePlan(reqMap,plan!=null?plan.getId():null);
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
			TripResponse tripResponse = planService.genearetePlan(reqParams);
			//			System.out.println(tripResponse);
			String planId = null;
			if (tripResponse==null)
				throw new TpException("TripResponse is null");
			if (tripResponse.getPlan()!=null)
				planId = tripResponse.getPlan().getId();


			reqParams.put(RequestParam.TIME_TRIP_PLAN, tripResponse.getPlanGenerateTime()+"");
			eventLogService.savePlan(reqParams,planId);
			//			printItineraty(response.getPlan());
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
		//		getDummy(response);
		String res = JSONUtil.getResponseJSON(response);
		//		System.out.println(res);
		//		System.out.println("============================================================");
		return res;
	}


	private void getDummy(TPResponse response) {
		String res = "{\r\n" + 
				"	\"id\" : \"5190869ee4b01c8a006e6b66\",\r\n" + 
				"	\"deviceId\" : \"1E435858-73CD-47D1-97AD-5B8B7F9B4BC8\",\r\n" + 
				"	\"createTime\" : 1368426142419,\r\n" + 
				"	\"date\" : 1368426000000,\r\n" + 
				"	\"from\" : {\r\n" + 
				"		\"name\" : \"9th Street\",\r\n" + 
				"		\"lon\" : -122.41283235133382,\r\n" + 
				"		\"lat\" : 37.774736545363886\r\n" + 
				"	},\r\n" + 
				"	\"to\" : {\r\n" + 
				"		\"name\" : \"corner of Parnassus Avenue and Woodland Avenue\",\r\n" + 
				"		\"lon\" : -122.453503,\r\n" + 
				"		\"lat\" : 37.764402\r\n" + 
				"	},\r\n" + 
				"	\"itineraries\" : [{\r\n" + 
				"			\"id\" : \"5190869ee4b01c8a006e6b67\",\r\n" + 
				"			\"planId\" : \"5190869ee4b01c8a006e6b66\",\r\n" + 
				"			\"duration\" : 966000,\r\n" + 
				"			\"startTime\" : 1368426000000,\r\n" + 
				"			\"endTime\" : 1368426966000,\r\n" + 
				"			\"walkTime\" : 966,\r\n" + 
				"			\"transitTime\" : 0,\r\n" + 
				"			\"waitingTime\" : 0,\r\n" + 
				"			\"walkDistance\" : 4485.610596622381,\r\n" + 
				"			\"elevationLost\" : 0.0,\r\n" + 
				"			\"elevationGained\" : 0.0,\r\n" + 
				"			\"transfers\" : -1,\r\n" + 
				"			\"legs\" : [{\r\n" + 
				"					\"id\" : \"5190869ee4b01c8a006e6b68\",\r\n" + 
				"					\"itinId\" : \"5190869ee4b01c8a006e6b67\",\r\n" + 
				"					\"startTime\" : 1368426000000,\r\n" + 
				"					\"endTime\" : 1368426966000,\r\n" + 
				"					\"distance\" : 4485.610596622381,\r\n" + 
				"					\"mode\" : \"BICYCLE\",\r\n" + 
				"					\"route\" : \"\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : 0,\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"9th Street\",\r\n" + 
				"						\"lon\" : -122.41283235133382,\r\n" + 
				"						\"lat\" : 37.774736545363886\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"corner of Parnassus Avenue and Woodland Avenue\",\r\n" + 
				"						\"lon\" : -122.453503,\r\n" + 
				"						\"lat\" : 37.764402,\r\n" + 
				"						\"arrival\" : 1368426966000,\r\n" + 
				"						\"departure\" : 1368426966000\r\n" + 
				"					},\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"a{peFfwcjVs@~@bAvAZ`@Zd@`ApA|DpFa@h@u@bAiA|Ae@p@e@j@GJe@n@eCfDq@~@EDGHQTn@z@RZ@@@@DFPTtAjBt@|ANVNVLPHJpAfBLPf@r@^d@RXDl@BTFx@\\\\tE@P@RBTBt@XjGF|@`@hGh@hI`@vGDl@F|@`@lGh@pH@TBVf@lHd@rGDn@@H@HDt@`@jGh@hIB`@b@hHX|DHhABh@BRxDc@fBUxCx@tA^`@JJ@RnCBZ@R??PnCPpC@R@N?@Dh@LhBPhC?BBTXrE\\\\pEvDc@n@IjC]F|@XjE@V@P`@zF@PBh@N`C\",\r\n" + 
				"						\"length\" : 102\r\n" + 
				"					},\r\n" + 
				"					\"steps\" : [{\r\n" + 
				"							\"distance\" : 40.62114672911859,\r\n" + 
				"							\"absoluteDirection\" : \"NORTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -122.41283235133382,\r\n" + 
				"							\"lat\" : 37.774736545363886,\r\n" + 
				"							\"elevation\" : \"\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 299.7972116684516,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -122.4131572,\r\n" + 
				"							\"lat\" : 37.7749964,\r\n" + 
				"							\"elevation\" : \"\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 391.5006357757967,\r\n" + 
				"							\"relativeDirection\" : \"RIGHT\",\r\n" + 
				"							\"absoluteDirection\" : \"NORTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -122.41557850000001,\r\n" + 
				"							\"lat\" : 37.773097400000005,\r\n" + 
				"							\"elevation\" : \"\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 387.72593561779905,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -122.41871920000001,\r\n" + 
				"							\"lat\" : 37.7755941,\r\n" + 
				"							\"elevation\" : \"\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 1935.224595429621,\r\n" + 
				"							\"relativeDirection\" : \"SLIGHTLY_RIGHT\",\r\n" + 
				"							\"absoluteDirection\" : \"WEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -122.42191430000001,\r\n" + 
				"							\"lat\" : 37.7731968,\r\n" + 
				"							\"elevation\" : \"\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 163.54114714622017,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTH\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -122.44364900000001,\r\n" + 
				"							\"lat\" : 37.770426,\r\n" + 
				"							\"elevation\" : \"\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 165.14302484655968,\r\n" + 
				"							\"relativeDirection\" : \"SLIGHTLY_RIGHT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTH\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -122.44335930000001,\r\n" + 
				"							\"lat\" : 37.768973200000005,\r\n" + 
				"							\"elevation\" : \"\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 558.8524856538053,\r\n" + 
				"							\"relativeDirection\" : \"RIGHT\",\r\n" + 
				"							\"absoluteDirection\" : \"WEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -122.44387270000001,\r\n" + 
				"							\"lat\" : 37.7675451,\r\n" + 
				"							\"elevation\" : \"\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 209.77678780819272,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTH\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -122.450146,\r\n" + 
				"							\"lat\" : 37.766729000000005,\r\n" + 
				"							\"elevation\" : \"\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 333.42762594681545,\r\n" + 
				"							\"relativeDirection\" : \"RIGHT\",\r\n" + 
				"							\"absoluteDirection\" : \"WEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -122.44977,\r\n" + 
				"							\"lat\" : 37.764866000000005,\r\n" + 
				"							\"elevation\" : \"\"\r\n" + 
				"						}\r\n" + 
				"					],\r\n" + 
				"					\"duration\" : 966000\r\n" + 
				"				}\r\n" + 
				"			],\r\n" + 
				"			\"tooSloped\" : false\r\n" + 
				"		}\r\n" + 
				"	],\r\n" + 
				"	\"appType\" : 4\r\n" + 
				"\r\n" + 
				"}\r\n" + 
				"";

		TripPlan trip = (TripPlan) JSONUtil.getObjectFromJson(res, TripPlan.class);
		response.setPlan(trip);
	}

	private void printItineraty(TripPlan plan) {		 
		try {
			System.out.println("===========================================================");			
			System.out.println("Plan("+plan.getItineraries().size()+"): "+new Date(plan.getDate())+", from:"+plan.getFrom().getName()+", to:"+plan.getTo().getName());
			if(plan.getItineraries()!=null && plan.getItineraries().size()>0){
				for (Itinerary itinerary : plan.getItineraries()) {
					System.out.println("------------------------------------------------");
					System.out.println("   Itinerary("+itinerary.getLegs().size()+"): "+DateFormatUtils.format(itinerary.getEndTime()-itinerary.getStartTime(), "HH:mm:ss"));
					for (Leg l : itinerary.getLegs()) {
						System.out.println("      Time: "+new Date(l.getStartTime())+", Trip:"+l.getTripId()+", Route:"+l.getRoute());
					}
				}
			}
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n\n");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	@POST
	@Path("/nextlegs/")
	@Produces({MediaType.APPLICATION_JSON})
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String getNextLegs(@Context HttpServletRequest httpRequest){
		TripResponse tripResponse = new TripResponse();
		try {
			Map<String,String> reqParam = ComUtils.parseMultipartRequest(httpRequest);
			String strLegs = reqParam.get(RequestParam.LEGS);	
			//			System.out.println(strLegs);
			List lstLegs =  JSONUtil.getLegsJson(strLegs);
			if(ComUtils.isEmptyList(lstLegs))
				throw new TpException(TP_CODES.INVALID_REQUEST.getCode(),"Error while getting JSON String from plan object.");

			long start = System.currentTimeMillis();
			tripResponse = planService.getNextLegs(lstLegs);			
			long end = System.currentTimeMillis();
			logger.debug(loggerName,"Operation took " + (end - start) + " msec");
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
		String res = JSONUtil.getResponseJSON(tripResponse);	
		return res; 
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
	public TpPlanService getPlanService() {
		return planService;
	}
	public void setPlanService(TpPlanService planService) {
		this.planService = planService;
	}

}