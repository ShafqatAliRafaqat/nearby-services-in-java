package com.synavos.maps.google.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@lombok.ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceDetails {

    @JsonProperty("address_components")
    private List<AddressComponent> addressComponents;

    @JsonProperty("formatted_address")
    private String formattedAddress;

    @JsonProperty("formatted_phone_number")
    private String formattedPhoneNumber;

    @JsonProperty("international_phone_number")
    private String internationalPhoneNumber;

    private String url;

    private String website;

    @JsonProperty("opening_hours")
    private OpeningHours openingHours;

    @JsonProperty("place_id")
    private String placeId;

    @JsonProperty("permanently_closed")
    private Boolean permanentlyClosed;
    
    
}
