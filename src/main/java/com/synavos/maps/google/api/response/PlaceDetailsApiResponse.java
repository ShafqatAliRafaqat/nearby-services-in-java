package com.synavos.maps.google.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@lombok.ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceDetailsApiResponse {

    private PlaceDetails result;

    private String status;
}
