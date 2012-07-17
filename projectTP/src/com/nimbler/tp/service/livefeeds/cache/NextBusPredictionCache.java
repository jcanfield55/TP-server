package com.nimbler.tp.service.livefeeds.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.nextbus.NextBusResponse;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.livefeeds.stub.NextBusApiClient;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.TpProperty;
/**
 * Very simple key-value cache that holds various data used in processing NextBus real time data.<br>
 * It uses Google Guava open source library for holding the elements.
 * @see <a href="http://code.google.com/p/guava-libraries/"> Google Guava</a>
 * @author nIKUNJ
 *
 */
public class NextBusPredictionCache {

	private static NextBusPredictionCache predictionCache = new NextBusPredictionCache();
	/**
	 * This cache holds prediction results in memory for configured time period.<br>
	 * [Key = agencyTag--routeTag--stopTag | value = prediction result]<br>
	 * So, it caches results for agencyTag--routeTag--stopTag combination as key
	 * and will return cached prediction if exists or will make request to NextBus API
	 * for live data and put in cache. 
	 */
	private ConcurrentMap<String, NextBusResponse> cache = null;

	private static String keySeparator = "--";

	private int cacheExpirationTimeInSec = 60;
	private int cacheinitialSize = 100;
	private int concurrencyLevel = 6;
	private String loggerName;

	/**
	 * 
	 * @return
	 */
	public static NextBusPredictionCache getInstance() {
		return predictionCache;
	}
	@SuppressWarnings("deprecation")
	private NextBusPredictionCache() {
		initCache();
		cache = new MapMaker()
		.concurrencyLevel(concurrencyLevel)/*.softKeys().weakValues()*/.expiration(cacheExpirationTimeInSec, TimeUnit.SECONDS).initialCapacity(cacheinitialSize)
		.makeComputingMap(new Function<String, NextBusResponse>() {
			public NextBusResponse apply(String key) {
				return getPrediction(key);
			}
		});
	}
	/**
	 * 
	 */
	private void initCache() {
		Properties prop = new Properties();
		try {
			prop.load(TpProperty.class.getClassLoader().getResourceAsStream("conf/cache.properties"));
			cacheExpirationTimeInSec = NumberUtils.toInt((String)prop.get("nextbus_cache_expiration_time_in_sec"), 60);
			cacheinitialSize = NumberUtils.toInt((String)prop.get("nextbus_cache_initial_size"), 100);
			concurrencyLevel = NumberUtils.toInt((String)prop.get("nextbus_cache_concurrency_level"), 6);
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
	public NextBusResponse getPrediction(String agencyTag, String routeTag, String stopTag) {
		return cache.get(agencyTag+keySeparator+routeTag+keySeparator+stopTag);
	}
	/**
	 * 
	 * @param compositeKey
	 * @return
	 */
	private NextBusResponse getPrediction(String compositeKey) {
		if (compositeKey == null)
			return null;
		String[] params = compositeKey.split(keySeparator);
		String agencyTag = params[0];
		String routeTag = params[1];
		String stopTag = params[2];
		NextBusApiClient client = BeanUtil.getNextBusAPIClient();
		try {
			return client.getPredictions(agencyTag, stopTag, routeTag);
		} catch (RealTimeDataException e) {
			LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
			logger.error(loggerName, e); 
		}
		return null;
	}
}