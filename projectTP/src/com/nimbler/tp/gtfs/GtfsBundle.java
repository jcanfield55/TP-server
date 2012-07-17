package com.nimbler.tp.gtfs;

public class GtfsBundle {
	private String defaultAgencyId;
	private String currentDataFile;
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
	@Override
	public String toString() {
		return "GtfsBundle [defaultAgencyId=" + defaultAgencyId
				+ ", currentDataFile=" + currentDataFile + ", downloadUrl="
				+ downloadUrl + "]";
	}
}
