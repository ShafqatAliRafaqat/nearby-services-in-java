package com.synavos.maps.google;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.web.client.RestTemplate;

import com.synavos.maps.google.api.request.PlaceDetailRequestBean;
import com.synavos.maps.google.api.response.PlaceDetails;
import com.synavos.maps.google.api.response.PlaceDetailsApiResponse;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;
import com.synavos.maps.utils.TaskExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class PlaceDetailsFetcher.
 *
 * @author Ibraheem Faiq
 * @since Apr 20, 2018
 */
@Slf4j
public class PlaceDetailsFetcher {

    /** The rest template. */
    private final RestTemplate restTemplate;

    public PlaceDetailsFetcher() {
	this.restTemplate = new RestTemplate();
    }

    public List<PlaceDetails> fetchDetails(final List<String> placeIds) {
	List<PlaceDetails> placeDetails = null;

	if (!CommonUtils.isNullOrEmptyCollection(placeIds)) {
	    placeDetails = Collections.synchronizedList(new LinkedList<>());

	    final List<Callable<Object>> tasks = new ArrayList<>(placeIds.size());
	    for (final String placeId : placeIds) {
		if (!StringUtils.isNullOrEmptyStr(placeId)) {
		    tasks.add(buildTask(placeDetails, placeId));
		}
	    }

	    TaskExecutor.executeTasks(tasks, GoogleMapProperties.MAX_PARALLEL_TASKS);
	}

	return placeDetails;
    }

    private Callable<Object> buildTask(final List<PlaceDetails> placeDetails, final String placeId) {
	return new Callable<Object>() {

	    @Override
	    public Object call() throws Exception {
		final PlaceDetails placeDetail = fetchDetails(
			new PlaceDetailRequestBean(GoogleMapProperties.API_KEY, placeId));

		if (CommonUtils.isNotNull(placeDetail)) {
		    placeDetails.add(placeDetail);
		    return Boolean.TRUE;
		}

		return Boolean.FALSE;
	    }
	};
    }

    /**
     * Fetch details.
     *
     * @param request
     *            the request
     * @return the place details api response
     */
    public PlaceDetails fetchDetails(final PlaceDetailRequestBean request) {
	PlaceDetails placeDetails = null;
	if (CommonUtils.isNotNull(request)) {
	    final String uri = request.toUri();

	    if (!StringUtils.isNullOrEmptyStr(uri)) {
		while (true) {
		    log.debug(log.isDebugEnabled() ? StringUtils.concatValues("Fetching place details for [", uri, "]")
			    : null);

		    final PlaceDetailsApiResponse apiResponse = restTemplate.getForObject(uri,
			    PlaceDetailsApiResponse.class);

		    if (quotaLimitReached(apiResponse)) {
			log.warn("Quota Limit has been reached for API, will re-try in one hour.");
			try {
			    Thread.sleep(TimeUnit.HOURS.toMillis(1));
			}
			catch (InterruptedException e) {
			    log.error(
				    "##InterruptedException## occurred while waiting to retry after quota limit reached",
				    e);
			}
			continue;
		    }

		    if (validResponse(apiResponse)) {
			placeDetails = apiResponse.getResult();

			log.debug(log.isDebugEnabled()
				? StringUtils.concatValues("Details for placeId[", placeDetails.getPlaceId(), "] : [",
					placeDetails, "]")
				: null);
		    }
		    else if (log.isDebugEnabled()) {
			log.debug(StringUtils.concatValues("Invalid Response for Place Details request [", request,
				"] : [", apiResponse, "]"));
		    }

		    break;
		}

	    }
	    else {
		log.warn(StringUtils.concatValues("##InvalidRequest## [", request, "]"));
	    }
	}

	return placeDetails;
    }

    private boolean quotaLimitReached(final PlaceDetailsApiResponse apiResponse) {
	return CommonUtils.isNotNull(apiResponse) && "OVER_QUERY_LIMIT".equals(apiResponse.getStatus());
    }

    private boolean validResponse(final PlaceDetailsApiResponse apiResponse) {
	return CommonUtils.isNotNull(apiResponse) && CommonUtils.isNotNull(apiResponse.getResult())
		&& "OK".equals(apiResponse.getStatus());
    }

}
