package com.nimbler.tp.service.livefeeds.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.wmata.RailPrediction;
import com.nimbler.tp.dataobject.wmata.WmataBusPrediction;
import com.nimbler.tp.service.livefeeds.stub.WmataApiClient;

/**
 * The Class WmataCachedApiClient.
 * @author nirmal
 */
public class WmataCachedApiClient {
	private String apiKey= "wateq3gxqzb9s597qky6khd7";
	@Autowired
	WmataApiClient apiClient;

	private int cacheExpirationTimeInSec = 30;
	LoadingCache<String, List<WmataBusPrediction>> busStopPrediction = null;
	ArrayListMultimap<String, RailPrediction> railStopPrediction = ArrayListMultimap.create();
	long lastRailPredictionFetchTime = 0;

	@PostConstruct
	private void init() {
		busStopPrediction = CacheBuilder.newBuilder().expireAfterWrite(cacheExpirationTimeInSec, TimeUnit.SECONDS).concurrencyLevel(10)
				.build(new CacheLoader<String, List<WmataBusPrediction>>() {
					public List<WmataBusPrediction> load(String stopId) throws Exception{
						return apiClient.getBusPredictionAtStop(apiKey, stopId);
					}
				});
	}
	public List<WmataBusPrediction> getBusPredictionAtStop(String stopId) throws RealTimeDataException, ExecutionException {
		return busStopPrediction.get(stopId);
	}

	/**
	 * Gets the rail prediction.
	 *
	 * @param apiFromStop the api from stop
	 * @return the rail prediction
	 * @throws ExecutionException the execution exception
	 * @throws RealTimeDataException the real time data exception
	 */
	public List<RailPrediction> getRailPrediction(String apiFromStop) throws  RealTimeDataException {
		ensureRailCache();
		List<RailPrediction> res =  railStopPrediction.get(apiFromStop);
		if(res==null || res.isEmpty())
			throw new RealTimeDataException("No Prediction Found for api stop: "+apiFromStop);
		return new ArrayList<RailPrediction>(res);
	}

	/**
	 * Ensure rail cache.
	 *
	 * @throws RealTimeDataException the real time data exception
	 */
	private synchronized void ensureRailCache() throws RealTimeDataException {
		if((System.currentTimeMillis()-lastRailPredictionFetchTime)>(cacheExpirationTimeInSec*1000)){
			railStopPrediction.clear();
			List<RailPrediction> predictions = apiClient.getAllRailPrediction(apiKey);
			for (RailPrediction predic : predictions) {
				railStopPrediction.put(predic.getLocationCode(), predic);
			}
			lastRailPredictionFetchTime = System.currentTimeMillis();
		}
	}
	//	 getter setter ========================================
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public WmataApiClient getApiClient() {
		return apiClient;
	}
	public void setApiClient(WmataApiClient apiClient) {
		this.apiClient = apiClient;
	}
	public int getCacheExpirationTimeInSec() {
		return cacheExpirationTimeInSec;
	}
	public void setCacheExpirationTimeInSec(int cacheExpirationTimeInSec) {
		this.cacheExpirationTimeInSec = cacheExpirationTimeInSec;
	}
	public static void main(String[] args) {
		try {
			WmataCachedApiClient apiClient = new WmataCachedApiClient();
			apiClient.init();
			Thread.sleep(6000);
			Thread.sleep(56000);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
