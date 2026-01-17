package com.clubportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClubPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClubPortalApplication.class, args);
        System.out.println(" Club Portal Backend is running on http://localhost:8080");
    }
}
