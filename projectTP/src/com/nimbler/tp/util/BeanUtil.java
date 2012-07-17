/*
 * @author nirmal
 */
package com.nimbler.tp.util;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.TPApplicationContext.SPRING_BEANS;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.rest.AdvisoriesRestService;
import com.nimbler.tp.service.APNService;
import com.nimbler.tp.service.TpEventLoggingService;
import com.nimbler.tp.service.TpFeedbackService;
import com.nimbler.tp.service.TpPlanService;
import com.nimbler.tp.service.UserManagementService;
import com.nimbler.tp.service.livefeeds.BARTApiImpl;
import com.nimbler.tp.service.livefeeds.NextBusApiImpl;
import com.nimbler.tp.service.livefeeds.stub.BARTApiClient;
import com.nimbler.tp.service.livefeeds.stub.NextBusApiClient;
import com.nimbler.tp.service.twitter.CaltrainAdvisoriesService;

/**
 * The Class BeanUtil.
 *
 * @author nirmal
 */
public class BeanUtil {

	/**
	 * Gets the trip plan service.
	 *
	 * @return the trip plan service
	 */
	public static final TpPlanService getPlanService(){
		return (TpPlanService) TPApplicationContext.getBeanInstance().getBean(SPRING_BEANS.PLAN_SERVICE.bean());
	}

	/**
	 * Gets the tP logging service.
	 *
	 * @return the tP logging service
	 */
	public static final TpEventLoggingService getTpEventLoggingService(){
		return (TpEventLoggingService) TPApplicationContext.getBeanInstance().getBean(SPRING_BEANS.EVENT_LOGGING_SERVICE.bean());
	}

	/**
	 * Gets the tP feedback service.
	 *
	 * @return the tP feedback service
	 */
	public static final TpFeedbackService getTpFeedbackService(){
		return (TpFeedbackService) TPApplicationContext.getBeanInstance().getBean(SPRING_BEANS.FEEDBACK_SERVICE.bean());
	}
	/**
	 * 
	 * @return
	 */
	public static final PersistenceService getPersistanceService(){
		return (PersistenceService) TPApplicationContext.getBeanInstance().getBean(SPRING_BEANS.PERSISTANCE_SERVICE.bean());
	}
	/**
	 * 
	 * @return
	 */
	public static final NextBusApiClient getNextBusAPIClient(){
		return (NextBusApiClient) TPApplicationContext.getBeanInstance().getBean(SPRING_BEANS.NEXT_BUS_API_CLIENT.bean());
	}
	/**
	 * 
	 * @return
	 */
	public static final BARTApiClient getBARTAPIClient(){
		return (BARTApiClient) TPApplicationContext.getBeanInstance().getBean(SPRING_BEANS.BART_API_CLIENT.bean());
	}

	/**
	 * 
	 * @return
	 */
	public static final NextBusApiImpl getNextBusApiImpl(){
		return (NextBusApiImpl) TPApplicationContext.getBeanInstance().getBean(SPRING_BEANS.NEXT_BUS_API_Impl.bean());
	}
	/**
	 * 
	 * @return
	 */
	public static final BARTApiImpl getBARTApiImpl(){
		return (BARTApiImpl) TPApplicationContext.getBeanInstance().getBean(SPRING_BEANS.BART_API_IMPL.bean());
	}

	public static final UserManagementService getUserManagementService() {
		return (UserManagementService) TPApplicationContext.getBeanInstance().getBean(SPRING_BEANS.USER_MANAGEMENT_SERVICE.bean());
	}

	public static final AdvisoriesRestService getTweetCountService() {
		return (AdvisoriesRestService) TPApplicationContext.getBeanInstance().getBean(SPRING_BEANS.Advisories_REST_SERVICE.bean());
	}

	public static final CaltrainAdvisoriesService getCaltrainService() {
		return (CaltrainAdvisoriesService) TPApplicationContext.getBeanInstance().getBean(SPRING_BEANS.CALTRAIN_ADVISORIES_SERVICE.bean());
	}

	public static APNService getApnService() {
		return (APNService) TPApplicationContext.getBeanInstance().getBean(SPRING_BEANS.APN_SERVICE.bean());
	}
}
