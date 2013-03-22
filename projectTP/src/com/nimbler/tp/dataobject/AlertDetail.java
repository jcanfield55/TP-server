package com.nimbler.tp.dataobject;

import org.apache.commons.collections.buffer.BoundedFifoBuffer;

import com.nimbler.tp.service.TpAlertService.ALERT_STATE;
import com.nimbler.tp.service.TpAlertService.ALERT_TYPE;

public class AlertDetail {

	private String lastErrorMsg;
	private long lastAlertTime;
	ALERT_TYPE alertType;
	ALERT_STATE alertState;
	BoundedFifoBuffer boundedFifoBuffer;
	public String getLastErrorMsg() {
		return lastErrorMsg;
	}
	public void setLastErrorMsg(String lastErrorMsg) {
		this.lastErrorMsg = lastErrorMsg;
	}
	public long getLastAlertTime() {
		return lastAlertTime;
	}
	public void setLastAlertTime(long lastAlertTime) {
		this.lastAlertTime = lastAlertTime;
	}
	public ALERT_TYPE getAlertType() {
		return alertType;
	}
	public void setAlertType(ALERT_TYPE alertType) {
		this.alertType = alertType;
	}
	public ALERT_STATE getAlertState() {
		return alertState;
	}
	public void setAlertState(ALERT_STATE alertState) {
		this.alertState = alertState;
	}
	@Override
	public String toString() {
		return "AlertDetail [lastErrorMsg=" + lastErrorMsg + ", lastAlertTime="
				+ lastAlertTime + ", alertType=" + alertType + ", alertState="
				+ alertState + "]";
	}
}
