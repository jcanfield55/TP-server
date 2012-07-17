/*
 * 
 */
package com.nimbler.tp.dataobject;

import javax.mail.Authenticator;

/**
 * The Class SmtpConfig.
 * @author nirmal
 */
public class SmtpConfig extends Authenticator {

	private Long idSmtpConfig;

	private String host;
	private String from;	
	private String userName;
	private String password;	
	private boolean enableDebug;	
	private boolean enableAuthentication;
	private int port;
	private String tls;
	private boolean isThirdParty;

	public Long getIdSmtpConfig() {
		return idSmtpConfig;
	}
	public void setIdSmtpConfig(Long idSmtpConfig) {
		this.idSmtpConfig = idSmtpConfig;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isEnableDebug() {
		return enableDebug;
	}
	public void setEnableDebug(boolean enableDebug) {
		this.enableDebug = enableDebug;
	}
	public boolean isEnableAuthentication() {
		return enableAuthentication;
	}
	public void setEnableAuthentication(boolean enableAuthentication) {
		this.enableAuthentication = enableAuthentication;
	}

	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	@Override
	public String toString() {
		return "SmtpConfig [idSmtpConfig=" + idSmtpConfig + ", host=" + host
				+ ", from=" + from + ", userName=" + userName + ", password="
				+ password + ", enableDebug=" + enableDebug
				+ ", enableAuthentication=" + enableAuthentication + ", port="
				+ port + "]";
	}
	public String getTls() {
		return tls;
	}
	public void setTls(String tls) {
		this.tls = tls;
	}
	public boolean isThirdParty() {
		return isThirdParty;
	}
	public void setThirdParty(boolean isThirdParty) {
		this.isThirdParty = isThirdParty;
	}
}