package com.nimbler.tp.dataobject;

import java.io.Serializable;

/**
 * 
 * @author nirmal
 *
 */
public class AgencyDetail implements Serializable{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 645589526118868192L;
	String gtfsAgencyId;
	String agencyName;
	String displayName;
	public String getGtfsAgencyId() {
		return gtfsAgencyId;
	}
	public void setGtfsAgencyId(String gtfsAgencyId) {
		this.gtfsAgencyId = gtfsAgencyId;
	}
	public String getAgencyName() {
		return agencyName;
	}
	public void setAgencyName(String agencyName) {
		this.agencyName = agencyName;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	@Override
	public String toString() {
		return "AgencyDetail [gtfsAgencyId=" + gtfsAgencyId + ", agencyName="
				+ agencyName + ", displayName=" + displayName + "]";
	}
}