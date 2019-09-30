package com.synavos.maps.beans;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class NearByServicesRequest.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NearByServicesRequest {

    /** The latitude. */
    @JsonProperty("lat")
    @NotNull(message = "lat/lng cannot be null for nearby request")
    private Double latitude;

    /** The longitude. */
    @JsonProperty("lng")
    @NotNull(message = "lat/lng cannot be null for nearby request")
    private Double longitude;

    /** The radius. */
    @NotNull(message = "radius cannot be null for nearby request")
    private Long radius;

    /** The types. */
    private List<String> types;

    /** The count. */
    private Integer count;

    private String token;

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
    public void setLatitude(Double latitude) {
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
    public void setLongitude(Double longitude) {
	this.longitude = longitude;
    }

    /**
     * Gets the radius.
     *
     * @return the radius
     */
    public Long getRadius() {
	return radius;
    }

    /**
     * Sets the radius.
     *
     * @param radius
     *            the new radius
     */
    public void setRadius(Long radius) {
	this.radius = radius;
    }

    /**
     * Gets the types.
     *
     * @return the types
     */
    public List<String> getTypes() {
	return types;
    }

    /**
     * Sets the types.
     *
     * @param types
     *            the new types
     */
    public void setTypes(List<String> types) {
	this.types = types;
    }

    /**
     * Sets the count.
     *
     * @param count
     *            the new count
     */
    public void setCount(Integer count) {
	this.count = count;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public Integer getCount() {
	return null != count && count > 0 ? count : 250;
    }

    public String getToken() {
	return token;
    }

    public void setToken(String token) {
	this.token = token;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "NearByServicesRequest [latitude=" + latitude + ", longitude=" + longitude + ", radius=" + radius
		+ ", types=" + types + ", count=" + count + "]";
    }

}
