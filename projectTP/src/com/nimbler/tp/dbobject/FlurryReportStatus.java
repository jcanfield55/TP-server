package com.nimbler.tp.dbobject;

import com.nimbler.tp.dataobject.flurry.FlurryEventResponse;
/**
 * 
 * @author nirmal
 *
 */
public class FlurryReportStatus {
	public enum FLURRY_EVENT_CALL_STATUS{
		CREATED,	//0	
		PENDING,//1
		REQUEST_SUCCESS,//2
		REQUEST_FAILED,//3
		GENERATE_SUCCESS,//4
		GENERATE_FAILED//5
	}

	private String id;
	private FlurryEventResponse eventResponse;
	private long createTime ;
	private long updateTime ;
	private String statusText;
	private String eventId;
	private String url;
	private int status = FLURRY_EVENT_CALL_STATUS.CREATED.ordinal();
	private transient String data;

	private int tryCount = 0;

	public int getTryCount() {
		return tryCount;
	}
	public void setTryCount(int tryCount) {
		this.tryCount = tryCount;
	}
	public void incTryCount() {
		this.tryCount ++;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	public FlurryEventResponse getEventResponse() {
		return eventResponse;
	}

	public long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public void setEventResponse(FlurryEventResponse eventResponse) {
		this.eventResponse = eventResponse;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public String getStatusText() {
		return statusText;
	}
	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public static FlurryReportStatus cerate() {
		FlurryReportStatus response = new FlurryReportStatus();
		response.setCreateTime(System.currentTimeMillis());
		return response;
	}
	@Override
	public String toString() {
		return "FlurryReportStatus [id=" + id + ", eventResponse="
				+ eventResponse + ", createTime=" + createTime
				+ ", statusText=" + statusText + ", status=" + status + "]";
	}
}
