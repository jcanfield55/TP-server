package com.nimbler.tp.rest;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.math.NumberUtils;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.dataobject.TweetResponse;
import com.nimbler.tp.service.APNService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.advisories.AdvisoriesService;
import com.nimbler.tp.service.twitter.TweetStore;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;
import com.nimbler.tp.util.TpException;

/**
 * 
 * @author nIKUNJ
 *
 */
@Path("/advisories/")
public class AdvisoriesRestService {

	private LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private String loggerName;

	@GET
	@Path("/count/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTweetCount(@QueryParam(RequestParam.DEVICE_ID)String deviceid, @QueryParam(RequestParam.NIMBLER_APP_TYPE)Integer appType,
			@QueryParam(RequestParam.AGENCY_IDS)String reqAgencyIds, @QueryParam(RequestParam.DEVICE_TOKEN)String deviceToken) {
		//		System.out.println("getTweetCount"+"-->"+new Date());
		TweetResponse response = null;  
		if (appType == null || appType ==0) {
			appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal();
		}
		int[] agencyIds = null;;
		if (reqAgencyIds == null)
			agencyIds = new int[]{AGENCY_TYPE.CALTRAIN.ordinal()};
		else
			agencyIds = getAgencyIdsFromReq(reqAgencyIds); 
		AdvisoriesService advisoriesService = BeanUtil.getAdvisoriesService();
		response =  advisoriesService.getAdvisoryCount(deviceid, deviceToken, appType, agencyIds);  
		return getJsonResponse(response);
	}

	@GET
	@Path("/all/")
	public String getAllTweets(@QueryParam(RequestParam.DEVICE_ID)String deviceid, @QueryParam(RequestParam.NIMBLER_APP_TYPE)Integer appType,
			@QueryParam(RequestParam.AGENCY_IDS)String reqAgencyIds, @QueryParam(RequestParam.DEVICE_TOKEN)String deviceToken) {

		TweetResponse response = null;  
		if (appType == null || appType ==0) {
			appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal();
		}
		int[] agencyIds = null;;
		if (reqAgencyIds == null)
			agencyIds = new int[]{AGENCY_TYPE.CALTRAIN.ordinal()};
		else
			agencyIds = getAgencyIdsFromReq(reqAgencyIds);

		AdvisoriesService advisoriesService = BeanUtil.getAdvisoriesService();
		response =  advisoriesService.getAllAdvisories(deviceid, deviceToken, appType, agencyIds);		
		return getJsonResponse(response);  
	}

	@GET
	@Path("/latest/")
	public String getTweetsAfterTime(@QueryParam("tweetTime") long tweetTime, @QueryParam(RequestParam.DEVICE_ID)String deviceid,
			@QueryParam(RequestParam.NIMBLER_APP_TYPE)Integer appType, @QueryParam(RequestParam.AGENCY_IDS)String reqAgencyIds,
			@QueryParam(RequestParam.DEVICE_TOKEN)String deviceToken) {
		System.out.println("getTweetsAfterTime"+"-->"+new Date());
		TweetResponse response = null;  
		if (appType == null || appType ==0) {
			appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal();
		}
		int[] agencyIds = null;;
		if (reqAgencyIds == null)
			agencyIds = new int[]{AGENCY_TYPE.CALTRAIN.ordinal()};
		else
			agencyIds = getAgencyIdsFromReq(reqAgencyIds);

		AdvisoriesService advisoriesService = BeanUtil.getAdvisoriesService();
		response =  advisoriesService.getAdvisoriesAfterTime(deviceid, deviceToken, tweetTime, appType, agencyIds);  
		return getJsonResponse(response);  
	}

	/* @GET
 @Path("/reset/")
 @Produces(MediaType.TEXT_PLAIN)
 public String resets() {
  BeanUtil.getCaltrainService().resetAllCounters();
  return "done";
 }*/

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
		String[] arr = reqAgencyIds.split(",");
		int[] agencyIds = new int[arr.length];
		for (int i=0;i<arr.length;i++) {
			agencyIds[i] = NumberUtils.toInt(arr[i].trim()); 
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