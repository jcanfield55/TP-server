package com.nimbler.tp.service.livefeeds.stub;

import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.bart.BartResponse;
import com.nimbler.tp.service.LoggingService;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
/**
 * REST client for BART real time data service.
 * @author suresh
 *
 */
public class BARTApiClient {

	@Autowired
	private LoggingService logger;

	private String loggerName;
	private Map<String, String> routeMap;
	private String baseUrl;
	private Client client;

	public void init(){
		client = Client.create();
	}
	/**
	 * Returns all routes available in BART.
	 * @param regKey
	 * @return
	 * @throws RealTimeDataException
	 */
	public BartResponse getRouteList(String regKey) throws RealTimeDataException {
		try {
			String url = baseUrl+routeMap.get("ROUTE_LIST");
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("cmd", "routes");
			queryParams.add("key", regKey);
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);			
			return resp.getEntity(BartResponse.class);
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting route list"+e.getMessage());
		} catch (UniformInterfaceException uie) {	
			logger.error(loggerName, uie.getMessage());
			throw new RealTimeDataException("Error while getting route list"+uie.getMessage()); 
		}
	}
	/**
	 * Returns route details for specified route with given name.
	 * @param routeTag
	 * @param regKey
	 * @return
	 * @throws RealTimeDataException
	 */
	public BartResponse getRouteInfo(String routeTag, String regKey) throws RealTimeDataException {
		try{
			String url = baseUrl +routeMap.get("ROUTE_LIST");
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("cmd", "routeinfo");
			queryParams.add("route", routeTag);
			queryParams.add("key", regKey);
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);
			return resp.getEntity(BartResponse.class);
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e);
			throw new RealTimeDataException("Error while getting routeInfo "+e.getMessage());
		} catch (UniformInterfaceException uie) {
			logger.error(loggerName, uie);
			throw new RealTimeDataException("Error while getting routeInfo "+uie.getMessage());
		}
	}
	/**
	 * Returns Estimate time of departure from specified station.
	 * @param origin
	 * @param regKey
	 * @return
	 * @throws RealTimeDataException
	 */
	public BartResponse getEstimationTime(String origin, String regKey) throws RealTimeDataException {
		try {
			String url = baseUrl + routeMap.get("ESTIMATE_TIME");
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("cmd", "etd");
			queryParams.add("orig", origin);
			queryParams.add("key", regKey);
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);
			return resp.getEntity(BartResponse.class);
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e);
			throw new RealTimeDataException("Error while getting estimation time "+e.getMessage());
		} catch (UniformInterfaceException uie) {
			logger.error(loggerName, uie);
			throw new RealTimeDataException("Error while getting estimation time "+uie.getMessage());
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

}
