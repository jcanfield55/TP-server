/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package com.apprika.otp.dataobject;

import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement
public class TPResponse {

	private HashMap<String, String> requestParameters;
	private List<Object> lstObjects;
	private Status status;

	public TPResponse() {
	}

	public HashMap<String, String> getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(HashMap<String, String> requestParameters) {
		this.requestParameters = requestParameters;
	}

	public List<Object> getLstObjects() {
		return lstObjects;
	}

	public void setLstObjects(List<Object> lstObjects) {
		this.lstObjects = lstObjects;
	}


	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	//	public static OTPResponse sucess() {
	//		OTPResponse response = new OTPResponse();
	//		response.setStatus(new ResponseStatus(TP_CODES.SUCESS));
	//		return response;
	//	}

}