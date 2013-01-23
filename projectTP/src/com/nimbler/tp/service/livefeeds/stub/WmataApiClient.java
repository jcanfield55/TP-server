package com.nimbler.tp.service.livefeeds.stub;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MultivaluedMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.wmata.BusStop;
import com.nimbler.tp.dataobject.wmata.Predictions;
import com.nimbler.tp.dataobject.wmata.RailLine;
import com.nimbler.tp.dataobject.wmata.RailPrediction;
import com.nimbler.tp.dataobject.wmata.RailStation;
import com.nimbler.tp.dataobject.wmata.WmataBusPredictions;
import com.nimbler.tp.dataobject.wmata.WmataRailLines;
import com.nimbler.tp.dataobject.wmata.WmataRailPredictions;
import com.nimbler.tp.dataobject.wmata.WmataRailStations;
import com.nimbler.tp.dataobject.wmata.WmataStops;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.ComUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
/**
 * REST client for WMATA real time data service.
 * @author nirmal
 *
 */
public class WmataApiClient {

	@Autowired
	private LoggingService logger;

	private String loggerName = "com.nimbler.tp.service.livefeeds";
	private Map<String, String> routeMap;
	private String baseUrl = "http://api.wmata.com/";
	private Client client;

	public String PARAM_API_KEY = "api_key";
	public String PARAM_ROUTE_ID = "routeId";
	Gson gson = null;

	@PostConstruct
	public void init(){
		client = Client.create();
		gson = new Gson();
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
	public WmataStops getStopsNearPoint(String regKey,String lat,String lon,String radius) throws RealTimeDataException {
		try {
			String url = baseUrl+routeMap.get("GET_STOP");
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("lat", lat);
			queryParams.add("lon", lon);
			queryParams.add("radius", radius);
			queryParams.add(PARAM_API_KEY, regKey);
			String strResp = webResource.queryParams(queryParams).accept("application/json").get(String.class);
			WmataStops res =  gson.fromJson(strResp, WmataStops.class);
			return res;
		} catch (RuntimeException e) {
			System.out
			.println("WmataApiClient.getStopsNearPoint() --> regKey: "+ regKey + " lat: " + lat + " lon: " + lon
					+ " radius: " + radius);
			e.printStackTrace();
			//			logger.error(loggerName, e.getMessage());
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
	public List<BusStop> getAllStops(String regKey) throws RealTimeDataException {
		try {
			String url = baseUrl+routeMap.get("GET_STOP");
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			String strResp = webResource.queryParams(queryParams).accept("application/json").get(String.class);
			WmataStops res =  gson.fromJson(strResp, WmataStops.class);
			if((res==null ||  ComUtils.isEmptyList(res.getStops())))
				throw new FeedsNotFoundException("No Stops Found.");
			return res.getStops();
		} catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting stops near point"+e.getMessage());
		}
	}

	/**
	 * Gets the bus prediction at stop.
	 *
	 * @param regKey the reg key
	 * @param stopId the stop id
	 * @throws RealTimeDataException
	 */
	public List<Predictions> getBusPredictionAtStop(String regKey,String stopId) throws RealTimeDataException {
		try {
			String url = baseUrl+routeMap.get("BUS_PREDICTION");
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("StopID", stopId);
			queryParams.add(PARAM_API_KEY, regKey);
			String strResp = webResource.queryParams(queryParams).accept("application/json").get(String.class);
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
		try {
			String url = baseUrl+routeMap.get("RAIL_LINES");
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			String strResp = webResource.queryParams(queryParams).accept("application/json").get(String.class);
			WmataRailLines res =  gson.fromJson(strResp, WmataRailLines.class);
			if((res==null ||  ComUtils.isEmptyList(res.getLines())))
				throw new FeedsNotFoundException("No Rail Line Found.");
			return res.getLines();
		} catch (RuntimeException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting Rail Lines"+e.getMessage());
		}
	}
	public List<RailPrediction> getRailPrediction(String regKey,String station) throws RealTimeDataException {
		try {
			String url = baseUrl+routeMap.get("RAIL_PREDICTION")+station;
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			String strResp = webResource.queryParams(queryParams).accept("application/json").get(String.class);
			System.out.println(strResp);
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
	 * Gets the rail station by lines.
	 *
	 * @param regKey the reg key
	 * @return the rail station by lines
	 * @throws RealTimeDataException the real time data exception
	 */
	public List<RailStation> getRailStationByLines(String regKey,String line) throws RealTimeDataException {
		try {
			String url = baseUrl+routeMap.get("RAIL_STATION_BY_LINES");
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(PARAM_API_KEY, regKey);
			queryParams.add("LineCode", line);
			String strResp = webResource.queryParams(queryParams).accept("application/json").get(String.class);
			WmataRailStations res =  gson.fromJson(strResp, WmataRailStations.class);
			if((res==null ||  ComUtils.isEmptyList(res.getLstStations())))
				throw new FeedsNotFoundException("No Rail Stations Found.");
			return res.getLstStations();
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
	/*	public static void main(String[] args) {
		try {
			WmataApiClient apiClient = new WmataApiClient();
			Map<String, String> map = new HashMap<String, String>();
			map.put("GET_STOP", "Bus.svc/json/JStops");
			map.put("BUS_PREDICTION", "NextBusService.svc/json/JPredictions");
			map.put("RAIL_LINES", "Rail.svc/json/JLines");
			map.put("RAIL_STATION_BY_LINES", "Rail.svc/json/JStations");
			apiClient.init();
			apiClient.setRouteMap(map);
			System.out.println(apiClient.getStopsNearPoint("wateq3gxqzb9s597qky6khd7", "38.964658","-77.062833", "10"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	public LoggingService getLogger() {
		return logger;
	}

	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}

}
