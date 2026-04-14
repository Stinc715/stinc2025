package com.clubportal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!dev")
@ConditionalOnProperty(name = "app.seed.production-club-content-refresh.enabled", havingValue = "true")
public class ProductionCanonicalClubContentRefreshInitializer implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(ProductionCanonicalClubContentRefreshInitializer.class);

    private final ClubDatasetSeeder clubDatasetSeeder;

    public ProductionCanonicalClubContentRefreshInitializer(ClubDatasetSeeder clubDatasetSeeder) {
        this.clubDatasetSeeder = clubDatasetSeeder;
    }

    @Override
    public void afterSingletonsInstantiated() {
        try {
            ClubDatasetSeeder.CanonicalContentRefreshResult result = clubDatasetSeeder.refreshCanonicalOperationalData();
            log.info(
                    "PROD_CANONICAL_CLUB_CONTENT_REFRESH updated {} clubs, recreated {} membership plans (deleted {}), recreated {} timeslots (deleted {}), and refreshed {} questions/{} answers (deleted {}/{}), window {} to {}.",
                    result.clubsUpdated(),
                    result.plansCreated(),
                    result.plansDeleted(),
                    result.timeslotsCreated(),
                    result.timeslotsDeleted(),
                    result.questionsCreated(),
                    result.answersCreated(),
                    result.questionsDeleted(),
                    result.answersDeleted(),
                    result.startDate(),
                    result.endDate()
            );
        } catch (IllegalStateException ex) {
            log.error("PROD_CANONICAL_CLUB_CONTENT_REFRESH skipped: {}", ex.getMessage());
        }
    }
}
