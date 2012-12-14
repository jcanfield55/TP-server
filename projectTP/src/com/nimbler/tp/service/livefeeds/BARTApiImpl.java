package com.nimbler.tp.service.livefeeds;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.math.NumberUtils;

import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.BartRouteInfo;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
import com.nimbler.tp.dataobject.bart.BartResponse;
import com.nimbler.tp.dataobject.bart.Estimate;
import com.nimbler.tp.dataobject.bart.EstimateTimeOfDiparture;
import com.nimbler.tp.dataobject.bart.Route;
import com.nimbler.tp.dataobject.bart.Station;
import com.nimbler.tp.gtfs.GtfsDataMonitor;
import com.nimbler.tp.service.livefeeds.cache.BartETDCache;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.TpConstants;
/**
 * Implementation class for processing BART real time data for a specif OTP leg.
 * @author nIKUNJ
 *
 */
public class BARTApiImpl implements RealTimeAPI {
	/**
	 * in minutes,can be updated from bean
	 */
	private int lateThreshold = 2;
	/**
	 * in minutes,can be updated from bean
	 */
	private int earlyThreshold = 1;

	private String bartAPIRegKey;

	private int maxTimeDifference = 30;//can be updated from bean
	private int maxTimeDifferenceForEarly = 10;//can be updated from bean


	@Override
	public LegLiveFeed getLiveFeeds(Leg leg) throws FeedsNotFoundException {
		LegLiveFeed resp = null;
		try {
			Long scheduledTime = leg.getStartTime();
			if (scheduledTime < System.currentTimeMillis())
				throw new RealTimeDataException("Leg scheduled time is already passed. No estimates possible.");

			String agencyId = leg.getAgencyId();
			String fromStopTag = leg.getFrom().getStopId().getId();
			String toStopTag = leg.getTo().getStopId().getId();
			String routeTag = leg.getRoute().trim();			
			BartResponse response = BartETDCache.getInstance().getEstimateTimeOfDepart(fromStopTag);
			List<Station> lstStation = response.getStation();  
			if (lstStation == null || lstStation.size()==0)
				throw new RealTimeDataException("Stations not found in ETD response for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);

			Station station = lstStation.get(0);
			List<EstimateTimeOfDiparture> etds = station.getEtdTimeDeparture();
			if (etds == null || etds.size()==0)
				throw new RealTimeDataException("ETDs not found in ETD response for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);

			EstimateTimeOfDiparture targetETD = null;
			//1. Compare TO stop in ETD response directly
			for (EstimateTimeOfDiparture etd : etds) {
				String destinationAbbr = etd.getAbbreviation();
				if (destinationAbbr.equalsIgnoreCase(toStopTag)) {
					targetETD = etd;
					break;
				}
			}
			/*if (targetETD == null) {
				String toStationInRoute = getLastStopOfRoute(routeTag);
				for (EstimateTimeOfDiparture etd : etds) {
					String destinationAbbr = etd.getAbbreviation();
					if (destinationAbbr.equalsIgnoreCase(toStationInRoute)) {//predict on base of last stop of the route
						targetETD = etd;
						break;
					}
				}
			}*/
			//2. Find TO stop in route(using head sign) and compare with ETD 
			if (targetETD == null) {
				String routeHeadSign = leg.getHeadsign().trim();
				int routeDirection  = getRouteDirection(routeTag, routeHeadSign);
				String lastStationInRoute = "";
				if (routeDirection == TpConstants.ROUTE_DIRECTION_OUTBOUND) {//in direction of route tag
					lastStationInRoute = getLastStopOfRoute(routeTag);
				} else if (routeDirection == TpConstants.ROUTE_DIRECTION_INBOUND) {//in reverse direction
					lastStationInRoute = getFirstStopOfRoute(routeTag);
				}
				for (EstimateTimeOfDiparture etd : etds) {
					String destinationAbbr = etd.getAbbreviation();
					if (destinationAbbr.equalsIgnoreCase(lastStationInRoute)) {//predict on base of last stop of the route
						targetETD = etd;
						break;
					}
				}
			}
			if (targetETD == null)
				throw new RealTimeDataException("Targeted station not found in estimation response.");

			List<Estimate> estimates = targetETD.getEstimate();
			if (estimates==null || estimates.size()==0)
				throw new RealTimeDataException("Estimates not found in ETD response for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);

			for (int index=1;index<=estimates.size();index++) {
				Integer minutesToDepart = getClosestEstimation(estimates, scheduledTime, index);
				if (minutesToDepart == -1)
					throw new RealTimeDataException("Valid minutes to departure not found in ETD response for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);

				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MINUTE, minutesToDepart);
				Long estimatedDepartureTime = cal.getTimeInMillis();

				int diff = (int) (estimatedDepartureTime - scheduledTime);
				diff = Math.abs(diff) /( 1000 * 60);
				if (diff > maxTimeDifference && maxTimeDifference != -1)
					throw new RealTimeDataException("Real time differce much higher then expected. Prediction will be ignored.");

				System.out.println("TO: "+toStopTag+",  From: "+fromStopTag+ "  Route: "+routeTag+" Time Diff: "+diff+"-"+minutesToDepart+
						" Schedule Time: "+new Date(scheduledTime)+" Estimated Time: "+new Date(estimatedDepartureTime));		
				
				int arrivalFlag = -1;				
				if (estimatedDepartureTime > scheduledTime) {
					if (diff > lateThreshold)
						//resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.DELAYED.ordinal());
						arrivalFlag = TpConstants.ETA_FLAG.DELAYED.ordinal();
					else
						//resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.ON_TIME.ordinal());
						arrivalFlag = TpConstants.ETA_FLAG.ON_TIME.ordinal();
				} else {
					System.out.println("Early : "+diff);
					if (diff > earlyThreshold) {
						if (estimates.size() == 1)//if only one train then only mark it as early
							//resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.EARLY.ordinal());
							arrivalFlag = TpConstants.ETA_FLAG.EARLY.ordinal();						
						else
							continue;
					} else
						//resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.ON_TIME.ordinal());
						arrivalFlag = TpConstants.ETA_FLAG.ON_TIME.ordinal();
				}
				if (arrivalFlag != -1) {
					resp = new LegLiveFeed();
					resp.setTimeDiffInMins(diff);
					resp.setLeg(leg);
					resp.setDepartureTime(estimatedDepartureTime);
					resp.setArrivalTimeFlag(arrivalFlag);
 					System.out.println("Got it!!");	
				}
				break;
			}
		} catch (RealTimeDataException e) {
			throw new FeedsNotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new FeedsNotFoundException("Unknown Exception: "+e);
		}
		System.out.println(resp);
		return resp;
	}
	/**
	 * 
	 * @param estimates
	 * @param scheduledTime
	 * @param index - specifies which closest match you want to get.
	 * 				  for example, 1 will return the first closest minutes, 2 will return second closest minutes. 	
	 * @return
	 */
	private Integer getClosestEstimation(List<Estimate> estimates, long scheduledTime, int index) {
		int defaultReturnVal = -1;
		if (estimates==null || estimates.size()==0)
			return defaultReturnVal;
		//key = difference between scheduled and estimated time, value = estimated time in min
		Map<Integer, Integer> diffToMiutes = new TreeMap<Integer, Integer>();		
		for (Estimate estimate: estimates) {
			int intMins = NumberUtils.toInt(estimate.getMinutes(), -1);
			if (intMins == -1) {
				//if BART vehicle is in leaving state and scheduled time is nearby current time, then mark it as on-time. 
				long currentTime = System.currentTimeMillis();
				long diff = (scheduledTime - currentTime) / (1000 * 60);
				if (Math.abs(diff) <= 3)
					return 1;
				else
					continue;
			}
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, intMins);
			Long estimatedDepartureTime = cal.getTimeInMillis();
			int diff = (int) (estimatedDepartureTime - scheduledTime);
			diffToMiutes.put(Math.abs(diff), intMins);
		}
		int count = 1;
		Iterator<Entry<Integer, Integer>> itr = diffToMiutes.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<Integer, Integer> entry = itr.next();
			if (count == index) {
				return entry.getValue();
			}
			count++;
		}
		//return diffToMiutes.size()>0 ? diffToMiutes.entrySet().iterator().next().getValue() : defaultReturnVal;
		return defaultReturnVal;
	}
	/**
	 * 
	 * @param routeTag
	 * @return
	 * @throws RealTimeDataException
	 */
	private String getLastStopOfRoute(String routeTag) throws RealTimeDataException {
		Route routeInfo = BartInMemoryDataStore.getInstance().getRouteInfo(routeTag, bartAPIRegKey);
		String routeAbbr = routeInfo.getAbbr();
		String[] fromToStationsInRoute = routeAbbr.split("-");//0=from, 1=to
		return fromToStationsInRoute[1];
	}
	/**
	 * 
	 * @param routeTag
	 * @return
	 * @throws RealTimeDataException
	 */
	private String getFirstStopOfRoute(String routeTag) throws RealTimeDataException {
		Route routeInfo = BartInMemoryDataStore.getInstance().getRouteInfo(routeTag, bartAPIRegKey);
		String routeAbbr = routeInfo.getAbbr();
		String[] fromToStationsInRoute = routeAbbr.split("-");//0=from, 1=to
		return fromToStationsInRoute[0];
	}
	/**
	 * Identifies direction of route.
	 * 0 = in direction of route name
	 * 1 = inverse direction
	 * -1 = can not find
	 * For route DALY - FREMONT: 0 = from DALY to Fremont, 1 = From fremont to Daly
	 * @param routeTag
	 * @param headSign
	 * @return
	 */

	private int getRouteDirection(String routeTag, String headSign) {
		GtfsDataMonitor gtfsBean = BeanUtil.getGtfsDataMonitorService();
		List<BartRouteInfo> bartRoutes = gtfsBean.getBartRouteInfo();
		for (BartRouteInfo info: bartRoutes) {
			if (info.getRouteTag().equals(routeTag)) {
				String dir = info.getHeadSignToDirectionMap().get(headSign);
				return NumberUtils.toInt(dir, -1); 
			}
		}
		return -1;
	}
	@Override
	public List<LegLiveFeed> getLiveFeeds(List<Leg> leg) {
		return null;
	}
	public int getTimeDiffercenceInMin() {
		return lateThreshold;
	}
	public void setTimeDiffercenceInMin(int timeDiffercenceInMin) {
		this.lateThreshold = timeDiffercenceInMin;
	}
	public int getMaxTimeDifference() {
		return maxTimeDifference;
	}
	public void setMaxTimeDifference(int maxTimeDifference) {
		this.maxTimeDifference = maxTimeDifference;
	}
	public int getMaxTimeDifferenceForEarly() {
		return maxTimeDifferenceForEarly;
	}
	public void setMaxTimeDifferenceForEarly(int maxTimeDifferenceForEarly) {
		this.maxTimeDifferenceForEarly = maxTimeDifferenceForEarly;
	}
	public String getBartAPIRegKey() {
		return bartAPIRegKey;
	}
	public void setBartAPIRegKey(String bartAPIRegKey) {
		this.bartAPIRegKey = bartAPIRegKey;
	}
}