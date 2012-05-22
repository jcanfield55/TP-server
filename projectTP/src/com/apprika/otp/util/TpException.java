/**
 * 
 * Copyright (C) 2012 Apprika Systems Pvt. Ltd. 
 * All rights reserved.
 *
 * 
 */
package com.apprika.otp.util;

import com.apprika.otp.util.OperationCode.TP_CODES;


public class TpException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5970799814601337613L;

	private int errCode;
	private String errMsg;	

	public TpException(int errCode, String errMsg) {
		this.errCode = errCode;
		this.errMsg = errMsg;
	}

	public TpException(String errMsg) {
		super(errMsg);	
		this.errMsg = errMsg;
	}

	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public TpException(int errCode) {
		super(TP_CODES.get(errCode)!=null?TP_CODES.get(errCode).getMsg():"");
		this.errCode = errCode;
		this.errMsg = TP_CODES.get(errCode)!=null?TP_CODES.get(errCode).getMsg():null;;
	}
	public TpException(TP_CODES error) {
		super(error.getMsg());
		this.errCode = error.getCode();
		this.errMsg = error.getMsg();
	}

	public String getErrMsg() {
		return errMsg;
	}

}
