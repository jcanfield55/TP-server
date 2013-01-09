package com.nimbler.tp.dbobject;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NimblerParams implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1279370116367654906L;
	private String id;
	private String name;
	private String value;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public enum NIMBLER_PARAMS {
		BETA_USERS,
		LAST_HEALTH_CHECK_TIME

	}
}
