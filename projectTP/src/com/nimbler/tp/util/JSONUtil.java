/*
 * @author nirmal
 */
package com.nimbler.tp.util;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.nimbler.tp.dataobject.Itinerary;
import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.dataobject.TripPlan;
import com.nimbler.tp.dataobject.TripResponse;
import com.nimbler.tp.util.OperationCode.TP_CODES;
/**
 * 
 * @author nikunj
 *
 */
public class JSONUtil {
	/**
	 * 
	 * @param plan
	 * @return
	 * @throws TPException
	 */
	public static String getJsonFromObj(Object obj) throws TpException {
		if(obj == null)
			return null;
		Gson gson = new Gson();
		String response =  gson.toJson(obj);
		if (response==null)
			throw new TpException(TP_CODES.FAIL.getCode(),"Error while getting JSON String from plan object.");
		return response;
	}
	public static Itinerary getItineraryJson(String jSon) {
		if(jSon == null)
			return null;
		Gson gson = new Gson();
		Itinerary response =  gson.fromJson(jSon,Itinerary.class);
		return response;
	}
	/**
	 * 
	 * @param planJsonString
	 * @return
	 * @throws TPException
	 */
	public static TripPlan getPlanObjFromJson(String planJsonString) throws TpException {
		Gson gson = new Gson();
		TripResponse response =  gson.fromJson(planJsonString, TripResponse.class);
		if (response==null || response.getPlan()==null) {
			throw new TpException("Error while getting Planb object from JSON string."); 
		}
		return response.getPlan();
	}

	/**
	 * Gets the full plan obj from json.
	 *
	 * @param planJsonString the plan json string
	 * @return the full plan obj from json
	 * @throws TpException the tp exception
	 */
	public static TripResponse getFullPlanObjFromJson(String planJsonString) throws TpException {
		Gson gson = new Gson();
		TripResponse response =  gson.fromJson(planJsonString, TripResponse.class);
		return response;
	}
	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static String getResponseJSON(TPResponse obj) {
		if(obj == null)
			return null;
		Gson gson = new Gson();
		return  gson.toJson(obj);
	}

	public static Object jPath(String data,String path){
		Object res = null;
		try {
			res =  JsonPath.read(data, path);
		} catch (Exception e) {	}
		return res;
	}
}