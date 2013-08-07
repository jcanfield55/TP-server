/*
 * @author nirmal
 */
package com.nimbler.tp.service.livefeeds.stub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MultivaluedMap;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.my511.My511Response;
import com.nimbler.tp.dataobject.my511.My511Route;
import com.nimbler.tp.dataobject.my511.My511RouteDirection;
import com.nimbler.tp.dataobject.my511.My511Stop;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.GtfsUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * The Client for 511.org
 *
 * @author nirmal
 */
public class My511ApiClient {
	@Autowired
	private LoggingService logger;

	private String baseUrl = "http://services.my511.org/Transit2.0/";
	private String loggerName = "com.nimbler.tp.service.livefeeds";
	private Client client;
	private Map<URL_KEY, String> urlMap;

	private String apiToken = "27b8b5da-a35e-4102-8778-e5d7e0527264";
	enum URL_KEY{
		GET_AGENCY_LIST(""),
		GET_ROUTE_LIST(""),
		GET_STOPS_FOR_ROUTE(""),
		NEXT_DEPARTUTE_FOR_STOP_ID("");
		private URL_KEY(String name) {
			this.name = name;
		}
		String name;
		public String gete() {
			return name;
		}
	}
	private enum API_PARAM{
		TOKEN("token"),
		ROUTE_IDF("routeIDF"),
		AGENCY_NAME("agencyName"),
		STOP_CODE("stopcode");
		API_PARAM(String name) {
			this.param = name;
		}
		String param;
		public String getParam() {
			return param;
		}
	}

	@PostConstruct
	public void init(){
		client = Client.create();
		if(urlMap == null){
			urlMap = new HashMap<URL_KEY, String>();
			urlMap.put(URL_KEY.GET_AGENCY_LIST, "GetAgencies.aspx");
			urlMap.put(URL_KEY.GET_ROUTE_LIST, "GetRoutesForAgency.aspx");
			urlMap.put(URL_KEY.GET_STOPS_FOR_ROUTE, "GetStopsForRoute.aspx");
			urlMap.put(URL_KEY.NEXT_DEPARTUTE_FOR_STOP_ID, "GetNextDeparturesByStopCode.aspx");
		}
	}
	/**
	 * Return all routes available in NextBus agency.
	 * @param agencyId
	 * @return
	 * @throws RealTimeDataException
	 */
	public My511Response getAgencyList() throws RealTimeDataException {
		try {
			String url = baseUrl+urlMap.get(URL_KEY.GET_AGENCY_LIST);
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(API_PARAM.TOKEN.getParam(), apiToken);			
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);
			return resp.getEntity(My511Response.class);
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e);
			throw new RealTimeDataException("Error while getting Next Bus Api route list"+e.getMessage());
		} catch (UniformInterfaceException uie) {	
			logger.error(loggerName, uie);
			throw new RealTimeDataException("Error while getting Next Bus Api route list"+uie.getMessage()); 
		}
	}

	/**
	 * Gets the next depature for stop id.
	 *
	 * @param stopId the stop id
	 * @param agency 
	 * @return the next depature for stop id
	 * @throws RealTimeDataException the real time data exception
	 */
	public My511Response getNextDepatureForStopId(String stopId, String agency) throws RealTimeDataException {
		try {
			String url = baseUrl+urlMap.get(URL_KEY.NEXT_DEPARTUTE_FOR_STOP_ID);
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(API_PARAM.TOKEN.getParam(), apiToken);			
			queryParams.add(API_PARAM.STOP_CODE.getParam(), stopId);			
			queryParams.add(API_PARAM.AGENCY_NAME.getParam(), agency);			
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);
			return resp.getEntity(My511Response.class);
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e);
			throw new RealTimeDataException("Error while getting Next Bus Api route list"+e.getMessage());
		} catch (UniformInterfaceException uie) {	
			logger.error(loggerName, uie);
			throw new RealTimeDataException("Error while getting Next Bus Api route list"+uie.getMessage()); 
		}
	}

	/**
	 * Gets the stops for route.
	 *
	 * @param agency the agency
	 * @param route the route
	 * @param direction the direction
	 * @return the stops for route
	 * @throws RealTimeDataException the real time data exception
	 */
	public My511Response getStopsForRoute(String agency, String route,String direction) throws RealTimeDataException {
		try {
			String url = baseUrl+urlMap.get(URL_KEY.GET_STOPS_FOR_ROUTE);
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(API_PARAM.TOKEN.getParam(), apiToken);			
			queryParams.add(API_PARAM.ROUTE_IDF.getParam(), agency+"~"+route+"~"+direction);			
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);
			return resp.getEntity(My511Response.class);
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e);
			throw new RealTimeDataException("Error while getting Next Bus Api route list"+e.getMessage());
		} catch (UniformInterfaceException uie) {	
			logger.error(loggerName, uie);
			throw new RealTimeDataException("Error while getting Next Bus Api route list"+uie.getMessage()); 
		}
	}
	public My511Response getRoutes(String agencyId) throws RealTimeDataException {
		try {
			String url = baseUrl+urlMap.get(URL_KEY.GET_ROUTE_LIST);
			WebResource webResource = client.resource(url);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(API_PARAM.TOKEN.getParam(), apiToken);			
			queryParams.add(API_PARAM.AGENCY_NAME.getParam(), agencyId);			
			ClientResponse resp = webResource.queryParams(queryParams).accept("text/xml").get(ClientResponse.class);
			return resp.getEntity(My511Response.class);
		} catch (ClientHandlerException e) {
			logger.error(loggerName, e);
			throw new RealTimeDataException("Error while getting Next Bus Api route list"+e.getMessage());
		} catch (UniformInterfaceException uie) {	
			logger.error(loggerName, uie);
			throw new RealTimeDataException("Error while getting Next Bus Api route list"+uie.getMessage()); 
		}
	}
	public static void main(String[] args) {
		try {
			My511ApiClient apiClient = new My511ApiClient();
			apiClient.init();
			//My511Response reponse = apiClient.getAgencyList();
			List<Class<?>> lstClasses = new ArrayList<Class<?>>();
			lstClasses.add(Agency.class);
			lstClasses.add(Route.class);
			lstClasses.add(Trip.class);
			lstClasses.add(Stop.class);

			GtfsRelationalDaoImpl dao = GtfsUtils.getGtfsDao("E:\\nimbler\\GTFS\\SamTrans.zip", lstClasses);

			Multimap<String, String> multiMap = HashMultimap.create();
			Multimap<String, String> multiMapd = HashMultimap.create();
			for (Trip trip : dao.getAllTrips()) {
				multiMap.put(trip.getRoute().getShortName(), trip.getTripHeadsign());
				multiMapd.put(trip.getTripHeadsign(), trip.getDirectionId());
			}
			Set<String> stops = new HashSet<String>();
			for (Stop stop : dao.getAllStops()) {
				stops.add(stop.getCode());
			}
			//			System.out.println(multiMap);
			System.out.println("------------------------");
			My511Response reponse = apiClient.getRoutes("SamTrans");
			for (My511Route route : reponse.getAgencyList().get(0).getRoutes()) {
				System.out.println(route.getName()+" - "+route.getCode());				
				for (My511RouteDirection direction : route.getRoureDirection()) {
					System.out.println("     "+direction.getCode()+" - "+direction.getName());
					My511Response my511stops = apiClient.getStopsForRoute("SamTrans", route.getCode(), direction.getCode());
					for (My511RouteDirection dir : my511stops.getAgencyList().get(0).getRoutes().get(0).getRoureDirection()) {
						for (My511Stop stop : dir.getStops()) {
							if(!stops.contains(stop.getCode()))
								System.out.println("     not in gtfs: "+stop);
						}
					}
					System.out.println();
				}
				Collection<String> headSigns=  multiMap.get(route.getCode());
				if(headSigns==null){
					System.out.println("---- null ---");
					continue;
				}
				System.out.println("     ------");
				for (String string : headSigns) {
					System.out.println("     "+string+" - "+multiMapd.get(string));
				}
				System.out.println("-------------------------------------\n");
			}
			//			My511Response reponse = apiClient.getNextDepatureForStopId("311035","SamTrans");
			//			System.out.println(reponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
