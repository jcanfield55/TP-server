/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.service;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.NimblerApps;
import com.nimbler.tp.dataobject.RequestMap;
import com.nimbler.tp.dbobject.User;
import com.nimbler.tp.mongo.MongoQueryConstant;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.DbUtils;
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
	 * @deprecated
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


	/**
	 * Save alert preferences.
	 *
	 * @param reqParam the req param
	 * @throws TpException the tp exception
	 */
	public void saveAlertPreferences(RequestMap req) throws TpException {
		try {
			int appType = req.getAppType();
			long time = System.currentTimeMillis();
			String deviceTocken = req.getString(RequestParam.DEVICE_TOKEN);

			BasicDBObject usr = new BasicDBObject();
			usr.put(TpConstants.APP_TYPE, appType);
			usr.put(TpConstants.NUMBER_OF_ALERT, req.getInt(RequestParam.ALERT,5));
			usr.put(TpConstants.DEVICE_ID, req.getString(RequestParam.DEVICE_ID));
			usr.put(TpConstants.MAX_WALK_DISTANCE, req.getDouble(RequestParam.MAX_DISTANCE));
			usr.put(TpConstants.DEVICE_TOKEN, deviceTocken);
			usr.put(RequestParam.APP_VERSION, req.getString(RequestParam.APP_VERSION,"-1"));

			usr.put(TpConstants.UPDATE_TIME, time);

			NimblerApps apps = BeanUtil.getNimblerAppsBean();
			Integer[] agencies = ComUtils.splitToIntArray(apps.getAppIdentifierToAgenciesMap().get(appType));

			if(agencies!=null)
				for (int i : agencies) {
					String enableColName = AGENCY_TYPE.values()[i].getEnableAdvisoryColumnName();
					usr.put(enableColName,req.getBoolean(enableColName,true));
				}
			usr.put(RequestParam.NOTIF_TIMING_MORNING,req.getBoolean(RequestParam.NOTIF_TIMING_MORNING,true));
			usr.put(RequestParam.NOTIF_TIMING_MIDDAY,req.getBoolean(RequestParam.NOTIF_TIMING_MIDDAY,false));
			usr.put(RequestParam.NOTIF_TIMING_EVENING,req.getBoolean(RequestParam.NOTIF_TIMING_EVENING,true));
			usr.put(RequestParam.NOTIF_TIMING_NIGHT,req.getBoolean(RequestParam.NOTIF_TIMING_NIGHT,false));
			usr.put(RequestParam.NOTIF_TIMING_WEEKEND,req.getBoolean(RequestParam.NOTIF_TIMING_WEEKEND,false));

			usr.put(RequestParam.ENABLE_STD_NOTIFICATION, req.getBoolean(RequestParam.ENABLE_STD_NOTIFICATION,false));
			usr.put(RequestParam.ENABLE_URGENT_NOTIFICATION,req.getBoolean(RequestParam.ENABLE_URGENT_NOTIFICATION,true));

			usr.put(RequestParam.TRANSIT_MODE,req.getInt(RequestParam.TRANSIT_MODE,User.TRANSIT_MODE.TRANSIT_WALK.ordinal()));
			usr.put(RequestParam.MAX_BIKE_DISTANCE,req.getDouble(RequestParam.MAX_BIKE_DISTANCE));
			usr.put(RequestParam.BIKE_TRIANGLE_BIKEFRIENDLY,(req.getDouble(RequestParam.BIKE_TRIANGLE_BIKEFRIENDLY)));
			usr.put(RequestParam.BIKE_TRIANGLE_FLAT, req.getDouble(RequestParam.BIKE_TRIANGLE_FLAT));
			usr.put(RequestParam.BIKE_TRIANGLE_QUICK, req.getDouble(RequestParam.BIKE_TRIANGLE_QUICK));

			BasicDBObject query =DbUtils.getDefaultAppFilterQuery(appType, deviceTocken);

			int count = persistenceService.getCount(MONGO_TABLES.users.name(), query, User.class);
			if (count>0) {
				persistenceService.update(MONGO_TABLES.users.name(),query, usr);
			} else {
				if(agencies!=null){
					for (Integer ag : agencies) {
						AGENCY_TYPE agency = AGENCY_TYPE.values()[ag];
						usr.put(agency.getLastReadTimeColumnName(), 0L);
						usr.put(agency.getPushTimeColumnName(), 0L);
					}
				}
				persistenceService.addDbObject(MONGO_TABLES.users.name(), usr) ;
			}
		} catch (NumberFormatException e) {
			logger.warn(loggerName,"NumberFormatException:"+ e.getMessage());
			throw new TpException(TP_CODES.INVALID_REQUEST);
		} catch (DBException e) {
			logger.warn(loggerName,"DBException:"+ e.getMessage());
			throw new TpException(TP_CODES.FAIL);
		}
	}

	/**
	 * @throws DBException 
	 * @return 
	 * @throws TpException 
	 * Update token.
	 *
	 * @param deviceToken the device token
	 * @param dummyId the dummy id
	 * @throws  
	 */
	public int updateToken(String deviceToken, String dummyId,int appType) throws TpException, DBException {		
		BasicDBObject query = new BasicDBObject();			
		query.put(TpConstants.DEVICE_TOKEN,deviceToken);
		query.put(TpConstants.APP_TYPE, appType);
		int count = persistenceService.getCount(MONGO_TABLES.users.name(), query, User.class);
		if(count>0){
			persistenceService.deleteUser(deviceToken,appType);
			logger.info(loggerName, "deleting early registered device token :"+deviceToken + "to replace dummy id: "+dummyId);
			//System.out.println( "deleting early registered device token :"+deviceToken + "to replace dummy id: "+dummyId);
		}
		query = new BasicDBObject();			
		query.put(TpConstants.DEVICE_TOKEN, dummyId);
		query.put(TpConstants.APP_TYPE, appType);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put(TpConstants.DEVICE_TOKEN,deviceToken);		
		map.put(TpConstants.UPDATE_TIME, System.currentTimeMillis());
		int updateCount = persistenceService.update(MONGO_TABLES.users.name(), query , map,false);
		//System.out.println("update: "+updateCount);
		return updateCount;
	}

	/**
	 * Gets the user by device token.
	 *
	 * @param deviceTockens the device tockens
	 * @return the user by device token
	 */
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