package web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.common.StopNotFoundException;
import com.nimbler.tp.dataobject.wmata.BusStop;
import com.nimbler.tp.dataobject.wmata.GtfsStop;
import com.nimbler.tp.dataobject.wmata.GtfsStop.GTFS_STOP_TYPE;
import com.nimbler.tp.dataobject.wmata.Point;
import com.nimbler.tp.dataobject.wmata.RailLine;
import com.nimbler.tp.dataobject.wmata.RailPrediction;
import com.nimbler.tp.dataobject.wmata.RailStation;
import com.nimbler.tp.dataobject.wmata.StopMapping;
import com.nimbler.tp.dataobject.wmata.WmataBusPrediction;
import com.nimbler.tp.gtfs.StopMapBuilder;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.livefeeds.stub.WmataApiClient;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.GtfsUtils;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.ETA_FLAG;
import com.nimbler.tp.util.TpConstants.GTFS_FILE;
import com.nimbler.tp.util.TpException;
import com.nimbler.tp.util.WmataUtil;

public class TestWmataApi {

	private static String BASE_DIRECTORY = "c:/wmata_data/";
	private static String API_BUS_STOP_DATA_FILE = BASE_DIRECTORY+"api_bus_stop_data.obj";
	private static String API_RAIL_STATION_DATA_FILE = BASE_DIRECTORY+"api_rail_station_data.obj";

	private static String FILTERED_GTFS_MAPPING_FILE = BASE_DIRECTORY+"filtered_gtfs_mapping.obj";

	private static String STOP_BINATY_FILE = BASE_DIRECTORY+"StopMap.obj";

	static String WMATA_GTFS_FILE = "c:/OTP_/GTFS/wmata.zip";

	static String WMATA_API_KEY = "wateq3gxqzb9s597qky6khd7";
	private static String[] RAIL_ROUTES = new String[]{"Blue","Green","Orange","Red","Yellow"};

	static WmataApiClient apiClient = new WmataApiClient();
	static GtfsUtils gtfsUtils = null;
	private static Map<String, String> busDirectionOverride = new HashMap<String, String>();
	static SimpleDateFormat dateFormat = new SimpleDateFormat();
	static StopMapping mapping = null;
	static long lateThreshold = 3;
	static long earlyThreshold = 1;
	static int count = 0;

	private static void init() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("GET_STOP", "Bus.svc/json/JStops");
		map.put("BUS_PREDICTION", "NextBusService.svc/json/JPredictions");
		map.put("RAIL_LINES", "Rail.svc/json/JLines");
		map.put("RAIL_STATION_BY_LINES", "Rail.svc/json/JStations");
		map.put("RAIL_PREDICTION", "StationPrediction.svc/json/GetPrediction/");
		apiClient.init();
		apiClient.setLogger(new LoggingService());
		apiClient.setRouteMap(map);
		gtfsUtils = new GtfsUtils(new LoggingService(),"com.nimbler.tp.service.livefeeds");
		busDirectionOverride.put("0", "1");
		busDirectionOverride.put("1", "0");
		dateFormat.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		//		mapping = (StopMapping) read(STOP_BINATY_FILE);
	}
	public static void main(String[] args) {
		try {
			StopMapBuilder.main(new String[]{WMATA_API_KEY,BASE_DIRECTORY,WMATA_GTFS_FILE});
			//			init();
			//			find_non_matched_rail_stations();
			//			startGtfsMapping();
			//			validateStops();
			//						validateRailStops();

			//			String[] bus = "28904,5662,36,1358854476000".split(",");
			//			System.out.println(DistanceLibrary.distance(38.887656,-77.01321,38.887475,-77.012344));
			//			realTimeBusTest(bus[0],bus[1],bus[2],NumberUtils.toLong(bus[3]));
			//			testRailRealTime("1305","Red","35229",1358771160000l);
			//			testRailRealTime("4710,Red,37018,1358775780000");
			//			checkAPI();

		} catch (Exception e) {
			if(e instanceof RealTimeDataException)
				System.err.println(e.getMessage());
			else
				e.printStackTrace();
		}
	}
	private static void checkAPI() {
		ExecutorService executorService = Executors.newFixedThreadPool(20);
		for (int i = 0; i < 9; i++) {

			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						//						for (int i = 0; i < 5; i++) {
						apiClient.getRailLines(WMATA_API_KEY);
						count++;
						//						}
					} catch (RealTimeDataException e) {
						e.printStackTrace();
					}
				}
			});
		}
		executorService.shutdown();
		while (!executorService.isTerminated()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			System.out.println("waiting..."+count);
		}
	}
	private static void realTimeBusTest(String gtfsFromStop,String gtfsTripId,String routeTag,long scheduledTime) throws RealTimeDataException, StopNotFoundException {
		boolean _verbose = true;
		if(_verbose){
			System.out.println("Current Time  : "+dateFormat.format(new Date()));
			System.out.println("Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		}
		MutablePair<String, String> stop_head = mapping.getTripLastStopAndHead().get(gtfsTripId);

		String directionId = busDirectionOverride.get(stop_head.getRight());

		if(ComUtils.isEmptyString(directionId))
			throw new RealTimeDataException("No Direction ID Found : "+stop_head.getRight());
		System.out.println("directionId: "+stop_head.getRight()+"->"+directionId);
		List<BusStop> lstBusStops =  WmataUtil.getBusStopIdFromGtfsStop(gtfsFromStop,routeTag,mapping);
		System.out.println("Total Stops: "+lstBusStops.size());
		List<WmataBusPrediction> lstPredictions =  new ArrayList<WmataBusPrediction>();
		for (BusStop busStop : lstBusStops) {
			System.out.println("Getting Real time prediction for stop:"+busStop.getStopId()+" - "+busStop.getName());
			try {
				List<WmataBusPrediction> lst = apiClient.getBusPredictionAtStop(WMATA_API_KEY, busStop.getStopId());
				lstPredictions.addAll(lst);
			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
			}
		}
		System.out.println("Total Prediction To filter: "+lstPredictions.size());
		for (Iterator<WmataBusPrediction> iterator = lstPredictions.iterator(); iterator.hasNext();) {
			WmataBusPrediction predictions = iterator.next();
			if(!StringUtils.equalsIgnoreCase(predictions.getRouteID(),routeTag) || !StringUtils.equalsIgnoreCase(predictions.getDirectionNum(),directionId))
				iterator.remove();
		}
		if(ComUtils.isEmptyList(lstPredictions))
			throw new RealTimeDataException("No Matching Prediction Found For GtfsStop:"+gtfsFromStop);

		if(_verbose ){
			System.out.println("Filtered---->");
			for (WmataBusPrediction p : lstPredictions) {
				System.out.println("       Route: "+p.getRouteID()+", Direction:"+p.getDirectionText()+"("+p.getDirectionNum()+"), VehicleID: "+p.getVehicleID()+", Min: "+p.getMinutes());
			}
		}

		MutablePair<Integer, Long> closestMatch = WmataUtil.getClosestEstimationForBus(lstPredictions, scheduledTime, 0 ,5,30);
		if(closestMatch==null)
			throw new RealTimeDataException("No Closest Match Found For GtfsStop:"+gtfsFromStop);

		System.out.println("ClosestMatch: "+(closestMatch.getLeft()/DateUtils.MILLIS_PER_MINUTE)+","+dateFormat.format(closestMatch.getRight())+"\n");

		int arrivalFlag = ETA_FLAG.ON_TIME.ordinal();
		long estimatedDepartureTime = closestMatch.getRight();
		int diff = closestMatch.getLeft();
		if (diff>(lateThreshold *DateUtils.MILLIS_PER_MINUTE)){
			arrivalFlag = TpConstants.ETA_FLAG.DELAYED.ordinal();
		}else if (diff<0 && Math.abs(diff) >(earlyThreshold *DateUtils.MILLIS_PER_MINUTE)){
			arrivalFlag = TpConstants.ETA_FLAG.EARLY.ordinal();
		}
		System.out.println("Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		System.out.println("Estimated time: "+dateFormat.format(new Date(estimatedDepartureTime))+"  "+ETA_FLAG.values()[arrivalFlag].name());
		System.out.println("Min : "+((estimatedDepartureTime-scheduledTime)/1000)/60);

	}
	private static void find_non_matched_rail_stations() throws IOException, ParseException, TpException, ClassNotFoundException, StopNotFoundException {
		StopMapping stopMapping = (StopMapping) read(STOP_BINATY_FILE);

		System.out.println("start..");
		//		List<String> lst = gtfsUtils.getColumnsFromFile(new File(WMATA_GTFS_FILE),new String[]{"route_id","trip_id"} ,GTFS_FILE.TRIPS.getFileName());
		//
		//		Map<String, String> trip_route = new HashMap<String, String>();
		//		for (String tr : lst) {
		//			if(isRailRoute(tr.split(",")[0]))
		//				trip_route.put(tr.split(",")[1], tr.split(",")[0]);
		//		}
		//		System.out.println("stops..");
		//		lst = gtfsUtils.getColumnsFromFile(new File(WMATA_GTFS_FILE),new String[]{"trip_id","stop_id"} ,GTFS_FILE.STOP_TIMES.getFileName());
		//		Map<String, String> trip_stop = new HashMap<String, String>();
		//		for (String tr : lst) {
		//			if(trip_route.containsKey(tr.split(",")[0]))
		//				trip_stop.put(tr.split(",")[0], tr.split(",")[1]);
		//		}
		//		System.out.println(trip_route.size());
		//		System.out.println(trip_stop.size());
		//		write("c:/temp", trip_route,trip_stop);
		//==========================================================================================
		ObjectInputStream outputStream = new ObjectInputStream(new FileInputStream("c:/temp"));
		Map<String, String> trip_route = (Map<String, String>) outputStream.readObject();
		Map<String, String> trip_stop = (Map<String, String>) outputStream.readObject();
		int c = 0;
		int t = 0;
		for (Map.Entry<String, String> entry : trip_stop.entrySet()) {
			String tripId = entry.getKey();
			String stopId = entry.getValue();
			String routeLine = trip_route.get(tripId);
			try {
				if(routeLine!=null){
					t++;
					List<String> s = 	WmataUtil.getWmataStopFromGtfsStopAndLine(stopId, routeLine, stopMapping);
					//						System.out.+println(s);
				}
			} catch (Exception e) {
				System.out.println(e.getMessage()+", trip Id: "+tripId+",routeId:"+routeLine);
				//					e.printStackTrace();
				c++;
			}

		}
		System.out.println("done.."+c+"/"+t);

	}
	private static void testRailRealTime(String string) throws RealTimeDataException, StopNotFoundException {
		String[] a = string.split(",");
		testRailRealTime(a[0], a[1], a[2], NumberUtils.toLong(a[3]));

	}
	private static void testRailRealTime(String formGrfsStopId,String  routeLine,String  tripId,long scheduledTime) throws RealTimeDataException, StopNotFoundException {
		boolean _verbose= true;
		if(_verbose){
			System.out.println("Current Time  : "+dateFormat.format(new Date()));
			System.out.println("Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		}
		StopMapping stopMapping = (StopMapping) read(STOP_BINATY_FILE);

		String agencyId = "wmata";
		int lateThreshold = 2;
		int earlyThreshold = 1;

		List<String> lstApiFromStop = WmataUtil.getWmataStopFromGtfsStopAndLine(formGrfsStopId,routeLine,stopMapping);
		String apiFromStop = lstApiFromStop.get(0);
		String gtfsTripDestStop = stopMapping.getTripLastStopAndHead().get(tripId).getLeft();
		String tripDestStop = WmataUtil.getWmataStopFromGtfsStopAndLine(gtfsTripDestStop,routeLine,stopMapping).get(0);
		if(_verbose)
			System.out.println("Current/Destination: "+apiFromStop+"-"+tripDestStop);
		List<RailPrediction> estimates = apiClient.getRailPrediction(WMATA_API_KEY, apiFromStop);
		if(ComUtils.isEmptyList(estimates))
			throw new RealTimeDataException("No Valid Estimation found form API");
		for (Iterator iterator = estimates.iterator(); iterator.hasNext();) {
			RailPrediction railPrediction = (RailPrediction) iterator.next();
			if(!StringUtils.equalsIgnoreCase(railPrediction.getDestinationCode(),tripDestStop)){
				iterator.remove();
			}
		}
		if(ComUtils.isEmptyList(estimates))
			throw new RealTimeDataException("No Valid Estimation found after filtering");

		if(_verbose){
			System.out.println("======  ======  ====== filtered =====  ==========  ==========");
			for (RailPrediction rp : estimates) {
				System.out.println("Destination: "+rp.getDestinationName()+"("+rp.getDestination()+")" +
						", Line: "+rp.getLine()+", Min: "+rp.getMin());
			}
			System.out.println("============  =============== ================ =========");
		}

		MutablePair<Integer, Long> closestMatch = WmataUtil.getClosestEstimationForRail(estimates, scheduledTime, 0,5,30);

		if (closestMatch == null)
			throw new RealTimeDataException("Valid minutes to departure not found in response for Agency: "+agencyId+", Stop Tag: "+formGrfsStopId+", Route Tag: "+routeLine);
		System.out.println("ClosestMatch: "+(closestMatch.getLeft()/DateUtils.MILLIS_PER_MINUTE)+","+dateFormat.format(closestMatch.getRight())+"\n");

		int arrivalFlag = ETA_FLAG.ON_TIME.ordinal();
		long estimatedDepartureTime = closestMatch.getRight();
		int diff = closestMatch.getLeft();
		if (diff>(lateThreshold*DateUtils.MILLIS_PER_MINUTE)){
			arrivalFlag = TpConstants.ETA_FLAG.DELAYED.ordinal();
		}else if (diff<0 && Math.abs(diff) >(earlyThreshold*DateUtils.MILLIS_PER_MINUTE)){
			arrivalFlag = TpConstants.ETA_FLAG.EARLY.ordinal();
		}
		System.out.println("Scheduled time: "+dateFormat.format(new Date(scheduledTime)));
		System.out.println("Estimated time: "+dateFormat.format(new Date(estimatedDepartureTime))+"  "+ETA_FLAG.values()[arrivalFlag].name());
		System.out.println("Min : "+((estimatedDepartureTime-scheduledTime)/1000)/60);
	}
	/**
	 * Start gtfs mapping.
	 */
	private static void startGtfsMapping() {
		try {

			StopMapping mapping = new StopMapping();
			readBusAndRailStopsFromGtfs(gtfsUtils,mapping);
			matchGtfsBusStopToApiStops(mapping);
			matchGtfsRailStopToApiStops(mapping);
			System.out.println("Writting Stops...."+STOP_BINATY_FILE);
			write(mapping, STOP_BINATY_FILE);
			System.out.println("Stops written....");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private static void validateStops() {
		long start = System.currentTimeMillis();
		Map<String, GtfsStop> stops = mapping.getBusGtfsStopsById();
		List<GtfsStop> lstStops = new ArrayList<GtfsStop>(stops.values());
		Map<Integer, Integer> counter = new HashMap<Integer, Integer>();

		System.out.println("before....");
		/*for (GtfsStop gtfsStop : lstStops) {
			//			List s = filterStopsByDistance(gtfsStop, new HashSet(gtfsStop.getLstBusStops()), 50);
			if(gtfsStop.getLstBusStops().size()>10){
				System.out.println(gtfsStop.getStopName()+" - "+gtfsStop.getLat()+","+gtfsStop.getLon());
				for (BusStop busStop : gtfsStop.getLstBusStops()) {
					System.out.println("       "+StringUtils.rightPad(busStop.getName(), 30) +" ******* " +
							""+StringUtils.rightPad(busStop.getRoutes().toString(),60)+" - " +
							""+StringUtils.rightPad(busStop.getLat()+","+busStop.getLon(),25)+" - " +
							""+DistanceLibrary.distance(gtfsStop, busStop)
							);
				}
				System.out.println("");
				//				filterStopsByDistance(gtfsStop, new HashSet(gtfsStop.getLstBusStops()), 10);
			}
			//			count(s, counter);
		}*/
		System.out.println(counter);
		for (GtfsStop gtfsStop : lstStops) {
			count(gtfsStop.getLstBusStops(), counter);
		}
		System.out.println(counter);
		System.out.println("after....");
		counter = new HashMap<Integer, Integer>();
		for (GtfsStop gtfsStop : lstStops) {
			List<BusStop> lst = WmataUtil.filterIfOneStopNameMatched(gtfsStop,false);
			count(lst, counter);
		}
		System.out.println(counter);
		long end = System.currentTimeMillis();
		System.out.println("Operation took " + (end - start)  + "msec");

	}




	/**
	 * Gets the bus and rail stops from gtfs.
	 *
	 * @param wmataGtfsFile the wmata gtfs file
	 * @param gtfsUtils the gtfs utils
	 * @param mapping
	 * @return the bus and rail stops from gtfs
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParseException the parse exception
	 * @throws TpException the tp exception
	 * @throws ClassNotFoundException
	 */
	private static void readBusAndRailStopsFromGtfs( GtfsUtils gtfsUtils, StopMapping mapping) throws IOException, ParseException, TpException, ClassNotFoundException {
		if(ComUtils.isFileExist(FILTERED_GTFS_MAPPING_FILE)){
			System.out.println("Reading filtered gtfs file at:"+FILTERED_GTFS_MAPPING_FILE);
			ObjectInputStream outputStream = new ObjectInputStream(new FileInputStream(FILTERED_GTFS_MAPPING_FILE));
			mapping.setBusGtfsStopsById((Map<String, GtfsStop>) outputStream.readObject());
			mapping.setRailGtfsStopsById((Map<String, GtfsStop>) outputStream.readObject());
			mapping.setTripLastStopAndHead( (Map<String, MutablePair<String, String>>) outputStream.readObject());
			System.out.println("done...");
			return;
		}

		File wmataGtfsFile = new File(WMATA_GTFS_FILE);
		if(!wmataGtfsFile.exists())
			throw new TpException("No Gtfs file found..."+wmataGtfsFile.getAbsolutePath());
		System.out.print("Reading Trips...");

		Map<String,MutablePair<String,String>> tripLastStopAndHead = new HashMap<String, MutablePair<String,String>>();
		List<String> lst = gtfsUtils.getColumnsFromFile(wmataGtfsFile,new String[]{"route_id","trip_id","direction_id"} ,GTFS_FILE.TRIPS.getFileName());
		lst.remove(0);
		System.out.println(lst.size());
		Set<String> railTrips = new HashSet<String>();
		Set<String> busTrips = new HashSet<String>();
		for (String data : lst) {
			String[] route_trip = data.split(",");
			if(isRailRoute(route_trip)){
				railTrips.add(route_trip[1]);
			}else{
				busTrips.add(route_trip[1]);
			}
			tripLastStopAndHead.put(route_trip[1],new MutablePair<String, String>(null,route_trip[2]));
		}
		System.out.println("Rail Trips: "+railTrips.size());
		System.out.println("Bus  Trips: "+busTrips.size());

		System.out.print("\nReading stop_times.txt...");

		lst = gtfsUtils.getColumnsFromFile(wmataGtfsFile,new String[]{"trip_id","stop_id","stop_sequence","route_id"} ,GTFS_FILE.STOP_TIMES.getFileName());
		lst.remove(0);
		System.out.println(lst.size());

		Set<String> railStopsSet = new HashSet<String>();
		Set<String> busStopsSet = new HashSet<String>();

		Map<String, MutablePair<String,Integer>> trip_stop_sequence = new HashMap<String, MutablePair<String,Integer>>();
		for (String data : lst) {
			String[] trip_stop = data.split(",");
			MutablePair<String,Integer> mutablePair = trip_stop_sequence.get(trip_stop[0]);
			if(mutablePair==null){
				trip_stop_sequence.put(trip_stop[0], new MutablePair<String,Integer>(trip_stop[1],NumberUtils.toInt(trip_stop[2])));
			}else{
				int offerValue = NumberUtils.toInt(trip_stop[2]);
				if(mutablePair.getRight()<offerValue){
					mutablePair.setRight(offerValue);
					mutablePair.setLeft(trip_stop[1]);
				}
			}
			if(railTrips.contains(trip_stop[0])){
				railStopsSet.add(trip_stop[1]);
			}else if (busTrips.contains(trip_stop[0])){
				busStopsSet.add(trip_stop[1]);
			}else{
				System.out.println("[ERROR]: no trip Found for .... "+trip_stop);
			}
		}

		for (Map.Entry<String, MutablePair<String, Integer>> entry : trip_stop_sequence.entrySet()) {
			String key = entry.getKey();
			MutablePair<String, Integer> mutablePair = entry.getValue();/*stop,sequence*/
			MutablePair<String,String> mp = tripLastStopAndHead.get(key);/*stop,head*/
			if(mp==null){
				System.out.println("MutablePair for trip: "+key);
				continue;
			}

			mp.setLeft(mutablePair.getLeft());
		}
		mapping.setTripLastStopAndHead(tripLastStopAndHead);

		System.out.println("Total Bus  Stops(in stop_times): "+busStopsSet.size());
		System.out.println("Total Rail Stops(Stop stop_times): "+railStopsSet.size());

		Collection commonStops = CollectionUtils.intersection(railStopsSet, busStopsSet);
		if(commonStops!=null && commonStops.size()>0)
			System.out.println("Common bus and rail stops("+commonStops.size()+") : "+commonStops);

		System.out.println("\nReading Stops....");
		lst = gtfsUtils.getColumnsFromFile(wmataGtfsFile,new String[]{"stop_id","stop_name","stop_lat","stop_lon"} ,GTFS_FILE.STOPS.getFileName());
		lst.remove(0);

		Map<String, GtfsStop> busStopMap = new HashMap<String, GtfsStop>();
		Map<String, GtfsStop> railStopMap = new HashMap<String, GtfsStop>();
		for (String data : lst) {
			String[] arrStopData = data.split(",");
			boolean used = false;
			if(busStopsSet.contains(arrStopData[0])){
				busStopMap.put(arrStopData[0], new GtfsStop(arrStopData[0],arrStopData[1],arrStopData[2],arrStopData[3],GTFS_STOP_TYPE.BUS.ordinal()));
				used = true;
			}
			if(railStopsSet.contains(arrStopData[0])){
				railStopMap.put(arrStopData[0], new GtfsStop(arrStopData[0],arrStopData[1],arrStopData[2],arrStopData[3],GTFS_STOP_TYPE.RAIL.ordinal()));
				if(!used)
					used=true;
			}
			if(!used)
				System.out.println("No stop id found...."+arrStopData);
		}
		mapping.setBusGtfsStopsById(busStopMap);
		mapping.setRailGtfsStopsById(railStopMap);
		System.out.println("Total bus  stop: "+busStopMap.size());
		System.out.println("Total rail stop: "+railStopMap.size());
		System.out.println("Writting filtered gtfs data.....");
		write(FILTERED_GTFS_MAPPING_FILE, busStopMap,railStopMap,tripLastStopAndHead);
		System.out.println("Data written...");
	}

	/**
	 * Match bus stops local.
	 *
	 * @param busStopCoordinate <gtfs_stop_id, {@link GtfsStop}>
	 * @throws RealTimeDataException
	 */
	private static void matchGtfsBusStopToApiStops(StopMapping mapping) throws RealTimeDataException {
		System.out.println("\nMapping bus stops....");
		Set<BusStop> lstStopSet = null;
		if(!ComUtils.isFileExist(API_BUS_STOP_DATA_FILE)){
			System.out.println("Getting bus stops from api....");
			List<BusStop> stops = apiClient.getAllStops(WMATA_API_KEY);
			System.out.println("done....."+stops.size());
			lstStopSet = new HashSet<BusStop>(stops);
			System.out.println("Writting bus stop api data....");
			write(stops, API_BUS_STOP_DATA_FILE);
			System.out.println("done...");
		}else{
			System.out.println("Reading bus stop api data....");
			lstStopSet = new HashSet<BusStop>((List<BusStop>) read(API_BUS_STOP_DATA_FILE));
			System.out.println("done...");
		}

		Map<String, BusStop> stopMap = new HashMap<String, BusStop>();
		for (BusStop stop : lstStopSet) {
			stopMap.put(stop.getStopId(), stop);
		}
		mapping.setBusApiStopsById(stopMap);

		Map<String, GtfsStop> gtfsBusStopMap = mapping.getBusGtfsStopsById();
		Map<Integer, Integer> counter = new HashMap<Integer, Integer>();
		int i=0;
		for (GtfsStop gtfsStop : gtfsBusStopMap.values()) {
			i++;
			if(i%100==0)
				System.out.print(i+", ");
			if(i%1000==0)
				System.out.println();
			List<BusStop> lstStops = WmataUtil.filterStopsByDistance(gtfsStop,lstStopSet,100);
			if(ComUtils.isEmptyList(lstStops))
				System.out.println("No matching stops found for: "+gtfsStop);
			gtfsStop.setLstBusStops(lstStops);
			WmataUtil.filterIfOneStopNameMatched(gtfsStop, true);
			count(gtfsStop.getLstBusStops(), counter);
		}
		System.out.println(counter);

	}

	/**
	 * Match gtfs rail stop to api stops.
	 *
	 * @param mapping the mapping
	 * @throws RealTimeDataException
	 */
	private static void matchGtfsRailStopToApiStops(StopMapping mapping) throws RealTimeDataException {
		System.out.println("\nMapping Rail stops....");
		Map<String, GtfsStop> railStopCoordinate = mapping.getRailGtfsStopsById();
		File file = new File(API_RAIL_STATION_DATA_FILE);
		Map<RailLine,List<RailStation>> railStationByLine = null;
		if(!file.exists()){
			System.out.println("Getting rail lines....");
			List<RailLine> railLines = apiClient.getRailLines(WMATA_API_KEY);
			railStationByLine = new HashMap<RailLine, List<RailStation>>();
			for (RailLine railLine : railLines) {
				System.out.println("Getting station for line: "+railLine.getLineCode());
				List<RailStation> railStations = apiClient.getRailStationByLines(WMATA_API_KEY,railLine.getLineCode());
				System.out.println(railLine.getLineCode()+"-"+railStations.size());
				railStationByLine.put(railLine,railStations);
			}
			System.out.println("Writting rail api data....."+API_RAIL_STATION_DATA_FILE);
			write(railStationByLine, API_RAIL_STATION_DATA_FILE);
			System.out.println("done....");
		}else{
			System.out.println("Reading rail api data....."+API_RAIL_STATION_DATA_FILE);
			railStationByLine = (Map<RailLine, List<RailStation>>) read(API_RAIL_STATION_DATA_FILE);
			System.out.println("done...");
		}
		mapping.setRailStationByRailLine(railStationByLine);
		Map<Integer, Integer> counter = new HashMap<Integer, Integer>();
		int i=0;

		Set<RailStation> lstRailStation = getRailStations(railStationByLine);
		for (final GtfsStop gtfsStop : railStopCoordinate.values()) {
			i++;
			if(i%100==0)
				System.out.print(i+", ");
			if(i%500==0)
				System.out.println();

			List<RailStation> lstRailStatios = WmataUtil.filterStopsByDistance(gtfsStop,lstRailStation,100);
			if(ComUtils.isEmptyList(lstRailStatios))
				System.out.println("No matching stops found for: "+gtfsStop);
			count(lstRailStatios, counter);
			gtfsStop.setLstRailStations(lstRailStatios);
		}
		System.out.println(counter);
	}


	private static Set<RailStation> getRailStations(Map<RailLine, List<RailStation>> railStationByLine) {
		Set<List<RailStation>> lists = new HashSet<List<RailStation>>(railStationByLine.values());
		Set<RailStation> res = new HashSet<RailStation>();
		for (List<RailStation> list : lists) {
			res.addAll(list);
		}
		return res;
	}

	private static void count(List mappedStop,Map<Integer, Integer> counter) {
		int size = 0;
		if(mappedStop!=null)
			size = mappedStop.size();
		Integer count = counter.get(size);
		if(count==null){
			count = 0;
		}
		count++;
		counter.put(size, count);
	}

	private static void write(Object stopMap,String path) {
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path));
			outputStream.writeObject(stopMap);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private static void write(String path,Object...obj) {
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path));
			for (int i = 0; i < obj.length; i++) {
				outputStream.writeObject(obj[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Filter stops by distance.
	 *
	 * @param p the p
	 * @param lstStops the lst stops
	 * @param maxDistance the max distance
	 * @return the list
	 */


	class ValueComparator implements Comparator<Point> {

		Map<Point, Double> base;
		public ValueComparator(Map<Point, Double> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with equals.
		public int compare(Point a, Point b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}
	private static boolean isRailRoute(String[] route_trip) {
		return isRailRoute(route_trip[0]);
	}
	private static boolean isRailRoute(String route) {
		for (String rail : RAIL_ROUTES) {
			if(rail.equalsIgnoreCase(route))
				return true;
		}
		return false;
	}

	private static Object read(String path) {
		try {
			ObjectInputStream outputStream = new ObjectInputStream(new FileInputStream(path));
			return outputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
