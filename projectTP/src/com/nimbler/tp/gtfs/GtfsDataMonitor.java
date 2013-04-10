/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
@SuppressWarnings("unchecked")
public class GtfsDataMonitor {

	@Autowired
	private LoggingService logger;
	@Autowired
	private MailService mailService;
	@Autowired
	private GtfsContext gtfsContext;

	private String loggerName;
	private String downloadDirectory;
	List<GtfsBundle> gtfsBundles;

	public GtfsDataMonitor() {

	}

	/**
	 * 
	 */
	public void init() {
		gtfsBundles = gtfsContext.getGtfsBundles();
		readGtfsFiles();
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
	 * Read gtfs files.
	 */
	public void readGtfsFiles() {
		try {
			if(ComUtils.isEmptyList(gtfsBundles))
				throw new TpException("No bundle found");
			ExecutorService service = Executors.newFixedThreadPool(gtfsBundles.size());
			for (GtfsBundle bundle : gtfsBundles){ 
				service.execute(new GtfsMetaExtractor(bundle,logger,loggerName));
			}
			service.shutdown();
			service.awaitTermination(10, TimeUnit.HOURS);
			System.out.println("Gtfs file read complete...........");
		} catch (TpException e) {
			logger.warn(loggerName, e.getErrMsg());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(loggerName, e);
		}
	}
	/**
	 * 
	 */
	public  void queryGtfsService() {
		SimpleDateFormat gtfsDateFormat = new SimpleDateFormat(TpConstants.GTFS_DATE_FORMAT);
		try {
			Map<String, Object> map = new HashMap<String,Object>();
			String strCompareDate = "20120823";
			Date date = gtfsDateFormat.parse(strCompareDate);
			for (GtfsBundle bundle : gtfsBundles){
				List<GtfsCalander> lstCalanders = bundle.getLstCalanders(); 
				for (GtfsCalander cal : lstCalanders) {
					cal.isServiceEnabled(date);
					//TODO 
				}
				List<GtfsCalandeDates> lstCalandeDates = bundle.getLstCalandeDates();
				for (GtfsCalandeDates calandeDates : lstCalandeDates) {
					calandeDates.getDate().equals(strCompareDate);
					//TODO 

				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	public GtfsContext getGtfsContext() {
		return gtfsContext;
	}
	public void setGtfsContext(GtfsContext gtfsContext) {
		this.gtfsContext = gtfsContext;
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
			Map<String, Date> newMap = null;
			if(!new File(currentFile).exists())
				throw new TpException("No currunt GTFS file found for "+agency+" to compare");
			GtfsUtils utils = new GtfsUtils(logger, loggerName);

			logger.info(loggerName, "Downloading: "+agency+", url:"+gtfsBundle.getDownloadUrl());
			try {
				String url = gtfsBundle.getDownloadUrl();
				if(url==null)
					throw new TpException("No Url Found");
				ComUtils.getDownloadFile(downloadFile,url);
				logger.debug(loggerName, "Download complete");
				newMap = utils.getExpireDateFromGtfs(downloadFile);
			} catch (Exception e) {		
				String className=""; 
				if(!(e instanceof TpException))
					className= "["+e.getClass().getSimpleName()+"] "; 
				result.addError(className+e.getMessage());
				logger.error(loggerName, " Error while downloading gtfs file: "+result.getError()+", for "+agency);
				result.ignoreErrorInSummery = true;
			}

			Map<String, Date> oldMap = utils.getExpireDateFromGtfs(new File(currentFile));
			Map<String, Date> crackedMap = null;
			if(!ComUtils.isEmptyString(gtfsBundle.getCrackedDataFile()))
				crackedMap = utils.getExpireDateFromGtfs(new File(gtfsBundle.getCrackedDataFile()));			
			result.setOldData(oldMap);
			result.setNewData(newMap);
			result.setCrackedData(crackedMap);
			logger.info(loggerName, "File check done for "+agency+", Old service count: "+oldMap.size()+",new service count: "+(newMap!=null?""+newMap.size():"0"));
		}catch (TpException e) {
			result.addError("Error while checking gtfs file from "+agency+" : "+e.getMessage());
			result.ignoreErrorInSummery = false;
			logger.error(loggerName,result.getError());
		}catch (ZipException e) {
			result.addError("Error[ZipException] while checking gtfs file from "+agency+" : "+e.getMessage());
			result.ignoreErrorInSummery = false;
			logger.error(loggerName,result.getError());
		}catch (UnknownHostException e) {
			result.addError("Error[UnknownHostException] while checking gtfs file from "+agency+" : "+e.getMessage());
			result.ignoreErrorInSummery = false;
			logger.error(loggerName,result.getError());
		}catch (NullPointerException e) {
			result.addError("NullPointerException while checking gtfs file from "+agency+" : "+e.getMessage());
			result.ignoreErrorInSummery = false;
			logger.error(loggerName,e);
		}catch (ArrayIndexOutOfBoundsException e) {
			result.addError("ArrayIndexOutOfBoundsException while checking gtfs file from "+agency+" : "+e.getMessage());
			result.ignoreErrorInSummery = false;
			logger.error(loggerName,result.getError());
		}catch (SocketException e) {
			result.addError("Error[SocketException] while checking gtfs file from "+agency+" : "+e.getMessage());
			result.ignoreErrorInSummery = false;
			logger.error(loggerName,result.getError());
		}catch (Exception e) {
			result.addError("Error while checking gtfs file from "+agency+" : "+e.getMessage());
			result.ignoreErrorInSummery = false;
			logger.error(loggerName,e);
		}
		return result;

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
