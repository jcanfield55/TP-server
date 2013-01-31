package com.nimbler.tp.dataobject;

import java.util.Map;
import java.util.TimeZone;

import com.nimbler.tp.service.livefeeds.RealTimeAPI;
/**
 * 
 * @author nIKUNJ
 *
 */
public class NimblerApps {

	private Map<String, Integer> appBundleToAppIdentifierMap;
	private Map<Integer, String> appIdentifierToAgenciesMap;//agencies must be comma separated ordinal values of TPConstants.AGENCY_TYPE
	private Map<String,RealTimeAPI> realTimeApiByAgency;
	/**
	 * NIMBLER_APP_TYPE, timrzone
	 */
	private Map<Integer,TimeZone> appTimeZone;


	public Map<String, Integer> getAppBundleToAppIdentifierMap() {
		return appBundleToAppIdentifierMap;
	}
	public void setAppBundleToAppIdentifierMap(Map<String, Integer> appBundleToAppIdentifierMap) {
		this.appBundleToAppIdentifierMap = appBundleToAppIdentifierMap;
	}
	public Map<Integer, String> getAppIdentifierToAgenciesMap() {
		return appIdentifierToAgenciesMap;
	}

	public Map<Integer, TimeZone> getAppTimeZone() {
		return appTimeZone;
	}

	public void setAppTimeZone(Map<Integer, TimeZone> appTimeZone) {
		this.appTimeZone = appTimeZone;
	}
	public void setAppIdentifierToAgenciesMap(Map<Integer, String> appIdentifierToAgenciesMap) {
		this.appIdentifierToAgenciesMap = appIdentifierToAgenciesMap;
	}
	public Map<String, RealTimeAPI> getRealTimeApiByAgency() {
		return realTimeApiByAgency;
	}

	public void setRealTimeApiByAgency(Map<String, RealTimeAPI> realTimeApiByAgency) {
		this.realTimeApiByAgency = realTimeApiByAgency;
	}
	public TimeZone getTimeZoneByApp(int app){
		return appTimeZone.get(app);
	}

}