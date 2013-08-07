/*
 * @author nirmal
 */
package com.nimbler.tp.service.livefeeds.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.trimet.arrivals.ResultSet;
import com.nimbler.tp.dataobject.trimet.vehicleposition.TrimetVehicle;
import com.nimbler.tp.dataobject.trimet.vehicleposition.TrimetVehiclePosResponse;
import com.nimbler.tp.service.livefeeds.stub.TrimetApiClient;

/**
 * The Wrapper for TrimetApiClient with caching capabilities.
 * @author nirmal
 */
public class TrimetCachedApiClient {

	@Autowired
	TrimetApiClient apiClient;

	private int cacheExpirationTimeInSec = 30;
	private int predictionExpirTimeInSec = 90;
	LoadingCache<String, ResultSet> stopPrediction = null;
	/**
	 * vehicle
	 */
	Map<String, TrimetVehicle> vehiclePosition = new ConcurrentHashMap<String, TrimetVehicle>();
	long lastVehiclePositionFetchTime = 0;
	long lastQueryTimeTime = 0;

	@PostConstruct
	private void init() {
		stopPrediction = CacheBuilder.newBuilder().expireAfterWrite(predictionExpirTimeInSec, TimeUnit.SECONDS).concurrencyLevel(10)
				.build(new CacheLoader<String, ResultSet>() {
					public ResultSet load(String stopId) throws Exception{						
						return apiClient.getArrivalForStops(stopId);
					}
					@Override
					public Map<String, ResultSet> loadAll(Iterable<? extends String> keys) throws Exception {
						ResultSet resultSet = apiClient.getArrivalForStops(Lists.newArrayList(keys));
						Map<String, ResultSet> map = new HashMap<String, ResultSet>();
						for (String stop : keys) {
							map.put(stop, resultSet); // same for all stop, filter it when required
						}
						return map;
					}
				});
	}
	public ResultSet getPredictionAtStop(String stopId) throws RealTimeDataException, ExecutionException {
		return stopPrediction.get(stopId);
	}
	public ImmutableMap<String, ResultSet> getPredictionAtStop(List<String> stopIds) throws RealTimeDataException, ExecutionException {
		return stopPrediction.getAll(stopIds);
	}

	/**
	 * Gets the rail prediction.
	 *
	 * @param apiFromStop the api from stop
	 * @return the rail prediction
	 * @throws ExecutionException the execution exception
	 * @throws RealTimeDataException the real time data exception
	 */
	public TrimetVehicle getVehiclePositionForTrip(String tripID) throws  RealTimeDataException {
		ensureVehiclePosCache();
		return  vehiclePosition.get(tripID);
	}

	/**
	 * Ensure vehicle pos cache.
	 *
	 * @throws RealTimeDataException the real time data exception
	 */
	private synchronized void ensureVehiclePosCache() throws RealTimeDataException {
		if((System.currentTimeMillis()-lastVehiclePositionFetchTime)>(cacheExpirationTimeInSec*1000)){
			Iterator<Entry<String, TrimetVehicle>> itr = vehiclePosition.entrySet().iterator();
			long curruntTime = System.currentTimeMillis();
			while (itr.hasNext()) {
				Map.Entry<String, TrimetVehicle> entry =  itr.next();
				if(entry.getValue().getExpires()<curruntTime)
					itr.remove();
			}
			TrimetVehiclePosResponse response = apiClient.getVehiclePosition(lastQueryTimeTime);
			for (TrimetVehicle vehicle : response.getResultSet().getVehicle()) {
				vehiclePosition.put(vehicle.getTripID()+"", vehicle);
			}
			lastQueryTimeTime = response.getResultSet().getQueryTime();
			lastVehiclePositionFetchTime = System.currentTimeMillis();
		}
	}
	//	 getter setter ========================================
	public int getCacheExpirationTimeInSec() {
		return cacheExpirationTimeInSec;
	}
	public void setCacheExpirationTimeInSec(int cacheExpirationTimeInSec) {
		this.cacheExpirationTimeInSec = cacheExpirationTimeInSec;
	}
}
