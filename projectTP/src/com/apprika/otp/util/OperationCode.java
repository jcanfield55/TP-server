/**
 * 
 * Copyright (C) 2012 Apprika Systems Pvt. Ltd. 
 * All rights reserved.
 *
 * 
 */
package com.apprika.otp.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @author nirmal
 * OTP error/response codes
 *
 */
public class OperationCode {

	public enum TP_CODES{
		FAIL(100, "Operation Failed"),
		INVALID_SESSION(101, "Invalid Session"),
		LOGIN_SUCESS(102, "Login Sucessfull"),
		LOGOUT_SUCESS(103, "Logout Sucessfull"),
		LOGIN_FAILURE(104, "Login Failed"),
		SUCESS(105, "Operation Completed Sucessfully"),
		INVALID_REQUEST(106, "Invalid Request"),
		DATA_NOT_EXIST(107, "Data Not Exist"),
		PERMITION_DENIED(108, "Operation Not Permitted"),
		INTERNAL_SERVER_ERROR(500, "Internal Server Error");

		private static final Map<Integer,TP_CODES> lookup	= new HashMap<Integer,TP_CODES>();
		static {
			for(TP_CODES s : EnumSet.allOf(TP_CODES.class))
				lookup.put(s.getCode(), s);
		}
		private int code;
					private String msg;

					private TP_CODES(int code, String msg) {
						this.code = code;
						this.msg = msg;
					}
					public int getCode() { return code; }
					public String getMsg() {return msg;}
					public static TP_CODES get(int code) { 
						return lookup.get(code); 
					}
	}
}
