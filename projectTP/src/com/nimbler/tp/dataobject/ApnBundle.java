/*
 * @author nirmal
 */
package com.nimbler.tp.dataobject;

import com.nimbler.tp.util.TpConstants.NIMBLER_APP_TYPE;

/**
 * The Class ApnBundle - details about apn certificates.
 *
 * @author nirmal
 */
public class ApnBundle{

	private String password;
	//	APN_CERT_TYPE, dev-prod
	private int certType;
	private String KEYSTORE_P12_FILE;
	private  NIMBLER_APP_TYPE appType;

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getCertType() {
		return certType;
	}
	public void setCertType(int certType) {
		this.certType = certType;
	}
	public String getKEYSTORE_P12_FILE() {
		return KEYSTORE_P12_FILE;
	}
	public void setKEYSTORE_P12_FILE(String kEYSTORE_P12_FILE) {
		KEYSTORE_P12_FILE = kEYSTORE_P12_FILE;
	}
	public NIMBLER_APP_TYPE getAppType() {
		return appType;
	}
	public void setAppType(NIMBLER_APP_TYPE appType) {
		this.appType = appType;
	}
	@Override
	public String toString() {
		return "ApnBundle [password=" + password + ", certType=" + certType
				+ ", KEYSTORE_P12_FILE=" + KEYSTORE_P12_FILE + ", appType="
				+ appType + "]";
	}
}