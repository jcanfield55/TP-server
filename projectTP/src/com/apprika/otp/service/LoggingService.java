package com.apprika.otp.service;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.apprika.otp.util.TpConstants;
/**
 * 
 * @author nirmal
 *
 */
public class LoggingService {	 

	private static Map<String, Logger> loggers;

	static {
		DOMConfigurator.configure(LoggingService.class.getClassLoader().getResource(TpConstants.FILE_LOG_CONFIGURATION));
		loggers = new HashMap<String, Logger>();

		for(Enumeration allLoggers = LogManager.getCurrentLoggers();allLoggers.hasMoreElements();) {
			Logger logger = (Logger) allLoggers.nextElement();
			if(logger != null) {
				loggers.put(logger.getName(), logger);
			}
		}
	}
	/**
	 * 
	 * @return
	 */
	/*public static LoggingService getLoggingService() {
		return loggingService;
	}*/
	public static Logger getLoggingService(String loggerName) {
		return loggers.get(loggerName);
	}
	/**
	 * 
	 * @param loggerName
	 * @param message
	 */
	public void info(String loggerName, String message) {
		Logger logger = loggers.get(loggerName);
		logger.info(message);
	}
	/**
	 * 
	 * @param loggerName
	 * @param message
	 */
	public void warn(String loggerName, String message) {
		Logger logger = loggers.get(loggerName);
		logger.warn(message);
	}
	/**
	 * 
	 * @param loggerName
	 * @param message
	 */
	public void debug(String loggerName, String message) {
		Logger logger = loggers.get(loggerName);
		logger.debug(message);
	}
	/**
	 * 
	 * @param loggerName
	 * @param message
	 */
	public void error(String loggerName, String message) {
		Logger logger = loggers.get(loggerName);
		logger.error(message);
	}
}