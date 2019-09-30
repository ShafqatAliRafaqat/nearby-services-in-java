package com.synavos.maps.properties;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.synavos.maps.constants.BusinessConstants;
import com.synavos.maps.constants.CommonConstants;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class GoogleMapProperties.
 *
 * @author Ibraheem Faiq
 * @since Mar 28, 2018
 */
@Slf4j
public class GoogleMapProperties {

    /** The api key. */
    public static String API_KEY;

    /** The next page wait time. */
    public static Long NEXT_PAGE_WAIT_TIME;

    /** The dist ref point. */
    public static Long DIST_REF_POINT_METERS;

    /** The max depth seed point. */
    public static Integer MAX_DEPTH_SEED_POINT;

    /** The max parallel tasks. */
    public static Integer MAX_PARALLEL_TASKS;

    /** The supported place types. */
    public static List<String> GOOGLE_SUPPORTED_PLACE_TYPES = new LinkedList<>();

    public static List<String> SUPPORTED_PLACE_TYPES = new LinkedList<>();

    /** The places cache refresh time ms. */
    public static Long PLACES_CACHE_REFRESH_TIME_MS;

    /** The application up cache priority. */
    public static Integer APPLICATION_UP_CACHE_PRIORITY;

    /** The max distance nearby km. */
    public static Long MAX_DISTANCE_NEARBY_KM;

    /** The server socket port. */
    public static Integer SERVER_SOCKET_PORT;

    /** The db refresh time min. */
    public static Long DB_REFRESH_TIME_MIN;

    /** The places dir. */
    public static Path PLACES_DIR;

    public static int INIT_PAGE_SIZE = 200;

    public static boolean GOOGLE_SCAN = false;

    public static boolean VALIDATE_TOKEN = false;

    public static String VALIDATION_URL = null;

    public void setApiKey(final String apiKey) {
	API_KEY = apiKey;
    }

    public void setNextPageWaitTime(final Long nextPageWaitTime) {
	NEXT_PAGE_WAIT_TIME = nextPageWaitTime;
    }

    public void setSeedPointMinDistance(final Long distance) {
	DIST_REF_POINT_METERS = distance;
    }

    public void setSeedPointMaxDepth(final Integer maxDepth) {
	if (maxDepth < 1) {
	    MAX_DEPTH_SEED_POINT = 1;
	}
	else {
	    MAX_DEPTH_SEED_POINT = maxDepth;
	}
    }

    public void setMaxParallelTasks(final Integer maxParallelTasks) {
	if (maxParallelTasks < 1) {
	    MAX_PARALLEL_TASKS = 1;
	}
	else {
	    MAX_PARALLEL_TASKS = maxParallelTasks;
	}
    }

    public void setGoogleSupportedTypes(String placeTypes) {
	final List<String> supportedTypes = CommonUtils.getListFromString(placeTypes, CommonConstants.COMMA);
	if (!CommonUtils.isNullOrEmptyCollection(supportedTypes)) {
	    GOOGLE_SUPPORTED_PLACE_TYPES.clear();
	    supportedTypes.forEach(type -> {
		if (BusinessConstants.GOOGLE_SUPPORTED_PLACES_TYPES.contains(type)) {
		    GOOGLE_SUPPORTED_PLACE_TYPES.add(type);
		}
	    });
	}

	if (CommonUtils.isNullOrEmptyCollection(GOOGLE_SUPPORTED_PLACE_TYPES)) {
	    log.error("No valid place types defined in system, exiting with error code -1");
	    System.exit(-1);
	}
	else {
	    log.warn("Application starting for following place types : " + GOOGLE_SUPPORTED_PLACE_TYPES.toString());
	}
    }

    public void setSupportedTypes(String placeTypes) {
	final List<String> supportedTypes = CommonUtils.getListFromString(placeTypes, CommonConstants.COMMA);
	if (!CommonUtils.isNullOrEmptyCollection(supportedTypes)) {
	    SUPPORTED_PLACE_TYPES.clear();
	    SUPPORTED_PLACE_TYPES.addAll(supportedTypes);
	}

	if (CommonUtils.isNullOrEmptyCollection(SUPPORTED_PLACE_TYPES)) {
	    log.error("No valid supported types defined in system, exiting with error code -1");
	    System.exit(-1);
	}
	else {
	    log.warn("Application starting for following types : " + SUPPORTED_PLACE_TYPES.toString());
	}
    }

    public void setCacheRefreshTime(final String cacheRefreshTime) {
	boolean validTime = false;

	if (cacheRefreshTime.length() > 1) {
	    final StringBuilder refreshTime = new StringBuilder(cacheRefreshTime);
	    final char unit = Character.toUpperCase(refreshTime.charAt(refreshTime.length() - 1));
	    final String timeStr = refreshTime.substring(0, refreshTime.length() - 1);
	    final Integer time = StringUtils.toInteger(timeStr);

	    if (null != time) {
		validTime = true;
		switch (unit) {
		    case CommonConstants.UNIT_SECOND:
			PLACES_CACHE_REFRESH_TIME_MS = TimeUnit.SECONDS.toMillis(time);
			break;
		    case CommonConstants.UNIT_MINUTE:
			PLACES_CACHE_REFRESH_TIME_MS = TimeUnit.MINUTES.toMillis(time);
			break;
		    case CommonConstants.UNIT_HOUR:
			PLACES_CACHE_REFRESH_TIME_MS = TimeUnit.HOURS.toMillis(time);
			break;
		    case CommonConstants.UNIT_DAY:
			PLACES_CACHE_REFRESH_TIME_MS = TimeUnit.DAYS.toMillis(time);
			break;
		    default:
			validTime = false;
			break;
		}
	    }
	}

	if (!validTime) {
	    log.error("Invalid cache refresh time specified, exiting system");
	    System.exit(-1);
	}
    }

    public void setAppUpPriority(final Integer priority) {
	APPLICATION_UP_CACHE_PRIORITY = priority;
    }

    public void setMaxDistanceNearby(final Long distance) {
	MAX_DISTANCE_NEARBY_KM = distance;
    }

    public void setPort(final Integer port) {
	SERVER_SOCKET_PORT = port;
    }

    public void setDbRefreshTimeMin(final Long dbRefreshTimeMin) {
	DB_REFRESH_TIME_MIN = dbRefreshTimeMin;
    }

    public static void setPlacesDir(final Path PLACES_DIR) {
	GoogleMapProperties.PLACES_DIR = PLACES_DIR;
    }

    public void setInitPageSize(final Integer PAGE_SIZE) {
	if (null != PAGE_SIZE && PAGE_SIZE > 0) {
	    GoogleMapProperties.INIT_PAGE_SIZE = PAGE_SIZE;
	}
    }

    public void setGoogleScan(final Boolean valueOf) {
	GoogleMapProperties.GOOGLE_SCAN = valueOf;
    }

    public void setValidateToken(final boolean validateToken) {
	GoogleMapProperties.VALIDATE_TOKEN = validateToken;
    }

    public void setValidationUrl(final String validationUrl) {
	GoogleMapProperties.VALIDATION_URL = validationUrl;
    }
}
