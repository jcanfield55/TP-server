package com.nimbler.tp.common;

public class StopNotFoundException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5465944294517051648L;

	public StopNotFoundException() {
	}

	public StopNotFoundException(String msg) {
		super(msg);
	}

	public StopNotFoundException(Throwable t) {
		super(t);
	}
}
