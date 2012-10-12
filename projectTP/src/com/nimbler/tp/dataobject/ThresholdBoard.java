package com.nimbler.tp.dataobject;
/**
 * 
 * @author nirmal
 *
 */
public class ThresholdBoard {

	private int threshold;
	private int increamentCount = 0;

	private long lastIncTime;
	private long lastResetTime;
	private boolean used = false;
	private long latestTweetTimeAtInc;


	public ThresholdBoard(int threshold) {
		this.threshold = threshold;
	}

	public ThresholdBoard(int threshold, int increamentCount) {
		this.threshold = threshold;
		this.increamentCount = increamentCount;
	}

	public int getThreshold() {
		return threshold;
	}
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public ThresholdBoard() {
		super();
	}
	public int getIncreamentCount() {
		return increamentCount;
	}
	public void setIncreamentCount(int increamentCount) {
		this.increamentCount = increamentCount;
	}
	public long getLastIncTime() {
		return lastIncTime;
	}
	public void setLastIncTime(long lastIncTime) {
		this.lastIncTime = lastIncTime;
	}
	public long getLastResetTime() {
		return lastResetTime;
	}
	public void setLastResetTime(long lastResetTime) {
		this.lastResetTime = lastResetTime;
	}
	public boolean isUsed() {
		return used;
	}
	public void setUsed(boolean used) {
		this.used = used;
	}
	public int getEligibleCount() {
		return threshold + increamentCount;
	}
	public void resetCounter() {
		increamentCount = 0;
		lastResetTime = System.currentTimeMillis();
	}
	public void incCounter(long lasTweetTimeAtInc, int thresholdToInc) {
		increamentCount  = increamentCount + thresholdToInc;
		lastIncTime = System.currentTimeMillis();
		latestTweetTimeAtInc = lasTweetTimeAtInc;
	}

	public long getLatestTweetTimeAtInc() {
		return latestTweetTimeAtInc;
	}

	public void setLatestTweetTimeAtInc(long latestTweetTimeAtInc) {
		this.latestTweetTimeAtInc = latestTweetTimeAtInc;
	}

	@Override
	public String toString() {
		return "ThresholdBoard [threshold=" + threshold + ", increamentCount="
				+ increamentCount + ", lastIncTime=" + lastIncTime
				+ ", lastResetTime=" + lastResetTime + "]\n";
	}
}