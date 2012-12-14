package com.nimbler.tp.service.advisories;

import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.TpConstants;
/**
 * 
 * @author nIKUNJ
 *
 */
public class AdvisoriesServiceFactory {
	
	/**
	 * 
	 * @param agencyFlag
	 * @return
	 */
	public static AdvisoriesService getService(int agencyFlag) {
		AdvisoriesService advisoriesService = null;
		if (agencyFlag == TpConstants.AGENCY_TYPE.CALTRAIN.ordinal() || agencyFlag == TpConstants.AGENCY_TYPE.BART.ordinal()) {
			advisoriesService = BeanUtil.getAdvisoriesService();
		}
		return advisoriesService;
	}
}