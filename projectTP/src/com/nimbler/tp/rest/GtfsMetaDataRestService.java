/*
 * @author nirmal
 */
package com.nimbler.tp.rest;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.dataobject.GtfsResponse;
import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.gtfs.GtfsBundle;
import com.nimbler.tp.gtfs.GtfsDataMonitor;
import com.nimbler.tp.gtfs.GtfsDataService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.ResponseUtil;
import com.nimbler.tp.util.TpConstants.GTFS_FILE;
import com.nimbler.tp.util.TpException;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;

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

	/**
	 * Gets the raw data.
	 *
	 * @param fileNames the file names
	 * @param strAgencyIds the str agency ids
	 * @return the raw data
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	@GET
	@Path("/rawdata/")	
	public String getRawData(@QueryParam(RequestParam.ENTITY)String fileNames,
			@DefaultValue("1,2,3,4") @QueryParam(RequestParam.AGENCY_IDS)String strAgencyIds) throws UnsupportedEncodingException, IOException, ClassNotFoundException {
		TPResponse response = ResponseUtil.createResponse(TP_CODES.SUCESS);
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		long start = System.currentTimeMillis();

		try {			
			if(ComUtils.isEmptyString(fileNames))
				throw new TpException(TP_CODES.INVALID_REQUEST);
			GtfsDataService gtfsDataService =  BeanUtil.getGtfsDataServiceBean();
			String[] arrFileName = fileNames.split(",");
			String[] agencyIds = strAgencyIds.split(",");
			for (int i = 0; i < arrFileName.length; i++) {
				GTFS_FILE file = null;
				for (GTFS_FILE f : GTFS_FILE.values()) {
					if(f.getName().equalsIgnoreCase(arrFileName[i])){
						file = f;
						break;
					}
				}
				if(file== null)
					throw new TpException(TP_CODES.INVALID_REQUEST);
				Map<String, List<String>> map =  gtfsDataService.getGtfsDataByAgency(file,agencyIds);
				resMap.putAll(map);
			}
			response.setData(resMap);
		} catch (TpException e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(e);			
		} catch (Exception e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		String res =  JSONUtil.getResponseJSON(response);
		long end = System.currentTimeMillis();		
		//		System.out.println("GetRawData took " + (end - start) + " mill sec");
		return res;
	}

	/**
	 * Gets the stop times.
	 *
	 * @param strAgencyId the str agency id
	 * @param tripId the trip id
	 * @return the stop times
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	//	@GET
	//	@Path("/stoptimes/")	
	//	public String getStopTimes(@QueryParam(RequestParam.AGENCY_IDS)String strAgencyId) throws UnsupportedEncodingException, IOException, ClassNotFoundException {
	@POST
	@Path("/stoptimes/")	
	public String getStopTimes(@Context HttpServletRequest httpRequest) throws UnsupportedEncodingException, IOException, ClassNotFoundException {
		TPResponse response = ResponseUtil.createResponse(TP_CODES.SUCESS);
		long start = System.currentTimeMillis();
		try {		
			Map<String,String> reqParam = ComUtils.parseMultipartRequest(httpRequest);
			String strAgencyId = reqParam.get(RequestParam.AGENCY_IDS);
			if(ComUtils.isEmptyString(strAgencyId))
				throw new TpException(TP_CODES.INVALID_REQUEST);
			GtfsDataService gtfsDataService =  BeanUtil.getGtfsDataServiceBean();			
			Map<String, List<String>> map =  gtfsDataService.getStopTimesByAgency(strAgencyId.split(","));
			response.setData(map);
		} catch (TpException e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(e);			
		} catch (Exception e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		String res =  JSONUtil.getResponseJSON(response);
		long end = System.currentTimeMillis();		
		//		System.out.println("GetStopTimes took " + (end - start) + "mill sec");
		return res;
	}
	@GET
	@Path("/stoptimes/all")	
	public Response getStopTimesAll() throws UnsupportedEncodingException, IOException, ClassNotFoundException {
		try {
			String fileName = "stop_times.zip";
			GtfsDataService gtfsDataService =  BeanUtil.getGtfsDataServiceBean();			
			File fileToSend = gtfsDataService.getStopTimesAll();
			ResponseBuilder rb = new ResponseBuilderImpl();
			rb.type("application/zip");
			rb.entity(fileToSend);
			rb.header("Content-Disposition",  "attachment; filename=\""+fileName+"\"");
			return Response.ok(fileToSend, "application/zip").build();
		} catch (TpException e) {
			return Response.noContent().build();
		} catch (Exception e) {
			logger.error(loggerName, e);
			return Response.serverError().build();
		}
	}

	/**
	 * Gets the trips.
	 *
	 * @param strAgencyId the str agency id
	 * @return the trips
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	@GET
	@Path("/trips/")	
	public String getTrips(@QueryParam(RequestParam.AGENCY_AND_ROUTE_IDS)String strAgencyIdAndRouteId) throws UnsupportedEncodingException, IOException, ClassNotFoundException {
		return handleGetTripsRequest(strAgencyIdAndRouteId);
	}
	@POST
	@Path("/trips/")
	public String getTrips(@Context HttpServletRequest request) throws UnsupportedEncodingException, IOException, ClassNotFoundException {
		TPResponse response = ResponseUtil.createResponse(TP_CODES.SUCESS);
		try {
			Map<String,String> req = ComUtils.parseMultipartRequest(request);
			String strAgencyIdAndRouteId = req.get(RequestParam.AGENCY_AND_ROUTE_IDS);			
			return handleGetTripsRequest(strAgencyIdAndRouteId);
		} catch (TpException e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(e);			
		} catch (Exception e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		String res =  JSONUtil.getResponseJSON(response);
		return res;
	}

	/**
	 * Handle get trips request.
	 *
	 * @param strAgencyIdAndRouteId the str agency id and route id
	 * @return the string
	 */
	private String handleGetTripsRequest(String strAgencyIdAndRouteId) {
		TPResponse response = ResponseUtil.createResponse(TP_CODES.SUCESS);
		try {
			//			long start = System.currentTimeMillis();
			if(ComUtils.isEmptyString(strAgencyIdAndRouteId))
				throw new TpException(TP_CODES.INVALID_REQUEST);
			GtfsDataService gtfsDataService =  BeanUtil.getGtfsDataServiceBean();			
			Map<String, List<String>> map =  gtfsDataService.getTripsByRouteId(strAgencyIdAndRouteId.split(","));
			response.setData(map);
		} catch (TpException e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(e);			
		} catch (Exception e) {
			logger.error(loggerName, e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		String res =  JSONUtil.getResponseJSON(response);
		//		long end = System.currentTimeMillis();		
		//		System.out.println("GetStopTimes took " + (end - start) + "mill sec");
		return res;
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
