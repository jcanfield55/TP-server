/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.util;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Constants used in application.
 * @author nirmal
 */
public class TpConstants {

	public static final String FILE_OTP_PROPERTY          = "conf/tp.properties";	
	public static final String FILE_SPRING_CONFIGURATION  = "conf/spring/ApplicationContext.xml";	
	public static final String FILE_LOG_CONFIGURATION     = "conf/logging/log4j.xml";
	public static final String FILE_USER_CREDENTIAL       = "conf/user.xml";

	public static final String OTP_HTML_FILE_PATH 		  = "conf/html/TpFeedbackTemplet.html";
	public static final String OTP_HTML_SUMMERY_FILE_PATH = "conf/html/GtfsSummery.html";
	public static final String GTFS_COMPARE_FILE_PATH     = "conf/html/GtfsMonitorResult.html";

	public static String  GRAPH_TEST_RESULT_HTML_FILE 	  = "conf/html/GraphTestResult.html";;
	public static String  GRAPH_TEST_SUMMERY_HTML_FILE    = "conf/html/GraphTestSummery.html";;

	public static final String STRING_SEPARATOR = ",";

	public static final String SERVER_DEFAULT     = TpProperty.getDefaultProperty("server.default");  
	public static final String REPO_RELATIVE_PATH = TpProperty.getDefaultProperty("file.repo.path");
	public static final File FILE_REPOSITORY      = new File(TpConstants.REPO_RELATIVE_PATH);
	public static final String TEMP_DIR_PATH =System.getProperty("java.io.tmpdir");


	public static final String DB_CONNECTION_URL      = "connection.url";
	public static final String DB_CONNECTION_DRIVER   = "connection.driver_class";
	public static final String DB_CONNECTION_USERNAME = "connection.username";
	public static final String DB_CONNECTION_PASSWORD = "connection.password";


	public static final String MAIL_PROP_SMTP_HOST     = "mail.smtp.host";
	public static final String MAIL_PROP_SMTP_AUTH     = "mail.smtp.auth";
	public static final String MAIL_PROP_AUTH_USER     = "mail.user";
	public static final String MAIL_PROP_AUTH_PASSWORD = "mail.password";

	public static final String SERVER_URL 				=TpProperty.getDefaultProperty("server.url");
	public static final String SERVER_WEB_URL 			=TpProperty.getDefaultProperty("server.web.url");
	public static final String WS_PLAN 					=TpProperty.getDefaultProperty("ws.plan");
	public static final String MONITOR_INTERVAL    		=TpProperty.getDefaultProperty("monitor.interval");
	public static final String MAX_TIME_UPPER_LIMIT    	=TpProperty.getDefaultProperty("maxtime.upper.limit");

	public static final String RESPONSE_PLAN            =TpProperty.getDefaultProperty("response.plan");
	public static final String RESPONSE_ERROR_ID    	=TpProperty.getDefaultProperty("response.error.id");
	public static final String RESPONSE_ERROR_MSG       =TpProperty.getDefaultProperty("response.error.msg");
	public static final String RESPONSE_ERROR_NOPATH    =TpProperty.getDefaultProperty("response.error.nopath");
	public static final String RESPONSE_PLAN_ITINERAIES =TpProperty.getDefaultProperty("response.plan.itineraies");
	public static final String[] OTP_PARAMETERS 		=TpProperty.getDefaultProperty("otp.parameters").trim().split(",");

	public static final String PLAN_FROM_PATH = TpProperty.getDefaultProperty("plan.from.path");
	public static final String PLAN_TO_PATH = TpProperty.getDefaultProperty("plan.to.path");
	public static final String PLAN_START_TIME_PATH = TpProperty.getDefaultProperty("plan.stattime.path");
	public static final String PLAN_END_TIME_PATH = TpProperty.getDefaultProperty("plan.entime.path");

	public static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public static final String GTFS_DATE_FORMAT = "yyyyMMdd";
	public static final String OTP_DATE_FORMAT = "dd/MM/yyyy hh:mm:ss a";
	SimpleDateFormat gtfsDateFormat = new SimpleDateFormat(TpConstants.GTFS_DATE_FORMAT);

	public static final String FEEDBACK_EMAIL_ID = TpProperty.getDefaultProperty("feedback.email.id");
	public static final String OTP_FAIL_NOTIFY_EMAIL_ID = TpProperty.getDefaultProperty("otp.fail.notify.emailid");
	public static final String OTP_FAIL_NOTIFY_EMAIL_SUBJECT = TpProperty.getDefaultProperty("otp.fail.notify.subject");
	public static final String FEEDBACK_EMAIL_SUBJECT = TpProperty.getDefaultProperty("feedback.email.subject");

	public static final String ZIP_AGENCY_FILE = "agency.txt";
	public static final String ZIP_CALENDAR_FILE = "calendar.txt";
	public static final String ZIP_CALENDAR_DATES_FILE = "calendar_dates.txt";
	public static final String ZIP_TRIPS_FILE = "trips.txt";
	public static final String ZIP_ROUTES_FILE = "routes.txt";
	public static String  OTP_HTML_STRING;

	public static final String DEVICE_ID 			= "deviceId";
	public static final String NUMBER_OF_ALERT 		= "numberOfAlert";
	public static final String MAX_WALK_DISTANCE 	= "maxWalkDistance";
	public static final String DEVICE_TOKEN 		= "deviceToken";
	public static final String LAST_ALERT_TIME 		= "lastAlertTime";
	public static final String LAST_PUSH_TIME 		= "lastPushTime";
	public static final String CREATE_TIME 		= "createTime";
	public static final String UPDATE_TIME 		= "updateTime";
	public static final String APP_TYPE 		= "appType";
	public static final String LAST_PUSH_TIME_BART  = "lastPushTimeBart";
	public static final String LAST_PUSH_TIME_ACT   = "lastPushTimeAct ";
	public static final String LAST_PUSH_TIME_SFMUNI = "lastPushTimeSfMuni";


	public static final String TWEET_TIME_DIFF 		= TpProperty.getDefaultProperty("tweet.time.diff");
	public static final String TWEET_CREATED 		= TpProperty.getDefaultProperty("tweet.created");
	public static final String TWEET_TEXT 			= TpProperty.getDefaultProperty("tweet.text");
	@Deprecated
	public static final String TWEET_MAX_COUNT 		= TpProperty.getDefaultProperty("tweet.max.count");
	public static final String TWEET_FROM_USER		= TpProperty.getDefaultProperty("tweet.from.user");
	public static final String APN_PASSWORD 		= TpProperty.getDefaultProperty("apn.password");
	public static final String TWEET_TO_USER_NAME 	= TpProperty.getDefaultProperty("tweet.to.user.name");

	public static String  GTFS_COMPARE_HTML_STRING;
	public static String  GTFS_COMPARE_HTML_SUMMERY_STRING;
	public static String  GRAPH_TEST_RESULT_HTML_STRING;
	public static String  GRAPH_TEST_SUMMERY_HTML_STRING;

	public static String NIMBLER_PARAMS_NAME ="name";
	public static String NIMBLER_PARAMS_VALUE="value";

	public enum MONGO_TABLES {
		users,
		feedback,
		plan,
		itinerary,
		leg,
		event_log,
		login,
		nimbler_params
	}

	public enum LIVE_FEED_MODES {
		BUS,
		TRAM,
		SUBWAY
	}

	public enum ETA_FLAG {
		DEFAULT,
		ON_TIME,
		DELAYED,
		EARLY,
		EARLIER,
		ITINERARY_TIME_SLIPPAGE
	}

	public static final String GTFS_DATA_COMPARE_MAIL_SUBJECT = TpProperty.getDefaultProperty("gtfs.compare.mail.subject");
	public static final String GTFS_DATA_COMPARE_MAIL_ID = TpProperty.getDefaultProperty("gtfs.monitor.notify.emailid");
	public static final String GRAPH_TEST_MAIL_SUBJECT = TpProperty.getDefaultProperty("graph.test.mail.subject");

	public static final int ROUTE_DIRECTION_OUTBOUND = 0;
	public static final int ROUTE_DIRECTION_INBOUND = 1;

	public enum NIMBLER_APP_TYPE {
		DEFAULT(""),//0
		/**
		 *Ordinal 1 
		 */
		CALTRAIN("Caltrain"),//1
		BART("BART"),//2
		SFMUNI("SF Muni"),//3
		/**
		 * Ordinal 4
		 */
		SF_BAY_AREA("SF");//4

		private String text;
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		private NIMBLER_APP_TYPE(String text) {
			this.text = text;
		}
	}

	public enum AGENCY_TYPE {
		DEFAULT("lastPushTime","","",""),
		CALTRAIN("lastPushTime","enableCaltrainAdv","Caltrain","lastReadTimeCaltrain"),//lastPushTimeCaltrain
		BART("lastPushTimeBart","enableBartAdv","BART","lastReadTimeBart"),
		SFMUNI("lastPushTimeSfMuni","enableSfMuniAdv","SF Muni","lastReadTimeSfMuni"),
		AC_TRANSIT("lastPushTimeAct","enableAcTransitAdv","AC Transit","lastReadTimeAct"),
		VTA("","","VTA",""),
		MENLO_MIDDAY("","","Menlo-Midday",""),
		SF_FERRIES("","","SF-ferries","");
		private String lastReadTimeColumnName;

		private String pushTimeColumnName;
		private String enableAdvisoryColumnName;
		private String text;

		public String getPushTimeColumnName() {
			return pushTimeColumnName;
		}
		private AGENCY_TYPE(String pushTimeColumnName,
				String enableAdvisoryColumnName,String txt,String readTime) {
			this.pushTimeColumnName = pushTimeColumnName;
			this.enableAdvisoryColumnName = enableAdvisoryColumnName;
			this.text = txt;
			this.lastReadTimeColumnName = readTime;
		}

		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public String getLastReadTimeColumnName() {
			return lastReadTimeColumnName;
		}
		public void setLastReadTimeColumnName(String lastReadTimeColumnName) {
			this.lastReadTimeColumnName = lastReadTimeColumnName;
		}
		public void setPushTimeColumnName(String pushTimeColumnName) {
			this.pushTimeColumnName = pushTimeColumnName;
		}
		public String getEnableAdvisoryColumnName() {
			return enableAdvisoryColumnName;
		}
		public void setEnableAdvisoryColumnName(String enableAdvisoryColumnName) {
			this.enableAdvisoryColumnName = enableAdvisoryColumnName;
		}
	}
	/**
	 * Not sequential
	 * @author nirmal
	 *
	 */
	public enum PUSH_MSG_CONSTANT{
		CALTRAIN_REGULAR_TWEET,
		CALTRAIN_REGULAR_TWEET_FOR_1_THRESHOLD,
		/**
		 * 1. count  
		 * 2. agency name
		 */
		SF_REGULAR_TWEET,
		SF_REGULAR_TWEET_FOR_1_THRESHOLD,
		/**
		 * 1. Agency name
		 * 2. Msg
		 */
		SF_URGENT_TWEET,
		/**
		 * 1. App name
		 * 2. Msg
		 */
		STANDARD_ADMIN_MSG,
		/**
		 * 1. agency name
		 * 2. Msg
		 */
		URGENT_TWEET,
	}
	public enum GTFS_FILE {
		UNDEFINED("invalid"),
		AGENCY         ("agency"),          
		STOPS          ("stops"),         
		ROUTES         ("routes"),         
		TRIPS          ("trips"),          
		STOP_TIMES     ("stop_times"),      
		CALENDAR       ("calendar"),    
		CALENDAR_DATES ("calendar_dates"),  
		FARE_ATTRIBUTES("fare_attributes"),
		FARE_RULES     ("fare_rules"),
		SHAPES         ("shapes"),
		FREQUENCIES    ("frequencies"),
		TRANSFERS      ("transfers"),
		FEED_INFO      ("feed_info");

		private String name;
		private String ext = ".txt";

		public String getName() {
			return name;
		}
		public String getFileName() {
			return name+ext;
		}

		public void setName(String name) {
			this.name = name;
		}

		private GTFS_FILE(String name) {
			this.name = name;
		}
	}
}