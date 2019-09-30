package com.synavos.maps.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synavos.maps.beans.Location;
import com.synavos.maps.google.PlaceDetailsFetcher;
import com.synavos.maps.google.api.response.AddressComponent;
import com.synavos.maps.google.api.response.Place;
import com.synavos.maps.google.api.response.PlaceDetails;
import com.synavos.maps.models.PlaceModel;
import com.synavos.maps.models.PlaceModelRepository;
import com.synavos.maps.mongo.PlaceRepository;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.PlaceUtils;
import com.synavos.maps.utils.StringUtils;

/**
 * The Class PlacesCache.
 *
 * @author Ibraheem Faiq
 * @since Mar 28, 2018
 */
public final class PlacesCache {

    /** The Constant LGR. */
    private static final Logger LGR = LoggerFactory.getLogger(PlacesCache.class);

    private LuceneCache luceneCache = null;

    private Queue<String> unprocessedPlaceIdsQueue = null;

    private static PlacesCache instance;

    private static PlaceRepository placeRepository = null;

    private static PlaceModelRepository placeModelRepository = null;

    public static void init(final PlaceRepository placeRepository, final PlaceModelRepository placeModelRepository) {
	PlacesCache.placeRepository = placeRepository;
	PlacesCache.placeModelRepository = placeModelRepository;
    }

    /**
     * Instantiates a new places cache.
     */
    private PlacesCache() {
	super();
	unprocessedPlaceIdsQueue = new ConcurrentLinkedQueue<>();
	try {
	    luceneCache = new LuceneCache();
	}
	catch (IOException e) {
	    LGR.error("##IOException## occurred while building Lucene cache.", e);
	}
    }

    /**
     * Gets the single instance of PlacesCache.
     *
     * @return single instance of PlacesCache
     */
    public static PlacesCache getInstance() {
	createInstance();
	return instance;
    }

    /**
     * Creates the instance.
     */
    private static void createInstance() {
	if (CommonUtils.isNull(instance)) {
	    synchronized (PlacesCache.class) {
		if (CommonUtils.isNull(instance)) {
		    LGR.debug(LGR.isDebugEnabled() ? "Creating a new instance of Places Cache." : null);

		    instance = new PlacesCache();

		    LGR.debug(LGR.isDebugEnabled() ? "Adding places data from system into the new cache..." : null);

		    final List<PlaceModel> models = placeModelRepository.findByDeleted(false);
		    placeRepository.deleteByBuild(true);

		    if (!CommonUtils.isNullOrEmptyCollection(models)) {
			LGR.debug(LGR.isDebugEnabled()
				? StringUtils.concatValues("No. of places found from system [", models.size(), "]")
				: null);

			LGR.trace(LGR.isTraceEnabled() ? StringUtils.concatValues("Places found from system : ", models)
				: null);

			models.stream().filter(model -> !model.getDeleted()).forEach(model -> {
			    placeRepository.save(model.toPlace());
			});
		    }
		    else {
			LGR.warn("No places data found from system !");
		    }
		}
	    }
	}
    }

    /**
     * Builds the.
     */
    public void buildCache() {
	LGR.info(LGR.isInfoEnabled() ? "Building cache..." : null);

	final List<String> placeIds = fetchPlaceDetails();

	if (!CommonUtils.isNullOrEmptyCollection(placeIds)) {
	    LGR.debug(LGR.isDebugEnabled() ? "Building Lucene cache..." : null);

	    final List<Place> places = placeRepository.findByPlaceIdIn(placeIds);

	    places.parallelStream().forEach(place -> {
		if (CommonUtils.isNotNull(place)) {
		    buildLuceneData(place);
		}
	    });

	    LGR.debug(LGR.isDebugEnabled() ? "Lucene Cache built successfully" : null);
	}
    }

    private void buildLuceneData(final Place place) {
	populateCity(place);
	populateTypes(place);
	this.luceneCache.addPlace(place);
    }

    private List<String> fetchPlaceDetails() {
	LGR.debug(LGR.isDebugEnabled() ? "Fetching place(s) details..." : null);

	final List<String> placeIds = new ArrayList<>(unprocessedPlaceIdsQueue.size());
	while (!unprocessedPlaceIdsQueue.isEmpty()) {
	    placeIds.add(unprocessedPlaceIdsQueue.poll());
	}

	final List<PlaceDetails> placeDetails = new PlaceDetailsFetcher().fetchDetails(placeIds);

	if (!CommonUtils.isNullOrEmptyCollection(placeDetails)) {
	    updatePlaceDetails(placeDetails);
	}

	return placeIds;
    }

    private void updatePlaceDetails(final List<PlaceDetails> placeDetails) {
	LGR.debug(LGR.isDebugEnabled()
		? StringUtils.concatValues("Setting Place details for :", placeDetails.size(), " places")
		: null);

	placeDetails.forEach(placeDetail -> {
	    Optional<Place> optionalPlace = placeRepository.findByPlaceId(placeDetail.getPlaceId());

	    optionalPlace.ifPresent(place -> {
		place.setDetails(placeDetail);
		placeRepository.save(place);
	    });
	});
    }

    public boolean buildPlaceCache(final Place place) {
	boolean success = false;
	if (CommonUtils.isNotNull(place)) {
	    buildLuceneData(place);
	}
	return success;
    }

    /**
     * Adds the place.
     *
     * @param place
     *            the place
     * @return true, if successful
     */
    public boolean addPlace(final Place place) {
	boolean added = false;

	if (CommonUtils.isNotNull(place) && CommonUtils.isNotNull(place.getPlaceId())) {

	    LGR.trace(LGR.isTraceEnabled() ? StringUtils.concatValues("Trying to add place [", place, "] to cache")
		    : null);

	    synchronized (this) {
		final Optional<Place> optionalPlace = placeRepository.findByPlaceId(place.getPlaceId());
		if (!optionalPlace.isPresent()) {
		    LGR.debug(LGR.isDebugEnabled() ? StringUtils.concatValues("Adding place [", place, "] to cache")
			    : null);

		    // needed to override google id
		    place.setId(null);

		    placeRepository.save(place);

		    // add un-build place Id to queue
		    if (place.isBuild()) {
			luceneCache.addPlace(place);
		    }
		    else {
			unprocessedPlaceIdsQueue.add(place.getPlaceId());
		    }
		    added = true;
		}
		else {
		    Place existingPlace = optionalPlace.get();
		    LGR.debug(LGR.isDebugEnabled()
			    ? StringUtils.concatValues("Comparing [", place, "] to [", existingPlace, "]")
			    : null);
		    if (!PlaceUtils.isSame(existingPlace, place)) {
			LGR.trace(LGR.isTraceEnabled()
				? StringUtils.concatValues("Place [", place, "] is not same as [", existingPlace, "]")
				: null);

			deletePlace(existingPlace);
			added = addPlace(place);
		    }
		    else {
			LGR.trace(LGR.isTraceEnabled()
				? StringUtils.concatValues("Place [", place, "] is already in cache, due to [",
					existingPlace, "]")
				: null);
		    }
		}
	    }
	}

	return added;
    }

    public boolean updatePlace(final Place place) {
	boolean update = false;

	if (CommonUtils.isNotNull(place)) {
	    deletePlace(place);
	    addPlace(place);
	    update = true;
	}

	return update;
    }

    public boolean deletePlace(final Place place) {
	boolean delete = false;

	if (CommonUtils.isNotNull(place)) {
	    luceneCache.deletePlace(place);
	    placeRepository.deleteByPlaceId(place.getPlaceId());
	    delete = true;
	}

	return delete;
    }

    private String getAddressValue(final Place place, final String type) {
	String value = null;

	if (!StringUtils.isNullOrEmptyStr(type)) {
	    final List<AddressComponent> addressComponents = place.getDetails().getAddressComponents();

	    for (final AddressComponent addressComponent : addressComponents) {
		if (CommonUtils.isNotNull(addressComponent)
			&& !CommonUtils.isNullOrEmptyCollection(addressComponent.getTypes())
			&& addressComponent.getTypes().contains(type)) {

		    value = addressComponent.getShortName();
		    break;
		}
	    }
	}

	return value;
    }

    private void populateCity(final Place place) {
	if (CommonUtils.isNotNull(place.getDetails())
		&& !CommonUtils.isNullOrEmptyCollection(place.getDetails().getAddressComponents())) {
	    final String city = getAddressValue(place, "administrative_area_level_2");
	    if (!StringUtils.isNullOrEmptyStr(city)) {
		final String fomattedCity = city.toLowerCase().trim();
		place.setCity(fomattedCity);
	    }

	    place.getDetails().setAddressComponents(null);
	}
    }

    private void populateTypes(final Place place) {
	final List<String> placeTypes = place.getTypes();

	for (final String placeType : GoogleMapProperties.SUPPORTED_PLACE_TYPES) {
	    if (!placeTypes.contains(placeType) && place.getName().toLowerCase().contains(placeType)) {
		placeTypes.add(placeType);
	    }
	}
    }

    /**
     * Gets the places.
     *
     * @return the places
     */
    public Collection<Place> getPlaces() {
	return placeRepository.findAll();
    }

    public List<Place> searchPlacesByNameAndCity(final List<String> nameTokens, final String city, final int count,
	    final Location location) {
	final List<String> placeIds = this.luceneCache.searchPlacesIdByNameAndCity(nameTokens, city, count, location);

	if (CommonUtils.isNullOrEmptyCollection(placeIds)) {
	    return null;
	}

	final List<Place> places = placeRepository.findByPlaceIdIn(placeIds);

	return places;
    }

    public List<Place> searchNearbyPlaces(final Location location, final Integer radius, final List<String> types,
	    final int count) {
	List<Place> places = Collections.emptyList();

	if (CommonUtils.isNotNull(location, radius) && radius >= 0) {
	    final List<String> placeIds = this.luceneCache.findPlaceIdsInRadius(location.getLatitude(),
		    location.getLongitude(), toMeters(radius), types, count);

	    if (!CommonUtils.isNullOrEmptyCollection(placeIds)) {
		places = placeRepository.findByPlaceIdIn(placeIds);
	    }
	}

	return places;
    }

    private int toMeters(final Integer radius) {
	return radius > 0 ? radius * 1000 : 999;
    }

}
