/*
 * 
 */
package com.nimbler.tp.dataobject;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.nimbler.tp.util.OperationCode.TP_CODES;

/**
 *
 */
@XmlRootElement
public class TPResponse {
	private int code;
	private String msg;
	private String reqId;
	private TripPlan plan;
	private Long planGenerateTime;
	private PlannerError error = null;
	private String appType = null;
	private String appBundleId = null;
	private Map data = null;

	public TPResponse() {

	}
	public Map getData() {
		return data;
	}

	public void setData(Map data) {
		this.data = data;
	}

	public PlannerError getError() {
		return error;
	}

	public void setError(PlannerError error) {
		this.error = error;
	}

	/**
	 * 
	 * @param error
	 */
	public TPResponse(TP_CODES error) {
		this.code = error.getCode();
		this.msg = error.getMsg();
	}
	/**
	 * 
	 * @param errCode
	 * @param errMsg
	 */
	public TPResponse(int errCode, String errMsg) {
		this.code = errCode;
		this.msg = errMsg;
	}
	public TripPlan getPlan() {
		return plan;
	}
	public Long getPlanGenerateTime() {
		return planGenerateTime;
	}
	public void setPlanGenerateTime(Long planGenerateTime) {
		this.planGenerateTime = planGenerateTime;
	}
	public void setPlan(TripPlan plan) {
		this.plan = plan;
	}
	public int getCode() {
		return code;
	}


	public void setCode(int code) {
		this.code = code;
	}


	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getReqId() {
		return reqId;
	}
	public void setReqId(String reqId) {
		this.reqId = reqId;
	}
	public String getAppType() {
		return appType;
	}
	public void setAppType(String appType) {
		this.appType = appType;
	}
	public String getAppBundleId() {
		return appBundleId;
	}
	public void setAppBundleId(String appBundleId) {
		this.appBundleId = appBundleId;
	}
	@Override
	public String toString() {
		return "TPResponse [code=" + code + ", msg=" + msg + ", reqId=" + reqId
				+ ", plan=" + plan + ", planGenerateTime=" + planGenerateTime
				+ ", error=" + error + "]";
	}
}	