/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.jobs.OtpMonitoringTask;
import com.nimbler.tp.service.smtp.MailService;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpProperty;
/**
 * 
 * @author suresh
 *
 */
public class OtpMonitoringService{

	private String loggerName;
	@Autowired
	private MailService mailService;
	@Autowired
	private LoggingService logger;

	private static int currentState = 0;
	private static String[] wsplanUrl = TpConstants.WS_PLAN.split(",");

	/**
	 * 
	 */
	public void init() {
		try {
			new OtpMonitoringTask().scheduling();
			wsplanUrl = TpConstants.WS_PLAN.split(",");
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
	}
	/**
	 * check otp server status
	 */
	public void checkOtpServerStatus(){
		if (isMailEnable()) {
			checkOtpServerResponseTime(wsplanUrl[currentState++]);
			if (currentState == wsplanUrl.length) 
				currentState= 0;
		} else {
			logger.debug(loggerName, "Mail disbled, skipping check...");
		}
	}
	private boolean isMailEnable() {
		boolean res  = false;
		try {
			TpProperty property = new TpProperty(TpConstants.FILE_OTP_PROPERTY);			
			res = BooleanUtils.toBoolean(property.getProperty("fail.notification.enable"));
		} catch (IOException e) {
			logger.error(loggerName,"Error while reading notification flag "+ e.getMessage());
		}
		return res;
	}
	/**
	 * It checks the otp server response time.
	 * It also take otp server resonse.If server contain error message it sends the message to admin.
	 * It also send mail if otpserver time is more then 10 sec.
	 */
	private void checkOtpServerResponseTime(String wsPlanUrl) {
		try {		
			logger.debug(loggerName, "Chek for plan:"+wsPlanUrl);
			Object[] arrResponse = getOtpServerData(wsPlanUrl);
			if(ComUtils.isEmptyString((String)arrResponse[0])){
				logger.error(loggerName, "Error while connecting to server: "+arrResponse[2]);
				mailService.sendMailForOtpCheckFail("Error while connecting to server: "+arrResponse[2]);
				logger.debug(loggerName, "Mail Sent...");
			}else if ((JSONUtil.jPath((String)arrResponse[0], TpConstants.RESPONSE_PLAN)) ==null) {
				Integer id = (Integer) JSONUtil.jPath((String)arrResponse[0], TpConstants.RESPONSE_ERROR_ID);
				String msg =(String) JSONUtil.jPath((String)arrResponse[0], TpConstants.RESPONSE_ERROR_MSG);
				Boolean nopath =(Boolean) JSONUtil.jPath((String)arrResponse[0], TpConstants.RESPONSE_ERROR_NOPATH);
				String message =id+"\n"+msg+"\n"+nopath;
				logger.error(loggerName, "Error in plan response: "+message+", response:"+arrResponse[0]);
				mailService.sendMailForOtpCheckFail(message);
				logger.debug(loggerName, "Mail Sent...");
				logger.warn(loggerName, "OTP response Fail:"+message);
			} else if ((Long)arrResponse[1] > (Integer.parseInt(TpConstants.MAX_TIME_UPPER_LIMIT))) {
				logger.info(loggerName, "response time found more then threasold, retrying....");
				Object[] time = getOtpServerData(wsPlanUrl);
				if ((Long)time[1]> (Integer.parseInt(TpConstants.MAX_TIME_UPPER_LIMIT))) {
					logger.error(loggerName, "OTP response time exeeed threshold value: "+time[1]);
					mailService.sendMailForOtpCheckFail("OTP response time exeeed threshold value: "+time[1]);
					logger.debug(loggerName, "Mail Sent...");
					logger.warn(loggerName, "OTP response time exeeed threshold value: "+time[1]);
				}else{
					logger.info(loggerName, "response time OK");
				}
			}
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
	}


	/**
	 * Gets the otp server data.
	 *
	 * @param wsPlanUrl the ws plan url
	 * @return the otp server data
	 */
	private Object[] getOtpServerData(String wsPlanUrl) {
		Object[] list = new Object[3];
		StringBuilder sb = new StringBuilder();
		long startTime = System.currentTimeMillis();
		HttpURLConnection connection = null; 
		try {
			connection = (HttpURLConnection)new URL(TpConstants.SERVER_URL+wsPlanUrl).openConnection();
			connection.setDoOutput(true);
			connection.addRequestProperty("Accept", "Application/json");
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while((line = br.readLine()) !=null){
				sb.append(line);
			}
			long endTime  = System.currentTimeMillis();
			long responseTime = (endTime-startTime)/1000;
			list[0] = sb.toString();
			list[1] = responseTime;
		}catch (Exception e) {
			list[2] = e.getMessage();
			logger.error(loggerName, e.getMessage());
		} 
		finally{
			if(connection !=null){
				connection.disconnect();
			}
		}
		return list;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public String getLoggerName() {
		return loggerName;
	}
}
