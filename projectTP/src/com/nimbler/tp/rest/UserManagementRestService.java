/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd.
 * All rights reserved.
 *
 */
package com.nimbler.tp.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.dataobject.NimblerApps;
import com.nimbler.tp.dataobject.TPCountResponse;
import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.dbobject.User;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.UserManagementService;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.PersistantHelper;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;
import com.nimbler.tp.util.TpException;
/**
 * 
 * @author suresh
 *
 */
@Path("/users/")
public class UserManagementRestService {

	private LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private String loggerName;

	@GET
	@Path("/preferences/update")
	@Produces(MediaType.TEXT_PLAIN)
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

			@DefaultValue("1") @QueryParam(RequestParam.NIMBLER_APP_TYPE)int appType) throws TpException {

		TPResponse response = new TPResponse();
		try {
			if (deviceid == null || alertCount == -2 || deviceToken == null || maxWalkDistance == null)
				throw new TpException(TP_CODES.INVALID_REQUEST);

			UserManagementService alertService = BeanUtil.getUserManagementService();
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

			alertService.saveAlertPreferences(reqUserValue);
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
			UserManagementService userManagementService = BeanUtil.getUserManagementService();
			User user = userManagementService.getUserByDeviceToken(deviceTocken);
			if(user!=null)
				res = JSONUtil.getJsonFromObj(user);
		} catch (TpException e) {
			e.printStackTrace();
		}
		return res;
	}

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
	@GET
	@Path("getAppType")
	@Produces(MediaType.TEXT_PLAIN)
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
	 * 
	 * @param appBundleId
	 * @return
	 */
	private int getAppTypeFromAppBundleId(String appBundleId) {
		NimblerApps apps = BeanUtil.getNimblerAppsBean();
		if (StringUtils.isEmpty(appBundleId))
			return NIMBLER_APP_TYPE.CALTRAIN.ordinal(); //default for current caltrain app users
		else
			return apps.getAppBundleToAppIdentifierMap().get(appBundleId.trim());
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
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public String getLoggerName() {
		return loggerName;
	}
}