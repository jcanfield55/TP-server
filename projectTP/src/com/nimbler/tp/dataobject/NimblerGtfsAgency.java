package com.nimbler.tp.dataobject;

import java.io.Serializable;
import java.util.List;

import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;

public class NimblerGtfsAgency implements Serializable{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 246845095620681145L;
	/**
	 * ordinal of {@link AGENCY_TYPE}
	 */
	private Integer nimberAgencyId;

	private String lastUpdate;

	private String exclusionType;

	private AGENCY_TYPE agencyType;

	List<AgencyDetail> agencies;

	private String advisoryName;

	public Integer getNimberAgencyId() {
		return nimberAgencyId;
	}

	public void setNimberAgencyId(Integer nimberAgencyId) {
		this.nimberAgencyId = nimberAgencyId;
	}

	public String getAdvisoryName() {
		return advisoryName;
	}

	public void setAdvisoryName(String advisoryName) {
		this.advisoryName = advisoryName;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public AGENCY_TYPE getAgencyType() {
		return agencyType;
	}

	public void setAgencyType(AGENCY_TYPE agencyType) {
		this.agencyType = agencyType;
	}

	public String getExclusionType() {
		return exclusionType;
	}

	public void setExclusionType(String exclusionType) {
		this.exclusionType = exclusionType;
	}

	public List<AgencyDetail> getAgencies() {
		return agencies;
	}

	public void setAgencies(List<AgencyDetail> agencies) {
		this.agencies = agencies;
	}
}
