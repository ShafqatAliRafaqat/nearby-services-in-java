package com.synavos.maps.google.api.response;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class NearbyResponse.
 *
 * @author Ibraheem Faiq
 * @since Mar 22, 2018
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.Getter
@lombok.Setter
@lombok.ToString
public class NearbyResponse {

    @JsonProperty("next_page_token")
    private String nextPageToken;

    // OK
    private String status;

    private Collection<Place> results;

}
