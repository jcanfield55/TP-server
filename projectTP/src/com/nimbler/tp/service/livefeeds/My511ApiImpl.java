package com.nimbler.tp.service.livefeeds;

import java.util.List;

import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.dataobject.LegLiveFeed;
import com.nimbler.tp.dataobject.nextbus.VehiclePosition;

public class My511ApiImpl implements RealTimeAPI{

	@Override
	public LegLiveFeed getLiveFeeds(Leg leg) throws FeedsNotFoundException {
		throw new FeedsNotFoundException("Not suported for 511");
	}

	@Override
	public LegLiveFeed getAllRealTimeFeeds(Leg leg)	throws FeedsNotFoundException {
		return null;
	}

	@Override
	public LegLiveFeed getLegArrivalTime(Leg leg) throws FeedsNotFoundException {
		throw new FeedsNotFoundException("Not suported for 511");
	}

	@Override
	public VehiclePosition getVehiclePosition(Leg leg)throws FeedsNotFoundException {
		throw new FeedsNotFoundException("Not suported for 511");
	}

	@Override
	public List<LegLiveFeed> getAllRealTimeFeeds(List<Leg> legs)
			throws FeedsNotFoundException {
		throw new FeedsNotFoundException("Not suported for 511");
	}

}
