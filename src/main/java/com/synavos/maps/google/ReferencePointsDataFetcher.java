package com.synavos.maps.google;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synavos.maps.beans.ReferencePoint;
import com.synavos.maps.cache.PlacesCache;
import com.synavos.maps.google.api.response.NearbyResponse;
import com.synavos.maps.google.api.response.Place;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;
import com.synavos.maps.utils.TaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReferencePointsDataFetcher {

    @Autowired
    private GoogleDataFetcher googleDataFetcher;

    public void fetchReferencePointsData(final List<ReferencePoint> referencePoints) {
	if (!CommonUtils.isNullOrEmptyCollection(referencePoints)) {

	    log.info(log.isInfoEnabled() ? "Querying Google for Places Data..." : null);

	    TaskExecutor.executeTasks(prepareTasks(referencePoints), GoogleMapProperties.MAX_PARALLEL_TASKS);

	    log.info(log.isInfoEnabled() ? "Finished Loading data from Google Places API" : null);
	}
    }

    private List<Callable<Object>> prepareTasks(final List<ReferencePoint> referencePoints) {
	return referencePoints.stream().map(req -> {
	    return new Callable<Object>() {

		@Override
		public Object call() throws Exception {
		    try {
			fetchData(req);
		    }
		    catch (final Exception ex) {
			log.error("Error occurred while fetching google data", ex);
		    }
		    return Boolean.TRUE;
		}

	    };
	}).collect(Collectors.toList());
    }

    private void fetchData(final ReferencePoint request) {
	if (CommonUtils.isNotNull(request)) {
	    log.debug(log.isDebugEnabled()
		    ? StringUtils.concatValues("Looking for places data at location [", request, "]")
		    : null);

	    final NearbyResponse nearbyResponse = googleDataFetcher.fetchNearByServices(request.getLocation(),
		    request.getDistance(), request.getTypes());

	    if (CommonUtils.isNotNull(nearbyResponse)) {
		final Collection<Place> places = nearbyResponse.getResults();

		if (!CommonUtils.isNullOrEmptyCollection(places)) {
		    log.trace(
			    log.isTraceEnabled() ? StringUtils.concatValues("Adding following places in cache", places)
				    : null);
		    places.forEach(PlacesCache.getInstance()::addPlace);
		}

	    }
	    else {
		log.error(StringUtils.concatValues("Invalid API response from google for [", request, "]"));
	    }
	}
    }

}
