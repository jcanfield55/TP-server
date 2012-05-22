/**
 * 
 */
package com.apprika.otp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import com.apprika.otp.dbobject.FeedBack.SOURCE;


/**
 * The Class ComUtils.
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
				} else {
					File file = new File(TpConstants.FILE_REPOSITORY,String.format(filePattern, item.getName()));
					item.write(file);
					lst.add(file);
				}
			}
		}
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
			FileReader fileReader = new FileReader(file);
			BufferedReader bf = new BufferedReader(fileReader);
			String line;
			StringBuilder sb = new StringBuilder();
			while((line=bf.readLine())!=null){
				sb.append(line);
			}
			TpConstants.OTP_HTML_STRING = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	/**
	 * To replce OTP html value
	 * @param source 
	 * @param from
	 * @param to
	 * @param startDate
	 * @param endDate
	 * @param feedbackTime
	 * @param source
	 * @param textFeedback 
	 * @return
	 */
	public static String getHtmlTemplet(String strTrip, int source, String textFeedback){
		String tripInfo[] = ComUtils.getXmlData(strTrip.replace("&", "&amp;"));
		String strSource = "-";
		if(source <= SOURCE.values().length)
			strSource = SOURCE.values()[source].name();

		String otpFeedback =TpConstants.OTP_HTML_STRING
				.replace("--from--", StringUtils.defaultIfBlank(tripInfo[0], "-"))
				.replace("--to--", StringUtils.defaultIfBlank(tripInfo[1], "-"))
				.replace("--startdate--", StringUtils.defaultIfBlank(tripInfo[2], "-"))
				.replace("--enddate--", StringUtils.defaultIfBlank(tripInfo[3], "-"))
				.replace("--feedbacktime--", new Date()+"")
				.replace("--txtfb--",StringUtils.defaultIfBlank(textFeedback, "-"))
				.replace("--source--", strSource);
		return otpFeedback;
	}
	/**
	 * 
	 * @param strTrip
	 * @return
	 * @throws JDOMException
	 * @throws IOException
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
			e.printStackTrace();
		}
		return otpInfo;
	}	
}
