/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dbobject.NimblerParams.NIMBLER_PARAMS;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.smtp.MailService;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpProperty;

/**
 * The Class DBHealthMonitoringService.
 *
 * @author nirmal
 */
public class DBHealthMonitoringService {

	@Autowired
	private LoggingService loggingService;

	@Autowired
	private PersistenceService persistenceService;

	@Autowired
	private MailService mailService;

	private String loggerName = DBHealthMonitoringService.class.getName();
	private boolean enableCheck = true;

	/**
	 * Checks mongodb health - excecuted by cron.
	 */
	public void checkHealth() {
		if(!enableCheck)
			return;
		try {
			persistenceService.upsertNimblerParam(NIMBLER_PARAMS.LAST_HEALTH_CHECK_TIME.name(), System.currentTimeMillis()+"");			
		} catch (DBException e) {
			loggingService.error(loggerName, e.getMessage());
			sendErrorMail(e);
		} catch (Exception e) {
			loggingService.error(loggerName, e);
			sendErrorMail(e);
		}
	}

	/**
	 * Send error mail.
	 *
	 * @param e the e
	 */
	private void sendErrorMail(Exception e) {
		try {
			String msg = "Error while Monitoring Database: "+e.getMessage();
			String subject = StringUtils.defaultIfBlank(TpProperty.getDefaultProperty("db_fail_msg.title"),"Nimbler Database Fail Alert !!!");
			String to = StringUtils.defaultIfBlank(TpProperty.getDefaultProperty("db_fail_msg.recepients"),TpConstants.OTP_FAIL_NOTIFY_EMAIL_ID);
			mailService.sendMail(to, subject, msg, false);
		} catch (Exception e1) {
			loggingService.error(loggerName, e1);
		}
	}

	public boolean isEnableCheck() {
		return enableCheck;
	}

	public void setEnableCheck(boolean enableCheck) {
		this.enableCheck = enableCheck;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
}
