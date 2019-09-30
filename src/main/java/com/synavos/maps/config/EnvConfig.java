package com.synavos.maps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EnvConfig {

    public static Boolean DEV_ENV;

    @Value("${environment.dev:false}")
    public void setDevEnv(final Boolean dev) {
	log.warn("Setting dev to [" + dev + "]");
	EnvConfig.DEV_ENV = dev;
    }
}
