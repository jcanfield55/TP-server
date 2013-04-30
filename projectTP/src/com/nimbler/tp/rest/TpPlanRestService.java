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

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.dataobject.TripPlan;
import com.nimbler.tp.dataobject.TripResponse;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.TpEventLoggingService;
import com.nimbler.tp.service.TpPlanService;
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
		//		System.out.println(res);
		//		System.out.println("============================================================");
		return res;
	}
	@GET
	@Path("/test/")
	public String test(@QueryParam("req") String username){
		System.out.println("------>"+username);
		return "OK";
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
			TpPlanService service = BeanUtil.getPlanService();
			long start = System.currentTimeMillis();
			tripResponse = service.getNextLegs(lstLegs);			
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
}