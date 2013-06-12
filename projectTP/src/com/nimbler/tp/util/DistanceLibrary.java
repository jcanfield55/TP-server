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

package com.nimbler.tp.util;
import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;
import static org.apache.commons.math3.util.FastMath.toDegrees;
import static org.apache.commons.math3.util.FastMath.toRadians;

import com.nimbler.tp.dataobject.wmata.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;


public class DistanceLibrary  {

	private static final DistanceLibrary instance = new DistanceLibrary();

	public static final double RADIUS_OF_EARTH_IN_KM = 6371.01;

	// Max admissible lat/lon delta for approximated distance computation
	public static final double MAX_LAT_DELTA_DEG = 4.0;
	public static final double MAX_LON_DELTA_DEG = 4.0;
	// 1 / Max over-estimation error of approximated distance,
	// for delta lat/lon in given range
	public static final double MAX_ERR_INV = 0.999462;

	/* (non-Javadoc)
	 * @see org.opentripplanner.common.geometry.DistanceLibrary#distance(com.vividsolutions.jts.geom.Coordinate, com.vividsolutions.jts.geom.Coordinate)
	 */
	public static final double distance(Point from, Point to) {
		return distance(from.getY(), from.getX(), to.getY(), from.getX());
	}

	/* (non-Javadoc)
	 * @see org.opentripplanner.common.geometry.DistanceLibrary#fastDistance(com.vividsolutions.jts.geom.Coordinate, com.vividsolutions.jts.geom.Coordinate)
	 */
	public static final double fastDistance(Coordinate from, Coordinate to) {
		return fastDistance(from.y, from.x, to.y, to.x);
	}

	/* (non-Javadoc)
	 * @see org.opentripplanner.common.geometry.DistanceLibrary#distance(double, double, double, double)
	 */
	public static final double distance(double lat1, double lon1, double lat2, double lon2) {
		return distance(lat1, lon1, lat2, lon2, RADIUS_OF_EARTH_IN_KM * 1000);
	}

	/* (non-Javadoc)
	 * @see org.opentripplanner.common.geometry.DistanceLibrary#fastDistance(double, double, double, double)
	 */
	public static final double fastDistance(double lat1, double lon1, double lat2, double lon2) {
		return fastDistance(lat1, lon1, lat2, lon2, RADIUS_OF_EARTH_IN_KM * 1000);
	}

	/* (non-Javadoc)
	 * @see org.opentripplanner.common.geometry.DistanceLibrary#distance(double, double, double, double, double)
	 */
	public static final double distance(double lat1, double lon1, double lat2, double lon2,
			double radius) {
		// http://en.wikipedia.org/wiki/Great-circle_distance
		lat1 = toRadians(lat1); // Theta-s
		lon1 = toRadians(lon1); // Lambda-s
		lat2 = toRadians(lat2); // Theta-f
		lon2 = toRadians(lon2); // Lambda-f

		double deltaLon = lon2 - lon1;

		double y = sqrt(p2(cos(lat2) * sin(deltaLon))
				+ p2(cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)));
		double x = sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(deltaLon);

		return radius * atan2(y, x);
	}


	/**
	 * Approximated, fast and under-estimated equirectangular distance between two points.
	 * Works only for small delta lat/lon, fall-back on exact distance if not the case.
	 * See: http://www.movable-type.co.uk/scripts/latlong.html
	 */
	public static final double fastDistance(double lat1, double lon1, double lat2, double lon2,
			double radius) {
		if (abs(lat1 - lat2) > MAX_LAT_DELTA_DEG
				|| abs(lon1 - lon2) > MAX_LON_DELTA_DEG)
			return distance(lat1, lon1, lat2, lon2, radius);

		double dLat = toRadians(lat2 - lat1);
		double dLon = toRadians(lon2 - lon1) * cos(toRadians((lat1 + lat2) / 2));
		return radius * sqrt(dLat * dLat + dLon * dLon) * MAX_ERR_INV;
	}

	private static final double p2(double a) {
		return a * a;
	}

	/** this is an overestimate */
	public static double metersToDegrees(double distance) {
		return 360 * distance / (2 * Math.PI * RADIUS_OF_EARTH_IN_KM * 1000);
	}

	public final Envelope bounds(double lat, double lon, double latDistance,
			double lonDistance) {

		double radiusOfEarth = RADIUS_OF_EARTH_IN_KM * 1000;

		double latRadians = toRadians(lat);
		double lonRadians = toRadians(lon);

		double latRadius = radiusOfEarth;
		double lonRadius = cos(latRadians) * radiusOfEarth;

		double latOffset = latDistance / latRadius;
		double lonOffset = lonDistance / lonRadius;

		double latFrom = toDegrees(latRadians - latOffset);
		double latTo = toDegrees(latRadians + latOffset);

		double lonFrom = toDegrees(lonRadians - lonOffset);
		double lonTo = toDegrees(lonRadians + lonOffset);

		return new Envelope(new Coordinate(lonFrom, latFrom), new Coordinate(lonTo, latTo));
	}

	public static DistanceLibrary getInstance() {
		return instance;
	}
}
