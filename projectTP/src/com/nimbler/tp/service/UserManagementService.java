/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dbobject.User;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
/**
 * 
 * @author suresh
 *
 */
public class UserManagementService {

	private String loggerName;
	@Autowired
	private LoggingService logger;
	@Autowired
	private PersistenceService persistenceService;

	public UserManagementService() {

	}
	/**
	 * 
	 * @param deviceid
	 * @param numberOfAlert
	 * @param deviceToken
	 * @param maxWalkDistance
	 */
	public void saveAlertPreferences(String deviceid, int numberOfAlert, String deviceToken, String maxWalkDistance) {
		try {
			BasicDBObject query = new BasicDBObject();
			query.put(TpConstants.DEVICE_ID, deviceid);
			int count = persistenceService.getCount(MONGO_TABLES.users.name(), query, User.class);
			if (count > 0) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(TpConstants.NUMBER_OF_ALERT, numberOfAlert);
				map.put(TpConstants.DEVICE_TOKEN, deviceToken);
				map.put(TpConstants.MAX_WALK_DISTANCE, maxWalkDistance);
				persistenceService.updateMultiColumn(MONGO_TABLES.users.name(), TpConstants.DEVICE_ID, deviceid, map);
			} else {
				User user = new User();
				user.setDeviceId(deviceid);
				user.setNumberOfAlert(numberOfAlert);
				user.setDeviceToken(deviceToken);
				user.setMaxWalkDistance(maxWalkDistance);
				persistenceService.addObject(MONGO_TABLES.users.name(), user);
			}
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage());
		}
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setPersistenceService(PersistenceService persistenceService) {
		this.persistenceService = persistenceService;
	}

	public PersistenceService getPersistenceService() {
		return persistenceService;
	}

	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}

	public LoggingService getLogger() {
		return logger;
	}

}
