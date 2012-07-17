/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.jobs;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.time.DateUtils;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.service.OtpMonitoringService;
import com.nimbler.tp.util.TpConstants;
/**
 * 
 * @author suresh
 *
 */
public class OtpMonitoringTask {

	public void scheduling() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				OtpMonitoringService service= (OtpMonitoringService) TPApplicationContext.getInstance().getBean(TPApplicationContext.SPRING_BEANS.OTP_MONITORING_SERVICE.bean());
				service.checkOtpServerStatus();
			}
		};
		Timer timer = new Timer();
		timer.schedule(timerTask, DateUtils.MILLIS_PER_MINUTE,Integer.parseInt(TpConstants.MONITOR_INTERVAL)*1000); // start after one min		
	}
}
