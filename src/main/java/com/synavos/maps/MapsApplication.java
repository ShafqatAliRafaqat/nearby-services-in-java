package com.synavos.maps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * The Class MapsApplication.
 *
 * @author Ibraheem Faiq
 * @since Apr 4, 2018
 */
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.synavos.maps.mongo")
public class MapsApplication {

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
	SpringApplication.run(MapsApplication.class, args);
    }
}
