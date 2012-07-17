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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

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

	@Autowired
	private LoggingService logger;
	//	private LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private String loggerName;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/test/")
	public String firstTest(@Context HttpServletRequest request) {
		return "Feedback service";
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
			logger.info(loggerName, e.getErrMsg());
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