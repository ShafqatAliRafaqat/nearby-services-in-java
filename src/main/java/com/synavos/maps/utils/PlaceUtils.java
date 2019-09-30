package com.synavos.maps.utils;

import com.synavos.maps.google.api.response.Place;

public class PlaceUtils {

    private PlaceUtils() {
	super();
    }

    public static boolean isSame(final Place p1, final Place p2) {
	boolean isSame = false;

	if (CommonUtils.isNotNull(p1, p2)) {
	    isSame = isEqual(p1.getGeometry().getLocation(), p2.getGeometry().getLocation())
		    && isEqual(p1.getName(), p2.getName()) 
		    && isEqual(p1.getPlaceId(), p2.getPlaceId())
		    && isEqual(p1.getTiming(), p2.getTiming())
		    && isEqual(p1.getTypes(), p2.getTypes())
		    && isEqual(p1.getVicinity(), p2.getVicinity()) 
		    && isEqual(p1.getDetails(), p2.getDetails());
	}

	return isSame;
    }

    private static boolean isEqual(Object o1, Object o2) {
	return (CommonUtils.isNull(o1) && CommonUtils.isNull(o2)) || (o1.equals(o2));
    }

}
