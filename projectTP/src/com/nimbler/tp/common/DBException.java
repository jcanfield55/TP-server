package com.nimbler.tp.common;

import com.nimbler.tp.util.OperationCode.TP_CODES;
/**
 * 
 * @author nIKUNJ
 *
 */
public class DBException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5970799814601337613L;

	private int errCode;
	private String errMsg;	
	/**
	 * 
	 * @param errCode
	 * @param errMsg
	 */
	public DBException(int errCode, String errMsg) {
		this.errCode = errCode;
		this.errMsg = errMsg;
	}
	/**
	 * 
	 * @param errCode
	 */
	public DBException(int errCode) {
		super(TP_CODES.get(errCode)!=null?TP_CODES.get(errCode).getMsg():"");
		this.errCode = errCode;
		this.errMsg = TP_CODES.get(errCode)!=null?TP_CODES.get(errCode).getMsg():null;
	}
	/**
	 * 
	 * @param error
	 */
	public DBException(TP_CODES error) {
		super(error.getMsg());
		this.errCode = error.getCode();
		this.errMsg = error.getMsg();
	}
	/**
	 * 
	 * @param errMsg
	 */
	public DBException(String errMsg) {
		super(errMsg);	
		this.errMsg = errMsg;
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
}