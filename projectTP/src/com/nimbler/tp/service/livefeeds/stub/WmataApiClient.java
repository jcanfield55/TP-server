/*
 * @author nirmal
 */
package com.nimbler.tp.service.livefeeds.stub;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MultivaluedMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.common.RequestLimitExceedException;
import com.nimbler.tp.dataobject.wmata.BusStop;
import com.nimbler.tp.dataobject.wmata.RailLine;
import com.nimbler.tp.dataobject.wmata.RailPrediction;
import com.nimbler.tp.dataobject.wmata.RailStation;
import com.nimbler.tp.dataobject.wmata.RailStopSequence;
import com.nimbler.tp.dataobject.wmata.WmataBusPrediction;
import com.nimbler.tp.dataobject.wmata.WmataBusPredictions;
import com.nimbler.tp.dataobject.wmata.WmataRailLines;
import com.nimbler.tp.dataobject.wmata.WmataRailPredictions;
import com.nimbler.tp.dataobject.wmata.WmataRailStations;
import com.nimbler.tp.dataobject.wmata.WmataRouteDetails;
import com.nimbler.tp.dataobject.wmata.WmataRoutes;
import com.nimbler.tp.dataobject.wmata.WmataStops;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.livefeeds.WmataApiTask;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.SpeedLimiter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
/**
 * REST client for WMATA real time data service.
 * @author nirmal
 *
 */
public class WmataApiClient{



	@Autowired
	private LoggingService logger;

	private String loggerName = "com.nimbler.tp.service.livefeeds";
	private Map<String, String> routeMap;
	private String baseUrl = "http://api.wmata.com/";
	//	private Client client;

	public String PARAM_API_KEY = "api_key";
	public String PARAM_ROUTE_ID = "routeId";
	Gson gson = null;

	private Map<String,WebResource> resourcePool = new Hashtable<String, WebResource>();
	ExecutorService executorService = null;
	private boolean	shoultIntrruptTask	= true;

	private SpeedLimiter  speedLimiter = null;
	private int requestPerSec = 4;
	/**
	 * Time for which request for API call can wait in queue for resource availability.
	 */
	private int resourceTimeOutMillSec = 180000;
	/**
	 * counts daily request.
	 */
	private int	POOL_SIZE	= 4;

	private AtomicLong requestCounter = new AtomicLong();

	private int dailyLimit = 10000;

	@PostConstruct
	public void init(){
		speedLimiter = new SpeedLimiter(requestPerSec,1,TimeUnit.SECONDS).withRampUptime(800);
		gson = new Gson();
		executorService = Executors.newFixedThreadPool(POOL_SIZE);
	}

	public interface WmataFunction<T,U> {
		public T call(U arg) throws Exception;
	}

	/**
	 * Gets the stops near point.
	 *
	 * @param regKey the reg key
	 * @param lat the lat
	 * @param lon the lon
	 * @param radius the radius
	 * @return the stops near point
	 * @throws RealTimeDataException the real time data exception
	 */
	public WmataStops getStopsNearPoint(final String regKey,final String lat,final String lon,final String radius) throws RealTimeDataException {
		String url = null;
		try {
			url = baseUrl+routeMap.get("GET_STOP");
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("lat", lat);
			queryParams.add("lon", lon);
			queryParams.add("radius", radius);
			queryParams.add(PARAM_API_KEY, regKey);
			Future<String> future = executorService.submit(createTask(queryParams, url, regKey,true));
			String strResp = getResponseFromFuture(future);
			WmataStops res =  gson.fromJson(strResp, WmataStops.class);
			return res;
		} catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting stops near point"+e.getMessage());
		}
	}

	/**
	 * Gets the all stops.
	 *
	 * @param regKey the reg key
	 * @return the all stops
	 * @throws RealTimeDataException the real time data exception
	 */
	public List<BusStop> getBusAllStops(final String regKey) throws RealTimeDataException {
		try {
			String url = null;
			url = baseUrl+routeMap.get("GET_STOP");
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			Future<String> future = executorService.submit(createTask(queryParams, url, regKey,true));
			String strResp = getResponseFromFuture(future); 
			WmataStops res =  gson.fromJson(strResp, WmataStops.class);
			if((res==null ||  ComUtils.isEmptyList(res.getStops())))
				throw new FeedsNotFoundException("No Stops Found.");
			return res.getStops();
		} catch (JsonSyntaxException e) {
			throw new RealTimeDataException(e);
		}
	}


	/**
	 * Gets the bus prediction at stop.
	 *
	 * @param regKey the reg key
	 * @param stopId the stop id
	 * @throws RealTimeDataException
	 */
	public List<WmataBusPrediction> getBusPredictionAtStop(String regKey,String stopId) throws RealTimeDataException {
		String url = null;
		try {
			url = baseUrl+routeMap.get("BUS_PREDICTION");
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("StopID", stopId);
			queryParams.add(PARAM_API_KEY, regKey);
			Future<String> future = executorService.submit(createTask(queryParams, url, regKey,true));
			String strResp = getResponseFromFuture(future);
			WmataBusPredictions res =  gson.fromJson(strResp, WmataBusPredictions.class);
			if((res==null ||  ComUtils.isEmptyList(res.getPredictions())))
				throw new FeedsNotFoundException("No RealTime Feed Available For Stop:"+stopId);
			return res.getPredictions();
		} catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting stops near point"+e.getMessage());
		}
	}

	/**
	 * Gets the rail lines.
	 *
	 * @param regKey the reg key
	 * @return the rail lines
	 * @throws RealTimeDataException the real time data exception
	 */
	public List<RailLine> getRailLines(String regKey) throws RealTimeDataException {
		String url = null;
		try {
			url = baseUrl+routeMap.get("RAIL_LINES");
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			Future<String> future = executorService.submit(createTask(queryParams, url, regKey,true));
			String strResp = getResponseFromFuture(future);
			WmataRailLines res =  gson.fromJson(strResp, WmataRailLines.class);
			if((res==null ||  ComUtils.isEmptyList(res.getLines())))
				throw new FeedsNotFoundException("No Rail Line Found.");
			return res.getLines();
		} catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting Rail Lines"+e.getMessage());
		}
	}

	/**
	 * Gets the rail prediction.
	 *
	 * @param regKey the reg key
	 * @param station the station
	 * @return the rail prediction
	 * @throws RealTimeDataException the real time data exception
	 */
	public List<RailPrediction> getRailPrediction(String regKey,String station) throws RealTimeDataException {
		String url = null;
		try {
			url = baseUrl+routeMap.get("RAIL_PREDICTION")+station;
			//			WebResource webResource =getResource(url,false); //do not cache
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			Future<String> future = executorService.submit(createTask(queryParams, url, regKey,false));
			String strResp = getResponseFromFuture(future);
			WmataRailPredictions res =  gson.fromJson(strResp, WmataRailPredictions.class);
			if((res==null ||  ComUtils.isEmptyList(res.getTrains())))
				throw new FeedsNotFoundException("No Rail Line Found.");
			return res.getTrains();
		} catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting Rail Lines"+e.getMessage());
		}
	}

	/**
	 * Gets the all rail prediction.
	 *
	 * @param regKey the reg key
	 * @return the all rail prediction
	 * @throws RealTimeDataException the real time data exception
	 */
	public List<RailPrediction> getAllRailPrediction(String regKey) throws RealTimeDataException {
		String url = null;
		try {
			url = baseUrl+routeMap.get("RAIL_PREDICTION_ALL");
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			Future<String> future = executorService.submit(createTask(queryParams, url, regKey,true));
			String strResp = getResponseFromFuture(future);
			WmataRailPredictions res =  gson.fromJson(strResp, WmataRailPredictions.class);
			if((res==null ||  ComUtils.isEmptyList(res.getTrains())))
				throw new FeedsNotFoundException("No Rail Predictions  Found.");
			return res.getTrains();
		} catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting Rail Lines"+e.getMessage());
		}
	}

	/**
	 * Gets the rail stop sequence.
	 *
	 * @param regKey the reg key
	 * @param lineCode the line code
	 * @param from the from
	 * @param to the to
	 * @return the rail stop sequence
	 * @throws RealTimeDataException the real time data exception
	 */
	public RailStopSequence getRailStopSequence(String regKey,String lineCode,String from,String to) throws RealTimeDataException {
		String url = null;
		try {
			//TODO cretae constant
			url = "http://api.wmata.com/Rail.svc/json/JPath";
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			queryParams.add("FromStationCode", from);
			queryParams.add("ToStationCode", to);
			Future<String> future = executorService.submit(createTask(queryParams, url, regKey,true));
			String strResp = getResponseFromFuture(future);
			RailStopSequence res =  gson.fromJson(strResp, RailStopSequence.class);
			if(res == null || res.isEmpty())
				throw new FeedsNotFoundException("No stop sequence found.");
			return res;
		} catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting Rail Lines"+e.getMessage());
		}
	}

	/**
	 * Gets the rail station by lines.
	 *
	 * @param regKey the reg key
	 * @return the rail station by lines
	 * @throws RealTimeDataException the real time data exception
	 */
	public List<RailStation> getRailStationByLines(String regKey,String line) throws RealTimeDataException {
		String url = null;
		try {
			url = baseUrl+routeMap.get("RAIL_STATION_BY_LINES");
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			queryParams.add("LineCode", line);
			Future<String> future = executorService.submit(createTask(queryParams, url, regKey,true));
			String strResp = getResponseFromFuture(future);
			WmataRailStations res =  gson.fromJson(strResp, WmataRailStations.class);
			if((res==null ||  ComUtils.isEmptyList(res.getLstStations())))
				throw new FeedsNotFoundException("No Rail Stations Found.");
			return res.getLstStations();
		} catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting Rail Stations"+e.getMessage());
		}
	}

	/**
	 * Gets the bus route details.
	 *
	 * @param regKey the reg key
	 * @param route the route
	 * @return the bus route details
	 * @throws RealTimeDataException the real time data exception
	 */
	public WmataRouteDetails getBusRouteDetails(String regKey,String route) throws RealTimeDataException {
		String url = null;
		try {
			url = "http://api.wmata.com/Bus.svc/json/JRouteDetails";
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			queryParams.add("routeId", route);
			Future<String> future = executorService.submit(createTask(queryParams, url, regKey,true));
			String strResp = getResponseFromFuture(future);
			WmataRouteDetails res =  gson.fromJson(strResp, WmataRouteDetails.class);
			return res;
		} catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting Rail Stations"+e.getMessage());
		}
	}

	/**
	 * Gets the bus routes.
	 *
	 * @param regKey the reg key
	 * @return the bus routes
	 * @throws RealTimeDataException the real time data exception
	 */
	public WmataRoutes getBusRoutes(String regKey) throws RealTimeDataException {
		String url = null;
		try {
			url = "http://api.wmata.com/Bus.svc/json/JRoutes";
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			Future<String> future = executorService.submit(createTask(queryParams, url, regKey,true));
			String strResp = getResponseFromFuture(future);
			WmataRoutes res =  gson.fromJson(strResp, WmataRoutes.class);
			return res;
		} catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting Rail Stations"+e.getMessage());
		}
	}

	/**
	 * Gets the all bus positions.
	 *
	 * @param regKey the reg key
	 * @param inclideVarialnts the inclide varialnts
	 * @return the all bus positions
	 * @throws RealTimeDataException the real time data exception
	 */
	public WmataRoutes getAllBusPositions(String regKey,Boolean inclideVarialnts) throws RealTimeDataException {
		String url = null;
		try {
			url = "http://api.wmata.com/Bus.svc/json/JBusPositions";
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			if(inclideVarialnts!=null)
				queryParams.add("includingVariations", String.valueOf(inclideVarialnts));
			Future<String> future = executorService.submit(createTask(queryParams, url, regKey,true));
			String strResp = getResponseFromFuture(future);
			WmataRoutes res =  gson.fromJson(strResp, WmataRoutes.class);
			return res;
		} catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting Rail Stations"+e.getMessage());
		}
	}

	public void setRouteMap(Map<String, String> routeMap) {
		this.routeMap = routeMap;
	}
	public Map<String, String> getRouteMap() {
		return routeMap;
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	public String getBaseUrl() {
		return baseUrl;
	}
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	public String getLoggerName() {
		return loggerName;
	}
	public String getPARAM_API_KEY() {
		return PARAM_API_KEY;
	}
	public void setPARAM_API_KEY(String pARAM_API_KEY) {
		PARAM_API_KEY = pARAM_API_KEY;
	}
	public String getPARAM_ROUTE_ID() {
		return PARAM_ROUTE_ID;
	}
	public void setPARAM_ROUTE_ID(String pARAM_ROUTE_ID) {
		PARAM_ROUTE_ID = pARAM_ROUTE_ID;
	}

	public LoggingService getLogger() {
		return logger;
	}

	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}
	private Client createClient() {
		return  Client.create();
	}

	/**
	 * Gets the resource.
	 *
	 * @param url the url
	 * @param shouldCache the cache
	 * @return the resource
	 * @throws InterruptedException the interrupted exception
	 * @throws TimeoutException the timeout exception
	 */
	private WebResource getResource(String url,boolean shouldCache) throws InterruptedException, TimeoutException {
		if(requestCounter.get()>=dailyLimit)
			throw new RequestLimitExceedException("Daily request execeed. Limit:"+dailyLimit+",Requested:"+requestCounter.get());
		WebResource resource =  resourcePool.get(url);
		if(resource==null){
			Client client = createClient();
			resource = client.resource(url);
			if(shouldCache)
				resourcePool.put(url, resource);
		}
		boolean permission = speedLimiter.tryAcquire(resourceTimeOutMillSec, TimeUnit.MILLISECONDS);
		if(!permission)
			throw new TimeoutException("Wmata resource queue timeout while getting resource for "+url+",cache:"+shouldCache);
		//		System.out.println("resource aquired.."+System.currentTimeMillis());
		requestCounter.addAndGet(1);
		return  resource;
	}

	/**
	 * Creates the task.
	 *
	 * @param queryParams the query params
	 * @param url the url
	 * @param regKey the reg key
	 * @return the wmata api task
	 */
	public WmataApiTask createTask(final MultivaluedMap<String, String> queryParams,String url,final String regKey,final boolean shouldCache){
		return new WmataApiTask(new WmataFunction<String,String>() {
			@Override
			public String call(String resUrl) throws InterruptedException, TimeoutException {
				WebResource webResource = getResource(resUrl,shouldCache);
				return webResource.queryParams(queryParams).accept("application/json").get(String.class);
			}
		},url);
	}

	/**
	 * Gets the response from future.
	 *
	 * @param future the future
	 * @return the response from future
	 * @throws RealTimeDataException the real time data exception
	 */
	private String getResponseFromFuture(Future<String> future) throws RealTimeDataException {
		try {
			return future.get(resourceTimeOutMillSec,TimeUnit.SECONDS);
		} catch (Exception e) {
			future.cancel(shoultIntrruptTask);
			throw new RealTimeDataException("Timeout while waiting to complete future task: "+e.getMessage());
		}/*finally{
			System.out.println("released.....");
		}*/
	}

	public void resetRequestCounter() {
		logger.debug(loggerName, "Resetting Counter...."+requestCounter.get());
		requestCounter.set(0);
	}

	public int getRequestPerSec() {
		return requestPerSec;
	}

	public void setRequestPerSec(int requestPerSec) {
		this.requestPerSec = requestPerSec;
	}

	public int getResourceTimeOutMillSec() {
		return resourceTimeOutMillSec;
	}

	public void setResourceTimeOutMillSec(int resourceTimeOutMillSec) {
		this.resourceTimeOutMillSec = resourceTimeOutMillSec;
	}

	public AtomicLong getRequestCounter() {
		return requestCounter;
	}

	public void setRequestCounter(AtomicLong requestCounter) {
		this.requestCounter = requestCounter;
	}

	public int getDailyLimit() {
		return dailyLimit;
	}

	public void setDailyLimit(int dailyLimit) {
		this.dailyLimit = dailyLimit;
	}

	public boolean isShoultIntrruptTask() {
		return shoultIntrruptTask;
	}

	public void setShoultIntrruptTask(boolean shoultIntrruptTask) {
		this.shoultIntrruptTask = shoultIntrruptTask;
	}

	public int getPOOL_SIZE() {
		return POOL_SIZE;
	}

	public void setPOOL_SIZE(int pOOL_SIZE) {
		POOL_SIZE = pOOL_SIZE;
	}
	public static void main(String[] args) {
		final WmataApiClient apiClient = new WmataApiClient();
		LoggingService loggingService = new LoggingService();

		apiClient.setLogger(new LoggingService());
		apiClient.setLoggerName("test");
		apiClient.setBaseUrl("http://api.wmata.com/");
		final String regKey = "wateq3gxqzb9s597qky6khd7";
		Map<String, String> map = new HashMap<String, String>();
		map.put("GET_STOP", "Bus.svc/json/JStops");
		map.put("BUS_PREDICTION","NextBusService.svc/json/JPredictions");
		map.put("RAIL_LINES", "Rail.svc/json/JLines");
		map.put("RAIL_STATION_BY_LINES","Rail.svc/json/JStations");
		map.put("RAIL_PREDICTION","StationPrediction.svc/json/GetPrediction/");
		map.put("RAIL_PREDICTION_ALL","StationPrediction.svc/json/GetPrediction/All");
		apiClient.setRouteMap(map);
		apiClient.init();
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			new Thread() {
				public void run() {
					try {
						System.out.println(apiClient.getBusPredictionAtStop(regKey, "4000279"));
						System.out.println(apiClient.getBusPredictionAtStop(regKey, "4000269"));
						//						System.out.println(apiClient.getBusAllStops(regKey));
					} catch (NullPointerException e) {
						e.printStackTrace();						
					} catch (RealTimeDataException e) {
						System.out.println("==============================="+e);
					}
				}
			}.start();
		}
		System.out.println("done....");
		while(true){
			long total = apiClient.getRequestCounter().get();
			System.out.println(System.currentTimeMillis()+"-"+total);
			if(total==200){
				long end = System.currentTimeMillis();
				System.out.println("Operation took " + (end - start) + " msec");
				System.exit(0);
			}
			ComUtils.sleep(500);
		}
	}
}
