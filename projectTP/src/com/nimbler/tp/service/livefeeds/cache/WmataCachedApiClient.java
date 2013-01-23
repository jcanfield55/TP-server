package com.nimbler.tp.service.livefeeds.cache;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.wmata.Predictions;
import com.nimbler.tp.dataobject.wmata.RailPrediction;
import com.nimbler.tp.service.livefeeds.stub.WmataApiClient;

/*
 * 
 */
public class WmataCachedApiClient {
	private String apiKey= "wateq3gxqzb9s597qky6khd7";
	@Autowired
	WmataApiClient apiClient;

	private int cacheExpirationTimeInSec = 1;
	LoadingCache<String, List<Predictions>> busStopPrediction = null;
	LoadingCache<String, List<RailPrediction>> railStopPrediction = null;

	@PostConstruct
	private void init() {
		busStopPrediction = CacheBuilder.newBuilder().expireAfterWrite(cacheExpirationTimeInSec, TimeUnit.SECONDS)
				.build(new CacheLoader<String, List<Predictions>>() {
					public List<Predictions> load(String stopId) throws Exception{
						return apiClient.getBusPredictionAtStop(apiKey, stopId);
					}
				});
		railStopPrediction = CacheBuilder.newBuilder().expireAfterWrite(cacheExpirationTimeInSec, TimeUnit.SECONDS)
				.build(new CacheLoader<String, List<RailPrediction>>() {
					public List<RailPrediction> load(String stopId) throws Exception{
						return apiClient.getRailPrediction(apiKey, stopId);
					}
				});
	}
	public List<Predictions> getBusPredictionAtStop(String stopId) throws RealTimeDataException, ExecutionException {
		return busStopPrediction.get(stopId);
	}
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
	public List<RailPrediction> getRailPrediction(String apiFromStop) throws ExecutionException {
		return railStopPrediction.get(apiFromStop);
	}

}
