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
import javax.ws.rs.core.Response;

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

	/**
	 * Cleanup extra parameter.
	 *
	 * @param plan the plan
	 */
	private void cleanupExtraParameter(TripPlan plan) {
		if(plan==null)
			return;
		plan.setRequestUrl(null);
		//plan.setRequestParameters(null);
		plan.setPlanUrlParams(null);
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
			TripPlan plan = tripResponse.getPlan();
			if (plan!=null)
				planId = plan.getId();

			reqParams.put(RequestParam.TIME_TRIP_PLAN, tripResponse.getPlanGenerateTime()+"");
			eventLogService.savePlan(reqParams,planId);
			response = new TPResponse(TP_CODES.SUCESS);
			response.setPlan(tripResponse.getPlan());
			response.setError(tripResponse.getError());
			response.setPlanGenerateTime(tripResponse.getPlanGenerateTime());
			cleanupExtraParameter(plan);
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
				"	\"id\" : \"519cf893e4b054c0551389a6\",\r\n" + 
				"	\"deviceId\" : \"70F3FA48-4C72-4166-B3BB-4A025705E6E2\",\r\n" + 
				"	\"createTime\" : 1369241747485,\r\n" + 
				"	\"date\" : 1369243800000,\r\n" + 
				"	\"from\" : {\r\n" + 
				"		\"name\" : \"service road\",\r\n" + 
				"		\"lon\" : -121.83075980000001,\r\n" + 
				"		\"lat\" : 37.2481875\r\n" + 
				"	},\r\n" + 
				"	\"to\" : {\r\n" + 
				"		\"name\" : \"Belick Street\",\r\n" + 
				"		\"lon\" : -121.94220133265001,\r\n" + 
				"		\"lat\" : 37.38027735624535\r\n" + 
				"	},\r\n" + 
				"	\"itineraries\" : [{\r\n" + 
				"			\"id\" : \"519cf893e4b054c0551389a7\",\r\n" + 
				"			\"planId\" : \"519cf893e4b054c0551389a6\",\r\n" + 
				"			\"duration\" : 4816000,\r\n" + 
				"			\"startTime\" : 1369244490000,\r\n" + 
				"			\"endTime\" : 1369249306000,\r\n" + 
				"			\"walkTime\" : 1768,\r\n" + 
				"			\"transitTime\" : 2660,\r\n" + 
				"			\"waitingTime\" : 388,\r\n" + 
				"			\"walkDistance\" : 2310.9236523799514,\r\n" + 
				"			\"elevationLost\" : 9.594024825559956,\r\n" + 
				"			\"elevationGained\" : 59.981092139161746,\r\n" + 
				"			\"transfers\" : 1,\r\n" + 
				"			\"legs\" : [{\r\n" + 
				"					\"id\" : \"519cf893e4b054c0551389a8\",\r\n" + 
				"					\"itinId\" : \"519cf893e4b054c0551389a7\",\r\n" + 
				"					\"startTime\" : 1369244580000,\r\n" + 
				"					\"endTime\" : 1369244760000,\r\n" + 
				"					\"distance\" : 2568.8237561340675,\r\n" + 
				"					\"mode\" : \"TRAM\",\r\n" + 
				"					\"route\" : \"901\",\r\n" + 
				"					\"routeId\" : \"901\",\r\n" + 
				"					\"agencyName\" : \"VTA\",\r\n" + 
				"					\"agencyUrl\" : \"http://www.vta.org\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : 0,\r\n" + 
				"					\"headsign\" : \"LRT ALUM ROCK - SANTA TERESA\",\r\n" + 
				"					\"agencyId\" : \"VTA\",\r\n" + 
				"					\"tripId\" : \"1483139\",\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"SNELL STATION (1)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4783\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.830546628,\r\n" + 
				"						\"lat\" : 37.247640794,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"COTTLE STATION (1)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4784\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.802252317,\r\n" + 
				"						\"lat\" : 37.242504791,\r\n" + 
				"						\"arrival\" : 1369244845000,\r\n" + 
				"						\"departure\" : 1369244845000,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"intermediateStops\" : [],\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"w|ibF|_rfVb_@yoD\",\r\n" + 
				"						\"length\" : 2\r\n" + 
				"					},\r\n" + 
				"					\"routeShortName\" : \"901\",\r\n" + 
				"					\"routeLongName\" : \"ALUM ROCK-SANTA TERESA VIA BAYPOINT\",\r\n" + 
				"					\"duration\" : 180000\r\n" + 
				"				}, {\r\n" + 
				"					\"id\" : \"519cf893e4b054c0551389a9\",\r\n" + 
				"					\"itinId\" : \"519cf893e4b054c0551389a7\",\r\n" + 
				"					\"startTime\" : 1369244906000,\r\n" + 
				"					\"endTime\" : 1369244969000,\r\n" + 
				"					\"distance\" : 79.21796275651978,\r\n" + 
				"					\"mode\" : \"WALK\",\r\n" + 
				"					\"route\" : \"\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : -25200000,\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"parking aisle\",\r\n" + 
				"						\"lon\" : -121.80235261131709,\r\n" + 
				"						\"lat\" : 37.24209715260431\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"COTTLE STATION (0)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4737\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.803159131,\r\n" + 
				"						\"lat\" : 37.242731596,\r\n" + 
				"						\"arrival\" : 1369244970000,\r\n" + 
				"						\"departure\" : 1369244970000,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"azhbFvolfVADMfBEf@CX\",\r\n" + 
				"						\"length\" : 5\r\n" + 
				"					},\r\n" + 
				"					\"steps\" : [{\r\n" + 
				"							\"distance\" : 79.21796275651978,\r\n" + 
				"							\"absoluteDirection\" : \"WEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.80235261131709,\r\n" + 
				"							\"lat\" : 37.24209715260431,\r\n" + 
				"							\"elevation\" : \"0,26.3,10,26.6,20,26.8,30,27.1,40,27.4,50,27.7,50,27.7,60,28.3,67,28.7,67,28.7,79,29.0\"\r\n" + 
				"						}\r\n" + 
				"					],\r\n" + 
				"					\"duration\" : 63000\r\n" + 
				"				}, {\r\n" + 
				"					\"id\" : \"519cf893e4b054c0551389aa\",\r\n" + 
				"					\"itinId\" : \"519cf893e4b054c0551389a7\",\r\n" + 
				"					\"startTime\" : 1369245060000,\r\n" + 
				"					\"endTime\" : 1369247540000,\r\n" + 
				"					\"distance\" : 21059.777048787077,\r\n" + 
				"					\"mode\" : \"TRAM\",\r\n" + 
				"					\"route\" : \"901\",\r\n" + 
				"					\"routeId\" : \"901\",\r\n" + 
				"					\"agencyName\" : \"VTA\",\r\n" + 
				"					\"agencyUrl\" : \"http://www.vta.org\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : 0,\r\n" + 
				"					\"headsign\" : \"LRT SANTA TERESA - ALUM ROCK\",\r\n" + 
				"					\"agencyId\" : \"VTA\",\r\n" + 
				"					\"tripId\" : \"1483066\",\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"COTTLE STATION (0)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4737\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.803159131,\r\n" + 
				"						\"lat\" : 37.242731596,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"COMPONENT STATION (0)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4755\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.925777086,\r\n" + 
				"						\"lat\" : 37.383483389,\r\n" + 
				"						\"arrival\" : 1369247540000,\r\n" + 
				"						\"departure\" : 1369247540000,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"intermediateStops\" : [{\r\n" + 
				"							\"name\" : \"SNELL STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4738\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.831338555,\r\n" + 
				"							\"lat\" : 37.248054577,\r\n" + 
				"							\"arrival\" : 1369245180000,\r\n" + 
				"							\"departure\" : 1369245180000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"BLOSSOM HILL STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4739\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.841811246,\r\n" + 
				"							\"lat\" : 37.253149716,\r\n" + 
				"							\"arrival\" : 1369245330000,\r\n" + 
				"							\"departure\" : 1369245330000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"OHLONE-CHYNOWETH STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4731\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.85966629,\r\n" + 
				"							\"lat\" : 37.257992959,\r\n" + 
				"							\"arrival\" : 1369245480000,\r\n" + 
				"							\"departure\" : 1369245480000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"BRANHAM STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4740\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.859381404,\r\n" + 
				"							\"lat\" : 37.267295633,\r\n" + 
				"							\"arrival\" : 1369245570000,\r\n" + 
				"							\"departure\" : 1369245570000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CAPITOL STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4741\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.863268615,\r\n" + 
				"							\"lat\" : 37.275293045,\r\n" + 
				"							\"arrival\" : 1369245660000,\r\n" + 
				"							\"departure\" : 1369245660000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CURTNER STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4742\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.872621797,\r\n" + 
				"							\"lat\" : 37.29375248,\r\n" + 
				"							\"arrival\" : 1369245810000,\r\n" + 
				"							\"departure\" : 1369245810000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"TAMIEN STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4743\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.88480874,\r\n" + 
				"							\"lat\" : 37.311927466,\r\n" + 
				"							\"arrival\" : 1369245960000,\r\n" + 
				"							\"departure\" : 1369245960000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"VIRGINIA STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4744\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.89010437,\r\n" + 
				"							\"lat\" : 37.31982806,\r\n" + 
				"							\"arrival\" : 1369246080000,\r\n" + 
				"							\"departure\" : 1369246080000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CHILDRENS DISCOVERY MUSEUM STA (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4745\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.893665023,\r\n" + 
				"							\"lat\" : 37.327764631,\r\n" + 
				"							\"arrival\" : 1369246200000,\r\n" + 
				"							\"departure\" : 1369246200000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CONVENTION CENTER STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4746\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.889754324,\r\n" + 
				"							\"lat\" : 37.330167643,\r\n" + 
				"							\"arrival\" : 1369246320000,\r\n" + 
				"							\"departure\" : 1369246320000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"SAN ANTONIO STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4747\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.888036544,\r\n" + 
				"							\"lat\" : 37.332821669,\r\n" + 
				"							\"arrival\" : 1369246470000,\r\n" + 
				"							\"departure\" : 1369246470000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"SANTA CLARA STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4748\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.890306491,\r\n" + 
				"							\"lat\" : 37.336005585,\r\n" + 
				"							\"arrival\" : 1369246620000,\r\n" + 
				"							\"departure\" : 1369246620000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"ST JAMES STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4749\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.89231429,\r\n" + 
				"							\"lat\" : 37.338552513,\r\n" + 
				"							\"arrival\" : 1369246760000,\r\n" + 
				"							\"departure\" : 1369246760000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"JAPANTOWN / AYER STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4750\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.897559175,\r\n" + 
				"							\"lat\" : 37.345556685,\r\n" + 
				"							\"arrival\" : 1369246900000,\r\n" + 
				"							\"departure\" : 1369246900000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CIVIC CENTER STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4751\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.902434282,\r\n" + 
				"							\"lat\" : 37.352015873,\r\n" + 
				"							\"arrival\" : 1369247040000,\r\n" + 
				"							\"departure\" : 1369247040000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"GISH STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4752\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.910160014,\r\n" + 
				"							\"lat\" : 37.362544086,\r\n" + 
				"							\"arrival\" : 1369247190000,\r\n" + 
				"							\"departure\" : 1369247190000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"METRO/AIRPORT STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4753\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.915989884,\r\n" + 
				"							\"lat\" : 37.370363515,\r\n" + 
				"							\"arrival\" : 1369247340000,\r\n" + 
				"							\"departure\" : 1369247340000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"KARINA COURT STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4754\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.92040537,\r\n" + 
				"							\"lat\" : 37.376277678,\r\n" + 
				"							\"arrival\" : 1369247440000,\r\n" + 
				"							\"departure\" : 1369247440000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}\r\n" + 
				"					],\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"a~hbFvtlfVg`@boDy^n`Ai]pnBcy@w@_q@fWkrBny@qpBbkAkp@b`@sp@fU_NmWsOwI{RdM}NpKwj@v_@kg@n]y`Aho@{o@jc@}c@rZal@p`@\",\r\n" + 
				"						\"length\" : 20\r\n" + 
				"					},\r\n" + 
				"					\"routeShortName\" : \"901\",\r\n" + 
				"					\"routeLongName\" : \"ALUM ROCK-SANTA TERESA VIA BAYPOINT\",\r\n" + 
				"					\"duration\" : 2480000\r\n" + 
				"				}, {\r\n" + 
				"					\"id\" : \"519cf893e4b054c0551389ab\",\r\n" + 
				"					\"itinId\" : \"519cf893e4b054c0551389a7\",\r\n" + 
				"					\"startTime\" : 1369247601000,\r\n" + 
				"					\"endTime\" : 1369249288000,\r\n" + 
				"					\"distance\" : 2231.7056896234317,\r\n" + 
				"					\"mode\" : \"WALK\",\r\n" + 
				"					\"route\" : \"\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : -25200000,\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"North 1st Street\",\r\n" + 
				"						\"lon\" : -121.92572135209195,\r\n" + 
				"						\"lat\" : 37.3835248978769\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"Belick Street\",\r\n" + 
				"						\"lon\" : -121.94220133265001,\r\n" + 
				"						\"lat\" : 37.38027735624535,\r\n" + 
				"						\"arrival\" : 1369249306000,\r\n" + 
				"						\"departure\" : 1369249306000\r\n" + 
				"					},\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"_ndcFxrdgVcG~DcFjDYRHXL\\\\bH|Rh@xAJXx@zBdCfHp@lBbArBpIzN~BxD`A~A`BtBaCxCqJlEvBvHxAo@hBw@d@S\",\r\n" + 
				"						\"length\" : 23\r\n" + 
				"					},\r\n" + 
				"					\"steps\" : [{\r\n" + 
				"							\"distance\" : 332.84477800839346,\r\n" + 
				"							\"absoluteDirection\" : \"NORTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.92572135209195,\r\n" + 
				"							\"lat\" : 37.3835248978769,\r\n" + 
				"							\"elevation\" : \"0,-23.2,10,-23.2,20,-23.3,30,-23.2,40,-23.3,50,-23.4,60,-23.4,70,-23.4,80,-23.4,90,-23.5,100,-23.4,110,-23.4,120,-23.4,130,-23.6,140,-23.7,150,-23.6,160,-23.7,170,-23.7,180,-23.8,190,-23.8,200,-23.8,210,-23.8,220,-23.9,230,-23.9,240,-23.9,250,-24.0,260,-24.0,270,-24.1,280,-24.1,290,-24.1,300,-24.0,310,-24.0,316,-24.0,316,-24.0,326,-23.9,333,-23.9\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 28.5056091443819,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.92764620000001,\r\n" + 
				"							\"lat\" : 37.3860977,\r\n" + 
				"							\"elevation\" : \"0,-23.9,10,-23.9,20,-23.9,29,-23.9\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 1251.02803735816,\r\n" + 
				"							\"relativeDirection\" : \"CONTINUE\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.92792940000001,\r\n" + 
				"							\"lat\" : 37.3859749,\r\n" + 
				"							\"elevation\" : \"0,-23.9,10,-24.0,20,-24.1,30,-24.0,40,-24.0,50,-24.0,60,-24.0,70,-24.0,80,-23.9,90,-23.9,100,-23.9,110,-23.9,120,-23.9,130,-23.9,140,-23.9,150,-23.9,160,-23.9,170,-23.9,180,-23.8,190,-23.9,200,-23.9,210,-23.8,220,-23.8,230,-23.8,240,-23.7,250,-23.9,260,-23.8,270,-23.9,280,-23.9,290,-23.9,300,-23.9,310,-24.0,325,-24.0,325,-24.0,335,-24.1,345,-24.1,355,-24.2,365,-24.1,372,-24.2,372,-24.2,384,-24.1,384,-24.1,394,-24.2,404,-24.2,414,-24.2,424,-24.2,434,-24.3,448,-24.2,448,-24.2,458,-24.2,468,-24.2,478,-24.3,488,-24.3,498,-24.3,508,-24.2,518,-24.2,528,-24.1,538,-24.1,548,-24.1,558,-24.0,568,-24.1,578,-24.0,588,-24.0,598,-24.0,608,-23.9,618,-23.9,628,-23.8,638,-23.7,648,-23.7,658,-23.7,668,-23.6,678,-23.6,688,-23.6,698,-23.4,708,-23.6,718,-23.4,728,-23.4,738,-23.5,748,-23.4,758,-23.5,768,-23.6,778,-23.5,788,-23.6,798,-23.6,808,-23.6,818,-23.6,828,-23.6,838,-23.6,848,-23.5,858,-23.5,868,-23.5,878,-23.3,888,-23.3,898,-23.2,908,-23.1,918,-23.1,928,-22.9,938,-22.8,948,-22.7,958,-22.5,968,-22.4,978,-22.2,988,-22.1,998,-22.0,1011,-21.9,1011,-21.9,1119,-21.6,1119,-21.6,1129,-21.7,1139,-21.7,1149,-21.8,1159,-21.8,1169,-21.9,1179,-22.0,1189,-22.1,1199,-22.3,1209,-22.4,1219,-22.7,1229,-22.9,1239,-23.0,1251,-23.0\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 324.4823043142586,\r\n" + 
				"							\"relativeDirection\" : \"RIGHT\",\r\n" + 
				"							\"absoluteDirection\" : \"NORTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.9394651,\r\n" + 
				"							\"lat\" : 37.379543600000005,\r\n" + 
				"							\"elevation\" : \"0,-23.0,10,-23.2,20,-23.3,30,-23.4,40,-23.7,50,-23.9,60,-24.1,70,-24.1,80,-24.2,90,-24.3,100,-24.3,110,-24.3,120,-24.4,130,-24.4,140,-24.5,150,-24.5,160,-24.5,170,-24.4,180,-24.4,190,-24.4,200,-24.3,210,-24.3,220,-24.3,230,-24.3,240,-24.3,250,-24.3,260,-24.3,270,-24.4,280,-24.4,290,-24.3,300,-24.4,310,-24.4,324,-24.5\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 153.4657323579014,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.94126410000001,\r\n" + 
				"							\"lat\" : 37.3820457,\r\n" + 
				"							\"elevation\" : \"0,-24.5,10,-24.6,20,-24.7,30,-24.6,40,-24.3,50,-24.6,60,-24.3,70,-24.3,80,-24.5,90,-24.4,100,-24.4,110,-24.2,120,-24.3,130,-24.4,140,-24.5,153,-24.4\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 141.3792284403364,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHEAST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.942829,\r\n" + 
				"							\"lat\" : 37.3814469,\r\n" + 
				"							\"elevation\" : \"0,-24.4,10,-24.5,20,-24.6,30,-24.5,40,-24.6,54,-24.5,54,-24.5,64,-24.5,74,-24.4,84,-24.4,94,-24.3,104,-24.3,118,-24.2,118,-24.2,128,-24.2,138,-24.1,141,-24.1\"\r\n" + 
				"						}\r\n" + 
				"					],\r\n" + 
				"					\"duration\" : 1687000\r\n" + 
				"				}\r\n" + 
				"			],\r\n" + 
				"			\"tooSloped\" : false\r\n" + 
				"		}, {\r\n" + 
				"			\"id\" : \"519cf893e4b054c0551389ac\",\r\n" + 
				"			\"planId\" : \"519cf893e4b054c0551389a6\",\r\n" + 
				"			\"duration\" : 4756000,\r\n" + 
				"			\"startTime\" : 1369245450000,\r\n" + 
				"			\"endTime\" : 1369250206000,\r\n" + 
				"			\"walkTime\" : 1768,\r\n" + 
				"			\"transitTime\" : 2660,\r\n" + 
				"			\"waitingTime\" : 328,\r\n" + 
				"			\"walkDistance\" : 2310.9236523799514,\r\n" + 
				"			\"elevationLost\" : 9.594024825559956,\r\n" + 
				"			\"elevationGained\" : 59.981092139161746,\r\n" + 
				"			\"transfers\" : 1,\r\n" + 
				"			\"legs\" : [{\r\n" + 
				"					\"id\" : \"519cf893e4b054c0551389ad\",\r\n" + 
				"					\"itinId\" : \"519cf893e4b054c0551389ac\",\r\n" + 
				"					\"startTime\" : 1369245540000,\r\n" + 
				"					\"endTime\" : 1369245720000,\r\n" + 
				"					\"distance\" : 2568.8237561340675,\r\n" + 
				"					\"mode\" : \"TRAM\",\r\n" + 
				"					\"route\" : \"901\",\r\n" + 
				"					\"routeId\" : \"901\",\r\n" + 
				"					\"agencyName\" : \"VTA\",\r\n" + 
				"					\"agencyUrl\" : \"http://www.vta.org\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : 0,\r\n" + 
				"					\"headsign\" : \"LRT ALUM ROCK - SANTA TERESA\",\r\n" + 
				"					\"agencyId\" : \"VTA\",\r\n" + 
				"					\"tripId\" : \"1483140\",\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"SNELL STATION (1)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4783\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.830546628,\r\n" + 
				"						\"lat\" : 37.247640794,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"COTTLE STATION (1)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4784\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.802252317,\r\n" + 
				"						\"lat\" : 37.242504791,\r\n" + 
				"						\"arrival\" : 1369245745000,\r\n" + 
				"						\"departure\" : 1369245745000,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"intermediateStops\" : [],\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"w|ibF|_rfVb_@yoD\",\r\n" + 
				"						\"length\" : 2\r\n" + 
				"					},\r\n" + 
				"					\"routeShortName\" : \"901\",\r\n" + 
				"					\"routeLongName\" : \"ALUM ROCK-SANTA TERESA VIA BAYPOINT\",\r\n" + 
				"					\"duration\" : 180000\r\n" + 
				"				}, {\r\n" + 
				"					\"id\" : \"519cf893e4b054c0551389ae\",\r\n" + 
				"					\"itinId\" : \"519cf893e4b054c0551389ac\",\r\n" + 
				"					\"startTime\" : 1369245806000,\r\n" + 
				"					\"endTime\" : 1369245869000,\r\n" + 
				"					\"distance\" : 79.21796275651978,\r\n" + 
				"					\"mode\" : \"WALK\",\r\n" + 
				"					\"route\" : \"\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : -25200000,\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"parking aisle\",\r\n" + 
				"						\"lon\" : -121.80235261131709,\r\n" + 
				"						\"lat\" : 37.24209715260431\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"COTTLE STATION (0)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4737\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.803159131,\r\n" + 
				"						\"lat\" : 37.242731596,\r\n" + 
				"						\"arrival\" : 1369245870000,\r\n" + 
				"						\"departure\" : 1369245870000,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"azhbFvolfVADMfBEf@CX\",\r\n" + 
				"						\"length\" : 5\r\n" + 
				"					},\r\n" + 
				"					\"steps\" : [{\r\n" + 
				"							\"distance\" : 79.21796275651978,\r\n" + 
				"							\"absoluteDirection\" : \"WEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.80235261131709,\r\n" + 
				"							\"lat\" : 37.24209715260431,\r\n" + 
				"							\"elevation\" : \"0,26.3,10,26.6,20,26.8,30,27.1,40,27.4,50,27.7,50,27.7,60,28.3,67,28.7,67,28.7,79,29.0\"\r\n" + 
				"						}\r\n" + 
				"					],\r\n" + 
				"					\"duration\" : 63000\r\n" + 
				"				}, {\r\n" + 
				"					\"id\" : \"519cf893e4b054c0551389af\",\r\n" + 
				"					\"itinId\" : \"519cf893e4b054c0551389ac\",\r\n" + 
				"					\"startTime\" : 1369245960000,\r\n" + 
				"					\"endTime\" : 1369248440000,\r\n" + 
				"					\"distance\" : 21059.777048787077,\r\n" + 
				"					\"mode\" : \"TRAM\",\r\n" + 
				"					\"route\" : \"901\",\r\n" + 
				"					\"routeId\" : \"901\",\r\n" + 
				"					\"agencyName\" : \"VTA\",\r\n" + 
				"					\"agencyUrl\" : \"http://www.vta.org\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : 0,\r\n" + 
				"					\"headsign\" : \"LRT SANTA TERESA - ALUM ROCK\",\r\n" + 
				"					\"agencyId\" : \"VTA\",\r\n" + 
				"					\"tripId\" : \"1483069\",\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"COTTLE STATION (0)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4737\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.803159131,\r\n" + 
				"						\"lat\" : 37.242731596,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"COMPONENT STATION (0)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4755\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.925777086,\r\n" + 
				"						\"lat\" : 37.383483389,\r\n" + 
				"						\"arrival\" : 1369248440000,\r\n" + 
				"						\"departure\" : 1369248440000,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"intermediateStops\" : [{\r\n" + 
				"							\"name\" : \"SNELL STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4738\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.831338555,\r\n" + 
				"							\"lat\" : 37.248054577,\r\n" + 
				"							\"arrival\" : 1369246080000,\r\n" + 
				"							\"departure\" : 1369246080000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"BLOSSOM HILL STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4739\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.841811246,\r\n" + 
				"							\"lat\" : 37.253149716,\r\n" + 
				"							\"arrival\" : 1369246230000,\r\n" + 
				"							\"departure\" : 1369246230000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"OHLONE-CHYNOWETH STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4731\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.85966629,\r\n" + 
				"							\"lat\" : 37.257992959,\r\n" + 
				"							\"arrival\" : 1369246380000,\r\n" + 
				"							\"departure\" : 1369246380000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"BRANHAM STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4740\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.859381404,\r\n" + 
				"							\"lat\" : 37.267295633,\r\n" + 
				"							\"arrival\" : 1369246470000,\r\n" + 
				"							\"departure\" : 1369246470000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CAPITOL STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4741\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.863268615,\r\n" + 
				"							\"lat\" : 37.275293045,\r\n" + 
				"							\"arrival\" : 1369246560000,\r\n" + 
				"							\"departure\" : 1369246560000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CURTNER STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4742\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.872621797,\r\n" + 
				"							\"lat\" : 37.29375248,\r\n" + 
				"							\"arrival\" : 1369246710000,\r\n" + 
				"							\"departure\" : 1369246710000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"TAMIEN STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4743\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.88480874,\r\n" + 
				"							\"lat\" : 37.311927466,\r\n" + 
				"							\"arrival\" : 1369246860000,\r\n" + 
				"							\"departure\" : 1369246860000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"VIRGINIA STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4744\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.89010437,\r\n" + 
				"							\"lat\" : 37.31982806,\r\n" + 
				"							\"arrival\" : 1369246980000,\r\n" + 
				"							\"departure\" : 1369246980000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CHILDRENS DISCOVERY MUSEUM STA (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4745\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.893665023,\r\n" + 
				"							\"lat\" : 37.327764631,\r\n" + 
				"							\"arrival\" : 1369247100000,\r\n" + 
				"							\"departure\" : 1369247100000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CONVENTION CENTER STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4746\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.889754324,\r\n" + 
				"							\"lat\" : 37.330167643,\r\n" + 
				"							\"arrival\" : 1369247220000,\r\n" + 
				"							\"departure\" : 1369247220000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"SAN ANTONIO STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4747\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.888036544,\r\n" + 
				"							\"lat\" : 37.332821669,\r\n" + 
				"							\"arrival\" : 1369247370000,\r\n" + 
				"							\"departure\" : 1369247370000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"SANTA CLARA STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4748\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.890306491,\r\n" + 
				"							\"lat\" : 37.336005585,\r\n" + 
				"							\"arrival\" : 1369247520000,\r\n" + 
				"							\"departure\" : 1369247520000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"ST JAMES STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4749\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.89231429,\r\n" + 
				"							\"lat\" : 37.338552513,\r\n" + 
				"							\"arrival\" : 1369247660000,\r\n" + 
				"							\"departure\" : 1369247660000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"JAPANTOWN / AYER STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4750\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.897559175,\r\n" + 
				"							\"lat\" : 37.345556685,\r\n" + 
				"							\"arrival\" : 1369247800000,\r\n" + 
				"							\"departure\" : 1369247800000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CIVIC CENTER STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4751\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.902434282,\r\n" + 
				"							\"lat\" : 37.352015873,\r\n" + 
				"							\"arrival\" : 1369247940000,\r\n" + 
				"							\"departure\" : 1369247940000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"GISH STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4752\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.910160014,\r\n" + 
				"							\"lat\" : 37.362544086,\r\n" + 
				"							\"arrival\" : 1369248090000,\r\n" + 
				"							\"departure\" : 1369248090000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"METRO/AIRPORT STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4753\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.915989884,\r\n" + 
				"							\"lat\" : 37.370363515,\r\n" + 
				"							\"arrival\" : 1369248240000,\r\n" + 
				"							\"departure\" : 1369248240000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"KARINA COURT STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4754\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.92040537,\r\n" + 
				"							\"lat\" : 37.376277678,\r\n" + 
				"							\"arrival\" : 1369248340000,\r\n" + 
				"							\"departure\" : 1369248340000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}\r\n" + 
				"					],\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"a~hbFvtlfVg`@boDy^n`Ai]pnBcy@w@_q@fWkrBny@qpBbkAkp@b`@sp@fU_NmWsOwI{RdM}NpKwj@v_@kg@n]y`Aho@{o@jc@}c@rZal@p`@\",\r\n" + 
				"						\"length\" : 20\r\n" + 
				"					},\r\n" + 
				"					\"routeShortName\" : \"901\",\r\n" + 
				"					\"routeLongName\" : \"ALUM ROCK-SANTA TERESA VIA BAYPOINT\",\r\n" + 
				"					\"duration\" : 2480000\r\n" + 
				"				}, {\r\n" + 
				"					\"id\" : \"519cf893e4b054c0551389b0\",\r\n" + 
				"					\"itinId\" : \"519cf893e4b054c0551389ac\",\r\n" + 
				"					\"startTime\" : 1369248501000,\r\n" + 
				"					\"endTime\" : 1369250188000,\r\n" + 
				"					\"distance\" : 2231.7056896234317,\r\n" + 
				"					\"mode\" : \"WALK\",\r\n" + 
				"					\"route\" : \"\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : -25200000,\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"North 1st Street\",\r\n" + 
				"						\"lon\" : -121.92572135209195,\r\n" + 
				"						\"lat\" : 37.3835248978769\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"Belick Street\",\r\n" + 
				"						\"lon\" : -121.94220133265001,\r\n" + 
				"						\"lat\" : 37.38027735624535,\r\n" + 
				"						\"arrival\" : 1369250206000,\r\n" + 
				"						\"departure\" : 1369250206000\r\n" + 
				"					},\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"_ndcFxrdgVcG~DcFjDYRHXL\\\\bH|Rh@xAJXx@zBdCfHp@lBbArBpIzN~BxD`A~A`BtBaCxCqJlEvBvHxAo@hBw@d@S\",\r\n" + 
				"						\"length\" : 23\r\n" + 
				"					},\r\n" + 
				"					\"steps\" : [{\r\n" + 
				"							\"distance\" : 332.84477800839346,\r\n" + 
				"							\"absoluteDirection\" : \"NORTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.92572135209195,\r\n" + 
				"							\"lat\" : 37.3835248978769,\r\n" + 
				"							\"elevation\" : \"0,-23.2,10,-23.2,20,-23.3,30,-23.2,40,-23.3,50,-23.4,60,-23.4,70,-23.4,80,-23.4,90,-23.5,100,-23.4,110,-23.4,120,-23.4,130,-23.6,140,-23.7,150,-23.6,160,-23.7,170,-23.7,180,-23.8,190,-23.8,200,-23.8,210,-23.8,220,-23.9,230,-23.9,240,-23.9,250,-24.0,260,-24.0,270,-24.1,280,-24.1,290,-24.1,300,-24.0,310,-24.0,316,-24.0,316,-24.0,326,-23.9,333,-23.9\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 28.5056091443819,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.92764620000001,\r\n" + 
				"							\"lat\" : 37.3860977,\r\n" + 
				"							\"elevation\" : \"0,-23.9,10,-23.9,20,-23.9,29,-23.9\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 1251.02803735816,\r\n" + 
				"							\"relativeDirection\" : \"CONTINUE\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.92792940000001,\r\n" + 
				"							\"lat\" : 37.3859749,\r\n" + 
				"							\"elevation\" : \"0,-23.9,10,-24.0,20,-24.1,30,-24.0,40,-24.0,50,-24.0,60,-24.0,70,-24.0,80,-23.9,90,-23.9,100,-23.9,110,-23.9,120,-23.9,130,-23.9,140,-23.9,150,-23.9,160,-23.9,170,-23.9,180,-23.8,190,-23.9,200,-23.9,210,-23.8,220,-23.8,230,-23.8,240,-23.7,250,-23.9,260,-23.8,270,-23.9,280,-23.9,290,-23.9,300,-23.9,310,-24.0,325,-24.0,325,-24.0,335,-24.1,345,-24.1,355,-24.2,365,-24.1,372,-24.2,372,-24.2,384,-24.1,384,-24.1,394,-24.2,404,-24.2,414,-24.2,424,-24.2,434,-24.3,448,-24.2,448,-24.2,458,-24.2,468,-24.2,478,-24.3,488,-24.3,498,-24.3,508,-24.2,518,-24.2,528,-24.1,538,-24.1,548,-24.1,558,-24.0,568,-24.1,578,-24.0,588,-24.0,598,-24.0,608,-23.9,618,-23.9,628,-23.8,638,-23.7,648,-23.7,658,-23.7,668,-23.6,678,-23.6,688,-23.6,698,-23.4,708,-23.6,718,-23.4,728,-23.4,738,-23.5,748,-23.4,758,-23.5,768,-23.6,778,-23.5,788,-23.6,798,-23.6,808,-23.6,818,-23.6,828,-23.6,838,-23.6,848,-23.5,858,-23.5,868,-23.5,878,-23.3,888,-23.3,898,-23.2,908,-23.1,918,-23.1,928,-22.9,938,-22.8,948,-22.7,958,-22.5,968,-22.4,978,-22.2,988,-22.1,998,-22.0,1011,-21.9,1011,-21.9,1119,-21.6,1119,-21.6,1129,-21.7,1139,-21.7,1149,-21.8,1159,-21.8,1169,-21.9,1179,-22.0,1189,-22.1,1199,-22.3,1209,-22.4,1219,-22.7,1229,-22.9,1239,-23.0,1251,-23.0\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 324.4823043142586,\r\n" + 
				"							\"relativeDirection\" : \"RIGHT\",\r\n" + 
				"							\"absoluteDirection\" : \"NORTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.9394651,\r\n" + 
				"							\"lat\" : 37.379543600000005,\r\n" + 
				"							\"elevation\" : \"0,-23.0,10,-23.2,20,-23.3,30,-23.4,40,-23.7,50,-23.9,60,-24.1,70,-24.1,80,-24.2,90,-24.3,100,-24.3,110,-24.3,120,-24.4,130,-24.4,140,-24.5,150,-24.5,160,-24.5,170,-24.4,180,-24.4,190,-24.4,200,-24.3,210,-24.3,220,-24.3,230,-24.3,240,-24.3,250,-24.3,260,-24.3,270,-24.4,280,-24.4,290,-24.3,300,-24.4,310,-24.4,324,-24.5\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 153.4657323579014,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.94126410000001,\r\n" + 
				"							\"lat\" : 37.3820457,\r\n" + 
				"							\"elevation\" : \"0,-24.5,10,-24.6,20,-24.7,30,-24.6,40,-24.3,50,-24.6,60,-24.3,70,-24.3,80,-24.5,90,-24.4,100,-24.4,110,-24.2,120,-24.3,130,-24.4,140,-24.5,153,-24.4\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 141.3792284403364,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHEAST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.942829,\r\n" + 
				"							\"lat\" : 37.3814469,\r\n" + 
				"							\"elevation\" : \"0,-24.4,10,-24.5,20,-24.6,30,-24.5,40,-24.6,54,-24.5,54,-24.5,64,-24.5,74,-24.4,84,-24.4,94,-24.3,104,-24.3,118,-24.2,118,-24.2,128,-24.2,138,-24.1,141,-24.1\"\r\n" + 
				"						}\r\n" + 
				"					],\r\n" + 
				"					\"duration\" : 1687000\r\n" + 
				"				}\r\n" + 
				"			],\r\n" + 
				"			\"tooSloped\" : false\r\n" + 
				"		}, {\r\n" + 
				"			\"id\" : \"519cf893e4b054c0551389b1\",\r\n" + 
				"			\"planId\" : \"519cf893e4b054c0551389a6\",\r\n" + 
				"			\"duration\" : 4756000,\r\n" + 
				"			\"startTime\" : 1369246350000,\r\n" + 
				"			\"endTime\" : 1369251106000,\r\n" + 
				"			\"walkTime\" : 1768,\r\n" + 
				"			\"transitTime\" : 2660,\r\n" + 
				"			\"waitingTime\" : 328,\r\n" + 
				"			\"walkDistance\" : 2310.9236523799514,\r\n" + 
				"			\"elevationLost\" : 9.594024825559956,\r\n" + 
				"			\"elevationGained\" : 59.981092139161746,\r\n" + 
				"			\"transfers\" : 1,\r\n" + 
				"			\"legs\" : [{\r\n" + 
				"					\"id\" : \"519cf893e4b054c0551389b2\",\r\n" + 
				"					\"itinId\" : \"519cf893e4b054c0551389b1\",\r\n" + 
				"					\"startTime\" : 1369246440000,\r\n" + 
				"					\"endTime\" : 1369246620000,\r\n" + 
				"					\"distance\" : 2568.8237561340675,\r\n" + 
				"					\"mode\" : \"TRAM\",\r\n" + 
				"					\"route\" : \"901\",\r\n" + 
				"					\"routeId\" : \"901\",\r\n" + 
				"					\"agencyName\" : \"VTA\",\r\n" + 
				"					\"agencyUrl\" : \"http://www.vta.org\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : 0,\r\n" + 
				"					\"headsign\" : \"LRT ALUM ROCK - SANTA TERESA\",\r\n" + 
				"					\"agencyId\" : \"VTA\",\r\n" + 
				"					\"tripId\" : \"1483141\",\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"SNELL STATION (1)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4783\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.830546628,\r\n" + 
				"						\"lat\" : 37.247640794,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"COTTLE STATION (1)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4784\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.802252317,\r\n" + 
				"						\"lat\" : 37.242504791,\r\n" + 
				"						\"arrival\" : 1369246645000,\r\n" + 
				"						\"departure\" : 1369246645000,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"intermediateStops\" : [],\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"w|ibF|_rfVb_@yoD\",\r\n" + 
				"						\"length\" : 2\r\n" + 
				"					},\r\n" + 
				"					\"routeShortName\" : \"901\",\r\n" + 
				"					\"routeLongName\" : \"ALUM ROCK-SANTA TERESA VIA BAYPOINT\",\r\n" + 
				"					\"duration\" : 180000\r\n" + 
				"				}, {\r\n" + 
				"					\"id\" : \"519cf893e4b054c0551389b3\",\r\n" + 
				"					\"itinId\" : \"519cf893e4b054c0551389b1\",\r\n" + 
				"					\"startTime\" : 1369246706000,\r\n" + 
				"					\"endTime\" : 1369246769000,\r\n" + 
				"					\"distance\" : 79.21796275651978,\r\n" + 
				"					\"mode\" : \"WALK\",\r\n" + 
				"					\"route\" : \"\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : -25200000,\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"parking aisle\",\r\n" + 
				"						\"lon\" : -121.80235261131709,\r\n" + 
				"						\"lat\" : 37.24209715260431\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"COTTLE STATION (0)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4737\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.803159131,\r\n" + 
				"						\"lat\" : 37.242731596,\r\n" + 
				"						\"arrival\" : 1369246770000,\r\n" + 
				"						\"departure\" : 1369246770000,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"azhbFvolfVADMfBEf@CX\",\r\n" + 
				"						\"length\" : 5\r\n" + 
				"					},\r\n" + 
				"					\"steps\" : [{\r\n" + 
				"							\"distance\" : 79.21796275651978,\r\n" + 
				"							\"absoluteDirection\" : \"WEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.80235261131709,\r\n" + 
				"							\"lat\" : 37.24209715260431,\r\n" + 
				"							\"elevation\" : \"0,26.3,10,26.6,20,26.8,30,27.1,40,27.4,50,27.7,50,27.7,60,28.3,67,28.7,67,28.7,79,29.0\"\r\n" + 
				"						}\r\n" + 
				"					],\r\n" + 
				"					\"duration\" : 63000\r\n" + 
				"				}, {\r\n" + 
				"					\"id\" : \"519cf893e4b054c0551389b4\",\r\n" + 
				"					\"itinId\" : \"519cf893e4b054c0551389b1\",\r\n" + 
				"					\"startTime\" : 1369246860000,\r\n" + 
				"					\"endTime\" : 1369249340000,\r\n" + 
				"					\"distance\" : 21059.777048787077,\r\n" + 
				"					\"mode\" : \"TRAM\",\r\n" + 
				"					\"route\" : \"901\",\r\n" + 
				"					\"routeId\" : \"901\",\r\n" + 
				"					\"agencyName\" : \"VTA\",\r\n" + 
				"					\"agencyUrl\" : \"http://www.vta.org\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : 0,\r\n" + 
				"					\"headsign\" : \"LRT SANTA TERESA - ALUM ROCK\",\r\n" + 
				"					\"agencyId\" : \"VTA\",\r\n" + 
				"					\"tripId\" : \"1483070\",\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"COTTLE STATION (0)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4737\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.803159131,\r\n" + 
				"						\"lat\" : 37.242731596,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"COMPONENT STATION (0)\",\r\n" + 
				"						\"stopId\" : {\r\n" + 
				"							\"agencyId\" : \"VTA\",\r\n" + 
				"							\"id\" : \"4755\"\r\n" + 
				"						},\r\n" + 
				"						\"lon\" : -121.925777086,\r\n" + 
				"						\"lat\" : 37.383483389,\r\n" + 
				"						\"arrival\" : 1369249340000,\r\n" + 
				"						\"departure\" : 1369249340000,\r\n" + 
				"						\"zoneId\" : \"1\"\r\n" + 
				"					},\r\n" + 
				"					\"intermediateStops\" : [{\r\n" + 
				"							\"name\" : \"SNELL STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4738\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.831338555,\r\n" + 
				"							\"lat\" : 37.248054577,\r\n" + 
				"							\"arrival\" : 1369246980000,\r\n" + 
				"							\"departure\" : 1369246980000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"BLOSSOM HILL STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4739\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.841811246,\r\n" + 
				"							\"lat\" : 37.253149716,\r\n" + 
				"							\"arrival\" : 1369247130000,\r\n" + 
				"							\"departure\" : 1369247130000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"OHLONE-CHYNOWETH STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4731\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.85966629,\r\n" + 
				"							\"lat\" : 37.257992959,\r\n" + 
				"							\"arrival\" : 1369247280000,\r\n" + 
				"							\"departure\" : 1369247280000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"BRANHAM STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4740\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.859381404,\r\n" + 
				"							\"lat\" : 37.267295633,\r\n" + 
				"							\"arrival\" : 1369247370000,\r\n" + 
				"							\"departure\" : 1369247370000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CAPITOL STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4741\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.863268615,\r\n" + 
				"							\"lat\" : 37.275293045,\r\n" + 
				"							\"arrival\" : 1369247460000,\r\n" + 
				"							\"departure\" : 1369247460000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CURTNER STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4742\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.872621797,\r\n" + 
				"							\"lat\" : 37.29375248,\r\n" + 
				"							\"arrival\" : 1369247610000,\r\n" + 
				"							\"departure\" : 1369247610000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"TAMIEN STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4743\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.88480874,\r\n" + 
				"							\"lat\" : 37.311927466,\r\n" + 
				"							\"arrival\" : 1369247760000,\r\n" + 
				"							\"departure\" : 1369247760000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"VIRGINIA STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4744\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.89010437,\r\n" + 
				"							\"lat\" : 37.31982806,\r\n" + 
				"							\"arrival\" : 1369247880000,\r\n" + 
				"							\"departure\" : 1369247880000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CHILDRENS DISCOVERY MUSEUM STA (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4745\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.893665023,\r\n" + 
				"							\"lat\" : 37.327764631,\r\n" + 
				"							\"arrival\" : 1369248000000,\r\n" + 
				"							\"departure\" : 1369248000000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CONVENTION CENTER STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4746\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.889754324,\r\n" + 
				"							\"lat\" : 37.330167643,\r\n" + 
				"							\"arrival\" : 1369248120000,\r\n" + 
				"							\"departure\" : 1369248120000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"SAN ANTONIO STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4747\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.888036544,\r\n" + 
				"							\"lat\" : 37.332821669,\r\n" + 
				"							\"arrival\" : 1369248270000,\r\n" + 
				"							\"departure\" : 1369248270000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"SANTA CLARA STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4748\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.890306491,\r\n" + 
				"							\"lat\" : 37.336005585,\r\n" + 
				"							\"arrival\" : 1369248420000,\r\n" + 
				"							\"departure\" : 1369248420000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"ST JAMES STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4749\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.89231429,\r\n" + 
				"							\"lat\" : 37.338552513,\r\n" + 
				"							\"arrival\" : 1369248560000,\r\n" + 
				"							\"departure\" : 1369248560000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"JAPANTOWN / AYER STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4750\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.897559175,\r\n" + 
				"							\"lat\" : 37.345556685,\r\n" + 
				"							\"arrival\" : 1369248700000,\r\n" + 
				"							\"departure\" : 1369248700000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"CIVIC CENTER STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4751\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.902434282,\r\n" + 
				"							\"lat\" : 37.352015873,\r\n" + 
				"							\"arrival\" : 1369248840000,\r\n" + 
				"							\"departure\" : 1369248840000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"GISH STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4752\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.910160014,\r\n" + 
				"							\"lat\" : 37.362544086,\r\n" + 
				"							\"arrival\" : 1369248990000,\r\n" + 
				"							\"departure\" : 1369248990000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"METRO/AIRPORT STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4753\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.915989884,\r\n" + 
				"							\"lat\" : 37.370363515,\r\n" + 
				"							\"arrival\" : 1369249140000,\r\n" + 
				"							\"departure\" : 1369249140000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}, {\r\n" + 
				"							\"name\" : \"KARINA COURT STATION (0)\",\r\n" + 
				"							\"stopId\" : {\r\n" + 
				"								\"agencyId\" : \"VTA\",\r\n" + 
				"								\"id\" : \"4754\"\r\n" + 
				"							},\r\n" + 
				"							\"lon\" : -121.92040537,\r\n" + 
				"							\"lat\" : 37.376277678,\r\n" + 
				"							\"arrival\" : 1369249240000,\r\n" + 
				"							\"departure\" : 1369249240000,\r\n" + 
				"							\"zoneId\" : \"1\"\r\n" + 
				"						}\r\n" + 
				"					],\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"a~hbFvtlfVg`@boDy^n`Ai]pnBcy@w@_q@fWkrBny@qpBbkAkp@b`@sp@fU_NmWsOwI{RdM}NpKwj@v_@kg@n]y`Aho@{o@jc@}c@rZal@p`@\",\r\n" + 
				"						\"length\" : 20\r\n" + 
				"					},\r\n" + 
				"					\"routeShortName\" : \"901\",\r\n" + 
				"					\"routeLongName\" : \"ALUM ROCK-SANTA TERESA VIA BAYPOINT\",\r\n" + 
				"					\"duration\" : 2480000\r\n" + 
				"				}, {\r\n" + 
				"					\"id\" : \"519cf893e4b054c0551389b5\",\r\n" + 
				"					\"itinId\" : \"519cf893e4b054c0551389b1\",\r\n" + 
				"					\"startTime\" : 1369249401000,\r\n" + 
				"					\"endTime\" : 1369251088000,\r\n" + 
				"					\"distance\" : 2231.7056896234317,\r\n" + 
				"					\"mode\" : \"WALK\",\r\n" + 
				"					\"route\" : \"\",\r\n" + 
				"					\"agencyTimeZoneOffset\" : -25200000,\r\n" + 
				"					\"from\" : {\r\n" + 
				"						\"name\" : \"North 1st Street\",\r\n" + 
				"						\"lon\" : -121.92572135209195,\r\n" + 
				"						\"lat\" : 37.3835248978769\r\n" + 
				"					},\r\n" + 
				"					\"to\" : {\r\n" + 
				"						\"name\" : \"Belick Street\",\r\n" + 
				"						\"lon\" : -121.94220133265001,\r\n" + 
				"						\"lat\" : 37.38027735624535,\r\n" + 
				"						\"arrival\" : 1369251106000,\r\n" + 
				"						\"departure\" : 1369251106000\r\n" + 
				"					},\r\n" + 
				"					\"legGeometry\" : {\r\n" + 
				"						\"points\" : \"_ndcFxrdgVcG~DcFjDYRHXL\\\\bH|Rh@xAJXx@zBdCfHp@lBbArBpIzN~BxD`A~A`BtBaCxCqJlEvBvHxAo@hBw@d@S\",\r\n" + 
				"						\"length\" : 23\r\n" + 
				"					},\r\n" + 
				"					\"steps\" : [{\r\n" + 
				"							\"distance\" : 332.84477800839346,\r\n" + 
				"							\"absoluteDirection\" : \"NORTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.92572135209195,\r\n" + 
				"							\"lat\" : 37.3835248978769,\r\n" + 
				"							\"elevation\" : \"0,-23.2,10,-23.2,20,-23.3,30,-23.2,40,-23.3,50,-23.4,60,-23.4,70,-23.4,80,-23.4,90,-23.5,100,-23.4,110,-23.4,120,-23.4,130,-23.6,140,-23.7,150,-23.6,160,-23.7,170,-23.7,180,-23.8,190,-23.8,200,-23.8,210,-23.8,220,-23.9,230,-23.9,240,-23.9,250,-24.0,260,-24.0,270,-24.1,280,-24.1,290,-24.1,300,-24.0,310,-24.0,316,-24.0,316,-24.0,326,-23.9,333,-23.9\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 28.5056091443819,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.92764620000001,\r\n" + 
				"							\"lat\" : 37.3860977,\r\n" + 
				"							\"elevation\" : \"0,-23.9,10,-23.9,20,-23.9,29,-23.9\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 1251.02803735816,\r\n" + 
				"							\"relativeDirection\" : \"CONTINUE\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.92792940000001,\r\n" + 
				"							\"lat\" : 37.3859749,\r\n" + 
				"							\"elevation\" : \"0,-23.9,10,-24.0,20,-24.1,30,-24.0,40,-24.0,50,-24.0,60,-24.0,70,-24.0,80,-23.9,90,-23.9,100,-23.9,110,-23.9,120,-23.9,130,-23.9,140,-23.9,150,-23.9,160,-23.9,170,-23.9,180,-23.8,190,-23.9,200,-23.9,210,-23.8,220,-23.8,230,-23.8,240,-23.7,250,-23.9,260,-23.8,270,-23.9,280,-23.9,290,-23.9,300,-23.9,310,-24.0,325,-24.0,325,-24.0,335,-24.1,345,-24.1,355,-24.2,365,-24.1,372,-24.2,372,-24.2,384,-24.1,384,-24.1,394,-24.2,404,-24.2,414,-24.2,424,-24.2,434,-24.3,448,-24.2,448,-24.2,458,-24.2,468,-24.2,478,-24.3,488,-24.3,498,-24.3,508,-24.2,518,-24.2,528,-24.1,538,-24.1,548,-24.1,558,-24.0,568,-24.1,578,-24.0,588,-24.0,598,-24.0,608,-23.9,618,-23.9,628,-23.8,638,-23.7,648,-23.7,658,-23.7,668,-23.6,678,-23.6,688,-23.6,698,-23.4,708,-23.6,718,-23.4,728,-23.4,738,-23.5,748,-23.4,758,-23.5,768,-23.6,778,-23.5,788,-23.6,798,-23.6,808,-23.6,818,-23.6,828,-23.6,838,-23.6,848,-23.5,858,-23.5,868,-23.5,878,-23.3,888,-23.3,898,-23.2,908,-23.1,918,-23.1,928,-22.9,938,-22.8,948,-22.7,958,-22.5,968,-22.4,978,-22.2,988,-22.1,998,-22.0,1011,-21.9,1011,-21.9,1119,-21.6,1119,-21.6,1129,-21.7,1139,-21.7,1149,-21.8,1159,-21.8,1169,-21.9,1179,-22.0,1189,-22.1,1199,-22.3,1209,-22.4,1219,-22.7,1229,-22.9,1239,-23.0,1251,-23.0\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 324.4823043142586,\r\n" + 
				"							\"relativeDirection\" : \"RIGHT\",\r\n" + 
				"							\"absoluteDirection\" : \"NORTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.9394651,\r\n" + 
				"							\"lat\" : 37.379543600000005,\r\n" + 
				"							\"elevation\" : \"0,-23.0,10,-23.2,20,-23.3,30,-23.4,40,-23.7,50,-23.9,60,-24.1,70,-24.1,80,-24.2,90,-24.3,100,-24.3,110,-24.3,120,-24.4,130,-24.4,140,-24.5,150,-24.5,160,-24.5,170,-24.4,180,-24.4,190,-24.4,200,-24.3,210,-24.3,220,-24.3,230,-24.3,240,-24.3,250,-24.3,260,-24.3,270,-24.4,280,-24.4,290,-24.3,300,-24.4,310,-24.4,324,-24.5\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 153.4657323579014,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHWEST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.94126410000001,\r\n" + 
				"							\"lat\" : 37.3820457,\r\n" + 
				"							\"elevation\" : \"0,-24.5,10,-24.6,20,-24.7,30,-24.6,40,-24.3,50,-24.6,60,-24.3,70,-24.3,80,-24.5,90,-24.4,100,-24.4,110,-24.2,120,-24.3,130,-24.4,140,-24.5,153,-24.4\"\r\n" + 
				"						}, {\r\n" + 
				"							\"distance\" : 141.3792284403364,\r\n" + 
				"							\"relativeDirection\" : \"LEFT\",\r\n" + 
				"							\"absoluteDirection\" : \"SOUTHEAST\",\r\n" + 
				"							\"stayOn\" : false,\r\n" + 
				"							\"bogusName\" : false,\r\n" + 
				"							\"lon\" : -121.942829,\r\n" + 
				"							\"lat\" : 37.3814469,\r\n" + 
				"							\"elevation\" : \"0,-24.4,10,-24.5,20,-24.6,30,-24.5,40,-24.6,54,-24.5,54,-24.5,64,-24.5,74,-24.4,84,-24.4,94,-24.3,104,-24.3,118,-24.2,118,-24.2,128,-24.2,138,-24.1,141,-24.1\"\r\n" + 
				"						}\r\n" + 
				"					],\r\n" + 
				"					\"duration\" : 1687000\r\n" + 
				"				}\r\n" + 
				"			],\r\n" + 
				"			\"tooSloped\" : false\r\n" + 
				"		}\r\n" + 
				"	],\r\n" + 
				"	\"appType\" : 4\r\n" + 
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
			String strAppType = reqParam.get(RequestParam.NIMBLER_APP_TYPE);	
			//			System.out.println(strLegs);
			List lstLegs =  JSONUtil.getLegsJson(strLegs);
			if(ComUtils.isEmptyList(lstLegs))
				throw new TpException(TP_CODES.INVALID_REQUEST.getCode(),"Error while getting JSON String from plan object.");

			long start = System.currentTimeMillis();
			tripResponse = planService.getNextLegs(lstLegs,strAppType);			
			long end = System.currentTimeMillis();
			logger.debug(loggerName,"Operation took " + (end - start) + " msec");
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
		String res = JSONUtil.getResponseJSON(tripResponse);	
		return res; 
	}
	@GET
	@Path("/graph/metadata/")
	public Object getGraphMetadata(@QueryParam(RequestParam.NIMBLER_APP_TYPE) String appType){
		try {
			return planService.getGraphMetaData(appType);
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
		return Response.status(500).build();
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