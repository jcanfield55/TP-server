package com.nimbler.tp.service.livefeeds;

import java.util.Map;

import com.nimbler.tp.common.FeedsNotFoundException;
import com.nimbler.tp.dataobject.Leg;
import com.nimbler.tp.util.BeanUtil;
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
	public RealTimeAPI getLiveFeedAPI(Leg leg) throws FeedsNotFoundException{
		String agencyName  = leg.getAgencyName();
		Map<String, RealTimeAPI> realmtimeAgencyMap = BeanUtil.getNimblerAppsBean().getRealTimeApiByAgency();
		RealTimeAPI api =  realmtimeAgencyMap.get(agencyName);
		if(api==null)
			throw new FeedsNotFoundException("No Implementation found for agency:"+agencyName);
		return api;
	}
}