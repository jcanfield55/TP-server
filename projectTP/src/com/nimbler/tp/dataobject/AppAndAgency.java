package com.nimbler.tp.dataobject;

import java.io.Serializable;

/**
 * 
 * @author nirmal
 *
 */
public class AppAndAgency implements Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 3821067248555989185L;

	Integer appId;
	Integer agencyId;

	public Integer getAppId() {
		return appId;
	}
	public Integer getAgencyId() {
		return agencyId;
	}

	public AppAndAgency(Integer appId, Integer agencyId) {
		this.appId = appId;
		this.agencyId = agencyId;
	}
	public static AppAndAgency of(Integer appId, Integer agencyId) {
		return new AppAndAgency(appId, agencyId);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((agencyId == null) ? 0 : agencyId.hashCode());
		result = prime * result + ((appId == null) ? 0 : appId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppAndAgency other = (AppAndAgency) obj;
		if (agencyId == null) {
			if (other.agencyId != null)
				return false;
		} else if (!agencyId.equals(other.agencyId))
			return false;
		if (appId == null) {
			if (other.appId != null)
				return false;
		} else if (!appId.equals(other.appId))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "AppAndAgency [appId=" + appId + ", agencyId=" + agencyId + "]";
	}

}
