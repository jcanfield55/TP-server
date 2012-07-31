/*
 * @author nirmal
 */
package com.nimbler.tp.util;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.nimbler.tp.dataobject.Itinerary;
import com.nimbler.tp.dataobject.TripResponse;
import com.nimbler.tp.gtfs.GtfsMonitorResult;
import com.nimbler.tp.util.HtmlUtil.GtfsSummery;
import com.nimbler.tp.util.HtmlUtil.TableRow;

/**
 * The Class GtfsUtils.
 *
 * @author nirmal
 */
@SuppressWarnings("unchecked")
public class GtfsUtils {	


	private static SimpleDateFormat reqDateFormat = new SimpleDateFormat("M/d/yyyy");

	/**
	 * Mearge monitor result.
	 *
	 * @param result the result
	 */
	public static void meargeMonitorResult(GtfsMonitorResult result) {
		Map<String,TableRow> merged = new LinkedHashMap<String, TableRow>();
		Map<String, Date> oldData = result.getOldData();
		if (oldData != null){
			for (Map.Entry<String, Date> entry : oldData.entrySet()) {
				String key = entry.getKey();
				Date value = entry.getValue();
				if(!merged.containsKey(key)){
					TableRow row = new TableRow();
					row.setService(key);
					row.setOldDate(value);
					merged.put(key, row);
				}
			}
		}		 
		Map<String, Date> newData = result.getNewData();
		if (newData != null && newData.size()>0){
			for (Map.Entry<String, Date> entry : newData.entrySet()) {
				String key = entry.getKey();
				Date value = entry.getValue();
				if(!merged.containsKey(key)){
					TableRow row = new TableRow();
					row.setService(key);
					row.setNewDate(value);
					merged.put(key, row);
				}else
					merged.get(key).setNewDate(value);
			}
		}

		Map<String, Date> crackedData = result.getCrackedData();
		if (crackedData != null && crackedData.size()>0){
			for (Map.Entry<String, Date> entry : crackedData.entrySet()) {
				String key = entry.getKey();
				Date value = entry.getValue();
				if(!merged.containsKey(key)){
					TableRow row = new TableRow();
					row.setService(key);
					row.setCrackedDate(value);
					merged.put(key, row);
				}else
					merged.get(key).setCrackedDate(value);
			}
		}
		result.setMerged(merged);		
	}

	/**
	 * Sets the gtfs summery.
	 *
	 * @param result the new gtfs summery
	 */
	public static void setGtfsSummery(GtfsMonitorResult result) {
		GtfsSummery summury = new GtfsSummery(result.getGtfsBundle().getDefaultAgencyId());
		List<TableRow> lstRows = new ArrayList<HtmlUtil.TableRow>(result.getMerged().values());
		for (TableRow tableRow : lstRows) {
			if(tableRow.getOldDate()==null)
				summury.incNewService();
			else if(tableRow.getNewDate()==null)
				summury.incCanceledService();
			if(tableRow.isExpireinSevenDays())
				summury.incExpInSevenDay();
			if(tableRow.isOldExpired())
				summury.incExpiredService();
			if(tableRow.isNewDataAvailable())
				summury.incNewData();
			if(tableRow.isSameDate())
				summury.incSameData();
			if(tableRow.isCracked())
				summury.incCrackedService();
			if(tableRow.isCrackedExpired())
				summury.incExpiredCrackedService();
		}
		result.setGtfsSummury(summury);
	}

	/**
	 * Gets the plan url from response.
	 *
	 * @param oldResponse the old response
	 * @param arrParams the arr params
	 * @return 
	 * @return the plan url from response
	 * @throws TpException 
	 */
	public static String getPlanUrlFromResponse(String baseUrl,TripResponse oldResponse, String[] arrParams,Date date) throws TpException {
		Map<String,String> map = oldResponse.getRequestParameters();
		if(map==null)
			throw new TpException("No request patameter found while generation reverse url");
		List<String> lstParams = new ArrayList<String>();
		for (int i = 0; i < arrParams.length; i++) {
			String val = map.get(arrParams[i]);
			if(!ComUtils.isEmptyString(val))
				lstParams.add(arrParams[i]+"="+URLEncoder.encode(val));
		}
		String mode = map.get("mode");
		mode = StringUtils.remove(mode, " ");
		mode = StringUtils.substringBetween(mode,"(",")") ;
		StringBuffer sbUrl = new StringBuffer().append(baseUrl);
		sbUrl.append(StringUtils.join(lstParams,"&"));
		sbUrl.append("&mode="+mode);
		if(date!=null)
			sbUrl.append("&date="+reqDateFormat.format(new Date()));
		else
			sbUrl.append("&date="+map.get("date"));
		return sbUrl.toString();
	}

	/**
	 * Gets the same itenerary.
	 *
	 * @param oldItineraries the old itineraries
	 * @param newItineraries the new itineraries
	 * @return the same itenerary
	 */
	public static Itinerary[] getSameItenerary(List<Itinerary> oldItineraries,List<Itinerary> newItineraries){
		if(ComUtils.isEmptyList(oldItineraries) || ComUtils.isEmptyList(newItineraries))
			return null;
		for (Itinerary old : oldItineraries) {
			for (Itinerary newIt : newItineraries) {
				if(old.equals(newIt))
					return new Itinerary[]{old,newIt};
			}
		}
		return null;
	}

	/**
	 * Checks if is same legs.
	 *
	 * @param old the old
	 * @param newIt the new it
	 * @return true, if is same legs
	 */
	public static boolean isSameLegs(Itinerary old,Itinerary newIt){		
		if(ComUtils.isEmptyList(old.getLegs()) || ComUtils.isEmptyList(newIt.getLegs())||
				old.getLegs().size()!=newIt.getLegs().size())
			return false;
		for (int i = 0; i < old.getLegs().size(); i++) {
			if(!old.getLegs().get(i).equals(newIt.getLegs().get(i)))
				return false;
		}
		return true;
	}
}
