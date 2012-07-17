package com.nimbler.tp.common;

public class RealTimeDataException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6417890113709757151L;


	public RealTimeDataException() {
	}

	public RealTimeDataException(String msg) {
		super(msg); 
	}

	public RealTimeDataException(Throwable t) {
		super(t);
	}
}
