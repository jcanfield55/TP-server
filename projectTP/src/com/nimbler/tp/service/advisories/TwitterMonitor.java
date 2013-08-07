/*
 * @author nirmal
 */
package com.nimbler.tp.service.advisories;

import static org.apache.commons.lang3.StringUtils.join;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.smtp.MailService;
import com.nimbler.tp.util.TpConstants;
/**
 * 
 * @author nirmal
 * 
 * Monitors tweet fetch from twitter, sends email if error exceeds limit.
 *
 */
public class TwitterMonitor {
	@Autowired 
	private LoggingService logger;

	private String loggerName = "com.nimbler.tp.service.advisories.AdvisoriesService";

	private boolean mailSent = false;

	private int openErrorCount = 0;
	/**
	 * After this count error will be sent in email
	 */
	private int errorThreshold = 4;

	private boolean enableNotification = true;

	@Autowired
	private MailService	mailService;

	private long lastMailSentTime = 0;

	private long remindPeriodInMillSec = DateUtils.MILLIS_PER_HOUR;

	/**
	 * Fetch sucess.
	 *
	 * @param tweetSources the list
	 */
	public void fetchSucess(String[] tweetSources) {
		try {
			if(!enableNotification)
				return;
			if(mailSent){
				mailService.sendMail(TpConstants.OTP_FAIL_NOTIFY_EMAIL_ID, "Twitter Error Recovered..!!!!",
						"Recovery Time: "+new Date()+", source: "+join(tweetSources),false);
			}
			reset();
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
	}

	private void reset() {
		mailSent = false;
		openErrorCount = 0;
	}

	/**
	 * Fetch failed.
	 *
	 * @param tweetSources the list
	 * @param e 
	 */
	public void fetchFailed(String[] tweetSources, Exception exception) {
		try {
			String sources = join(tweetSources);
			logger.debug(loggerName, "Source: "+sources+", open error count: "+openErrorCount);
			if(!enableNotification)
				return;
			openErrorCount++;

			// send mail if not sent OR send remainder
			if((!mailSent && openErrorCount>=errorThreshold) || 
					(mailSent && (System.currentTimeMillis()-lastMailSentTime)>remindPeriodInMillSec)){ 
				String error = exception.getClass().getSimpleName()+": "+exception.getMessage()+" for source: "+sources;
				logger.debug(loggerName, "Sending mail : "+error);
				mailService.sendMail(TpConstants.OTP_FAIL_NOTIFY_EMAIL_ID, "Error Fetching Tweets..!!!!", error,false);
				mailSent = true;
				lastMailSentTime = System.currentTimeMillis();
			}
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
	}

	//============================================ Getter/ Setter ===========================================

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

	public boolean isMailSent() {
		return mailSent;
	}

	public void setMailSent(boolean mailSent) {
		this.mailSent = mailSent;
	}

	public int getOpenErrorCount() {
		return openErrorCount;
	}

	public void setOpenErrorCount(int openErrorCount) {
		this.openErrorCount = openErrorCount;
	}

	public int getErrorThreshold() {
		return errorThreshold;
	}

	public void setErrorThreshold(int errorThreshold) {
		this.errorThreshold = errorThreshold;
	}

	public boolean isEnableNotification() {
		return enableNotification;
	}

	public void setEnableNotification(boolean enableNotification) {
		this.enableNotification = enableNotification;
	}

	public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public long getLastMailSentTime() {
		return lastMailSentTime;
	}

	public void setLastMailSentTime(long lastMailSentTime) {
		this.lastMailSentTime = lastMailSentTime;
	}

	public long getRemindPeriodInMillSec() {
		return remindPeriodInMillSec;
	}

	public void setRemindPeriodInMillSec(long remindPeriodInMillSec) {
		this.remindPeriodInMillSec = remindPeriodInMillSec;
	}

}
