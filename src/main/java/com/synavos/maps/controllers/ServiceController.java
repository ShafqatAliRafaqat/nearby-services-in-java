package com.synavos.maps.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.synavos.maps.beans.NearByServicesRequest;
import com.synavos.maps.beans.ServiceResponse;
import com.synavos.maps.beans.TextSearchRequest;
import com.synavos.maps.cache.PlacesCache;
import com.synavos.maps.cache.ReferencePointsCache;
import com.synavos.maps.models.PlaceModel;
import com.synavos.maps.services.NearByService;
import com.synavos.maps.services.PlacesService;
import com.synavos.maps.services.TextSearchService;

/**
 * @author Ibraheem Faiq
 * @since Apr 16, 2018
 *
 */
@RestController
public class ServiceController {

    @Autowired
    private NearByService nearByService;

    @Autowired
    private TextSearchService textSearchService;

    @Autowired
    private PlacesService placesService;

    private ResponseEntity<?> buildErrorResponse(final Errors errors) {
	final Map<String, Object> errResp = new HashMap<>();
	errResp.put("msg",
		errors.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.joining(",")));
	return ResponseEntity.badRequest().body(errResp);
    }

    @GetMapping("/refPoints")
    public ResponseEntity<?> getReferencePoints() {
	return ResponseEntity.ok(ReferencePointsCache.getInstance().getRefPoints());
    }

    @GetMapping("/locations")
    public ResponseEntity<?> getLocations() {
	return ResponseEntity.ok(PlacesCache.getInstance().getPlaces());
    }

    @PostMapping("/nearby")
    public ResponseEntity<?> getNearBy(@RequestBody @Valid final NearByServicesRequest nearByServicesRequest,
	    @RequestHeader("Authorization") String token, final Errors errors) {
	ResponseEntity<?> response = null;

	if (errors.hasErrors()) {
	    response = buildErrorResponse(errors);
	}
	else {
	    final ServiceResponse serviceResponse = nearByService.fetchNearbyServices(nearByServicesRequest, token);
	    response = ResponseEntity.status(serviceResponse.getHttpStatus()).body(serviceResponse);
	}

	return response;
    }

    @PostMapping("/search")
    public ResponseEntity<?> getNearBy(@RequestBody @Valid final TextSearchRequest textSearchRequest,
	    @RequestHeader("Authorization") String token, final Errors errors) {
	ResponseEntity<?> response = null;

	if (errors.hasErrors()) {
	    response = buildErrorResponse(errors);
	}
	else {
	    final ServiceResponse serviceResponse = textSearchService.execute(textSearchRequest, token);
	    response = ResponseEntity.status(serviceResponse.getHttpStatus()).body(serviceResponse);
	}

	return response;
    }

    @PostMapping("/addPlace")
    public ResponseEntity<?> addPlaceInCache(@RequestBody @Valid final PlaceModel placeModel, final Errors errors) {
	ResponseEntity<?> response = null;
	if (errors.hasErrors()) {
	    response = buildErrorResponse(errors);
	}
	else {
	    placesService.savePlace(placeModel);
	    response = ResponseEntity.ok().build();
	}

	return response;
    }

}
