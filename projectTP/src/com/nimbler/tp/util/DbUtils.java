/*
 * @author nirmal
 */
package com.nimbler.tp.util;

import com.mongodb.BasicDBObject;
import com.nimbler.tp.mongo.MongoQueryConstant;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;

/**
 * The Class DbUtils.
 *
 * @author nirmal
 */
public class DbUtils {

	/**
	 * Ge default app filter query.
	 *
	 * @param appType the app type
	 * @param deviceTocken the device tocken
	 * @return the basic db object
	 */
	public static  BasicDBObject getDefaultAppFilterQuery(Integer appType, String deviceTocken) {
		if(appType==null)
			appType = NIMBLER_APP_TYPE.CALTRAIN.ordinal();
		BasicDBObject query = new BasicDBObject();
		if(deviceTocken!=null)
			query.put(TpConstants.DEVICE_TOKEN, deviceTocken);
		if(appType == NIMBLER_APP_TYPE.CALTRAIN.ordinal())
			query.put(TpConstants.APP_TYPE, new BasicDBObject(MongoQueryConstant.IN, new Object[]{null,NIMBLER_APP_TYPE.CALTRAIN.ordinal()}));//handle caltrain app
		else
			query.put(TpConstants.APP_TYPE, appType);
		return query;
	}

	/**
	 * Ge default app filter query.
	 *
	 * @param appType the app type
	 * @return the basic db object
	 */
	public static BasicDBObject getDefaultAppFilterQuery(Integer appType) {
		return getDefaultAppFilterQuery(appType,null);
	}
}
