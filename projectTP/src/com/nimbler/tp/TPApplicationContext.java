/**
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.nimbler.tp.util.TpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TPApplicationContext.
 *
 * @author nirmal
 */
public class TPApplicationContext {

	/**
	 * The Enum SPRING_BEANS.
	 */
	public enum SPRING_BEANS{
		MONGO_OPERATION_TEMPLATE("mongoOpetation"),
		PERSISTANCE_SERVICE("persistenceService"),
		EVENT_LOGGING_SERVICE("tpEventLoggingService"),
		FEEDBACK_SERVICE("tpFeedbackService"),
		PLAN_SERVICE("tpPlanService"),
		OTP_MONITORING_SERVICE("otpMonitoringService"),
		BART_API_CLIENT("bartApiClient"),
		NEXT_BUS_API_CLIENT("nextBusApiClient"),

		NEXT_BUS_API_Impl("nextBusApiImpl"),
		BART_API_IMPL("bartApiImpl"),

		CALTRAIN_ADVISORIES_SERVICE("caltrainAdvisoriesService"),
		USER_MANAGEMENT_SERVICE("userManagementService"),
		Advisories_REST_SERVICE("advisoriesService"),
		APN_SERVICE("apnService"),
		GTFS_DATA_MONITOR("gtfsMonitoring"),
		TWITTER_ADVISORIES_SERVICE("twitterAdvisoriesService"),
		ADVISORIES_PUSH_SERVICE("advisoriesPushService"),
		NIMBLER_APPS_BEAN("nimblerApps"),
		GTFS_DATA_SERVICE("gtfsDataService"),
		FLURRY_MANAGEMENT_SERVICE("flurryManagementService");
		String name;

		/**
		 * Instantiates a new sPRIN g_ beans.
		 *
		 * @param name the name
		 */
		private SPRING_BEANS(String name) {
			this.name = name;
		}

		/**
		 * Instantiates a new sPRIN g_ beans.
		 */
		private SPRING_BEANS() {
			this.name = name();
		}

		/**
		 * Bean.
		 *
		 * @return the string
		 */
		public String bean() {
			return name;
		}
	}

	/** The ctxt. */
	private static TPApplicationContext ctxt = new TPApplicationContext();

	/** The main ctxt. */
	ClassPathXmlApplicationContext mainCtxt;


	/**
	 * Instantiates a new tP application context.
	 */
	private TPApplicationContext(){
		load();
	}

	/**
	 * Load.
	 */
	private void load() {
		mainCtxt = new ClassPathXmlApplicationContext(getBeanList());
		System.out.println("Context Initialized...");
	}

	/**
	 * Gets the bean list.
	 *
	 * @return the bean list
	 */
	private String[] getBeanList() {
		//		return new String[]{OtpConstants.FILE_SPRING_CONFIGURATION,"conf/spring/mongo-config.xml"};
		return new String[]{TpConstants.FILE_SPRING_CONFIGURATION};
	}

	/**
	 * Gets the single instance of OTPApplicationContext.
	 *
	 * @return single instance of OTPApplicationContext
	 */
	public static TPApplicationContext getInstance(){
		return ctxt;
	}

	/**
	 * Gets the bean instance.
	 *
	 * @return the bean instance
	 */
	public static TPApplicationContext getBeanInstance(){
		return ctxt;
	}

	/**
	 * Gets the bean.
	 *
	 * @param beanName the bean name
	 * @return the bean
	 */
	public Object getBean(String beanName) {
		return mainCtxt.getBean(beanName);
	}

	/**
	 * Gets the bean by name.
	 *
	 * @param beanName the bean name
	 * @return the bean by name
	 */
	public static Object getBeanByName(String beanName) {
		return getInstance().getBean(beanName);
	}
}
