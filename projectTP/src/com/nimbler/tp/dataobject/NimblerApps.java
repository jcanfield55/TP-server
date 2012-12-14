package com.nimbler.tp.dataobject;

import java.util.Map;
/**
 * 
 * @author nIKUNJ
 *
 */
public class NimblerApps {

	private Map<String, Integer> appBundleToAppIdentifierMap;
	private Map<Integer, String> appIdentifierToAgenciesMap;//agencies must be comma separated ordinal values of TPConstants.AGENCY_TYPE
	
	public Map<String, Integer> getAppBundleToAppIdentifierMap() {
		return appBundleToAppIdentifierMap;
	}
	public void setAppBundleToAppIdentifierMap(Map<String, Integer> appBundleToAppIdentifierMap) {
		this.appBundleToAppIdentifierMap = appBundleToAppIdentifierMap;
	}
	public Map<Integer, String> getAppIdentifierToAgenciesMap() {
		return appIdentifierToAgenciesMap;
	}
	public void setAppIdentifierToAgenciesMap(Map<Integer, String> appIdentifierToAgenciesMap) {
		this.appIdentifierToAgenciesMap = appIdentifierToAgenciesMap;
	}
}