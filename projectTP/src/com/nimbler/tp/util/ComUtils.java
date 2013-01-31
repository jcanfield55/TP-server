/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd.
 * All rights reserved.
 *
 */
package com.nimbler.tp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;


/**
 * The Class ComUtils.
 *
 * @author nirmal
 */
public class ComUtils {

	/**
	 * Checks if is empty string.
	 *
	 * @param str the str
	 * @return true, if is empty string
	 */
	public static boolean isEmptyString(String str){
		if(str==null || "".equals(str.trim()) || str.trim().length()==0){
			return true;
		}
		return false;
	}

	/**
	 * Checks if is empty list.
	 *
	 * @param lst the lst
	 * @return true, if is empty list
	 */
	public static boolean isEmptyList(List lst) {
		if(lst==null)
			return true;
		if(lst.size()>0)
			return false;
		return true;
	}

	/**
	 * Checks if is valid op code.
	 *
	 * @param opcode the opcode
	 * @param clazz the clazz
	 * @return true, if is valid op code
	 */
	@SuppressWarnings("unchecked")
	public static boolean isValidOpCode(String opcode, Class clazz) {
		boolean sucess = false;
		if(!ComUtils.isEmptyString(opcode)){
			try {
				Enum.valueOf(clazz,opcode);
				sucess = true;
			} catch (IllegalArgumentException e) {
				System.err.println("Error in ComUtils.isValidOpCode: "+e.getMessage());
			}
		}
		return sucess;
	}


	/**
	 * Format date.
	 *
	 * @param time the time
	 * @return the string
	 */
	public static String formatDate(long time){
		SimpleDateFormat formatter = new SimpleDateFormat(TpConstants.DEFAULT_DATE_FORMAT_PATTERN);
		return formatter.format(new Date(time));
	}

	/**
	 * Adds the time to date.
	 *
	 * @param date the date
	 * @param strTime the str time
	 * @return the date
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	private static Date addTimeToDate(Date date,String strTime) throws IllegalArgumentException{
		String[] arrTime = strTime.split(":");
		if(arrTime.length!=4)
			throw new IllegalArgumentException("argument must contain four ':' separated value");
		int month = Integer.valueOf(arrTime[0]);
		int day = Integer.valueOf(arrTime[1]);
		int hr = Integer.valueOf(arrTime[2]);
		int min = Integer.valueOf(arrTime[3]);

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, month);
		c.add(Calendar.DAY_OF_MONTH, day);
		c.add(Calendar.HOUR_OF_DAY, hr);
		c.add(Calendar.MINUTE, min);
		return c.getTime();
	}

	/**
	 * Adds the time current to date.
	 *
	 * @param strTime the str time
	 * @return the date
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	private static Date addTimeCurrentToDate(String strTime) throws IllegalArgumentException{
		return addTimeToDate(new Date(), strTime);
	}


	/**
	 * Gets the date to string.
	 *
	 * @param date the date
	 * @param dateFmtPattern the date fmt pattern
	 * @return the date to string
	 */
	public static String getDateToString(Date date,String dateFmtPattern) {
		SimpleDateFormat format = new SimpleDateFormat(dateFmtPattern);
		String strDate = format.format(date);
		return (strDate != null) ? strDate : null;
	}

	/**
	 * Gets the string to date.
	 *
	 * @param strDate the str date
	 * @param dateFmtPattern the date fmt pattern
	 * @return the string to date
	 */
	public static Date getStringToDate(String strDate,String dateFmtPattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFmtPattern);
		Date date = null;
		try {
			date = sdf.parse(strDate);
		} catch (ParseException e) {
		}
		return (date != null) ? date : null;
	}

	/**
	 * Parses the multipart request.
	 *
	 * @param request the request
	 * @param map the map
	 * @param lst the lst
	 * @param filePattern the file pattern
	 * @throws Exception the exception
	 */
	public static void parseMultipartRequest(HttpServletRequest request,Map<String,String> map,List<File> lst, String filePattern) throws Exception {
		if(filePattern==null)
			filePattern = "%s";
		if (ServletFileUpload.isMultipartContent(request)) {
			DiskFileItemFactory  fileItemFactory = new DiskFileItemFactory();
			ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
			List items = uploadHandler.parseRequest(request);
			Iterator itr = items.iterator();
			while(itr.hasNext()) {
				FileItem item = (FileItem) itr.next();
				if(item.isFormField()) {
					map.put(item.getFieldName(), item.getString());
				} else if(lst!=null){
					File file = new File(TpConstants.FILE_REPOSITORY,String.format(filePattern, item.getName()));
					item.write(file);
					lst.add(file);
				}
			}
		}
	}
	public static Map parseMultipartRequest(HttpServletRequest request) throws Exception {
		Map map = null;
		if (ServletFileUpload.isMultipartContent(request)) {
			map = new HashMap();
			DiskFileItemFactory  fileItemFactory = new DiskFileItemFactory();
			ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
			List items = uploadHandler.parseRequest(request);
			Iterator itr = items.iterator();
			while(itr.hasNext()) {
				FileItem item = (FileItem) itr.next();
				if(item.isFormField()) {
					map.put(item.getFieldName(), item.getString());
				}
			}
		}
		return map;
	}
	/**
	 * Gets the file names from files.
	 *
	 * @param lstFile the lst file
	 * @return the file names from files
	 */
	public static List<String> getFileNamesFromFiles(List<File> lstFile) {
		if(ComUtils.isEmptyList(lstFile))
			return null;
		List<String> lstRes = new ArrayList<String>();
		for (File file : lstFile){
			lstRes.add(file.getAbsolutePath());
		}
		return lstRes;
	}

	/**
	 * 
	 * @param startDate
	 * @param endDate
	 * @param from
	 * @param to
	 * @param feedbackTime
	 * @param source
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static void readHtmlTemplet(){
		try {
			File file = new File(ComUtils.class.getClassLoader().getResource(TpConstants.OTP_HTML_FILE_PATH).toURI());
			TpConstants.OTP_HTML_STRING = FileUtils.readFileToString(file, "UTF-8");

			file = new File(ComUtils.class.getClassLoader().getResource(TpConstants.GTFS_COMPARE_FILE_PATH).toURI());
			TpConstants.GTFS_COMPARE_HTML_STRING = FileUtils.readFileToString(file, "UTF-8");

			file = new File(ComUtils.class.getClassLoader().getResource(TpConstants.OTP_HTML_SUMMERY_FILE_PATH).toURI());
			TpConstants.GTFS_COMPARE_HTML_SUMMERY_STRING = FileUtils.readFileToString(file, "UTF-8");

			file = new File(ComUtils.class.getClassLoader().getResource(TpConstants.GRAPH_TEST_RESULT_HTML_FILE).toURI());
			TpConstants.GRAPH_TEST_RESULT_HTML_STRING = FileUtils.readFileToString(file, "UTF-8");

			file = new File(ComUtils.class.getClassLoader().getResource(TpConstants.GRAPH_TEST_SUMMERY_HTML_FILE).toURI());
			TpConstants.GRAPH_TEST_SUMMERY_HTML_STRING = FileUtils.readFileToString(file, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param strTrip
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 * @deprecated
	 * 
	 */
	public static String[] getXmlData(String strTrip){
		String[] otpInfo = new String[4];
		try {
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new StringReader(strTrip));
			Element beans = document.getRootElement();
			if(XPath.selectSingleNode(beans, TpConstants.PLAN_FROM_PATH) !=null){
				otpInfo[0] = ((Element)XPath.selectSingleNode(beans, TpConstants.PLAN_FROM_PATH)).getValue();
			}
			if(XPath.selectSingleNode(beans, TpConstants.PLAN_TO_PATH) !=null){
				otpInfo[1] = ((Element)XPath.selectSingleNode(beans, TpConstants.PLAN_TO_PATH)).getValue();
			}
			if(XPath.selectSingleNode(beans, TpConstants.PLAN_START_TIME_PATH) !=null){
				otpInfo[2] = ((Element)XPath.selectSingleNode(beans, TpConstants.PLAN_START_TIME_PATH)).getValue();
			}
			if(XPath.selectSingleNode(beans, TpConstants.PLAN_END_TIME_PATH) !=null){
				otpInfo[3] = ((Element)XPath.selectSingleNode(beans, TpConstants.PLAN_END_TIME_PATH)).getValue();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			//			e.printStackTrace();
		}
		return otpInfo;
	}

	/**
	 * 
	 * @return
	 * @author suresh
	 */
	public static String getFormatedDate(String format) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
	/**
	 * 
	 * @param date
	 * @return
	 * @author suresh
	 */
	public static long convertIntoTime(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		long res = 0;
		try {
			Date time = sdf.parse(date);
			res= time.getTime();
		} catch (ParseException e) {
			System.err.println("Error in to convert Time in milliseconds: "+e.getMessage());
		}
		return res;
	}
	/**
	 * 
	 * @param date
	 * @return
	 * @throws TpException
	 * @author suresh
	 */
	public static Date convertIntoDate(String date) throws TpException {
		SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");
		Date time =null;
		try {
			time =(Date)sdf.parse(date);
		} catch (ParseException e) {
			System.err.println("Error in to convert into Date : "+e.getMessage());
		}
		return time;
	}
	/**
	 * Gets the download file.
	 *
	 * @param downloadFile the download file
	 * @param url the url
	 * @return the download file
	 * @throws MalformedURLException the malformed url exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void getDownloadFile(File downloadFile, String strUrl) throws MalformedURLException, IOException {
		InputStream in = null;
		FileOutputStream fout = null;
		try{
			HttpURLConnection url  = (HttpURLConnection) new URL(strUrl).openConnection();
			url.setConnectTimeout(15000);
			in = url.getInputStream();
			fout = new FileOutputStream(downloadFile);
			IOUtils.copy(in, fout);
		}finally{
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(fout);
		}
	}

	/**
	 * Sleep.
	 *
	 * @param millSec the mill sec
	 */
	public static void sleep(long millSec) {
		try {
			Thread.sleep(millSec);
		} catch (InterruptedException e) {}
	}

	/**
	 * Gets the week start time in millis.
	 *
	 * @param time the time
	 * @param timzone the timzone
	 * @return the week start time in millis
	 */
	public static long getWeekStartTimeInMillis(long time, String timzone) {
		Calendar calendar = null;
		if(!isEmptyString(timzone))
			calendar = Calendar.getInstance(TimeZone.getTimeZone(timzone));
		else
			calendar = Calendar.getInstance();

		calendar.setTimeInMillis(time);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTimeInMillis();
	}

	/**
	 * Gets the week end time in millis.
	 *
	 * @param time the time
	 * @param timzone the timzone
	 * @return the week end time in millis
	 */
	public static long getWeekEndTimeInMillis(long time, String timzone) {
		Calendar calendar = null;
		if(!isEmptyString(timzone))
			calendar = Calendar.getInstance(TimeZone.getTimeZone(timzone));
		else
			calendar = Calendar.getInstance();

		calendar.setTimeInMillis(time);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		return calendar.getTimeInMillis();
	}

	/**
	 * Removes the quotation.
	 *
	 * @param arr the arr
	 */
	public static void removeQuotation(String[] arr){
		if(arr==null)
			return;
		for (int i = 0; i < arr.length; i++) {
			if(arr[i]!=null){
				if(arr[i].startsWith("\""))
					arr[i] = arr[i].substring(1);
				if(arr[i].endsWith("\""))
					arr[i] = arr[i].substring(0,arr[i].length()-1);
			}
		}
	}

	/**
	 * Gets the list from array.
	 *
	 * @param strings the strings
	 * @return the list from array
	 */
	public static  List<String> getListFromArray(String[] strings) {
		if(strings==null)
			return null;
		List<String> lst = new ArrayList<String>();
		for (String type : strings) {
			lst.add(type);
		}
		return lst;
	}
	public static void main(String[] args) {
		//System.out.println(isWeekEnd());
		try {
			DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
			Date tripDate = format.parse("11/28/2012");


			System.out.println(DateUtils.isSameDay(new Date(), tripDate));
		} catch (Exception es) {
			es.printStackTrace();
		}
	}

	/**
	 * Gets the today date.
	 *
	 * @param startTime the start time
	 * @return the today date
	 */
	public static long getTodayDateTime(long startTime) {
		Calendar legTime = Calendar.getInstance();
		legTime.setTimeInMillis(startTime);
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, legTime.get(Calendar.HOUR_OF_DAY));
		now.set(Calendar.MINUTE, legTime.get(Calendar.MINUTE));
		now.set(Calendar.SECOND, legTime.get(Calendar.SECOND));
		now.set(Calendar.MILLISECOND, legTime.get(Calendar.MILLISECOND));
		return now.getTimeInMillis();
	}
	/**
	 * Crrently we have one to one mapping for app and agency.
	 * @param appType
	 * @return
	 *//*
	public static int[] getAgenciesForApp(Integer appType) {
		//handle special case for current caltrain app with no appType parameter
		if (appType == null)
			return new int[]{AGENCY_TYPE.CALTRAIN.ordinal()};

		NIMBLER_APP_TYPE type = NIMBLER_APP_TYPE.values()[appType];
		switch (type) {
		case CALTRAIN:
			return new int[]{AGENCY_TYPE.CALTRAIN.ordinal()};
		case BART:
			return new int[]{AGENCY_TYPE.BART.ordinal()};
		default:
			return new int[]{AGENCY_TYPE.CALTRAIN.ordinal()};
		}
	}*/
	/**
	 * Crrently we have one to one mapping for app and agency.
	 * @param agencyType
	 * @return
	 */
	public static Integer[] getAppsSupportingAgency(Integer agencyType) {
		AGENCY_TYPE type = AGENCY_TYPE.values()[agencyType];
		switch (type) {
		case CALTRAIN:
			return new Integer[]{NIMBLER_APP_TYPE.CALTRAIN.ordinal()};
		case BART:
			return new Integer[]{NIMBLER_APP_TYPE.BART.ordinal()};
		default:
			return new Integer[]{NIMBLER_APP_TYPE.CALTRAIN.ordinal()};
		}
	}

	/**
	 * Split to int array.
	 *
	 * @param values the values
	 * @return the integer[]
	 */
	public static Integer[] splitToIntArray(String values) {
		if(values == null)
			return null;
		String[] vals = values.split(",");
		Integer[] integers = new Integer[vals.length];
		for (int i = 0; i < vals.length; i++) {
			integers[i] = NumberUtils.toInt(vals[i]);
		}
		return integers;
	}
	public static boolean isWeekEnd(TimeZone timeZone){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(timeZone);
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		return (day==1 || day==7);
	}
	public static boolean isFileExist(String file) {
		return new File(file).exists();
	}
}