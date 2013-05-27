/*
 * @author nirmal
 */
package com.nimbler.tp.rest;

import java.io.File;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.dataobject.TweetResponse;
import com.nimbler.tp.dbobject.AdvisoryFetchLog;
import com.nimbler.tp.dbobject.AdvisoryFetchLog.ADVISORY_FETCH_EVENT;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.APNService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.advisories.AdvisoriesService;
import com.nimbler.tp.service.twitter.TweetStore;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.RequestParam;
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
@Path("/advisories/")
public class AdvisoriesRestService {

	@Autowired
	private LoggingService logger;
	private String loggerName;

	@GET
	@Path("/count/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTweetCount(@QueryParam(RequestParam.DEVICE_ID)String deviceid, @QueryParam(RequestParam.NIMBLER_APP_TYPE)Integer appType,
			@QueryParam(RequestParam.AGENCY_IDS)String reqAgencyIds, @QueryParam(RequestParam.DEVICE_TOKEN)String deviceToken,
			@QueryParam(RequestParam.APP_VERSION)String appVersion) {
		//		System.out.println("getTweetCount"+"-->"+new Date());
		TweetResponse response = null;  
		if (appType == null || appType ==0) {
			appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal();
		}
		int[] agencyIds = getAgencyIdsFromReq(reqAgencyIds);

		saveAdvisoryFetchLog(deviceid, deviceToken, appType, reqAgencyIds, appVersion, null, ADVISORY_FETCH_EVENT.COUNT.ordinal());
		AdvisoriesService advisoriesService = BeanUtil.getAdvisoriesService();
		response =  advisoriesService.getAdvisoryCount(deviceid, deviceToken, appType, agencyIds);  
		return getJsonResponse(response);
	}


	@GET
	@Path("/all/")
	public String getAllTweets(@QueryParam(RequestParam.DEVICE_ID)String deviceid, @QueryParam(RequestParam.NIMBLER_APP_TYPE) Integer appType,
			@QueryParam(RequestParam.AGENCY_IDS)String reqAgencyIds, @QueryParam(RequestParam.DEVICE_TOKEN)String deviceToken,
			@QueryParam(RequestParam.APP_VERSION)String appVersion) {

		TweetResponse response = null;  
		if (appType == null || appType ==0) {
			appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal();
		}
		int[] agencyIds = getAgencyIdsFromReq(reqAgencyIds);

		saveAdvisoryFetchLog(deviceid, deviceToken, appType, reqAgencyIds, appVersion, null, ADVISORY_FETCH_EVENT.ALL.ordinal());

		AdvisoriesService advisoriesService = BeanUtil.getAdvisoriesService();
		response =  advisoriesService.getAllAdvisories(deviceid, deviceToken, appType, agencyIds);		
		return getJsonResponse(response);  
	}

	@GET
	@Path("/latest/")
	public String getTweetsAfterTime(@QueryParam("tweetTime") long tweetTime, @QueryParam(RequestParam.DEVICE_ID)String deviceid,
			@QueryParam(RequestParam.NIMBLER_APP_TYPE)Integer appType, @QueryParam(RequestParam.AGENCY_IDS)String reqAgencyIds,
			@QueryParam(RequestParam.DEVICE_TOKEN)String deviceToken,
			@QueryParam(RequestParam.APP_VERSION)String appVersion) {
		//		System.out.println("getTweetsAfterTime"+"-->"+new Date());
		TweetResponse response = null;  
		if (appType == null || appType ==0) {
			appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal();
		}
		int[] agencyIds = getAgencyIdsFromReq(reqAgencyIds);

		saveAdvisoryFetchLog(deviceid,deviceToken,appType,reqAgencyIds,appVersion,tweetTime,ADVISORY_FETCH_EVENT.LATEST.ordinal());
		AdvisoriesService advisoriesService = BeanUtil.getAdvisoriesService();
		response =  advisoriesService.getAdvisoriesAfterTime(deviceid, deviceToken, tweetTime, appType, agencyIds);  
		return getJsonResponse(response);  
	}



	/**
	 * Save advisory fetch log.
	 *
	 * @param deviceid the deviceid
	 * @param deviceToken the device token
	 * @param appType the app type
	 * @param reqAgencyIds the req agency ids
	 * @param appVersion the app version
	 * @param tweetTime the tweet time
	 * @param type the type
	 */
	private void saveAdvisoryFetchLog(String deviceid, String deviceToken,Integer appType, String reqAgencyIds, 
			String appVersion,Long tweetTime, int type) {
		try {
			AdvisoryFetchLog log = new  AdvisoryFetchLog();
			log.setCreateTime(System.currentTimeMillis());
			log.setDeviceId(deviceid);
			log.setDeviceToken(deviceToken);
			log.setAppType(appType);
			log.setAgencyIds(reqAgencyIds);
			log.setAppVersion(appVersion);
			log.setLastTweetTime(tweetTime);
			log.setType(type);
			PersistenceService persistenceService = BeanUtil.getPersistanceService();
			persistenceService.addObject(MONGO_TABLES.advisory_fetch_log.name(), log);
		} catch (Exception e) {
			logger.error(loggerName, e);
		}

	}

	@GET
	@Path("/incrementCounters/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getIncrementCounters() {
		try {
			return JSONUtil.getJsonFromObj(BeanUtil.getAdvisoriesPushServiceBean().getThresholdBoards());
		} catch (TpException e) {
			e.printStackTrace();
		}
		return "";
	}

	@GET
	@Path("/tweets/")
	@Produces(MediaType.TEXT_PLAIN)
	public String testGetTweets() {
		return TweetStore.getInstance().getAllTweets().toString();
	}

	@GET
	@Path("/push/")
	@Produces(MediaType.TEXT_PLAIN)
	public String testPush(@QueryParam("token") String token, @QueryParam("msg") String msg, @QueryParam("appType") String appType) {
		try {
			if(ComUtils.isEmptyString(token) || ComUtils.isEmptyString(msg))
				return "Invalid";
			APNService apnService =  BeanUtil.getApnService();
			apnService.push(token, msg, 1, false, true, NIMBLER_APP_TYPE.values()[NumberUtils.toInt(appType, NIMBLER_APP_TYPE.CALTRAIN.ordinal())]);
			return "done";
		} catch (Exception e) {
			logger.error(loggerName,e);
		}
		return "error";
	}

	/**
	 * Gets the agency image.
	 * @return 
	 *
	 * @return the agency image
	 */
	@GET
	@Path("/download/{image}")
	@Produces("image/*")
	public Response getAgencyImage(@PathParam("image") String strImage) {
		File image = new File(TpConstants.DOWNLOAD_IMAGE_PATH+"/"+strImage);
		if (!image.exists()) {
			throw new WebApplicationException(404);
		}
		String mimeType = new MimetypesFileTypeMap().getContentType(image);
		return Response.ok(image, mimeType).build();
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
	 * @param reqAgencyIds
	 * @return
	 */
	private int[] getAgencyIdsFromReq(String reqAgencyIds) {
		int[] agencyIds = null;;
		if (reqAgencyIds == null){
			agencyIds = new int[]{AGENCY_TYPE.CALTRAIN.ordinal()};
		}else{
			String[] arr = reqAgencyIds.split(",");
			agencyIds = new int[arr.length];
			for (int i=0;i<arr.length;i++) {
				agencyIds[i] = NumberUtils.toInt(arr[i].trim()); 
			}
		}
		return agencyIds;
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