package com.clubportal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClubPortalApplication {

    private static final Logger log = LoggerFactory.getLogger(ClubPortalApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ClubPortalApplication.class, args);
        log.info("Club Portal Backend is running on http://localhost:8080");
    }
}
