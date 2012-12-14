package com.nimbler.tp.service.advisories;

import com.nimbler.tp.dataobject.TweetResponse;

/**
 * 
 * @author nIKUNJ
 *
 */
public interface AdvisoriesService {

	/**
	 * Get unread advisories count for specific device id of specific agency.
	 * @param deviceid
	 * @param deviceToken
	 * @param appType
	 * @param agencyIds
	 * @return
	 */
	public TweetResponse getAdvisoryCount(String deviceid, String deviceToken, int appType, int[] agencyIds);
	/**
	 * Get all advisories of specific agency.
	 * @param deviceid
	 * @param deviceToken
	 * @param appType
	 * @param agencyIds
	 * @return
	 */
	public TweetResponse getAllAdvisories(String deviceid, String deviceToken, int appType, int[] agencyIds);
	/**
	 * Get latest advisories after specified time.	 
	 * @param deviceid
	 * @param deviceToken
	 * @param lastAdvisoryTime
	 * @param appType
	 * @param agencyIds
	 * @return
	 */
	public TweetResponse getAdvisoriesAfterTime(String deviceid, String deviceToken, long lastAdvisoryTime, int appType, int[] agencyIds);
}
