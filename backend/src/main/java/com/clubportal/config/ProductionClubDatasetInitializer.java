package com.clubportal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!dev")
@ConditionalOnProperty(name = "app.seed.production-clubs.enabled", havingValue = "true")
public class ProductionClubDatasetInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductionClubDatasetInitializer.class);

    private final ClubDatasetSeeder clubDatasetSeeder;
    private final int expectedExistingClubCount;
    private final int expectedExistingClubUserCount;
    private final boolean allowEmptyDataset;

    public ProductionClubDatasetInitializer(
            ClubDatasetSeeder clubDatasetSeeder,
            @Value("${app.seed.production-clubs.expected-existing-club-count:2}") int expectedExistingClubCount,
            @Value("${app.seed.production-clubs.expected-existing-club-user-count:2}") int expectedExistingClubUserCount,
            @Value("${app.seed.production-clubs.allow-empty-dataset:false}") boolean allowEmptyDataset) {
        this.clubDatasetSeeder = clubDatasetSeeder;
        this.expectedExistingClubCount = expectedExistingClubCount;
        this.expectedExistingClubUserCount = expectedExistingClubUserCount;
        this.allowEmptyDataset = allowEmptyDataset;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (clubDatasetSeeder.isCanonicalSeedPresent()) {
            log.info("PROD_CLUB_SEED skipped because the canonical 20-club dataset is already present.");
            return;
        }

        ClubDatasetSeeder.CurrentClubSnapshot snapshot = clubDatasetSeeder.snapshotCurrentClubDataset();
        int clubCount = snapshot.clubCount();
        int clubUserCount = snapshot.clubUserCount();
        boolean expectedPairCount = clubCount == expectedExistingClubCount
                && clubUserCount == expectedExistingClubUserCount;
        boolean emptyDataset = allowEmptyDataset && clubCount == 0 && clubUserCount == 0;

        if (!expectedPairCount && !emptyDataset) {
            throw new IllegalStateException(
                    "Refusing to replace production club data. Existing clubs=" + clubCount
                            + ", existing club users=" + clubUserCount
                            + ", expected clubs=" + expectedExistingClubCount
                            + ", expected club users=" + expectedExistingClubUserCount
                            + ". If this is intentional, adjust app.seed.production-clubs.expected-existing-club-count "
                            + "and app.seed.production-clubs.expected-existing-club-user-count, or set "
                            + "app.seed.production-clubs.allow-empty-dataset=true for an empty target.");
        }

        ClubDatasetSeeder.SeedExecutionResult result = clubDatasetSeeder.reseedForCurrentWindow();
        log.info("PROD_CLUB_SEED rebuilt {} clubs and {} club accounts for {} to {}",
                result.clubCount(),
                result.clubCount(),
                result.startDate(),
                result.endDate());
        log.info("PROD_CLUB_SEED replaced previous club names={} previous club emails={}",
                result.previousClubNames(),
                result.previousClubEmails());
    }
}
