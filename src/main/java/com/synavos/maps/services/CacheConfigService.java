package com.synavos.maps.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synavos.maps.config.CacheConfig;
import com.synavos.maps.mongo.CacheConfigRepository;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CacheConfigService {

    private CacheConfigRepository cacheConfigRepository;

    @Autowired
    public CacheConfigService(final CacheConfigRepository cacheConfigRepository) {
	this.cacheConfigRepository = cacheConfigRepository;
    }

    private CacheConfig getCacheConfig() {
	CacheConfig cacheConfig = null;
	final List<CacheConfig> cacheConfigs = cacheConfigRepository.findAll();

	if (!CommonUtils.isNullOrEmptyCollection(cacheConfigs)) {
	    cacheConfig = cacheConfigs.get(0);
	}
	else {
	    cacheConfig = new CacheConfig();
	    cacheConfig = cacheConfigRepository.save(cacheConfig);
	}

	return cacheConfig;
    }

    public void updateLastCacheDate() {
	if (isCacheAllowed()) {
	    CacheConfig cacheConfig = getCacheConfig();
	    cacheConfig.setLastCachedAt(new Date());
	    cacheConfigRepository.save(cacheConfig);
	}
    }

    public boolean isCacheAllowed() {
	boolean cacheAllowed = false;

	CacheConfig cacheConfig = getCacheConfig();
	log.info(StringUtils.concatValues("Cache Config : ", cacheConfig));
	if (CommonUtils.isNull(cacheConfig.getLastCachedAt())) {
	    cacheAllowed = true;
	}
	else {
	    final long lastCachedAt = cacheConfig.getLastCachedAt().getTime();
	    final long newCacheTime = lastCachedAt + GoogleMapProperties.PLACES_CACHE_REFRESH_TIME_MS;

	    log.info(StringUtils.concatValues("New cache time [", newCacheTime, "], Places Refresh time [",
		    GoogleMapProperties.PLACES_CACHE_REFRESH_TIME_MS, "]"));

	    final long currentTime = new Date().getTime();

	    cacheAllowed = currentTime > newCacheTime;

	    log.info(StringUtils.concatValues("Current time [", currentTime, "]"));
	}

	return cacheAllowed;
    }

    public Date getLastCachedAt() {
	return getCacheConfig().getLastCachedAt();
    };

}
