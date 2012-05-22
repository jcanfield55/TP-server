package com.apprika.otp.dbobject;

import java.io.Serializable;

/**
 * The Class FeedBack.
 * @author nirmal
 */
public class FeedBack implements Serializable {

	public enum FEEDBACK_FORMAT_TYPE{
		UNDEFINED,
		TEXT,
		AUDIO,
		TEXT_AUDIO;
	}
	public enum SOURCE{
		UNDEFINED,
		TRIP,
		ITINERARY,
		LEG;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -1989283767936507352L;
	private String id;
	private User user;
	private int source;
	private int formatType;
	private String data;
	private String audioFileUrl;
	private float rating;	
	private long createTime;	
	private Trip trip;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public int getFormatType() {
		return formatType;
	}
	public void setFormatType(int formatType) {
		this.formatType = formatType;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public float getRating() {
		return rating;
	}

	public String getAudioFileUrl() {
		return audioFileUrl;
	}
	public void setAudioFileUrl(String audioFileUrl) {
		this.audioFileUrl = audioFileUrl;
	}
	public void setRating(float rating) {
		this.rating = rating;
	}
	public Trip getTrip() {
		return trip;
	}
	public void setTrip(Trip trip) {
		this.trip = trip;
	}
	@Override
	public String toString() {
		return "FeedBack [id=" + id + ", user=" + user + ", source=" + source
				+ ", formatType=" + formatType + ", data=" + data
				+ ", audioFileUrl=" + audioFileUrl + ", rating=" + rating
				+ ", createTime=" + createTime + ", trip=" + trip + "]";
	}

}
