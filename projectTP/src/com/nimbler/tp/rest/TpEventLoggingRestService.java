/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.rest;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.service.LoggingService;

/**
 * The Tp Logging WebService.
 *
 * @author nirmal
 */
@Path("/event/")
@Deprecated
public class TpEventLoggingRestService {

	@Autowired
	private LoggingService logger ;
	private String loggerName;

	/*@POST
	@Path("/geocode")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	@Deprecated
	public TPResponse geoCodeEvent(@FormParam(RequestParam.TYPE) int eventType,
			@FormParam(RequestParam.RAW_ADDRESS) String rawAddress,
			@FormParam(RequestParam.FORMATTED_ADDRESS) String formattedAddress,
			@FormParam(RequestParam.DEVICE_ID) String deviceId,
			@FormParam(RequestParam.GEO_RESPONSE) String geoResponse){
		TPResponse response = null;
		try {
			if(ComUtils.isEmptyString(rawAddress) || ComUtils.isEmptyString(formattedAddress) || 
					ComUtils.isEmptyString(geoResponse) ||ComUtils.isEmptyString(deviceId))
				throw new TpException(TP_CODES.INVALID_REQUEST);

			TpEventLoggingService service = (TpEventLoggingService) TPApplicationContext.getBeanByName(SPRING_BEANS.EVENT_LOGGING_SERVICE.bean());
			//			response = service.geoCode(eventType, rawAddress, formattedAddress, geoResponse,deviceId);
		} catch (TpException e) {			
			logger.error(loggerName, e.getErrMsg());
			response = ResponseUtil.createResponse(e);
		} catch (Exception e) {	
			logger.error(loggerName,e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@POST
	@Path("/revgeocode")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	@Deprecated
	public TPResponse reverseGeoCodeEvent(
			@FormParam(RequestParam.LAT) Double lat,
			@FormParam(RequestParam.LON) Double lon,
			@FormParam(RequestParam.DEVICE_ID) String deviceId,
			@FormParam(RequestParam.GEO_RESPONSE) String geoResponse){
		TPResponse response = null;
		try {
			if(lat==null || lon==null ||  
					ComUtils.isEmptyString(deviceId) || ComUtils.isEmptyString(geoResponse))
				throw new TpException(TP_CODES.INVALID_REQUEST);

			TpEventLoggingService service = (TpEventLoggingService) TPApplicationContext.getBeanByName(SPRING_BEANS.EVENT_LOGGING_SERVICE.bean());
			//			response = service.reverseGeoCode(lat,lon,deviceId,geoResponse);
		} catch (TpException e) {			
			logger.error(loggerName, e.getErrMsg());
			response = ResponseUtil.createResponse(e);
		} catch (Exception e) {	
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		return response;
	}*/

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
}
