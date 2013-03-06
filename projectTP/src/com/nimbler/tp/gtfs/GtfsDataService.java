/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.GtfsUtils;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.TpConstants.GTFS_FILE;
import com.nimbler.tp.util.TpException;

public class GtfsDataService {

	@Autowired
	private LoggingService loggingService;
	@Autowired
	private GtfsContext gtfsContext;

	/**
	 * AGENCY_TYPE, BUNDLE
	 */
	private Map<String, GtfsBundle> bundleMap = new HashMap<String, GtfsBundle>();

	/**
	 * file name,columns array
	 */
	private Map<String, String[]> gtfsColums;
	private Map<String, List<String>> stopTimesByTripId = new HashMap<String, List<String>>();

	/**
	 * <agencyOrdinal_routeid,list of data>
	 */
	private Map<String, List<String>> tripsByRouteId = new HashMap<String, List<String>>();

	private String loggerName = GtfsDataService.class.getName();

	private Map<String,List<String>> gtfsData = null;

	private String ageciesToLoad ="1,2,3,4";
	private boolean gtfsReadCompleted = false;

	private boolean useInMemoryGtfs = true;

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

	private void readGtfsData() {
		System.out.println("Reading Gtfs Data...");
		gtfsData = new HashMap<String,List<String>>();
		String[] arrAgencies = ageciesToLoad.split(",");
		long start1 = System.currentTimeMillis();
		for (GTFS_FILE file : GTFS_FILE.values()) {
			if(file.ordinal()==0)
				continue;
			try {
				long start = System.currentTimeMillis();
				if(file.equals(GTFS_FILE.STOP_TIMES)){
					Map<String, List<String>> temp =  createIndexForColumnWithAgency(arrAgencies,file,"trip_id");
					stopTimesByTripId.putAll(temp);
				}if(file.equals(GTFS_FILE.TRIPS)){
					Map<String, List<String>> temp =  createIndexForColumnWithAgency(arrAgencies,file,"route_id");
					tripsByRouteId.putAll(temp);
				}else{
					Map<String, List<String>> temp =  readGtfsDataByAgency(file,arrAgencies);
					gtfsData.putAll(temp);
				}
				long end = System.currentTimeMillis();
				System.out.println("read done for: "+file.getFileName()+", Time:"+(end - start) / 1000+ " sec");
			} catch (TpException e) {
				loggingService.warn(loggerName,"Error While Reading File:"+file+", "+ e.getMessage());
			}
		}
		long end1 = System.currentTimeMillis();
		gtfsReadCompleted = true;
		System.out.println("All Gtfs read done in "+ (end1 - start1) / 1000 + "sec");
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
		try {
			GtfsUtils gtfsUtils= new GtfsUtils(loggingService, loggerName);
			for (int i = 0; i < agencyIdOrdinals.length; i++) {
				GtfsBundle bundle = bundleMap.get(agencyIdOrdinals[i]);
				if(bundle==null){
					System.out.println("Could not find bundle for: "+agencyIdOrdinals[i]);
					continue;
				}
				try {
					List<String> lstData = gtfsUtils.getColumnsFromFile(new File(bundle.getValidFile()),gtfsColums.get(gtfsFile.getName()), gtfsFile.getFileName());
					if(lstData!=null)
						resMap.put(agencyIdOrdinals[i]+"_"+gtfsFile.getName(), lstData);
				} catch (TpException e) {
					loggingService.error(loggerName, e.getMessage());
				} catch (Exception e) {
					loggingService.error(loggerName,"Error While reading agency:"+agencyIdOrdinals[i]+" for file:"+gtfsFile, e);
				}
			}
		} catch (Exception e) {
			throw new TpException(e.getMessage());
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
				List<String> lstData = gtfsUtils.getColumnsFromFile(new File(bundle.getValidFile()),gtfsColums.get(gtfsFile.getName()), gtfsFile.getFileName());
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
				List<String> lstData = gtfsUtils.getColumnsFromFile(new File(bundle.getValidFile()),gtfsColums.get(gtfsFile.getName()), gtfsFile.getFileName());
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
			mapToQuery = stopTimesByTripId;
		}
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		resMap.put(RequestParam.HEADERS, Arrays.asList(gtfsColums.get(GTFS_FILE.STOP_TIMES.getName())));
		for (int t = 0; t < strAgencyId.length; t++) {
			String key =  strAgencyId[t];
			resMap.put(key, mapToQuery.get(key));
		}
		return resMap;
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
			mapToQuery = tripsByRouteId;
		}
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		resMap.put(RequestParam.HEADERS, Arrays.asList(gtfsColums.get(GTFS_FILE.TRIPS.getName())));
		for (int t = 0; t < strAgencyIdAndRouteId.length; t++) {
			String key =  strAgencyIdAndRouteId[t];
			resMap.put(key, mapToQuery.get(key));
		}
		return resMap;
	}

	/**
	 * Gets the column index of gtfs file.
	 *
	 * @param file the file
	 * @param columnName the column name
	 * @return the column index of gtfs file
	 */
	public int getColumnIndexOfGtfsFile(GTFS_FILE file,String columnName) {
		return ArrayUtils.indexOf(gtfsColums.get(file.getName()), columnName);
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
		return gtfsColums;
	}
	public void setGtfsColums(Map<String, String[]> gtfsColums) {
		this.gtfsColums = gtfsColums;
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
}
