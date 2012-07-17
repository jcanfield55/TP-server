package com.nimbler.tp.service.livefeeds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	
	private static NBInMemoryDataStore dataStore = new  NBInMemoryDataStore();
	
	private static NextBusApiClient nextBusClient = BeanUtil.getNextBusAPIClient();

	public static NBInMemoryDataStore getInstance() {
		return dataStore;
	}

	private NBInMemoryDataStore() {
	}
	/**
	 * List of routes available in NextBus in details. 
	 */
	private List<Route> nextBusRouteConfigs = new ArrayList<Route>();
	/**
	 * Route tag to route title map.
	 */
	private Map<String, String> nextBusRouteMap=  new HashMap<String, String>(); 

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
		if (nextBusRouteMap.size()==0) {
			NextBusResponse respBody = nextBusClient.getRouteList(agencyId); 
			if (respBody!=null && respBody.getRoute()!=null && respBody.getRoute().size()>0) {
				Map<String, String> synchedMap = Collections.synchronizedMap(nextBusRouteMap);
				for (Route route : respBody.getRoute()) {
					synchedMap.put(route.getTag(), route.getTitle());					
				}
			}
		}		
		boolean routeExists = nextBusRouteMap.containsKey(routeTag);
		if (routeExists)
			return routeTag;

		String routeTagInLiveFeed = null;
		Iterator<String> itr = nextBusRouteMap.keySet().iterator();
		while (itr.hasNext()) {
			String tag = itr.next();
			String title = nextBusRouteMap.get(tag);
			if (title.equalsIgnoreCase(routeTag)) {
				routeTagInLiveFeed = tag;
				break;
			}
		}
		return routeTagInLiveFeed;
	}
}