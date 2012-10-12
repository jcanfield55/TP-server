package com.nimbler.tp.dataobject;

import java.util.ArrayList;
import java.util.List;

import com.nimbler.tp.util.OperationCode.TP_CODES;
/**
 * 
 * @author nIKUNJ
 *
 */
public class PlanLiveFeeds {

	private String planId;
	private int errCode;
	private String errMsg;
	private List<LiveFeedResponse> itinLiveFeeds;

	/**
	 * 
	 */
	public PlanLiveFeeds(String planId) {
		this.planId = planId;
		this.errCode = TP_CODES.SUCESS.getCode();
	}

	public PlanLiveFeeds() {
		this.errCode = TP_CODES.SUCESS.getCode();	
	}

	public String getPlanId() {
		return planId;
	}
	public void setPlanId(String planId) {
		this.planId = planId;
	}
	public int getErrCode() {
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
	public List<LiveFeedResponse> getItinLiveFeeds() {
		return itinLiveFeeds;
	}
	public void setItinLiveFeeds(List<LiveFeedResponse> itinLiveFeeds) {
		this.itinLiveFeeds = itinLiveFeeds;
	}
	/**
	 * 
	 * @param code
	 */
	public void setError(int code) {
		this.errCode = code;
		this.errMsg = TP_CODES.get(code).getMsg(); 
	}
	/**
	 * 
	 * @param itinFeed
	 */
	public void addItinLiveFeeds(LiveFeedResponse itinFeed) {
		if (itinLiveFeeds==null)
			itinLiveFeeds = new ArrayList<LiveFeedResponse>();
		itinLiveFeeds.add(itinFeed); 
	}
	@Override
	public String toString() {
		return "PlanLiveFeeds [planId=" + planId + ", errCode=" + errCode
				+ ", errMsg=" + errMsg + ", itinLiveFeeds=" + itinLiveFeeds
				+ "]";
	}
}