/*
 * @author nirmal
 */
package com.nimbler.tp.util;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import com.mongodb.BasicDBObject;
import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.UserStatistics;
import com.nimbler.tp.dbobject.NimblerParams;
import com.nimbler.tp.dbobject.NimblerParams.NIMBLER_PARAMS;
import com.nimbler.tp.dbobject.User.BOOLEAN_VAL;
import com.nimbler.tp.mongo.MongoQueryConstant;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;
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
	 * Gets the user statistics.
	 *
	 * @param appType the app type
	 * @return the user statistics
	 */
	public UserStatistics getUserStatistics(int appType) {
		UserStatistics statistics = new UserStatistics();
		statistics.setAppType(appType);

		BasicDBObject query = new BasicDBObject();
		query.put(TpConstants.APP_TYPE, appType);
		statistics.setTotal(getUserCount(query));

		long lastDay = DateUtils.addHours(new Date(), -24).getTime();
		long lastWeek = DateUtils.addDays(new Date(), -7).getTime();
		long lastMonth = DateUtils.addDays(new Date(), -30).getTime();

		statistics.setCreateInLast24(getUserAfterTime(appType,lastDay,TpConstants.CREATE_TIME));
		statistics.setCreateInLastWeek(getUserAfterTime(appType,lastWeek,TpConstants.CREATE_TIME));
		statistics.setCreateInLastMonth(getUserAfterTime(appType,lastMonth,TpConstants.CREATE_TIME));

		statistics.setUpdateInLast24(getUserAfterTime(appType,lastDay,TpConstants.UPDATE_TIME));
		statistics.setUpdateInLastWeek(getUserAfterTime(appType,lastWeek,TpConstants.UPDATE_TIME));
		statistics.setUpdateInLastMonth(getUserAfterTime(appType,lastMonth,TpConstants.UPDATE_TIME));

		statistics.setInvalid(getUserWithAlertCount(appType,-3));
		statistics.setDisabledPush(getUserWithAlertCount(appType,-1));
		statistics.setSubscribedForEveryPush(getUserWithAlertCount(appType,1));
		statistics.setSubscribedRarePush(getUserWithAlertCount(appType,10));
		statistics.setUninstalled(getUserWithAlertCount(appType,-2));

		statistics.setUsingAcTransitAdv(getUserWithFilter(appType, AGENCY_TYPE.AC_TRANSIT.getEnableAdvisoryColumnName(), BOOLEAN_VAL.TRUE.ordinal()));
		statistics.setUsingBartAdv(getUserWithFilter(appType, AGENCY_TYPE.BART.getEnableAdvisoryColumnName(), BOOLEAN_VAL.TRUE.ordinal()));
		statistics.setUsingMuniAdv(getUserWithFilter(appType, AGENCY_TYPE.SFMUNI.getEnableAdvisoryColumnName(), BOOLEAN_VAL.TRUE.ordinal()));
		statistics.setUsingCaltrainAdv(getUserWithFilter(appType, AGENCY_TYPE.CALTRAIN.getEnableAdvisoryColumnName(), BOOLEAN_VAL.TRUE.ordinal()));

		statistics.setTotalPlan(getPlanCount(appType, null));
		statistics.setPlanInLast24(getPlanCount(appType, lastDay));
		statistics.setPlanInLastWeek(getPlanCount(appType, lastWeek));
		statistics.setPlanInLastMonth(getPlanCount(appType, lastMonth));
		return statistics;
	}

	/**
	 * Gets the user with alert count.
	 *
	 * @param apptype the apptype
	 * @param alertCount the alert count
	 * @return the user with alert count
	 */
	private Integer getUserWithAlertCount(int apptype, int alertCount) {
		return getUserWithFilter(apptype,TpConstants.NUMBER_OF_ALERT,alertCount);
	}

	private Integer getUserWithFilter(int apptype, String column,int value) {
		BasicDBObject query = new BasicDBObject();
		query.put(TpConstants.APP_TYPE, apptype);
		query.put(column, value);
		return getUserCount(query);
	}

	/**
	 * Gets the user after time.
	 *
	 * @param appType the app type
	 * @param time the time
	 * @param column the column
	 * @return the user after time
	 */
	private Integer getUserAfterTime(int appType, long time,String column) {
		BasicDBObject query = new BasicDBObject();
		query.put(TpConstants.APP_TYPE, appType);
		query.put(column, new BasicDBObject(MongoQueryConstant.GREATER_THAN,time));
		return getUserCount(query);		
	}
	private int getUserCount(BasicDBObject query) {
		try {
			return persistenceService.getCount(MONGO_TABLES.users.name(), query, null);
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
		return -1;
	}

	/**
	 * Gets the plan count.
	 *
	 * @param apptype the apptype
	 * @param time the time
	 * @return the plan count
	 */
	private int getPlanCount(int apptype, Long time) {
		try {
			BasicDBObject query = new BasicDBObject();
			query.put(TpConstants.APP_TYPE, apptype);
			if(time!=null)
				query.put(TpConstants.CREATE_TIME, new BasicDBObject(MongoQueryConstant.GREATER_THAN,time));
			return persistenceService.getCount(MONGO_TABLES.plan.name(), query, null);
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
		return -1;
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
