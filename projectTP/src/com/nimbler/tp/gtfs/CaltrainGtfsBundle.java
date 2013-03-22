package com.nimbler.tp.gtfs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.HttpUtils;
import com.nimbler.tp.util.TpException;

public class CaltrainGtfsBundle extends GtfsBundle {

	private String pageUrl="http://www.caltrain.com/developer/Developer_License_Agreement_and_Privacy_Policy.html?IsSubmitted=True";
	private String patternString="href=\"(http[s]{0,1}:\\/\\/www.caltrain.com[a-zA-Z0-9-_\\/]{1,100}\\.zip)\"";
	@Autowired
	private LoggingService loggingService;

	private String loggerName = "com.nimbler.tp.gtfs.GtfsDataMonitor";

	private long lastFetch = 0;
	private int cacheIntervalMillSec = 3600000;

	private String parsedDownloadUrl = null;

	@Override
	public String getDownloadUrl() {
		try {
			if(!ComUtils.isEmptyString(parsedDownloadUrl) && ((System.currentTimeMillis()-lastFetch)<cacheIntervalMillSec)){
				return parsedDownloadUrl;
			}
			loggingService.debug(loggerName, "Getting url for caltrain download..");
			String pageText = HttpUtils.requestHttpGet(pageUrl, "*/*",null);
			if(ComUtils.isEmptyString(pageText)){
				loggingService.error(loggerName, "No page text found..");
				return null;
			}
			String url = getUrlFromHtmlPage(pageText);
			loggingService.debug(loggerName, "Caltrain gtfs file download url:"+url);
			if(!ComUtils.isEmptyString(url)){
				lastFetch = System.currentTimeMillis();
				parsedDownloadUrl = url;
			}
			return url;
		} catch (TpException e) {
			loggingService.error(loggerName, e.getMessage());
		} catch (Exception e) {
			loggingService.error(loggerName, e);
		}
		return null;
	}
	public String  getUrlFromHtmlPage(String  pageText){
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher=  pattern.matcher(pageText);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	public String getPageUrl() {
		return pageUrl;
	}
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	public String getPatternString() {
		return patternString;
	}
	public void setPatternString(String patternString) {
		this.patternString = patternString;
	}
	public LoggingService getLoggingService() {
		return loggingService;
	}
	public void setLoggingService(LoggingService loggingService) {
		this.loggingService = loggingService;
	}
	public String getLoggerName() {
		return loggerName;
	}
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	public long getLastFetch() {
		return lastFetch;
	}
	public void setLastFetch(long lastFetch) {
		this.lastFetch = lastFetch;
	}
	public int getCacheIntervalMillSec() {
		return cacheIntervalMillSec;
	}
	public void setCacheIntervalMillSec(int cacheIntervalMillSec) {
		this.cacheIntervalMillSec = cacheIntervalMillSec;
	}
	public String getParsedDownloadUrl() {
		return parsedDownloadUrl;
	}
	public void setParsedDownloadUrl(String parsedDownloadUrl) {
		this.parsedDownloadUrl = parsedDownloadUrl;
	}
	public static void main(String[] args) {
		CaltrainGtfsBundle bundle = new CaltrainGtfsBundle();
		LoggingService loggingService = new LoggingService();
		loggingService.init();
		bundle.setLoggingService(loggingService);
		bundle.setLoggerName("console");
		System.out.println(bundle.getDownloadUrl());
		System.out.println(bundle.getDownloadUrl());
	}
}

