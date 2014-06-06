/*
 * @author nirmal
 */
package com.nimbler.tp.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.service.smtp.MailService;


/**
 * The Class 
 * Not used
 * @author nirmal
 */
public class TpAlertService {

	@Autowired 
	private LoggingService logger;
	private String loggerName = "syslog";

	private Map<ALERT_TYPE, ALERT_STATE> alertStatus = new HashMap<TpAlertService.ALERT_TYPE, TpAlertService.ALERT_STATE>(); 

	@Autowired 
	MailService mailService;
	public enum ALERT_TYPE{
		DEFAULT_ERROR("Unknown"),
		DATABASE_ERROR("Database"),
		OTP_PLAN_ERROR("Opentripplanner Plan"),
		TWITTER_ERROR("Twitter");

		private String error;

		private ALERT_TYPE(String error) {
			this.error = error;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}
	}

	public enum ALERT_STATE{
		OPEN,
		CLOSED;
	}


	@PostConstruct
	private void init() {
		for (ALERT_TYPE type : ALERT_TYPE.values()) {
			alertStatus.put(type, ALERT_STATE.CLOSED);
		}
	}

	public void notifyRecovery(ALERT_TYPE type,String msg,boolean ovverride) {

	}
	public void issueAlert(ALERT_TYPE type,String msg,boolean ovverride) {
		ALERT_STATE curruntState = alertStatus.get(type);
		switch (curruntState) {
		case OPEN:

			break;
		case CLOSED:

			break;

		default:
			break;
		}
		if(curruntState.equals(ALERT_STATE.OPEN)){

		}

	}
	/*gettr setter*/
	public LoggingService getLogger() {
		return logger;
	}
	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}
	public String getLoggerName() {
		return loggerName;
	}
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	public MailService getMailService() {
		return mailService;
	}
	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}
	public static void main(String[] args) {
	}
}
