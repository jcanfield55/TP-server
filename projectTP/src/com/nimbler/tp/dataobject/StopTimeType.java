/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (props, at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package com.nimbler.tp.dataobject;

import javax.xml.bind.annotation.XmlRootElement;

import org.onebusaway.gtfs.model.Trip;

@XmlRootElement
public class StopTimeType {


	public StopTimeType() {
	}
	public StopTimeType(Trip trip) {
		this.agencyId = trip.getId().getAgencyId();
		this.tripId = trip.getId().getId();
		this.headsign = trip.getTripHeadsign();
		this.routeShortName = trip.getRouteShortName();
		this.routeLongName = trip.getRoute().getLongName();
		this.routeId = trip.getRoute().getId().getId();
	}

	public  String agencyId;

	public  String tripId;
	public Long startTime;
	public String headsign;
	public String routeId;
	public String routeShortName;
	public String routeLongName;
	public String message;

	public boolean isValid(){
		return startTime!=null;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StopTimeType ["
				+ (agencyId != null ? "agencyId=" + agencyId + ", " : "")
				+ (tripId != null ? "tripId=" + tripId + ", " : "")
				+ (startTime != null ? "startTime=" + startTime + ", " : "")
				+ (headsign != null ? "headsign=" + headsign + ", " : "")
				+ (routeId != null ? "routeId=" + routeId + ", " : "")
				+ (routeShortName != null ? "routeShortName=" + routeShortName
						+ ", " : "")
						+ (routeLongName != null ? "routeLongName=" + routeLongName
								+ ", " : "")
								+ (message != null ? "message=" + message : "") + "]";
	}

}
