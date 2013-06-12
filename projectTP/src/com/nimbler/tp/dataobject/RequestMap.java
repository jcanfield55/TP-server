/*
 * @author nirmal
 */
package com.nimbler.tp.dataobject;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.trim;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.nimbler.tp.dbobject.User.BOOLEAN_VAL;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;

/**
 * The Class RequestMap.
 *
 * @author nirmal
 */
public class RequestMap implements Serializable{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -3456580115405125151L;
	Map<String,String> reqParam;

	/**
	 * static constructor
	 *
	 * @param req the request Map
	 * @return the request map
	 */
	public static RequestMap of(Map<String, String[]> req) {
		Map<String,String> reqParams = new HashMap<String, String>();
		for (Map.Entry<String, String[]> entry : req.entrySet()) {
			String key = entry.getKey();
			String[] value = entry.getValue();
			if(value!=null && value.length>0){
				reqParams.put(key, trim(value[0]));
			}
		}
		return new RequestMap(reqParams);
	}

	/**
	 * Instantiates a new request map.
	 *
	 * @param reqParam the req param
	 */
	public RequestMap(Map<String, String> reqParam) {
		if(reqParam==null)
			throw new RuntimeException("Request param map null");
		this.reqParam = reqParam;
	}
	public Integer getInt(String key, String defaultValue) {
		return NumberUtils.toInt(defaultIfEmpty(reqParam.get(key),defaultValue));
	}
	public Integer getInt(String key, int defaultValue) {
		return NumberUtils.toInt(reqParam.get(key),defaultValue);
	}
	public Long getLong(String key, String defaultValue) {
		return NumberUtils.toLong(defaultIfEmpty(reqParam.get(key),defaultValue));
	}
	public Double getDouble(String key, String defaultValue) {
		return NumberUtils.toDouble(defaultIfEmpty(reqParam.get(key),defaultValue));
	}
	public Double getDouble(String key) {
		return NumberUtils.toDouble(reqParam.get(key));
	}
	public int getBoolean(String key, boolean defaultValue) {
		String val = reqParam.get(key);
		if(StringUtils.isBlank(val)){
			return (defaultValue)? BOOLEAN_VAL.TRUE.ordinal():BOOLEAN_VAL.FALSE.ordinal();
		}
		return NumberUtils.toInt(val);
	}
	public String getString(String key, String defaultValue) {
		return defaultIfEmpty(reqParam.get(key),defaultValue);
	}
	public String getString(String key) {
		return reqParam.get(key);
	}
	public int getAppType() {
		return NumberUtils.toInt(reqParam.get(RequestParam.NIMBLER_APP_TYPE),NIMBLER_APP_TYPE.CALTRAIN.ordinal());
	}

	public Map<String, String> getReqParam() {
		return reqParam;
	}

	@Override
	public String toString() {		
		return "RequestMap [\n" + reqParam + "]";
	}
}
