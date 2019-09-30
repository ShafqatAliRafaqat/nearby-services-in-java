package com.synavos.maps.beans;

import java.util.regex.Pattern;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.synavos.maps.kdtree.KdTree.XYZPoint;
import com.synavos.maps.utils.CommonUtils;

/**
 * The Class Location.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location extends XYZPoint {

    /**
     * Instantiates a new location.
     *
     * @param latitude
     *            the latitude
     * @param longitude
     *            the longitude
     */
    public Location(final double latitude, final double longitude) {
	super(latitude, longitude);
	this.latitude = latitude;
	this.longitude = longitude;
    }

    public static Location parseLocation(final String latlon) {
	Location location = null;
	final String[] coordinates = latlon.split(Pattern.quote(","));
	if (!CommonUtils.isNullOrEmptyArray(coordinates) && coordinates.length == 2) {
	    try {
		final double latitude = Double.parseDouble(coordinates[0]);
		final double longitude = Double.parseDouble(coordinates[1]);

		location = new Location(latitude, longitude);
	    }
	    catch (final Exception ex) {
		// do nothing
	    }
	}

	return location;
    }

    /**
     * Instantiates a new location.
     */
    public Location() {
	super();
    }

    /** The latitude. */
    @JsonProperty("lat")
    private Double latitude;

    /** The longitude. */
    @JsonProperty("lng")
    private Double longitude;

    /**
     * Gets the latitude.
     *
     * @return the latitude
     */
    public Double getLatitude() {
	return latitude;
    }

    /**
     * Sets the latitude.
     *
     * @param latitude
     *            the new latitude
     */
    public void setLatitude(final Double latitude) {
	this.latitude = latitude;
    }

    /**
     * Gets the longitude.
     *
     * @return the longitude
     */
    public Double getLongitude() {
	return longitude;
    }

    /**
     * Sets the longitude.
     *
     * @param longitude
     *            the new longitude
     */
    public void setLongitude(final Double longitude) {
	this.longitude = longitude;
    }

    @Override
    public String toString() {
	return this.latitude + "," + this.longitude;
    }

    @Override
    public double getX() {
	return super.toX(getLatitude(), getLongitude());
    }

    @Override
    public double getY() {
	return super.toY(getLatitude(), getLongitude());
    }

    @Override
    public double getZ() {
	return super.toZ(getLatitude());
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
	result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Location other = (Location) obj;
	if (latitude == null) {
	    if (other.latitude != null)
		return false;
	}
	else if (!latitude.equals(other.latitude))
	    return false;
	if (longitude == null) {
	    if (other.longitude != null)
		return false;
	}
	else if (!longitude.equals(other.longitude))
	    return false;
	return true;
    }

    public GeoJsonPoint toGeoJsonPoint() {
	return new GeoJsonPoint(this.getLatitude(), this.getLongitude());
    }

    public XYZPoint toXYZPoint() {
	return new XYZPoint(getLatitude(), getLongitude());
    }
}
