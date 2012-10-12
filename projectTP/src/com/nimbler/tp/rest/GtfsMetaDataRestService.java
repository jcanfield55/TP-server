/*
 * @author nirmal
 */
package com.nimbler.tp.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.dataobject.GtfsResponse;
import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.gtfs.GtfsBundle;
import com.nimbler.tp.gtfs.GtfsDataMonitor;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.ResponseUtil;

@Path("/gtfs/")
public class GtfsMetaDataRestService {

	private LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private String loggerName;

	/**
	 * Gets the last gtfs update time.
	 *
	 * @return the last gtfs update time
	 */
	@GET
	@Path("/updateTime/")
	public String getLastGtfsUpdateTime() {
		TPResponse response = null;
		try {
			GtfsDataMonitor gtfsDataMonitor =  BeanUtil.getGtfsDataMonitorService();
			List<GtfsBundle> lstBundles =  gtfsDataMonitor.getGtfsBundles();
			Map<String, String> res = new HashMap<String, String>();
			for (GtfsBundle gtfsBundle : lstBundles) {
				if(gtfsBundle.isEnableAgency())
					res.putAll(getMapWithAgencies(gtfsBundle, gtfsBundle.getLastUpdateDate()));
			}
			response = new GtfsResponse(TP_CODES.SUCESS);
			((GtfsResponse)response).setGtfsUpdateTime(res);

		} catch (Exception e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}		
		return JSONUtil.getResponseJSON(response);
		//		return "{\"gtfsUpdateTime\":{\"caltrain-ca-us\":\"20120910\",\"MIDDAY\":\"20120910\",\"BART\":\"20120910\",\"VTA\":\"20120910\",\"AC Transit\":\"20120910\",\"SFMTA\":\"20120910\",\"AirBART\":\"20120910\"},\"code\":105,\"msg\":\"Operation Completed Sucessfully\"}";
	}

	/**
	 * Gets the map with agencies.
	 *
	 * @param gtfsBundle the gtfs bundle
	 * @param value the value
	 * @return the map with agencies
	 */
	private Map getMapWithAgencies(GtfsBundle gtfsBundle, Object value) {
		List<String> lstAgencies = gtfsBundle.getAgencyIds();
		Map map  = new HashMap();
		for (String angency : lstAgencies) {
			map.put(angency, value);
		}
		return map;
	}

	/**
	 * Gets the service by weekday by agency.
	 *
	 * @return the service by weekday by agency
	 */
	@GET
	@Path("/serviceByWeekday/")
	public String getServiceByWeekday() {
		TPResponse response = null;
		try {
			GtfsDataMonitor gtfsDataMonitor =  BeanUtil.getGtfsDataMonitorService();
			List<GtfsBundle> lstBundles =  gtfsDataMonitor.getGtfsBundles();
			Map<String, String[]> res = new HashMap<String, String[]>();
			for (GtfsBundle gtfsBundle : lstBundles) {
				if(gtfsBundle.isEnableAgency())
					res.putAll(getMapWithAgencies(gtfsBundle, gtfsBundle.getServiceOnDaysHash()));				
			}
			response = new GtfsResponse(TP_CODES.SUCESS);
			((GtfsResponse)response).setGtfsServiceByWeekDay(res);
		} catch (Exception e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		return JSONUtil.getResponseJSON(response);
		//		return "{\"gtfsServiceByWeekDay\":{\"caltrain-ca-us\":[\"WE_20120701\",\"WD_20120701\",\"WD_20120701\",\"WD_20120701\",\"WD_20120701\",\"WD_20120701\",\"ST_20120701,WE_20120701\"],\"MIDDAY\":[\"\",\"WD\",\"WD\",\"WD\",\"WD\",\"WD\",\"\"],\"BART\":[\"SUN,SUNAB\",\"M-FSAT,WKDY\",\"M-FSAT,WKDY\",\"M-FSAT,WKDY\",\"M-FSAT,WKDY\",\"M-FSAT,WKDY\",\"M-FSAT,SAT\"],\"VTA\":[\"ef485f4a261aed8ccfc97ebfef5c1207\",\"3f574248447f9c409607ac0f6314e3bd\",\"3f574248447f9c409607ac0f6314e3bd\",\"3f574248447f9c409607ac0f6314e3bd\",\"3f574248447f9c409607ac0f6314e3bd\",\"3f574248447f9c409607ac0f6314e3bd\",\"8b74c76d6c1ce85b5d73693158a52211\"],\"AC Transit\":[\"1206SU-System-Sunday-00\",\"8da2db328b2d1a902d75edf455ebd502\",\"aa05f91edc7912c685ae55eef601bd4b\",\"8da2db328b2d1a902d75edf455ebd502\",\"1de912baf464f4997caf29cf8e509cb2\",\"9456c69754e12b149f923bfe830f5be0\",\"1206SU-System-Saturday-00\"],\"SFMTA\":[\"3\",\"1\",\"1\",\"1\",\"1\",\"1\",\"2\"],\"AirBART\":[\"SUN,SUNAB\",\"M-FSAT,WKDY\",\"M-FSAT,WKDY\",\"M-FSAT,WKDY\",\"M-FSAT,WKDY\",\"M-FSAT,WKDY\",\"M-FSAT,SAT\"]},\"code\":105,\"msg\":\"Operation Completed Sucessfully\"}";
	}

	/**
	 * Gets the calendar by date.
	 *
	 * @return the calendar by date
	 */
	@GET
	@Path("/calendarByDate/")
	public String getDatesAndServiceException() {
		TPResponse response = null;
		try {
			GtfsDataMonitor gtfsDataMonitor =  BeanUtil.getGtfsDataMonitorService();
			List<GtfsBundle> lstBundles =  gtfsDataMonitor.getGtfsBundles();
			Map<String, Map<String,String>> res = new HashMap<String, Map<String,String>>();
			for (GtfsBundle gtfsBundle : lstBundles) {
				if(gtfsBundle.isEnableAgency())
					res.putAll(getMapWithAgencies(gtfsBundle, gtfsBundle.getDatesAndServiceWithException()));	
			}
			response = new GtfsResponse(TP_CODES.SUCESS);
			((GtfsResponse)response).setGtfsServiceExceptionDates(res);
		} catch (Exception e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		return JSONUtil.getResponseJSON(response);
		//		return "{\"gtfsServiceExceptionDates\":{\"caltrain-ca-us\":{\"20121122\":\"WE_20120701\",\"20120704\":\"WE_20120701\",\"20121225\":\"WE_20120701\",\"20130101\":\"WE_20120701\",\"20120903\":\"WE_20120701\"},\"MIDDAY\":{\"20111124\":\"\",\"20111125\":\"\",\"20110905\":\"\",\"20110704\":\"\",\"20120903\":\"\",\"20111226\":\"\",\"20140704\":\"\",\"20150101\":\"\",\"20140901\":\"\",\"20130902\":\"\",\"20120704\":\"\",\"20121225\":\"\",\"20141225\":\"\",\"20120528\":\"\",\"20141128\":\"\",\"20141127\":\"\",\"20121123\":\"\",\"20140526\":\"\",\"20131225\":\"\",\"20110530\":\"\",\"20121122\":\"\",\"20130527\":\"\",\"20120102\":\"\",\"20110101\":\"\",\"20131128\":\"\",\"20100906\":\"\",\"20131129\":\"\",\"20150703\":\"\",\"20100705\":\"\",\"20140101\":\"\",\"20130101\":\"\",\"20101224\":\"\",\"20150525\":\"\",\"20151225\":\"\",\"20130704\":\"\",\"20101126\":\"\",\"20101125\":\"\",\"20100531\":\"\",\"20151126\":\"\",\"20151127\":\"\",\"20150907\":\"\"},\"BART\":{\"20131225\":\"SUN,SUNAB\",\"20121122\":\"SUN,SUNAB\",\"20130218\":\"M-FSAT,SAT\",\"20130527\":\"SUN,SUNAB\",\"20120116\":\"M-FSAT,SAT\",\"20131128\":\"SUN,SUNAB\",\"20120903\":\"SUN,SUNAB\",\"20120220\":\"M-FSAT,SAT\",\"20140101\":\"SUN,SUNAB\",\"20130902\":\"SUN,SUNAB\",\"20130121\":\"M-FSAT,SAT\",\"20120704\":\"SUN,SUNAB\",\"20130101\":\"SUN,SUNAB\",\"20121225\":\"SUN,SUNAB\",\"20120528\":\"SUN,SUNAB\",\"20130704\":\"SUN,SUNAB\"},\"VTA\":{},\"AC Transit\":{\"20120704\":\"1206SU-System-Sunday-00\"},\"SFMTA\":{\"20120704\":\"3\",\"20120903\":\"3\"},\"AirBART\":{\"20131225\":\"SUN,SUNAB\",\"20121122\":\"SUN,SUNAB\",\"20130218\":\"M-FSAT,SAT\",\"20130527\":\"SUN,SUNAB\",\"20120116\":\"M-FSAT,SAT\",\"20131128\":\"SUN,SUNAB\",\"20120903\":\"SUN,SUNAB\",\"20120220\":\"M-FSAT,SAT\",\"20140101\":\"SUN,SUNAB\",\"20130902\":\"SUN,SUNAB\",\"20130121\":\"M-FSAT,SAT\",\"20120704\":\"SUN,SUNAB\",\"20130101\":\"SUN,SUNAB\",\"20121225\":\"SUN,SUNAB\",\"20120528\":\"SUN,SUNAB\",\"20130704\":\"SUN,SUNAB\"}},\"code\":105,\"msg\":\"Operation Completed Sucessfully\"}";
	}
	public LoggingService getLogger() {
		return logger;
	}
	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}
	public String getLoggerName() {
		return loggerName;
	}
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
}
