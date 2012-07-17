/*
 * @author nirmal
 */
package com.nimbler.tp.service;


import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.PayloadBuilder;
import com.notnoop.exceptions.InvalidSSLConfig;
import com.notnoop.exceptions.NetworkIOException;
/**
 * 
 * @author nirmal
 *
 */
public class APNService {
	@Autowired
	private LoggingService logger;
	private String loggerName;

	private String password;
	public String KEYSTORE_P12_FILE;
	private ApnsService service;
	private int poolSize;
	private int certType;
	private boolean isQueued = false;
	private boolean isNonBlocking = false;

	public enum APN_CERT_TYPE{
		UNDEFINED,
		SAND_BOX,
		PRODUCTION
	}


	public APNService() {
	}

	/**
	 * On load.
	 */
	public void init(){
		try {			
			InputStream is = APNService.class.getClassLoader().getResourceAsStream(KEYSTORE_P12_FILE);
			ApnsServiceBuilder serviceBuilder =	APNS.newService().withCert(is, password);
			if(certType == APN_CERT_TYPE.SAND_BOX.ordinal())
				serviceBuilder = serviceBuilder.withSandboxDestination();
			else
				serviceBuilder = serviceBuilder.withProductionDestination();			
			if(isQueued)
				serviceBuilder = serviceBuilder.asQueued();
			if(poolSize!=-1)
				serviceBuilder = serviceBuilder.asPool(poolSize);
			if(isNonBlocking)
				serviceBuilder = serviceBuilder.asNonBlocking();
			service = serviceBuilder.build();			
		} catch (InvalidSSLConfig e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send alert notification.
	 *
	 * @param deviceToken the device token
	 * @param msg the alert message - optional
	 * @param badge the badge - optional
	 * @param isUrgent the is urgent - optional
	 * @return true, if successful
	 */
	public boolean push(String deviceToken,String msg,Integer badge,Boolean isUrgent){
		boolean success = false;
		String payload = null;		
		try {
			PayloadBuilder payloadBuilder = APNS.newPayload();
			if(msg!=null)
				payloadBuilder.alertBody(msg);
			if(badge!=null)
				payloadBuilder.badge(badge);
			if(isUrgent!=null)
				payloadBuilder.customField("isUrgent",isUrgent+"");
			payload = payloadBuilder.build();
			logger.debug(loggerName,"Sending push notification..."+payload);
			service.push(deviceToken, payload);			
			success = true;
			logger.debug(loggerName,"notification  sent to "+deviceToken);
		} catch (NetworkIOException e) {
			logger.error(loggerName, "NetworkIOException: "+e.getMessage()+",deviceToken:"+deviceToken+", payload:"+payload);			
		}catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage()+",deviceToken:"+deviceToken+", payload:"+payload);
		}catch (Exception e) {			
			logger.error(loggerName,"deviceToken:"+deviceToken+", payload:"+payload, e);
		}
		return success;
	}

	/**
	 * Push.
	 *
	 * @param list of deviceTokens
	 * @param msg the msg
	 * @param badge the badge
	 * @param isUrgent the is urgent
	 * @return true, if successful
	 */
	public boolean push(List<String> deviceTokens,String msg,Integer badge,Boolean isUrgent){
		boolean success = false;
		try {
			PayloadBuilder payloadBuilder = APNS.newPayload();
			if(msg!=null)
				payloadBuilder.alertBody(msg);
			if(badge!=null)
				payloadBuilder.badge(badge);
			if(isUrgent!=null)
				payloadBuilder.customField("isUrgent",isUrgent+"");
			String payload = payloadBuilder.build();
			logger.debug(loggerName,"Sending push notification..."+payload);
			service.push(deviceTokens, payload);
			success = true;
			logger.debug(loggerName,"notification  sent ");
		} catch (NetworkIOException e) {
			logger.error(loggerName, "NetworkIOException: "+e.getMessage());
		}catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
		}catch (Exception e) {
			logger.error(loggerName, e);
		}
		return success;
	}
	/**
	 * Push with custom fields.
	 *
	 * @param deviceToken the device token
	 * @param msg the alert message - optional
	 * @param badge the badge  - optional
	 * @param customFields the custom fields - optional
	 * @return true, if successful
	 */
	public boolean pushWithCustomFields(String deviceToken,String msg,Integer badge,Map customFields){
		boolean success = false;
		String payload = null;
		try {
			PayloadBuilder payloadBuilder = APNS.newPayload();
			if(msg!=null)
				payloadBuilder.alertBody(msg);
			if(badge!=null)
				payloadBuilder.badge(badge);
			if(customFields!=null)
				payloadBuilder.customFields(customFields);
			payload = payloadBuilder.build();
			logger.debug(loggerName,"Sending push notification..."+payload);
			service.push(deviceToken, payload);
			success = true;
			logger.debug(loggerName,"notification sent to "+deviceToken);
		} catch (NetworkIOException e) {
			logger.error(loggerName, "NetworkIOException: "+e.getMessage()+",deviceToken:"+deviceToken+", payload:"+payload);			
		}catch (Exception e) {
			logger.error(loggerName,"deviceToken:"+deviceToken+", payload:"+payload, e);
		}
		return success;
	}

	/**
	 * Close.
	 */
	public void close() {
		try {
			service.stop();
		} catch (Exception e) {
			logger.error(loggerName,"Error While closing APN service:"+e.getMessage());			
		}
	}

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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getKEYSTORE_P12_FILE() {
		return KEYSTORE_P12_FILE;
	}
	public void setKEYSTORE_P12_FILE(String kEYSTORE_P12_FILE) {
		KEYSTORE_P12_FILE = kEYSTORE_P12_FILE;
	}
	public int getPoolSize() {
		return poolSize;
	}
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
	public ApnsService getService() {
		return service;
	}
	public int getCertType() {
		return certType;
	}
	public void setCertType(int certType) {
		this.certType = certType;
	}
	public boolean isQueued() {
		return isQueued;
	}
	public void setIsQueued(boolean isQueued) {
		this.isQueued = isQueued;
	}
	public boolean isNonBlocking() {
		return isNonBlocking;
	}
	public void setIsNonBlocking(boolean isNonBlocking) {
		this.isNonBlocking = isNonBlocking;
	}
}
