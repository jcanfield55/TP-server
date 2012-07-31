/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.service.twitter;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jayway.jsonpath.JsonPath;
import com.mongodb.BasicDBObject;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.Tweet;
import com.nimbler.tp.dbobject.User;
import com.nimbler.tp.mongo.MongoQueryConstant;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.APNService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.HttpUtils;
import com.nimbler.tp.util.StatusMsgConfig;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
import com.nimbler.tp.util.TpException;
/**
 * 
 * @author suresh
 *
 */
public class CaltrainAdvisoriesService {

	@Autowired
	private LoggingService logger;

	@Autowired
	private PersistenceService persistenceService;

	@Autowired
	private APNService apnService;

	private String loggerName;

	private String timeIntervalInMin;

	private String tweetUrl;

	private List<String> tweetSources;

	private int maxAlertThreshhold;	

	public void init() {
		//moved to quartz scheduling, look ApplicationContext.xml -nikunj
		/*try { 
			new CaltrainAdvisoriesTask().schedule(timeIntervalInMin);
		} catch (Exception e) {
			logger.error(loggerName, e);
		}*/
		maxAlertThreshhold = NumberUtils.toInt(TpConstants.TWEET_MAX_COUNT, 6);
	}
	/**
	 * 
	 * @throws TpException
	 */
	public void getLatestTweets() {
		try {
			if (tweetSources ==null || tweetSources.size() == 0) 
				throw new TpException("No default tweet source found.");
			List<String> list = new ArrayList<String>();
			for (String tweetResource : tweetSources) { 
				list.add("from:"+tweetResource);
			}
			String queryParam = StringUtils.join(list,"+OR+")+ " since:" + ComUtils.getFormatedDate("yyyy-MM-dd");
			String response = getTwitterResponse(queryParam);
			List<Tweet> tweetList = getTweets(response);
			TweetStore.getInstance().setTweet(tweetList);

			long timeDiff = NumberUtils.toLong(TpConstants.TWEET_TIME_DIFF)*60*60*1000;
			//long timeDiff = 5 * 60 * 1000;
			long lastTimeLeg = System.currentTimeMillis() - timeDiff;
			int latestTweetCount = 0;
			for (Tweet tweet : tweetList) {
				if (tweet.getTime() >= lastTimeLeg) {
					latestTweetCount++;
				}
			}
			if (latestTweetCount>0) {
				for (int i=0;i<latestTweetCount;i++) {
					Tweet tweet = tweetList.get(i); 
					if (i<maxAlertThreshhold)
						publishTweets(tweet.getTime(), i+1, lastTimeLeg);
					else {
						publishTweets(tweet.getTime(), latestTweetCount, lastTimeLeg);
						break;
					}
				}
			}
		} catch (TpException e) {
			logger.error(loggerName, e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
	}
	/**
	 * 
	 * @param response
	 * @return
	 */
	public List<Tweet> getTweets(String response) {
		List<Tweet> tweetList = new ArrayList<Tweet>();
		List<String> createdDate = JsonPath.read(response, TpConstants.TWEET_CREATED);
		List<String> tweets = JsonPath.read(response, TpConstants.TWEET_TEXT);
		List<String> fromUser = JsonPath.read(response,TpConstants.TWEET_FROM_USER );
		List<String> toUser = JsonPath.read(response,TpConstants.TWEET_TO_USER_NAME );

		List<Long> tweetTime = getTweetTime(response);
		for(int i=0; i<tweets.size(); i++) {
			if(toUser.get(i) == null || "".equals(toUser.get(i)) ) {
				Tweet tweet = new Tweet();
				tweet.setTweetTime(createdDate.get(i));
				tweet.setTime(tweetTime.get(i));
				tweet.setTweet("@"+fromUser.get(i)+":"+tweets.get(i));
				tweetList.add(tweet);
			}
		}
		return tweetList;
	}
	/**
	 * 
	 * @param response
	 * @return
	 */
	public List<Long> getTweetTime(String response) {
		List<String> createdDate = JsonPath.read(response, TpConstants.TWEET_CREATED);
		List<Long> time = new ArrayList<Long>();
		for (String date : createdDate) {
			time.add(ComUtils.convertIntoTime(date.substring(0,date.length()-6)));
		}
		return time;
	}
	/**
	 * 
	 * @param userid
	 * @return
	 * @throws TpException 
	 */
	private String getTwitterResponse(String queryParameter) throws TpException {
		String response =null;
		try {
			String baseUrl = tweetUrl + "?q=" + URLEncoder.encode(queryParameter, "UTF-8")+"&rpp=100";
			response = HttpUtils.getHttpResponse(baseUrl);
		} catch (IOException e) {
			logger.error(loggerName, e.getMessage());
			throw new TpException("Error while getting twitter response "+e.getMessage());
		} 
		return response;
	}
	/**
	 * 
	 * @param lastSentTime
	 * @param tweetCount
	 */
	private void publishTweets(long lastSentTime, int tweetCount, long lastLegTime) {
		try {
			String alertMsg = StatusMsgConfig.getInstance().getMsg("CALTRAIN_REGULAR_TWEET");
			String alertMsgForSingleThreshold = StatusMsgConfig.getInstance().getMsg("CALTRAIN_REGULAR_TWEET_FOR_1_THRESHOLD");
			Map<Integer, String> msgCache = new HashMap<Integer, String>();
			int pageSize = 500;
			int never = -1;
			BasicDBObject query = new BasicDBObject();
			query.put(TpConstants.LAST_PUSH_TIME, new BasicDBObject(MongoQueryConstant.LESS_THAN, lastSentTime));
			query.put(TpConstants.NUMBER_OF_ALERT, new BasicDBObject(MongoQueryConstant.LESS_THAN_EQUAL, tweetCount).append(MongoQueryConstant.NOT_EQUAL, never));

			int count = persistenceService.getCount(MONGO_TABLES.users.name() ,query ,User.class);
			int totalPages = (int) Math.ceil(count/(double)pageSize);

			for (int pageNumber=0; pageNumber<totalPages; pageNumber++) {
				List<User> resultSet = persistenceService.getUserListByPaging(MONGO_TABLES.users.name(), TpConstants.LAST_PUSH_TIME, lastSentTime, 
						TpConstants.NUMBER_OF_ALERT, tweetCount,never, pageSize, User.class);
				if (resultSet==null || resultSet.size()==0)
					break;
				List<String> pushSuccessDevices = new ArrayList<String>();
				for (User user : resultSet) {
					String formattedMsg = null;
					int newTweetCount = TweetStore.getInstance().getTweetCountAfterTime(user.getLastPushTime(), lastLegTime);
					if (user.getNumberOfAlert()==1) {
						formattedMsg = alertMsgForSingleThreshold;
					} else { 
						formattedMsg = msgCache.get(newTweetCount);
						if (formattedMsg==null) {
							formattedMsg = String.format(alertMsg, newTweetCount, TpConstants.TWEET_TIME_DIFF);
							msgCache.put(newTweetCount, formattedMsg);
						}
					}
					pushToPhone(user.getDeviceToken(),newTweetCount, formattedMsg, false);
					pushSuccessDevices.add(user.getId());
				}
				long sentTime = System.currentTimeMillis();
				persistenceService.updateMultiById(MONGO_TABLES.users.name(), pushSuccessDevices , TpConstants.LAST_PUSH_TIME, sentTime);
			}
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage());
		}
	}
	/**
	 * 
	 * @param message
	 */
	@SuppressWarnings("unchecked")
	public int pushUrgentTweets(String message) {
		int pushNotification = PUSH_NOTIFICATION.FAIL.ordinal();
		try {
			TweetStore.getInstance().addUrgentTweet(createTweetObj(message, System.currentTimeMillis(), true)); 
			int pageSize = 500;
			int never = -1;
			BasicDBObject query = new BasicDBObject();
			query.put(TpConstants.NUMBER_OF_ALERT, new BasicDBObject(MongoQueryConstant.GREATER_THAN, never));

			int count = persistenceService.getCount(MONGO_TABLES.users.name(), query, User.class);
			int totalPage = (int) Math.ceil(count/(double)pageSize);

			for (int pageNumber=1; pageNumber<=totalPage; pageNumber++) {
				List<User> resultSet = persistenceService.getListByPagging(MONGO_TABLES.users.name(), TpConstants.NUMBER_OF_ALERT, never, pageNumber, pageSize, User.class);
				if (resultSet==null || resultSet.size()==0)
					break;
				List<String> pushSuccessDevices = new ArrayList<String>();
				for (User user : resultSet) {
					pushToPhone(user.getDeviceToken(), 0, message, true);
					pushSuccessDevices.add(user.getId());
				}
				pushNotification = PUSH_NOTIFICATION.SUCCESS.ordinal();
			}
			if (count == 0) {
				pushNotification = PUSH_NOTIFICATION.NO_DEVICE_FOUND.ordinal();
			}
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage());
		}
		return pushNotification;
	}
	/**
	 * Push To phone 
	 * @param currentTime
	 * @param lastTime
	 */
	private boolean pushToPhone(String deviceToken, int tweetCount, String message, boolean isUrgent) {
		if (tweetCount>0)
			return apnService.push(deviceToken, message, tweetCount, isUrgent);
		else
			return apnService.push(deviceToken, message, null, isUrgent);
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public String getTweetUrl() {
		return tweetUrl;
	}

	public void setTweetUrl(String tweetUrl) {
		this.tweetUrl = tweetUrl;
	}

	public String getTimeIntervalInMin() {
		return timeIntervalInMin;
	}
	public void setTimeIntervalInMin(String timeIntervalInMin) {
		this.timeIntervalInMin = timeIntervalInMin;
	}
	public APNService getApnService() {
		return apnService;
	}
	public void setApnService(APNService apnService) {
		this.apnService = apnService;
	}
	/**
	 * 
	 * @param msg
	 * @param timestamp
	 * @param isUrgent
	 * @return
	 */
	private Tweet createTweetObj(String msg, Long timestamp, boolean isUrgent) {
		Tweet tweet = new  Tweet();
		tweet.setIsUrgent(isUrgent);
		tweet.setTime(timestamp); 
		tweet.setTweet(msg);
		return tweet;
	}

	public enum PUSH_NOTIFICATION {
		FAIL,
		SUCCESS,
		NO_DEVICE_FOUND
	}

	public int getMaxAlertThreshhold() {
		return maxAlertThreshhold;
	}
	public void setMaxAlertThreshhold(int maxAlertThreshhold) {
		this.maxAlertThreshhold = maxAlertThreshhold;
	}
	public List<String> getTweetSources() {
		return tweetSources;
	}
	public void setTweetSources(List<String> tweetSources) {
		this.tweetSources = tweetSources;
	}
}