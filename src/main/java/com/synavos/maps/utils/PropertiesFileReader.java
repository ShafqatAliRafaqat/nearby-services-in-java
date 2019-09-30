package com.synavos.maps.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.synavos.maps.properties.GoogleMapProperties;

@Service
public class PropertiesFileReader extends Thread {

    private static final Logger LGR = LoggerFactory.getLogger(PropertiesFileReader.class);

    private static FileTime lastUpdatedTime = null;

    private static final String FILE_NAME = "server-config.properties";

    @Value("${server.config.path}")
    private String serverConfigFilePath;

    public void execute() throws IOException {
	_execute();
	this.start();
    }

    @Override
    public void run() {
	while (true) {
	    try {
		Thread.sleep(10000);
		_execute();
	    }
	    catch (final Exception ex) {
		LGR.error("##Exception## ocucrred while running PropertiesFileReader", ex);
	    }
	}
    }

    private void _execute() throws IOException {
	final Map<String, String> propertiesMap = readProperties();
	if (!CommonUtils.isNullOrEmptyMap(propertiesMap)) {
	    updateProperties(propertiesMap);
	}
    }

    private void updateProperties(final Map<String, String> propertiesMap) {
	LGR.info(propertiesMap.toString());

	GoogleMapProperties googleMapProperties = new GoogleMapProperties();
	synchronized (GoogleMapProperties.class) {
	    googleMapProperties.setApiKey(propertiesMap.get("google.maps.api.key"));

	    googleMapProperties.setNextPageWaitTime(Long.valueOf(propertiesMap.get("google.maps.next.page.wait.time")));

	    googleMapProperties.setSeedPointMinDistance(Long.valueOf(propertiesMap.get("maps.ref.point.distance")));

	    googleMapProperties.setSeedPointMaxDepth(Integer.valueOf(propertiesMap.get("maps.seed.point.max.depth")));

	    googleMapProperties.setMaxParallelTasks(Integer.valueOf(propertiesMap.get("max.parallel.tasks")));

	    googleMapProperties.setGoogleSupportedTypes(propertiesMap.get("google.maps.supported.place.types"));

	    googleMapProperties.setSupportedTypes(propertiesMap.get("supported.place.types"));

	    googleMapProperties.setCacheRefreshTime(propertiesMap.get("google.maps.cache.refresh.time"));

	    googleMapProperties.setAppUpPriority(Integer.valueOf(propertiesMap.get("google.maps.cache.up.priority")));

	    googleMapProperties.setMaxDistanceNearby(Long.valueOf(propertiesMap.get("maps.nearby.max.distance")));

	    googleMapProperties.setDbRefreshTimeMin(Long.valueOf(propertiesMap.get("db.refresh.time")));

	    googleMapProperties.setInitPageSize(Integer.valueOf(propertiesMap.get("init.page.size")));

	    googleMapProperties.setGoogleScan(Boolean.valueOf(propertiesMap.get("google.scan")));

	    googleMapProperties.setValidateToken(Boolean.valueOf(propertiesMap.get("validate.token")));

	    googleMapProperties.setValidationUrl(propertiesMap.get("validation.url"));
	}

    }

    private Map<String, String> readProperties() throws IOException {
	Map<String, String> properties = null;
	final Path filePath = Paths.get(serverConfigFilePath, File.separator, FILE_NAME);
	if (readFile(filePath)) {
	    final List<String> lines = Files.readAllLines(filePath);

	    if (!CommonUtils.isNullOrEmptyCollection(lines)) {
		properties = new HashMap<>(lines.size());

		for (final String line : lines) {
		    final String[] params = line.trim().split(Pattern.quote("="));

		    if (!CommonUtils.isNullOrEmptyArray(params) && params.length == 2) {
			properties.put(params[0].trim(), params[1].trim());
		    }
		}
	    }
	    else {
		LGR.error(StringUtils.concatValues(FILE_NAME, " was found with no data"));
		System.exit(-1);
	    }
	}

	return properties;
    }

    private boolean readFile(final Path filePath) throws IOException {
	boolean exists = Files.exists(filePath, LinkOption.NOFOLLOW_LINKS);
	if (exists) {
	    final FileTime lastModifiedTime = Files.getLastModifiedTime(filePath, LinkOption.NOFOLLOW_LINKS);
	    if (CommonUtils.isNull(lastUpdatedTime) || lastUpdatedTime.compareTo(lastModifiedTime) < 0) {
		lastUpdatedTime = lastModifiedTime;
	    }
	    else {
		return false;
	    }
	}
	else {
	    LGR.error(StringUtils.concatValues(FILE_NAME, " not be found."));
	    System.exit(-1);
	}

	return exists;
    }

    public String getFilePath() {
	String dirtyPath = getClass().getResource("").toString();
	String jarPath = dirtyPath.replaceAll("^.*file:/", ""); // removes file:/ and everything before it
	jarPath = jarPath.replaceAll("jar!.*", "jar"); // removes everything after .jar, if .jar exists in dirtyPath
	jarPath = jarPath.replaceAll("%20", " "); // necessary if path has spaces within
	if (!jarPath.endsWith(".jar")) { // this is needed if you plan to run the app using Spring Tools Suit play
					 // button.
	    jarPath = jarPath.replaceAll("/classes/.*", "/classes/");
	}

	String directoryPath = Paths.get(jarPath).getParent().toAbsolutePath().toString(); // Paths - from java 8

	LGR.info(LGR.isInfoEnabled() ? StringUtils.concatValues("Application starting from [", directoryPath, "]")
		: null);

	return directoryPath;
    }

}
