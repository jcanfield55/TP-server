package com.nimbler.tp.service.livefeeds;

import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.LIVE_FEED_MODES;
/**
 * Factory class that provides appropriate handler for getting real time data.
 * Selection criteria is OTP Leg type.
 * @author nIKUNJ
 *
 */
public class RealTimeAPIFactory {

	private static RealTimeAPIFactory  factory = new RealTimeAPIFactory(); 

	/**
	 * 
	 * @return
	 */
	public static RealTimeAPIFactory getInstance() {
		return factory;
	}
	/**
	 * 
	 * @param legMode
	 * @return
	 */
	public RealTimeAPI getLiveFeedAPI(Leg leg) {
		RealTimeAPI realTimeAPI = null;
		String legMode = leg.getMode();
		if (legMode.equals(LIVE_FEED_MODES.BUS.name()) || legMode.equals(LIVE_FEED_MODES.TRAM.name())) {
			realTimeAPI =  BeanUtil.getNextBusApiImpl();
		} else if (legMode.equals(LIVE_FEED_MODES.SUBWAY.name())) {
			realTimeAPI =  BeanUtil.getBARTApiImpl();
		}
//		realTimeAPI =  BeanUtil.getWMATAApiImpl();
		return realTimeAPI;
	}
}