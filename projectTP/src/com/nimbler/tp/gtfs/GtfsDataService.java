/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import com.nimbler.tp.dataobject.AgencyAndId;
import com.nimbler.tp.dataobject.BartRouteInfo;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.NimblerApps;
import com.nimbler.tp.dataobject.NimblerGtfsAgency;
import com.nimbler.tp.dataobject.Place;
import com.nimbler.tp.dataobject.RealTimePrediction;
import com.nimbler.tp.dataobject.StopTimeType;
import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.TpPlanService;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.GtfsUtils;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.ResponseUtil;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;
import com.nimbler.tp.util.TpConstants.GTFS_FILE;
import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;
import com.nimbler.tp.util.TpException;
import com.nimbler.tp.util.WmataUtil;
import com.nimbler.tp.util.ZipUtil;

public class GtfsDataService {

	@Autowired
	private LoggingService loggingService;

	private String loggerName = GtfsDataService.class.getName();

	@Autowired
	private GtfsContext gtfsContext;

	@Autowired
	private TpPlanService planService;

	@Autowired
	private NimblerApps nimblerApps;

	/** 
	 * AGENCY_TYPE, BUNDLE 
	 */
	private Map<String, GtfsBundle> bundleMap = new HashMap<String, GtfsBundle>();

	/**
	 * file name,columns array
	 */
	private Map<String, String[]> gtfsColumHeaders;
	/**
	 * 
	 */
	private Map<String, List<String>> rawStopTimesByTripId = new HashMap<String, List<String>>();

	/**
	 * <agencyOrdinal_routeid,list of data>
	 */
	private Map<String, List<String>> rawTripDataByRouteId = new HashMap<String, List<String>>();

	/**
	 * Maps gtfs trip id to nextbus api trip id for actransit<br \>
	 * trip id for <b>gtfs --> api</b> <br \>
	 * e.g <b>3341907-1212WR-DB-Weekday-01</b> --> <b>3341907</b>
	 */
	private BidiMap acTransitTripIdMapping = new DualHashBidiMap();

	/**
	 * agencyOrdinal_gtfsFile.getName() --> list of rows
	 */
	private Map<String,List<String>> gtfsData = new HashMap<String,List<String>>();
	/**
	 * 
	 */
	private RouteStopIndex bartRouteStops = new RouteStopIndex();

	/**
	 * contains mapping for tripid --> trip stopid sequence, used for next bus
	 */
	TripStopIndex tripStopIndex = new TripStopIndex();

	private List<BartRouteInfo> bartRouteInfo = new ArrayList<BartRouteInfo>();

	private String ageciesToLoad ="1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20";

	private String allStopTimesZipFile ="gtfsStopTimes.zip";

	private boolean gtfsReadCompleted = false;

	private boolean useInMemoryGtfs = true;

	private int MAX_EARLY_MIN = 5*60;
	private int MAX_DELAY_MIN = 60*60;

	@SuppressWarnings("unused")
	@PostConstruct
	private void init() {
		List<GtfsBundle> gtfsBundles = gtfsContext.getGtfsBundles();
		for (GtfsBundle gtfsBundle : gtfsBundles) {
			bundleMap.put(gtfsBundle.getAgencyType().ordinal()+"", gtfsBundle);
		}
		if(useInMemoryGtfs){
			new Thread(){
				public void run() {			
					readGtfsData();
				};
			}.start();
		}
	}

	protected void test() {
		Leg leg = new Leg();
		leg.setStartTime(1365011280000L);
		leg.setEndTime(1365016740005L);
		leg.setAgencyId("BART");
		Place formPlace = new Place();
		formPlace.setStopId(new AgencyAndId("BART","19TH_N"));
		leg.setFrom(formPlace);
		Place toPlace = new Place();
		toPlace.setStopId(new AgencyAndId("BART","ASHB"));
		leg.setTo(toPlace);
		RealTimePrediction realTimePrediction = new RealTimePrediction();
		realTimePrediction.setEpochTime(1365016680000L);
		getMatchingScheduleForRealTime(leg, AGENCY_TYPE.BART, realTimePrediction);

	}

	/**
	 * Read gtfs data.
	 */
	private void readGtfsData() {
		long start = System.currentTimeMillis();

		createStopIndexList();
		createBartStopIndexList();		
		readRawGtfsData();
		readBartRouteInfo();

		long end1 = System.currentTimeMillis();
		gtfsReadCompleted = true;		
		System.out.println("All Gtfs read done in "+ (end1 - start) / 1000 + "sec");
	}

	/**
	 * Read raw gtfs data.
	 */
	private void readRawGtfsData() {
		String[] arrAgencies = ageciesToLoad.split(",");
		for (GTFS_FILE file : GTFS_FILE.values()) {
			if(file.ordinal()==0)
				continue;
			try {
				long start = System.currentTimeMillis();
				if(file.equals(GTFS_FILE.STOP_TIMES)){
					Map<String, List<String>> temp =  createIndexForColumnWithAgency(arrAgencies,file,"trip_id");
					rawStopTimesByTripId.putAll(temp);
				}if(file.equals(GTFS_FILE.TRIPS)){
					Map<String, List<String>> temp =  createIndexForColumnWithAgency(arrAgencies,file,"route_id");
					rawTripDataByRouteId.putAll(temp);
				}else{					
					Map<String, List<String>> temp =  readGtfsDataByAgency(file,arrAgencies);
					gtfsData.putAll(temp);					
				}
				long end = System.currentTimeMillis();
				System.out.println("read done for: "+file.getFileName()+", Time:"+(end - start) / 1000+ " sec");
			} catch (TpException e) {
				loggingService.warn(loggerName,"[TpException] File:"+file+", "+ e.getMessage());
			}
		}
	}

	/**
	 * Creates the next bus stop index list {@link TripStopIndex}.
	 */
	private void createStopIndexList() {
		long start = System.currentTimeMillis();
		List<Class<?>> lstClasses = new ArrayList<Class<?>>();
		lstClasses.add(Agency.class);
		lstClasses.add(Stop.class);
		lstClasses.add(Route.class);
		lstClasses.add(Trip.class);
		lstClasses.add(StopTime.class);
		for (GtfsBundle bundle : gtfsContext.getGtfsBundles()) {
			try {
				if(bundle.getAgencyType().equals(AGENCY_TYPE.AC_TRANSIT)){
					GtfsRelationalDaoImpl context = GtfsUtils.getGtfsDao(bundle.getValidFile(), lstClasses);
					tripStopIndex.save(bundle.getAgencyType(), context,new Transformer() {
						@Override
						public Object transform(Object obj) {
							Trip trip = (Trip) obj;
							return GtfsUtils.getACtransitGtfsTripIdFromApiId(trip.getId().getId());
						}
					});
					for (Trip trip : context.getAllTrips()) {
						String tripId = trip.getId().getId();
						String mappedTripId = GtfsUtils.getACtransitGtfsTripIdFromApiId(tripId);
						if(mappedTripId!=null)
							acTransitTripIdMapping.put(tripId, mappedTripId);						
					}
				}else if (bundle.getAgencyType().equals(AGENCY_TYPE.SFMUNI) || bundle.getAgencyType().equals(AGENCY_TYPE.TRIMET)) {
					GtfsRelationalDaoImpl context = GtfsUtils.getGtfsDao(bundle.getValidFile(), lstClasses);
					tripStopIndex.save(bundle.getAgencyType(), context,new Transformer() {
						@Override
						public Object transform(Object obj) {
							Trip trip = (Trip) obj;
							return trip.getId().getId();
						}
					});

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("next bus stop index created..."+ (end - start)/1000 + " sec");
	}

	/**
	 * Creates the bart stop index list.
	 */
	private void createBartStopIndexList() {
		try {
			GtfsReader reader = new GtfsReader();
			GtfsBundle bartBundle = gtfsContext.getBundle(AGENCY_TYPE.BART);
			if(bartBundle==null){
				loggingService.error(loggerName, "No valid bart file found");
				return;
			}
			String bartFile = bartBundle.getValidFile();
			reader.setInputLocation(new File(bartFile));

			List<Class<?>> entityClasses = new ArrayList<Class<?>>();
			entityClasses.add(Agency.class);
			entityClasses.add(Route.class);
			entityClasses.add(Trip.class);
			entityClasses.add(Stop.class);
			entityClasses.add(StopTime.class);
			reader.setEntityClasses(entityClasses);

			GtfsRelationalDaoImpl store = new GtfsRelationalDaoImpl();
			reader.setEntityStore(store);
			reader.run();			

			TreeMultimap<String, StopTime> trip_stoptime = TreeMultimap.create();

			SetMultimap<String, String> route_trip = HashMultimap.create();
			ArrayListMultimap<String, String> tripStopTimesArray = ArrayListMultimap.create();
			for (String trip : trip_stoptime.keySet()) {
				SortedSet<StopTime>  sortedStopTimes= trip_stoptime.get(trip);
				route_trip.put(sortedStopTimes.iterator().next().getTrip().getRoute().getId().getId(), trip);
				for (StopTime stopTime : sortedStopTimes) {
					tripStopTimesArray.put(trip, stopTime.getStop().getId().getId().toLowerCase());
				}
			}
			bartRouteStops.save(route_trip,tripStopTimesArray);
			System.out.println("Bart Stop sequence built..");
		} catch (Exception e) {
			e.printStackTrace();
			loggingService.error(loggerName, e);
		}
	}

	/**
	 * Read BART routes and trip information used in BART prediction.
	 */
	public void readBartRouteInfo() {
		try {
			GtfsBundle bartBundle = gtfsContext.getBundle(AGENCY_TYPE.BART);
			if(bartBundle==null){
				loggingService.error(loggerName, "No valid bart file found");
				return;
			}
			String bartGtfsFileName = bartBundle.getValidFile();
			GtfsUtils util = new GtfsUtils(loggingService, loggerName);
			List<BartRouteInfo> bartRoutes = util.getBARTRoutes(bartGtfsFileName);
			if (bartRoutes!=null && bartRoutes.size()>0) {
				util.updateBARTRouteDetails(bartGtfsFileName, bartRoutes);
				this.bartRouteInfo = bartRoutes;
				System.out.println(String.format("Gtfs file read complete for    :      [%-15s]", "BART - route info"));
			}
		} catch (IOException e) {
			loggingService.error(loggerName, "Error while extracting BART GTFS files: "+e.getMessage());
		} catch (TpException e) {
			loggingService.error(loggerName, "Error while extracting BART GTFS files: "+e.getMessage());
		} catch (Exception e) {
			loggingService.error(loggerName, "Error while parsing BART GTFS files: ",e);
		}
	}

	/**
	 * Gets the closest schedule.
	 *
	 * @param leg the leg
	 * @param type the type
	 * @param realTimePrediction 
	 * @return the closest schedule
	 */
	@SuppressWarnings("unused")
	public void getMatchingScheduleForRealTime(Leg leg,AGENCY_TYPE type, RealTimePrediction realTimePrediction) {
		try {
			String routeId = leg.getRouteId();
			String fromStopId=leg.getFrom().getStopId().getId();
			String toStopId=leg.getTo().getStopId().getId();
			long realTimeMill = realTimePrediction.getEpochTime();

			if(type.equals(AGENCY_TYPE.BART)){
				Leg reqLeg = getRequestLeg(leg,realTimeMill);				
				StopTimeType stopTimeType = planService.getClosestTime(reqLeg,realTimeMill,NIMBLER_APP_TYPE.SF_BAY_AREA.ordinal()+"");
				if(stopTimeType!=null ){										
					long scheduleTime = ComUtils.getSecondsSinceMidNight(stopTimeType.startTime)*1000;
					realTimePrediction.setScheduleTime(DurationFormatUtils.formatDuration(scheduleTime,"HH:mm:ss"));
					realTimePrediction.setScheduleTripId(stopTimeType.tripId);
				}else{
					loggingService.debug(loggerName,"No matching found for :"+leg.getTripId()+", date: "+new Date(leg.getStartTime())+", real time: "+new Date(realTimeMill));
				}
			}else if (type.equals(AGENCY_TYPE.SFMUNI) || type.equals(AGENCY_TYPE.AC_TRANSIT)){
				String tripId = realTimePrediction.getTripId();
				String[] headers = gtfsColumHeaders.get(GTFS_FILE.STOP_TIMES.getName());
				int stopIndex = ArrayUtils.indexOf(headers, "stop_id");
				int departTimeIndex = ArrayUtils.indexOf(headers, "departure_time");
				int tripIdIndex = ArrayUtils.indexOf(headers, "trip_id");
				String id = null; 
				if(type.equals(AGENCY_TYPE.AC_TRANSIT)){					
					id = type.ordinal()+"_"+acTransitTripIdMapping.getKey(tripId);;
				}else
					id = type.ordinal()+"_"+tripId;
				List<String> stopTimes = null;
				stopTimes=rawStopTimesByTripId.get(id);
				if(stopTimes==null || stopTimes.isEmpty()){
					loggingService.info(loggerName, "Stop times found for trip: "+id+",agency:"+type.toString());
					return;
				}
				for (String stopTime : stopTimes) {
					String[] arrStopTime = stopTime.split(",");
					if(arrStopTime.length>1 && arrStopTime[stopIndex].equalsIgnoreCase(fromStopId)){						
						realTimePrediction.setScheduleTime(arrStopTime[departTimeIndex]);						
						realTimePrediction.setScheduleTripId(arrStopTime[tripIdIndex]);
						break;
					}
				}
			}else if(type.equals(AGENCY_TYPE.WMATA)){
				TimeZone tz = WmataUtil.getTimeZone();
				Leg reqLeg = getRequestLeg(leg,realTimeMill);
				StopTimeType stopTimeType = planService.getClosestTime(reqLeg,realTimeMill,NIMBLER_APP_TYPE.WDC.ordinal()+"");
				if(stopTimeType!=null ){
					long scheduleTime = ComUtils.getSecondsSinceMidNight(stopTimeType.startTime,tz)*1000;
					realTimePrediction.setScheduleTime(DurationFormatUtils.formatDuration(scheduleTime,"HH:mm:ss"));
					realTimePrediction.setScheduleTripId(stopTimeType.tripId);
				}
			}
		} catch (TpException e) {
			loggingService.debug(loggerName,e.getMessage());
		} catch (Exception e) {			
			loggingService.error(loggerName,e);
		}
	}

	private Leg getRequestLeg(Leg leg, long epochTime) {
		Leg reqLeg = new Leg();
		reqLeg.setStartTime(epochTime-(MAX_DELAY_MIN*1000));
		reqLeg.setEndTime(epochTime+(MAX_EARLY_MIN*1000));
		reqLeg.setAgencyId(leg.getFrom().getStopId().getAgencyId());
		reqLeg.setFrom(leg.getFrom());
		reqLeg.setTo(leg.getTo());		
		return reqLeg;
	}

	/**
	 * Gets the gtfs data by agency.
	 *
	 * @param gtfsFile the gtfs file
	 * @param agencies - i.e SFMTA,VTA,Caltrain
	 * @return the gtfs data by agency
	 * @throws TpException the tp exception
	 */
	public Map<String, List<String>> readGtfsDataByAgency(GTFS_FILE gtfsFile, String[] agencyIdOrdinals) throws TpException {
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		String[] columns = gtfsColumHeaders.get(gtfsFile.getName());
		if(columns==null || columns.length==0)
			throw new TpException("Unsuported File: "+gtfsFile.getFileName());
		try {
			GtfsUtils gtfsUtils= new GtfsUtils(loggingService, loggerName);
			for (int i = 0; i < agencyIdOrdinals.length; i++) {
				GtfsBundle bundle = bundleMap.get(agencyIdOrdinals[i]);
				if(bundle==null){
					System.out.println("Could not find bundle for: "+agencyIdOrdinals[i]);
					continue;
				}
				try {
					List<String> lstData = gtfsUtils.getColumnsFromFile(new File(bundle.getValidFile()),gtfsColumHeaders.get(gtfsFile.getName()), gtfsFile.getFileName());
					if(lstData!=null)
						resMap.put(agencyIdOrdinals[i]+"_"+gtfsFile.getName(), lstData);
				} catch (TpException e) {				
					loggingService.error(loggerName, e.getMessage());
				} catch (Exception e) {					
					loggingService.error(loggerName,"Error While reading agency:"+agencyIdOrdinals[i]+" for file:"+gtfsFile, e);
				}
			}
		} catch (Exception e) {
			throw new TpException("Error While reading file: "+gtfsFile.getFileName()+" "+e.getMessage());
		}
		return resMap;
	}

	/**
	 * Read stop times by agency.
	 *
	 * @param agencyIdOrdinals the agency id ordinals
	 * @return the map
	 * @throws TpException the tp exception
	 * @deprecated
	 */
	public Map<String, List<String>> readStopTimesByAgency(String[] agencyIdOrdinals) throws TpException {
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		GTFS_FILE gtfsFile = GTFS_FILE.STOP_TIMES;
		try {
			GtfsUtils gtfsUtils= new GtfsUtils(loggingService, loggerName);
			for (int i = 0; i < agencyIdOrdinals.length; i++) {
				GtfsBundle bundle = bundleMap.get(agencyIdOrdinals[i]);	
				if(bundle==null){
					System.out.println("Could not find bundle for: "+agencyIdOrdinals[i]);
					continue;
				}
				int index = getColumnIndexOfGtfsFile(GTFS_FILE.STOP_TIMES, "trip_id");
				if(index==-1){					
					loggingService.error(loggerName, "Could Not find index of trip_id for agency: "+agencyIdOrdinals[i]);
					continue;
				}
				List<String> lstData = gtfsUtils.getColumnsFromFile(new File(bundle.getValidFile()),gtfsColumHeaders.get(gtfsFile.getName()), gtfsFile.getFileName());
				if(lstData!=null){
					for (String line : lstData) {
						String key = agencyIdOrdinals[i]+"_"+line.split(",")[index];
						List<String> data = resMap.get(key);
						if(data==null){
							data = new ArrayList<String>();
							resMap.put(key, data);
						}
						data.add(line);
					}
				}
			}
		} catch (Exception e) {
			loggingService.error(loggerName, e);
		}
		return resMap;
	}

	/**
	 * Read trips by agency.
	 *
	 * @param agencyIdOrdinals the agency id ordinals
	 * @param columnName 
	 * @param gtfsFile 
	 * @return the map
	 * @throws TpException the tp exception
	 */
	public Map<String, List<String>> createIndexForColumnWithAgency(String[] agencyIdOrdinals, GTFS_FILE gtfsFile, String columnName) throws TpException {
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();		
		try {
			GtfsUtils gtfsUtils= new GtfsUtils(loggingService, loggerName);
			for (int i = 0; i < agencyIdOrdinals.length; i++) {
				GtfsBundle bundle = bundleMap.get(agencyIdOrdinals[i]);	
				if(bundle==null){
					System.out.println("Could not find bundle for: "+agencyIdOrdinals[i]);
					continue;
				}
				int index = getColumnIndexOfGtfsFile(gtfsFile, columnName);
				if(index==-1){					
					loggingService.error(loggerName, "Could Not find index of route_id for agency: "+agencyIdOrdinals[i]);
					continue;
				}
				List<String> lstData = gtfsUtils.getColumnsFromFile(new File(bundle.getValidFile()),gtfsColumHeaders.get(gtfsFile.getName()), gtfsFile.getFileName());
				if(lstData!=null){
					for (String line : lstData) {
						String key = agencyIdOrdinals[i]+"_"+line.split(",")[index];
						List<String> data = resMap.get(key);
						if(data==null){
							data = new ArrayList<String>();
							resMap.put(key, data);
						}
						data.add(line);
					}
				}
			}
		} catch (Exception e) {
			loggingService.error(loggerName, e);
		}
		return resMap;
	}


	/**
	 * Gets the gtfs data by agency.
	 *
	 * @param gtfsFile the gtfs file
	 * @param agencyIdOrdinals the agency id ordinals
	 * @return <agencyid_filename, data>
	 * @throws TpException the tp exception
	 */
	public Map<String, List<String>> getGtfsDataByAgency(GTFS_FILE gtfsFile, String[] agencyIdOrdinals) throws TpException {
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		if(!useInMemoryGtfs){
			return readGtfsDataByAgency(gtfsFile, agencyIdOrdinals);
		}
		if(!gtfsReadCompleted)
			throw new TpException(TP_CODES.DATA_NOT_EXIST);
		for (int i = 0; i < agencyIdOrdinals.length; i++) {
			String key = agencyIdOrdinals[i]+"_"+gtfsFile.getName();
			List<String> fileData = gtfsData.get(key);
			if(fileData==null){
				loggingService.warn(loggerName, "Could not find: "+agencyIdOrdinals[i]+"_"+gtfsFile.getName());
				continue;
			}
			resMap.put(key, fileData);
		}
		return resMap;
	}

	/**
	 * Gets the stop times by agency.
	 *
	 * @param agencyIdOrdinals the agency id ordinals
	 * @param tripIds the trip ids
	 * @return the stop times by agency
	 * @throws TpException the tp exception
	 */
	public Map<String, List<String>> getStopTimesByAgency(String[] strAgencyId) throws TpException {		
		Map<String, List<String>> mapToQuery  = null;
		if(!useInMemoryGtfs){
			String[] ids = GtfsUtils.getAgencyIdsFromTripIdCombo(strAgencyId);
			mapToQuery =  readStopTimesByAgency(ids);
		}else{
			if(!gtfsReadCompleted)
				throw new TpException(TP_CODES.DATA_NOT_EXIST);
			mapToQuery = rawStopTimesByTripId;
		}
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		resMap.put(RequestParam.HEADERS, Arrays.asList(gtfsColumHeaders.get(GTFS_FILE.STOP_TIMES.getName())));
		for (int t = 0; t < strAgencyId.length; t++) {
			String key =  strAgencyId[t];					
			resMap.put(key, mapToQuery.get(key));
		}
		return resMap;
	}
	public File getStopTimesAll() throws TpException, IOException {		
		if(!gtfsReadCompleted)
			throw new TpException(TP_CODES.DATA_NOT_EXIST);
		File file = new File(allStopTimesZipFile);
		synchronized (allStopTimesZipFile) {
			if(!file.exists()){
				TPResponse response = ResponseUtil.createResponse(TP_CODES.SUCESS);
				response.setData(rawStopTimesByTripId);
				String res =  JSONUtil.getResponseJSON(response);
				ZipUtil.writeStopTimes(file,res);
			}
		}
		return file;
	}
	/**
	 * Gets the trips by route id.
	 *
	 * @param strAgencyIdAndRouteId the str agency id and route id
	 * @return the trips by route id
	 * @throws TpException the tp exception
	 */
	public Map<String, List<String>> getTripsByRouteId(String[] strAgencyIdAndRouteId) throws TpException {		
		Map<String, List<String>> mapToQuery  = null;
		if(!useInMemoryGtfs){
			String[] ids = GtfsUtils.getAgencyIdsFromTripIdCombo(strAgencyIdAndRouteId);
			mapToQuery =   createIndexForColumnWithAgency(ids,GTFS_FILE.TRIPS,"trip_id");
		}else{
			if(!gtfsReadCompleted)
				throw new TpException(TP_CODES.DATA_NOT_EXIST);
			mapToQuery = rawTripDataByRouteId;
		}
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		resMap.put(RequestParam.HEADERS, Arrays.asList(gtfsColumHeaders.get(GTFS_FILE.TRIPS.getName())));
		for (int t = 0; t < strAgencyIdAndRouteId.length; t++) {
			String key =  strAgencyIdAndRouteId[t];					
			resMap.put(key, mapToQuery.get(key));
		}
		return resMap;
	}

	/**
	 * Gets the nimbler agency details for app.
	 *
	 * @param appType the app type
	 * @return the nimbler agency details for app
	 * @throws TpException the tp exception
	 */
	public List<NimblerGtfsAgency> getNimblerAgencyDetailsForApp(Integer appType,boolean extended) throws TpException {
		Map<Integer, String> map = nimblerApps.getAppRouteSupportAgencies();
		String agencies = map.get(appType);
		if(agencies==null)
			throw new TpException("Invalid app type");
		String[] arrAgencies = agencies.split(",");		
		List<NimblerGtfsAgency> nimblerGtfsAgencies = new ArrayList<NimblerGtfsAgency>();
		List<GtfsBundle> lstBundles =  gtfsContext.getGtfsBundles();
		for (GtfsBundle bundle : lstBundles) {		
			if(ArrayUtils.contains(arrAgencies, bundle.getAgencyType().ordinal()+"")){
				NimblerGtfsAgency nimblerGtfsAgency =  GtfsUtils.getAgencyDetail(bundle,extended);
				if(bundle.getAdvisoryName()!=null)
					nimblerGtfsAgency.setAdvisoryName(bundle.getAdvisoryName());			
				nimblerGtfsAgencies.add(nimblerGtfsAgency);
			}
		}
		return nimblerGtfsAgencies;
	}

	/**
	 * Gets the column index of gtfs file.
	 *
	 * @param file the file
	 * @param columnName the column name
	 * @return the column index of gtfs file
	 */
	public int getColumnIndexOfGtfsFile(GTFS_FILE file,String columnName) {
		return ArrayUtils.indexOf(gtfsColumHeaders.get(file.getName()), columnName);
	}

	public GtfsContext getGtfsContext() {
		return gtfsContext;
	}

	public void setGtfsContext(GtfsContext gtfsContext) {
		this.gtfsContext = gtfsContext;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	public Map<String, GtfsBundle> getBundleMap() {
		return bundleMap;
	}
	public void setBundleMap(Map<String, GtfsBundle> bundleMap) {
		this.bundleMap = bundleMap;
	}
	public Map<String, String[]> getGtfsColums() {
		return gtfsColumHeaders;
	}
	public void setGtfsColums(Map<String, String[]> gtfsColums) {
		this.gtfsColumHeaders = gtfsColums;
	}


	public LoggingService getLoggingService() {
		return loggingService;
	}


	public void setLoggingService(LoggingService loggingService) {
		this.loggingService = loggingService;
	}

	public String getAgeciesToLoad() {
		return ageciesToLoad;
	}

	public void setAgeciesToLoad(String ageciesToLoad) {
		this.ageciesToLoad = ageciesToLoad;
	}

	public boolean isUseInMemoryGtfs() {
		return useInMemoryGtfs;
	}

	public void setUseInMemoryGtfs(boolean useInMemoryGtfs) {
		this.useInMemoryGtfs = useInMemoryGtfs;
	}


	public int getMAX_EARLY_MIN() {
		return MAX_EARLY_MIN;
	}

	public void setMAX_EARLY_MIN(int mAX_EARLY_MIN) {
		MAX_EARLY_MIN = mAX_EARLY_MIN;
	}

	public int getMAX_DELAY_MIN() {
		return MAX_DELAY_MIN;
	}

	public void setMAX_DELAY_MIN(int mAX_DELAY_MIN) {
		MAX_DELAY_MIN = mAX_DELAY_MIN;
	}

	public List<BartRouteInfo> getBartRouteInfo() {
		return bartRouteInfo;
	}

	public void setBartRouteInfo(List<BartRouteInfo> bartRouteInfo) {
		this.bartRouteInfo = bartRouteInfo;
	}

	public RouteStopIndex getBartRouteStops() {
		return bartRouteStops;
	}

	public void setBartRouteStops(RouteStopIndex bartRouteStops) {
		this.bartRouteStops = bartRouteStops;
	}

	public TripStopIndex getTripStopIndex() {
		return tripStopIndex;
	}

	public void setTripStopIndex(TripStopIndex tripStopIndex) {
		this.tripStopIndex = tripStopIndex;
	}

	public BidiMap getAcTransitTripIdMapping() {
		return acTransitTripIdMapping;
	}

	public void setAcTransitTripIdMapping(BidiMap acTransitTripIdMapping) {
		this.acTransitTripIdMapping = acTransitTripIdMapping;
	}
}
