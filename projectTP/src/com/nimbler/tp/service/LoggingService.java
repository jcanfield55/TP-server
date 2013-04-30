/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.service;

import static java.lang.String.format;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.nimbler.tp.util.TpConstants;
/**
 * 
 * @author nirmal
 *
 */
public class LoggingService {	 

	private Map<String, Logger> loggers;

	public void init() {
		DOMConfigurator.configure(LoggingService.class.getClassLoader().getResource(TpConstants.FILE_LOG_CONFIGURATION));
		loggers = new HashMap<String, Logger>();

		for (Enumeration allLoggers = LogManager.getCurrentLoggers();allLoggers.hasMoreElements();) {
			Logger logger = (Logger) allLoggers.nextElement();
			if (logger != null) {
				loggers.put(logger.getName(), logger);
			}
		}
	}
	/**
	 * 
	 * @param loggerName
	 * @param message
	 */
	public void info(String loggerName, String message) {
		Logger logger = loggers.get(loggerName);
		logger.info(getClassName()+message);
	}
	/**
	 * 
	 * @param loggerName
	 * @param message
	 */
	public void warn(String loggerName, String message) {
		Logger logger = loggers.get(loggerName);
		logger.warn(getClassName()+message);
	}
	/**
	 * 
	 * @param loggerName
	 * @param message
	 */
	public void debug(String loggerName, String message) {
		Logger logger = loggers.get(loggerName);
		logger.debug(getClassName()+message);
	}

	public void debug(String loggerName, String message,String...values) {
		message = format(message,values);
		debug(loggerName, message);
	}
	/**
	 * 
	 * @param loggerName
	 * @param message
	 */
	public void error(String loggerName, String message) {
		Logger logger = loggers.get(loggerName);
		logger.error(getClassName()+message);
	}
	/**
	 * 
	 * @param loggerName
	 * @param t
	 */
	public void error(String loggerName, String msg, Throwable t) {
		Logger logger = loggers.get(loggerName);
		logger.error(msg, t);
	}

	/**
	 * Error.
	 *
	 * @param loggerName the logger name
	 * @param t the t
	 */
	public void error(String loggerName,  Throwable t) {
		Logger logger = loggers.get(loggerName);
		if(logger!=null)
			logger.error("", t);
	}
	/**
	 * 
	 * @param loggerName
	 * @param message
	 */
	public void fatal(String loggerName, String message) {
		Logger logger = loggers.get(loggerName);
		logger.fatal(getClassName()+message);
	}
	public void fatal(String loggerName, String msg,Throwable t) {
		Logger logger = loggers.get(loggerName);
		logger.fatal(msg, t);
	}
	/**
	 * Gets the class name.
	 *
	 * @return the class name
	 */
	String getClassName(){
		try {			
			StackTraceElement[] arrStack = Thread.currentThread().getStackTrace();
			if(arrStack.length<4)
				return "";
			StackTraceElement stack = arrStack[3];
			String[] className = stack.getClassName().split("\\.");

			return "["+className[className.length-1]+"] ["+stack.getMethodName()+"] ";
		} catch (Exception e) {}
		return "";
	}
}