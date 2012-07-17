/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.jobs;

import java.util.Timer;
import java.util.TimerTask;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.TPApplicationContext.SPRING_BEANS;
import com.nimbler.tp.service.twitter.CaltrainAdvisoriesService;
/**
 * 
 * @author suresh
 *
 */
public class CaltrainAdvisoriesTask {

	public void schedule(String timeInterval) {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				CaltrainAdvisoriesService service= (CaltrainAdvisoriesService) TPApplicationContext.getInstance().getBean(SPRING_BEANS.CALTRAIN_ADVISORIES_SERVICE.bean());
				service.getLatestTweets();
			}
		};
		Timer timer = new Timer();
		timer.schedule(timerTask, 1000,Integer.parseInt(timeInterval)*60*1000); // start after one min
	}
}