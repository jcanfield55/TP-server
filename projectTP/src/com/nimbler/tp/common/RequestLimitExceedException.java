package com.nimbler.tp.common;

public class RequestLimitExceedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1578652146562161851L;

	/**
	 * 
	 */

	public RequestLimitExceedException() {
	}

	public RequestLimitExceedException(String msg) {
		super(msg);
	}

	public RequestLimitExceedException(Throwable t) {
		super(t);
	}
}
