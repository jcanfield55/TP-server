package com.nimbler.tp.gtfs;

public class GtfsBundle {
	private String defaultAgencyId;
	private String currentDataFile;
	private String crackedDataFile;
	private String downloadUrl;

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
	public void setCrackedDataFile(String crackedDataFile) {
		this.crackedDataFile = crackedDataFile;
	}
	@Override
	public String toString() {
		return "GtfsBundle [defaultAgencyId=" + defaultAgencyId
				+ ", currentDataFile=" + currentDataFile + ", crackedDataFile="
				+ crackedDataFile + ", downloadUrl=" + downloadUrl + "]\n";
	}
}
