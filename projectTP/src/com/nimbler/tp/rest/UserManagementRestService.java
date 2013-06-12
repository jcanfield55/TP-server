/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.dataobject.NimblerApps;
import com.nimbler.tp.dataobject.NimblerGtfsAgency;
import com.nimbler.tp.dataobject.RequestMap;
import com.nimbler.tp.dataobject.TPCountResponse;
import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.dataobject.UserStatistics;
import com.nimbler.tp.dbobject.User;
import com.nimbler.tp.gtfs.GtfsDataService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.UserManagementService;
import com.nimbler.tp.service.flurry.FlurryManagementService;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.HtmlUtil;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.PersistantHelper;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.ResponseUtil;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;
import com.nimbler.tp.util.TpException;
/**
 * 
 * @author suresh
 *
 */
@Path("/users/")
@SuppressWarnings("unchecked")
public class UserManagementRestService {

	@Autowired
	private LoggingService logger;

	@Autowired
	private UserManagementService userMgmtService;

	@Autowired
	private FlurryManagementService flurryMgmtService;

	@Autowired
	private NimblerApps nimblerApps;

	@Autowired
	GtfsDataService gtfsDataService;

	private String loggerName;

	//@GET
	//@Path("/preferences/update")
	//@Produces(MediaType.TEXT_PLAIN)
	@Deprecated
	public String saveAlertPreference(@QueryParam(RequestParam.DEVICE_ID)String deviceid,
			@DefaultValue("-2") @QueryParam(RequestParam.ALERT)int alertCount,
			@QueryParam(RequestParam.DEVICE_TOKEN)String deviceToken,
			@QueryParam(RequestParam.ENABLE_STD_NOTIFICATION)int enableStdNot,
			@QueryParam(RequestParam.ENABLE_URGENT_NOTIFICATION)int enableUrgntNot,
			@QueryParam(RequestParam.MAX_DISTANCE)String maxWalkDistance,

			@DefaultValue("2") @QueryParam(RequestParam.ADV_ENABLE_SF_MUNI)int enableSfMuniAdv,
			@DefaultValue("2") @QueryParam(RequestParam.ADV_ENABLE_BART)int enableBartAdv,
			@DefaultValue("1") @QueryParam(RequestParam.ADV_ENABLE_CALTRAIN)int enableCaltrainAdv,
			@DefaultValue("2") @QueryParam(RequestParam.ADV_ENABLE_AC_TRANSIT)int enableAcTransitAdv,
			@DefaultValue("1") @QueryParam(RequestParam.ADV_ENABLE_WMATA)int enableWmata,

			@QueryParam(RequestParam.TRANSIT_MODE)int transitMod,
			@QueryParam(RequestParam.MAX_BIKE_DISTANCE)double maxBikeDist,

			@DefaultValue("1") @QueryParam(RequestParam.NOTIF_TIMING_MORNING)int notifTimingMorning,
			@DefaultValue("2") @QueryParam(RequestParam.NOTIF_TIMING_MIDDAY) int notifTimingMidday, 
			@DefaultValue("1") @QueryParam(RequestParam.NOTIF_TIMING_EVENING)int notifTimingEvening,
			@DefaultValue("2") @QueryParam(RequestParam.NOTIF_TIMING_NIGHT)  int notifTimingNight,  
			@DefaultValue("2") @QueryParam(RequestParam.NOTIF_TIMING_WEEKEND)int notifTimingWeekend,

			@QueryParam(RequestParam.BIKE_TRIANGLE_BIKEFRIENDLY)double bikeTriangleBikeFriendly,
			@QueryParam(RequestParam.BIKE_TRIANGLE_FLAT)double bikeTriangleFlat,
			@QueryParam(RequestParam.BIKE_TRIANGLE_QUICK)double bikeTriangleQuick,
			@QueryParam(RequestParam.APP_VERSION) String appVersion,

			@DefaultValue("1") @QueryParam(RequestParam.NIMBLER_APP_TYPE)int appType) throws TpException {

		TPResponse response = new TPResponse();
		try {
			if (deviceid == null || alertCount == -2 || deviceToken == null || maxWalkDistance == null)
				throw new TpException(TP_CODES.INVALID_REQUEST);

			logger.debug(loggerName, "Preferences are :"+deviceid+","+ alertCount + ","+ deviceToken+","+maxWalkDistance);

			User reqUserValue = new User();
			reqUserValue.setDeviceToken(deviceToken);
			reqUserValue.setNumberOfAlert(alertCount);
			reqUserValue.setDeviceId(deviceid);
			reqUserValue.setEnableStdNotifSound(enableStdNot);
			reqUserValue.setEnableUrgntNotifSound(enableUrgntNot);
			reqUserValue.setMaxWalkDistance(maxWalkDistance);
			reqUserValue.setAppType(appType);

			reqUserValue.setEnableAcTransitAdv(enableAcTransitAdv);
			reqUserValue.setEnableBartAdv(enableBartAdv);
			reqUserValue.setEnableCaltrainAdv(enableCaltrainAdv);
			reqUserValue.setEnableSfMuniAdv(enableSfMuniAdv);
			reqUserValue.setEnableWmataAdv(enableWmata);
			reqUserValue.setNotifTimingMorning(notifTimingMorning);
			reqUserValue.setNotifTimingMidday(notifTimingMidday);
			reqUserValue.setNotifTimingEvening(notifTimingEvening);
			reqUserValue.setNotifTimingNight(notifTimingNight);
			reqUserValue.setNotifTimingWeekend(notifTimingWeekend);

			reqUserValue.setBikeTriangleBikeFriendly(bikeTriangleBikeFriendly);
			reqUserValue.setBikeTriangleFlat(bikeTriangleFlat);
			reqUserValue.setBikeTriangleQuick(bikeTriangleQuick);

			reqUserValue.setTransitMode(transitMod);
			reqUserValue.setMaxBikeDist(maxBikeDist);
			reqUserValue.setAppVer(appVersion);
			userMgmtService.saveAlertPreferences(reqUserValue);
			response.setCode(TP_CODES.SUCESS.getCode()); 
		} catch (TpException e) {
			logger.error(loggerName, e.getErrMsg());
			response.setCode(e.getErrCode()); 
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setCode(TP_CODES.FAIL.ordinal());
		}
		return getJsonResponse(response);
	}

	/**
	 * Save alert preference abstract.
	 *
	 * @param request the request
	 * @return the string
	 * @throws TpException the tp exception
	 */
	@GET
	@Path("/preferences/update")
	@Produces(MediaType.TEXT_PLAIN)
	public String saveAlertPreferenceAbstract(@Context HttpServletRequest request) throws TpException {
		TPResponse response = new TPResponse();
		try {			
			Map<String,String[]> req = request.getParameterMap();
			userMgmtService.saveAlertPreferences(RequestMap.of(req));
			response.setCode(TP_CODES.SUCESS.getCode());
		} catch (TpException e) {
			logger.error(loggerName, e.getErrMsg());
			response.setCode(e.getErrCode());
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setCode(TP_CODES.FAIL.ordinal());
		}
		return getJsonResponse(response);
	}

	/**
	 * Update device token.
	 *
	 * @param deviceToken the device token
	 * @param dummyId the dummy id
	 * @param appType the app type
	 * @return the string
	 */
	@GET
	@Path("/preferences/update/token")	
	public String updateDeviceToken(@QueryParam(RequestParam.DEVICE_TOKEN)String deviceToken,
			@QueryParam(RequestParam.DUMMY_TOKEN_ID)String dummyId,@QueryParam(TpConstants.APP_TYPE)String appType){
		TPResponse response = null;
		try {
			if(ComUtils.isEmptyString(deviceToken) || ComUtils.isEmptyString(dummyId) || ComUtils.isEmptyString(appType))
				throw new TpException(TP_CODES.INVALID_REQUEST);
			logger.debug(loggerName, "Dummy token updated: "+dummyId+"-->"+deviceToken);			
			int count = userMgmtService.updateToken(deviceToken,dummyId,Integer.parseInt(appType));
			response = new TPResponse((count>0)?TP_CODES.SUCESS:TP_CODES.DATA_NOT_EXIST);
		} catch (TpException e) {			
			response = ResponseUtil.createResponse(e);
		}catch (Exception e) {
			logger.error(loggerName, e);
			response = new TPResponse(TP_CODES.FAIL);
		}
		return getJsonResponse(response);
	}

	/**
	 * Save alert preference.
	 *
	 * @param deviceTocken the device tocken
	 * @param appType the app type
	 * @return the string
	 */
	@GET
	@Path("/preferences/get")
	@Produces(MediaType.TEXT_PLAIN)
	public String saveAlertPreference(@QueryParam(RequestParam.DEVICE_TOKEN)String deviceTocken,
			@QueryParam(TpConstants.APP_TYPE) String appType
			){
		String res = "Not Found";
		try {
			if(appType==null || deviceTocken==null)
				return res;
			User user = userMgmtService.getUserByDeviceToken(deviceTocken);
			if(user!=null)
				res = JSONUtil.getJsonFromObj(user);
		} catch (TpException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Gets the counts.
	 *
	 * @return the counts
	 * @throws TpException the tp exception
	 */
	@GET
	@Path("/statistics/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCounts() throws TpException {
		TPCountResponse response = new TPCountResponse();
		try {
			PersistantHelper persistanceHelper = new PersistantHelper();
			response.setTotalUserCount(persistanceHelper.getTotalUserCount());
			response.setTotalPlanCount(persistanceHelper.getTotalPlanCount());
			response.setTotalFeedbackCount(persistanceHelper.getTotalFeedbackCount());
			response.setTotalItineraryCount(persistanceHelper.getTotalItineraryCount());
			response.setTotalLegCount(persistanceHelper.getTotalLegCount());

			response.setLast24hourUserCount(persistanceHelper.getLast24hourUserCount());
			response.setLast24hourUpdateUserCount(persistanceHelper.getLast24hourUserUpdateCount());
			response.setLast24hourPlanCount(persistanceHelper.getLast24hourPlanCount());
			response.setLast24hourFeedbackCount(persistanceHelper.getLast24hourFeebackCount());

			logger.debug(loggerName, response.toString());
			response.setCode(TP_CODES.SUCESS.getCode()); 
		} catch (Exception e) {
			logger.error(loggerName, e);
			response.setCode(TP_CODES.FAIL.ordinal());
		}
		return getJsonResponse(response);
	}

	/**
	 * Gets the user state.
	 *
	 * @return the user state
	 * @throws TpException the tp exception
	 */
	@GET
	@Path("/state/")
	@Produces(MediaType.TEXT_HTML)
	public String getUserState() throws TpException {
		try {
			PersistantHelper persistanceHelper = new PersistantHelper();
			List<NIMBLER_APP_TYPE> appTypes = new ArrayList<TpConstants.NIMBLER_APP_TYPE>();
			appTypes.add(NIMBLER_APP_TYPE.CALTRAIN);
			appTypes.add(NIMBLER_APP_TYPE.SF_BAY_AREA);

			List<UserStatistics> lstStatistics = new ArrayList<UserStatistics>();
			for (NIMBLER_APP_TYPE type : appTypes) {
				UserStatistics statistics =  persistanceHelper.getUserStatistics(type.ordinal());
				lstStatistics.add(statistics);
			}			
			String res = HtmlUtil.getUserStatistics(lstStatistics);
			return res; 
		} catch (Exception e) {
			logger.error(loggerName, e);			
		}
		return "Error While getting data";
	}

	/**
	 * Gets the orphan gtfs route tag.
	 *
	 * @return the orphan gtfs route tag
	 * @throws TpException the tp exception
	 */
	@GET
	@Path("/orphanGtfsRoutes/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getorphanGtfsRouteTag() throws TpException {		
		try {
			Map<String, Collection<String>> map =  BeanUtil.getNextBusApiImpl().getOrphanGtfsRouteTag().asMap();
			return JSONUtil.getJsonFromObj(map);
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
		return "Not Found";
	}

	/**
	 * Request flurry.
	 *
	 * @param start the start
	 * @param end the end
	 * @return the string
	 * @throws TpException the tp exception
	 */
	@GET
	@Path("/flurry/")
	@Produces(MediaType.TEXT_PLAIN)
	public String requestFlurry(@QueryParam("start") String start,@QueryParam("end") String end) throws TpException {
		TPResponse response = ResponseUtil.createResponse(TP_CODES.SUCESS);
		try {
			flurryMgmtService.requestDailyFlurryReport(Long.parseLong(start), Long.parseLong(end));
		} catch (NumberFormatException e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(TP_CODES.INVALID_REQUEST);
		} catch (Exception e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(TP_CODES.FAIL);
		}
		return getJsonResponse(response);
	}

	/**
	 * Gets the app type.
	 *
	 * @param appBundleId the app bundle id
	 * @return the app type
	 */
	@GET
	@Path("getAppType")
	public String getAppType(
			@QueryParam(RequestParam.NIMBLER_APP_BUNDLE_ID) String appBundleId) {
		String res = "";
		try {
			TPResponse response = new TPResponse(TP_CODES.SUCESS);
			response.setAppType(getAppTypeFromAppBundleId(appBundleId)+"");
			response.setAppBundleId(appBundleId);
			return JSONUtil.getJsonFromObj(response);
		} catch (Exception e) {
			logger.error(loggerName, e);
			JSONUtil.getResponseJSON(new TPResponse(TP_CODES.FAIL));
		}
		return res;
	}

	/**
	 * Gets the app agency details.
	 *
	 * @param appType the app type
	 * @return the app agency details
	 */
	@GET
	@Path("getAppAgencies")
	public String getAppAgencyDetails(
			@QueryParam(TpConstants.APP_TYPE) Integer appType) {
		TPResponse tpResponse = null;
		try {
			List<NimblerGtfsAgency> agencies = gtfsDataService.getNimblerAgencyDetailsForApp(appType,false);
			if(agencies!=null){
				tpResponse = ResponseUtil.createResponse(TP_CODES.SUCESS);
				tpResponse.setAgencies(agencies);
			}else{
				tpResponse = ResponseUtil.createResponse(TP_CODES.DATA_NOT_EXIST);
			}
		} catch (TpException e) {
			tpResponse = ResponseUtil.createResponse(e);
		} catch (Exception e) {
			logger.error(loggerName, e);
			tpResponse =new TPResponse(TP_CODES.FAIL);
		}
		return JSONUtil.getResponseJSON(tpResponse);
	}

	/**
	 * Gets the app type from app bundle id.
	 *
	 * @param appBundleId the app bundle id
	 * @return the app type from app bundle id
	 */
	private int getAppTypeFromAppBundleId(String appBundleId) {
		if (StringUtils.isEmpty(appBundleId))
			return NIMBLER_APP_TYPE.CALTRAIN.ordinal(); //default for current caltrain app users
		return nimblerApps.getAppBundleToAppIdentifierMap().get(appBundleId.trim());
	}

	/**
	 * Gets the json response.
	 *
	 * @param response the response
	 * @return the json response
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
	 * Sets the logger name.
	 *
	 * @param loggerName the new logger name
	 */
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	/**
	 * Gets the logger name.
	 *
	 * @return the logger name
	 */
	public String getLoggerName() {
		return loggerName;
	}

	public LoggingService getLogger() {
		return logger;
	}

	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}

	public UserManagementService getUserMgmtService() {
		return userMgmtService;
	}

	public void setUserMgmtService(UserManagementService userMgmtService) {
		this.userMgmtService = userMgmtService;
	}

	public FlurryManagementService getFlurryMgmtService() {
		return flurryMgmtService;
	}

	public void setFlurryMgmtService(FlurryManagementService flurryMgmtService) {
		this.flurryMgmtService = flurryMgmtService;
	}

	public NimblerApps getNimblerApps() {
		return nimblerApps;
	}

	public void setNimblerApps(NimblerApps nimblerApps) {
		this.nimblerApps = nimblerApps;
	}

	public GtfsDataService getGtfsDataService() {
		return gtfsDataService;
	}

	public void setGtfsDataService(GtfsDataService gtfsDataService) {
		this.gtfsDataService = gtfsDataService;
	}

}