/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.util;

import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.util.OperationCode.TP_CODES;


/**
 * The Class ResponseUtil.
 *
 * @author nirmal
 */
public class ResponseUtil {

	/**
	 * Creates the response.
	 *
	 * @param codes the codes
	 * @return the tP response
	 */
	public static TPResponse createResponse(TP_CODES codes) {
		return new TPResponse(codes);
	}
	/**
	 * Creates the response.
	 *
	 * @param exception the exception
	 * @return the tP response
	 */
	public static  TPResponse createResponse(TpException exception) {
		int code = exception.getErrCode()!=0?exception.getErrCode():TP_CODES.FAIL.getCode();
		TPResponse response = new TPResponse(code, exception.getErrMsg());
		return response;
	}
}