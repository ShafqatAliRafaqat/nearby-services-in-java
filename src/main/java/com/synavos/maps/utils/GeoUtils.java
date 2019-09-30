package com.synavos.maps.utils;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import com.synavos.maps.beans.Location;

/**
 * The Class GeoUtils.
 *
 * @author Ibraheem Faiq
 * @since Mar 26, 2018
 */
public class GeoUtils {

    /** The Constant EARTH_RADIUS_METERS. */
    public static final Double EARTH_RADIUS_METERS = 6371000D;

    private GeoUtils() {
	super();
    }

    /**
     * Find reference point.
     *
     * @param location
     *            the location
     * @param distance
     *            the distance
     * @param bearing
     *            the bearing
     */
    public static Location findReferencePoint(final Location location, final Long distance, Double angle) {
	if (!CommonUtils.isNotNull(location, distance, angle)) {
	    return null;
	}

	final double lat1 = toRadians(location.getLatitude());
	final double lng1 = toRadians(location.getLongitude());
	final double angularDistance = distance / EARTH_RADIUS_METERS;

	final double bearing = toRadians(angle);

	final double lat2 = asin(sin(lat1) * cos(angularDistance) + cos(lat1) * sin(angularDistance) * cos(bearing));
	final double lng2 = lng1
		+ atan2(sin(bearing) * sin(angularDistance) * cos(lat1), cos(angularDistance) - sin(lat1) * sin(lat2));

	Location refLocation = new Location(truncate(toDegrees(lat2)), truncate(toDegrees(lng2)));

	return refLocation;
    }

    private static Double truncate(Double value) {
	return CommonUtils.truncateDecimal(value, 7);
    }

    /**
     * Distance.
     *
     * @param loc1
     *            the loc 1
     * @param loc2
     *            the loc 2
     * @return the double
     */
    public static Double distance(final Location loc1, final Location loc2) {
	Double distance = null;

	if (CommonUtils.isNotNull(loc1, loc2)) {
	    final double lat1 = toRadians(loc1.getLatitude());
	    final double lat2 = toRadians(loc2.getLatitude());

	    final double deltaLat = toRadians(loc2.getLatitude() - loc1.getLatitude());
	    final double deltaLng = toRadians(loc2.getLongitude() - loc1.getLongitude());

	    final double a = (sin(deltaLat / 2) * sin(deltaLat / 2))
		    + cos(lat1) * cos(lat2) * (sin(deltaLng / 2) * sin(deltaLng / 2));

	    final double c = 2 * atan2(sqrt(a), sqrt(1 - a));

	    distance = EARTH_RADIUS_METERS * c;
	}

	return distance;
    }

    public static Long estimatedDistance(final Location loc1, final Location loc2) {
	if (CommonUtils.isNotNull(loc1, loc2)) {
	    Double accurateDistance = distance(loc1, loc2);
	    return Double.valueOf(CommonUtils.round(accurateDistance / 1000, 0)).longValue();
	}

	return null;
    }

}
