/*
 * 
 */
package com.synavos.maps.services;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synavos.maps.beans.ReferencePoint;
import com.synavos.maps.cache.PlacesCache;
import com.synavos.maps.cache.ReferencePointsCache;
import com.synavos.maps.google.ReferencePointModelService;
import com.synavos.maps.google.ReferencePointsDataFetcher;
import com.synavos.maps.google.ReferencePointsGenerator;
import com.synavos.maps.google.api.response.Place;
import com.synavos.maps.models.PlaceModel;
import com.synavos.maps.models.PlaceModelRepository;
import com.synavos.maps.models.ReferencePointModel;
import com.synavos.maps.mongo.PlaceRepository;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class PlacesService.
 *
 * @author Ibraheem Faiq
 * @since Apr 6, 2018
 */
@Service

/** The Constant log. */
@Slf4j
public class PlacesService {

    /** The Constant EXECUTOR_SERVICE. */
    private static final ExecutorService EXECUTOR_SERVICE;

    static {
	EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    }

    /** The reference point service. */
    private final ReferencePointModelService referencePointService;

    /** The reference points generator. */
    private final ReferencePointsGenerator referencePointsGenerator;

    /** The reference points data fetcher. */
    private final ReferencePointsDataFetcher referencePointsDataFetcher;

    /** The reference point model service. */
    private final ReferencePointModelService referencePointModelService;

    /** The place model repository. */
    private final PlaceModelRepository placeModelRepository;

    /** The cache config service. */
    private final CacheConfigService cacheConfigService;

    /** The place repository. */
    private final PlaceRepository placeRepository;

    /** The area requests. */
    private final ConcurrentLinkedQueue<Runnable> areaRequests = new ConcurrentLinkedQueue<>();

    /** The Constant LOCK. */
    private static final AtomicBoolean LOCK = new AtomicBoolean(false);

    private java.sql.Timestamp lastRefreshTime;

    @Autowired
    public PlacesService(final ReferencePointModelService referencePointService,
	    final ReferencePointsGenerator referencePointsGenerator,
	    final ReferencePointsDataFetcher referencePointsDataFetcher,
	    final ReferencePointModelService referencePointModelService,
	    final PlaceModelRepository placeModelRepository, final CacheConfigService cacheConfigService,
	    final PlaceRepository placeRepository) {

	super();

	this.referencePointService = referencePointService;
	this.referencePointsGenerator = referencePointsGenerator;
	this.referencePointsDataFetcher = referencePointsDataFetcher;
	this.referencePointModelService = referencePointModelService;
	this.placeModelRepository = placeModelRepository;
	this.cacheConfigService = cacheConfigService;
	this.placeRepository = placeRepository;

	lastRefreshTime = placeModelRepository.getCurrentTime();
    }

    /**
     * Submit area scan request.
     *
     * @param referencePointModel
     *            the reference point model
     */
    public void submitAreaScanRequest(final ReferencePointModel referencePointModel) {
	if (CommonUtils.isNotNull(referencePointModel) && GoogleMapProperties.GOOGLE_SCAN) {
	    final Runnable thread = () -> {
		final List<ReferencePoint> refPoints = referencePointsGenerator
			.generateReferencePoints(Arrays.asList(referencePointModel));

		if (!CommonUtils.isNullOrEmptyCollection(refPoints)) {

		    log.debug(log.isDebugEnabled()
			    ? StringUtils.concatValues("Scanning new area for [", referencePointModel, "]")
			    : null);

		    scanRegion(referencePointModel, refPoints);
		}
		else if (log.isTraceEnabled()) {
		    log.trace("No new region found to be scanned!");
		}

		releaseLock();
	    };

	    areaRequests.add(thread);
	}
    }

    /**
     * Scan region.
     *
     * @param referencePointModel
     *            the reference point model
     * @param refPoints
     *            the ref points
     */
    private void scanRegion(final ReferencePointModel referencePointModel, final List<ReferencePoint> refPoints) {
	log.debug(
		log.isDebugEnabled()
			? StringUtils.concatValues("Saving reference point [", referencePointModel,
				"] in DB and fetching data from Places API...")
			: null);

	referencePointModelService.saveByPriority(referencePointModel);

	fetchPlaces(refPoints);
    }

    /**
     * Execute.
     */
    public void execute() {
	final Set<Integer> definedPriorities = referencePointService.fetchDistinctDefinedPriorities();

	if (!CommonUtils.isNullOrEmptyCollection(definedPriorities)) {

	    log.info(
		    log.isInfoEnabled()
			    ? StringUtils.concatValues("Caching data till priority [",
				    GoogleMapProperties.APPLICATION_UP_CACHE_PRIORITY, "] before starting system.")
			    : null);

	    // priorities are in ascending order, break the loop as soon as a priority
	    // greater than cache up priority is found
	    for (final Integer priority : definedPriorities) {
		if (GoogleMapProperties.APPLICATION_UP_CACHE_PRIORITY >= priority) {

		    // populate places data for this priority
		    populatePlacesData(priority);
		}
		else {
		    break;
		}
	    }

	    if (CommonUtils.isNullOrEmptyCollection(PlacesCache.getInstance().getPlaces())) {
		exitSystem(StringUtils.concatValues("No Places data found for starting system at defined priority [",
			GoogleMapProperties.APPLICATION_UP_CACHE_PRIORITY, "]"));
	    }
	    else {
		log.info(log.isInfoEnabled()
			? StringUtils.concatValues("Cache has been built for defined priority, total places fetched [",
				PlacesCache.getInstance().getPlaces().size(),
				"] rest of cache will be build in parallel")
			: null);

		startCacheManager();
		startRequestProcessor();
		startDbRefresher();
	    }
	}
	else {
	    exitSystem("No priorities data found, application is exiting.");
	}
    }

    private void startDbRefresher() {
	new Thread(() -> dbRefresher()).start();
    }

    private void dbRefresher() {
	while (true) {
	    try {
		Thread.sleep(TimeUnit.MINUTES.toMillis(GoogleMapProperties.DB_REFRESH_TIME_MIN));

		log.info(log.isInfoEnabled()
			? StringUtils.concatValues("Refreshing places data from DB, last refresh time [",
				lastRefreshTime, "].")
			: null);

		final List<PlaceModel> placeModels = placeModelRepository.findByDeleted(false);
		lastRefreshTime = placeModelRepository.getCurrentTime();

		if (!CommonUtils.isNullOrEmptyCollection(placeModels)) {
		    final PlacesCache cache = PlacesCache.getInstance();

		    placeRepository.deleteByBuild(true);

		    placeModels.forEach(placeModel -> {
			final Place place = placeModel.toPlace();

			cache.deletePlace(place);
			cache.addPlace(place);
		    });
		}

		log.info(log.isInfoEnabled() ? "Places data refreshed from DB." : null);
	    }
	    catch (final Exception ex) {
		log.error("##Exception## occurred in DB refreshed thread", ex);
	    }
	}
    }

    // private void updateCache(final List<PlaceModel> placeModels, final
    // PlacesCache cache) {
    // log.info(log.isInfoEnabled() ? StringUtils.concatValues("Updated places found
    // : ", placeModels) : null);
    // for (final PlaceModel placeModel : placeModels) {
    // if (placeModel.getDeleted()) {
    // cache.deletePlace(placeModel.toPlace());
    // }
    // else {
    // cache.updatePlace(placeModel.toPlace());
    // }
    // }
    // }

    /**
     * Start request processor.
     */
    private void startRequestProcessor() {
	final Runnable requestProcessor = () -> {
	    processAreaScanRequests();
	};
	new Thread(requestProcessor).start();
    }

    /**
     * Start cache manager.
     */
    private void startCacheManager() {
	final Runnable cacheManager = () -> {
	    startThread();
	};
	new Thread(cacheManager).start();
    }

    /**
     * Acquire lock.
     *
     * @param waitTimeMs
     *            the wait time ms
     */
    private void acquireLock(long waitTimeMs) {
	while (true) {
	    try {
		if (!LOCK.get()) {
		    LOCK.set(true);
		    break;
		}
		else {
		    Thread.sleep(waitTimeMs);
		}
	    }
	    catch (final Exception ex) {
		log.error("Error occurred while acquiring lock", ex);
	    }
	}
    }

    /**
     * Release lock.
     */
    private void releaseLock() {
	LOCK.set(false);
    }

    /**
     * Process area scan requests.
     */
    private void processAreaScanRequests() {
	log.info(log.isInfoEnabled() ? "Starting area scanner..." : null);

	while (true) {
	    try {
		acquireLock(10000);

		final Runnable task = areaRequests.poll();

		if (CommonUtils.isNotNull(task)) {
		    log.trace(log.isTraceEnabled() ? "Processing area scan request..." : null);
		    EXECUTOR_SERVICE.execute(task);
		    log.trace(log.isTraceEnabled() ? "Area scan request processed" : null);
		}
		else {
		    releaseLock();
		}

		Thread.sleep(250);
	    }
	    catch (final Exception ex) {
		log.error("Exception occurred while processing area scan request");
	    }
	}
    }

    /**
     * Start thread.
     */
    private void startThread() {
	// sleep before starting thread, did it for better log viewing
	startSleep(5000);

	log.info(log.isInfoEnabled() ? "Starting cache manager..." : null);

	// when running first time, we need build remaining priorities data
	boolean firstRun = true;

	while (true) {
	    try {
		// acquire lock before processing
		acquireLock(1000);

		Date cacheStartDate = null;

		// clear reference points cache for a cache run
		// we need to build reference points in every cache refresh call
		if (!firstRun) {
		    ReferencePointsCache.getInstance().clear();
		    cacheStartDate = new Date();
		}

		final Set<Integer> definedPriorities = referencePointService.fetchDistinctDefinedPriorities();

		final Long startTime = System.nanoTime();

		for (final Integer priority : definedPriorities) {
		    if (!firstRun || (firstRun && GoogleMapProperties.APPLICATION_UP_CACHE_PRIORITY < priority)) {
			// populate places data for this priority
			populatePlacesData(priority);
			Thread.sleep(1000);
		    }
		}

		log.info(
			log.isInfoEnabled() && !firstRun
				? StringUtils.concatValues("Time taken to refresh cache ",
					TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime), "s")
				: null);

		cacheConfigService.updateLastCacheDate();

		firstRun = false;

		log.info(
			log.isInfoEnabled()
				? StringUtils.concatValues("Total places fetched : [",
					PlacesCache.getInstance().getPlaces().size(), "]")
				: null);

		// delete all old places
		if (null != cacheStartDate) {
		    placeRepository.deleteByCachedAtLessThanAndBuild(cacheStartDate, false);
		}

		releaseLock();
		sleep();
	    }
	    catch (final Exception ex) {
		log.error("Exception occurred while refreshing places data", ex);
	    }
	}
    }

    /**
     * Start sleep.
     *
     * @param ms
     *            the ms
     */
    private void startSleep(long ms) {
	try {
	    Thread.sleep(ms);
	}
	catch (final Exception ex) {
	    // do nothing if interrupted
	}
    }

    /**
     * Sleep.
     *
     * @throws InterruptedException
     *             the interrupted exception
     */
    private void sleep() throws InterruptedException {
	final long currentTimeMillis = System.currentTimeMillis();
	final long lastCachedAt = cacheConfigService.getLastCachedAt().getTime();

	final long cacheTime = lastCachedAt + GoogleMapProperties.PLACES_CACHE_REFRESH_TIME_MS;

	long sleepTime = cacheTime - currentTimeMillis;
	if (sleepTime < 0) {
	    sleepTime = 100;
	}

	log.info(
		log.isInfoEnabled()
			? StringUtils.concatValues("Cache Manager going to sleep, will re-execute at [",
				(new Date(currentTimeMillis + sleepTime)), "]")
			: null);

	Thread.sleep(sleepTime);
    }

    /**
     * Populate places data.
     *
     * @param priority
     *            the priority
     */
    private void populatePlacesData(final Integer priority) {
	log.info(log.isInfoEnabled() ? StringUtils.concatValues("Building cache for priority [", priority, "]") : null);

	final List<ReferencePointModel> referencePointModels = referencePointService.fetchRequests(priority);
	final List<ReferencePoint> refPoints = referencePointsGenerator.generateReferencePoints(referencePointModels);

	if (cacheConfigService.isCacheAllowed()) {
	    fetchPlaces(refPoints);
	}
	else {
	    log.warn("Cache not allowed");
	}
    }

    private void fetchPlaces(final List<ReferencePoint> refPoints) {
	referencePointsDataFetcher.fetchReferencePointsData(refPoints);
	PlacesCache.getInstance().buildCache();
    }

    /**
     * Exit system.
     *
     * @param _log
     *            the log
     */
    private void exitSystem(String _log) {
	log.error(_log);
	System.exit(-1);
    }

    /**
     * Save place.
     *
     * @param placeModel
     *            the place model
     */
    public void savePlace(final PlaceModel placeModel) {
	if (CommonUtils.isNotNull(placeModel)) {
	    PlacesCache.getInstance().addPlace(placeModel.toPlace());
	}
    }
}
