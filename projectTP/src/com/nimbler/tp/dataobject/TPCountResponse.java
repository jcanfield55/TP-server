package com.nimbler.tp.dataobject;

public class TPCountResponse {

	private int code;
	private int totalUserCount;
	private int totalFeedbackCount;
	private int totalPlanCount;
	private int totalLegCount;
	private int totalItineraryCount;
	private int last24hourUserCount;//last 24 hour
	private int last24hourFeedbackCount;//last 24 hour count
	private int last24hourPlanCount;//last 24 hour count
	private int last24hourUpdateUserCount;//last 24 hour update time count
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public int getTotalUserCount() {
		return totalUserCount;
	}
	public void setTotalUserCount(int totalUserCount) {
		this.totalUserCount = totalUserCount;
	}
	public int getTotalFeedbackCount() {
		return totalFeedbackCount;
	}
	public void setTotalFeedbackCount(int totalFeedbackCount) {
		this.totalFeedbackCount = totalFeedbackCount;
	}
	public int getTotalPlanCount() {
		return totalPlanCount;
	}
	public void setTotalPlanCount(int totalPlanCount) {
		this.totalPlanCount = totalPlanCount;
	}
	public int getTotalLegCount() {
		return totalLegCount;
	}
	public void setTotalLegCount(int totalLegCount) {
		this.totalLegCount = totalLegCount;
	}
	public int getTotalItineraryCount() {
		return totalItineraryCount;
	}
	public void setTotalItineraryCount(int totalItineraryCount) {
		this.totalItineraryCount = totalItineraryCount;
	}
	public int getLast24hourUserCount() {
		return last24hourUserCount;
	}
	public void setLast24hourUserCount(int last24hourUserCount) {
		this.last24hourUserCount = last24hourUserCount;
	}
	public int getLast24hourFeedbackCount() {
		return last24hourFeedbackCount;
	}
	public void setLast24hourFeedbackCount(int last24hourFeedbackCount) {
		this.last24hourFeedbackCount = last24hourFeedbackCount;
	}
	public int getLast24hourPlanCount() {
		return last24hourPlanCount;
	}
	public void setLast24hourPlanCount(int last24hourPlanCount) {
		this.last24hourPlanCount = last24hourPlanCount;
	}
	public int getLast24hourUpdateUserCount() {
		return last24hourUpdateUserCount;
	}
	public void setLast24hourUpdateUserCount(int last24hourUpdateUserCount) {
		this.last24hourUpdateUserCount = last24hourUpdateUserCount;
	}
	
	public TPCountResponse() {
		
	}
	@Override
	public String toString() {
		return "TPCountResponse [code=" + code + ", totalUserCount="
				+ totalUserCount + ", totalFeedbackCount=" + totalFeedbackCount
				+ ", totalPlanCount=" + totalPlanCount + ", totalLegCount="
				+ totalLegCount + ", totalItineraryCount="
				+ totalItineraryCount + ", last24hourUserCount="
				+ last24hourUserCount + ", last24hourFeedbackCount="
				+ last24hourFeedbackCount + ", last24hourPlanCount="
				+ last24hourPlanCount + ", last24hourUpdateUserCount="
				+ last24hourUpdateUserCount + "]";
	}
}
