/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;

/**
 * The Class GtfsBundle.
 *
 * @author nirmal
 */
public class GtfsBundle {
	private String defaultAgencyId;
	private String currentDataFile;
	private String crackedDataFile;
	private String downloadUrl;

	private String lastUpdateDate;
	private List<String> agencyIds;
	private List<GtfsCalander> lstCalanders;
	private List<GtfsCalandeDates> lstCalandeDates;
	private boolean enableAgency = true;
	private boolean enableHashing = true;

	private AGENCY_TYPE agencyType = null;
	/**
	 * contains array of comma separated string for services that are available for particular day
	 * e.g <br />
	 * [z,y,z -- for sunday <br />
	 *  a,b,c ] -- for monday<br />
	 */
	private String[] serviceOnDays = new String[]{"","","","","","",""};
	private String[] serviceOnDaysHash  = new String[]{"","","","","","",""};;

	private Map<String, String> datesAndServiceWithException = new HashMap<String, String>();

	private boolean isExtracted = false;

	public String getDefaultAgencyId() {		
		return defaultAgencyId;
	}
	public void setDefaultAgencyId(String defaultAgencyId) {
		this.defaultAgencyId = defaultAgencyId;
	}
	public String getCurrentDataFile() {
		return currentDataFile;
	}
	public void setCurrentDataFile(String currentDataFile) {
		this.currentDataFile = currentDataFile;
	}
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getCrackedDataFile() {
		return crackedDataFile;
	}
	public String getValidFile() {
		return StringUtils.defaultIfBlank(crackedDataFile, currentDataFile);
	}
	public void setCrackedDataFile(String crackedDataFile) {
		this.crackedDataFile = crackedDataFile;
	}
	public Map<String, String> getDatesAndServiceWithException() {
		return datesAndServiceWithException;
	}
	public List<GtfsCalander> getLstCalanders() {
		return lstCalanders;
	}
	public void setLstCalanders(List<GtfsCalander> lstCalanders) {
		this.lstCalanders = lstCalanders;
	}
	public List<GtfsCalandeDates> getLstCalandeDates() {
		return lstCalandeDates;
	}
	public void setLstCalandeDates(List<GtfsCalandeDates> lstCalandeDates) {
		this.lstCalandeDates = lstCalandeDates;
	}
	public boolean isExtracted() {
		return isExtracted;
	}
	public void setExtracted(boolean isExtracted) {
		this.isExtracted = isExtracted;
	}
	public String getLastUpdateDate() {
		return lastUpdateDate;
	}
	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	public void setDatesAndServiceWithException(
			Map<String, String> datesAndServiceWithException) {
		this.datesAndServiceWithException = datesAndServiceWithException;
	}
	public String[] getServiceOnDays() {
		return serviceOnDays;
	}

	public String[] getServiceOnDaysHash() {
		return serviceOnDaysHash;
	}
	public boolean isEnableAgency() {
		return enableAgency;
	}
	public void setEnableAgency(boolean enableAgency) {
		this.enableAgency = enableAgency;
	}
	public void setServiceOnDaysHash(String[] serviceOnDaysHash) {
		this.serviceOnDaysHash = serviceOnDaysHash;
	}
	public boolean isEnableHashing() {
		return enableHashing;
	}
	public void setEnableHashing(boolean enableHashing) {
		this.enableHashing = enableHashing;
	}
	public void setServiceOnDays(String[] serviceOnDays) {
		this.serviceOnDays = serviceOnDays;
	}
	public List<String> getAgencyIds() {
		return agencyIds;
	}
	public void setAgencyIds(List<String> agencyIds) {
		this.agencyIds = agencyIds;
	}

	public AGENCY_TYPE getAgencyType() {
		return agencyType;
	}
	public void setAgencyType(AGENCY_TYPE agencyType) {
		this.agencyType = agencyType;
	}
	@Override
	public String toString() {
		return "GtfsBundle [defaultAgencyId=" + defaultAgencyId
				+ ", currentDataFile=" + currentDataFile + ", crackedDataFile="
				+ crackedDataFile + ", downloadUrl=" + downloadUrl
				+ ", lstCalanders=" + lstCalanders + ", lstCalandeDates="
				+ lstCalandeDates + ", isExtracted=" + isExtracted + "]\n";
	}
}
