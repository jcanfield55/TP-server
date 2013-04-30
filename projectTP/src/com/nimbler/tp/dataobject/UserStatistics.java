package com.nimbler.tp.dataobject;

import java.io.Serializable;

public class UserStatistics implements Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5338553952904376004L;

	private Integer appType ;
	private String appName ;
	private Integer total;
	private Integer updateInLast24;
	private Integer updateInLastWeek;
	private Integer updateInLastMonth;

	private Integer createInLast24 ;
	private Integer createInLastWeek ;
	private Integer createInLastMonth ;

	private Integer subscribedForEveryPush ;	
	private Integer subscribedRarePush;	

	private Integer usingBartAdv;
	private Integer usingCaltrainAdv;
	private Integer usingMuniAdv;
	private Integer usingAcTransitAdv;

	private Integer disabledPush ;
	private Integer uninstalled ;
	private Integer invalid ;
	private Integer totalPlan;
	private Integer planInLast24;
	private Integer planInLastWeek;
	private Integer planInLastMonth;


	public Integer getTotal() {
		return total;
	}
	public Integer getPlanInLast24() {
		return planInLast24;
	}

	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public void setPlanInLast24(Integer planInLast24) {
		this.planInLast24 = planInLast24;
	}

	public Integer getPlanInLastWeek() {
		return planInLastWeek;
	}

	public void setPlanInLastWeek(Integer planInLastWeek) {
		this.planInLastWeek = planInLastWeek;
	}

	public Integer getPlanInLastMonth() {
		return planInLastMonth;
	}

	public void setPlanInLastMonth(Integer planInLastMonth) {
		this.planInLastMonth = planInLastMonth;
	}

	public Integer getAppType() {
		return appType;
	}

	public void setAppType(Integer appType) {
		this.appType = appType;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}
	public Integer getUpdateInLast24() {
		return updateInLast24;
	}
	public void setUpdateInLast24(Integer updateInLast24) {
		this.updateInLast24 = updateInLast24;
	}
	public Integer getUpdateInLastWeek() {
		return updateInLastWeek;
	}
	public void setUpdateInLastWeek(Integer updateInLastWeek) {
		this.updateInLastWeek = updateInLastWeek;
	}
	public Integer getUpdateInLastMonth() {
		return updateInLastMonth;
	}
	public void setUpdateInLastMonth(Integer updateInLastMonth) {
		this.updateInLastMonth = updateInLastMonth;
	}
	public Integer getCreateInLast24() {
		return createInLast24;
	}
	public void setCreateInLast24(Integer createInLast24) {
		this.createInLast24 = createInLast24;
	}
	public Integer getCreateInLastWeek() {
		return createInLastWeek;
	}
	public void setCreateInLastWeek(Integer createInLastWeek) {
		this.createInLastWeek = createInLastWeek;
	}
	public Integer getCreateInLastMonth() {
		return createInLastMonth;
	}
	public void setCreateInLastMonth(Integer createInLastMonth) {
		this.createInLastMonth = createInLastMonth;
	}
	public Integer getSubscribedForEveryPush() {
		return subscribedForEveryPush;
	}
	public void setSubscribedForEveryPush(Integer subscribedForEveryPush) {
		this.subscribedForEveryPush = subscribedForEveryPush;
	}
	public Integer getSubscribedRarePush() {
		return subscribedRarePush;
	}
	public void setSubscribedRarePush(Integer subscribedRarePush) {
		this.subscribedRarePush = subscribedRarePush;
	}
	public Integer getUsingBartAdv() {
		return usingBartAdv;
	}
	public void setUsingBartAdv(Integer usingBartAdv) {
		this.usingBartAdv = usingBartAdv;
	}
	public Integer getUsingCaltrainAdv() {
		return usingCaltrainAdv;
	}
	public void setUsingCaltrainAdv(Integer usingCaltrainAdv) {
		this.usingCaltrainAdv = usingCaltrainAdv;
	}
	public Integer getUsingMuniAdv() {
		return usingMuniAdv;
	}
	public void setUsingMuniAdv(Integer usingMuniAdv) {
		this.usingMuniAdv = usingMuniAdv;
	}
	public Integer getUsingAcTransitAdv() {
		return usingAcTransitAdv;
	}
	public void setUsingAcTransitAdv(Integer usingAcTransitAdv) {
		this.usingAcTransitAdv = usingAcTransitAdv;
	}
	public Integer getDisabledPush() {
		return disabledPush;
	}
	public void setDisabledPush(Integer disabledPush) {
		this.disabledPush = disabledPush;
	}
	public Integer getUninstalled() {
		return uninstalled;
	}
	public void setUninstalled(Integer uninstalled) {
		this.uninstalled = uninstalled;
	}
	public Integer getInvalid() {
		return invalid;
	}
	public void setInvalid(Integer invalid) {
		this.invalid = invalid;
	}
	public Integer getTotalPlan() {
		return totalPlan;
	}
	public void setTotalPlan(Integer totalPlan) {
		this.totalPlan = totalPlan;
	}
	@Override
	public String toString() {
		return "UserStatistics [appType=" + appType + ", total=" + total
				+ ", updateInLast24=" + updateInLast24 + ", updateInLastWeek="
				+ updateInLastWeek + ", updateInLastMonth=" + updateInLastMonth
				+ ", createInLast24=" + createInLast24 + ", createInLastWeek="
				+ createInLastWeek + ", createInLastMonth=" + createInLastMonth
				+ ", subscribedForEveryPush=" + subscribedForEveryPush
				+ ", subscribedRarePush=" + subscribedRarePush
				+ ", usingBartAdv=" + usingBartAdv + ", usingCaltrainAdv="
				+ usingCaltrainAdv + ", usingMuniAdv=" + usingMuniAdv
				+ ", usingAcTransitAdv=" + usingAcTransitAdv
				+ ", disabledPush=" + disabledPush + ", uninstalled="
				+ uninstalled + ", invalid=" + invalid + ", totalPlan="
				+ totalPlan + "]";
	}
}
