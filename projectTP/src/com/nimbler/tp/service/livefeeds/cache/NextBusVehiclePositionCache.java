/*
 * @author nirmal
 */
package com.nimbler.tp.service.livefeeds.cache;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.nextbus.NbVehicle;
import com.nimbler.tp.dataobject.nextbus.NextBusResponse;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.livefeeds.stub.NextBusApiClient;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.TpProperty;
/**
 * Very simple key-value cache that holds various data used in processing NextBus real time data.<br>
 * It uses Google Guava open source library for holding the elements.
 * @see <a href="http://code.google.com/p/guava-libraries/"> Google Guava</a>
 * @author nirmal
 *
 */
public class NextBusVehiclePositionCache {

	private static NextBusVehiclePositionCache predictionCache = new NextBusVehiclePositionCache();

	Cache<String, NbVehicle> cache = null;

	LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private String loggerName = "com.nimbler.tp.service.livefeeds";


	private static String keySeparator = "--";

	private int cacheExpirationTimeInSec = 300;
	private int concurrencyLevel = 2;
	private int updateIntervalMsec = 20000;
	/**
	 * Time when api updated
	 */
	private Map<String, Long> lastApiUpdateTime = new HashMap<String, Long>();
	/**
	 * Time cache is updated
	 */
	private Map<String, Long> lastUpdateTime = new HashMap<String, Long>();

	boolean enableService = true;

	/**
	 * 
	 * @return
	 */
	public static NextBusVehiclePositionCache getInstance() {
		return predictionCache;
	}

	private NextBusVehiclePositionCache() {
		initCache();
		cache = CacheBuilder.newBuilder().concurrencyLevel(concurrencyLevel).
				expireAfterWrite(cacheExpirationTimeInSec, TimeUnit.SECONDS).build();
	}

	/**
	 * Inits the cache.
	 */
	private void initCache() {
		Properties prop = new Properties();
		try {
			prop.load(TpProperty.class.getClassLoader().getResourceAsStream("conf/cache.properties"));
			cacheExpirationTimeInSec = NumberUtils.toInt((String)prop.get("nextbus_vp_cache_expiration_time_in_sec"), cacheExpirationTimeInSec);
			concurrencyLevel = NumberUtils.toInt((String)prop.get("nextbus_vp_cache_concurrency_level"), concurrencyLevel);
			updateIntervalMsec = NumberUtils.toInt((String)prop.get("nextbus_vp_cache_update_inteval_ms"), updateIntervalMsec);
			String enable = (String) prop.get("nextbus_vp_enableevel");
			if(enable!=null)
				enableService = BooleanUtils.toBoolean(enable);				
			loggerName = (String) prop.get("nextbus_cache_loggername");
			keySeparator = (String) prop.get("nextbus_cache_compositekey_separator");
		} catch (FileNotFoundException e) {
			System.err.println("Cache configuration file not found. Default will be used: "+e.getMessage());
		} catch (IOException e) {
			System.err.println("Cache configuration file not found. Default will be used: "+e.getMessage());
		}
	}
	/**
	 * 
	 * @param agencyTag
	 * @param routeTag
	 * @param stopTag
	 * @return
	 */
	public NbVehicle getVehiclePosition(String agencyTag, String vehicleId) {
		if(enableService && shouldUpdateCache(agencyTag)){
			synchronized (this) {
				if(shouldUpdateCache(agencyTag)){// to avoid re-request for concurrent request 
					getVehiclePosition(agencyTag);
				}
			}			
		}
		return cache.getIfPresent(agencyTag+keySeparator+vehicleId);
	}


	/**
	 * Should update cache.
	 *
	 * @param agencyTag the agency tag
	 * @return true, if successful
	 */
	private boolean shouldUpdateCache(String agencyTag) {
		Long lastUpdate = lastUpdateTime.get(agencyTag);
		return ((lastUpdate==null) || (System.currentTimeMillis()-lastUpdate)>updateIntervalMsec);
	}

	/**
	 * Gets the vehicle position.
	 *
	 * @return the vehicle position
	 */	
	public void getVehiclePosition(String agencyTag) {
		NextBusApiClient client = BeanUtil.getNextBusAPIClient();
		try {			
			Long lastTime = defaultIfNull(lastApiUpdateTime.get(agencyTag),0L);
			NextBusResponse res = client.getVehiclePosition(agencyTag, null,lastTime+"");			
			//			System.out.println("VehiclePosition-->"+res);
			if(res==null){
				logger.warn(loggerName, "Null response for "+agencyTag);
				return;
			}
			List<NbVehicle> lstVehicles = res.getVehicles();
			if(res.getError()!=null){
				logger.error(loggerName, res.getError().toString());
			}
			if(!ComUtils.isEmptyList(lstVehicles)){			
				for (NbVehicle nbVehicle : lstVehicles) {
					cache.put(agencyTag+keySeparator+nbVehicle.getVehicleId(), nbVehicle);							
				}
			}
			if(res.getLastTime()!=null)
				lastApiUpdateTime.put(agencyTag, res.getLastTime().getTime());
			lastUpdateTime.put(agencyTag, System.currentTimeMillis());

		} catch (RealTimeDataException e) {
			logger.error(loggerName, e); 
		}
	}
}