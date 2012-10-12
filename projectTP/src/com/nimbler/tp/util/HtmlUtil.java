/*
 * @author nirmal
 */
package com.nimbler.tp.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.nimbler.tp.dataobject.TripPlan;
import com.nimbler.tp.dbobject.FeedBack;
import com.nimbler.tp.dbobject.FeedBack.FEEDBACK_SOURCE_TYPE;
import com.nimbler.tp.gtfs.GtfsMonitorResult;
import com.nimbler.tp.gtfs.PlanCompareResult;

/**
 * The Class HtmlUtil.
 *
 * @author nirmal
 */
public class HtmlUtil {	
	public static final String DATE_FORMAT_PATTERN = "dd-MMM-yyyy";
	private static final String table="<table width=\"70%\" cellspacing=\"1\" cellpadding=\"1\" border=\"0\" style=\"background: #1879AD; margin-top: 15\">--data--</table>";
	private static final String ROW_COLSPAN_BG = "#E8E8E8";
	/**
	 * Gets the result table.
	 *
	 * @param monitorResult the monitor result
	 * @return the result table
	 * @throws IOException 
	 */
	public static List<String> getResultTable(List<GtfsMonitorResult> lstGtfsMonitorResults) throws IOException{
		StringBuffer body = new StringBuffer();	
		int i=1;
		for (GtfsMonitorResult result : lstGtfsMonitorResults) {			
			StringBuffer sb = new StringBuffer();
			sb.append("<br>");
			String tag = getHeaderLink(result.getGtfsBundle().getDownloadUrl(), result.getGtfsBundle().getDefaultAgencyId(),i,sb);
			addComparisonTable(sb,result,tag);
			sb.append("<br>");
			body.append(sb.toString());
			i++;
		}
		String html = TpConstants.GTFS_COMPARE_HTML_STRING.replace("--data--", body.toString());		
		File htmlFile = new File(SystemUtils.getJavaIoTmpDir().getAbsolutePath()+"/Result_"+DateFormatUtils.format(new Date(), HtmlUtil.DATE_FORMAT_PATTERN)+".html");
		FileUtils.writeByteArrayToFile(htmlFile, html.getBytes());			
		List<String> lstAttachement = Arrays.asList( new String[]{htmlFile.getAbsolutePath()});
		return lstAttachement;
	}

	/**
	 * Gets the result summery table.
	 *
	 * @param lstMonitorResults the lst monitor results
	 * @return the result summery table
	 */
	public static String getResultSummeryTable(List<GtfsMonitorResult> lstMonitorResults) {
		StringBuffer body = new StringBuffer();		
		for (GtfsMonitorResult result : lstMonitorResults) {
			body.append(getSummeryRow(result, "Data is upto date"));
		}
		return TpConstants.GTFS_COMPARE_HTML_SUMMERY_STRING.replace("--data--",  body.toString());
	}

	/**
	 * Adds the comparison table.
	 *
	 * @param sb the sb
	 * @param result the result
	 * @param tag 
	 */
	private static void addComparisonTable(StringBuffer sb,	GtfsMonitorResult result, String tag) {
		StringBuffer sbTable = new StringBuffer();
		sbTable.append(getHeadRow());		
		if(result.getError()!=null && !result.ignoreErrorInSummery){
			sbTable.append("<tr style='color: red; background:"+ROW_COLSPAN_BG+";'><td align='center' colspan='4'>"+result.getError()+"</td></tr>");
		}else{
			List<TableRow> lsTableRows = new ArrayList<TableRow>(result.getMerged().values());
			for (TableRow row : lsTableRows) 
				sbTable.append(getTableRow(row));
		}
		String strTable = table.replace("--data--", sbTable.toString());				
					sb.append("<div id=\""+tag+"\">"+strTable+"</div>");
	}

	/**
	 * Gets the date.
	 *
	 * @param date the date
	 * @return the date
	 */
	private static String getDate(Date date) {		 
		if(date==null)
			return "-";
		else
			return DateFormatUtils.format(date, DATE_FORMAT_PATTERN);
	}

	/**
	 * The Class TableRow.
	 *
	 * @author nirmal
	 */
	public static class TableRow{
		private String service;
		private Date oldDate;
		private Date newDate;
		private Date crackedDate;
		public String getService() {
			return service;
		}

		public void setService(String service) {
			this.service = service;
		}


		public Date getOldDate() {
			return oldDate;
		}


		public void setOldDate(Date oldDate) {
			this.oldDate = oldDate;
		}


		public Date getNewDate() {
			return newDate;
		}


		public void setNewDate(Date newDate) {
			this.newDate = newDate;
		}
		public Date getCrackedDate() {
			return crackedDate;
		}
		public void setCrackedDate(Date crackedDate) {
			this.crackedDate = crackedDate;
		}

		public boolean isCracked(){
			return crackedDate!=null;
		}
		public boolean isNewDataAvailable(){
			boolean res = false;
			if(oldDate!=null && newDate!=null)
				return oldDate.compareTo(newDate)==-1;
			return res;
		}
		public boolean isSameDate(){
			boolean res = false;
			if(oldDate!=null && newDate!=null)
				return oldDate.compareTo(newDate)==0;
			return res;
		}
		public boolean isExpireinSevenDays(){
			boolean res = false;
			if(oldDate!=null){
				long days = (oldDate.getTime()-new Date().getTime())/DateUtils.MILLIS_PER_DAY;
				if(days<7 && days>0)
					return true;
			}
			return res;
		}
		public boolean isOldExpired(){
			boolean res = false;
			if(oldDate!=null )
				return oldDate.compareTo(new Date())==-1;
			return res;
		}
		public boolean isCrackedExpired(){
			boolean res = false;
			if(crackedDate!=null )
				return crackedDate.compareTo(new Date())==-1;
			return res;
		}
	}

	/**
	 * The Class Summury.
	 *
	 * @author nirmal
	 */
	public static class GtfsSummery{
		private String agency;
		private int newService=0;
		private int canceledService=0;
		private int expiredService=0;
		private int updateService=0;
		private int sameData=0;
		private int expInSevenDay=0;
		private int crackedService=0;
		private int expiredCrackedService=0;

		public GtfsSummery() {
		}
		public boolean isChange(){
			if(newService==0 && expInSevenDay==0 && canceledService==0 && expiredService==0 && updateService==0 && crackedService==0 && sameData>0)
				return false;
			else
				return true;
		}
		public GtfsSummery(String agency) {
			this.agency = agency;
		}

		public String getAgency() {
			return agency;
		}
		public int getCanceledService() {
			return canceledService;
		}
		public void setCanceledService(int canceledService) {
			this.canceledService = canceledService;
		}
		public void setAgency(String agency) {
			this.agency = agency;
		}
		public int getNewService() {
			return newService;
		}
		public int getCrackedService() {
			return crackedService;
		}
		public void setCrackedService(int crackedService) {
			this.crackedService = crackedService;
		}
		public int getExpiredCrackedService() {
			return expiredCrackedService;
		}
		public void setExpiredCrackedService(int expiredCrackedService) {
			this.expiredCrackedService = expiredCrackedService;
		}
		public void setNewService(int newService) {
			this.newService = newService;
		}

		public int getExpiredService() {
			return expiredService;
		}

		public int getExpInSevenDay() {
			return expInSevenDay;
		}
		public void setExpInSevenDay(int expInSevenDay) {
			this.expInSevenDay = expInSevenDay;
		}
		public void setExpiredService(int expiredService) {
			this.expiredService = expiredService;
		}
		public int getUpdateService() {
			return updateService;
		}
		public void setUpdateService(int newData) {
			this.updateService = newData;
		}
		public void incNewService() {
			this.newService++;
		}
		public void incSameData() {
			this.sameData++;
		}
		public void incCanceledService() {
			this.canceledService++;
		}
		public void incExpiredService() {
			this.expiredService++;
		}
		public void incCrackedService() {
			this.crackedService++;
		}
		public void incExpiredCrackedService() {
			this.expiredCrackedService++;
		}
		public void incNewData() {
			this.updateService++;
		}
		public void incExpInSevenDay() {
			this.expInSevenDay++;
		}
		public int getSameData() {
			return sameData;
		}
		public void setSameData(int sameData) {
			this.sameData = sameData;
		}
		@Override
		public String toString() {
			return "GtfsSummery [agency=" + String.format("%-20s", agency) + ", newService="
					+ newService + ", canceledService=" + canceledService
					+ ", expiredService=" + expiredService + ", updateService="
					+ updateService + ", sameData=" + sameData
					+ ", expInSevenDay=" + expInSevenDay + "]";
		}
	}	

	/**
	 * Gets the head row.
	 *
	 * @return the head row
	 */
	private static String getHeadRow(){
		return "<tr style=\"color: #fff; text-aling: left;\">" +
				"<td align='left' width=\"40%\"><b>Service<b></td>" +
				"<td align='left'><b>Old Date</b></td>" +
				"<td align='left'><b>New Date<b></td>" +
				"<td align='left'><b>Cracked Date<b></td>" +
				"</tr>";
	}

	/**
	 * Gets the table row.
	 *
	 * @param row the row
	 * @return the table row
	 */
	private static String getTableRow(TableRow row){
		String bg = row.isNewDataAvailable()?" style=\"background-color: #BAF7A0\"":"";
		String red = row.isOldExpired()?"  style=\"color: red;\"":"";
		String orange = row.isExpireinSevenDays()?"  style=\"color: #D08310;\"":"";
		String res =  "" +
				"<tr style=\"color: #000; text-aling: left; background: #fff;\">" +
				"	<td "+bg+"><div>"+row.getService()+"</div></td>" +
				"	<td "+bg+"><div"+StringUtils.defaultString(red, orange)+">"+getDate(row.getOldDate())+"</div></td>" +
				"	<td "+bg+"><div>"+getDate(row.getNewDate())+"</div></td>" +
				"	<td "+bg+"><div>"+getDate(row.getCrackedDate())+"</div></td>" +
				"</tr>";
		return res;
	}

	/**
	 * Gets the header link.
	 *
	 * @param link the link
	 * @param text the text
	 * @param i the i
	 * @param sb 
	 * @return the header link
	 */
	private static String getHeaderLink(String link,String text, int i, StringBuffer sb){		
		String tag = System.nanoTime()+"";		
		sb.append("<div style='font-weight: bolder; color: #1879AD;'>" +
				" <a href=\"#"+tag+"\" style=\"text-decoration:none;\" onclick=\"showHide(this,'"+tag+"'); return false;\">[ - ]</a> " 
				+i+
				".<a style='font-weight: bold; color: #0E5D84;'" +
				"href='"+link+"'> "+text+"</a></div>");
		return tag;

	}
	private static String getHyperLink(String link,String text){
		return "<a style='font-weight: bold; color: #0E5D84;'" +
				"href='"+link+"'> "+text+"</a>";
	}

	/**
	 * Gets the summery row.
	 *
	 * @param summery the summery
	 * @param msg the msg
	 * @return the summery row
	 */
	private static String getSummeryRow(GtfsMonitorResult result,String msg){
		GtfsSummery summery = result.getGtfsSummury();
		String res =  null;
		if (!ComUtils.isEmptyString(result.getError())){
			res =  "" +
					"<tr style=\"color: #000;; background: #E8E8E8;\">" +
					"	<td ><div align=\"left\">"+summery.getAgency()+"</div></td>" +
					"		<td colspan=\"7\" align=\"center\" style=\"background: "+ROW_COLSPAN_BG+"; color: red;\"><div>"+result.getError()+"</div></td>" +
					"</tr>";
		}else if(summery.isChange()){
			res =   "<tr style=\"color: #000; background: #fff;\">" +
					"<td><div align=\"left\">"+summery.getAgency()+"</div></td>" +
					"<td><div align=\"center\">"+summery.getExpiredService()+"</div></td>" +
					"<td><div align=\"center\">"+summery.getNewService()+"</div></td>" +
					"<td><div align=\"center\">"+summery.getCanceledService()+"</div></td>" +
					"<td><div align=\"center\">"+summery.getUpdateService()+"</div></td>" +
					"<td><div align=\"center\">"+summery.getExpInSevenDay()+"</div></td>" +
					"<td><div align=\"center\">"+summery.getCrackedService()+"</div></td>" +
					"<td><div align=\"center\">"+summery.getExpiredCrackedService()+"</div></td>" +
					"</tr>";
		}else{
			res =  "" +
					"<tr style=\"color: #000;; background: #E8E8E8;\">" +
					"	<td ><div align=\"left\">"+summery.getAgency()+"</div></td>" +
					"		<td colspan=\"7\" align=\"center\" style=\"background: "+ROW_COLSPAN_BG+"; color: #0cc712\"><div>"+msg+"</div></td>" +
					"</tr>";
		}
		return res;
	}

	/**
	 * Gets the grapg test result summery table.
	 *
	 * @param sucessCount the sucess count
	 * @param failCount the fail count
	 * @return the grapg test result summery table
	 */
	public static String getGraphTestResultSummeryTable(int sucessCount,int failCount) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("" +
				"<tr style=\"color: #000; background: #fff;\">" +
				"<td><div align=\"left\">Total Test</div></td>" +				
				"<td><div align=\"center\">"+(sucessCount+failCount)+"</div></td>" +
				"</tr>");
		buffer.append("" +
				"<tr style=\"color: #000; background: #fff;\">" +
				"<td><div align=\"left\">Sucess Test</div></td>" +				
				"<td><div align=\"center\">"+sucessCount+"</div></td>" +
				"</tr>");
		buffer.append("" +
				"<tr style=\"color: #000; background: #fff;\">" +
				"<td><div align=\"left\">Failed Test</div></td>" +				
				"<td><div align=\"center\">"+failCount+"</div></td>" +
				"</tr>");
		return TpConstants.GRAPH_TEST_SUMMERY_HTML_STRING.replace("--data--", buffer.toString());

	}

	/**
	 * Gets the graph test detail result.
	 *
	 * @param lstFailResult the lst fail result
	 * @return the graph test detail result
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static List<String> getGraphTestDetailResult(List<PlanCompareResult> lstFailResult) throws IOException {
		StringBuffer buffer = new StringBuffer();
		for (PlanCompareResult res : lstFailResult) {
			buffer.append("" +
					"<tr style=\"color: #000; background: #fff;\">" +
					"<td><div align=\"left\">"+getHyperLink(res.getUrl(), res.getFileName())+"</div></td>" +				
					"<td><div align=\"center\">"+res.getErrorString()+"</div></td>" +
					"</tr>");
		}
		File htmlFile = new File(SystemUtils.getJavaIoTmpDir().getAbsolutePath()+"/GraphTest_"+DateFormatUtils.format(new Date(), HtmlUtil.DATE_FORMAT_PATTERN)+".html");
		FileUtils.writeByteArrayToFile(htmlFile, TpConstants.GRAPH_TEST_RESULT_HTML_STRING.replace("--data--", buffer.toString()).getBytes());			
		List<String> lstAttachement = Arrays.asList( new String[]{htmlFile.getAbsolutePath()});
		return lstAttachement;
	}

	/**
	 * Gets the html templet.
	 *
	 * @param plan the plan
	 * @param feedback the feedback
	 * @return the html templet
	 */
	public static String getFeedbackHtmlTemplet(TripPlan plan, FeedBack feedback) {
		String from = null,to=null,date=null,webUrl=null; 
		if(plan!=null){
			/*from = plan.getFrom()!=null ? plan.getFrom().getName() : null;
				to = plan.getTo()!=null ? plan.getTo().getName() : null;*/
			from = feedback.getAddFrom()!=null ? feedback.getAddFrom() : (plan.getFrom()!=null ? plan.getFrom().getName() : null);
			to =  feedback.getAddTo()!=null ? feedback.getAddTo() : (plan.getTo()!=null ? plan.getTo().getName() : null);

			date = DateFormatUtils.format(plan.getDate(), TpConstants.OTP_DATE_FORMAT);
			if(plan.getPlanUrlParams()!=null)
				webUrl = "<a href='"+TpConstants.SERVER_WEB_URL+"#/submit&"+plan.getPlanUrlParams()+"'>"+TpConstants.SERVER_WEB_URL+"</a>";			
		}
		if(feedback.getSource() == FEEDBACK_SOURCE_TYPE.OVERVIEW.ordinal()){
			from = feedback.getAddFrom();
			to = feedback.getAddTo();
			date = feedback.getDate();
			if(from!=null && from.equalsIgnoreCase("(null)"))
				from=null;
			if(to!=null && to.equalsIgnoreCase("(null)"))
				to=null;
			if(date!=null && date.equalsIgnoreCase("(null)"))
				date=null;
		}
		String otpFeedback = TpConstants.OTP_HTML_STRING
				.replace("--from--", StringUtils.defaultIfBlank(from, "-"))
				.replace("--to--", StringUtils.defaultIfBlank(to, "-"))
				.replace("--date--", StringUtils.defaultIfBlank(date, "-"))
				.replace("--weburl--", StringUtils.defaultIfBlank(webUrl, "-"))
				.replace("--feedbacktime--", DateFormatUtils.format(new Date(), TpConstants.OTP_DATE_FORMAT))
				.replace("--sendermail--",StringUtils.defaultIfBlank(feedback.getEmailId(), "-"))
				.replace("--txtfb--",StringUtils.defaultIfBlank(feedback.getFbText(), "-"))
				.replace("--source--", FEEDBACK_SOURCE_TYPE.values()[feedback.getSource()].name());
		return otpFeedback;

	}

}
