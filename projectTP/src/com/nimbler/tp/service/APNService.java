/*
 * @author nirmal
 */
package com.nimbler.tp.service;


import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.nimbler.tp.dataobject.ApnBundle;
import com.nimbler.tp.dbobject.User.USER_STATE;
import com.nimbler.tp.mongo.MongoQueryConstant;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.PayloadBuilder;
import com.notnoop.apns.ReconnectPolicy;
import com.notnoop.apns.internal.ReconnectPolicies.EveryHalfHour;
import com.notnoop.apns.internal.Utilities;
import com.notnoop.exceptions.ApnsDeliveryErrorException;
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
	private int cacheLenth = 1000;	
	private List<ApnBundle> lstApnBundles;
	@Autowired
	private PersistenceService persistenceService;
	private boolean runInactiveDeviceMonitorThread = true;
	private int inactiveDeviceMonitorIntervalMin= 30;
	private int MAX_PAYLOAD = 250;
	private String	BODY_POST_FIX	= "...";

	private ReconnectPolicy reconnectPolicy =  new EveryHalfHour();

	private Map<NIMBLER_APP_TYPE, ApnsService> serviceMap = null;
	private static final boolean _debug = false;

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
		if(runInactiveDeviceMonitorThread){
			new Thread(){
				public void run() {
					while (true) {
						try {
							updateInactiveTokens();
							ComUtils.sleep(inactiveDeviceMonitorIntervalMin*DateUtils.MILLIS_PER_MINUTE);
						} catch (Exception e) {
							logger.error(loggerName, e);
						}
					}
				};
			}.start();
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
			if(reconnectPolicy!=null)
				serviceBuilder = serviceBuilder.withReconnectPolicy(reconnectPolicy.copy());
			serviceBuilder = serviceBuilder.withCacheLength(cacheLenth);			
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
			payloadBuilder.resizeAlertBody(MAX_PAYLOAD,BODY_POST_FIX);			
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
			payloadBuilder.resizeAlertBody(MAX_PAYLOAD,BODY_POST_FIX);
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
			payloadBuilder.resizeAlertBody(MAX_PAYLOAD,BODY_POST_FIX);
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
	/**
	 * Fetch inactive device tokens from Apple APNS service and
	 * update them in database. These devices will not get push 
	 * notifications next time. 
	 */
	public void updateInactiveTokens() {
		try {
			int PAGE_SIZE = 200;
			logger.debug(loggerName, "Checking fo for inactive tokens...");
			if (serviceMap==null || serviceMap.size()==0) {
				logger.warn(loggerName, "No APN service found.");
				return;
			}
			for (Map.Entry<NIMBLER_APP_TYPE, ApnsService> entry : serviceMap.entrySet()) {
				NIMBLER_APP_TYPE appType = entry.getKey();
				ApnsService apnsService = entry.getValue();
				try {
					Map<String, Date> inactiveDevices = apnsService.getInactiveDevices();				
					printDevices(inactiveDevices,appType);				
					if (inactiveDevices!=null && inactiveDevices.size()>0) {					
						List<Object[]> tokensToUpdate  = getLowerCaseListArray(inactiveDevices.keySet(),PAGE_SIZE);
						for (Object[] tokens : tokensToUpdate) {
							BasicDBObject query = new BasicDBObject();					
							query.put(TpConstants.DEVICE_TOKEN, new BasicDBObject(MongoQueryConstant.IN,tokens));				
							query.put(TpConstants.APP_TYPE, appType.ordinal());
							Map<String, Object> map = new HashMap<String, Object>();
							map.put(TpConstants.NUMBER_OF_ALERT, TpConstants.INACTIVE_DEVICES_NO_OF_ALEARTS);
							int res = persistenceService.updateMulti(MONGO_TABLES.users.name(), query, map);
							logger.info(loggerName, "updated devices: "+res);
						}
					}
				} catch (Exception e) {
					logger.error(loggerName, "error while getting inactive token, appType:"+appType,e);					
				}
			}
		} catch (Exception e) {
			logger.error(loggerName, "Error while updating invalid device tokens: ",e); 
		}
	}

	/**
	 * Gets the lower case list array.
	 *
	 * @param keySet the key set
	 * @param pageSize the page size
	 * @return the lower case list array
	 */
	private static List<Object[]> getLowerCaseListArray(Collection<String> keySet,int pageSize) {
		List<Object[]> res = new ArrayList<Object[]>();
		int i = 1;
		int totalsize = keySet.size();
		List<String> elementObj = new ArrayList<String>();
		for (String token : keySet) {
			if(!ComUtils.isEmptyString(token)){
				elementObj.add(token.toLowerCase());
			}
			if(i%pageSize==0 || i==totalsize){
				res.add(elementObj.toArray());
				elementObj = new ArrayList<String>();
			}
			i++;
		}
		return res;
	}

	/**
	 * Prints the devices.
	 *
	 * @param inactiveDevices the inactive devices
	 * @param appType the app type
	 */
	private void printDevices(Map<String, Date> inactiveDevices, NIMBLER_APP_TYPE appType) {
		try {
			if(inactiveDevices==null || inactiveDevices.size()==0){
				logger.info(loggerName, "No inactive Devide found");			
				return;
			}
			logger.info(loggerName, "Inactive devices for app type: "+appType.name());
			if(_debug)
				System.out.println( "Inactive devices for app type: "+appType.name());
			for (Map.Entry<String, Date> entry : inactiveDevices.entrySet()) {
				String key = entry.getKey();
				Date value = entry.getValue();
				logger.debug(loggerName,"      "+ key+"="+value);
				if(_debug)
					System.out.println("      "+ key+"="+value);
			}
		} catch (Exception e) {
			logger.error(loggerName, e);
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


	public boolean isRunInactiveDeviceMonitorThread() {
		return runInactiveDeviceMonitorThread;
	}

	public void setRunInactiveDeviceMonitorThread(
			boolean runInactiveDeviceMonitorThread) {
		this.runInactiveDeviceMonitorThread = runInactiveDeviceMonitorThread;
	}

	public int getInactiveDeviceMonitorIntervalMin() {
		return inactiveDeviceMonitorIntervalMin;
	}

	public void setInactiveDeviceMonitorIntervalMin(
			int inactiveDeviceMonitorIntervalMin) {
		this.inactiveDeviceMonitorIntervalMin = inactiveDeviceMonitorIntervalMin;
	}


	public int getMAX_PAYLOAD() {
		return MAX_PAYLOAD;
	}

	public void setMAX_PAYLOAD(int mAX_PAYLOAD) {
		MAX_PAYLOAD = mAX_PAYLOAD;
	}

	public String getBODY_POST_FIX() {
		return BODY_POST_FIX;
	}

	public void setBODY_POST_FIX(String bODY_POST_FIX) {
		BODY_POST_FIX = bODY_POST_FIX;
	}

	public ReconnectPolicy getReconnectPolicy() {
		return reconnectPolicy;
	}

	public void setReconnectPolicy(ReconnectPolicy reconnectPolicy) {
		this.reconnectPolicy = reconnectPolicy;
	}

	public int getCacheLenth() {
		return cacheLenth;
	}

	public void setCacheLenth(int cacheLenth) {
		this.cacheLenth = cacheLenth;
	}

	@Override
	public void messageSent(ApnsNotification message, boolean resent) {

	}

	@Override
	public void cacheLengthExceeded(int newCacheLength) {
		if(_debug)
			System.out.println("APNService.cacheLengthExceeded() --> newCacheLength: "+ newCacheLength);
		logger.warn(loggerName, "newCacheLength: " + newCacheLength);
	}
	@Override
	public void connectionClosed(DeliveryError deliveryerror, int i) {
		if(_debug)
			System.out.println("APNService.connectionClosed() --> deliveryerror: "	+ deliveryerror + " i: " + i);
		logger.warn(loggerName, "DeliveryError: "+ deliveryerror+", int: "+ i);		
	}

	@Override
	public void messageSendFailed(ApnsNotification apnsnotification,Throwable throwable) {
		try {
			if(_debug)
				System.out.println("APNService.messageSendFailed() --> apnsnotification: "	+ apnsnotification + " throwable: " + throwable);
			logger.warn(loggerName, "apnsnotification: " + apnsnotification+ " throwable: " + throwable);
			if(throwable instanceof ApnsDeliveryErrorException){
				String token = trimToNull(Utilities.encodeHex(apnsnotification.getDeviceToken()));
				int code = ((ApnsDeliveryErrorException)throwable).getDeliveryError().code();
				if(code == DeliveryError.INVALID_TOKEN.code() && token!=null){
					token = token.toLowerCase();
					int count = persistenceService.updateSingleIntObject(MONGO_TABLES.users.name(),TpConstants.DEVICE_TOKEN,
							token, TpConstants.NUMBER_OF_ALERT,USER_STATE.INVALID_TOKEN_FOR_PUSH.code());
					logger.warn(loggerName, count+" - "+token + "(s) marked "+USER_STATE.INVALID_TOKEN_FOR_PUSH.code());
				}
			}	 
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
	}

	@Override
	public void notificationsResent(int resendCount) {
		if(_debug)
			System.out.println("APNService.notificationsResent() --> resendCount: "	+ resendCount);
		logger.debug(loggerName, "resendCount: " + resendCount);

	}



}