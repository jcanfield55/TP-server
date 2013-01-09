package com.nimbler.tp.util;

import java.util.List;

import com.mongodb.BasicDBObject;
import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dbobject.NimblerParams;
import com.nimbler.tp.dbobject.NimblerParams.NIMBLER_PARAMS;
import com.nimbler.tp.mongo.MongoQueryConstant;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;

public class PersistantHelper {
	private LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private static String loggerName = "com.nimbler.tp.mongo.PersistenceService";
	private PersistenceService persistenceService = BeanUtil.getPersistanceService();
	private final int FIXED_TIME = -1;

	public String getTestUserDeviceTokens() {
		String deviceTokens = "";
		try {
			List<NimblerParams> resultSet = (List<NimblerParams>) persistenceService.find(MONGO_TABLES.nimbler_params.name(),TpConstants.NIMBLER_PARAMS_NAME, 
					NIMBLER_PARAMS.BETA_USERS.name(),NimblerParams.class);
			if (resultSet != null && resultSet.size() > 0){
				deviceTokens =  resultSet.get(0).getValue().trim();
			}
		} catch (Exception e) {
			logger.error(loggerName, e.getMessage());
		} 
		return deviceTokens;
	}
	/**
	 * get total user  count
	 * @return count
	 */
	public int getTotalUserCount() {
		return getCount(MONGO_TABLES.users.name(), FIXED_TIME);
	}
	/**
	 * get total count for plan
	 * @return count
	 */
	public int getTotalPlanCount() {
		return getCount(MONGO_TABLES.plan.name(), FIXED_TIME);
	}
	/**
	 * get total feedback user count
	 * @return count
	 */
	public int getTotalFeedbackCount() {
		return getCount(MONGO_TABLES.feedback.name(), FIXED_TIME);
	}
	/**
	 * get total itinerary count
	 * @return count
	 */
	public int getTotalItineraryCount() {
		return getCount(MONGO_TABLES.itinerary.name(), FIXED_TIME);
	}
	/**
	 * get total leg count
	 * @return
	 */
	public int getTotalLegCount() {
		return getCount(MONGO_TABLES.leg.name(), FIXED_TIME);
	}

	/**
	 * get last 24 hour user count
	 * @return
	 */
	public int getLast24hourUserCount() {
		return getCount(MONGO_TABLES.users.name(), getTimeInMilliSecond());
	}
	/**
	 * get last 24 hour feedback count
	 * @return
	 */
	public int getLast24hourFeebackCount() {
		return getCount(MONGO_TABLES.feedback.name(), getTimeInMilliSecond());	
	}

	/**
	 * get last 24 hour plan count
	 * @return
	 */
	public int getLast24hourPlanCount() {
		return getCount(MONGO_TABLES.plan.name(), getTimeInMilliSecond());	
	}
	/**
	 * get last 24 hour upadate count
	 * @return
	 */
	public int getLast24hourUserUpdateCount() {
		int count=0;
		try {
			long time = getTimeInMilliSecond();
			BasicDBObject query = new BasicDBObject();
			query.put(TpConstants.UPDATE_TIME, new BasicDBObject(MongoQueryConstant.GREATER_THAN,time));
			count = persistenceService.getCount(MONGO_TABLES.users.name(), query, null);
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage());
		}
		return count;
	}

	/**
	 * get count 
	 * @param collectionName
	 * @return
	 */
	private int getCount(String collectionName, long time) {
		int count=0;
		try {
			if (time < 0){
				count = persistenceService.getRowCount(collectionName);
			} else {
				BasicDBObject query = new BasicDBObject();
				query.put(TpConstants.CREATE_TIME, new BasicDBObject(MongoQueryConstant.GREATER_THAN,time));
				count = persistenceService.getCount(collectionName, query, null);
			}
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage());
		}
		return count;
	}

	/**
	 * get last 24 hour time
	 * @return
	 */
	private long getTimeInMilliSecond() {
		long currentTime = System.currentTimeMillis();
		long lastTime = 24*60*60*1000;//24 hour
		return currentTime - lastTime;
	}
}
