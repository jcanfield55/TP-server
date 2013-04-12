/*
 * @author nirmal
 */
package com.nimbler.tp.service.flurry;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.flurry.FlurryEventResponse;
import com.nimbler.tp.dataobject.flurry.FlurryReport;
import com.nimbler.tp.dataobject.flurry.FlurrySessionEventData;
import com.nimbler.tp.dbobject.FlurryReportStatus;
import com.nimbler.tp.dbobject.FlurryReportStatus.FLURRY_EVENT_CALL_STATUS;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.ReportException;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
import com.nimbler.tp.util.TpException;
/**
 * 
 * @author nirmal
 *
 */
public class FlurryManagementService {
	@Autowired
	private LoggingService logger; 
	@Autowired
	private PersistenceService persistenceService;
	private int hours = -24;

	private String loggerName = FlurryManagementService.class.getName();//com.nimbler.tp.service.flurry.FlurryManagementService

	private String apiAccessCode="J7J5ND3DB96MXCZV7RHY";
	private String apiKey="WWV2WN4JMY35D4GYCPDJ";
	private String getEventUrl = "http://api.flurry.com/rawData/Events";
	private String getReportUrl = "http://api.flurry.com/rawData/getReport";
	private boolean enableService = true;

	@PostConstruct
	private void init() {
		logger.debug(loggerName, "Service Started....");
	}
	/**
	 * Request daily flurry report.
	 */
	public void requestDailyFlurryReport() {
		logger.debug(loggerName, "request start....");		
		long endTime  = System.currentTimeMillis();
		long startTime  = DateUtils.addHours(new Date(endTime), hours).getTime();
		//		long startTime  = DateUtils.addMinutes(new Date(endTime), -10).getTime();
		requestDailyFlurryReport(startTime,endTime);
	}

	/**
	 * Request daily flurry report.
	 *
	 * @param startTime the start time
	 * @param endTime the end time
	 */
	@SuppressWarnings({ "cast", "unchecked" })
	public void retryRequestDailyFlurryReport() {
		try {
			List<FlurryReportStatus> lstFlurryReportStatus = null;  
			lstFlurryReportStatus = (List<FlurryReportStatus>) persistenceService.findByInFlurry(
					MONGO_TABLES.flurry_report_status.name(), 
					"status", new Integer[]{ FLURRY_EVENT_CALL_STATUS.REQUEST_FAILED.ordinal(),FLURRY_EVENT_CALL_STATUS.GENERATE_FAILED.ordinal()},
					"tryCount",3,FlurryReportStatus.class);

			if(ComUtils.isEmptyList(lstFlurryReportStatus)){
				logger.debug(loggerName, "No panding failed request found to request...");
				return;
			}
			for (FlurryReportStatus status : lstFlurryReportStatus) {
				try {
					String data =  null;
					data = requestHttpGet(status.getUrl(), "Application/json", null);
					if(ComUtils.isEmptyString(data)){
						logger.error(loggerName, "No response found, request failed for flurry");
						status.setStatus(FLURRY_EVENT_CALL_STATUS.REQUEST_FAILED.ordinal());
						return;
					}
					logger.debug(loggerName, data);
					FlurryEventResponse res = (FlurryEventResponse) JSONUtil.getObjFromJson(data, FlurryEventResponse.class);
					status.setEventResponse(res);
					if(res.getCode()!=null || res.getReportUrl()==null){
						status.setStatus(FLURRY_EVENT_CALL_STATUS.REQUEST_FAILED.ordinal());
						status.setStatusText(res.getMessage());
						logger.warn(loggerName, res.toString());
						status.incTryCount();
						return;
					}
					status.setStatus(FLURRY_EVENT_CALL_STATUS.REQUEST_SUCCESS.ordinal());
				} catch (TpException e) {
					logger.warn(loggerName, e.getMessage());
					status.setStatus(FLURRY_EVENT_CALL_STATUS.REQUEST_FAILED.ordinal());
					status.setStatusText(e.getMessage());
					saveStatus(status);
				} catch (Exception e) {			
					logger.error(loggerName, e);
					status.setStatus(FLURRY_EVENT_CALL_STATUS.REQUEST_FAILED.ordinal());
					status.setStatusText(e.getClass()+":"+e.getMessage());
				}finally{
					status.incTryCount();
					saveStatus(status);
				}
			}
		} catch (Exception e1) {
			logger.error(loggerName, e1);
		}
	}
	public void requestDailyFlurryReport(long startTime,long endTime) {
		FlurryReportStatus status = FlurryReportStatus.cerate();;
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("apiAccessCode", apiAccessCode);
			params.put("apiKey", apiKey);
			params.put("startTime", ""+startTime);
			params.put("endTime", endTime+"");
			String url = ComUtils.createUrlWithParams(getEventUrl, params);
			logger.debug(loggerName, "URL: "+url);
			status.setUrl(url);
			String data =  null;
			data = requestHttpGet(getEventUrl, "Application/json", params);
			if(ComUtils.isEmptyString(data)){
				logger.error(loggerName, "No response found, request failed for flurry");
				status.setStatus(FLURRY_EVENT_CALL_STATUS.REQUEST_FAILED.ordinal());
				return;
			}
			logger.debug(loggerName, data);
			FlurryEventResponse res = (FlurryEventResponse) JSONUtil.getObjFromJson(data, FlurryEventResponse.class);
			status.setEventResponse(res);
			if(res.getCode()!=null || res.getReportUrl()==null){
				status.setStatus(FLURRY_EVENT_CALL_STATUS.REQUEST_FAILED.ordinal());
				status.setStatusText(res.getMessage());
				logger.warn(loggerName, res.toString());
				return;
			}
			status.setStatus(FLURRY_EVENT_CALL_STATUS.REQUEST_SUCCESS.ordinal());
		} catch (TpException e) {
			logger.warn(loggerName, e.getMessage());
			status.setStatus(FLURRY_EVENT_CALL_STATUS.REQUEST_FAILED.ordinal());
			status.setStatusText(e.getMessage());
			saveStatus(status);
		} catch (Exception e) {			
			logger.error(loggerName, e);
			status.setStatus(FLURRY_EVENT_CALL_STATUS.REQUEST_FAILED.ordinal());
			status.setStatusText(e.getClass()+":"+e.getMessage());
		}finally{
			saveStatus(status);
		}
	}

	/**
	 * Fetch flurry report.
	 */
	@SuppressWarnings({ "cast", "unchecked" })
	public void fetchFlurryReports() {
		try {
			logger.debug(loggerName, "checking for pending reports....");
			List<FlurryReportStatus> lstFlurryReportStatus =  (List<FlurryReportStatus>) persistenceService.findByIn(MONGO_TABLES.flurry_report_status.name(), 
					"status", new Integer[]{ FLURRY_EVENT_CALL_STATUS.REQUEST_SUCCESS.ordinal(),FLURRY_EVENT_CALL_STATUS.PENDING.ordinal()}, FlurryReportStatus.class);
			if(ComUtils.isEmptyList(lstFlurryReportStatus)){
				logger.debug(loggerName, "No Pending Reports to download...");
				return;
			}
			logger.debug(loggerName, "Reports to fetch..."+lstFlurryReportStatus.size());
			for (FlurryReportStatus status : lstFlurryReportStatus) {
				try {
					FlurryEventResponse res = status.getEventResponse();
					String url = res.getReportUrl().getReportUri();					
					String json= downloadEventData(url,status);
					if(json!=null){
						saveEventsInDatabase(json,status);
					}
				} catch (TpException e) {
					logger.error(loggerName,e.getMessage());
					status.setStatus(FLURRY_EVENT_CALL_STATUS.GENERATE_FAILED.ordinal());
					status.setStatusText("TpException: "+e.getMessage());
				} catch (DBException e) {
					logger.error(loggerName,e);
					status.setStatus(FLURRY_EVENT_CALL_STATUS.GENERATE_FAILED.ordinal());
					status.setStatusText("DBException: "+e.getMessage());
				} catch (Exception e) {
					logger.error(loggerName,e);
				}finally{
					saveStatus(status);					
				}
				break;
			}
		} catch (Exception e) {
			logger.error(loggerName, e);
			e.printStackTrace();
		}
	}

	/**
	 * Save events in database.
	 *
	 * @param json the json
	 * @param status 
	 * @throws TpException 
	 * @throws DBException 
	 */
	private void saveEventsInDatabase(String json, FlurryReportStatus status) throws TpException, DBException {
		logger.debug(loggerName, "saving events in  database.");
		FlurryReport flurryReport = (FlurryReport) JSONUtil.getObjFromJson(json, FlurryReport.class);
		logger.debug(loggerName,"Query Data: "+flurryReport.getQueryData());
		List<FlurrySessionEventData> lstEventDatas = flurryReport.getLstFlurrySessionEvents();
		flurryReport.setLstFlurrySessionEvents(null);
		persistenceService.addObject(MONGO_TABLES.flurry_events_meta.name(), flurryReport);
		status.setEventId(flurryReport.getId());
		if(ComUtils.isEmptyList(lstEventDatas)){
			logger.warn(loggerName, "No Events Found");
			return;
		}
		for (FlurrySessionEventData fEventData : lstEventDatas) {
			fEventData.setMetaData(flurryReport.getId());
		}
		logger.debug(loggerName, "events "+lstEventDatas.size());
		persistenceService.addObjects(MONGO_TABLES.flurry_events.name(), lstEventDatas);

	}
	public void testFlurry(){
		long end = System.currentTimeMillis();
		long start = DateUtils.addHours(new Date(end),-1).getTime();
		//		requestDailyFlurryReport(start, end);
		for (int i = 0; i < 5; i++) {
			System.out.println("pol report..");
			fetchFlurryReports();
			ComUtils.sleep(60000);
		}
	}

	public String requestHttpGet(String strUrl,String accept,Map<String,String> params) throws TpException {
		String res = null;
		for (int i = 0; i < 3; i++) {
			HttpURLConnection connection = null; 
			try {
				String url = strUrl;
				if(params!=null && params.size()>0){
					List<String> lstParams= new ArrayList<String>();
					for (Map.Entry<String, String> entry : params.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue();
						lstParams.add(key+"="+URLEncoder.encode(value));
					}
					url = strUrl+"?"+join(lstParams,"&");
				}
				connection = (HttpURLConnection)new URL(url).openConnection();
				connection.setDoOutput(true);
				connection.setConnectTimeout(60000);
				connection.addRequestProperty("Accept", accept);
				int responseCode = connection.getResponseCode();
				if(responseCode != 200){
					res = IOUtils.toString(connection.getInputStream());
					logger.error(loggerName, "http "+responseCode+" while requesting url :"+url+", response: "+res);
					throw new TpException(res);
				}
				res = IOUtils.toString(connection.getInputStream());
				return res;
			} catch (UnknownHostException e) {
				throw new TpException("UnknownHost: "+e.getMessage());
			} catch (MalformedURLException e) {
				throw new TpException("MalformedURL: "+e.getMessage());
			}catch (IOException e) {
				String retry = "";
				if(i<2)
					retry = "  retrying...";	
				else
					throw new TpException(e.getMessage());
				logger.error(loggerName, e.getMessage()+retry);
				ComUtils.sleep(DateUtils.MILLIS_PER_MINUTE*10);
			}finally{
				if(connection !=null){
					connection.disconnect();
				}
			}
		}
		return null;
	}
	/**
	 * Download file.
	 *
	 * @param url the url
	 * @param status the status
	 * @return true, if successful
	 * @throws ReportException the report exception
	 * @throws TpException the tp exception
	 */
	public String downloadEventData(String url, FlurryReportStatus status) throws  ReportException, TpException {
		HttpURLConnection connection = null; 
		try {
			logger.debug(loggerName, "downloading from url: "+url);
			connection = (HttpURLConnection)new URL(url).openConnection();
			connection.setDoOutput(true);
			int responseCode = connection.getResponseCode();
			if(responseCode != 200){				
				try {
					String res = IOUtils.toString(connection.getInputStream());
					logger.debug(loggerName, res);
					status.setStatusText(res);
				} catch (IOException e) {
					logger.error(loggerName, e.getMessage());
					status.setStatusText("IOException: "+e.getMessage());
				} catch (Exception e) {
					logger.error(loggerName, e);
					status.setStatusText("Exception: "+e.getMessage());
				}
				throw new TpException(status.getStatusText());
			}
			String contentType = connection.getHeaderField("Content-Type");			
			if(equalsIgnoreCase(contentType, "application/octet-stream")){
				logger.debug(loggerName, "octet-stream...");
				GZIPInputStream gzipInputStream = new GZIPInputStream(connection.getInputStream());
				String jsonstringRes = IOUtils.toString(gzipInputStream); 
				status.setStatus(FLURRY_EVENT_CALL_STATUS.GENERATE_SUCCESS.ordinal());
				return jsonstringRes;
			}else if(equalsIgnoreCase(contentType, "application/json")){				
				String res = IOUtils.toString(connection.getInputStream());
				status.setStatus(FLURRY_EVENT_CALL_STATUS.PENDING.ordinal());
				logger.debug(loggerName,"json response.."+ res);
				status.setStatusText(res);
				status.setUpdateTime(System.currentTimeMillis());
				return null;
			}else{
				logger.warn(loggerName, "No valid content type found.."+contentType);
			}
		} catch (UnknownHostException e) {
			throw new TpException("UnknownHost: "+e.getMessage());
		} catch (MalformedURLException e) {
			throw new TpException("MalformedURL: "+e.getMessage());
		}catch (IOException e) {
			throw new TpException("IOException: "+e.getMessage());
		}finally{			
			if(connection !=null){
				connection.disconnect();
			}
		}
		return null;
	}

	/**
	 * Add/Overwrite status.
	 *
	 * @param status the status
	 */
	private void saveStatus(FlurryReportStatus status) {
		if(status==null)
			return;
		try {
			persistenceService.addObject(MONGO_TABLES.flurry_report_status.name(),status);
		} catch (DBException e) {			
			logger.error(loggerName, e);
		}		

	}
	public LoggingService getLogger() {
		return logger;
	}
	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}
	public PersistenceService getPersistenceService() {
		return persistenceService;
	}
	public void setPersistenceService(PersistenceService persistenceService) {
		this.persistenceService = persistenceService;
	}
	public String getLoggerName() {
		return loggerName;
	}
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}


	public int getHours() {
		return hours;
	}

	public String getApiAccessCode() {
		return apiAccessCode;
	}

	public void setApiAccessCode(String apiAccessCode) {
		this.apiAccessCode = apiAccessCode;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getGetEventUrl() {
		return getEventUrl;
	}
	public void setGetEventUrl(String getEventUrl) {
		this.getEventUrl = getEventUrl;
	}

	public String getGetReportUrl() {
		return getReportUrl;
	}
	public void setGetReportUrl(String getReportUrl) {
		this.getReportUrl = getReportUrl;
	}
	public void setHours(int hours) {
		this.hours = hours;
	}
	public boolean isEnableService() {
		return enableService;
	}
	public void setEnableService(boolean enableService) {
		this.enableService = enableService;
	}


}
