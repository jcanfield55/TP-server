package com.nimbler.tp.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.Tweet;
import com.nimbler.tp.dataobject.TweetResponse;
import com.nimbler.tp.dbobject.User;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.twitter.TweetStore;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
import com.nimbler.tp.util.TpException;

/**
 * 
 * @author suresh
 *
 */
@Path("/advisories/")
public class AdvisoriesRestService {

	private LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private String loggerName;
	@GET
	@Path("/count/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTweetCount(@QueryParam(RequestParam.DEVICE_ID)String deviceid) {
		int tweetCount =0;
		TweetResponse response = new TweetResponse();
		try {
			if (deviceid == null || "".equals(deviceid))
				throw new TpException(TP_CODES.INVALID_REQUEST);
			PersistenceService persistenceService = BeanUtil.getPersistanceService();
			List<User> resultSet = (List<User>) persistenceService.find(MONGO_TABLES.users.name(), TpConstants.DEVICE_ID, deviceid, User.class);
			if (resultSet !=null && resultSet.size()>0) {
				long lastSentTime = resultSet.get(0).getLastAlertTime();
				List<Tweet> tweet = TweetStore.getInstance().getTweet();
				if (tweet != null && tweet.size()>0) {
					for (Tweet tweets : tweet) {
						if (tweets.getTime() > lastSentTime)
							tweetCount++;
					}
				}
				if (tweetCount >0) {
					if (tweetCount==tweet.size())
						response.setAllNew(true);
				}
			} else
				logger.warn(loggerName, "No deviceid found in database.");
		} catch (DBException e) {
			logger.error(loggerName, e.getErrMsg());
			response.setErrCode(TP_CODES.FAIL.getCode()); 
		} catch (TpException e) {
			logger.error(loggerName, e.getErrMsg());
			response.setErrCode(e.getErrCode());
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setErrCode(TP_CODES.FAIL.getCode()); 
		}
		response.setTweetCount(tweetCount);
		return getJsonResponse(response);
	}
	@GET
	@Path("/all/")
	public String getAllTweets(@QueryParam(RequestParam.DEVICE_ID)String deviceid) {
		TweetResponse response = new TweetResponse();
		try {
			List<Tweet> allTweet = new ArrayList<Tweet>(); 
			List<Tweet> generalTweets = TweetStore.getInstance().getTweet();
			if (generalTweets !=null && generalTweets.size()>0)
				allTweet.addAll(generalTweets);

			List<Tweet> urgentTweets = TweetStore.getInstance().getUrgentTweets();
			if (urgentTweets !=null &&urgentTweets.size()>0)
				allTweet.addAll(urgentTweets); 	

			if (allTweet.size()==0) {
				logger.error(loggerName, "No Tweets are available.");
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
			if (deviceid!=null) {
				PersistenceService persistenceService = BeanUtil.getPersistanceService();
				long lastSeenTime = System.currentTimeMillis();
				Map<String, Object> columnsToUpdate = new HashMap<String, Object>(); 
				columnsToUpdate.put(TpConstants.LAST_ALERT_TIME, lastSeenTime);
				columnsToUpdate.put(TpConstants.LAST_PUSH_TIME, lastSeenTime);
				persistenceService.updateMultiColumn(MONGO_TABLES.users.name(), TpConstants.DEVICE_ID, deviceid, columnsToUpdate);
				//				persistenceService.updateSingleObject(MONGO_TABLES.users.name(), TpConstants.DEVICE_ID, deviceid, TpConstants.LAST_ALERT_TIME, lastSeenTime);
				//				persistenceService.updateSingleObject(MONGO_TABLES.users.name(), TpConstants.DEVICE_ID, deviceid, TpConstants.LAST_PUSH_TIME, lastSeenTime);
			}
		} catch (TpException tpe) {
			logger.error(loggerName, tpe.getErrMsg());
			response.setError(tpe.getErrCode());
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setError(TP_CODES.FAIL.getCode());
		}
		return getJsonResponse(response);
	}
	@GET
	@Path("/latest/")
	public String getTweetsAfterTime(@QueryParam("tweetTime") long tweetTime, @QueryParam(RequestParam.DEVICE_ID)String deviceid) {
		TweetResponse response = new TweetResponse();
		try {
			List<Tweet> list = TweetStore.getInstance().getTweet();
			if(list == null || list.size() == 0) {
				logger.debug(loggerName, "No Tweets are available at this time."); 
				response.setError(TP_CODES.DATA_NOT_EXIST.getCode());
				throw new TpException(TP_CODES.DATA_NOT_EXIST.getCode());
			}
			List<Tweet> latestTweets = new ArrayList<Tweet>();
			for (Tweet tweet: list) {
				if (tweet.getTime() > tweetTime) {
					latestTweets.add(tweet);
				}
			}
			if (latestTweets.size()>0) {
				response.setTweet(latestTweets);
				if (deviceid!=null) {
					PersistenceService persistenceService = BeanUtil.getPersistanceService();
					long lastSeenTime = System.currentTimeMillis();
					persistenceService.updateSingleObject(MONGO_TABLES.users.name(), TpConstants.DEVICE_ID, deviceid, TpConstants.LAST_ALERT_TIME, lastSeenTime);
					persistenceService.updateSingleObject(MONGO_TABLES.users.name(), TpConstants.DEVICE_ID, deviceid, TpConstants.LAST_PUSH_TIME, lastSeenTime);
				}
			}
			//			else 
			//				response.setErrCode(TP_CODES.DATA_NOT_EXIST.getCode());
			response.setTweetCount(latestTweets.size());
		} catch (TpException tpe) {
			logger.debug(loggerName, tpe.getErrMsg());
			response.setError(tpe.getErrCode());
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setError(TP_CODES.FAIL.getCode());
		}
		return getJsonResponse(response);
	}
	@GET
	@Path("/reset/")
	@Produces(MediaType.TEXT_PLAIN)
	public String resets() {
		BeanUtil.getCaltrainService().resetAllCounters();
		return "done";
	}
	@GET
	@Path("/incrementCounters/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getIncrementCounters() {
		return BeanUtil.getCaltrainService().getThresholdBoards().toString();
	}
	@GET
	@Path("/tweets/")
	@Produces(MediaType.TEXT_PLAIN)
	public String testGetTweets() {
		return TweetStore.getInstance().getTweet().toString();
	}

	
	public String add(@QueryParam("t") String t) {
		Tweet tweet = new Tweet();
		tweet.setIsUrgent(false);
		tweet.setTime(System.currentTimeMillis());
		tweet.setTweet(t);		
		List<Tweet> lst = new ArrayList<Tweet>();
		lst.add(tweet);
		lst.addAll(TweetStore.getInstance().getTweet());
		TweetStore.getInstance().setTweet(lst);
		return TweetStore.getInstance().getTweet().toString();
	}
	/**
	 * 
	 * @param response
	 * @return
	 */
	private String getJsonResponse(Object response) {
		try {
			return JSONUtil.getJsonFromObj(response);
		} catch (TpException e) {
			logger.error(loggerName, e.getMessage());  
		}
		return "";
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
}