package com.nimbler.tp.dataobject;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Credential")
public class Credential {
	
	private List<UserCredential> userCredentials;

	@XmlElement(name="UserCredential")
	public List<UserCredential> getUserCredentials() {
		return userCredentials;
	}

	public void setUserCredentials(List<UserCredential> userCredentials) {
		this.userCredentials = userCredentials;
	}

	@Override
	public String toString() {
		return "Credential [userCredentials=" + userCredentials + "]";
	}
	
}
