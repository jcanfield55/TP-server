package com.nimbler.tp.dataobject.flurry;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;
/**
 * 
 * @author nirmal
 *
 */
public class FlurryEventResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8037264062401996478L;
	private Integer code;
	private String message;

	@SerializedName("@type")
	private String type;

	@SerializedName("@reportReady")
	private String reportReady;

	@SerializedName("@version")
	private String version;

	@SerializedName("@generatedDate")
	private String generatedDate;

	@SerializedName("report")
	private FlurryReportUrl reportUrl;


	public Integer getCode() {
		return code;
	}


	public FlurryReportUrl getReportUrl() {
		return reportUrl;
	}


	public void setReportUrl(FlurryReportUrl reportUrl) {
		this.reportUrl = reportUrl;
	}


	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getGeneratedDate() {
		return generatedDate;
	}

	public void setGeneratedDate(String generatedDate) {
		this.generatedDate = generatedDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getReportReady() {
		return reportReady;
	}

	public void setReportReady(String reportReady) {
		this.reportReady = reportReady;
	}


	public boolean isMsg(){
		if(code!=null || message!=null)
			return true;
		return false;
	}

	public static class FlurryReportUrl {
		@SerializedName("@reportUri")
		private String reportUri;
		@SerializedName("@reportId")
		private String reportId;
		@SerializedName("@format")
		private String format;

		public String getReportUri() {
			return reportUri;
		}
		public void setReportUri(String reportUri) {
			this.reportUri = reportUri;
		}
		public String getReportId() {
			return reportId;
		}
		public void setReportId(String reportId) {
			this.reportId = reportId;
		}
		public String getFormat() {
			return format;
		}
		public void setFormat(String format) {
			this.format = format;
		}



	}

	@Override
	public String toString() {
		return "FlurryEventResponse [code=" + code + ", message=" + message
				+ ", type=" + type + ", reportReady=" + reportReady
				+ ", version=" + version + ", generatedDate=" + generatedDate
				+ ", reportUrl=" + reportUrl + "]";
	}
}
