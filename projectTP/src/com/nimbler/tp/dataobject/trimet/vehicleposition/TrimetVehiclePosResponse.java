/*
 * @author nirmal
 */
package com.nimbler.tp.dataobject.trimet.vehicleposition;

import java.io.Serializable;

/**
 * The Class TrimetVehiclePosResponse.
 *
 * @author nirmal
 */
public class TrimetVehiclePosResponse implements Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4962700144975111258L;
	TrimetResultSet resultSet;
	Error error;

	public TrimetResultSet getResultSet() {
		return resultSet;
	}
	public void setResultSet(TrimetResultSet resultSet) {
		this.resultSet = resultSet;
	}
	@Override
	public String toString() {
		return "TrimetVehiclePosResponse [resultSet=" + resultSet + "]";
	}

	/**
	 * The Class Error.
	 *
	 * @author nirmal
	 */
	public static class Error implements Serializable{

		/**
		 * 
		 */
		private static final long	serialVersionUID	= -6613680418050420603L;
		String content;
	}

}
