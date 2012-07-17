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
import com.nimbler.tp.dataobject.bart.BartResponse;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.livefeeds.BARTApiImpl;
import com.nimbler.tp.service.livefeeds.stub.BARTApiClient;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.TpProperty;
/**
 * A simple key-value cache that caches data used in processing real time data
 * from BART API.
 * It uses Google Guava open source library for holding the elements.<br>
 * @see <a href="http://code.google.com/p/guava-libraries/"> Google Guava</a>
 * @author nIKUNJ
 *
 */
public class BartETDCache {

	private static BartETDCache etdCache = new BartETDCache();
	/**
	 * This cache holds ETD results in memory for configured time period.<br>
	 * [Key = station tag | value = ETD results]<br>
	 * So, it caches results for statiom ID as key
	 * and will return cached ETD responses if exists or will make request to BART API
	 * for live data and put in cache.
	 */
	private ConcurrentMap<String, BartResponse> cache;
	private int cacheExpirationTimeInSec = 60;
	private int cacheinitialSize = 100;
	private int concurrencyLevel = 6;
	private String loggerName;
	/**
	 * Registration key used in BART API requiests.
	 */
	private String bartAPIRegKey;
	private BARTApiClient client;

	/**
	 * 
	 * @return
	 */
	public static BartETDCache getInstance() {
		return etdCache;
	}
	@SuppressWarnings("deprecation")
	private BartETDCache() {
		initCache();
		BARTApiImpl impl = BeanUtil.getBARTApiImpl();
		this.bartAPIRegKey = impl.getBartAPIRegKey();
		client = BeanUtil.getBARTAPIClient();

		cache = new MapMaker()
		.concurrencyLevel(concurrencyLevel)/*.softKeys().weakValues()*/.expiration(cacheExpirationTimeInSec, TimeUnit.SECONDS).initialCapacity(cacheinitialSize)
		.makeComputingMap(new Function<String, BartResponse>() {
			public BartResponse apply(String key) {
				return getBartETD(key, bartAPIRegKey);
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
			cacheExpirationTimeInSec = NumberUtils.toInt((String)prop.get("bart_cache_expiration_time_in_sec"), 60);
			cacheinitialSize = NumberUtils.toInt((String)prop.get("bart_cache_initial_size"), 100);
			concurrencyLevel = NumberUtils.toInt((String)prop.get("bart_cache_concurrency_level"), 6);
			loggerName = (String) prop.get("cache_loggername");
		} catch (FileNotFoundException e) {
			System.err.println("Cache configuration file not found. Default will be used: "+e.getMessage());
		} catch (IOException e) {
			System.err.println("Cache configuration file not found. Default will be used: "+e.getMessage());
		}
	}
	/**
	 * 
	 * @param originSt
	 * @return
	 */
	public BartResponse getEstimateTimeOfDepart(String originSt) {
		return cache.get(originSt);
	}
	/**
	 * 
	 * @param originSt
	 * @param regKey
	 * @return
	 */
	private BartResponse getBartETD(String originSt, String regKey) {
		if (originSt == null)
			return null;
		try {
			return client.getEstimationTime(originSt, regKey);
		} catch (RealTimeDataException e) {
			LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
			logger.error(loggerName, e); 
		}
		return null;
	}
}