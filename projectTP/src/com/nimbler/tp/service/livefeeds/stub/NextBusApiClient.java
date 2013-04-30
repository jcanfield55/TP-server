/*
 * @author nirmal
 */
package com.nimbler.tp.service.livefeeds.stub;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.nextbus.NextBusResponse;
import com.nimbler.tp.service.LoggingService;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
/**
 * REST client for NextBus real time API.
 * @author suresh
 *
 */
public class NextBusApiClient {
	@Autowired
	private LoggingService logger;

	private String baseUrl;
	private String loggerName = "com.nimbler.tp.service.livefeeds";
	private Client client;

	public void init(){
		client = Client.create();
	}
	/**
	 * Return all routes available in NextBus agency.
	 * @param agencyId
	 * @return
	 * @throws RealTimeDataException
	 */
	public NextBusResponse getRouteList(String agencyId) throws RealTimeDataException {
		try {
			WebResource webResource = client.resource(baseUrl);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("command", "routeList");
			queryParams.add("a", agencyId);
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);			
			return resp.getEntity(NextBusResponse.class);
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e);
			throw new RealTimeDataException("Error while getting Next Bus Api route list"+e.getMessage());
		} catch (UniformInterfaceException uie) {	
			logger.error(loggerName, uie);
			throw new RealTimeDataException("Error while getting Next Bus Api route list"+uie.getMessage()); 
		}
	}
	/**
	 * Returns NextBus route details for specified route name.
	 * @param agencyId
	 * @param rootTag
	 * @return
	 * @throws RealTimeDataException
	 */
	public NextBusResponse getRouteConfig(String agencyId, String rootTag) throws RealTimeDataException {
		try {
			WebResource webResource = client.resource(baseUrl);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("command", "routeConfig");
			queryParams.add("a", agencyId);
			queryParams.add("r", rootTag);
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);
			return resp.getEntity(NextBusResponse.class);
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e);
			throw new RealTimeDataException("Error while getting Next Bus Api routeConfig "+e.getMessage());
		} catch (UniformInterfaceException uie) {
			logger.error(loggerName, uie);
			throw new RealTimeDataException("Error while getting Next Bus Api routeInfo "+uie.getMessage());
		}
	}
	/**
	 * Returns prediction response for Next Bus real time data for specified agency-route-stop combination.
	 * @param agencyId
	 * @param stopTag
	 * @param rootTag
	 * @return
	 * @throws RealTimeDataException
	 */
	public NextBusResponse getPredictions(String agencyId, String stopTag, String rootTag) throws RealTimeDataException {
		try {
			WebResource webResource = client.resource(baseUrl);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("command", "predictions");
			queryParams.add("a", agencyId);
			queryParams.add("s", stopTag);
			queryParams.add("r", rootTag);
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);
			return resp.getEntity(NextBusResponse.class);
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e);
			throw new RealTimeDataException("Error while getting Next Bus Api prediction "+e.getMessage());
		} catch (UniformInterfaceException uie) {
			logger.error(loggerName, uie);
			throw new RealTimeDataException("Error while getting Next Bus Api prediction "+uie.getMessage());
		}
	}
	/**
	 * Get available agency list in NextBus.
	 * @return
	 * @throws RealTimeDataException
	 */
	public NextBusResponse getAgencyList() throws RealTimeDataException {
		try {
			WebResource webResource = client.resource(baseUrl);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("command", "agencyList");
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);
			return resp.getEntity(NextBusResponse.class);
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting Next Bus Api aggencyList "+e.getMessage());
		} catch (UniformInterfaceException e) {
			logger.error(loggerName, e.getMessage());
			throw new RealTimeDataException("Error while getting Next Bus Api aggencyList "+e.getMessage());
		}
	}
	/**
	 * Returns prediction response for Next Bus real time data of multiple stops of specific agency..
	 * @param agencyId
	 * @param multiStopWithRoute
	 * @return
	 * @throws RealTimeDataException
	 */
	public NextBusResponse getPredictionsForMultiStops(String agencyId, List<String> multiStopWithRoute) throws RealTimeDataException {
		try {
			WebResource webResource = client.resource(baseUrl);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("command", "predictions");
			queryParams.add("a", agencyId);
			for (String stop : multiStopWithRoute) {
				queryParams.add("stops", stop);
			}
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);
			return resp.getEntity(NextBusResponse.class);
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e);
			throw new RealTimeDataException("Error while getting Next Bus Api prediction "+e.getMessage());
		} catch (UniformInterfaceException uie) {
			logger.error(loggerName, uie);
			throw new RealTimeDataException("Error while getting Next Bus Api prediction "+uie.getMessage());
		}
	}

	/**
	 * Gets the vehicle position.
	 *
	 * @param agencyId the agency id
	 * @param routeTag the route tag
	 * @param time the time
	 * @return the vehicle position
	 * @throws RealTimeDataException the real time data exception
	 */
	public NextBusResponse getVehiclePosition(String agencyId, String routeTag,String time) throws RealTimeDataException {
		try {
			WebResource webResource = client.resource(baseUrl);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("command", "vehicleLocations");
			queryParams.add("a", agencyId);
			if(routeTag!=null)
				queryParams.add("r", routeTag	);
			queryParams.add("t", time);
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);			
			NextBusResponse response =  resp.getEntity(NextBusResponse.class);
			return response;
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e);
			throw new RealTimeDataException("Error while getting Next Bus VehiclePosition "+e.getMessage());
		} catch (UniformInterfaceException uie) {
			logger.error(loggerName, uie);
			throw new RealTimeDataException("Error while getting Next Bus VehiclePosition "+uie.getMessage());
		}
	}
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	public LoggingService getLogger() {
		return logger;
	}
	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}

}