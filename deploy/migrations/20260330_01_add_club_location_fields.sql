SET @schema_name = DATABASE();

SET @stmt = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'club'
      AND COLUMN_NAME = 'google_place_id'
  ),
  'SELECT 1',
  'ALTER TABLE `club` ADD COLUMN `google_place_id` VARCHAR(255) NULL AFTER `display_location`'
);
PREPARE add_club_google_place_id FROM @stmt;
EXECUTE add_club_google_place_id;
DEALLOCATE PREPARE add_club_google_place_id;

SET @stmt = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'club'
      AND COLUMN_NAME = 'location_lat'
  ),
  'SELECT 1',
  'ALTER TABLE `club` ADD COLUMN `location_lat` DOUBLE NULL AFTER `google_place_id`'
);
PREPARE add_club_location_lat FROM @stmt;
EXECUTE add_club_location_lat;
DEALLOCATE PREPARE add_club_location_lat;

SET @stmt = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'club'
      AND COLUMN_NAME = 'location_lng'
  ),
  'SELECT 1',
  'ALTER TABLE `club` ADD COLUMN `location_lng` DOUBLE NULL AFTER `location_lat`'
);
PREPARE add_club_location_lng FROM @stmt;
EXECUTE add_club_location_lng;
DEALLOCATE PREPARE add_club_location_lng;
