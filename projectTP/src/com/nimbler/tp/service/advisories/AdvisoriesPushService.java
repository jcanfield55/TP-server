/*
 * @author nirmal
 */
package com.nimbler.tp.service.advisories;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.document.mongodb.query.BasicQuery;

import com.jayway.jsonpath.JsonPath;
import com.mongodb.BasicDBObject;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.NimblerApps;
import com.nimbler.tp.dataobject.ThresholdBoard;
import com.nimbler.tp.dataobject.Tweet;
import com.nimbler.tp.dbobject.User;
import com.nimbler.tp.dbobject.User.BOOLEAN_VAL;
import com.nimbler.tp.mongo.MongoQueryConstant;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.APNService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.twitter.TweetStore;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.HttpUtils;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.StatusMsgConfig;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;
import com.nimbler.tp.util.TpConstants.PUSH_MSG_CONSTANT;
import com.nimbler.tp.util.TpException;

/**
 * 
 * @author nIKUNJ
 *
 */
public class AdvisoriesPushService {

	@Autowired 
	private LoggingService logger;

	@Autowired
	private PersistenceService persistenceService;

	@Autowired
	private APNService apnService;

	private String loggerName = "com.nimbler.tp.service.advisories.AdvisoriesService";

	private String timeIntervalInMin = "3";

	private String tweetUrl = "http://search.twitter.com/search.json";

	private Map<Integer, String> agencyTweetSourceMap;

	private int maxAlertThreshhold = 10;

	private int pushIntervalStartTime = 5; // in hour

	private int pushIntervalEndTime = 22;

	private int validTweetHours = 6;

	private int thresholdToInc = 1;

	private Map<Integer, Vector<ThresholdBoard>> thresholdBoards = new HashMap<Integer, Vector<ThresholdBoard>>();
	/**
	 * <agency, multiplier>
	 */
	private Map<Integer, Integer> agencyPushThresholdWeight = new HashMap<Integer, Integer>();

	private Map<String, String>  agencyTweetSourceIconMap;

	private static Object thresholdLock = new  Object(); 

	public int maxTweetTextSizeToSend = 140;

	private int pageSize = 500;

	private boolean enablePushNotification = true;

	/**
	 *<column name,start-end min of day> 
	 */
	private Map<String, String> pushTimeInterval = new HashMap<String, String>();

	@Autowired
	private NimblerApps nimblerApps;

	public void init() {	
		AGENCY_TYPE[] agencyTypes = AGENCY_TYPE.values();
		for (AGENCY_TYPE agency : agencyTypes) {
			if(agency.ordinal()==0)
				continue;
			Vector<ThresholdBoard> boards =  thresholdBoards.get(agency.ordinal());
			if(boards==null){
				boards = new Vector<ThresholdBoard>();
				thresholdBoards.put(agency.ordinal(), boards);
			}
			for (int i = 1; i <= maxAlertThreshhold; i++) 
				boards.add(new ThresholdBoard(agency.ordinal(),i));
		}
	}

	/**
	 * Reset all counters.
	 */
	public void resetAllCounters() {		
		synchronized (thresholdLock) {
			for (Vector<ThresholdBoard> thresholdBoard : thresholdBoards.values()) {
				for (ThresholdBoard board : thresholdBoard) {
					logger.debug(loggerName, "before reset Threshold + Increament: "+board.getThreshold()+"+"+board.getIncreamentCount()+"");
					board.resetCounter();
				}			
			}
		}
		logger.info(loggerName, "All threshold counters reset.");
	}
	/**
	 * 
	 */
	public void fetchAndPushAdvisories() {
		try {
			logger.debug(loggerName, "Fetching Tweets.......");
			fetchTweets();
			if(getCurrentPushIntervalName()==null){
				logger.info(loggerName, "Not valid push time, skipping");
				return;
			}
			if(!enablePushNotification){
				logger.warn(loggerName, "Push notification Disabled, skipping..");
				return;
			}
			logger.debug(loggerName, "Sending push Advisories......");
			pushAdvisories();
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
	}
	/**
	 * 
	 */
	private void fetchTweets() {
		Iterator<Integer> agencies = agencyTweetSourceMap.keySet().iterator();		
		while (agencies.hasNext()) {
			Integer agency = agencies.next();
			String commaSeparatedSource = agencyTweetSourceMap.get(agency);
			String[] tweetSources = commaSeparatedSource.split(",");
			List<String> list = new ArrayList<String>();
			for (String source: tweetSources){
				list.add("from:"+source.trim());
			}
			for (int i = 0; i < 3; i++) {
				try {
					String queryParam = StringUtils.join(list,"+OR+")+ " since:" + ComUtils.getFormatedDate("yyyy-MM-dd");
					String response = getTwitterResponse(queryParam);
					List<Tweet> tweetList = getTweets(response);
					if (tweetList!=null)
						TweetStore.getInstance().setTweet(tweetList, agency);
					break;
				} catch (Exception e) {
					String retry="";
					if(i<2)
						retry = " - retrying....";
					logger.error(loggerName, "Error while getting twitter response for source: "+commaSeparatedSource+": "+e.getMessage()+retry);
					ComUtils.sleep(2000);
				}
			}
		}
	}
	/**
	 * 
	 */
	private void pushAdvisories() {
		NimblerApps apps = BeanUtil.getNimblerAppsBean();
		Iterator<Integer> appIdentifiers = apps.getAppIdentifierToAgenciesMap().keySet().iterator();
		Set<ThresholdIncData> boardsToInc = new HashSet<ThresholdIncData>();
		while (appIdentifiers.hasNext()) {
			int appIdentifier = appIdentifiers.next();
			//For many agencies in one app: use this for sending push to specific app for specific agencies
			String agencies = apps.getAppIdentifierToAgenciesMap().get(appIdentifier);
			String[] agencyArr = agencies.split(",");

			for (String agency: agencyArr) {
				//logger.debug(loggerName, "App:"+appIdentifier+", agency: "+agency);
				int agencyId = NumberUtils.toInt(agency, AGENCY_TYPE.CALTRAIN.ordinal());
				long timeDiff = NumberUtils.toLong(TpConstants.TWEET_TIME_DIFF)*60*60*1000;
				long lastTimeLeg = System.currentTimeMillis() - timeDiff;
				int lastFrameTweetCount = 0;
				List<Tweet> tweetList  = TweetStore.getInstance().getTweets(agencyId); 
				for (Tweet tweet : tweetList) {
					if (tweet.getTime() >= lastTimeLeg)
						lastFrameTweetCount++;
				}
				if (lastFrameTweetCount <= 0) {
					logger.debug(loggerName, "No new tweets found for agency:"+agency);
					continue;
				}
				synchronized (thresholdLock) {
					int maxTweetToIterate =  0;
					Vector<ThresholdBoard> thresholdBoard = thresholdBoards.get(agencyId);
					for (ThresholdBoard board : thresholdBoard) {
						board.setUsed(false);
						if (maxTweetToIterate < board.getEligibleCount())
							maxTweetToIterate = board.getEligibleCount();
					}
					Tweet latestTweet = tweetList.get(0);
					sortThresholdBoards(thresholdBoard,true); // to get largest eligible count
					logger.debug(loggerName, "Iterate for tweet count max: "+maxTweetToIterate);
					int iterateCount = Math.min(lastFrameTweetCount, maxTweetToIterate);
					for (int i = 1; i <= iterateCount; i++) {
						Tweet tweet = tweetList.get(i - 1);
						for (ThresholdBoard board : thresholdBoard) {
							int agencyMultilierWeight = agencyPushThresholdWeight.get(agencyId);
							if (board.getEligibleCount()*agencyMultilierWeight == i && !board.isUsed()) {
								int sentCount = publishTweets(tweet.getTime(), board.getThreshold(),lastTimeLeg, latestTweet, 
										NIMBLER_APP_TYPE.values()[appIdentifier], AGENCY_TYPE.values()[agencyId]);
								//logger.debug(loggerName, "threasold - sentCount:"+board.getThreshold()+"-"+sentCount);

								if (sentCount>0 && board.getThreshold() != 1 && latestTweet.getTime() != board.getLatestTweetTimeAtInc()) {
									logger.debug(loggerName, "Increamenting counter for: "+board.getThreshold()+"+"+board.getIncreamentCount()+ ", tweet: "+tweet.getTweet()+ ", sent count: "+sentCount);

									ThresholdIncData tid =  new ThresholdIncData(board, latestTweet.getTime(), thresholdToInc);
									if(!boardsToInc.contains(tid))
										boardsToInc.add(tid);

									board.setUsed(true); // to avoid use of incremented count in next iteration
								}
							}
						}
					}
				}
			}
		}
		if(boardsToInc.size()>0){
			for (ThresholdIncData data : boardsToInc) {
				ThresholdBoard board = data.getBoard();				
				board.incCounter(data.getTime(),data.getIncCount());
				logger.debug(loggerName, "Increamented counter for: "+board.getThreshold()+"+"+board.getIncreamentCount());
			}
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
				tweet.setSource(agencyTweetSourceIconMap.get(StringUtils.lowerCase(fromUser.get(i))));
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
	 * @throws UnsupportedEncodingException 
	 */
	private String getTwitterResponse(String queryParameter) throws  UnsupportedEncodingException, TpException {
		String response =null;
		String baseUrl = tweetUrl + "?q=" + URLEncoder.encode(queryParameter, "UTF-8")+"&rpp=100";
		response = HttpUtils.getHttpResponse(baseUrl);
		return response;
	}
	/**
	 * 	
	 * @param lastSentTime
	 * @param tweetCount
	 * @param lastLegTime
	 * @param latestTweetTime
	 * @param agency_TYPE
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int publishTweets(long lastSentTime, int tweetCount, long lastLegTime, Tweet latestTweet, NIMBLER_APP_TYPE appType, AGENCY_TYPE agencyType) {
		int count = 0;
		try {
			Map<Integer, String> msgCache = new HashMap<Integer, String>();
			String alertMsg = "";
			String pushTimeColName =  agencyType.getPushTimeColumnName();
			if(appType.ordinal() == NIMBLER_APP_TYPE.CALTRAIN.ordinal()){
				alertMsg = StatusMsgConfig.getInstance().getMsg(PUSH_MSG_CONSTANT.CALTRAIN_REGULAR_TWEET.name());
				//				pushTimeColName= TpConstants.LAST_PUSH_TIME;
			}else{
				alertMsg = StatusMsgConfig.getInstance().getMsg(PUSH_MSG_CONSTANT.SF_REGULAR_TWEET.name());
				alertMsg = String.format(alertMsg, "%s",agencyType.getText(),"%s");
			}			

			BasicDBObject queryObject = new BasicDBObject();
			boolean isWeekEnd = ComUtils.isWeekEnd();
			if(isWeekEnd){
				queryObject.put(RequestParam.NOTIF_TIMING_WEEKEND, BOOLEAN_VAL.TRUE.ordinal());
			}else{
				String intervalColumnName = getCurrentPushIntervalName(); //morning/evening
				if(intervalColumnName==null){
					logger.info(loggerName, "No valid interval column found for current time (possibly entered in blackout), skip sending.....");
					return count;
				}
				queryObject.put(intervalColumnName, BOOLEAN_VAL.TRUE.ordinal());
			}
			queryObject.put(agencyType.getEnableAdvisoryColumnName(), BOOLEAN_VAL.TRUE.ordinal());
			queryObject.put(TpConstants.APP_TYPE, appType.ordinal());
			queryObject.put(TpConstants.NUMBER_OF_ALERT, tweetCount);
			queryObject.put(pushTimeColName, new BasicDBObject(MongoQueryConstant.LESS_THAN, lastSentTime));

			count = persistenceService.getCount(MONGO_TABLES.users.name() ,queryObject ,User.class);
			//logger.debug(loggerName, "User count: "+count+", tweetCount: "+tweetCount+",agency :"+agencyType+", App :"+appType);
			int totalPages = (int) Math.ceil(count/(double)pageSize);
			BasicQuery basicQuery = new BasicQuery(queryObject);
			basicQuery.setLimit(pageSize);
			for (int pageNumber=0; pageNumber<totalPages; pageNumber++) {
				List<User> resultSet = persistenceService.findByQuery(MONGO_TABLES.users.name(),basicQuery,	User.class);

				if (resultSet==null || resultSet.size()==0)
					break;
				List<String> pushSuccessDevices = new ArrayList<String>();
				List<String> pushedDeviceIds = new ArrayList<String>();
				for (User user : resultSet) {
					long  usrLastPushTime = getLastPushTime(user,agencyType.ordinal());
					if (user.getNumberOfAlert()==1) {//then send actual tweet text
						List<String> newTweets = TweetStore.getInstance().getTweetsAfterTime(usrLastPushTime, lastLegTime, agencyType.ordinal());
						if (usrLastPushTime==0) {//if fresh installation then send only latest tweet, don't send all
							pushToPhone(user.getDeviceToken(), 1, newTweets.get(0), false, user.isStandardNotifSoundEnable(), appType);
						} else {
							for (String tweet: newTweets)
								pushToPhone(user.getDeviceToken(), newTweets.size(), tweet, false, user.isStandardNotifSoundEnable(), appType);							
						}
					} else {
						int newTweetCount = TweetStore.getInstance().getTweetCountAfterTime(usrLastPushTime, lastLegTime, agencyType.ordinal());
						String formattedMsg = msgCache.get(newTweetCount);
						if (formattedMsg==null) {
							formattedMsg = String.format(alertMsg, newTweetCount, StringUtils.abbreviate(latestTweet.getTweet(), maxTweetTextSizeToSend) );
							msgCache.put(newTweetCount, formattedMsg);
						}
						pushToPhone(user.getDeviceToken(), newTweetCount, formattedMsg, false, user.isStandardNotifSoundEnable(), appType);
					}
					pushSuccessDevices.add(user.getId());
					pushedDeviceIds.add(user.getDeviceId());
				}
				//System.out.println("Tweets pushed to: "+pushedDeviceIds);
				persistenceService.updateMultiById(MONGO_TABLES.users.name(), pushSuccessDevices , pushTimeColName, latestTweet.getTime());
			}
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage());
		}
		return count;
	}
	/**
	 * 
	 * @param message
	 * @param appType
	 * @return
	 */
	public int pushUrgentTweetsByAgency(String tweet, AGENCY_TYPE agencyType) {
		int pushNotification = PUSH_NOTIFICATION.FAIL.ordinal();
		try {
			String templet = StatusMsgConfig.getInstance().getMsg(PUSH_MSG_CONSTANT.URGENT_TWEET.name());
			String msg = String.format(templet,agencyType.getText(), tweet);
			logger.debug(loggerName, "Sending tweet By agency,Tweet: "+tweet+", agency type: "+agencyType);
			TweetStore.getInstance().addUrgentTweet(new Tweet(msg,System.currentTimeMillis(),true), agencyType.ordinal());
			int never = -1;
			BasicDBObject queryObj = new BasicDBObject();
			queryObj.put(TpConstants.NUMBER_OF_ALERT, new BasicDBObject(MongoQueryConstant.NOT_EQUAL, never));
			queryObj.put(agencyType.getEnableAdvisoryColumnName(), BOOLEAN_VAL.TRUE.ordinal());
			/*	
			queryObj.put(TpConstants.APP_TYPE, agencyType.ordinal());
			 * if(agencyType.ordinal() == AGENCY_TYPE.CALTRAIN.ordinal()){
				DBObject appCalTrain = new BasicDBObject(TpConstants.APP_TYPE, new BasicDBObject(MongoQueryConstant.IN, new Object[]{null,NIMBLER_APP_TYPE.CALTRAIN.ordinal()}));
				DBObject appBay = new BasicDBObject(TpConstants.APP_TYPE, NIMBLER_APP_TYPE.SF_BAY_AREA.ordinal());
				appBay.put(agencyType.getEnableAdvisoryColumnName(),BOOLEAN_VAL.TRUE.ordinal());
				BasicDBList or = new BasicDBList();
				or.add(appCalTrain);
				or.add(appBay);
				queryObj.put(MongoQueryConstant.OR, or);				
			}else{
				queryObj.put(TpConstants.APP_TYPE, NIMBLER_APP_TYPE.SF_BAY_AREA.ordinal());
			}*/

			BasicQuery basicQuery = new BasicQuery(queryObj);
			int count = persistenceService.getCount(MONGO_TABLES.users.name(), queryObj, User.class);
			logger.debug(loggerName, "count: "+count);
			int totalPage = (int) Math.ceil(count/(double)pageSize);
			logger.debug(loggerName, "Sending Admin push for agency: "+ agencyType.name()+", count: "+count+", Msg: "+msg);
			for (int pageNumber=0; pageNumber<totalPage; pageNumber++) {
				basicQuery.setLimit(pageSize);
				basicQuery.setSkip(pageSize*pageNumber);
				List<User> resultSet = persistenceService.findByQuery(MONGO_TABLES.users.name(),basicQuery,User.class);
				if (resultSet==null || resultSet.size()==0)
					break;
				List<String> pushSuccessDevices = new ArrayList<String>();
				for (User user : resultSet) {
					int appType = NumberUtils.toInt(user.getAppType()+"", NIMBLER_APP_TYPE.CALTRAIN.ordinal());
					if(appType==0)
						appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal();
					pushToPhone(user.getDeviceToken(), 0, msg, true, user.isUrgentNotifSoundEnable(), 
							NIMBLER_APP_TYPE.values()[appType]);//i know its bad!!
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
	 * Push urgent tweets by app.
	 *
	 * @param message the message
	 * @param appType the app type
	 * @return the int
	 */
	public int pushTweetsByApp(String tweet, int appType) {
		int pushNotification = PUSH_NOTIFICATION.FAIL.ordinal();
		try {			
			int never = -1;
			BasicDBObject queryObj = new BasicDBObject();
			queryObj.put(TpConstants.NUMBER_OF_ALERT, new BasicDBObject(MongoQueryConstant.NOT_EQUAL, never));
			queryObj.put(TpConstants.APP_TYPE, appType);
			logger.debug(loggerName, "Sending tweet By App,Tweet: "+tweet+", appType : "+appType);
			/*	if(appType == NIMBLER_APP_TYPE.CALTRAIN.ordinal()){
				queryObj.put(TpConstants.APP_TYPE, new BasicDBObject(MongoQueryConstant.IN, new Object[]{null,NIMBLER_APP_TYPE.CALTRAIN.ordinal()}));
			}else{
				queryObj.put(TpConstants.APP_TYPE, NIMBLER_APP_TYPE.SF_BAY_AREA.ordinal());
			}*/
			String templet = StatusMsgConfig.getInstance().getMsg(PUSH_MSG_CONSTANT.STANDARD_ADMIN_MSG.name());
			String msg = String.format(templet,NIMBLER_APP_TYPE.values()[appType].getText() ,tweet);			

			BasicQuery basicQuery = new BasicQuery(queryObj);
			int count = persistenceService.getCount(MONGO_TABLES.users.name(), queryObj, User.class);
			int totalPage = (int) Math.ceil(count/(double)pageSize);
			logger.debug(loggerName, "Sending Admin push for app: "+NIMBLER_APP_TYPE.values()[appType].name()+", count: "+count+", Msg: "+msg);
			for (int pageNumber=0; pageNumber<totalPage; pageNumber++) {
				basicQuery.setLimit(pageSize);
				basicQuery.setSkip(pageSize*pageNumber);
				List<User> resultSet = persistenceService.findByQuery(MONGO_TABLES.users.name(),basicQuery,User.class);
				if (resultSet==null || resultSet.size()==0)
					break;
				List<String> pushSuccessDevices = new ArrayList<String>();
				for (User user : resultSet) {					
					pushToPhone(user.getDeviceToken(), 0, msg, false, user.isStandardNotifSoundEnable(), 
							NIMBLER_APP_TYPE.values()[appType]);
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
	public int pushTweetToTestUser(String message, String betaUserDeviceToken, boolean playSound, NIMBLER_APP_TYPE appType) {
		logger.debug(loggerName ,"sending tweet to Test Users, Message:"+message+", Tockens: "+betaUserDeviceToken+",playSound: "+playSound);
		int pushNotification = PUSH_NOTIFICATION.FAIL.ordinal();
		try { 
			String[] deviceTokens = betaUserDeviceToken.split(",");
			List<String> lstDeviceTokans = new ArrayList<String>();
			for (int i = 0; i < deviceTokens.length; i++) {
				if (StringUtils.trimToNull(deviceTokens[i])!=null)
					lstDeviceTokans.add(deviceTokens[i].trim());
			}
			boolean sucess =  apnService.push(lstDeviceTokans, message, null, false, playSound, appType);
			logger.debug(loggerName ,"push sucess result: "+sucess);
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
	private boolean pushToPhone(String deviceToken, int tweetCount, String message, boolean isUrgent, boolean playSound, NIMBLER_APP_TYPE appType) {
		if (tweetCount>0)
			return apnService.push(deviceToken, message, tweetCount, isUrgent, playSound, appType);
		else
			return apnService.push(deviceToken, message, null, isUrgent, playSound, appType);
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
	private boolean isValidPushTimeForCalTrain() {		
		int current = NumberUtils.toInt(DateFormatUtils.format(new Date(), "HH"));
		return pushIntervalStartTime<=current && pushIntervalEndTime>current; 
	}

	/**
	 * Gets the current push interval name.
	 *
	 * @return the current push interval name
	 */
	private String getCurrentPushIntervalName(){
		Calendar c =  Calendar.getInstance();
		int currentMins = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
		for (Entry<String, String> entry : pushTimeInterval.entrySet()) {
			String name = entry.getKey();
			String strInterval = entry.getValue();
			String[] interval = strInterval.split("-");
			int start = Integer.parseInt(interval[0]);
			int end = Integer.parseInt(interval[1]);
			if(start<= currentMins && currentMins<end)
				return name;
		}
		return null;
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

	public int getMaxTweetTextSizeToSend() {
		return maxTweetTextSizeToSend;
	}

	public void setMaxTweetTextSizeToSend(int maxTweetTextSizeToSend) {
		this.maxTweetTextSizeToSend = maxTweetTextSizeToSend;
	}

	public Map<Integer, Vector<ThresholdBoard>> getThresholdBoards() {
		return thresholdBoards;
	}

	public void setThresholdBoards(
			Map<Integer, Vector<ThresholdBoard>> thresholdBoards) {
		this.thresholdBoards = thresholdBoards;
	}

	private void sortThresholdBoards(Vector<ThresholdBoard> thresholdBoard, final boolean assending) {
		Collections.sort(thresholdBoard,new Comparator<ThresholdBoard>() {
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

	public Map<Integer, String> getAgencyTweetSourceMap() {
		return agencyTweetSourceMap;
	}

	public void setAgencyTweetSourceMap(Map<Integer, String> agencyTweetSourceMap) {
		this.agencyTweetSourceMap = agencyTweetSourceMap;
	}

	public NimblerApps getNimblerApps() {
		return nimblerApps;
	}

	public void setNimblerApps(NimblerApps nimblerApps) {
		this.nimblerApps = nimblerApps;
	}

	public Map<Integer, Integer> getAgencyPushThresholdWeight() {
		return agencyPushThresholdWeight;
	}

	public void setAgencyPushThresholdWeight(
			Map<Integer, Integer> agencyPushThresholdWeight) {
		this.agencyPushThresholdWeight = agencyPushThresholdWeight;
	}

	public Map<String, String> getPushTimeInterval() {
		return pushTimeInterval;
	}

	public void setPushTimeInterval(Map<String, String> pushTimeInterval) {
		this.pushTimeInterval = pushTimeInterval;
	}
	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	private long getLastPushTime(User user, int agencyId) {
		if (agencyId == AGENCY_TYPE.BART.ordinal()) {
			return user.getLastPushTimeBart();
		} else if (agencyId == AGENCY_TYPE.AC_TRANSIT.ordinal()) {
			return user.getLastPushTimeAct();
		} else if (agencyId == AGENCY_TYPE.SFMUNI.ordinal()) {
			return user.getLastPushTimeSfMuni(); 
		} else if (agencyId == AGENCY_TYPE.CALTRAIN.ordinal()) {
			//return user.getLastPushTimeCaltrain();
			return user.getLastPushTime();
		} else {
			return user.getLastPushTime();
		}
	}
	static class ThresholdIncData{
		ThresholdBoard board;
		long time;
		int incCount;
		public ThresholdBoard getBoard() {
			return board;
		}
		public void setBoard(ThresholdBoard board) {
			this.board = board;
		}
		public long getTime() {
			return time;
		}
		public void setTime(long time) {
			this.time = time;
		}
		public int getIncCount() {
			return incCount;
		}
		public void setIncCount(int incCount) {
			this.incCount = incCount;
		}
		@Override
		public String toString() {
			return "ThresholdIndData [board=" + board + ", time=" + time
					+ ", incCount=" + incCount + "]";
		}
		private ThresholdIncData(ThresholdBoard board, long time, int incCount) {
			this.board = board;
			this.time = time;
			this.incCount = incCount;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((board == null) ? 0 : board.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ThresholdIncData other = (ThresholdIncData) obj;
			if (board == null) {
				if (other.board != null)
					return false;
			} else if (!board.equals(other.board))
				return false;
			return true;
		}
	}
	public boolean isEnablePushNotification() {
		return enablePushNotification;
	}

	public void setEnablePushNotification(boolean enablePushNotification) {
		this.enablePushNotification = enablePushNotification;
	}

	public Map<String, String> getAgencyTweetSourceIconMap() {
		return agencyTweetSourceIconMap;
	}

	public void setAgencyTweetSourceIconMap(
			Map<String, String> agencyTweetSourceIconMap) {
		this.agencyTweetSourceIconMap = agencyTweetSourceIconMap;
	}
}