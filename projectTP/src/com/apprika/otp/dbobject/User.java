/**
 * 
 */
package com.apprika.otp.dbobject;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class User.
 *
 * @author nirmal
 */
@XmlRootElement
public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8231065322203477262L;
	private String id;	
	private String deviceId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public User() {

	}
	public User(String deviceId) {
		this.deviceId = deviceId;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", deviceId=" + deviceId + "]";
	}

}
