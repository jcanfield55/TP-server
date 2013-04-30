package com.nimbler.tp.dataobject.nextbus;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="lastTime")
public class LastTime implements Serializable{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -7990828585417892811L;
	private long time = 0;

	@XmlAttribute
	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public LastTime() {
	}

	@Override
	public String toString() {
		return "LastTime [time=" + time + "]";
	}
}
