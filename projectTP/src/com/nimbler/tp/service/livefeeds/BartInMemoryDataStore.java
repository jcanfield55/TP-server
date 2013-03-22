package com.nimbler.tp.service.livefeeds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.bart.BartResponse;
import com.nimbler.tp.dataobject.bart.Route;
import com.nimbler.tp.dataobject.bart.Routes;
import com.nimbler.tp.service.livefeeds.stub.BARTApiClient;
import com.nimbler.tp.util.BeanUtil;
/**
 * Holds default data used while processing real time data from BART API.<br>
 * It holds data which is static and will not change over time.(May change over a long time.)<br>
 * For example, route list, route details etc.
 * @author nIKUNJ
 *
 */
public class BartInMemoryDataStore {

	private static BartInMemoryDataStore dataStore = new  BartInMemoryDataStore();

	private static BARTApiClient bartClient = BeanUtil.getBARTAPIClient();
	/**
	 * Contains mapping between OTP route names and route names that we are getting in real time data from BART API.
	 */
	private Map<String, String> otpRouteToBartRouteMap = new HashMap<String, String>(){
		@Override
		public String put(String key, String value) {
			return super.put(key.toLowerCase(), value);	
		};
		@Override
		public String get(Object key) {
			return super.get(((String)key).toLowerCase());
		};
	};
	/**
	 * Contains mapping between route names to its details.
	 */
	private Map<String, Route> routeDetailMap = new HashMap<String, Route>();

	public static BartInMemoryDataStore getInstance() {
		return dataStore;
	}
	/**
	 * 
	 */
	private BartInMemoryDataStore() {
		initOtpRouteToBartRouteMap();
	}
	/**
	 * Returns route details.
	 * @param routeTagFromOTP
	 * @param regKey
	 * @return
	 * @throws RealTimeDataException
	 */
	public Route getRouteInfo(String routeTagFromOTP, String regKey) throws RealTimeDataException {
		String bartRouteTag = otpRouteToBartRouteMap.get(routeTagFromOTP);
		if (bartRouteTag!=null) {
			Route route = routeDetailMap.get(bartRouteTag);
			if (route == null) {
				BartResponse response = bartClient.getRouteList(regKey);
				List<Routes> routes = response.getRoutes();
				if (routes == null || routes.size()==0)
					throw new RealTimeDataException("No routes found in GET ROUTE INFO response for BART.");

				List<Route> lstRoutes = routes.get(0).getRoute();
				if (lstRoutes == null || lstRoutes.size()==0)
					throw new RealTimeDataException("No routes found in GET ROUTE INFO response for BART.");

				for (Route rt: lstRoutes) {
					routeDetailMap.put(rt.getName(), rt);
				}
				return routeDetailMap.get(bartRouteTag);
			} else 
				return route;
		} else
			throw new RealTimeDataException("Proper Route tag mapping not found for OTP route name: "+routeTagFromOTP);
	}
	/**
	 * 
	 */
	private void initOtpRouteToBartRouteMap() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(BartInMemoryDataStore.class.getClassLoader().getResourceAsStream("conf/OtpRouteToBARTRoutes.properties")));
			String s = null;
			while ((s=reader.readLine())!=null) {
				String[] arr = s.split("=");
				if (arr.length==2)
					otpRouteToBartRouteMap.put(arr[0], arr[1]); 
			}
		} catch (IOException e) {
			System.err.println("OTP routes to BART routes mapping file not found: "+e.getMessage());
		}  finally {
			if (reader!=null)
				try {
					reader.close();
				} catch (IOException e) {
					System.err.println("Error while closing input stream for file: conf/OtpRouteToBARTRoutes.properties-->"+e.getMessage());
				}
		}
	}
}