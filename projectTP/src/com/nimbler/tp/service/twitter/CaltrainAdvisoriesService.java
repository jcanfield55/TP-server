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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jayway.jsonpath.JsonPath;
import com.mongodb.BasicDBObject;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.ThresholdBoard;
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

	private int maxAlertThreshhold = 10;

	private int pushIntervalStartTime; // in hour

	private int pushIntervalEndTime;

	private int validTweetHours;

	private int thresholdToInc = 1;

	public int maxTweetTextSizeToSend = 140;

	private Vector<ThresholdBoard> thresholdBoards = new Vector<ThresholdBoard>();

	private static Object thresholdLock = new  Object(); 

	public void init() {
		for (int i = 1; i <= maxAlertThreshhold; i++) 
			thresholdBoards.add(new ThresholdBoard(i));
	}

	/**
	 * Reset all counters.
	 */
	public void resetAllCounters() {
		logger.info(loggerName, "resetting all counters.......");
		synchronized (thresholdLock) {
			for (ThresholdBoard board : thresholdBoards) {
				logger.debug(loggerName, "before reset Threshold + Increament: "+board.getThreshold()+"+"+board.getIncreamentCount()+"");
				board.resetCounter();
			}			
		}
	}

	/**
	 * Gets the latest tweets.
	 *
	 * @return the latest tweets
	 */
	public void getLatestTweets() {
		try {
			logger.debug(loggerName, "get latest tweet...");
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

			boolean validPushTime = isValidPushTime();
			if (!validPushTime)
				return;

			long timeDiff = NumberUtils.toLong(TpConstants.TWEET_TIME_DIFF)*60*60*1000;
			//long timeDiff = 5 * 60 * 1000;
			long lastTimeLeg = System.currentTimeMillis() - timeDiff;
			int latestTweetCount = 0;
			for (Tweet tweet : tweetList) {
				if (tweet.getTime() >= lastTimeLeg) {
					latestTweetCount++;
				}
			}

			if (latestTweetCount > 0) {
				synchronized (thresholdLock) {
					int maxTweetToIterate =  0;
					for (ThresholdBoard board : thresholdBoards){ 
						board.setUsed(false);
						if(maxTweetToIterate < board.getEligibleCount())
							maxTweetToIterate = board.getEligibleCount();
					}

					Tweet latestTweet = tweetList.get(0);
					sortThresholdBoards(true); // to get largest eligible count
					logger.debug(loggerName, "iterate for tweet count max: "+maxTweetToIterate);
					for (int i = 1; i <= latestTweetCount; i++) {
						Tweet tweet = tweetList.get(i - 1);
						if (i <= maxTweetToIterate) {
							for (ThresholdBoard thresholdBoard : thresholdBoards) {

								if (thresholdBoard.getEligibleCount() == i && !thresholdBoard.isUsed()){									
									int sentCount = publishTweets(tweet.getTime(), thresholdBoard.getThreshold(),lastTimeLeg,latestTweet);
									logger.debug(loggerName, "threasold - sentCount:"+thresholdBoard.getThreshold()+"-"+sentCount);

									if(sentCount>0 && thresholdBoard.getThreshold() != 1 && latestTweet.getTime() != thresholdBoard.getLatestTweetTimeAtInc() ){
										/*logger.debug(loggerName, "Increamenting counter for: "
												+thresholdBoard.getThreshold()+"+"+thresholdBoard.getIncreamentCount()
												+ ", tweet: "+tweet.getTweet()
												+ ", sent count: "+sentCount);*/

										thresholdBoard.incCounter(latestTweet.getTime(),thresholdToInc);
										thresholdBoard.setUsed(true); // to avoid use of incremented count in next iteration
									}
								}
							}
						} 
					}
				}
				//logger.debug(loggerName," thresholdBoards: "+thresholdBoards);
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
				boolean validTweet = validateTweet(tweetTime.get(i));
				if (!validTweet)
					continue;
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
	 * @param latestTweet.getTime() 
	 * @param b 
	 * @return 
	 */
	private int publishTweets(long lastSentTime, int tweetCount, long lastLegTime, Tweet latestTweet) {
		int count = 0;
		try {
			String alertMsg = StatusMsgConfig.getInstance().getMsg("CALTRAIN_REGULAR_TWEET");
			//String alertMsgForSingleThreshold = StatusMsgConfig.getInstance().getMsg("CALTRAIN_REGULAR_TWEET_FOR_1_THRESHOLD");
			Map<Integer, String> msgCache = new HashMap<Integer, String>();
			int pageSize = 500;
			int never = -1;
			BasicDBObject query = new BasicDBObject();
			query.put(TpConstants.LAST_PUSH_TIME, new BasicDBObject(MongoQueryConstant.LESS_THAN, lastSentTime));
			query.put(TpConstants.NUMBER_OF_ALERT, tweetCount);

			count = persistenceService.getCount(MONGO_TABLES.users.name() ,query ,User.class);
			int totalPages = (int) Math.ceil(count/(double)pageSize);

			for (int pageNumber=0; pageNumber<totalPages; pageNumber++) {
				List<User> resultSet = persistenceService.getUserListByPaging(MONGO_TABLES.users.name(), 
						TpConstants.LAST_PUSH_TIME, lastSentTime, 
						TpConstants.NUMBER_OF_ALERT, tweetCount,never, pageSize, User.class);
				if (resultSet==null || resultSet.size()==0)
					break;
				List<String> pushSuccessDevices = new ArrayList<String>();
				for (User user : resultSet) {
					if (user.getNumberOfAlert()==1) {//then send actual tweet text
						List<String> newTweets = TweetStore.getInstance().getTweetsAfterTime(user.getLastPushTime(), lastLegTime);
						if (user.getLastPushTime()==0) {//if fresh installation then send only latest tweet, don't send all
							pushToPhone(user.getDeviceToken(), 1, newTweets.get(0), false, user.isStandardNotifSoundEnable());
						} else {
							for (String tweet: newTweets) {
								pushToPhone(user.getDeviceToken(), newTweets.size(), tweet, false, user.isStandardNotifSoundEnable());
							}
						}
					} else {
						int newTweetCount = TweetStore.getInstance().getTweetCountAfterTime(user.getLastPushTime(), lastLegTime);
						String formattedMsg = msgCache.get(newTweetCount);
						if (formattedMsg==null) {
							formattedMsg = String.format(alertMsg, newTweetCount, StringUtils.abbreviate(latestTweet.getTweet(), maxTweetTextSizeToSend) );
							msgCache.put(newTweetCount, formattedMsg);
						}
						pushToPhone(user.getDeviceToken(), newTweetCount, formattedMsg, false, user.isStandardNotifSoundEnable());
					}
					pushSuccessDevices.add(user.getId());
				}
				//				long sentTime = System.currentTimeMillis();
				//System.out.println("Push Time: "+sentTime+"-->"+new Date());
				persistenceService.updateMultiById(MONGO_TABLES.users.name(), pushSuccessDevices , TpConstants.LAST_PUSH_TIME, latestTweet.getTime());
			}
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage());
		}
		return count;
	}
	/**
	 * 
	 * @param message
	 */
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
					pushToPhone(user.getDeviceToken(), 0, message, true, user.isUrgentNotifSoundEnable());
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
	 * 
	 * @param message
	 * @param betaUserDeviceToken
	 * @return
	 */
	public int pushTweetToTestUser(String message, String betaUserDeviceToken, boolean playSound) {
		int pushNotification = PUSH_NOTIFICATION.FAIL.ordinal();
		try {
			TweetStore.getInstance().addUrgentTweet(createTweetObj(message, System.currentTimeMillis(), true)); 
			String[] deviceTokens = betaUserDeviceToken.split(",");
			List<String> lstDeviceTokans = new ArrayList<String>();
			for (int i = 0; i < deviceTokens.length; i++) {
				if(StringUtils.trimToNull(deviceTokens[i])!=null)
					lstDeviceTokans.add(deviceTokens[i].trim());
			}
			apnService.push(lstDeviceTokans, message, null, true, playSound);
			pushNotification = PUSH_NOTIFICATION.SUCCESS.ordinal();
		} catch (Exception e) {
			logger.error(loggerName, e.getMessage());
		}
		return pushNotification;
	}
	/**
	 * Push To phone 
	 * @param currentTime
	 * @param lastTime
	 */
	private boolean pushToPhone(String deviceToken, int tweetCount, String message, boolean isUrgent, boolean playSound) {
		if (tweetCount>0)
			return apnService.push(deviceToken, message, tweetCount, isUrgent, playSound);
		else
			return apnService.push(deviceToken, message, null, isUrgent, playSound);
	}
	/**
	 * 
	 * @param loggerName
	 */
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	/**
	 * 
	 * @return
	 */
	public String getLoggerName() {
		return loggerName;
	}
	/**
	 * 
	 * @return
	 */
	public String getTweetUrl() {
		return tweetUrl;
	}
	/**
	 * 
	 * @param tweetUrl
	 */
	public void setTweetUrl(String tweetUrl) {
		this.tweetUrl = tweetUrl;
	}
	/**
	 * 
	 * @return
	 */
	public String getTimeIntervalInMin() {
		return timeIntervalInMin;
	}
	/**
	 * 
	 * @param timeIntervalInMin
	 */
	public void setTimeIntervalInMin(String timeIntervalInMin) {
		this.timeIntervalInMin = timeIntervalInMin;
	}
	/**
	 * 
	 * @return
	 */
	public APNService getApnService() {
		return apnService;
	}
	/**
	 * 
	 * @param apnService
	 */
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
	/**
	 * 
	 */
	public void clearUrgentAdvisories() {
		TweetStore.getInstance().clearUrgentAdvisories();
	}
	/**
	 * 
	 * @author nikunj
	 *
	 */
	public enum PUSH_NOTIFICATION {
		FAIL,
		SUCCESS,
		NO_DEVICE_FOUND
	}
	/**
	 * 
	 * @return
	 */
	private boolean isValidPushTime() {
		int current = NumberUtils.toInt(DateFormatUtils.format(new Date(), "HH"));
		return pushIntervalStartTime<=current && pushIntervalEndTime>current; 
	}
	/**
	 * 
	 * @param createdDate
	 * @return
	 */
	private boolean validateTweet(Long createdDate) {
		long oldtimeLimit = System.currentTimeMillis()- (validTweetHours * DateUtils.MILLIS_PER_HOUR);		
		return (createdDate > oldtimeLimit);
	}
	/**
	 * 
	 * @return
	 */
	public int getMaxAlertThreshhold() {
		return maxAlertThreshhold;
	}
	/**
	 * 
	 * @param maxAlertThreshhold
	 */
	public void setMaxAlertThreshhold(int maxAlertThreshhold) {
		this.maxAlertThreshhold = maxAlertThreshhold;
	}
	/**
	 * 
	 * @return
	 */
	public List<String> getTweetSources() {
		return tweetSources;
	}
	/**
	 * 
	 * @param tweetSources
	 */
	public void setTweetSources(List<String> tweetSources) {
		this.tweetSources = tweetSources;
	}
	/**
	 * 
	 * @return
	 */
	public int getPushIntervalStartTime() {
		return pushIntervalStartTime;
	}
	/**
	 * 
	 * @param pushIntervalStartTime
	 */
	public void setPushIntervalStartTime(int pushIntervalStartTime) {
		this.pushIntervalStartTime = pushIntervalStartTime;
	}
	/**
	 * 
	 * @return
	 */
	public int getPushIntervalEndTime() {
		return pushIntervalEndTime;
	}
	/**
	 * 
	 * @param pushIntervalEndTime
	 */
	public void setPushIntervalEndTime(int pushIntervalEndTime) {
		this.pushIntervalEndTime = pushIntervalEndTime;
	}
	public int getValidTweetHours() {
		return validTweetHours;
	}
	public void setValidTweetHours(int validTweetHours) {
		this.validTweetHours = validTweetHours;
	}
	public Vector<ThresholdBoard> getThresholdBoards() {
		return thresholdBoards;
	}

	public void setThresholdBoards(Vector<ThresholdBoard> thresholdBoards) {
		this.thresholdBoards = thresholdBoards;
	}


	private void sortThresholdBoards(final boolean assending) {
		Collections.sort(thresholdBoards,new Comparator<ThresholdBoard>() {
			@Override
			public int compare(ThresholdBoard o1, ThresholdBoard o2) {
				if(assending)
					return o1.getThreshold() - o2.getThreshold();
				else
					return o2.getThreshold() - o1.getThreshold();
			}
		});
	}

	public int getThresholdToInc() {
		return thresholdToInc;
	}

	public void setThresholdToInc(int thresholdToInc) {
		this.thresholdToInc = thresholdToInc;
	}



}