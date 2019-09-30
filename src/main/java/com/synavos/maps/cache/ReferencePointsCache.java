package com.synavos.maps.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import com.synavos.maps.beans.Location;
import com.synavos.maps.kdtree.KdTree;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.GeoUtils;
import com.synavos.maps.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class ReferencePointsCache.
 *
 * @author Ibraheem Faiq
 * @since Mar 28, 2018
 */
@Slf4j
public class ReferencePointsCache {

    /** The ref points locations. */
    private KdTree<Location> kdTree;

    /**
     * Instantiates a new reference points cache.
     */
    private ReferencePointsCache() {
	super();
	kdTree = new KdTree<>();
    }

    /**
     * The Class SingletonHolder.
     */
    private static class SingletonHolder {
	private static final ReferencePointsCache INSTANCE = new ReferencePointsCache();
    }

    /**
     * Gets the single instance of ReferencePointsCache.
     *
     * @return single instance of ReferencePointsCache
     */
    public static ReferencePointsCache getInstance() {
	return SingletonHolder.INSTANCE;
    }

    public boolean addRefPoint(final Location referenceLocation) {
	boolean added = false;
	// check if provided data is valid
	if (CommonUtils.isNotNull(referenceLocation)) {
	    synchronized (this) {
		final Location nearestLocation = getNearestLocation(referenceLocation);

		if (CommonUtils.isNotNull(nearestLocation) && inDefinedDistance(nearestLocation, referenceLocation)) {
		    log.debug(
			    log.isDebugEnabled()
				    ? StringUtils.concatValues("Reference Point [", referenceLocation,
					    "] already in cache due to [", nearestLocation, "], skipping insertion")
				    : null);
		}
		else {
		    log.debug(log.isDebugEnabled()
			    ? StringUtils.concatValues("Adding Reference Point [", referenceLocation, "] to Cache...")
			    : null);

		    kdTree.add(referenceLocation);

		    added = true;
		}
	    }
	}

	return added;
    }

    private boolean inDefinedDistance(final Location loc1, final Location loc2) {
	boolean inDistance = false;
	final Long distance = GeoUtils.distance(loc1, loc2).longValue();

	if (Long.compare(distance, GoogleMapProperties.DIST_REF_POINT_METERS) <= 0) {
	    inDistance = true;
	}

	return inDistance;
    }

    private Location getNearestLocation(final Location searchPoint) {
	final Collection<Location> nearestPoints = kdTree.nearestNeighbourSearch(1, searchPoint);

	Location nearestPoint = null;
	if (!CommonUtils.isNullOrEmptyCollection(nearestPoints)) {
	    nearestPoint = nearestPoints.stream().findFirst().get();
	}

	return nearestPoint;
    }

    /**
     * Gets the ref points.
     *
     * @return the ref points
     */
    public Collection<Location> getRefPoints() {
	final Collection<Location> refPoints = new LinkedList<>();

	final Iterator<Location> iterator = kdTree.iterator();
	while (iterator.hasNext()) {
	    refPoints.add(iterator.next());
	}

	return Collections.unmodifiableCollection(refPoints);
    }

    public boolean isAreaScanned(final Location location) {
	boolean areaScanned = false;

	if (CommonUtils.isNotNull(location)) {
	    Location nearestLocation = getNearestLocation(location);
	    areaScanned = GeoUtils.distance(nearestLocation, location) <= GoogleMapProperties.DIST_REF_POINT_METERS;
	}

	return areaScanned;
    }

    /**
     * Clear.
     */
    public void clear() {
	synchronized (this) {
	    this.kdTree = new KdTree<>();
	}
    }

}
