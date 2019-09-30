package com.synavos.maps.google.api.request;

import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;

/**
 * The Class PlaceDetailRequestBean.
 *
 * @author Ibraheem Faiq
 * @since Apr 20, 2018
 */
public class PlaceDetailRequestBean {

    /** The api key. */
    private String apiKey;

    /** The place id. */
    private String placeId;

    /**
     * Instantiates a new place detail request bean.
     *
     * @param apiKey
     *            the api key
     * @param placeId
     *            the place id
     */
    public PlaceDetailRequestBean(final String apiKey, final String placeId) {
	this.apiKey = apiKey;
	this.placeId = placeId;
    }

    /**
     * To uri.
     *
     * @return the string
     */
    public String toUri() {
	if (!StringUtils.isNullOrEmptyStr(this.apiKey, this.placeId)) {
	    final StringBuilder uriBuilder = new StringBuilder(
		    "https://maps.googleapis.com/maps/api/place/details/json?");

	    uriBuilder.append("placeid=");
	    uriBuilder.append(placeId);
	    uriBuilder.append("&");
	    uriBuilder.append("fields=");
	    uriBuilder.append("address_component,formatted_address,id,name,permanently_closed,place_id,url,formatted_phone_number,international_phone_number,opening_hours,website");
	    uriBuilder.append("&");
	    uriBuilder.append("key=");
	    uriBuilder.append(apiKey);

	    return uriBuilder.toString();
	}
	else {
	    return null;
	}
    }

    @Override
    public String toString() {
	String uri = this.toUri();
	return CommonUtils.isNotNull(uri) ? uri
		: StringUtils.concatValues("Place id [", this.placeId, "], Api Key [", this.apiKey, "]");
    }

}
