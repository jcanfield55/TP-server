/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.service;

import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dbobject.EventLog;
import com.nimbler.tp.dbobject.EventLog.EVENT_TYPE;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.smtp.MailService;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;

/**
 * The Class TpEventLoggingService.
 */
public class TpEventLoggingService {

	@Autowired	
	private LoggingService logger;
	@Autowired
	private PersistenceService persistanceService;
	@Autowired
	private MailService mailService;

	private String loggerName;

	public TpEventLoggingService() {
	}

	/**
	 * Save plan.
	 *
	 * @param reqMap the request parameter  map
	 * @param planId the plan id
	 * @return EventLog db id
	 */
	public String savePlan(Map<String, String> reqMap,String planId) {
		String res = null;
		try {
			EventLog log = new EventLog();
			log.setCerateTime(System.currentTimeMillis());
			log.setDeviceId(reqMap.get(RequestParam.DEVICE_ID));
			log.setEventType(EVENT_TYPE.SAVE_PLAN_WITH_GEO.ordinal());
			log.setRawAddrTo(reqMap.get(RequestParam.RAW_ADDRESS_TO));
			log.setRawAddrFrom(reqMap.get(RequestParam.RAW_ADDRESS_FROM));
			log.setFrmtdAddrFrom(reqMap.get(RequestParam.FORMATTED_ADDRESS_FROM));
			log.setFrmtdAddrTo(reqMap.get(RequestParam.FORMATTED_ADDRESS_TO));

			log.setLatFrom(NumberUtils.toDouble(reqMap.get(RequestParam.LAT_FROM)));
			log.setLatTo(NumberUtils.toDouble(reqMap.get(RequestParam.LAT_TO)));
			log.setLonFrom(NumberUtils.toDouble(reqMap.get(RequestParam.LON_FROM)));
			log.setLonTo(NumberUtils.toDouble(reqMap.get(RequestParam.LON_TO)));

			log.setGeoRespFrom(reqMap.get(RequestParam.GEO_RESPONSE_FROM));
			log.setGeoRespTo(reqMap.get(RequestParam.GEO_RESPONSE_TO));

			log.setFromType(NumberUtils.toInt(reqMap.get(RequestParam.FROM_TYPE)));
			log.setToType(NumberUtils.toInt(reqMap.get(RequestParam.TO_TYPE)));

			log.setTimeFrom(NumberUtils.toDouble(reqMap.get(RequestParam.TIME_FROM)));
			log.setTimeTo(NumberUtils.toDouble(reqMap.get(RequestParam.TIME_TO)));
			log.setTimeTripPlan(NumberUtils.toDouble(reqMap.get(RequestParam.TIME_TRIP_PLAN)));

			log.setPlanId(planId);
			logger.debug(loggerName, "New Log:"+log);
			persistanceService.addObject(MONGO_TABLES.event_log.name(), log);
			res = log.getId();			
		} catch (DBException e) {
			// do not throw exception as event insertion fail can be ignored for response , just log 
			logger.error(loggerName, e.getMessage()); 
		}
		return res;
	}
	/**
	 * Generate plan.
	 *
	 * @param deviceId the device id
	 * @param planId the plan id
	 * @return the tP response
	 */
	@Deprecated
	public void generatePlan(String deviceId, String planId) {
		try {
			EventLog log = new EventLog();
			log.setCerateTime(System.currentTimeMillis());
			log.setDeviceId(deviceId);
			log.setPlanId(planId);
			log.setEventType(EVENT_TYPE.GENERATE_PLAN.ordinal());		
			persistanceService.addObject(MONGO_TABLES.event_log.name(), log);
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage()); 
		}
	}
	/**
	 * 
	 * @param planId
	 * @return
	 */
	public EventLog getLogForPlan(String planId) {
		try {
			return (EventLog)persistanceService.findOne(MONGO_TABLES.event_log.name(), "planId", planId, EventLog.class);
		} catch (DBException e) {
			logger.error(loggerName, "Exception while getting Event Log for Plan: "+planId+"-->"+e.getMessage()); 
		} 
		return null;
	}
	public LoggingService getLogger() {
		return logger;
	}

	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}

	public PersistenceService getPersistanceService() {
		return persistanceService;
	}

	public void setPersistanceService(PersistenceService persistanceService) {
		this.persistanceService = persistanceService;
	}

	public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
}
