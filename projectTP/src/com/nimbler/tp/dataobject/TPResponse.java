/*
 * 
 */
package com.nimbler.tp.dataobject;

import javax.xml.bind.annotation.XmlRootElement;

import com.nimbler.tp.util.OperationCode.TP_CODES;

/**
 *
 */
@XmlRootElement
public class TPResponse {
	private int code;
	private String msg;
	private TripPlan plan;
	private PlannerError error = null;
	
	public TPResponse() {

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
	@Override
	public String toString() {
		return "TPResponse [code=" + code + ", msg=" + msg + "]";
	}
}	