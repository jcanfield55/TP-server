/*
 * @author nirmal
 */
package com.nimbler.tp.service.livefeeds.stub;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.trimet.schedule.ResultSet;
import com.nimbler.tp.dataobject.trimet.vehicleposition.TrimetVehiclePosResponse;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.TpException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;


public class TrimetApiClient {
	private static final String	baseUrl	= "http://developer.trimet.org/ws/V1/routeConfig";	
	private static final String	arrivalBaseUrl	= "http://developer.trimet.org/ws/V1/arrivals";	
	private  String	vahiclePosBaseUrl	= "http://developer.trimet.org/beta/v2/vehicles";	

	private String	apiKey	= "753120DE15A79F84E52EBFC12";
	private Client	client	= null;

	@SuppressWarnings("unused")
	@PostConstruct
	private void init() {
		client = Client.create();
	}
	/**
	 * Gets the all routes details.
	 * http://developer.trimet.org/ws/V1/routeConfig?
	 *	// route=xyz&dir=true&tp=true&appid=753120DE15A79F84E52EBFC12/tp/true
	 * @param apiKey the api key
	 * @return the all routes details
	 * @throws RealTimeDataException the real time data exception
	 */
	public ResultSet getAllRoutesDetails() throws RealTimeDataException {
		// 
		try {
			WebResource webResource = client.resource(baseUrl);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("appid", apiKey);
			queryParams.add("dir", "true");
			queryParams.add("route", "all");// any string
			queryParams.add("tp", "true");// any string
			ClientResponse resp = webResource.queryParams(queryParams)
					.accept("text/xml").get(ClientResponse.class);
			return resp.getEntity(ResultSet.class);
		} catch (ClientHandlerException e) {
			throw new RealTimeDataException(
					"Error while getting Trimet Api route list"	+ e.getMessage());
		} catch (UniformInterfaceException uie) {
			throw new RealTimeDataException("Error while getting Trimet Api route list"+ uie.getMessage());
		}
	}

	/**
	 * Gets the vehicle position.
	 *
	 * @return the vehicle position
	 * @throws RealTimeDataException the real time data exception
	 * @throws TpException 
	 */
	public TrimetVehiclePosResponse getVehiclePosition(Long since) throws RealTimeDataException {
		try {
			WebResource webResource = client.resource(vahiclePosBaseUrl);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			if(since!=null)
				queryParams.add("since", since+"");			
			queryParams.add("appid", apiKey);			
			ClientResponse resp = webResource.queryParams(queryParams)
					.accept("application/json").get(ClientResponse.class);
			String res = resp.getEntity(String.class);
			TrimetVehiclePosResponse response = (TrimetVehiclePosResponse) JSONUtil.getObjFromJson(res, TrimetVehiclePosResponse.class);
			return response;
		} catch (TpException e) {
			throw new RealTimeDataException(e);
		} catch (ClientHandlerException e) {
			throw new RealTimeDataException("Error while getting Trimet Api route list"	+ e.getMessage());
		} catch (UniformInterfaceException uie) {
			throw new RealTimeDataException("Error while getting Trimet Api route list"+ uie.getMessage());
		}
	}

	/**
	 * Gets the arrival for stops.
	 *
	 * @param stopId the stop id
	 * @return the arrival for stops
	 * @throws RealTimeDataException the real time data exception
	 */
	public com.nimbler.tp.dataobject.trimet.arrivals.ResultSet getArrivalForStops(String stopId) throws RealTimeDataException {
		return getArrivalForStops(Lists.newArrayList(stopId));
	}

	/**
	 * Gets the arrivals.
	 * http://developer.trimet.org/ws/V1/arrivals?locIDs=9969&appID=753120DE15A79F84E52EBFC12
	 * @param lstStops the lst stops
	 * @return the arrivals
	 * @throws RealTimeDataException the real time data exception
	 */
	public com.nimbler.tp.dataobject.trimet.arrivals.ResultSet getArrivalForStops(List<String> lstStops) throws RealTimeDataException {
		try {
			String locIds = StringUtils.join(lstStops.iterator(), ","); 
			WebResource webResource = client.resource(arrivalBaseUrl);
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("appid", apiKey);
			queryParams.add("locIDs", locIds);
			ClientResponse resp = webResource.queryParams(queryParams)
					.accept("text/xml").get(ClientResponse.class);
			return resp.getEntity(com.nimbler.tp.dataobject.trimet.arrivals.ResultSet.class);
		} catch (ClientHandlerException e) {
			throw new RealTimeDataException(
					"Error while getting Trimet Api route list"	+ e.getMessage());
		} catch (UniformInterfaceException uie) {
			throw new RealTimeDataException("Error while getting Trimet Api route list"+ uie.getMessage());
		}
	}
	public static void main(String[] args) {
		try {
			TrimetApiClient apiClient = new TrimetApiClient();
			apiClient.init();
			TrimetVehiclePosResponse res = apiClient.getVehiclePosition(null);
			System.out.println(res);
		} catch (RealTimeDataException e) {
			e.printStackTrace();
		}
	}
}
