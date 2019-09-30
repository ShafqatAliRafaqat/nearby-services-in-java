package com.synavos.maps.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.synavos.maps.beans.Location;
import com.synavos.maps.beans.NearByServicesRequest;
import com.synavos.maps.beans.ServiceResponse;
import com.synavos.maps.cache.PlacesCache;
import com.synavos.maps.cache.ReferencePointsCache;
import com.synavos.maps.google.api.response.Place;
import com.synavos.maps.models.ReferencePointModel;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;
import com.synavos.maps.utils.TokenValidator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NearByService {

    @Autowired
    private PlacesService placesService;

    @Autowired
    private TokenValidator tokenValidator;

    /**
     * Fetch nearby services.
     *
     * @param request
     *            the request
     * @return the nearby service response
     */
    public ServiceResponse fetchNearbyServices(final NearByServicesRequest request, final String token) {
	final Long startTime = System.nanoTime();

	final ServiceResponse nearbyServiceResponse = new ServiceResponse(HttpStatus.NO_CONTENT, "No data found");

	if (!tokenValidator.isValidToken(token)) {
	    nearbyServiceResponse.setHttpStatus(HttpStatus.UNAUTHORIZED);
	    nearbyServiceResponse.setMsg("UNAUTHORIZED");
	}
	else {
	    Collection<Place> results = new ArrayList<>(1);

	    if (CommonUtils.isNotNull(request)) {
		log.info(StringUtils.concatValues("Fetching nearby services for [", request.toString(), "]"));

		// validate request
		validateRequest(request);

		// create location for request
		final Location reqLocation = new Location(request.getLatitude(), request.getLongitude());

		results = PlacesCache.getInstance().searchNearbyPlaces(reqLocation, request.getRadius().intValue(),
			request.getTypes(), request.getCount());

		if (!CommonUtils.isNullOrEmptyCollection(results)) {
		    nearbyServiceResponse.setHttpStatus(HttpStatus.OK);
		    nearbyServiceResponse.setMsg("OK");
		    nearbyServiceResponse.setPlaces(results);
		}
		else if (!ReferencePointsCache.getInstance().isAreaScanned(reqLocation)) {
		    nearbyServiceResponse.setHttpStatus(HttpStatus.OK);
		    nearbyServiceResponse.setMsg("AREA_NOT_SUPPORTED");
		    nearbyServiceResponse.setPlaces(Collections.emptyList());
		}

		// scan requested area to see if we are missing any portion of it
		scanRegion(request);

		log.debug(
			log.isDebugEnabled()
				? StringUtils.concatValues("Nearby services fetched count for [", request.toString(),
					"] : [", CommonUtils.isNotNull(results) ? results.size() : 0, "] ")
				: null);
	    }
	}

	log.info(
		log.isInfoEnabled()
			? StringUtils.concatValues("Time taken by nearby service [",
				TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime), "ms]")
			: null);

	return nearbyServiceResponse;
    }

    private void validateRequest(final NearByServicesRequest request) {
	if (request.getRadius() > GoogleMapProperties.MAX_DISTANCE_NEARBY_KM) {
	    log.warn("Changing radius to max distance allowed");
	    request.setRadius(GoogleMapProperties.MAX_DISTANCE_NEARBY_KM);
	}
    }

    private void scanRegion(final NearByServicesRequest request) {
	if (CommonUtils.isNullOrEmptyCollection(request.getTypes())
		|| CommonUtils.isAnyCommon(GoogleMapProperties.GOOGLE_SUPPORTED_PLACE_TYPES, request.getTypes())) {

	    final ReferencePointModel referencePointModel = preapreReferencePointModel(request);
	    placesService.submitAreaScanRequest(referencePointModel);

	    log.debug(log.isDebugEnabled() ? StringUtils.concatValues("Area scan request submitted for [", request, "]")
		    : null);
	}
    }

    private ReferencePointModel preapreReferencePointModel(final NearByServicesRequest request) {
	final long requestedRadiusInMeters = request.getRadius() * 1000;
	final int depth = (int) (requestedRadiusInMeters / GoogleMapProperties.DIST_REF_POINT_METERS);

	final ReferencePointModel model = new ReferencePointModel();

	model.setDepth(depth);
	model.setLatitude(request.getLatitude());
	model.setLongitude(request.getLongitude());

	final String placeTypeStr = org.springframework.util.StringUtils
		.collectionToCommaDelimitedString(GoogleMapProperties.GOOGLE_SUPPORTED_PLACE_TYPES);

	model.setPlaceTypes(placeTypeStr);

	return model;
    }

}
