package com.nimbler.tp.dataobject;

public class Tweet {

	private String tweet;
	private Long time;
	private String tweetTime;
	private Boolean isUrgent;
	
	public String getTweet() {
		return tweet;
	}


	public void setTweet(String tweet) {
		this.tweet = tweet;
	}


	public Long getTime() {
		return time;
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
				+ tweetTime + ", isUrgent=" + isUrgent + "]";
	}
	
	
}