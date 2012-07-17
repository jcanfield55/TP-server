/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.rest;

import java.util.HashMap;
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
import com.nimbler.tp.util.HttpUtils;
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
	@GET
	@Path("/generate/")
	@Produces({MediaType.APPLICATION_JSON})
	public String generatePlan(@Context HttpServletRequest httpRequest){
		TPResponse response = null;
		try {
			Map<String,String> reqParams = HttpUtils.getRequestParameters(httpRequest);
			TpPlanService service = BeanUtil.getPlanService();
			TripResponse tripResponse = service.genearetePlan(reqParams);
			String planId = null;
			if(tripResponse==null)
				throw new TpException("TripResponse is null");
			if(tripResponse.getPlan()!=null)
				planId = tripResponse.getPlan().getId();
			TpEventLoggingService tpLoggingService = BeanUtil.getTpEventLoggingService();
			tpLoggingService.savePlan(reqParams,planId);

			response = new TPResponse(TP_CODES.SUCESS);
			response.setPlan(tripResponse.getPlan());
			response.setError(tripResponse.getError());
			return JSONUtil.getJsonFromObj(response);
		} catch (TpException e) {
			logger.info(loggerName, e.getErrMsg());
			response =ResponseUtil.createResponse(e);
		}catch (Exception e) {
			logger.error(loggerName, e);
			response =ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		return JSONUtil.getResponseJSON(response);		
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