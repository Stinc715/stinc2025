package com.clubportal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!dev")
@ConditionalOnProperty(name = "app.seed.production-community-qa.enabled", havingValue = "true")
public class ProductionClubCommunityQaInitializer implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(ProductionClubCommunityQaInitializer.class);

    private final ClubDatasetSeeder clubDatasetSeeder;

    public ProductionClubCommunityQaInitializer(ClubDatasetSeeder clubDatasetSeeder) {
        this.clubDatasetSeeder = clubDatasetSeeder;
    }

    @Override
    public void afterSingletonsInstantiated() {
        ClubDatasetSeeder.CommunityQaSeedResult result = clubDatasetSeeder.backfillCommunityQaIfMissing();
        if (result.questionsCreated() == 0 && result.answersCreated() == 0) {
            log.info("PROD_COMMUNITY_QA_SEED skipped because every club already has community content.");
            return;
        }
        log.info("PROD_COMMUNITY_QA_SEED added {} questions and {} answers across {} clubs.",
                result.questionsCreated(),
                result.answersCreated(),
                result.clubsUpdated());
    }
}
