package com.nimbler.tp.dataobject.my511;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * 
 * @author nirmal
 *
 */
@XmlRootElement(name="RTT")
public class My511Response {
	private List<My511Agency> agencyList;

	@XmlElementWrapper(name="AgencyList")
	@XmlElement(name="Agency")
	public List<My511Agency> getAgencyList() {
		return agencyList;
	}

	public void setAgencyList(List<My511Agency> agencyList) {
		this.agencyList = agencyList;
	}

	@Override
	public String toString() {
		return "My511Response [agencyList=" + agencyList + "]";
	}
}
