package com.nimbler.tp.dataobject;

import java.util.List;

import com.nimbler.tp.util.OperationCode.TP_CODES;

public class TweetResponse {

	private int errCode;
	private String errMsg;
	private int tweetCount;
	private boolean isUrgent;
	private boolean allNew;//if a device is eligible for getting all available tweets, then it will be true 
	private List<Tweet> tweet;

	public boolean isAllNew() {
		return allNew;
	}
	public void setAllNew(boolean allNew) {
		this.allNew = allNew;
	}
	public TweetResponse() {
		this.errCode = TP_CODES.SUCESS.getCode();
	}
	public boolean isUrgent() {
		return isUrgent;
	}
	public void setUrgent(boolean isUrgent) {
		this.isUrgent = isUrgent;
	}

	public int getTweetCount() {
		return tweetCount;
	}
	public void setTweetCount(int tweetCount) {
		this.tweetCount = tweetCount;
	}
	public int getErrCode() {
		return errCode;
	}
	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}
	public String getErrMsg() {
		return errMsg;
	}
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	public List<Tweet> getTweet() {
		return tweet;
	}
	public void setTweet(List<Tweet> tweet) {
		this.tweet = tweet;
	}

	/**
	 * 
	 * @param code
	 */
	public void setError(int code) {
		this.errCode = code;
		this.errMsg = TP_CODES.get(code).getMsg(); 
	}

	@Override
	public String toString() {
		return "TweetResponse [errCode=" + errCode + ", errMsg=" + errMsg
				+ ", tweet=" + tweet + "]";
	}
}