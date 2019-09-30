package com.synavos.maps.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.synavos.maps.beans.ServiceResponse;
import com.synavos.maps.beans.TextSearchRequest;
import com.synavos.maps.cache.PlacesCache;
import com.synavos.maps.google.api.response.Place;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;
import com.synavos.maps.utils.TokenValidator;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class TextSearchService.
 *
 * @author Ibraheem Faiq
 * @since Apr 16, 2018
 */
@Service
@Slf4j
public class TextSearchService {

    @Autowired
    private TokenValidator tokenValidator;

    /**
     * Execute.
     *
     * @param request
     *            the request
     * @return the service response
     */
    public ServiceResponse execute(final TextSearchRequest request, final String token) {
	final Long startTime = System.nanoTime();
	final ServiceResponse serviceResponse = new ServiceResponse(HttpStatus.NO_CONTENT, "No data found");

	if (!tokenValidator.isValidToken(token)) {
	    serviceResponse.setHttpStatus(HttpStatus.UNAUTHORIZED);
	    serviceResponse.setMsg("UNAUTHORIZED");
	}
	else {
	    Collection<Place> results = new ArrayList<>(1);

	    log.info(StringUtils.concatValues("Fetching services for [", request.toString(), "]"));

	    if (CommonUtils.isNotNull(request) && !StringUtils.isNullOrEmptyStr(request.getText())) {
		final List<String> searchParams = prepareSearchParams(request.getText());
		results = PlacesCache.getInstance().searchPlacesByNameAndCity(searchParams, request.getCity(),
			request.getCount(), request.getLocation());
	    }

	    if (!CommonUtils.isNullOrEmptyCollection(results)) {
		serviceResponse.setHttpStatus(HttpStatus.OK);
		serviceResponse.setMsg("OK");
		serviceResponse.setPlaces(results);
	    }

	    log.debug(
		    log.isDebugEnabled()
			    ? StringUtils.concatValues("Text based services fetched count for [", request.toString(),
				    "] : [", CommonUtils.isNotNull(results) ? results.size() : 0, "] ")
			    : null);
	}

	log.info(
		log.isInfoEnabled()
			? StringUtils.concatValues("Time taken for text search service [",
				TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime), "ms]")
			: null);

	return serviceResponse;
    }

    /**
     * Prepare search params.
     *
     * @param text
     *            the text
     * @return the list
     */
    private List<String> prepareSearchParams(final String text) {
	List<String> tokensList = null;

	final String formattedStr = replaceCharacters(text);
	final String[] tokens = formattedStr.trim().split("\\s+");

	if (tokens.length > 0) {
	    tokensList = Arrays.asList(tokens);
	}

	return tokensList;
    }

    /**
     * Replace characters.
     *
     * @param text
     *            the text
     * @return the string
     */
    private String replaceCharacters(final String text) {
	final String SPACE = " ";
	String formattedStr = text.replace(",", SPACE);
	formattedStr = text.replace("(", SPACE);
	formattedStr = text.replace(")", SPACE);
	formattedStr = text.replace(";", SPACE);
	formattedStr = text.replace(".", SPACE);
	return formattedStr;
    }

}
