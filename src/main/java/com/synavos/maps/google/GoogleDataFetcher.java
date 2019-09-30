package com.synavos.maps.google;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.synavos.maps.beans.Location;
import com.synavos.maps.google.api.request.NearbyRequestBean;
import com.synavos.maps.google.api.response.NearbyResponse;
import com.synavos.maps.google.api.response.Place;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class GoogleDataFetcher.
 *
 * @author Ibraheem Faiq
 * @since Mar 22, 2018
 */
@Service
@Slf4j
public class GoogleDataFetcher {

    /**
     * Fetch near by services.
     *
     * @param lat
     *            the lat
     * @param lng
     *            the lng
     * @param radius
     *            the radius
     * @param type
     *            the type
     * @return the response entity
     */
    public NearbyResponse fetchNearByServices(final Location location, final Long radius, final List<String> types) {
	NearbyResponse nearbyResponse = null;

	final List<Place> places = new LinkedList<>();

	if (CommonUtils.isNotNull(location, radius, types)) {
	    for (final String type : types) {
		final NearbyRequestBean nearByRequest = prepareNearByRequest(location.getLatitude(),
			location.getLongitude(), radius, type);

		log.debug(log.isDebugEnabled()
			? StringUtils.concatValues("Fetching nearby Services for Request : \n\t", nearByRequest)
			: null);

		final NearbyResponse resp = processNearByRequest(nearByRequest);

		if (CommonUtils.isNotNull(resp)) {

		    if (CommonUtils.isNull(nearbyResponse)) {
			nearbyResponse = resp;
		    }

		    if (!CommonUtils.isNullOrEmptyCollection(resp.getResults())) {
			log.trace(log.isTraceEnabled()
				? StringUtils.concatValues("Request [", nearByRequest, "], Results [",
					resp.getResults(), "]")
				: null);

			places.addAll(resp.getResults());
		    }
		}
		else {
		    nearbyResponse = null;
		    break;
		}

	    }

	    if (CommonUtils.isNotNull(nearbyResponse)) {
		nearbyResponse.setResults(places);
	    }
	}

	return nearbyResponse;
    }

    private NearbyResponse processNearByRequest(final NearbyRequestBean _nearByRequest) {
	final NearbyRequestBean nearByRequest = _nearByRequest;

	final RestTemplate restTemplate = new RestTemplate();

	NearbyResponse nearbyResponse = null;

	final List<Place> places = new LinkedList<>();

	while (true) {
	    final String requestUri = nearByRequest.toUri();

	    log.debug(log.isDebugEnabled() ? StringUtils.concatValues("Quering google with request [", requestUri, "]")
		    : null);

	    final NearbyResponse _nearbyResponse = restTemplate.getForObject(requestUri, NearbyResponse.class);

	    if (quotaLimitReached(_nearbyResponse)) {
		log.warn("Quota Limit has been reached for API, will re-try in one hour.");
		try {
		    Thread.sleep(TimeUnit.MINUTES.toMillis(10));
		}
		catch (InterruptedException e) {
		    log.error("##InterruptedException## occurred while waiting to retry after quota limit reached", e);
		}
		continue;
	    }

	    if (validNearbyResponse(_nearbyResponse, nearByRequest)) {
		log.debug(log.isDebugEnabled() ? "Processing Google Maps API response..." : null);

		if (null == nearbyResponse) {
		    nearbyResponse = _nearbyResponse;
		}

		log.debug(log.isDebugEnabled()
			? StringUtils.concatValues("No. of places found [", _nearbyResponse.getResults().size(), "]")
			: null);

		if (!CommonUtils.isNullOrEmptyCollection(_nearbyResponse.getResults())) {
		    _nearbyResponse.getResults().forEach(place -> {
			if (CommonUtils.isNotNull(place, place.getPlaceId())) {
			    places.add(place);
			}
		    });
		}

		if (!StringUtils.isNullOrEmptyStr(_nearbyResponse.getNextPageToken())) {
		    waitBeforeNextPage();
		    nearByRequest.setPagetoken(_nearbyResponse.getNextPageToken());
		}
		else {
		    nearbyResponse.setNextPageToken(null);
		    nearbyResponse.setResults(places);

		    log.debug(
			    log.isDebugEnabled()
				    ? StringUtils.concatValues("Total No. of places found [",
					    nearbyResponse.getResults().size(), "]")
				    : null);
		    break;
		}
	    }
	    else {
		log.error(StringUtils.concatValues("Invalid response from google [", _nearbyResponse, "]"));
		nearbyResponse = null;
		break;
	    }
	}

	return nearbyResponse;
    }

    private boolean quotaLimitReached(final NearbyResponse _nearbyResponse) {
	return CommonUtils.isNotNull(_nearbyResponse) && "OVER_QUERY_LIMIT".equals(_nearbyResponse.getStatus());
    }

    private void waitBeforeNextPage() {
	try {
	    Thread.sleep(GoogleMapProperties.NEXT_PAGE_WAIT_TIME);
	}
	catch (final InterruptedException e) {
	    CommonUtils.logException(log, e, getClass().getName());
	}
    }

    private boolean validNearbyResponse(final NearbyResponse nearbyResponse, NearbyRequestBean nearbyRequestBean) {
	boolean valid = null != nearbyResponse
		&& Arrays.asList("OK", "ZERO_RESULTS").contains(nearbyResponse.getStatus());
	if (!valid) {
	    log.error(StringUtils.concatValues("####################\n\nAPI call Failed for [", nearbyRequestBean,
		    "]\n\n####################"));
	}

	return valid;
    }

    private NearbyRequestBean prepareNearByRequest(final Double lat, final Double lng, final Long radius,
	    final String type) {
	// @formatter:off
	return NearbyRequestBean.builder().latLong(lat, lng).key(GoogleMapProperties.API_KEY).type(type).radius(radius)
		.build();
	// @formatter:on
    }
}
