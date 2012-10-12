package com.nimbler.tp.util;

import java.util.List;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dbobject.NimblerParams;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;

public class PersistantHelper {
	private LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private static String loggerName = "com.nimbler.tp.mongo.PersistenceService";

	public String getTestUserDeviceTokens() {
		String deviceTokens = "";
		try {
			PersistenceService persistenceService = BeanUtil.getPersistanceService();
			List<NimblerParams> resultSet = persistenceService.getCollectionList(MONGO_TABLES.nimbler_params.name(), NimblerParams.class);
			if (resultSet != null && resultSet.size() > 0){
				deviceTokens =  resultSet.get(0).getValue().trim();
			}
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage());
		} 
		return deviceTokens;
	}
}
