package com.synavos.maps.google.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.synavos.maps.beans.Location;

@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.Data
public class ViewPort {

    private Location northeast;

    private Location southwest;
}
