/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import org.apache.commons.io.FilenameUtils;
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
public class GtfsDataMonitor {

	@Autowired
	private LoggingService logger;
	@Autowired
	private MailService mailService;
	private String loggerName;
	private String downloadDirectory;
	List<GtfsBundle> gtfsBundles;

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
			mailService.sendMail(TpConstants.FEEDBACK_EMAIL_ID,TpConstants.GTFS_DATA_COMPARE_MAIL_SUBJECT,strSummery,true,lstAttachement,lstAttachement);
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
	private GtfsMonitorResult checkSingleGtfs(GtfsBundle gtfsBundle){
		GtfsMonitorResult result = new GtfsMonitorResult(gtfsBundle);
		String agency = gtfsBundle.getDefaultAgencyId();
		String currentFile = gtfsBundle.getCurrentDataFile();
		try {
			logger.debug(loggerName, "Check start for "+agency+", current file:"+gtfsBundle.getCurrentDataFile());
			String fileName = FilenameUtils.getName(currentFile);
			File downloadFile = new File(downloadDirectory+fileName);
			if(downloadFile.exists()){
				boolean res = downloadFile.delete();
				logger.debug(loggerName, "Old file "+downloadFile.getAbsolutePath()+" delete: "+res);
			}
			if(!new File(currentFile).exists())
				throw new TpException("No currunt GTFS file found for "+agency+" to compare");
			logger.info(loggerName, "Downloading: "+agency+", url:"+gtfsBundle.getDownloadUrl());
			ComUtils.getDownloadFile(downloadFile,gtfsBundle.getDownloadUrl());
			logger.debug(loggerName, "Download complete");
			Map<String, Date> newMap = GtfsUtils.getExpireDateFromGtfs(downloadFile);
			Map<String, Date> oldMap = GtfsUtils.getExpireDateFromGtfs(currentFile);
			result.setOldData(oldMap);
			result.setNewData(newMap);
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
