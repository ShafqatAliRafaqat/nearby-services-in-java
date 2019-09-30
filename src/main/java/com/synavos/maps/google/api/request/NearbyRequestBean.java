package com.synavos.maps.google.api.request;

import java.lang.reflect.Field;

import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.ToString;

/**
 * The Class NearbyRequestBean.
 *
 * @author Ibraheem Faiq
 * @since Mar 22, 2018
 */
@Component
@lombok.Getter
@lombok.Setter(value = AccessLevel.PRIVATE)
@ToString
public class NearbyRequestBean {

    /**
     * Instantiates a new nearby request bean.
     */
    private NearbyRequestBean() {
	super();
    }

    /** The latitude. */
    private String location;

    /** The type. */
    private String type;

    /** The radius. */
    private Long radius;

    /** The key. */
    private String key;

    /** The pagetoken. */
    private String pagetoken;

    /**
     * Builder.
     *
     * @return the nearby request builder
     */
    public static NearbyRequestBuilder builder() {
	return new NearbyRequestBuilder();
    }

    public void setPagetoken(String pagetoken) {
	this.pagetoken = pagetoken;
    }

    /**
     * To URI.
     *
     * @return the string
     */
    public String toUri() {
	boolean locationAdded = false;
	boolean keyAdded = false;
	boolean radiusAdded = false;

	final StringBuilder uri = new StringBuilder();
	uri.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");

	final Field[] fields = this.getClass().getDeclaredFields();
	for (final Field field : fields) {
	    try {
		field.setAccessible(true);

		final Object fieldValue = field.get(this);
		if (null != fieldValue) {
		    final String fieldName = field.getName();

		    if ("key".equals(fieldName)) {
			keyAdded = true;
		    }
		    else if ("location".equals(fieldName)) {
			locationAdded = true;
		    }
		    else if ("radius".equals(fieldName)) {
			radiusAdded = true;
		    }

		    uri.append(fieldName);
		    uri.append("=");
		    uri.append(fieldValue);
		    uri.append("&");
		}
	    }
	    catch (final Exception ex) {
		return null;
	    }
	}

	if (!locationAdded || !keyAdded || !radiusAdded) {
	    return null;
	}

	uri.deleteCharAt(uri.length() - 1);

	return uri.toString();
    }

    /**
     * The Class Builder.
     */
    public static class NearbyRequestBuilder {

	/** The nearby request bean. */
	private NearbyRequestBean nearbyRequestBean;

	/**
	 * Instantiates a new nearby request builder.
	 */
	public NearbyRequestBuilder() {
	    this.nearbyRequestBean = new NearbyRequestBean();
	}

	/**
	 * Lat long.
	 *
	 * @param latitude
	 *            the latitude
	 * @param longitude
	 *            the longitude
	 * @return the nearby request builder
	 */
	public NearbyRequestBuilder latLong(double latitude, double longitude) {
	    nearbyRequestBean.setLocation(latitude + "," + longitude);
	    return this;
	}

	/**
	 * Type.
	 *
	 * @param type
	 *            the type
	 * @return the nearby request builder
	 */
	public NearbyRequestBuilder type(final String type) {
	    nearbyRequestBean.setType(type);
	    return this;
	}

	/**
	 * Radius.
	 *
	 * @param radius
	 *            the radius
	 * @return the nearby request builder
	 */
	public NearbyRequestBuilder radius(final Long radius) {
	    nearbyRequestBean.setRadius(radius);
	    return this;
	}

	/**
	 * Key.
	 *
	 * @param key
	 *            the key
	 * @return the nearby request builder
	 */
	public NearbyRequestBuilder key(final String key) {
	    nearbyRequestBean.setKey(key);
	    return this;
	}

	/**
	 * Pagetoken.
	 *
	 * @param pagetoken
	 *            the pagetoken
	 * @return the nearby request builder
	 */
	public NearbyRequestBuilder pagetoken(final String pagetoken) {
	    nearbyRequestBean.setPagetoken(pagetoken);
	    return this;
	}

	/**
	 * Builds the.
	 *
	 * @return the nearby request bean
	 */
	public NearbyRequestBean build() {
	    return this.nearbyRequestBean;
	}

    }
}
