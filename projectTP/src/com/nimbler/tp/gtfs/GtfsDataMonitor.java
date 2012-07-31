/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.smtp.MailService;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.GtfsUtils;
import com.nimbler.tp.util.HtmlUtil;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpException;


/**
 * 
 * @author nirmal
 *
 */
@SuppressWarnings("unchecked")
public class GtfsDataMonitor {
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	@Autowired
	private LoggingService logger;
	@Autowired
	private MailService mailService;
	private String loggerName;
	private String downloadDirectory;
	List<GtfsBundle> gtfsBundles;
	// used for debug only, must true on production
	private static final boolean enableNewDownload = true;

	public GtfsDataMonitor() {
	}

	/**
	 * Check gtfs.
	 */
	public void checkGtfs() {
		try {
			if(ComUtils.isEmptyList(gtfsBundles))
				throw new TpException("No bundle found");
			logger.debug(loggerName, "Starting Monitoring for "+gtfsBundles.size()+" bundles");
			List<GtfsMonitorResult> lstMonitorResults = new ArrayList<GtfsMonitorResult>();			
			for (GtfsBundle gtfsBundle : gtfsBundles){ 
				GtfsMonitorResult result = checkSingleGtfs(gtfsBundle);
				GtfsUtils.meargeMonitorResult(result);
				GtfsUtils.setGtfsSummery(result);
				lstMonitorResults.add(result);								
			}			
			for (GtfsMonitorResult gtfsMonitorResult : lstMonitorResults) {
				logger.info(loggerName, gtfsMonitorResult.getGtfsSummury()+"");
			}
			List<String> lstAttachement = HtmlUtil.getResultTable(lstMonitorResults);
			String strSummery = HtmlUtil.getResultSummeryTable(lstMonitorResults);
			mailService.sendMail(TpConstants.GTFS_DATA_COMPARE_MAIL_ID,TpConstants.GTFS_DATA_COMPARE_MAIL_SUBJECT,strSummery,true,lstAttachement,lstAttachement);
		}catch (TpException e) {
			logger.info(loggerName, e.getErrMsg());
		}catch (Exception e) {
			logger.error(loggerName, e);
		}
	}

	/**
	 * Check single gtfs.
	 *
	 * @param gtfsBundle the gtfs bundle
	 * @return 
	 * @throws TpException 
	 */
	@SuppressWarnings("unused")
	private GtfsMonitorResult checkSingleGtfs(GtfsBundle gtfsBundle){
		GtfsMonitorResult result = new GtfsMonitorResult(gtfsBundle);
		String agency = gtfsBundle.getDefaultAgencyId();
		String currentFile = gtfsBundle.getCurrentDataFile();
		try {
			logger.debug(loggerName, "Check start for "+agency+", current file:"+gtfsBundle.getCurrentDataFile());
			String fileName = FilenameUtils.getName(currentFile);
			File downloadFile = new File(downloadDirectory+fileName);
			if(downloadFile.exists() && enableNewDownload){
				boolean res = downloadFile.delete();
				logger.debug(loggerName, "Old file "+downloadFile.getAbsolutePath()+" delete: "+res);
			}
			if(!new File(currentFile).exists())
				throw new TpException("No currunt GTFS file found for "+agency+" to compare");
			if(enableNewDownload){
				logger.info(loggerName, "Downloading: "+agency+", url:"+gtfsBundle.getDownloadUrl());
				ComUtils.getDownloadFile(downloadFile,gtfsBundle.getDownloadUrl());
				logger.debug(loggerName, "Download complete");
			}else
				logger.info(loggerName, "Skipping download: "+agency+", url:"+gtfsBundle.getDownloadUrl());
			Map<String, Date> newMap = getExpireDateFromGtfs(downloadFile);
			Map<String, Date> oldMap = getExpireDateFromGtfs(new File(currentFile));
			Map<String, Date> crackedMap = null;
			if(!ComUtils.isEmptyString(gtfsBundle.getCrackedDataFile()))
				crackedMap = getExpireDateFromGtfs(new File(gtfsBundle.getCrackedDataFile()));			
			result.setOldData(oldMap);
			result.setNewData(newMap);
			result.setCrackedData(crackedMap);
			logger.info(loggerName, "File check done for "+agency+", Old service count: "+oldMap.size()+",new service count: "+newMap.size());
		}catch (TpException e) {
			result.setError("Error while checking gtfs file from "+agency+" : "+e.getMessage());
			logger.error(loggerName,result.getError());
		}catch (ZipException e) {
			result.setError("Error[ZipException] while checking gtfs file from "+agency+" : "+e.getMessage());
			logger.error(loggerName,result.getError());
		}catch (UnknownHostException e) {
			result.setError("Error[UnknownHostException] while checking gtfs file from "+agency+" : "+e.getMessage());
			logger.error(loggerName,result.getError());
		}catch (NullPointerException e) {
			result.setError("NullPointerException while checking gtfs file from "+agency+" : "+e.getMessage());
			logger.error(loggerName,e);
		}catch (ArrayIndexOutOfBoundsException e) {
			result.setError("ArrayIndexOutOfBoundsException while checking gtfs file from "+agency+" : "+e.getMessage());
			logger.error(loggerName,result.getError());
		}catch (SocketException e) {
			result.setError("Error[SocketException] while checking gtfs file from "+agency+" : "+e.getMessage());
			logger.error(loggerName,result.getError());
		}catch (Exception e) {
			result.setError("Error while checking gtfs file from "+agency+" : "+e.getMessage());
			logger.error(loggerName,e);
		}
		return result;

	}
	class GtfsMonitorTask{
	}

	/**
	 * Gets the expire date from gtfs.
	 *
	 * @param file the file
	 * @return the expire date from gtfs
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParseException the parse exception
	 * @throws TpException the tp exception
	 */
	public Map<String, Date> getExpireDateFromGtfs(File file) throws IOException, ParseException, TpException {
		ZipFile zipFile = new ZipFile(file);
		ZipEntry zipEntry = zipFile.getEntry(TpConstants.ZIP_CALENDAR_FILE);		
		List<String> lstLines = IOUtils.readLines(zipFile.getInputStream(zipEntry),"ISO-8859-1");
		Map<String, Date> mapServiceIdAndDate = new HashMap<String, Date>();
		if(ComUtils.isEmptyList(lstLines))
			throw new TpException("Invalid file, No data found in file: "+file);
		String[] headers = lstLines.get(0).split(",");
		int END_DATE_INDEX = -1;
		int SERVICE_ID_INDEX = -1;
		for (int i = 0; i < headers.length; i++) {
			if(headers[i].toLowerCase().indexOf("service_id")!=-1)
				SERVICE_ID_INDEX = i;
			else if (headers[i].toLowerCase().indexOf("end_date")!=-1)
				END_DATE_INDEX =i;
		}
		if(END_DATE_INDEX==-1 || SERVICE_ID_INDEX==-1)
			throw new TpException("No service_id or end_date found in data for file:"+file);
		for (int i = 1; i < lstLines.size(); i++) {					
			try {
				String[] line = lstLines.get(i).split(",");
				if(line==null || SERVICE_ID_INDEX > (line.length-1) || END_DATE_INDEX > (line.length-1)){
					logger.warn(loggerName, "empty line found in gtfs");
					continue;					
				}
				String key =line[SERVICE_ID_INDEX];
				String val = line[END_DATE_INDEX];
				if(ComUtils.isEmptyString(key) || ComUtils.isEmptyString(val));
				mapServiceIdAndDate.put(key, dateFormat.parse(StringUtils.remove(val, "\"")));
			} catch (Exception e) {
				logger.error(loggerName,"Malformed data Found at line "+i+", data: "+lstLines.get(i));				
			}
		}
		return mapServiceIdAndDate;
	}
	public String getDownloadDirectory() {
		return downloadDirectory;
	}
	public void setDownloadDirectory(String downloadDirectory) {
		this.downloadDirectory = downloadDirectory;
	}
	public List<GtfsBundle> getGtfsBundles() {
		return gtfsBundles;
	}
	public void setGtfsBundles(List<GtfsBundle> gtfsBundles) {
		this.gtfsBundles = gtfsBundles;
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

	public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}
}
