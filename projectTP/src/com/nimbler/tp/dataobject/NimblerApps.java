/*
 * @author nirmal
 */
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
	/**
	 * agencies must be comma separated ordinal values of TPConstants.AGENCY_TYPE, 
	 * Agencies with advisory only
	 */
	private Map<Integer, String> appIdentifierToAgenciesMap;
	/**
	 * Used to get agency details, get all agency that is possible to get in routing 
	 */
	private Map<Integer, String> appRouteSupportAgencies;

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

	/**
	 * Gets the app identifier to agencies map.
	 *
	 * @return the app identifier to agencies map
	 */
	public Map<Integer, String> getAppIdentifierToAgenciesMap() {
		return appIdentifierToAgenciesMap;
	}


	/**
	 * Gets the app route support agencies.
	 *
	 * @return the app route support agencies
	 */
	public Map<Integer, String> getAppRouteSupportAgencies() {
		return appRouteSupportAgencies;
	}

	/**
	 * Sets the app route support agencies.
	 *
	 * @param appRouteSupportAgencies the app route support agencies
	 */
	public void setAppRouteSupportAgencies(
			Map<Integer, String> appRouteSupportAgencies) {
		this.appRouteSupportAgencies = appRouteSupportAgencies;
	}
	/**
	 * Gets the NIMBLER_APP_TYPE.ordinal(), timezone.
	 *
	 * @return the NIMBLER_APP_TYPE.ordinal(), timezone
	 */
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