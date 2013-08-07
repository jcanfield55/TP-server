/*
 * @author nirmal
 */
package com.nimbler.tp.service.advisories;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.document.mongodb.query.BasicQuery;

import com.jayway.jsonpath.JsonPath;
import com.mongodb.BasicDBObject;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.NimblerApps;
import com.nimbler.tp.dataobject.ThresholdBoard;
import com.nimbler.tp.dataobject.Tweet;
import com.nimbler.tp.dbobject.NimblerParams;
import com.nimbler.tp.dbobject.User;
import com.nimbler.tp.dbobject.User.BOOLEAN_VAL;
import com.nimbler.tp.mongo.MongoQueryConstant;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.APNService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.smtp.MailService;
import com.nimbler.tp.service.twitter.TweetStore;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.StatusMsgConfig;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;
import com.nimbler.tp.util.TpConstants.PUSH_MSG_CONSTANT;

/**
 * 
 * @author nIKUNJ,nirmal
 *
 */
@SuppressWarnings("unchecked")
public class AdvisoriesPushService {

	@Autowired 
	private LoggingService logger;

	@Autowired
	private PersistenceService persistenceService;

	@Autowired
	private APNService apnService;

	private String loggerName = "com.nimbler.tp.service.advisories.AdvisoriesService";


	private Map<Integer, String> agencyTweetSourceMap;

	private int maxAlertThreshhold = 10;

	private int pushIntervalStartTime = 5; // in hour

	private int pushIntervalEndTime = 22;

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
	@Autowired
	private BartAlertCriteria bartAlertCriteria = null;



	@Autowired
	private TwitterMonitor twitterMonitor;
	@Autowired
	TwitterSearchManager twitterSearchApi;

	/**
	 *<column name,start-end min of day> 
	 */
	private Map<String, String> pushTimeInterval = new HashMap<String, String>();

	@Autowired
	MailService mailService;

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
	 * @param agencies
	 */
	public void resetAllCounters(Integer[] agencies) {
		synchronized (thresholdLock) {
			for (Integer agency : agencies) {
				Vector<ThresholdBoard> boards =  thresholdBoards.get(agency);
				if(boards==null){
					logger.error(loggerName, "No ThresholdBoards found to reset for agency id "+agency);
					continue;
				}
				for (ThresholdBoard board : boards) {
					logger.debug(loggerName, "before reset Threshold + Increament: "+board.getThreshold()+"+"+board.getIncreamentCount()+"");
					board.resetCounter();
				}			
			}
		}
		logger.info(loggerName, "All threshold counters reset.");
	}

	public void onDayFinish(String strAgencies) {
		Integer[] agencies = ComUtils.splitToIntArray(strAgencies);
		resetAllCounters(agencies);
		clearUrgentAdvisories(agencies);
	}
	/**
	 * 
	 */
	public void fetchAndPushAdvisories() {
		try {
			logger.debug(loggerName, "Fetching Tweets.......");
			fetchTweets();
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
			int tryCount = 3;
			for (int i = 0; i < tryCount; i++) {
				try {
					List<Tweet> tweetList = twitterSearchApi.fetchTweets(tweetSources,agencyTweetSourceIconMap);
					TweetStore.getInstance().setTweet(tweetList, agency);
					twitterMonitor.fetchSucess(tweetSources);
					break;
				} catch (Exception e) {
					String retry="";
					if(i<(tryCount-1))
						retry = " - retrying....";
					else
						twitterMonitor.fetchFailed(tweetSources,e);
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

			TimeZone tz = ComUtils.getAppTimeZone(appIdentifier);
			if(getCurrentPushIntervalName(tz)==null){
				logger.info(loggerName, "Not valid push time for App: "+appIdentifier+", skipping");
				continue;
			}
			//For many agencies in one app: use this for sending push to specific app for specific agencies
			String agencies = apps.getAppIdentifierToAgenciesMap().get(appIdentifier);
			String[] agencyArr = agencies.split(",");

			for (String agency: agencyArr) {
				//logger.debug(loggerName, "App:"+appIdentifier+", agency: "+agency);
				int agencyId = NumberUtils.toInt(agency, AGENCY_TYPE.CALTRAIN.ordinal());
				long timeDiff = NumberUtils.toLong(TpConstants.TWEET_TIME_DIFF)*60*60*1000;
				long lastTimeLeg = System.currentTimeMillis() - timeDiff;
				if(agencyId == AGENCY_TYPE.BART.ordinal()){
					pushTweetToBart(appIdentifier,lastTimeLeg);
					continue;
				}
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
					//					logger.debug(loggerName, "Iterate for tweet count max: "+maxTweetToIterate);
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
									//									logger.debug(loggerName, "Increamenting counter for: "+board.getThreshold()+"+"+board.getIncreamentCount()+ ", tweet: "+tweet.getTweet()+ ", sent count: "+sentCount);

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
	 * Push tweet to bart.
	 *
	 * @param appIdentifier the app identifier
	 * @param lastTimeLeg the last time leg
	 */
	private void pushTweetToBart(int appIdentifier, long lastTimeLeg) {
		try {			
			logger.debug(loggerName, "sending tweet to bart..."+appIdentifier);
			List<Tweet> tweetList  = TweetStore.getInstance().getTweets(AGENCY_TYPE.BART.ordinal());
			NimblerParams nimblerParams = 	(NimblerParams) persistenceService.findOne(
					MONGO_TABLES.nimbler_params.name(),TpConstants.NIMBLER_PARAMS_NAME,
					AGENCY_TYPE.BART.getPushTimeColumnName()+"_"+appIdentifier, NimblerParams.class);
			long lastTweetSentTime =-1;
			if(nimblerParams!=null)
				lastTweetSentTime = Long.parseLong(nimblerParams.getValue());
			if(ComUtils.isEmptyList(tweetList)){
				logger.debug(loggerName, "No tweet to send....");
				return;
			}

			Tweet latestTweet = tweetList.get(0);
			long maxTweetTime = Math.max(lastTweetSentTime,latestTweet.getTime());
			ListIterator<Tweet> itrTweet = tweetList.listIterator(tweetList.size());
			while (itrTweet.hasPrevious()) {
				Tweet tweet =  itrTweet.previous();
				if( BooleanUtils.isTrue(tweet.getIsUrgent()) || tweet.getTime()<=lastTweetSentTime)
					continue;
				logger.debug(loggerName, "Tweet: "+tweet.getTweet()+", "+tweet.getTime());
				logger.debug(loggerName,"latest Tweet: "+latestTweet.getTime()+" : "+latestTweet.getTweet());
				Object[] alertCounts = bartAlertCriteria.getElibleAlertCount(tweet);
				logger.debug(loggerName, "Eligible Alert Counts: "+ReflectionToStringBuilder.toString(alertCounts,ToStringStyle.SHORT_PREFIX_STYLE));
				publishTweetsBart(alertCounts,tweet,lastTimeLeg,NIMBLER_APP_TYPE.values()[appIdentifier],maxTweetTime);
			}			
			persistenceService.upsertNimblerParam(AGENCY_TYPE.BART.getPushTimeColumnName()+"_"+appIdentifier, maxTweetTime+"");
			logger.debug(loggerName, "done....");
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
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
			TimeZone timeZone = ComUtils.getAppTimeZone(appType.ordinal());
			BasicDBObject queryObject = new BasicDBObject();
			boolean isWeekEnd = ComUtils.isWeekEnd(timeZone);
			if(isWeekEnd){
				queryObject.put(RequestParam.NOTIF_TIMING_WEEKEND, BOOLEAN_VAL.TRUE.ordinal());
			}else{
				String intervalColumnName = getCurrentPushIntervalName(timeZone); //morning/evening
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
	 * Publish tweets bart.
	 *
	 * @param alertCount 1,5,6,7,8,9,10, or 1,3
	 * @param lastLegTime the last leg time
	 * @param latestTweet the latest tweet
	 * @param appType the app type
	 * @param maxTweetTime 
	 * @return the int
	 */
	private int publishTweetsBart(Object[] alertCount,Tweet latestTweet,long lastTimeLeg, NIMBLER_APP_TYPE appType, long maxTweetTime) {
		int count = 0;
		try {
			Map<Integer, String> msgCache = new HashMap<Integer, String>();
			AGENCY_TYPE agencyType = AGENCY_TYPE.BART;

			BasicDBObject queryObject = new BasicDBObject();
			TimeZone timeZone = ComUtils.getAppTimeZone(appType.ordinal());
			boolean isWeekEnd = ComUtils.isWeekEnd(timeZone);
			if(isWeekEnd){
				queryObject.put(RequestParam.NOTIF_TIMING_WEEKEND, BOOLEAN_VAL.TRUE.ordinal());
			}else{
				String intervalColumnName = getCurrentPushIntervalName(timeZone); //morning/evening
				if(intervalColumnName==null){
					logger.info(loggerName, "No valid interval column found for current time (possibly entered in blackout), skip sending.....");
					return count;
				}
				queryObject.put(intervalColumnName, BOOLEAN_VAL.TRUE.ordinal());
			}
			queryObject.put(agencyType.getEnableAdvisoryColumnName(), BOOLEAN_VAL.TRUE.ordinal());
			queryObject.put(TpConstants.APP_TYPE, appType.ordinal());
			queryObject.put(TpConstants.NUMBER_OF_ALERT, new BasicDBObject(MongoQueryConstant.IN, alertCount));
			String queryExp = String.format(bartAlertCriteria.getTweetTimeQuery(), latestTweet.getTime());
			queryObject.put(MongoQueryConstant.WHERE, queryExp);

			count = persistenceService.getCount(MONGO_TABLES.users.name() ,queryObject ,User.class);

			int totalPages = (int) Math.ceil(count/(double)pageSize);
			BasicQuery basicQuery = new BasicQuery(queryObject);
			basicQuery.setLimit(pageSize);
			String alertMsg = StatusMsgConfig.getInstance().getMsg(PUSH_MSG_CONSTANT.SF_REGULAR_TWEET.name());
			alertMsg = String.format(alertMsg, "%s",agencyType.getText(),"%s");
			for (int pageNumber=0; pageNumber<totalPages; pageNumber++) {
				List<User> resultSet = persistenceService.findByQuery(MONGO_TABLES.users.name(),basicQuery,	User.class);
				if (resultSet==null || resultSet.size()==0)
					break;
				List<String> pushSuccessDevices = new ArrayList<String>();
				List<String> pushedDeviceIds = new ArrayList<String>();
				for (User user : resultSet) {
					long  usrLastPushTime = user.getLastPushTimeBart();
					if (user.getNumberOfAlert()==1) {//then send actual tweet text
						List<String> newTweets = TweetStore.getInstance().getTweetsAfterTime(usrLastPushTime, lastTimeLeg, agencyType.ordinal());
						if (usrLastPushTime==0) {//if fresh installation then send only latest tweet, don't send all
							pushToPhone(user.getDeviceToken(), 1, newTweets.get(0), false, user.isStandardNotifSoundEnable(), appType);
						} else {							
							pushToPhone(user.getDeviceToken(), newTweets.size(), latestTweet.getTweet(), false, user.isStandardNotifSoundEnable(), appType);							
						}
					} else {
						int newTweetCount = TweetStore.getInstance().getTweetCountAfterTime(usrLastPushTime,lastTimeLeg , agencyType.ordinal());
						String formattedMsg = msgCache.get(newTweetCount);
						if (formattedMsg==null) {
							formattedMsg = String.format(alertMsg, newTweetCount, latestTweet.getTweet());
							msgCache.put(newTweetCount, formattedMsg);
						}
						pushToPhone(user.getDeviceToken(), newTweetCount, formattedMsg, false, user.isStandardNotifSoundEnable(), appType);
					}
					pushSuccessDevices.add(user.getId());
					pushedDeviceIds.add(user.getDeviceId());
				}
				//System.out.println("Tweets pushed to: "+pushedDeviceIds);
				persistenceService.updateMultiById(MONGO_TABLES.users.name(), pushSuccessDevices , agencyType.getPushTimeColumnName(), maxTweetTime);
				logger.debug(loggerName, "               for tweet:    "+latestTweet.getTime()+" - "+latestTweet.getTweet()+"\n" +
						"               max time :    "+maxTweetTime);

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

			Tweet tweetObj = new Tweet(msg,System.currentTimeMillis(),true);
			tweetObj.setSource(agencyTweetSourceIconMap.get(agencyType.ordinal()+""));
			TweetStore.getInstance().addUrgentTweet(tweetObj, agencyType.ordinal());

			BasicDBObject queryObj = new BasicDBObject();
			queryObj.put(TpConstants.NUMBER_OF_ALERT, new BasicDBObject(MongoQueryConstant.GREATER_THAN, 0));
			queryObj.put(agencyType.getEnableAdvisoryColumnName(), BOOLEAN_VAL.TRUE.ordinal());

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
			BasicDBObject queryObj = new BasicDBObject();
			queryObj.put(TpConstants.NUMBER_OF_ALERT, new BasicDBObject(MongoQueryConstant.GREATER_THAN, 0));
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
	public void clearUrgentAdvisories(Integer[] agencies) {
		logger.debug(loggerName, "clearing for agency: "+ToStringBuilder.reflectionToString(agencies));
		logger.debug(loggerName,"before...."+ TweetStore.getInstance().getUrgentTweets(ArrayUtils.toPrimitive(agencies)).toString());
		TweetStore.getInstance().clearUrgentAdvisories(agencies);
		logger.debug(loggerName,"after...."+ TweetStore.getInstance().getUrgentTweets(ArrayUtils.toPrimitive(agencies)).toString());
		logger.debug(loggerName, "done....");
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
	 * Gets the current push interval name.
	 *
	 * @return the current push interval name
	 */
	private String getCurrentPushIntervalName(TimeZone timeZone){
		Calendar c =  Calendar.getInstance();
		c.setTimeZone(timeZone);
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
			return user.getLastPushTime();
		} else {
			try {
				String strMethod = AGENCY_TYPE.values()[agencyId].getPushTimeColumnName();
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


	public BartAlertCriteria getBartAlertCriteria() {
		return bartAlertCriteria;
	}

	public void setBartAlertCriteria(BartAlertCriteria bartAlertCriteria) {
		this.bartAlertCriteria = bartAlertCriteria;
	}

	public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}


	public void setAgencyTweetSourceIconMap(
			Map<String, String> agencyTweetSourceIconMap) {
		this.agencyTweetSourceIconMap = agencyTweetSourceIconMap;
	}
}