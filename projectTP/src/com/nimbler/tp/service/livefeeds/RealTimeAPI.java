/*
 * @author nirmal
 */
package com.nimbler.tp.service.livefeeds;

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
	 * Gets the all real time feeds.
	 *
	 * @param leg the leg
	 * @return the all real time feeds
	 * @throws FeedsNotFoundException the feeds not found exception
	 */
	public abstract LegLiveFeed getAllRealTimeFeeds(Leg leg) throws FeedsNotFoundException;

	/**
	 * Gets the leg arrival time.
	 *
	 * @param leg the leg
	 * @return the leg arrival time
	 * @throws FeedsNotFoundException the feeds not found exception
	 */
	public abstract LegLiveFeed getLegArrivalTime(Leg leg) throws FeedsNotFoundException;
}
