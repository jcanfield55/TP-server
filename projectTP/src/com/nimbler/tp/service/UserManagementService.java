/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd.
 * All rights reserved.
 *
 */
package com.nimbler.tp.service;

import static com.nimbler.tp.util.RequestParam.BIKE_TRIANGLE_BIKEFRIENDLY;
import static com.nimbler.tp.util.RequestParam.BIKE_TRIANGLE_FLAT;
import static com.nimbler.tp.util.RequestParam.BIKE_TRIANGLE_QUICK;
import static com.nimbler.tp.util.RequestParam.DEVICE_ID;
import static com.nimbler.tp.util.RequestParam.DEVICE_TOKEN;
import static com.nimbler.tp.util.RequestParam.ENABLE_STD_NOTIFICATION;
import static com.nimbler.tp.util.RequestParam.ENABLE_URGENT_NOTIFICATION;
import static com.nimbler.tp.util.RequestParam.MAX_BIKE_DISTANCE;
import static com.nimbler.tp.util.RequestParam.NOTIF_TIMING_EVENING;
import static com.nimbler.tp.util.RequestParam.NOTIF_TIMING_MIDDAY;
import static com.nimbler.tp.util.RequestParam.NOTIF_TIMING_MORNING;
import static com.nimbler.tp.util.RequestParam.NOTIF_TIMING_NIGHT;
import static com.nimbler.tp.util.RequestParam.NOTIF_TIMING_WEEKEND;
import static com.nimbler.tp.util.RequestParam.TRANSIT_MODE;
import static com.nimbler.tp.util.TpConstants.APP_TYPE;
import static com.nimbler.tp.util.TpConstants.CREATE_TIME;
import static com.nimbler.tp.util.TpConstants.MAX_WALK_DISTANCE;
import static com.nimbler.tp.util.TpConstants.NUMBER_OF_ALERT;
import static com.nimbler.tp.util.TpConstants.UPDATE_TIME;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.NimblerApps;
import com.nimbler.tp.dbobject.User;
import com.nimbler.tp.dbobject.User.BOOLEAN_VAL;
import com.nimbler.tp.mongo.MongoQueryConstant;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;
import com.nimbler.tp.util.TpException;
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
	 * Save alert preferences.
	 *
	 * @param usr the req user value
	 */
	public void saveAlertPreferences(User usr) {
		try {
			if (usr.getAppType() == 0)
				usr.setAppType(NIMBLER_APP_TYPE.CALTRAIN.ordinal());
			BasicDBObject query = new BasicDBObject();
			query.put(TpConstants.DEVICE_TOKEN, usr.getDeviceToken());
			if(usr.getAppType() == NIMBLER_APP_TYPE.CALTRAIN.ordinal())
				query.put(TpConstants.APP_TYPE, new BasicDBObject(MongoQueryConstant.IN, new Object[]{null,NIMBLER_APP_TYPE.CALTRAIN.ordinal()}));
			else
				query.put(TpConstants.APP_TYPE, usr.getAppType());
			int count = persistenceService.getCount(MONGO_TABLES.users.name(), query, User.class);
			long time = System.currentTimeMillis();
			if (count > 0) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(TpConstants.NUMBER_OF_ALERT, usr.getNumberOfAlert());
				map.put(TpConstants.DEVICE_ID, usr.getDeviceId());
				map.put(TpConstants.MAX_WALK_DISTANCE, usr.getMaxWalkDistance());
				map.put(TpConstants.UPDATE_TIME, time);

				map.put(RequestParam.ADV_ENABLE_AC_TRANSIT,usr.getEnableAcTransitAdv());
				map.put(RequestParam.ADV_ENABLE_BART,usr.getEnableBartAdv());
				map.put(RequestParam.ADV_ENABLE_CALTRAIN,usr.getEnableCaltrainAdv());
				map.put(RequestParam.ADV_ENABLE_SF_MUNI,usr.getEnableSfMuniAdv());
				map.put(RequestParam.ADV_ENABLE_WMATA,usr.getEnableWmataAdv());

				map.put(RequestParam.NOTIF_TIMING_MORNING,usr.getNotifTimingMorning());
				map.put(RequestParam.NOTIF_TIMING_MIDDAY,usr.getNotifTimingMidday());
				map.put(RequestParam.NOTIF_TIMING_EVENING,usr.getNotifTimingEvening());
				map.put(RequestParam.NOTIF_TIMING_NIGHT,usr.getNotifTimingNight());
				map.put(RequestParam.NOTIF_TIMING_WEEKEND,usr.getNotifTimingWeekend());
				map.put(RequestParam.TRANSIT_MODE,usr.getTransitMode());
				map.put(RequestParam.MAX_BIKE_DISTANCE,usr.getMaxBikeDist());

				map.put(TpConstants.APP_TYPE, usr.getAppType());
				map.put(RequestParam.ENABLE_STD_NOTIFICATION, usr.getEnableStdNotifSound());
				map.put(RequestParam.ENABLE_URGENT_NOTIFICATION, usr.getEnableUrgntNotifSound());

				map.put(RequestParam.BIKE_TRIANGLE_BIKEFRIENDLY, usr.getBikeTriangleBikeFriendly());
				map.put(RequestParam.BIKE_TRIANGLE_FLAT, usr.getBikeTriangleFlat());
				map.put(RequestParam.BIKE_TRIANGLE_QUICK, usr.getBikeTriangleQuick());
				persistenceService.update(MONGO_TABLES.users.name(), query , map);
			} else {
				usr.setCreateTime(time);
				usr.setUpdateTime(time);
				persistenceService.addObject(MONGO_TABLES.users.name(), usr);
			}
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage());
		}
	}

	public void saveAlertPreferences(Map<String,String> reqParam) throws TpException {
		try {
			int appType = NumberUtils.toInt(reqParam.get(RequestParam.NIMBLER_APP_TYPE),NIMBLER_APP_TYPE.CALTRAIN.ordinal());
			String deviceTocken = reqParam.get(RequestParam.DEVICE_TOKEN);

			BasicDBObject usr = new BasicDBObject();
			usr.put(APP_TYPE, appType);
			usr.put(NUMBER_OF_ALERT, NumberUtils.toInt(reqParam.get(NUMBER_OF_ALERT),5));
			usr.put(DEVICE_ID, reqParam.get(DEVICE_ID));
			usr.put(MAX_WALK_DISTANCE, NumberUtils.toDouble(reqParam.get(MAX_WALK_DISTANCE)));
			usr.put(DEVICE_TOKEN, deviceTocken);

			long time = System.currentTimeMillis();
			usr.put(UPDATE_TIME, time);

			NimblerApps apps = BeanUtil.getNimblerAppsBean();
			String[] strAppIdentifiers = apps.getAppIdentifierToAgenciesMap().get(appType).split(",");
			int[] agencies = new int[strAppIdentifiers.length];
			for (int i = 0; i < strAppIdentifiers.length; i++) {
				agencies[i] = NumberUtils.toInt(strAppIdentifiers[i]);
			}
			for (int i : agencies) {
				String enableColName = AGENCY_TYPE.values()[i].getEnableAdvisoryColumnName();
				usr.put(enableColName,Integer.parseInt(reqParam.get(enableColName)));
			}
			usr.put(NOTIF_TIMING_MORNING,NumberUtils.toInt(reqParam.get(NOTIF_TIMING_MORNING),BOOLEAN_VAL.TRUE.ordinal()));
			usr.put(NOTIF_TIMING_MIDDAY,NumberUtils.toInt(reqParam.get(NOTIF_TIMING_MIDDAY),BOOLEAN_VAL.FALSE.ordinal()));
			usr.put(NOTIF_TIMING_EVENING,NumberUtils.toInt(reqParam.get(NOTIF_TIMING_EVENING),BOOLEAN_VAL.TRUE.ordinal()));
			usr.put(NOTIF_TIMING_NIGHT,NumberUtils.toInt(reqParam.get(NOTIF_TIMING_NIGHT),BOOLEAN_VAL.FALSE.ordinal()));
			usr.put(NOTIF_TIMING_WEEKEND,NumberUtils.toInt(reqParam.get(NOTIF_TIMING_WEEKEND),BOOLEAN_VAL.FALSE.ordinal()));

			usr.put(ENABLE_STD_NOTIFICATION, NumberUtils.toInt(reqParam.get(ENABLE_STD_NOTIFICATION),BOOLEAN_VAL.FALSE.ordinal()));
			usr.put(ENABLE_URGENT_NOTIFICATION, NumberUtils.toInt(reqParam.get(ENABLE_URGENT_NOTIFICATION),BOOLEAN_VAL.TRUE.ordinal()));

			usr.put(TRANSIT_MODE,NumberUtils.toInt(reqParam.get(TRANSIT_MODE),User.TRANSIT_MODE.TRANSIT_WALK.ordinal()));
			usr.put(MAX_BIKE_DISTANCE,NumberUtils.toDouble(reqParam.get(MAX_BIKE_DISTANCE)));
			usr.put(BIKE_TRIANGLE_BIKEFRIENDLY,NumberUtils.toDouble(reqParam.get(BIKE_TRIANGLE_BIKEFRIENDLY)));
			usr.put(BIKE_TRIANGLE_FLAT, NumberUtils.toDouble(reqParam.get(BIKE_TRIANGLE_FLAT)));
			usr.put(BIKE_TRIANGLE_QUICK, NumberUtils.toDouble(reqParam.get(BIKE_TRIANGLE_QUICK)));

			BasicDBObject query = new BasicDBObject();
			query.put(TpConstants.DEVICE_TOKEN, deviceTocken);
			if(appType == NIMBLER_APP_TYPE.CALTRAIN.ordinal())
				query.put(TpConstants.APP_TYPE, new BasicDBObject(MongoQueryConstant.IN, new Object[]{null,NIMBLER_APP_TYPE.CALTRAIN.ordinal()}));//handle caltrain app
			else
				query.put(TpConstants.APP_TYPE, appType);
			int count = persistenceService.getCount(MONGO_TABLES.users.name(), query, User.class);
			if (count>0) {
				persistenceService.update(MONGO_TABLES.users.name(),query, usr);
			} else {
				usr.put(CREATE_TIME, time);
				persistenceService.addDbObject(MONGO_TABLES.users.name(), usr);
			}
		} catch (NumberFormatException e) {
			logger.warn(loggerName,"NumberFormatException:"+ e.getMessage());
			throw new TpException(TP_CODES.INVALID_REQUEST);
		} catch (DBException e) {
			logger.warn(loggerName,"DBException:"+ e.getMessage());
			throw new TpException(TP_CODES.FAIL);
		}
	}
	public User getUserByDeviceToken(String deviceTockens) {
		User res = null;
		try {
			List objRes =  persistenceService.find(MONGO_TABLES.users.name(), TpConstants.DEVICE_TOKEN, deviceTockens, User.class);
			if(!ComUtils.isEmptyList(objRes))
				res = (User) objRes.get(0);
		} catch (DBException e) {
			logger.error(loggerName, e);
		}
		return res;
	}

	/**
	 * 
	 * @param deviceid
	 * @param numberOfAlert
	 * @param deviceToken
	 * @param maxWalkDistance
	 * @param enableUrgntNot
	 * @param enableStdNot
	 * @deprecated
	 */
	public void saveAlertPreferences(String deviceid, int numberOfAlert, String deviceToken, String maxWalkDistance, int enableStdNot, int enableUrgntNot,
			int appType) {
		try {
			BasicDBObject query = new BasicDBObject();
			query.put(TpConstants.DEVICE_TOKEN, deviceToken);
			int count = persistenceService.getCount(MONGO_TABLES.users.name(), query, User.class);
			long time = System.currentTimeMillis();
			if (appType == 0)
				appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal();
			if (count > 0) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(TpConstants.NUMBER_OF_ALERT, numberOfAlert);
				map.put(TpConstants.DEVICE_ID, deviceid);
				map.put(TpConstants.MAX_WALK_DISTANCE, maxWalkDistance);
				map.put(TpConstants.UPDATE_TIME, time);
				map.put(TpConstants.APP_TYPE, appType);
				if (enableStdNot!=0)
					map.put(RequestParam.ENABLE_STD_NOTIFICATION, enableStdNot);
				if (enableUrgntNot!=0)
					map.put(RequestParam.ENABLE_URGENT_NOTIFICATION, enableUrgntNot);
				persistenceService.updateMultiColumn(MONGO_TABLES.users.name(), TpConstants.DEVICE_TOKEN, deviceToken, map);
			} else {
				User user = new User();
				user.setDeviceId(deviceid);
				user.setNumberOfAlert(numberOfAlert);
				user.setDeviceToken(deviceToken);
				user.setMaxWalkDistance(maxWalkDistance);
				user.setCreateTime(time);
				user.setUpdateTime(time);
				user.setEnableStdNotifSound(enableStdNot);
				user.setEnableUrgntNotifSound(enableUrgntNot);
				user.setAppType(appType);
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