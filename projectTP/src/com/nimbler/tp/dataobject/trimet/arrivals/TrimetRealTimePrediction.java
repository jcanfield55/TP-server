package com.nimbler.tp.dataobject.trimet.arrivals;

import com.nimbler.tp.dataobject.RealTimePrediction;
import com.nimbler.tp.service.livefeeds.TrimetApiImpl.TrimetArrivalStatus;

public class TrimetRealTimePrediction extends RealTimePrediction{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5171570274647319014L;
	public static TrimetRealTimePrediction of(ArrivalType  arrival) {
		TrimetRealTimePrediction prediction = new TrimetRealTimePrediction();
		prediction.setDirection(arrival.getDir()+"");
		prediction.setRouteTag(arrival.getRoute()+"");
		prediction.setScheduleTime(arrival.getScheduled()+"");
		return prediction;
	}
	/**
	 * {@link TrimetArrivalStatus}
	 */
	private int status = 0;
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}

}
