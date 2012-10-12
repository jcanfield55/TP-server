/*
 * 
 */
package com.nimbler.tp.dataobject;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.nimbler.tp.util.OperationCode.TP_CODES;

/**
 *Response pojo used for gtfs meta data.<br />
 *<b>i.e</b><br /> 
 *1. last gtfs update time<br />
 *2. exception dates<br />
 *3. service on week days
 */
@XmlRootElement
public class GtfsResponse extends TPResponse{
	private Map<String, String> gtfsUpdateTime;
	private Map<String, String[]> gtfsServiceByWeekDay;
	Map<String, Map<String,String>>  gtfsServiceExceptionDates;
	public GtfsResponse() {

	}

	public GtfsResponse(TP_CODES codes) {
		super(codes);
	}

	public Map<String, String> getGtfsUpdateTime() {
		return gtfsUpdateTime;
	}
	public void setGtfsUpdateTime(Map<String, String> gtfsUpdateTime) {
		this.gtfsUpdateTime = gtfsUpdateTime;
	}

	public Map<String,String[]> getGtfsServiceByWeekDay() {
		return gtfsServiceByWeekDay;
	}

	public Map<String, Map<String, String>> getGtfsServiceExceptionDates() {
		return gtfsServiceExceptionDates;
	}

	public void setGtfsServiceExceptionDates(
			Map<String, Map<String, String>> gtfsServiceExceptionDates) {
		this.gtfsServiceExceptionDates = gtfsServiceExceptionDates;
	}

	public void setGtfsServiceByWeekDay(Map<String, String[]> gtfsServiceByWeekDay) {
		this.gtfsServiceByWeekDay = gtfsServiceByWeekDay;
	}
}	