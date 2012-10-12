/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.TpFeedbackService;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.ResponseUtil;
import com.nimbler.tp.util.TpException;

/**
 * The  Feedback REST web service.
 *
 * @author nirmal
 */
@Path("/feedback/") 
public class TpFeedbackRestService {

	private LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private String loggerName;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/test/")
	public String firstTest(@DefaultValue("2") @QueryParam("test") List<Integer> test) {
		return "Feedback service: "+test;
	}

	@POST
	@Path("/new/")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public TPResponse upload(@Context HttpServletRequest request) throws Exception {
		TPResponse response = null;		
		try {
			Map<String,String> reqParamMap = new HashMap<String, String>();
			List<File> lstFile = new ArrayList<File>();
			ComUtils.parseMultipartRequest(request, reqParamMap, lstFile,System.currentTimeMillis()+"_%s");

			TpFeedbackService service =BeanUtil.getTpFeedbackService();
			response =  service.addFeedback(reqParamMap, lstFile);
		} catch (TpException e) {
			logger.warn(loggerName, e.getErrMsg());
			response = ResponseUtil.createResponse(e);
		} catch (Exception e) {
			logger.error(loggerName,e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
}