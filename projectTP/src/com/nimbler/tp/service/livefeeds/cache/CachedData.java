package com.nimbler.tp.service.livefeeds.cache;

import java.io.Serializable;
import java.util.List;
/**
 * 
 * @author nirmal
 *
 * @param <T>
 */
public class CachedData<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5702555626675549080L;

	private List<T> lstData;
	private long cacheTime;

	public List<T> getLstData() {
		return lstData;
	}

	public void setLstData(List<T> lstData) {
		this.lstData = lstData;
	}

	public CachedData(List<T> lstData) {
		cacheTime=System.currentTimeMillis();
		this.lstData = lstData;
	}

	public long getCacheTime() {
		return cacheTime;
	}

	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
	}

	public CachedData(List<T> lstData, long cacheTime) {
		this.lstData = lstData;
		this.cacheTime = cacheTime;
	}

	@Override
	public String toString() {
		return "CachedData [lstData=" + lstData + ", cacheTime=" + cacheTime
				+ "]";
	}
}
