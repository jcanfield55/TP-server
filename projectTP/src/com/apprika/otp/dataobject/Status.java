package com.apprika.otp.dataobject;

import java.io.Serializable;

import com.apprika.otp.util.OperationCode.TP_CODES;

public class Status implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3949423544203379411L;
	private int code;
	private String msg;	

	public Status() {
		super();
	}

	public Status(int errCode, String errMsg) {
		this.code = errCode;
		this.msg = errMsg;
	}

	public Status(String errMsg) {
		this.msg = errMsg;
	}
	public Status(int errCode) {
		this.code = errCode;		
		this.msg = TP_CODES.get(errCode)!=null?TP_CODES.get(errCode).getMsg():null;
	}
	public Status(TP_CODES error) {
		this.code = error.getCode();
		this.msg = error.getMsg();
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
		return "Status [code=" + code + ", msg=" + msg + "]";
	}
}
