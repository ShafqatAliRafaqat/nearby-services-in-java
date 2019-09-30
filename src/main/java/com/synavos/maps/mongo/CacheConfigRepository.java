package com.synavos.maps.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.synavos.maps.config.CacheConfig;

public interface CacheConfigRepository extends MongoRepository<CacheConfig, String> {}
