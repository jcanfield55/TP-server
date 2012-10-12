package com.nimbler.tp.dataobject;

import java.util.HashMap;
import java.util.Map;

public class BartRouteInfo {

	private String routeId;
	private String routeTag;
	private Map<String, String> headSignToDirectionMap = new HashMap<String, String>();
	
	public BartRouteInfo(String routeId, String routeTag) {
		this.routeId = routeId;
		this.routeTag = routeTag;
	}
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	public String getRouteTag() {
		return routeTag;
	}
	public void setRouteTag(String routeTag) {
		this.routeTag = routeTag;
	}
	public Map<String, String> getHeadSignToDirectionMap() {
		return headSignToDirectionMap;
	}
	public void setHeadSignToDirectionMap(Map<String, String> headSignToDirectionMap) {
		this.headSignToDirectionMap = headSignToDirectionMap;
	}
	@Override
	public String toString() {
		return "BartRouteInfo [routeId=" + routeId + ", routeTag=" + routeTag
				+ ", headSignToDirectionMap=" + headSignToDirectionMap + "]";
	}
}