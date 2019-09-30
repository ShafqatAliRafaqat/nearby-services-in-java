package com.synavos.maps.google;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.synavos.maps.beans.Location;
import com.synavos.maps.beans.ReferencePoint;
import com.synavos.maps.cache.ReferencePointsCache;
import com.synavos.maps.models.ReferencePointModel;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.GeoUtils;
import com.synavos.maps.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class ReferencePointsGenerator.
 *
 * @author Ibraheem Faiq
 * @since Mar 26, 2018
 */
@Service
@Slf4j
public class ReferencePointsGenerator {

    private static final List<Double> expansionAngles = new LinkedList<>();

    static {
	// North
	expansionAngles.add(0.0D);

	// East
	expansionAngles.add(90.0D);

	// South
	expansionAngles.add(180.0D);

	// West
	expansionAngles.add(270.0D);
    }

    public List<ReferencePoint> generateReferencePoints(final List<ReferencePointModel> referencePointModels) {
	final List<ReferencePoint> finalList = new LinkedList<>();

	log.debug(log.isDebugEnabled() ? "Generating reference points" : null);

	if (!CommonUtils.isNullOrEmptyCollection(referencePointModels)) {
	    referencePointModels.forEach(refPointsRequest -> {
		final List<ReferencePoint> refPoints = generateReferencePoints(refPointsRequest);

		if (!CommonUtils.isNullOrEmptyCollection(refPoints)) {

		    refPoints.forEach(refPoint -> {
			boolean savedRefPoint = ReferencePointsCache.getInstance().addRefPoint(refPoint.getLocation());
			if (savedRefPoint) {
			    finalList.add(refPoint);
			}
		    });
		}
	    });
	}

	log.debug(
		log.isDebugEnabled()
			? StringUtils.concatValues("Reference Point(s) Generated, Total Reference Points [",
				ReferencePointsCache.getInstance().getRefPoints().size(), "]")
			: null);

	return finalList;
    }

    private boolean addInFinalList(final List<ReferencePoint> finalList, final Location location, List<String> types) {

	final long count = finalList.stream()
		.filter(existingPoint -> isSameRefLocation(location, existingPoint.getLocation())).count();

	if (count == 0) {
	    final ReferencePoint referencePoint = new ReferencePoint();
	    referencePoint.setDistance(GoogleMapProperties.DIST_REF_POINT_METERS);
	    referencePoint.setLocation(location);
	    referencePoint.setTypes(types);

	    finalList.add(referencePoint);

	    return true;
	}

	return false;
    }

    private boolean isSameRefLocation(final Location loc1, Location loc2) {
	long minDistance = 10l;

	boolean same = false;

	if (CommonUtils.isNotNull(loc1, loc2)) {
	    same = loc1.equals(loc2);

	    if (!same) {
		final Double distance = GeoUtils.distance(loc1, loc2);

		if (Double.compare(distance, minDistance) < 0) {
		    same = true;
		}
	    }
	}

	return same;
    }

    private List<ReferencePoint> generateReferencePoints(final ReferencePointModel referencePointModel) {
	if (CommonUtils.isNotNull(referencePointModel, referencePointModel.getLocation())) {
	    final List<ReferencePoint> finalList = new LinkedList<>();

	    log.debug(log.isDebugEnabled() ? StringUtils.concatValues("Validating request [", referencePointModel, "]")
		    : null);

	    validateRequest(referencePointModel);

	    log.debug(log.isDebugEnabled()
		    ? StringUtils.concatValues("Generating reference points for [", referencePointModel, "]")
		    : null);

	    addInFinalList(finalList, referencePointModel.getLocation(), referencePointModel.getTypes());

	    final Map<Integer, List<Location>> locations = new HashMap<>(referencePointModel.getDepth());
	    locations.put(0, createList(referencePointModel.getLocation()));

	    int currentDepth = 0;

	    while (currentDepth < referencePointModel.getDepth()) {

		log.debug(log.isDebugEnabled()
			? StringUtils.concatValues("Generating reference point at depth [", currentDepth, "]")
			: null);

		final int newDepth = currentDepth + 1;

		final List<Location> locationsList = locations.get(currentDepth);

		if (!CommonUtils.isNullOrEmptyCollection(locationsList)) {
		    locationsList.forEach(loc -> {
			final List<Location> refLocList = createReferencePoints(loc);
			addInDepth(locations, newDepth, refLocList, referencePointModel.getTypes(), finalList);
		    });
		}

		++currentDepth;
	    }

	    return finalList;
	}

	return null;
    }

    private void validateRequest(final ReferencePointModel referencePointModel) {

	if (null == referencePointModel.getDepth() || referencePointModel.getDepth() < 0) {
	    referencePointModel.setDepth(0);
	}
	else if (referencePointModel.getDepth() > GoogleMapProperties.MAX_DEPTH_SEED_POINT) {
	    referencePointModel.setDepth(GoogleMapProperties.MAX_DEPTH_SEED_POINT);
	}

	int depth = referencePointModel.getDepth() * 2;
	referencePointModel.setDepth(depth > 20 ? 20 : depth);
    }

    private void addInDepth(final Map<Integer, List<Location>> locations, final int depth,
	    final List<Location> newLocations, final List<String> types, final List<ReferencePoint> finalList) {

	final List<Location> existingLocations = getExistingLocations(locations, depth);

	newLocations.forEach(newLocation -> {
	    if (addInFinalList(finalList, newLocation, types)) {
		existingLocations.add(newLocation);
	    }
	});
    }

    private List<Location> getExistingLocations(final Map<Integer, List<Location>> locations, final int depth) {
	List<Location> existingLocations = locations.get(depth);

	if (null == existingLocations) {
	    existingLocations = createList();
	    locations.put(depth, existingLocations);
	}

	return existingLocations;
    }

    private List<Location> createReferencePoints(final Location loc) {
	final List<Location> refLocList = new ArrayList<>(4);

	expansionAngles.forEach(angle -> {
	    final Location refLoc = GeoUtils.findReferencePoint(loc, GoogleMapProperties.DIST_REF_POINT_METERS, angle);
	    refLocList.add(refLoc);
	});

	return refLocList;
    }

    private List<Location> createList(final Location... locations) {
	List<Location> list = null;

	if (!CommonUtils.isNullOrEmptyArray(locations)) {
	    list = new ArrayList<>(locations.length);
	    for (final Location providedLocation : locations) {
		list.add(providedLocation);
	    }
	}
	else {
	    list = new LinkedList<>();
	}

	return list;
    }

}
