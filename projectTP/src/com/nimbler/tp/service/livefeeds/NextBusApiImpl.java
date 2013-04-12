/*
 * @author nirmal
 */
package com.nimbler.tp.service.livefeeds;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
import com.nimbler.tp.dataobject.RealTimePrediction;
import com.nimbler.tp.dataobject.nextbus.Direction;
import com.nimbler.tp.dataobject.nextbus.NextBusResponse;
import com.nimbler.tp.dataobject.nextbus.Prediction;
import com.nimbler.tp.dataobject.nextbus.Predictions;
import com.nimbler.tp.gtfs.GtfsDataService;
import com.nimbler.tp.gtfs.TripStopIndex;
import com.nimbler.tp.service.livefeeds.cache.NextBusPredictionCache;
import com.nimbler.tp.util.GtfsUtils;
import com.nimbler.tp.util.PlanUtil;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;
/**
 * Implementation for getting real time data from NextBus real time API for specific leg.
 * @author nIKUNJ
 *
 */
public class NextBusApiImpl implements RealTimeAPI {

	private static final boolean _verbose = false;

	private Map<String, String> agencyMap;

	private Multimap<String, String> orphanGtfsRouteTag = HashMultimap.create();

	private int timeDiffercenceInMin;
	@Autowired
	GtfsDataService gtfsDataService;

	enum NEXTBUS_API_NAME{
		AC_TRANSIT("actransit"),
		SF_MUNI("sf-muni");
		private NEXTBUS_API_NAME(String name){
			this.name = name;
		}
		String name;
		public String getName() {
			return name;
		}
	}


	@Override
	public LegLiveFeed getLiveFeeds(Leg leg) throws FeedsNotFoundException {
		LegLiveFeed resp = null;
		try {
			//System.out.println(leg.getTripId()+"-->"+new Date(leg.getStartTime()));
			Long scheduledTime = leg.getStartTime();
			String agencyId = leg.getAgencyId();
			String agencyTag = agencyMap.get(agencyId);
			if (agencyTag==null)
				throw new RealTimeDataException("Agency not supported for Real Time feeds: "+agencyId);

			String fromStopTag = leg.getFrom().getStopId().getId();
			String toStopTag = leg.getTo().getStopId().getId();
			String routeTag = leg.getRoute();
			routeTag = NBInMemoryDataStore.getInstance().getRouteTag(agencyTag, routeTag);
			NextBusResponse respBody = NextBusPredictionCache.getInstance().getPrediction(agencyTag, routeTag, fromStopTag);
			List<Predictions> predictionsList = respBody.getPredictions();
			if (predictionsList==null || predictionsList.size()==0)
				throw new RealTimeDataException("Prediction results not found for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);

			Predictions predictions = predictionsList.get(0);			
			List<Direction> directions = predictions.getDirection();			
			if (directions==null)
				throw new RealTimeDataException("Directions not found in Prediction response " +
						"for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);

			if(_verbose){
				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				System.out.println("From Stop: "+leg.getFrom().getName()+"("+leg.getFrom().getStopId().getId()+") -- To Stop: "+leg.getTo().getName()+"("+leg.getTo().getStopId().getId()+") -- Route: "+routeTag);
				System.out.println("Scheduled: "+ scheduledTime+" -- > "+new Date(scheduledTime));
			}
			OUTER: 
				for (Direction direction : directions) {				
					List<Prediction> predictionList = direction.getPrediction();
					if (predictionList==null || predictionList.size()==0)
						throw new RealTimeDataException("Predictions objects not found in Prediction response " +
								"for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);	
					for (Prediction prediction : predictionList) {
						//if (prediction.getTripTag().equalsIgnoreCase(leg.getTripId())) {	//removed for actransit with substring trip id match					
						if (isTripIdSame(prediction, leg, agencyTag)) {						
							resp = new LegLiveFeed();
							Long predictedTime = prediction.getEpochTime();
							if(_verbose){								
								System.out.println("Predicted: "+ predictedTime+" -- > "+new Date(predictedTime)+" ("+prediction.getMinutes()+")");
								System.out.println("Direction: "+leg.getHeadsign()+"-->"+direction.getTitle());
								System.out.println("");
							}
							if (scheduledTime < predictedTime) {
								int diff = (int) (predictedTime - scheduledTime);
								diff = diff /( 1000 * 60);
								if (diff > timeDiffercenceInMin)
									resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.DELAYED.ordinal());
								else
									resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.ON_TIME.ordinal());
								if(_verbose)
									System.out.println("Difference: "+diff);
								resp.setTimeDiffInMins(diff);
							} else {
								int diff = (int) (scheduledTime - predictedTime);
								diff = diff /( 1000 * 60);
								if (diff > timeDiffercenceInMin)
									resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.EARLY.ordinal());
								else
									resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.ON_TIME.ordinal());

								resp.setTimeDiffInMins(diff); 
							}
							resp.setLeg(leg);
							resp.setDepartureTime(predictedTime);
							PlanUtil.setArrivalTime(resp);
							break OUTER;
						}else if(_verbose){
							System.out.println("Non Matched: "+prediction.finePrint());
						}
					}
				}
			if (resp == null ) {				
				//				System.err.println("Real time feeds not found for Trip: "+leg.getTripId()+", From: "+leg.getFrom()+", To: "
				//						+leg.getTo()+", Starting at: "+new Date(leg.getStartTime())+"-->"+sb.toString()+
				//						","+ "Direction: "+leg.getHeadsign());
				throw new RealTimeDataException("Real time feeds not found for Trip: "+leg.getTripId()+", From: "+fromStopTag+", To: "
						+toStopTag+", Starting at: "+new Date(leg.getStartTime())); 
			}
			return resp;
		} catch (RealTimeDataException e) {
			throw new FeedsNotFoundException(e.getMessage());  
		} catch (Exception e) {
			throw new FeedsNotFoundException("Unknown Exception: "+e);
		}
	}

	/* (non-Javadoc)
	 * @see com.nimbler.tp.service.livefeeds.RealTimeAPI#getRealTimeFeeds(com.nimbler.tp.dataobject.Leg)
	 */
	@Override
	public LegLiveFeed getAllRealTimeFeeds(Leg leg) throws FeedsNotFoundException {
		LegLiveFeed resp = new LegLiveFeed();
		List<RealTimePrediction> lstRealTimePredictions = new ArrayList<RealTimePrediction>();
		try {			
			Long scheduledTime = leg.getStartTime();
			String agencyId = leg.getAgencyId();
			String routeName = leg.getRoute();

			List<Direction> directions = getPredictionForLeg(leg,false).getDirection();
			String agencyTag = agencyMap.get(agencyId);
			String logData = ", Agency: "+agencyId+",From Stop: "+leg.getFrom()+", Route Tag: "+leg.getRoute()+", RouteSortName: "+routeName;
			if(_verbose){
				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				System.out.println("From Stop: "+leg.getFrom().getStopId()+" -- To Stop: "+leg.getTo().getStopId()+" -- Route: "+routeName);
				System.out.println("   Scheduled: "+ scheduledTime+" -- > "+new Date(scheduledTime));
			}
			for (Direction direction : directions) {				
				List<Prediction> predictionList = direction.getPrediction();
				if (predictionList==null || predictionList.size()==0)
					throw new RealTimeDataException("RealTimePrediction objects not found in Prediction response " +logData);	
				for (Prediction prediction : predictionList) {					
					if (isDirectionMatch(prediction,leg,agencyTag)) {
						lstRealTimePredictions.add(new RealTimePrediction(prediction));
						if(_verbose){
							Long predictedTime = prediction.getEpochTime();
							System.out.println("   Predicted: "+ predictedTime+" -- > "+new Date(predictedTime)+" ("+prediction.getMinutes()+"),   Direction: "+leg.getHeadsign()+"-->"+direction.getTitle());
						}
					}else if(_verbose){
						Long predictedTime = prediction.getEpochTime();
						System.out.println(" X-Predicted: "+ predictedTime+" -- > "+new Date(predictedTime)+" ("+prediction.getMinutes()+"),   Direction: "+leg.getHeadsign()+"-->"+direction.getTitle());					
					}
				}
			}
			if(_verbose)
				System.out.println("");
			if(lstRealTimePredictions.isEmpty()){
				throw new FeedsNotFoundException("No Valid feeds found in response");
			}
			resp.setEmptyLeg(leg);
			resp.setLstPredictions(lstRealTimePredictions);
			setScheduleTimesInPredisction(lstRealTimePredictions,leg,agencyTag);
			return resp;
		} catch (RealTimeDataException e) {
			throw new FeedsNotFoundException(e.getMessage());  
		} catch (Exception e) {
			throw new FeedsNotFoundException("Unknown Exception: "+e);
		}
	}

	/**
	 * Checks if is direction match.
	 *
	 * @param prediction the direction
	 * @param leg the headsign
	 * @param agencyTag the agency tag
	 * @return true, if is direction match
	 */
	private boolean isDirectionMatch(Prediction prediction, Leg leg, String agencyTag) {
		TripStopIndex tripStopIndex =  gtfsDataService.getTripStopIndex();
		String trip =  prediction.getTripTag();
		String from =  leg.getFrom().getStopId().getId();
		String to = leg.getTo().getStopId().getId();
		boolean res = false;
		if(equalsIgnoreCase(agencyTag, NEXTBUS_API_NAME.AC_TRANSIT.getName())){
			res = tripStopIndex.isValidStopsForTrip(AGENCY_TYPE.AC_TRANSIT,trip, from, to);
		}else if (equalsIgnoreCase(agencyTag, NEXTBUS_API_NAME.SF_MUNI.getName())){
			res= tripStopIndex.isValidStopsForTrip(AGENCY_TYPE.SFMUNI,trip, from, to);
		}
		return res;
	}
	private boolean isTripIdSame(Prediction prediction, Leg leg, String agencyTag) {
		String realTimeTripId =  prediction.getTripTag();		
		String tripID = leg.getTripId();
		if(equalsIgnoreCase(agencyTag, NEXTBUS_API_NAME.AC_TRANSIT.getName()))
			tripID = GtfsUtils.getACtransitGtfsTripIdFromApiId(tripID);
		return equalsIgnoreCase(tripID, realTimeTripId);
	}

	/**
	 * Gets the prediction for leg.
	 *
	 * @param leg the leg
	 * @return the prediction for leg
	 * @throws RealTimeDataException the real time data exception
	 */
	private Predictions getPredictionForLeg(Leg leg,boolean forArrival) throws RealTimeDataException {
		String agencyId = leg.getAgencyId();
		String agencyTag = agencyMap.get(agencyId);
		if (agencyTag==null)
			throw new RealTimeDataException("Agency not supported for Real Time feeds: "+agencyId);

		String fromStopTag = leg.getFrom().getStopId().getId();
		String toStopTag = leg.getTo().getStopId().getId();
		String routeName = leg.getRoute();
		String stopId = forArrival?toStopTag:fromStopTag;

		String routeTag = NBInMemoryDataStore.getInstance().getRouteTag(agencyTag, routeName);
		String logData = ", Agency: "+agencyId+",ForArrival"+forArrival+", Stop Tag: "+stopId+", Route Tag: "+routeTag+", RouteSortName: "+routeName;

		if(routeTag==null){
			orphanGtfsRouteTag.put(agencyId, routeName);
			throw new RealTimeDataException("No route tag found from api for matching gtfs"+logData);
		}
		if(orphanGtfsRouteTag.containsEntry(agencyId, routeName))
			orphanGtfsRouteTag.remove(agencyId, routeName);

		NextBusResponse respBody = NextBusPredictionCache.getInstance().getPrediction(agencyTag, routeTag, stopId);
		List<Predictions> predictionsList = respBody.getPredictions();			
		if (predictionsList==null || predictionsList.size()==0){				
			if(respBody.getError()!=null)
				throw new RealTimeDataException(respBody.getError().toString());
			throw new RealTimeDataException("No Prediction"+ logData);
		}

		Predictions predictions = predictionsList.get(0);	
		if(predictions.getDirTitleBecauseNoPredictions()!=null){
			throw new RealTimeDataException("DirTitleBecauseNoPredictions:"+predictions.getDirTitleBecauseNoPredictions()+logData);
		}
		List<Direction> directions = predictions.getDirection();			
		if (directions==null)
			throw new RealTimeDataException("Directions not found "+logData);
		return predictions;
	}

	@Override
	public LegLiveFeed getLegArrivalTime(Leg leg) throws FeedsNotFoundException {
		LegLiveFeed resp = new LegLiveFeed();
		List<RealTimePrediction> lstRealTimePredictions = new ArrayList<RealTimePrediction>();
		try {			
			Long scheduledTime = leg.getStartTime();
			String agencyId = leg.getAgencyId();
			String routeName = leg.getRoute();

			List<Direction> directions = getPredictionForLeg(leg,true).getDirection();
			String agencyTag = agencyMap.get(agencyId);
			String logData = ", Agency: "+agencyId+",To Stop: "+leg.getTo()+", Route Tag: "+leg.getRoute()+", RouteSortName: "+routeName;
			OUTER:
				for (Direction direction : directions) {				
					List<Prediction> predictionList = direction.getPrediction();
					if (predictionList==null || predictionList.size()==0)
						throw new RealTimeDataException("RealTimePrediction objects not found in Prediction response " +logData);	
					if (direction.getTitle().toLowerCase().contains(leg.getHeadsign().toLowerCase())) {
						//TODO match trip ids
						for (Prediction prediction : predictionList) {	
							if(prediction.getTripTag().equalsIgnoreCase(leg.getTripId())){
								lstRealTimePredictions.add(new RealTimePrediction(prediction));							
								if(_verbose){
									Long predictedTime = prediction.getEpochTime();
									System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
									System.out.println("From Stop: "+leg.getFrom().getStopId()+" -- To Stop: "+leg.getTo().getStopId()+" -- Route: "+routeName);
									System.out.println("Scheduled: "+ scheduledTime+" -- > "+new Date(scheduledTime));
									System.out.println("Predicted: "+ predictedTime+" -- > "+new Date(predictedTime)+" ("+prediction.getMinutes()+")");
									System.out.println("Direction: "+leg.getHeadsign()+"-->"+direction.getTitle());
									System.out.println("");
								}
							}
							break OUTER;
						}				
					}
				}
			if(lstRealTimePredictions.isEmpty()){
				throw new FeedsNotFoundException("No Valid feeds found in response");
			}
			resp.setEmptyLeg(leg);
			resp.setLstPredictions(lstRealTimePredictions);
			setScheduleTimesInPredisction(lstRealTimePredictions,leg,agencyTag);
			return resp;
		} catch (RealTimeDataException e) {
			throw new FeedsNotFoundException(e.getMessage());  
		} catch (Exception e) {
			throw new FeedsNotFoundException("Unknown Exception: "+e);
		}
	}



	/**
	 * Sets the schedule times in predisction.
	 *
	 * @param lstRealTimePredictions the lst real time predictions
	 * @param leg the leg
	 * @param agencyTag the agency tag
	 */
	private void setScheduleTimesInPredisction(	List<RealTimePrediction> lstRealTimePredictions, Leg leg, String agencyTag) {
		AGENCY_TYPE type = null;
		if("actransit".equalsIgnoreCase(agencyTag))
			type = AGENCY_TYPE.AC_TRANSIT;
		else if("sf-muni".equalsIgnoreCase(agencyTag))
			type = AGENCY_TYPE.SFMUNI;
		if(type==null){
			System.out.println("[ERROR] no agency type found for agencyTag: "+agencyTag);
			return;
		}
		for (RealTimePrediction realTimePrediction : lstRealTimePredictions) {
			gtfsDataService.getMatchingScheduleForRealTime(leg, type,realTimePrediction);
		}

	}

	public Map<String, String> getAgencyMap() {
		return agencyMap;
	}
	public void setAgencyMap(Map<String, String> agencyMap) {
		this.agencyMap = agencyMap;
	}
	public int getTimeDiffercenceInMin() {
		return timeDiffercenceInMin;
	}
	public void setTimeDiffercenceInMin(int timeDiffercenceInMin) {
		this.timeDiffercenceInMin = timeDiffercenceInMin;
	}

	public Multimap<String, String> getOrphanGtfsRouteTag() {
		return orphanGtfsRouteTag;
	}

	public void setOrphanGtfsRouteTag(Multimap<String, String> orphanGtfsRouteTag) {
		this.orphanGtfsRouteTag = orphanGtfsRouteTag;
	}


}