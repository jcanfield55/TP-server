/*
 * @author nirmal
 */
package com.nimbler.tp.util;

import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
import com.nimbler.tp.dataobject.LiveFeedResponse;
import com.nimbler.tp.dataobject.TraverseMode;
import com.nimbler.tp.util.TpConstants.ETA_FLAG;

/**
 * The Class PlanUtil.
 *
 * @author nirmal
 */
public class PlanUtil {

	public static LegLiveFeed shiftLeg(List<Leg> legs, LegLiveFeed legLiveFeed) {
		Leg liveLeg = legLiveFeed.getLeg();
		long oneMin = DateUtils.MILLIS_PER_MINUTE;
		//		oneMin = 1;
		if(legLiveFeed.getArrivalTimeFlag() == ETA_FLAG.EARLY.ordinal()){			
			Leg prevLeg = getLegAtOffset(legs, liveLeg, -1); 
			if(prevLeg==null || !prevLeg.getMode().equals(TraverseMode.WALK.toString()))
				return null;
			Leg prePrevLeg = getLegAtOffset(legs, liveLeg, -2);
			if(prePrevLeg != null){
				long endPoint = prePrevLeg.getEndTime();
				long startPoint = prevLeg.getStartTime();
				if(endPoint > startPoint-(legLiveFeed.getTimeDiffInMins()*oneMin)) //can't shift
					return null;
			}
			int diffInMin = legLiveFeed.getTimeDiffInMins();
			LegLiveFeed feedToAdd = new LegLiveFeed();
			feedToAdd.setLeg(prevLeg);
			feedToAdd.setTimeDiffInMins(legLiveFeed.getTimeDiffInMins());
			feedToAdd.setArrivalTimeFlag(legLiveFeed.getArrivalTimeFlag());
			feedToAdd.setArrivalTime(prevLeg.getEndTime() - (diffInMin * oneMin));
			feedToAdd.setDepartureTime(prevLeg.getStartTime() - (diffInMin * oneMin));
			return feedToAdd;
		}else if (legLiveFeed.getArrivalTimeFlag() == ETA_FLAG.DELAYED.ordinal()){
			Leg nextLeg = getLegAtOffset(legs, liveLeg, 1); 
			if(nextLeg==null || !nextLeg.getMode().equals(TraverseMode.WALK.toString()))
				return null;
			Leg nextToNextLeg = getLegAtOffset(legs, liveLeg, 2);
			if(nextToNextLeg != null){
				long endPoint = nextLeg.getEndTime();
				long startPoint = nextToNextLeg.getStartTime();
				if(endPoint + legLiveFeed.getTimeDiffInMins()*oneMin > startPoint) //can't shift
					return null;
			}
			int diffInMin = legLiveFeed.getTimeDiffInMins();
			LegLiveFeed feedToAdd = new LegLiveFeed();
			feedToAdd.setLeg(nextLeg);
			feedToAdd.setTimeDiffInMins(legLiveFeed.getTimeDiffInMins());
			feedToAdd.setArrivalTimeFlag(legLiveFeed.getArrivalTimeFlag());
			feedToAdd.setArrivalTime(nextLeg.getEndTime() + (diffInMin * oneMin));
			feedToAdd.setDepartureTime(nextLeg.getStartTime() + (diffInMin * oneMin));
			return feedToAdd;
		}
		return null;
	}

	/**
	 * Set dirty flag if leg conflict occurs.
	 *
	 * @param legs the legs
	 * @param itinFeeds the itin feeds
	 */
	public static void validateLegConflict(List<Leg> legs, LiveFeedResponse itinFeeds) {
		List<LegLiveFeed> lstLiveFeeds = itinFeeds.getLegLiveFeeds();
		if(ComUtils.isEmptyList(lstLiveFeeds))
			return;
		for (LegLiveFeed legLiveFeed : lstLiveFeeds) {
			if(legLiveFeed.getArrivalTimeFlag() == ETA_FLAG.EARLY.ordinal()){
				Leg prevLeg = getLegAtOffset(legs, legLiveFeed.getLeg(), -1);
				if(prevLeg!=null){
					if(prevLeg.getEndTime() > legLiveFeed.getDepartureTime()){
						itinFeeds.setDirty(true);
						break;
					}
				}
			}else if(legLiveFeed.getArrivalTimeFlag() == ETA_FLAG.DELAYED.ordinal()){
				Leg nextLeg = getLegAtOffset(legs, legLiveFeed.getLeg(), 1);
				if(nextLeg!=null){
					if(legLiveFeed.getLeg().getEndTime()+(legLiveFeed.getTimeDiffInMins()*DateUtils.MILLIS_PER_MINUTE) > nextLeg.getStartTime()){
						itinFeeds.setDirty(true);
						break;
					}
				}
			}
		}
	}



	/**
	 * Gets the leg index.
	 *
	 * @param legs the legs
	 * @param leg the leg
	 * @return the leg index
	 */
	public static int getLegIndex(List<Leg> legs, Leg leg) {
		if(legs==null || leg==null)
			return -1;
		if(!ComUtils.isEmptyList(legs) && leg.getId()!=null){
			for (int i = 0; i < legs.size(); i++) {
				if(leg.getId().equals(legs.get(i).getId()))
					return i;
			}
		}
		return -1;
	}

	/**
	 * Gets the leg at offset.
	 *
	 * @param legs - Legs
	 * @param leg - Relative leg
	 * @param offset the offset of leg. i.e -1 for previous, 1 for next
	 * @return the leg at offset
	 */
	public static Leg getLegAtOffset(List<Leg> legs, Leg leg,int offset) {
		int index = getLegIndex(legs, leg);
		int target = index+offset;
		if(target>=0 && target< legs.size())
			return legs.get(target);
		else
			return null;
	}

	/**
	 * Sets the arrival time.
	 *
	 * @param resp the new arrival time
	 */
	public static void setArrivalTime(LegLiveFeed resp) {
		int flag = resp.getTimeDiffInMins();
		if(flag == ETA_FLAG.DELAYED.ordinal()){
			resp.setArrivalTime(resp.getLeg().getEndTime() + (resp.getTimeDiffInMins() * DateUtils.MILLIS_PER_MINUTE));
		}else if(flag==ETA_FLAG.EARLY.ordinal()){
			resp.setArrivalTime(resp.getLeg().getEndTime() - (resp.getTimeDiffInMins() * DateUtils.MILLIS_PER_MINUTE));
		}
	}
}
