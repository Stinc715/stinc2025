package com.clubportal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevClubDatasetInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevClubDatasetInitializer.class);

    private final ClubDatasetSeeder clubDatasetSeeder;

    public DevClubDatasetInitializer(ClubDatasetSeeder clubDatasetSeeder) {
        this.clubDatasetSeeder = clubDatasetSeeder;
    }

    @Override
    public void run(ApplicationArguments args) {
        ClubDatasetSeeder.SeedExecutionResult result = clubDatasetSeeder.reseedForCurrentWindow();
        log.info("DEV_CLUB_SEED rebuilt {} clubs and {} club accounts for {} to {}",
                result.clubCount(),
                result.clubCount(),
                result.startDate(),
                result.endDate());
        log.info("DEV_CLUB_SEED replaced previous club names={} previous club emails={}",
                result.previousClubNames(),
                result.previousClubEmails());
    }
}
