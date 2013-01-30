package com.nimbler.tp.dataobject;

public class Tweet {

	private String tweet;
	private Long time;
	private String tweetTime;
	private String source;
	private Boolean isUrgent;

	public Tweet(String tweet, Long time, Boolean isUrgent) {
		this.tweet = tweet;
		this.time = time;
		this.isUrgent = isUrgent;
	}
	public Tweet() {
	}

	public String getTweet() {
		return tweet;
	}


	public void setTweet(String tweet) {
		this.tweet = tweet;
	}


	public Long getTime() {
		return time;
	}

	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public void setTime(Long time) {
		this.time = time;
	}


	public String getTweetTime() {
		return tweetTime;
	}


	public void setTweetTime(String tweetTime) {
		this.tweetTime = tweetTime;
	}


	public Boolean getIsUrgent() {
		return isUrgent;
	}


	public void setIsUrgent(Boolean isUrgent) {
		this.isUrgent = isUrgent;
	}


	@Override
	public String toString() {
		return "Tweet [tweet=" + tweet + ", time=" + time + ", tweetTime="
				+ tweetTime + ", source=" + source + ", isUrgent=" + isUrgent
				+ "]";
	}


}