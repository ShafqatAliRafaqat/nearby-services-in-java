package com.synavos.maps.google.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.synavos.maps.beans.Location;

@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.Data
@lombok.EqualsAndHashCode
public class Geometry {

    private Location location;

}
