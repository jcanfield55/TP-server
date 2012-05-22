package com.apprika.otp.util;

import com.apprika.otp.dataobject.TPResponse;
import com.apprika.otp.dataobject.Status;
import com.apprika.otp.util.OperationCode.TP_CODES;

public class ResponseUtil {
	public static TPResponse createResponse(TP_CODES codes) {
		TPResponse response = new TPResponse();
		response.setStatus(new Status(codes));
		return response;
	}

	public static  TPResponse createResponse(TpException exception) {
		TPResponse response = new TPResponse();
		response.setStatus(new Status(exception.getErrCode(),exception.getErrMsg()));
		return response;
	}
}
