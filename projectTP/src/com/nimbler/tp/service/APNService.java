/*
 * @author nirmal
 */
package com.nimbler.tp.service;


import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.dataobject.ApnBundle;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.PayloadBuilder;
import com.notnoop.exceptions.InvalidSSLConfig;
import com.notnoop.exceptions.NetworkIOException;
/**
 * 
 * @author nirmal
 *
 */
public class APNService implements ApnsDelegate{
	@Autowired
	private LoggingService logger;
	private String loggerName = "com.nimbler.tp.service.APNService";

	private int poolSize = 5;
	private  String sound = null;
	private boolean isQueued = false;	
	private List<ApnBundle> lstApnBundles;

	private Map<NIMBLER_APP_TYPE, ApnsService> serviceMap = null;

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
		serviceMap = new HashMap<TpConstants.NIMBLER_APP_TYPE, ApnsService>();
		for (ApnBundle bundle : lstApnBundles) {
			loadService(bundle);
		}
	}

	/**
	 * Load service.
	 *
	 * @param bundle the bundle
	 */
	private void loadService(ApnBundle bundle) {
		try {   
			InputStream is = APNService.class.getClassLoader().getResourceAsStream(bundle.getKEYSTORE_P12_FILE());
			ApnsServiceBuilder serviceBuilder = APNS.newService().withCert(is, bundle.getPassword());
			if(bundle.getCertType() == APN_CERT_TYPE.SAND_BOX.ordinal())
				serviceBuilder = serviceBuilder.withSandboxDestination();
			else
				serviceBuilder = serviceBuilder.withProductionDestination();   
			if(isQueued)
				serviceBuilder = serviceBuilder.asQueued();
			if(poolSize!=-1)
				serviceBuilder = serviceBuilder.asPool(poolSize);
			ApnsService service = serviceBuilder.withDelegate(this).build();			
			serviceMap.put(bundle.getAppType(), service);
		} catch (InvalidSSLConfig e) {
			e.printStackTrace();
			logger.error(loggerName, "Error Loading Bundle: "+bundle,e);
		}catch (Exception e) {
			e.printStackTrace();
			logger.error(loggerName, "Error Loading Bundle: "+bundle,e);
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
	public boolean push(String deviceToken,String msg,Integer badge,Boolean isUrgent,boolean useSound, NIMBLER_APP_TYPE appType){
		boolean success = false;
		String payload = null;  
		try {				 
			PayloadBuilder payloadBuilder = APNS.newPayload();
			if(sound!=null && useSound)
				payloadBuilder.sound(sound);
			if(msg!=null)
				payloadBuilder.alertBody(msg);
			if(badge!=null)
				payloadBuilder.badge(badge);
			if(isUrgent!=null)
				payloadBuilder.customField("isUrgent",isUrgent+"");
			payload = payloadBuilder.build();
			logger.debug(loggerName,"Sending push notification..."+payload+", appType:"+appType);
			getApnServiceByType(appType).push(deviceToken, payload);   
			success = true;
			logger.debug(loggerName,"notification  sent to "+deviceToken+", appType:"+appType);
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
	 * Gets the service.
	 *
	 * @param appType the app type
	 * @return the service
	 */
	public ApnsService getApnServiceByType(NIMBLER_APP_TYPE appType) {
		ApnsService apnsService =  serviceMap.get(appType);
		if(apnsService == null)
			throw new IllegalArgumentException("Invalid application type: "+appType+", No matching service found");
		return apnsService;
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
	public boolean push(List<String> deviceTokens,String msg,Integer badge,Boolean isUrgent,boolean useSound, NIMBLER_APP_TYPE appType){
		boolean success = false;
		try {
			PayloadBuilder payloadBuilder = APNS.newPayload();
			if(sound!=null && useSound)
				payloadBuilder.sound(sound);
			if(msg!=null)
				payloadBuilder.alertBody(msg);
			if(badge!=null)
				payloadBuilder.badge(badge);
			if(isUrgent!=null)
				payloadBuilder.customField("isUrgent",isUrgent+"");
			String payload = payloadBuilder.build();
			logger.debug(loggerName,"Sending push notification..."+payload+", appType:"+appType);
			getApnServiceByType(appType).push(deviceTokens, payload);
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
	private boolean pushWithCustomFields(String deviceToken,String msg,Integer badge,Map customFields,boolean useSound){
		boolean success = false;
		String payload = null;
		try {
			PayloadBuilder payloadBuilder = APNS.newPayload();
			if(sound!=null && useSound)
				payloadBuilder.sound(sound);
			if(msg!=null)
				payloadBuilder.alertBody(msg);
			if(badge!=null)
				payloadBuilder.badge(badge);
			if(customFields!=null)
				payloadBuilder.customFields(customFields);
			payload = payloadBuilder.build();
			logger.debug(loggerName,"Sending push notification..."+payload);
			//			getService(appType).push(deviceToken, payload);
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
			Set<ApnsService> lstApnsServices = new HashSet<ApnsService>(serviceMap.values());
			for (ApnsService apnsService : lstApnsServices) {
				if(apnsService != null)
					apnsService.stop();
			}
		} catch (Exception e) {
			logger.error(loggerName,"Error While closing APN service:",e);   
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
	public int getPoolSize() {
		return poolSize;
	}
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
	public List<ApnBundle> getLstApnBundles() {
		return lstApnBundles;
	}

	public void setLstApnBundles(List<ApnBundle> lstApnBundles) {
		this.lstApnBundles = lstApnBundles;
	}

	public boolean isQueued() {
		return isQueued;
	}

	public void setQueued(boolean isQueued) {
		this.isQueued = isQueued;
	}

	public void setSound(String str) {
		if(!ComUtils.isEmptyString(str))
			this.sound = str;
	}

	public String getSound() {
		return sound;
	}

	@Override
	public void connectionClosed(DeliveryError deliveryerror, int i) {
		//		System.out.println("DeliveryError: "+ deliveryerror+", int: "+ i);		
		logger.error(loggerName, "DeliveryError: "+ deliveryerror+", int: "+ i);		
	}

	@Override
	public void messageSendFailed(ApnsNotification apnsnotification,Throwable throwable) {
		//		System.out.println("ApnsNotification: "+ apnsnotification+", Throwable: "+ throwable);
		logger.error(loggerName,"ApnsNotification: "+ apnsnotification+", Throwable: "+ throwable+", "+new String(apnsnotification.getDeviceToken()));
	}

	@Override
	public void messageSent(ApnsNotification apnsnotification) {
		//		System.out.println("APNService.messageSent()"+ new String(apnsnotification.getDeviceToken()));
	}

}