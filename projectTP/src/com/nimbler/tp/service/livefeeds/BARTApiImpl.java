package com.nimbler.tp.service.livefeeds;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.math.NumberUtils;

import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
import com.nimbler.tp.dataobject.bart.BartResponse;
import com.nimbler.tp.dataobject.bart.Estimate;
import com.nimbler.tp.dataobject.bart.EstimateTimeOfDiparture;
import com.nimbler.tp.dataobject.bart.Station;
import com.nimbler.tp.service.livefeeds.cache.BartETDCache;
import com.nimbler.tp.util.TpConstants;
/**
 * Implementation class for processing BART real time data for a specif OTP leg.
 * @author nIKUNJ
 *
 */
public class BARTApiImpl implements RealTimeAPI {

	private int timeDiffercenceInMin;

	private String bartAPIRegKey;

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
			String routeTag = leg.getRoute();
			BartResponse response = BartETDCache.getInstance().getEstimateTimeOfDepart(fromStopTag);
			List<Station> lstStation = response.getStation();  
			if (lstStation == null || lstStation.size()==0)
				throw new RealTimeDataException("Stations not found in ETD response for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);

			Station station = lstStation.get(0);
			List<EstimateTimeOfDiparture> etds = station.getEtdTimeDeparture();
			if (etds == null || etds.size()==0)
				throw new RealTimeDataException("ETDs not found in ETD response for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);

			/*Route routeInfo = BartInMemoryDataStore.getInstance().getRouteInfo(routeTag, bartAPIRegKey);
			String routeAbbr = routeInfo.getAbbr();
			String[] fromToStationsInRoute = routeAbbr.split("-");//0=from, 1=to
			String toStationInRoute = fromToStationsInRoute[1];*/

			for (EstimateTimeOfDiparture etd : etds) {
				String destinationAbbr = etd.getAbbreviation();
				if (destinationAbbr.equalsIgnoreCase(toStopTag)) {//check if to station in route and destination station in estimation are same 
					List<Estimate> estimates = etd.getEstimate();
					if (estimates==null || estimates.size()==0)
						throw new RealTimeDataException("Estimates not found in ETD response for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);

					Integer minutesToDepart = getClosestEstimation(estimates, scheduledTime);
					if (minutesToDepart == -1)
						throw new RealTimeDataException("Valid minutes to departure not found in ETD response for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);

					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MINUTE, minutesToDepart);
					Long estimatedDepartureTime = cal.getTimeInMillis();
					resp = new LegLiveFeed();
					/*System.out.println("-----------------------------------------------------------------");
					System.out.println("Orig Stop: "+fromStopTag+" To: "+toStopTag);
					System.out.println("Scheduled: "+ scheduledTime+" -- > "+new Date(scheduledTime));
					System.out.println("Predicted: "+ estimatedDepartureTime+" -- > "+new Date(estimatedDepartureTime));
					System.out.println("Minutes To Depart: "+ minutesToDepart);*/
					if (scheduledTime < estimatedDepartureTime) {
						int diff = (int) (estimatedDepartureTime - scheduledTime);
						diff = diff / 1000 / 60;
						if (diff > timeDiffercenceInMin)
							resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.DELAYED.ordinal());
						else 
							resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.ON_TIME.ordinal());
						resp.setTimeDiffInMins(diff);
					} else {
						int diff = (int) (scheduledTime - estimatedDepartureTime);
						diff = diff / 1000 / 60;
						if (diff > timeDiffercenceInMin)
							resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.EARLY.ordinal());
						else 
							resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.ON_TIME.ordinal());
						resp.setTimeDiffInMins(diff);
					}
					//					System.out.println(resp.getArrivalTimeFlag());
					//					System.out.println("-----------------------------------------------------------------");
					resp.setLeg(leg); 
					resp.setDepartureTime(estimatedDepartureTime);
					break;
				}
			}
		} catch (RealTimeDataException e) {
			throw new FeedsNotFoundException(e.getMessage()); 
		} catch (Exception e) {
			throw new FeedsNotFoundException("Unknown Exception: "+e);
		}
		return resp;
	}
	/**
	 * 
	 * @param estimates
	 * @param scheduledTime
	 * @return
	 */
	private Integer getClosestEstimation(List<Estimate> estimates, long scheduledTime) {
		if (estimates==null || estimates.size()==0)
			return -1;
		//key = difference between scheduled and estimated time, value = estimated time in min
		Map<Integer, Integer> diffToMiutes = new TreeMap<Integer, Integer>();
		for (Estimate estimate: estimates) {
			Integer intMins = NumberUtils.toInt(estimate.getMinutes(), 1);
			//System.out.println("Estimates: "+intMins);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, intMins);
			Long estimatedDepartureTime = cal.getTimeInMillis();
			int diff = (int) (estimatedDepartureTime - scheduledTime);
			diffToMiutes.put(Math.abs(diff), intMins);
		}
		return diffToMiutes.size()>0 ? diffToMiutes.entrySet().iterator().next().getValue() : -1;
	}
	@Override
	public List<LegLiveFeed> getLiveFeeds(List<Leg> leg) {
		return null;
	}
	public String getBartAPIRegKey() {
		return bartAPIRegKey;
	}
	public void setBartAPIRegKey(String bartAPIRegKey) {
		this.bartAPIRegKey = bartAPIRegKey;
	}
	public int getTimeDiffercenceInMin() {
		return timeDiffercenceInMin;
	}
	public void setTimeDiffercenceInMin(int timeDiffercenceInMin) {
		this.timeDiffercenceInMin = timeDiffercenceInMin;
	}
}