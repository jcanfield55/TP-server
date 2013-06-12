package com.nimbler.tp.service.advisories;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.document.mongodb.query.BasicQuery;

import com.mongodb.BasicDBObject;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.Tweet;
import com.nimbler.tp.dataobject.TweetResponse;
import com.nimbler.tp.dbobject.User;
import com.nimbler.tp.mongo.MongoQueryConstant;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.twitter.TweetStore;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;
import com.nimbler.tp.util.TpException;
/**
 * 
 * @author nIKUNJ
 *
 */
public class TwitterAdvisoriesService implements AdvisoriesService {

	@Autowired
	private LoggingService logger;

	private String loggerName;

	@Override
	public TweetResponse getAdvisoryCount(String deviceid, String deviceToken, int appType, int[] agencyIds) {
		int tweetCount =0;
		TweetResponse response = new TweetResponse();
		try {
			User dbUser = null;
			if (!ComUtils.isEmptyString(deviceid)) {
				dbUser = getUserByDeviceID(deviceid);//for Old Caltrain app 
			} else {
				dbUser = getUserByDeviceToken(deviceToken, appType);//for new SF app 
			}
			if (dbUser!=null) {
				List<Tweet> allTweets = new ArrayList<Tweet>();
				for (int agency: agencyIds) {
					//Integer[] enabledAgencies = user.getEnabledAgencies();
					long lastReadTime = 0;;
					if (appType == NIMBLER_APP_TYPE.CALTRAIN.ordinal()) {
						lastReadTime = dbUser.getLastAlertTime();
					} else {
						lastReadTime = getAdvisoryLastReadTime(dbUser, agency); 
					}
					List<Tweet> tweets = TweetStore.getInstance().getTweets(agency);					
					if (tweets != null && tweets.size()>0) {
						allTweets.addAll(tweets); 
						for (Tweet tweet : tweets) {						
							if (tweet.getTime() > lastReadTime)
								tweetCount++;
						}
					}
				}
				if (tweetCount >0) {
					if (tweetCount==allTweets.size())
						response.setAllNew(true);
				}
			} else
				logger.debug(loggerName, "No device found in database. DeviceId: "+deviceid+" DeviceToken: "+deviceToken);

		} catch (DBException e) {
			logger.error(loggerName, e.getErrMsg());
			response.setErrCode(TP_CODES.FAIL.getCode()); 
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setErrCode(TP_CODES.FAIL.getCode()); 
		}
		response.setTweetCount(tweetCount);		
		return response;
	}


	@Override
	public TweetResponse getAllAdvisories(String deviceid, String deviceToken, int appType, int[] agencyIds) {
		TweetResponse response = new TweetResponse();
		try {
			List<Tweet> allTweet = new ArrayList<Tweet>();
			List<Tweet> generalTweets = TweetStore.getInstance().getTweets(agencyIds);
			if (generalTweets !=null && generalTweets.size()>0)
				allTweet.addAll(generalTweets);

			List<Tweet> urgentTweets = TweetStore.getInstance().getUrgentTweets(agencyIds);
			if (urgentTweets !=null && urgentTweets.size()>0)
				allTweet.addAll(urgentTweets);

			if (allTweet.size()==0) {
				logger.debug(loggerName, "No Tweets are available.");
				response.setError(TP_CODES.FAIL.getCode());
				throw new TpException(TP_CODES.DATA_NOT_EXIST.getCode());
			}
			Collections.sort(allTweet, new Comparator<Tweet>() {
				@Override
				public int compare(Tweet o1, Tweet o2) {
					return ((o1.getTime() > o2.getTime()) ? -1 : (o1.getTime() == o2.getTime()) ? 0 : 1);
				}
			});
			response.setTweet(allTweet);
			response.setTweetCount(generalTweets.size());

			long lastReadTime = allTweet.get(0).getTime();
			Map<String, Object> columnsToUpdate = new HashMap<String, Object>();
			if (appType == NIMBLER_APP_TYPE.CALTRAIN.ordinal()) {
				columnsToUpdate.put(TpConstants.LAST_ALERT_TIME, lastReadTime);					
				columnsToUpdate.put(TpConstants.LAST_PUSH_TIME, lastReadTime);
			}
			for (int agency: agencyIds) {
				columnsToUpdate.put(AGENCY_TYPE.values()[agency].getPushTimeColumnName(), lastReadTime);
				columnsToUpdate.put(AGENCY_TYPE.values()[agency].getLastReadTimeColumnName(), lastReadTime);
			}
			if (!ComUtils.isEmptyString(deviceid)) {
				PersistenceService persistenceService = BeanUtil.getPersistanceService();
				persistenceService.updateMultiColumn(MONGO_TABLES.users.name(), TpConstants.DEVICE_ID, deviceid, columnsToUpdate);
			} else {
				updateUserByDeviceToken(deviceToken, appType, columnsToUpdate);  
			}
		} catch (TpException tpe) {
			logger.debug(loggerName, tpe.getErrMsg());
			response.setError(tpe.getErrCode());
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setError(TP_CODES.FAIL.getCode());
		}
		return response;
	}

	@Override
	public TweetResponse getAdvisoriesAfterTime(String deviceid, String deviceToken, long lastAdvisoryTime, int appType, int[] agencyIds) {
		TweetResponse response = new TweetResponse();
		try {
			List<Tweet> tweets = TweetStore.getInstance().getTweets(agencyIds);
			if(tweets == null || tweets.size() == 0) {
				logger.debug(loggerName, "No new Tweets are available at this time."); 
				response.setError(TP_CODES.DATA_NOT_EXIST.getCode());
				throw new TpException(TP_CODES.DATA_NOT_EXIST.getCode());
			}
			List<Tweet> latestTweets = new ArrayList<Tweet>();
			for (Tweet tweet: tweets) {
				if (tweet.getTime() > lastAdvisoryTime) {
					latestTweets.add(tweet);
				}
			}
			if (latestTweets.size()>0) {
				response.setTweet(latestTweets);

				long lastReadTime = latestTweets.get(0).getTime();
				Map<String, Object> columnsToUpdate = new HashMap<String, Object>();
				if (appType == NIMBLER_APP_TYPE.CALTRAIN.ordinal()) {
					columnsToUpdate.put(TpConstants.LAST_ALERT_TIME, lastReadTime);					
					columnsToUpdate.put(TpConstants.LAST_PUSH_TIME, lastReadTime);
				}
				for (int agency: agencyIds) {
					columnsToUpdate.put(AGENCY_TYPE.values()[agency].getPushTimeColumnName(), lastReadTime);
					columnsToUpdate.put(AGENCY_TYPE.values()[agency].getLastReadTimeColumnName(), lastReadTime);
				}					
				if (!ComUtils.isEmptyString(deviceid)) {
					PersistenceService persistenceService = BeanUtil.getPersistanceService();								
					persistenceService.updateMultiColumn(MONGO_TABLES.users.name(), TpConstants.DEVICE_ID, deviceid, columnsToUpdate);				
				} else {
					updateUserByDeviceToken(deviceToken, appType, columnsToUpdate);  
				}
			}			
			response.setTweetCount(latestTweets.size());
		} catch (TpException tpe) {
			logger.debug(loggerName, tpe.getErrMsg());
			response.setError(tpe.getErrCode());
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setError(TP_CODES.FAIL.getCode());
		}
		return response;
	}
	/**
	 * 
	 * @param user
	 * @param agency
	 * @return
	 */
	private long getAdvisoryLastReadTime(User user, int agency) {
		if (agency == AGENCY_TYPE.CALTRAIN.ordinal()) {
			return user.getLastReadTimeCaltrain();
		} else if (agency == AGENCY_TYPE.BART.ordinal()) {
			return user.getLastReadTimeBart();
		} else if (agency == AGENCY_TYPE.AC_TRANSIT.ordinal()) {
			return user.getLastReadTimeAct();
		} else if (agency == AGENCY_TYPE.SFMUNI.ordinal()) {
			return user.getLastReadTimeSfMuni();
		} else {//dynamic for all other
			try {
				String strMethod = AGENCY_TYPE.values()[agency].getLastReadTimeColumnName();
				strMethod = StringUtils.capitalize(strMethod);
				Method method = user.getClass().getMethod("get"+strMethod);
				Object val = method.invoke(user);
				if(val!=null)
					return (Long)val;
			} catch (Exception e) {
				logger.error(loggerName, "user:"+ user + ", agency: \" + agency",e);
			}
		}
		return 0;
	}
	/**
	 * 
	 * @param deviceToken
	 * @param appType
	 * @return
	 * @throws DBException 
	 */
	private User getUserByDeviceToken(String deviceToken, int appType) throws DBException {
		User user  = null;
		PersistenceService persistenceService = BeanUtil.getPersistanceService();
		BasicDBObject queryObj = new BasicDBObject();
		if (appType == NIMBLER_APP_TYPE.CALTRAIN.ordinal()) {
			queryObj.put(TpConstants.APP_TYPE, new BasicDBObject(MongoQueryConstant.IN, new Object[]{null,NIMBLER_APP_TYPE.CALTRAIN.ordinal()}));
		} else { 
			queryObj.put(TpConstants.APP_TYPE, appType);
		}		
		queryObj.put(TpConstants.DEVICE_TOKEN, deviceToken);
		BasicQuery basicQuery = new BasicQuery(queryObj);		
		List<User> resultSet = persistenceService.findByQuery(MONGO_TABLES.users.name(),basicQuery,User.class);
		if (resultSet!=null && resultSet.size()>0)
			user = resultSet.get(0);
		return user;		
	}
	/**
	 * 
	 * @param deviceId
	 * @return
	 * @throws DBException
	 */
	private User getUserByDeviceID(String deviceId) throws DBException {
		PersistenceService persistenceService = BeanUtil.getPersistanceService();
		return (User) persistenceService.findOne(MONGO_TABLES.users.name(), TpConstants.DEVICE_ID, deviceId, User.class);
	}
	/**
	 * 
	 * @param deviceToken
	 * @param appType
	 * @param columnsToUpdate
	 * @throws DBException
	 */
	private void updateUserByDeviceToken(String deviceToken, int appType, Map<String, Object> columnsToUpdate) throws DBException {
		PersistenceService persistenceService = BeanUtil.getPersistanceService();
		BasicDBObject queryObj = new BasicDBObject();
		if (appType == NIMBLER_APP_TYPE.CALTRAIN.ordinal()) {
			queryObj.put(TpConstants.APP_TYPE, new BasicDBObject(MongoQueryConstant.IN, new Object[]{null,NIMBLER_APP_TYPE.CALTRAIN.ordinal()}));
		} else { 
			queryObj.put(TpConstants.APP_TYPE, appType);
		}		
		queryObj.put(TpConstants.DEVICE_TOKEN, deviceToken);
		persistenceService.update(MONGO_TABLES.users.name(), queryObj, columnsToUpdate);		
	}

	public LoggingService getLogger() {
		return logger;
	}

	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
}