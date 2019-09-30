package com.synavos.maps.config;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@lombok.Data
@lombok.ToString
public class CacheConfig {

    @Id
    private String id;

    private Date lastCachedAt;
}
