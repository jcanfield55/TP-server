package com.apprika.otp.util;

import java.io.File;

import org.apache.log4j.Logger;

import com.apprika.otp.service.LoggingService;
import com.apprika.otp.servlet.MainServlet;

/**
 * Constants used in application.
 * @author nirmal
 */
public class TpConstants {

	public static final String FILE_OTP_PROPERTY         = "conf/tp.properties";	
	public static final String FILE_SPRING_CONFIGURATION = "conf/spring/ApplicationContext.xml";	
	public static final String FILE_LOG_CONFIGURATION    = "conf/logging/log4j.xml";

	public static final String OTP_HTML_FILE_PATH = "conf/OTP.html";
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

	public static final String PLAN_FROM_PATH = TpProperty.getDefaultProperty("plan.from.path");
	public static final String PLAN_TO_PATH = TpProperty.getDefaultProperty("plan.to.path");
	public static final String PLAN_START_TIME_PATH = TpProperty.getDefaultProperty("plan.stattime.path");
	public static final String PLAN_END_TIME_PATH = TpProperty.getDefaultProperty("plan.entime.path");

	public static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

	public static final Logger logger = LoggingService.getLoggingService(MainServlet.class.getName());

	public static final String PUBLIC_RESPONSE_URL =  SERVER_DEFAULT+"/survey.jsp?sessid=%s";
	public static final String PUBLIC_SHARED_RESPONSE_URL =  SERVER_DEFAULT+"/shareSurvey.jsp";

	public static final String FEEDBACK_EMAIL_ID = TpProperty.getDefaultProperty("feedback.email.id");
	public static final String FEEDBACK_EMAIL_SUBJECT = TpProperty.getDefaultProperty("feedback.email.subject");
	public static String  OTP_HTML_STRING;

	public enum MONGO_TABLES{
		users,
		feedback
	}
}