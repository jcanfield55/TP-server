package com.nimbler.tp.service.livefeeds;

import java.util.List;
import java.util.Map;

import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
import com.nimbler.tp.dataobject.nextbus.Direction;
import com.nimbler.tp.dataobject.nextbus.NextBusResponse;
import com.nimbler.tp.dataobject.nextbus.Prediction;
import com.nimbler.tp.dataobject.nextbus.Predictions;
import com.nimbler.tp.service.livefeeds.cache.NextBusPredictionCache;
import com.nimbler.tp.util.TpConstants;
/**
 * Implementation for getting real time data from NextBus real time API for specific leg.
 * @author nIKUNJ
 *
 */
public class NextBusApiImpl implements RealTimeAPI {

	private Map<String, String> agencyMap;

	private int timeDiffercenceInMin;

	@Override
	public LegLiveFeed getLiveFeeds(Leg leg) throws FeedsNotFoundException {
		LegLiveFeed resp = null;
		try {
			Long scheduledTime = leg.getStartTime();
			String agencyId = leg.getAgencyId();
			String agencyTag = agencyMap.get(agencyId);
			if (agencyTag==null)
				throw new RealTimeDataException("Agency not supported for Real Time feeds: "+agencyId);

			String fromStopTag = leg.getFrom().getStopId().getId();
			//	String toStopTag = leg.getTo().getStopId().getId();
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

			for (Direction direction : directions) {
				List<Prediction> predictionList = direction.getPrediction();
				if (predictionList==null || predictionList.size()==0) 
					throw new RealTimeDataException("Predictions objects not found in Prediction response " +
							"for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);	

				for (Prediction prediction : predictionList) {
					if (prediction.getTripTag().equalsIgnoreCase(leg.getTripId())) {
						resp = new LegLiveFeed();
						Long predictedTime = prediction.getEpochTime();
						/*System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
						System.out.println("From Stop: "+leg.getFrom().getName()+" -- To Stop: "+leg.getTo().getName());
						System.out.println("Scheduled: "+ scheduledTime+" -- > "+new Date(scheduledTime));
						System.out.println("Predicted: "+ predictedTime+" -- > "+new Date(predictedTime));
						System.out.println("");*/
						if (scheduledTime < predictedTime) {
							int diff = (int) (predictedTime - scheduledTime);
							diff = diff /( 1000 * 60);
							if (diff > timeDiffercenceInMin)
								resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.DELAYED.ordinal());
							else
								resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.ON_TIME.ordinal());
							//	System.out.println(diff);
							resp.setTimeDiffInMins(diff);
						} else {
							int diff = (int) (scheduledTime - predictedTime);
							diff = diff /( 1000 * 60);
							if (diff > timeDiffercenceInMin)
								resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.EARLY.ordinal());
							else
								resp.setArrivalTimeFlag(TpConstants.ETA_FLAG.ON_TIME.ordinal());
							resp.setTimeDiffInMins(diff); 
							//System.out.println(diff);
						}
						/*System.out.println(resp.getArrivalTimeFlag());
						System.out.println("-----------------------------------------------------------------");*/
						resp.setLeg(leg);
						resp.setDepartureTime(predictedTime);
						break;
					}
				}
				if (resp!=null)
					break;
			}
			return resp;
		} catch (RealTimeDataException e) {
			throw new FeedsNotFoundException(e.getMessage());  
		} catch (Exception e) {
			throw new FeedsNotFoundException("Unknown Exception: "+e);
		}
	}

	@Override
	public List<LegLiveFeed> getLiveFeeds(List<Leg> legs) {
		return null;
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
}