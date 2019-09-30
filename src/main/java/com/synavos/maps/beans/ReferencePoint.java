package com.synavos.maps.beans;

import java.util.List;

@lombok.Data
@lombok.ToString
public final class ReferencePoint {

    private Location location;

    private Long distance;

    private List<String> types;

    private boolean dataFetched;

}
