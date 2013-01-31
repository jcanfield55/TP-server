/*
 * @author nirmal
 */
package com.nimbler.tp.service.livefeeds;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.common.RealTimeDataException;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
import com.nimbler.tp.dataobject.RealTimePrediction;
import com.nimbler.tp.dataobject.nextbus.Direction;
import com.nimbler.tp.dataobject.nextbus.NextBusResponse;
import com.nimbler.tp.dataobject.nextbus.Prediction;
import com.nimbler.tp.dataobject.nextbus.Predictions;
import com.nimbler.tp.service.livefeeds.cache.NextBusPredictionCache;
import com.nimbler.tp.util.PlanUtil;
import com.nimbler.tp.util.TpConstants;
/**
 * Implementation for getting real time data from NextBus real time API for specific leg.
 * @author nIKUNJ
 *
 */
public class NextBusApiImpl implements RealTimeAPI {

	private static final boolean _verbose = false;

	private Map<String, String> agencyMap;

	private int timeDiffercenceInMin;

	@Override
	public LegLiveFeed getLiveFeeds(Leg leg) throws FeedsNotFoundException {
		LegLiveFeed resp = null;
		try {
			//			System.out.println(leg.getTripId()+"-->"+new Date(leg.getStartTime()));
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
			StringBuilder sb = new StringBuilder();
			for (Direction direction : directions) {
				List<Prediction> predictionList = direction.getPrediction();
				if (predictionList==null || predictionList.size()==0)
					throw new RealTimeDataException("Predictions objects not found in Prediction response " +
							"for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);

				for (Prediction prediction : predictionList) {
					sb.append(prediction.getTripTag()+"--"+prediction.getMinutes()+", ");
					if (prediction.getTripTag().equalsIgnoreCase(leg.getTripId()) && direction.getTitle().toLowerCase().contains(leg.getHeadsign().toLowerCase())) {
						resp = new LegLiveFeed();
						Long predictedTime = prediction.getEpochTime();
						if(_verbose){
							System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
							System.out.println("From Stop: "+leg.getFrom().getName()+" -- To Stop: "+leg.getTo().getName()+" -- Route: "+routeTag);
							System.out.println("Scheduled: "+ scheduledTime+" -- > "+new Date(scheduledTime));
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
							//							System.out.println("Difference: "+diff);
						}
						/*System.out.println(resp.getArrivalTimeFlag());
						System.out.println("-----------------------------------------------------------------");*/
						resp.setLeg(leg);
						resp.setDepartureTime(predictedTime);
						PlanUtil.setArrivalTime(resp);
						break;
					}
				}
				if (resp!=null)
					break;
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
			String agencyTag = agencyMap.get(agencyId);
			if (agencyTag==null)
				throw new RealTimeDataException("Agency not supported for Real Time feeds: "+agencyId);

			String fromStopTag = leg.getFrom().getStopId().getId();
			//			String toStopTag = leg.getTo().getStopId().getId();
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
			StringBuilder sb = new StringBuilder();
			for (Direction direction : directions) {
				List<Prediction> predictionList = direction.getPrediction();
				if (predictionList==null || predictionList.size()==0)
					throw new RealTimeDataException("RealTimePrediction objects not found in Prediction response " +
							"for Agency: "+agencyId+", Stop Tag: "+fromStopTag+", Route Tag: "+routeTag);

				for (Prediction prediction : predictionList) {
					sb.append(prediction.getTripTag()+"--"+prediction.getMinutes()+", ");
					if (direction.getTitle().toLowerCase().contains(leg.getHeadsign().toLowerCase())) {
						lstRealTimePredictions.add(new RealTimePrediction(prediction));
						if(_verbose){
							Long predictedTime = prediction.getEpochTime();
							System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
							System.out.println("From Stop: "+leg.getFrom().getName()+" -- To Stop: "+leg.getTo().getName()+" -- Route: "+routeTag);
							System.out.println("Scheduled: "+ scheduledTime+" -- > "+new Date(scheduledTime));
							System.out.println("Predicted: "+ predictedTime+" -- > "+new Date(predictedTime)+" ("+prediction.getMinutes()+")");
							System.out.println("Direction: "+leg.getHeadsign()+"-->"+direction.getTitle());
							System.out.println("");
						}
						break;
					}
				}
			}
			resp.setEmptyLeg(leg);
			resp.setLstPredictions(lstRealTimePredictions);
			return resp;
		} catch (RealTimeDataException e) {
			throw new FeedsNotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new FeedsNotFoundException("Unknown Exception: "+e);
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
}