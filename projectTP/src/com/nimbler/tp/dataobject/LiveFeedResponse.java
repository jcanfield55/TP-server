package com.nimbler.tp.dataobject;

import java.util.ArrayList;
import java.util.List;

import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.TpConstants.ETA_FLAG;
/**
 * 
 * @author nIKUNJ
 *
 */
public class LiveFeedResponse {

	private String itineraryId;
	private Integer errCode;
	private String errMsg;
	private int arrivalTimeFlag;//if any of the legs in this Itinerary is delayed, then mark whole itinerary status as delayed.
	private List<LegLiveFeed> legLiveFeeds;	

	/**
	 * 
	 * @return
	 */
	public int getArrivalTimeFlag() {
		return arrivalTimeFlag;
	}
	/**
	 * 
	 * @param arrivalTimeFlag
	 */
	public void setArrivalTimeFlag(int arrivalTimeFlag) {
		this.arrivalTimeFlag = arrivalTimeFlag;
	}

	/**
	 * 
	 */
	public LiveFeedResponse() {
		this.errCode = TP_CODES.SUCESS.getCode();
		this.arrivalTimeFlag = ETA_FLAG.ON_TIME.ordinal();
	}

	public List<LegLiveFeed> getLegLiveFeeds() {
		return legLiveFeeds;
	}

	public void setLegLiveFeeds(List<LegLiveFeed> legLiveFeeds) {
		this.legLiveFeeds = legLiveFeeds;
	}

	public String getItineraryId() {
		return itineraryId;
	}

	public void setItineraryId(String itineraryId) {
		this.itineraryId = itineraryId;
	}

	public void addLegLiveFeed(LegLiveFeed legFeed) {
		if (legLiveFeeds==null)
			legLiveFeeds = new ArrayList<LegLiveFeed>();
		legLiveFeeds.add(legFeed); 
	}

	public Integer getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	/**
	 * 
	 * @param code
	 */
	public void setError(int code) {
		this.errCode = code;
		this.errMsg = TP_CODES.get(code).getMsg(); 
	}

	@Override
	public String toString() {
		return "LiveFeedResponse [itineraryId=" + itineraryId + ", errCode="
				+ errCode + ", errMsg=" + errMsg + ", legLiveFeeds="
				+ legLiveFeeds + "]";
	}
}