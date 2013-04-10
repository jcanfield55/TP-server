package com.nimbler.tp.dataobject.nextbus;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class NextBusError {
	private boolean shouldRetry;
	private String msg;


	public NextBusError() {
	}

	@XmlValue
	public String getMsg() {
		return msg;
	}

	@XmlAttribute
	public boolean isShouldRetry() {
		return shouldRetry;
	}


	public void setShouldRetry(boolean shouldRetry) {
		this.shouldRetry = shouldRetry;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "NextBusError [msg=" + msg + ", shouldRetry=" + shouldRetry
				+ "]";
	}
}
