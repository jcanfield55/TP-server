package com.nimbler.tp.service.livefeeds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;

import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.nextbus.Direction;
import com.nimbler.tp.dataobject.nextbus.NextBusResponse;
import com.nimbler.tp.dataobject.nextbus.Route;
import com.nimbler.tp.service.livefeeds.stub.NextBusApiClient;
import com.nimbler.tp.util.BeanUtil;

/**
 * Holds default data used while processing real time data from NextBus API.<br>
 * It holds data which is static and will not change over time.(May change over a long time.)<br>
 * For example, route list, route details etc.
 * @author nIKUNJ
 *
 */
public class NBInMemoryDataStore {

	private static NBInMemoryDataStore dataStore = null;

	static Map<String, String> otpRouteToMuniTag = new HashMap<String, String>();

	private static NextBusApiClient nextBusClient = BeanUtil.getNextBusAPIClient();

	public static synchronized NBInMemoryDataStore getInstance() {
		if(dataStore == null){
			dataStore = new  NBInMemoryDataStore();
		}
		return dataStore;
	}
	private void init() {
		try {
			Properties property = new Properties();
			property.load(NBInMemoryDataStore.class.getClassLoader().getResourceAsStream("conf/OtpRouteToMuniRoutes.properties"));
			for (Object obj : property.keySet()) {
				String key = (String) obj;
				otpRouteToMuniTag.put(key.toLowerCase(), property.getProperty(key));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private NBInMemoryDataStore() {
		init();
	}
	/**
	 * List of routes available in NextBus in details. 
	 */
	private List<Route> nextBusRouteConfigs = new ArrayList<Route>();
	/**
	 * Route tag to route title map.
	 */
	private Map<String, DualHashBidiMap> nextBusRouteMap=  new HashMap<String, DualHashBidiMap>(); 

	/**
	 * 
	 * @param route
	 */
	public void addNextBusRouteConfig (Route route) {
		this.nextBusRouteConfigs.add(route); 
	}
	/**
	 * Get list of directions for specified routes. Directions will contain list 
	 * of stops in that route.
	 * @param agencyId
	 * @param routeTag
	 * @return
	 * @throws RealTimeDataException
	 */
	public List<Direction> getRouteDirections(String agencyId, String routeTag) throws RealTimeDataException {
		List<Direction> routeDirections = null;
		for (Route route : nextBusRouteConfigs) {
			if (route.getTag().equalsIgnoreCase(routeTag)) {
				routeDirections =  route.getDirection();
				return routeDirections;
			}
		}
		NextBusResponse respBody = nextBusClient.getRouteConfig(agencyId, routeTag);
		if (respBody!=null && respBody.getRoute()!=null && respBody.getRoute().size()>0) {
			Collection<Route> synchedList = Collections.synchronizedCollection(nextBusRouteConfigs);
			synchedList.add(respBody.getRoute().get(0));
			routeDirections = respBody.getRoute().get(0).getDirection();
		}
		return routeDirections;
	}
	/**
	 * Get route tag from route title.
	 * @param agencyId
	 * @param routeTag
	 * @return
	 * @throws RealTimeDataException
	 */
	public String getRouteTag(String agencyId, String routeTag) throws RealTimeDataException {
		String agency = otpRouteToMuniTag.get("default.agency");
		if(StringUtils.equalsIgnoreCase(agencyId, agency)){
			String res = otpRouteToMuniTag.get(routeTag.toLowerCase());
			if(res!=null)
				return res;	
		}

		DualHashBidiMap map = nextBusRouteMap.get(agencyId.toLowerCase());
		if(map == null){
			NextBusResponse respBody = nextBusClient.getRouteList(agencyId); 
			if (respBody!=null && respBody.getRoute()!=null && respBody.getRoute().size()>0) {
				map = new DualHashBidiMap();
				for (Route route : respBody.getRoute()) {
					map.put(route.getTag(), route.getTitle().toLowerCase());					
				}
				nextBusRouteMap.put(agencyId.toLowerCase(), map);
			}
		}	
		if(map==null)
			return null;
		if (map.containsKey(routeTag))
			return routeTag;
		String route = (String) map.getKey(routeTag.toLowerCase());
		return route;
	}
}