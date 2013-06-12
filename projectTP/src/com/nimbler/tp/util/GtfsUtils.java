/*
 * @author nirmal
 */
package com.nimbler.tp.util;

import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;

import com.nimbler.tp.dataobject.BartRouteInfo;
import com.nimbler.tp.dataobject.Itinerary;
import com.nimbler.tp.dataobject.NimblerGtfsAgency;
import com.nimbler.tp.dataobject.TripResponse;
import com.nimbler.tp.gtfs.GtfsBundle;
import com.nimbler.tp.gtfs.GtfsCalandeDates;
import com.nimbler.tp.gtfs.GtfsCalander;
import com.nimbler.tp.gtfs.GtfsMonitorResult;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.HtmlUtil.GtfsSummery;
import com.nimbler.tp.util.HtmlUtil.TableRow;

/**
 * The Class GtfsUtils.
 *
 * @author nirmal
 */
@SuppressWarnings("unchecked")
public class GtfsUtils {	

	private static final String strDateFormat = "M/d/yyyy";

	SimpleDateFormat gtfsDateFormat = new SimpleDateFormat(TpConstants.GTFS_DATE_FORMAT);
	private static final String GTFS_ENCODE_FORMAT = "ISO-8859-1";
	private LoggingService logger;
	private String loggerName;

	public GtfsUtils(LoggingService logger, String loggerName) {
		this.logger = logger;
		this.loggerName = loggerName;
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
		Map<String, Date> mapServiceIdAndDate = new HashMap<String, Date>();
		if(file == null || !file.exists())
			return mapServiceIdAndDate;
		List<String> lstLines = IOUtils.readLines(getZipEntryDataStream(file,TpConstants.ZIP_CALENDAR_FILE),GTFS_ENCODE_FORMAT);
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
				mapServiceIdAndDate.put(key, gtfsDateFormat.parse(StringUtils.remove(val, "\"")));
			} catch (Exception e) {
				logger.error(loggerName,"Malformed data Found at line "+i+", data: "+lstLines.get(i));				
			}
		}
		return mapServiceIdAndDate;
	}

	/**
	 * Gets the columns from file.
	 *
	 * @param file the file
	 * @param columns the columns
	 * @param ignoreError the ignore error
	 * @return the columns from file
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParseException the parse exception
	 * @throws TpException the tp exception
	 */
	public List<String> getColumnsFromFile(File file,String[] columns,String entryName) throws IOException, ParseException, TpException {
		return getColumnsFromFile(file, columns, entryName,true,true);
	}
	public List getColumnsFromFile(File file,String[] columns,String entryName,boolean join,boolean withHeader) throws IOException, ParseException, TpException {
		List res = new ArrayList();
		if(file == null || !file.exists() || columns==null)
			return res;
		List<String> lstLines = IOUtils.readLines(getZipEntryDataStream(file,entryName),GTFS_ENCODE_FORMAT);

		if(ComUtils.isEmptyList(lstLines))
			throw new TpException("Invalid file, No data found in file: "+file);
		String[] headers = lstLines.get(0).split(",");

		int[] colIndex = new int[columns.length]; 
		for (int i = 0; i < columns.length; i++) {
			colIndex[i] = -1;
			for (int j = 0; j < headers.length; j++) {
				if(headers[j].toLowerCase().indexOf(columns[i])!=-1)
					colIndex[i]=j;
			}
		}
		if(withHeader){
			res.add(join?StringUtils.join(columns,","):columns);
		}
		for (int i = 1; i < lstLines.size(); i++) {					
			try {
				String line = lstLines.get(i);
				if(ComUtils.isEmptyString(line)){
					//					logger.warn(loggerName, "empty line found in gtfs");
					continue;
				}
				String[] arrLines = split(line);
				if(arrLines==null)
					continue;
				String[] temp = new String[columns.length];
				for (int j = 0; j < colIndex.length; j++) {
					if(colIndex[j] !=-1 && colIndex[j] < arrLines.length && arrLines[colIndex[j]]!=null)
						temp[j] =arrLines[colIndex[j]];
				}
				res.add(join?StringUtils.join(temp,","):temp);
			} catch (Exception e) {				
				logger.error(loggerName,"Malformed data Found at line "+i+", data: "+lstLines.get(i),e);				
			}
		}
		return res;
	}

	private String[] split(String line) {
		CSVParser csvParser = new CSVParser();
		try {
			String[] arrValues =   csvParser.parseLine(line);
			if(arrValues!=null){
				for (int i = 0; i < arrValues.length; i++) {
					arrValues[i] =  StringUtils.trim(StringUtils.replace(arrValues[i],",", "-"));
				}
			}
			return arrValues;
		} catch (IOException e) {
			logger.error(loggerName, e.getMessage()+", line:"+line);			
		}	
		return null;
	}

	/**
	 * Gets the zip entry data stream.
	 *
	 * @param file the file
	 * @param fileName the file name
	 * @return the zip entry data stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TpException the tp exception
	 */
	private InputStream getZipEntryDataStream(File file, String fileName) throws IOException, TpException {		
		ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
		ZipEntry zipEntry = null;
		ZipEntry temp = null; 
		while((temp=zipInputStream.getNextEntry())!=null){
			if(fileName.equalsIgnoreCase(temp.getName())){
				zipEntry = temp;
				return zipInputStream;	
			}
		}
		if(zipEntry == null)
			throw new TpException("Could not find zip Entry: "+fileName+" in " + file.getAbsolutePath());
		return null;
	}

	/**
	 * Read from gtfs.
	 *
	 * @param file the file
	 * @param columns the columns
	 * @return the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParseException the parse exception
	 * @throws TpException the tp exception
	 */
	public List<GtfsCalander> readCalanderGtfs(File file) throws IOException, ParseException, TpException {
		List<GtfsCalander> lstGtfsCalanders = new ArrayList<GtfsCalander>();
		/*ZipFile zipFile = new ZipFile(file);
		ZipEntry zipEntry = zipFile.getEntry(TpConstants.ZIP_CALENDAR_FILE);		
		List<String> lstLines = IOUtils.readLines(zipFile.getInputStream(zipEntry),GTFS_ENCODE_FORMAT);*/		
		List<String> lstLines = IOUtils.readLines(getZipEntryDataStream(file, TpConstants.ZIP_CALENDAR_FILE),GTFS_ENCODE_FORMAT);		
		if(ComUtils.isEmptyList(lstLines))
			throw new TpException("Invalid file, No data found in file: "+file);
		String[] headers = lstLines.get(0).split(",");
		int END_DATE_INDEX = -1;
		int START_DATE_INDEX = -1;
		int SERVICE_ID_INDEX = -1;
		int[] WEEK_INDEX = new int[]{-1,-1,-1,-1,-1,-1,-1};
		for (int i = 0; i < headers.length; i++) {			
			if(headers[i].toLowerCase().indexOf("service_id")!=-1)
				SERVICE_ID_INDEX = i;
			else if (headers[i].toLowerCase().indexOf("start_date")!=-1)
				START_DATE_INDEX =i;
			else if (headers[i].toLowerCase().indexOf("end_date")!=-1)
				END_DATE_INDEX =i;
			else if (headers[i].toLowerCase().indexOf("sunday")!=-1)
				WEEK_INDEX[0] =i;
			else if (headers[i].toLowerCase().indexOf("monday")!=-1)
				WEEK_INDEX[1] =i;
			else if (headers[i].toLowerCase().indexOf("tuesday")!=-1)
				WEEK_INDEX[2] =i;
			else if (headers[i].toLowerCase().indexOf("wednesday")!=-1)
				WEEK_INDEX[3] =i;
			else if (headers[i].toLowerCase().indexOf("thursday")!=-1)
				WEEK_INDEX[4] =i;
			else if (headers[i].toLowerCase().indexOf("friday")!=-1)
				WEEK_INDEX[5] =i;
			else if (headers[i].toLowerCase().indexOf("saturday")!=-1)
				WEEK_INDEX[6] =i;

		}
		if(START_DATE_INDEX == -1 || END_DATE_INDEX==-1 || SERVICE_ID_INDEX==-1 ||ArrayUtils.contains(WEEK_INDEX, -1))
			throw new TpException("No valid header index found  in data for file(calander.txt):"+file+", header: "+lstLines.get(0));
		for (int i = 1; i < lstLines.size(); i++) {					
			try {
				GtfsCalander calander = new GtfsCalander();
				String[] line = lstLines.get(i).split(",");
				ComUtils.removeQuotation(line);
				if(line==null || line.length<9){
					logger.warn(loggerName, "empty/invalid line found in gtfs:"+file.getAbsoluteFile()+" at line:"+lstLines.get(i));
					continue;					
				}
				int[] weekDays = new int[7]; 
				for (int j = 0; j < WEEK_INDEX.length; j++) 
					weekDays[j] = NumberUtils.toInt(line[WEEK_INDEX[j]]);

				calander.setServiceName(line[SERVICE_ID_INDEX]);
				calander.setStartDate(gtfsDateFormat.parse(line[START_DATE_INDEX]));
				calander.setEndDate(gtfsDateFormat.parse(line[END_DATE_INDEX]));
				calander.setWeeklyStatusForService(weekDays);
				lstGtfsCalanders.add(calander);
			} catch (Exception e) {
				logger.error(loggerName,"Malformed data Found in file(calander.txt):"+file.getAbsolutePath()+" at line "+i+", data: "+lstLines.get(i),e);				
			}
		}
		return lstGtfsCalanders;
	}
	/**
	 * Read agency ids.
	 *
	 * @param gtfsFile the gtfs file
	 * @return the list
	 * @throws IOException 
	 * @throws ZipException 
	 * @throws TpException 
	 */
	public List<String> readAgencyIds(File file) throws ZipException, IOException, TpException {
		List<String> lstRes = new ArrayList<String>();
		List<String> lstLines = IOUtils.readLines(getZipEntryDataStream(file, TpConstants.ZIP_AGENCY_FILE),GTFS_ENCODE_FORMAT);		
		if(ComUtils.isEmptyList(lstLines))
			throw new TpException("Invalid file, No data found in file: "+file);
		String[] headers = lstLines.get(0).split(",");
		int AGENCY_ID = -1;
		int AGENCY_NAME = -1;
		for (int i = 0; i < headers.length; i++) {			
			if(headers[i].toLowerCase().indexOf("agency_id")!=-1)
				AGENCY_ID = i;
			else if (headers[i].toLowerCase().indexOf("agency_name")!=-1)
				AGENCY_NAME =i;
		}
		if(AGENCY_ID == -1 || AGENCY_NAME==-1)
			throw new TpException("No valid header index found  in data for file(agency.txt):"+file+", header: "+lstLines.get(0));
		for (int i = 1; i < lstLines.size(); i++) {					
			try {
				String[] line = lstLines.get(i).split(",");
				ComUtils.removeQuotation(line);
				if(line==null || line.length<2){
					logger.warn(loggerName, "empty/invalid line found in gtfs:"+file.getAbsoluteFile()+" at line:"+lstLines.get(i));
					continue;					
				}
				lstRes.add(StringUtils.defaultIfBlank(line[AGENCY_ID], line[AGENCY_NAME]));
			} catch (Exception e) {
				logger.error(loggerName,"Malformed data Found in file(calander.txt):"+file.getAbsolutePath()+" at line "+i+", data: "+lstLines.get(i),e);				
			}
		}
		return lstRes;
	}

	/**
	 * Read calander dates gtfs.
	 *
	 * @param file the file
	 * @return the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParseException the parse exception
	 * @throws TpException the tp exception
	 */
	public List<GtfsCalandeDates> readCalanderDatesGtfs(File file) throws IOException, ParseException, TpException {
		List<GtfsCalandeDates> lstGtfsCalanders = new ArrayList<GtfsCalandeDates>();
		try {
			List<String> lstLines = IOUtils.readLines(getZipEntryDataStream(file,TpConstants.ZIP_CALENDAR_DATES_FILE),GTFS_ENCODE_FORMAT);		
			if(ComUtils.isEmptyList(lstLines))
				throw new TpException("Invalid file, No data found in file: "+file);
			String[] headers = lstLines.get(0).split(",");
			int EXCEPTION_TYPE_INDEX = -1;
			int DATE_INDEX = -1;
			int SERVICE_ID_INDEX = -1;
			for (int i = 0; i < headers.length; i++) {
				if(headers[i].toLowerCase().indexOf("service_id")!=-1)
					SERVICE_ID_INDEX = i;
				else if (headers[i].toLowerCase().indexOf("date")!=-1)
					DATE_INDEX =i;
				else if (headers[i].toLowerCase().indexOf("exception_type")!=-1)
					EXCEPTION_TYPE_INDEX =i;
			}
			if(DATE_INDEX == -1 || EXCEPTION_TYPE_INDEX==-1 || SERVICE_ID_INDEX==-1 )
				throw new TpException("No service_id,start_date or end_date found in data for file:"+file);
			for (int i = 1; i < lstLines.size(); i++) {					
				try {
					GtfsCalandeDates calanderDates = new GtfsCalandeDates();
					String[] line = lstLines.get(i).split(",");
					ComUtils.removeQuotation(line);
					if(line==null || SERVICE_ID_INDEX > (line.length-1) || EXCEPTION_TYPE_INDEX > (line.length-1) ||  DATE_INDEX > (line.length-1)){
						logger.warn(loggerName, "empty line found in gtfs file:"+file);
						continue;					
					}
					calanderDates.setServiceName(line[SERVICE_ID_INDEX]);
					calanderDates.setDate(line[DATE_INDEX]);
					calanderDates.setServiceType(line[EXCEPTION_TYPE_INDEX]);
					lstGtfsCalanders.add(calanderDates);
				} catch (Exception e) {
					logger.error(loggerName,"Malformed data Found in file(calander_dates.txt):"+file.getAbsolutePath()+" at line "+i+", data: "+lstLines.get(i),e);					
				}
			}
		} catch (TpException e) {
			logger.error(loggerName, e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
		}
		return lstGtfsCalanders;
	}

	/* ===================================== static methods =================================================*/
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

			if(tableRow.getOldDate()!=null || tableRow.getCrackedDate()!=null){
				if(tableRow.isCracked() && !tableRow.isCrackedExpired() ){
					summury.incActiveService();
				}else if(!tableRow.isCracked() && !tableRow.isOldExpired())
					summury.incActiveService();
			}

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
		//mode = StringUtils.substringBetween(mode,"(",")") ;
		StringBuffer sbUrl = new StringBuffer().append(baseUrl);
		sbUrl.append(StringUtils.join(lstParams,"&"));
		sbUrl.append("&mode="+mode);
		SimpleDateFormat reqDateFormat = new SimpleDateFormat(strDateFormat);
		if(date!=null)
			sbUrl.append("&date="+reqDateFormat.format(new Date()));
		else
			sbUrl.append("&date="+map.get("date"));
		return sbUrl.toString();
	}

	/**
	 * Gets the gtfs dao.
	 *
	 * @param file the file
	 * @param entityClasses
	 * @return the gtfs dao
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static  GtfsRelationalDaoImpl getGtfsDao(String file, List<Class<?>> entityClasses) throws IOException {
		GtfsReader reader = new GtfsReader();
		reader.setInputLocation(new File(file));
		GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
		reader.setEntityStore(dao);
		if(entityClasses!=null)
			reader.setEntityClasses(entityClasses);
		reader.run();
		return dao;
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
	/**
	 * Read list of routes from routes.txt file for BART agency.
	 * @param filePath
	 * @return
	 * @throws ZipException
	 * @throws IOException
	 * @throws TpException
	 */
	public List<BartRouteInfo> getBARTRoutes(String filePath) throws ZipException, IOException, TpException {
		File file = new File(filePath);
		List<BartRouteInfo> bartRoutes = new ArrayList<BartRouteInfo>();
		List<String> lstLines = IOUtils.readLines(getZipEntryDataStream(file, TpConstants.ZIP_ROUTES_FILE),GTFS_ENCODE_FORMAT);		
		if (ComUtils.isEmptyList(lstLines))
			throw new TpException("BART GTFS: Invalid file, No data found in file: "+file);
		String[] headers = lstLines.get(0).split(",");
		int ROUTE_ID = -1;
		int ROUTE_NAME = -1;
		for (int i = 0; i < headers.length; i++) {			
			if (headers[i].toLowerCase().indexOf("route_id")!=-1)
				ROUTE_ID = i;
			else if (headers[i].toLowerCase().indexOf("route_long_name")!=-1)
				ROUTE_NAME =i;
		}
		if (ROUTE_ID == -1 || ROUTE_NAME==-1)
			throw new TpException("BART GTFS: No valid header index found  in data for file(routes.txt):"+file+", header: "+lstLines.get(0));

		for (int i = 1; i < lstLines.size(); i++) {
			try {
				String[] line = lstLines.get(i).split(",");
				ComUtils.removeQuotation(line);
				if (line==null || line.length<2) {
					logger.warn(loggerName, "BART GTFS: Empty/Invalid line found in gtfs:"+file.getAbsoluteFile()+" at line:"+lstLines.get(i));
					continue;
				}
				bartRoutes.add(new BartRouteInfo(line[ROUTE_ID].trim(), line[ROUTE_NAME].trim()));
			} catch (Exception e) {
				logger.error(loggerName,"BART GTFS: Malformed data Found in file(routes.txt):"+file.getAbsolutePath()+" at line "+i+", data: "+lstLines.get(i),e);				
			}
		}
		return bartRoutes;
	}
	/**
	 * Read trip headsign and their directions for a route from trips.txt file and update them
	 * in route list. (For BART agency)
	 *   
	 * @param fileName
	 * @param bartRoutes
	 * @throws ZipException
	 * @throws IOException
	 * @throws TpException
	 */
	public void updateBARTRouteDetails(String fileName, List<BartRouteInfo> bartRoutes) throws ZipException, IOException, TpException {
		File file = new File(fileName);
		/*ZipFile zipFile = new ZipFile(file);
		ZipEntry zipEntry = zipFile.getEntry(TpConstants.ZIP_TRIPS_FILE);*/		
		List<String> lstLines = IOUtils.readLines(getZipEntryDataStream(file, TpConstants.ZIP_TRIPS_FILE),GTFS_ENCODE_FORMAT);		
		if (ComUtils.isEmptyList(lstLines))
			throw new TpException("BART GTFS: Invalid file, No data found in file: "+file);
		String[] headers = lstLines.get(0).split(",");
		int ROUTE_ID = -1;
		int TRIP_HEADSIGN = -1;
		int DIRECTION_ID = -1;//optional
		for (int i = 0; i < headers.length; i++) {			
			if (headers[i].toLowerCase().indexOf("route_id")!=-1)
				ROUTE_ID = i;
			else if (headers[i].toLowerCase().indexOf("trip_headsign")!=-1)
				TRIP_HEADSIGN =i;
			else if (headers[i].toLowerCase().indexOf("direction_id")!=-1)
				DIRECTION_ID = i;
		}
		if (ROUTE_ID == -1 || TRIP_HEADSIGN==-1)
			throw new TpException("BART GTFS: No valid header index found  in data for file(trips.txt):"+file+", header: "+lstLines.get(0));

		for (int i = 1; i < lstLines.size(); i++) {
			try {
				String[] line = lstLines.get(i).split(",");
				ComUtils.removeQuotation(line);
				if (line==null || line.length<2) {
					logger.warn(loggerName, "BART GTFS: Empty/Invalid line found in gtfs:"+file.getAbsoluteFile()+" at line:"+lstLines.get(i));
					continue;
				}
				String routeId = line[ROUTE_ID];
				for (BartRouteInfo info: bartRoutes) {
					if (info.getRouteId().equals(routeId)) {
						info.getHeadSignToDirectionMap().put(line[TRIP_HEADSIGN].trim(), StringUtils.defaultIfBlank(line[DIRECTION_ID], "-1"));
						break;
					}
				}
			} catch (Exception e) {
				logger.error(loggerName,"BART GTFS: Malformed data Found in file(trips.txt):"+file.getAbsolutePath()+" at line "+i+", data: "+lstLines.get(i),e);				
			}
		}
	}

	/**
	 * Gets the agency ids from trip id combo.
	 *
	 * @param strAgencyId the str agency id
	 * @return the agency ids from trip id combo
	 */
	public static String[] getAgencyIdsFromTripIdCombo(String[] strAgencyId) {
		if(strAgencyId==null)
			return null;
		String[] res = new String[strAgencyId.length];
		for (int i = 0; i < strAgencyId.length; i++) {
			res[i] =StringUtils.substringBefore(strAgencyId[i], "_");
		}
		return res;
	}
	public static String getACtransitGtfsTripIdFromApiId(String tripID) {
		return  substringBefore(tripID, "-");
	}

	public static NimblerGtfsAgency getAgencyDetail(GtfsBundle bundle,boolean extended) {
		NimblerGtfsAgency agency = new NimblerGtfsAgency();
		if(extended){
			agency.setLastUpdate(bundle.getLastUpdateDate());
			agency.setAgencyType(bundle.getAgencyType());
		}
		agency.setNimberAgencyId(bundle.getAgencyType().ordinal());
		agency.setAgencies(bundle.getAgencies());
		agency.setExclusionType(bundle.getExclusionType().getText());
		return agency;
	}
}