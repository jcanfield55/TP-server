package com.nimbler.tp.service.livefeeds;

import java.util.List;

import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
/**
 * 
 * @author nIKUNJ
 *
 */
public interface RealTimeAPI {
	/**
	 * Will return real time departure data for specific leg.
	 * @param leg
	 * @return
	 * @throws FeedsNotFoundException
	 */
	public abstract LegLiveFeed getLiveFeeds(Leg leg) throws FeedsNotFoundException;
	/**
	 * Will return real time departure data for multiple legs.
	 * @param leg
	 * @return
	 * @throws FeedsNotFoundException
	 */
	public abstract List<LegLiveFeed> getLiveFeeds(List<Leg> leg) throws FeedsNotFoundException ;
}
