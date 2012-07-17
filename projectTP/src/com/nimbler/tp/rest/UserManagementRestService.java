/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.UserManagementService;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.TpException;
/**
 * 
 * @author suresh
 *
 */
@Path("/users/")
public class UserManagementRestService {

	private LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private String loggerName;

	@GET
	@Path("/preferences/update")
	@Produces(MediaType.TEXT_PLAIN)
	public String saveAlertPreference(@QueryParam(RequestParam.DEVICE_ID)String deviceid,
			@QueryParam(RequestParam.ALERT)String alertCount,
			@QueryParam(RequestParam.DEVICE_TOKEN)String deviceToken,
			@QueryParam(RequestParam.MAX_DISTANCE)String maxWalkDistance) throws TpException {

		TPResponse response = new TPResponse();
		try {
			if (deviceid == null || alertCount ==null || deviceToken == null || maxWalkDistance == null)
				throw new TpException(TP_CODES.INVALID_REQUEST);

			UserManagementService alertService = BeanUtil.getUserManagementService();
			alertService.saveAlertPreferences(deviceid, Integer.parseInt(alertCount), deviceToken, maxWalkDistance);
			logger.info(loggerName, "Preferences are :"+deviceid+","+ alertCount + ","+ deviceToken+","+maxWalkDistance);
			response.setCode(TP_CODES.SUCESS.getCode()); 
		} catch (TpException e) {
			logger.error(loggerName, e.getErrMsg());
			response.setCode(e.getErrCode()); 
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setCode(TP_CODES.FAIL.ordinal());
		}
		return getJsonResponse(response);
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
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public String getLoggerName() {
		return loggerName;
	}
}