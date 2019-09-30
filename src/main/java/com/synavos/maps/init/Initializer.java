package com.synavos.maps.init;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.synavos.maps.cache.PlacesCache;
import com.synavos.maps.constants.CommonConstants;
import com.synavos.maps.google.api.response.Place;
import com.synavos.maps.models.PlaceModelRepository;
import com.synavos.maps.mongo.PlaceRepository;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.services.PlacesService;
import com.synavos.maps.utils.PropertiesFileReader;
import com.synavos.maps.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class Initializer.
 *
 * @author Ibraheem Faiq
 * @since Apr 4, 2018
 */
@Component
@Slf4j
public class Initializer implements ApplicationListener<ContextRefreshedEvent> {

    /** The places service. */
    @Autowired
    private PlacesService placesService;

    @Autowired
    private PropertiesFileReader fileReader;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private PlaceModelRepository placeModelRepository;

    @Value("${data.store.path}")
    private String DATA_STORE_PATH;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent startupEvent) {
	try {
	    init();
	}
	catch (final Exception e) {
	    log.error("##IOException## occurred on system startup", e);
	    System.exit(-1);
	}
    }

    private void init() throws IOException {
	log.info(log.isInfoEnabled() ? "Initializing startup data..." : null);

	fileReader.execute();

	createStoreFolders();

	initPlacesCache();

	placesService.execute();

	log.info(log.isInfoEnabled() ? "Startup data initialized." : null);
    }

    private void initPlacesCache() {
	PlacesCache.init(placeRepository, placeModelRepository);

	log.info(log.isInfoEnabled() ? "Creating Places Cache" : null);

	final PlacesCache placesCache = PlacesCache.getInstance();

	log.info(log.isInfoEnabled() ? "Building Lucene Index..." : null);

	final AtomicLong count = new AtomicLong(0l);
	long totalCount = placeRepository.countByTypesIn(GoogleMapProperties.SUPPORTED_PLACE_TYPES);

	final int pages = (int) Math.ceil((double) totalCount / (double) GoogleMapProperties.INIT_PAGE_SIZE);

	log.debug(log.isDebugEnabled()
		? StringUtils.concatValues("Total Records to Fetch [", totalCount, "], Total Pages [", pages, "]")
		: null);

	for (int page = 0; page < pages; page++) {
	    int displayPage = (page + 1);
	    log.debug(log.isDebugEnabled()
		    ? StringUtils.concatValues("Fetching Page [", displayPage, "], Total Pages [", pages, "]")
		    : null);

	    final Page<Place> places = placeRepository
		    .findAll(PageRequest.of(page, GoogleMapProperties.INIT_PAGE_SIZE));

	    log.debug(log.isDebugEnabled()
		    ? StringUtils.concatValues("Building lucene cache for Page [", displayPage, "]")
		    : null);

	    final List<Callable<Object>> threads = places.stream().map(place -> {
		return new Callable<Object>() {
		    @Override
		    public Object call() throws Exception {
			placesCache.buildPlaceCache(place);
			count.incrementAndGet();
			return Boolean.TRUE;
		    }
		};
	    }).collect(Collectors.toList());

	    final ExecutorService executorService = Executors.newFixedThreadPool(threads.size());
	    try {
		executorService.invokeAll(threads);
	    }
	    catch (final InterruptedException e) {
		log.error("##InterruptedException## occurred while building cache", e);
	    }

	    log.debug(
		    log.isDebugEnabled() ? StringUtils.concatValues("Built Lucene cache for Page [", displayPage, "] ")
			    : null);
	}

	log.info(log.isInfoEnabled()
		? StringUtils.concatValues("Lucene Index built successfully, places added [", count.get(), "]")
		: null);
    }

    private void createStoreFolders() {
	log.info(log.isInfoEnabled() ? StringUtils.concatValues("Creating store folders at [", DATA_STORE_PATH, "]")
		: null);

	final Path placesDir = Paths.get(DATA_STORE_PATH, File.separator, CommonConstants.PLACES_FOLDER);
	createDir(placesDir);
	GoogleMapProperties.setPlacesDir(placesDir);
    }

    private void createDir(final Path path) {
	final File dir = new File(path.toString());
	if (!dir.exists()) {
	    try {
		Files.createDirectory(path);
	    }
	    catch (IOException e) {
		log.error("Cannot create store folders");
		System.exit(-1);
	    }
	}
    }

}
