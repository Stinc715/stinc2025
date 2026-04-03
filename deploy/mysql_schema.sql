-- Club Portal core schema (MySQL / AWS RDS MySQL compatible)
-- Safe to run multiple times (uses IF NOT EXISTS).
--
-- NOTE: This file does NOT create/select a database.
-- Apply it to the target DB explicitly, e.g.:
--   mysql -D club_portal_db < deploy/mysql_schema.sql

-- 1) User
CREATE TABLE IF NOT EXISTS `user` (
  `user_id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(120) NOT NULL,
  `email` VARCHAR(120) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(40) DEFAULT NULL,
  `avatar_file_name` VARCHAR(255) DEFAULT NULL,
  `avatar_mime_type` VARCHAR(120) DEFAULT NULL,
  `avatar_updated_at` DATETIME DEFAULT NULL,
  `role` ENUM('USER','CLUB','ADMIN') NOT NULL DEFAULT 'USER',
  `session_version` INT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @user_avatar_file_name_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND column_name = 'avatar_file_name'
);
SET @user_avatar_file_name_sql := IF(
  @user_avatar_file_name_col_exists = 0,
  'ALTER TABLE `user` ADD COLUMN `avatar_file_name` VARCHAR(255) NULL AFTER `phone`',
  'SELECT 1'
);
PREPARE user_avatar_file_name_stmt FROM @user_avatar_file_name_sql;
EXECUTE user_avatar_file_name_stmt;
DEALLOCATE PREPARE user_avatar_file_name_stmt;

SET @user_avatar_mime_type_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND column_name = 'avatar_mime_type'
);
SET @user_avatar_mime_type_sql := IF(
  @user_avatar_mime_type_col_exists = 0,
  'ALTER TABLE `user` ADD COLUMN `avatar_mime_type` VARCHAR(120) NULL AFTER `avatar_file_name`',
  'SELECT 1'
);
PREPARE user_avatar_mime_type_stmt FROM @user_avatar_mime_type_sql;
EXECUTE user_avatar_mime_type_stmt;
DEALLOCATE PREPARE user_avatar_mime_type_stmt;

SET @user_avatar_updated_at_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND column_name = 'avatar_updated_at'
);
SET @user_avatar_updated_at_sql := IF(
  @user_avatar_updated_at_col_exists = 0,
  'ALTER TABLE `user` ADD COLUMN `avatar_updated_at` DATETIME NULL AFTER `avatar_mime_type`',
  'SELECT 1'
);
PREPARE user_avatar_updated_at_stmt FROM @user_avatar_updated_at_sql;
EXECUTE user_avatar_updated_at_stmt;
DEALLOCATE PREPARE user_avatar_updated_at_stmt;

SET @user_session_version_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND column_name = 'session_version'
);
SET @user_session_version_sql := IF(
  @user_session_version_col_exists = 0,
  'ALTER TABLE `user` ADD COLUMN `session_version` INT NOT NULL DEFAULT 1 AFTER `role`',
  'SELECT 1'
);
PREPARE user_session_version_stmt FROM @user_session_version_sql;
EXECUTE user_session_version_stmt;
DEALLOCATE PREPARE user_session_version_stmt;

SET @user_role_column_type := (
  SELECT COLUMN_TYPE
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND column_name = 'role'
  LIMIT 1
);
SET @user_role_sql := IF(
  @user_role_column_type IS NULL,
  'SELECT 1',
  IF(
    @user_role_column_type <> 'enum(''USER'',''CLUB'',''ADMIN'')',
    'ALTER TABLE `user` MODIFY COLUMN `role` ENUM(''USER'',''CLUB'',''ADMIN'') NOT NULL DEFAULT ''USER''',
    'SELECT 1'
  )
);
PREPARE user_role_stmt FROM @user_role_sql;
EXECUTE user_role_stmt;
DEALLOCATE PREPARE user_role_stmt;

-- 2) Club
CREATE TABLE IF NOT EXISTS `club` (
  `club_id` INT NOT NULL AUTO_INCREMENT,
  `club_name` VARCHAR(120) NOT NULL,
  `description` TEXT,
  `category` VARCHAR(60),
  `category_tags` TEXT,
  `email` VARCHAR(120),
  `phone` VARCHAR(40),
  `display_location` VARCHAR(255),
  `google_place_id` VARCHAR(255),
  `location_lat` DOUBLE,
  `location_lng` DOUBLE,
  `opening_start` VARCHAR(5),
  `opening_end` VARCHAR(5),
  `display_courts` INT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`club_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `club_image` (
  `image_id` INT NOT NULL AUTO_INCREMENT,
  `club_id` INT NOT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `original_name` VARCHAR(255) DEFAULT NULL,
  `mime_type` VARCHAR(120) DEFAULT NULL,
  `size_bytes` BIGINT DEFAULT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `is_primary` TINYINT(1) NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`image_id`),
  KEY `idx_club_image_club_id` (`club_id`),
  CONSTRAINT `fk_club_image_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) Venue
CREATE TABLE IF NOT EXISTS `venue` (
  `venue_id` INT NOT NULL AUTO_INCREMENT,
  `club_id` INT NOT NULL,
  `venue_name` VARCHAR(120) NOT NULL,
  `location` VARCHAR(255),
  `capacity` INT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`venue_id`),
  KEY `idx_venue_club_id` (`club_id`),
  FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) TimeSlot
CREATE TABLE IF NOT EXISTS `timeslot` (
  `timeslot_id` INT NOT NULL AUTO_INCREMENT,
  `venue_id` INT NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `max_capacity` INT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`timeslot_id`),
  KEY `idx_timeslot_venue_id` (`venue_id`),
  KEY `idx_timeslot_start_end` (`start_time`, `end_time`),
  FOREIGN KEY (`venue_id`) REFERENCES `venue` (`venue_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Migration for existing databases: add timeslot.price if missing.
SET @timeslot_price_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'timeslot'
    AND column_name = 'price'
);
SET @timeslot_price_sql := IF(
  @timeslot_price_col_exists = 0,
  'ALTER TABLE `timeslot` ADD COLUMN `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER `max_capacity`',
  'SELECT 1'
);
PREPARE timeslot_price_stmt FROM @timeslot_price_sql;
EXECUTE timeslot_price_stmt;
DEALLOCATE PREPARE timeslot_price_stmt;

-- Migration for existing databases: add club display fields if missing.
SET @club_display_location_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'club'
    AND column_name = 'display_location'
);
SET @club_display_location_sql := IF(
  @club_display_location_col_exists = 0,
  'ALTER TABLE `club` ADD COLUMN `display_location` VARCHAR(255) NULL AFTER `phone`',
  'SELECT 1'
);
PREPARE club_display_location_stmt FROM @club_display_location_sql;
EXECUTE club_display_location_stmt;
DEALLOCATE PREPARE club_display_location_stmt;

SET @club_google_place_id_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'club'
    AND column_name = 'google_place_id'
);
SET @club_google_place_id_sql := IF(
  @club_google_place_id_col_exists = 0,
  'ALTER TABLE `club` ADD COLUMN `google_place_id` VARCHAR(255) NULL AFTER `display_location`',
  'SELECT 1'
);
PREPARE club_google_place_id_stmt FROM @club_google_place_id_sql;
EXECUTE club_google_place_id_stmt;
DEALLOCATE PREPARE club_google_place_id_stmt;

SET @club_location_lat_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'club'
    AND column_name = 'location_lat'
);
SET @club_location_lat_sql := IF(
  @club_location_lat_col_exists = 0,
  'ALTER TABLE `club` ADD COLUMN `location_lat` DOUBLE NULL AFTER `google_place_id`',
  'SELECT 1'
);
PREPARE club_location_lat_stmt FROM @club_location_lat_sql;
EXECUTE club_location_lat_stmt;
DEALLOCATE PREPARE club_location_lat_stmt;

SET @club_location_lng_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'club'
    AND column_name = 'location_lng'
);
SET @club_location_lng_sql := IF(
  @club_location_lng_col_exists = 0,
  'ALTER TABLE `club` ADD COLUMN `location_lng` DOUBLE NULL AFTER `location_lat`',
  'SELECT 1'
);
PREPARE club_location_lng_stmt FROM @club_location_lng_sql;
EXECUTE club_location_lng_stmt;
DEALLOCATE PREPARE club_location_lng_stmt;

SET @club_opening_start_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'club'
    AND column_name = 'opening_start'
);
SET @club_opening_start_sql := IF(
  @club_opening_start_col_exists = 0,
  'ALTER TABLE `club` ADD COLUMN `opening_start` VARCHAR(5) NULL AFTER `display_location`',
  'SELECT 1'
);
PREPARE club_opening_start_stmt FROM @club_opening_start_sql;
EXECUTE club_opening_start_stmt;
DEALLOCATE PREPARE club_opening_start_stmt;

SET @club_opening_end_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'club'
    AND column_name = 'opening_end'
);
SET @club_opening_end_sql := IF(
  @club_opening_end_col_exists = 0,
  'ALTER TABLE `club` ADD COLUMN `opening_end` VARCHAR(5) NULL AFTER `opening_start`',
  'SELECT 1'
);
PREPARE club_opening_end_stmt FROM @club_opening_end_sql;
EXECUTE club_opening_end_stmt;
DEALLOCATE PREPARE club_opening_end_stmt;

SET @club_display_courts_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'club'
    AND column_name = 'display_courts'
);
SET @club_display_courts_sql := IF(
  @club_display_courts_col_exists = 0,
  'ALTER TABLE `club` ADD COLUMN `display_courts` INT NULL AFTER `opening_end`',
  'SELECT 1'
);
PREPARE club_display_courts_stmt FROM @club_display_courts_sql;
EXECUTE club_display_courts_stmt;
DEALLOCATE PREPARE club_display_courts_stmt;

SET @club_category_tags_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'club'
    AND column_name = 'category_tags'
);
SET @club_category_tags_sql := IF(
  @club_category_tags_col_exists = 0,
  'ALTER TABLE `club` ADD COLUMN `category_tags` TEXT NULL AFTER `category`',
  'SELECT 1'
);
PREPARE club_category_tags_stmt FROM @club_category_tags_sql;
EXECUTE club_category_tags_stmt;
DEALLOCATE PREPARE club_category_tags_stmt;

-- Migration for existing databases: add club_image.is_primary if missing.
SET @club_image_is_primary_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'club_image'
    AND column_name = 'is_primary'
);
SET @club_image_is_primary_sql := IF(
  @club_image_is_primary_col_exists = 0,
  'ALTER TABLE `club_image` ADD COLUMN `is_primary` TINYINT(1) NULL DEFAULT 0 AFTER `sort_order`',
  'SELECT 1'
);
PREPARE club_image_is_primary_stmt FROM @club_image_is_primary_sql;
EXECUTE club_image_is_primary_stmt;
DEALLOCATE PREPARE club_image_is_primary_stmt;

-- 5) MembershipPlan
CREATE TABLE IF NOT EXISTS `membership_plan` (
  `plan_id` INT NOT NULL AUTO_INCREMENT,
  `club_id` INT NOT NULL,
  `plan_code` VARCHAR(20) DEFAULT NULL,
  `benefit_type` VARCHAR(30) NOT NULL DEFAULT 'DISCOUNT',
  `plan_name` VARCHAR(120) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `duration_days` INT NOT NULL,
  `discount_percent` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  `included_bookings` INT NOT NULL DEFAULT 0,
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `description` TEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`plan_id`),
  KEY `idx_membership_plan_club_id` (`club_id`),
  FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @membership_plan_code_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'membership_plan'
    AND column_name = 'plan_code'
);
SET @membership_plan_code_sql := IF(
  @membership_plan_code_col_exists = 0,
  'ALTER TABLE `membership_plan` ADD COLUMN `plan_code` VARCHAR(20) NULL AFTER `club_id`',
  'SELECT 1'
);
PREPARE membership_plan_code_stmt FROM @membership_plan_code_sql;
EXECUTE membership_plan_code_stmt;
DEALLOCATE PREPARE membership_plan_code_stmt;

SET @membership_benefit_type_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'membership_plan'
    AND column_name = 'benefit_type'
);
SET @membership_benefit_type_sql := IF(
  @membership_benefit_type_col_exists = 0,
  'ALTER TABLE `membership_plan` ADD COLUMN `benefit_type` VARCHAR(30) NOT NULL DEFAULT ''DISCOUNT'' AFTER `plan_code`',
  'SELECT 1'
);
PREPARE membership_benefit_type_stmt FROM @membership_benefit_type_sql;
EXECUTE membership_benefit_type_stmt;
DEALLOCATE PREPARE membership_benefit_type_stmt;

SET @membership_discount_percent_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'membership_plan'
    AND column_name = 'discount_percent'
);
SET @membership_discount_percent_sql := IF(
  @membership_discount_percent_col_exists = 0,
  'ALTER TABLE `membership_plan` ADD COLUMN `discount_percent` DECIMAL(5,2) NOT NULL DEFAULT 0.00 AFTER `duration_days`',
  'SELECT 1'
);
PREPARE membership_discount_percent_stmt FROM @membership_discount_percent_sql;
EXECUTE membership_discount_percent_stmt;
DEALLOCATE PREPARE membership_discount_percent_stmt;

SET @membership_included_bookings_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'membership_plan'
    AND column_name = 'included_bookings'
);
SET @membership_included_bookings_sql := IF(
  @membership_included_bookings_col_exists = 0,
  'ALTER TABLE `membership_plan` ADD COLUMN `included_bookings` INT NOT NULL DEFAULT 0 AFTER `discount_percent`',
  'SELECT 1'
);
PREPARE membership_included_bookings_stmt FROM @membership_included_bookings_sql;
EXECUTE membership_included_bookings_stmt;
DEALLOCATE PREPARE membership_included_bookings_stmt;

SET @membership_enabled_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'membership_plan'
    AND column_name = 'enabled'
);
SET @membership_enabled_sql := IF(
  @membership_enabled_col_exists = 0,
  'ALTER TABLE `membership_plan` ADD COLUMN `enabled` TINYINT(1) NOT NULL DEFAULT 1 AFTER `discount_percent`',
  'SELECT 1'
);
PREPARE membership_enabled_stmt FROM @membership_enabled_sql;
EXECUTE membership_enabled_stmt;
DEALLOCATE PREPARE membership_enabled_stmt;

-- 6) UserMembership
CREATE TABLE IF NOT EXISTS `user_membership` (
  `user_membership_id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `plan_id` INT NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `included_bookings_total` INT DEFAULT NULL,
  `remaining_bookings` INT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_membership_id`),
  KEY `idx_user_membership_user_id` (`user_id`),
  KEY `idx_user_membership_plan_id` (`plan_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  FOREIGN KEY (`plan_id`) REFERENCES `membership_plan` (`plan_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @user_membership_total_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user_membership'
    AND column_name = 'included_bookings_total'
);
SET @user_membership_total_sql := IF(
  @user_membership_total_col_exists = 0,
  'ALTER TABLE `user_membership` ADD COLUMN `included_bookings_total` INT NULL AFTER `status`',
  'SELECT 1'
);
PREPARE user_membership_total_stmt FROM @user_membership_total_sql;
EXECUTE user_membership_total_stmt;
DEALLOCATE PREPARE user_membership_total_stmt;

SET @user_membership_remaining_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user_membership'
    AND column_name = 'remaining_bookings'
);
SET @user_membership_remaining_sql := IF(
  @user_membership_remaining_col_exists = 0,
  'ALTER TABLE `user_membership` ADD COLUMN `remaining_bookings` INT NULL AFTER `included_bookings_total`',
  'SELECT 1'
);
PREPARE user_membership_remaining_stmt FROM @user_membership_remaining_sql;
EXECUTE user_membership_remaining_stmt;
DEALLOCATE PREPARE user_membership_remaining_stmt;

-- 7) BookingRecord
CREATE TABLE IF NOT EXISTS `booking_record` (
  `booking_id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `timeslot_id` INT NOT NULL,
  `booking_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `price_paid` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `user_membership_id` INT NULL,
  `membership_credit_used` TINYINT(1) NOT NULL DEFAULT 0,
  `booking_verification_code` VARCHAR(6) DEFAULT NULL,
  PRIMARY KEY (`booking_id`),
  UNIQUE KEY `uk_booking_user_timeslot` (`user_id`, `timeslot_id`),
  UNIQUE KEY `uk_booking_verification_code` (`booking_verification_code`),
  KEY `idx_booking_record_timeslot_id` (`timeslot_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  FOREIGN KEY (`timeslot_id`) REFERENCES `timeslot` (`timeslot_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @booking_credit_used_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'booking_record'
    AND column_name = 'membership_credit_used'
);
SET @booking_credit_used_sql := IF(
  @booking_credit_used_col_exists = 0,
  'ALTER TABLE `booking_record` ADD COLUMN `membership_credit_used` TINYINT(1) NOT NULL DEFAULT 0 AFTER `user_membership_id`',
  'SELECT 1'
);
PREPARE booking_credit_used_stmt FROM @booking_credit_used_sql;
EXECUTE booking_credit_used_stmt;
DEALLOCATE PREPARE booking_credit_used_stmt;

SET @booking_price_paid_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'booking_record'
    AND column_name = 'price_paid'
);
SET @booking_price_paid_sql := IF(
  @booking_price_paid_col_exists = 0,
  'ALTER TABLE `booking_record` ADD COLUMN `price_paid` DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER `status`',
  'SELECT 1'
);
PREPARE booking_price_paid_stmt FROM @booking_price_paid_sql;
EXECUTE booking_price_paid_stmt;
DEALLOCATE PREPARE booking_price_paid_stmt;

SET @booking_user_membership_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'booking_record'
    AND column_name = 'user_membership_id'
);
SET @booking_user_membership_sql := IF(
  @booking_user_membership_col_exists = 0,
  'ALTER TABLE `booking_record` ADD COLUMN `user_membership_id` INT NULL AFTER `price_paid`',
  'SELECT 1'
);
PREPARE booking_user_membership_stmt FROM @booking_user_membership_sql;
EXECUTE booking_user_membership_stmt;
DEALLOCATE PREPARE booking_user_membership_stmt;

SET @booking_verification_code_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'booking_record'
    AND column_name = 'booking_verification_code'
);
SET @booking_verification_code_sql := IF(
  @booking_verification_code_col_exists = 0,
  'ALTER TABLE `booking_record` ADD COLUMN `booking_verification_code` VARCHAR(6) DEFAULT NULL AFTER `user_membership_id`',
  'SELECT 1'
);
PREPARE booking_verification_code_stmt FROM @booking_verification_code_sql;
EXECUTE booking_verification_code_stmt;
DEALLOCATE PREPARE booking_verification_code_stmt;

SET @booking_verification_code_idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'booking_record'
    AND index_name = 'uk_booking_verification_code'
);
SET @booking_verification_code_idx_sql := IF(
  @booking_verification_code_idx_exists = 0,
  'ALTER TABLE `booking_record` ADD UNIQUE KEY `uk_booking_verification_code` (`booking_verification_code`)',
  'SELECT 1'
);
PREPARE booking_verification_code_idx_stmt FROM @booking_verification_code_idx_sql;
EXECUTE booking_verification_code_idx_stmt;
DEALLOCATE PREPARE booking_verification_code_idx_stmt;

-- 8) ClubAdmin
CREATE TABLE IF NOT EXISTS `club_admin` (
  `club_admin_id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `club_id` INT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`club_admin_id`),
  UNIQUE KEY `uk_club_admin_user_club` (`user_id`, `club_id`),
  KEY `idx_club_admin_club_id` (`club_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9) Transaction
CREATE TABLE IF NOT EXISTS `transaction` (
  `transaction_id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `user_membership_id` INT NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `payment_method` VARCHAR(40) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `transaction_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`transaction_id`),
  KEY `idx_transaction_user_id` (`user_id`),
  KEY `idx_transaction_user_membership_id` (`user_membership_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_membership_id`) REFERENCES `user_membership` (`user_membership_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `checkout_session` (
  `checkout_session_pk` INT NOT NULL AUTO_INCREMENT,
  `session_id` VARCHAR(64) NOT NULL,
  `order_no` VARCHAR(32) DEFAULT NULL,
  `user_id` INT NOT NULL,
  `club_id` INT NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `timeslot_id` INT DEFAULT NULL,
  `membership_plan_id` INT DEFAULT NULL,
  `booking_id` INT DEFAULT NULL,
  `user_membership_id` INT DEFAULT NULL,
  `transaction_id` INT DEFAULT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `currency` VARCHAR(10) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `provider` VARCHAR(40) NOT NULL,
  `provider_session_id` VARCHAR(255) DEFAULT NULL,
  `checkout_url` VARCHAR(2048) DEFAULT NULL,
  `failure_reason` VARCHAR(255) DEFAULT NULL,
  `expires_at` DATETIME NOT NULL,
  `completed_at` DATETIME DEFAULT NULL,
  `cancelled_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`checkout_session_pk`),
  UNIQUE KEY `uk_checkout_session_session_id` (`session_id`),
  UNIQUE KEY `uk_checkout_session_order_no` (`order_no`),
  UNIQUE KEY `uk_checkout_session_provider_session_id` (`provider_session_id`),
  KEY `idx_checkout_session_user_status` (`user_id`, `status`, `expires_at`),
  KEY `idx_checkout_session_type_club` (`type`, `club_id`, `status`, `expires_at`),
  KEY `idx_checkout_session_timeslot` (`timeslot_id`, `status`, `expires_at`),
  CONSTRAINT `fk_checkout_session_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_checkout_session_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_checkout_session_timeslot` FOREIGN KEY (`timeslot_id`) REFERENCES `timeslot` (`timeslot_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_checkout_session_membership_plan` FOREIGN KEY (`membership_plan_id`) REFERENCES `membership_plan` (`plan_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_checkout_session_booking` FOREIGN KEY (`booking_id`) REFERENCES `booking_record` (`booking_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_checkout_session_user_membership` FOREIGN KEY (`user_membership_id`) REFERENCES `user_membership` (`user_membership_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_checkout_session_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `transaction` (`transaction_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @checkout_order_no_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'checkout_session'
    AND column_name = 'order_no'
);
SET @checkout_order_no_sql := IF(
  @checkout_order_no_col_exists = 0,
  'ALTER TABLE `checkout_session` ADD COLUMN `order_no` VARCHAR(32) DEFAULT NULL AFTER `session_id`',
  'SELECT 1'
);
PREPARE checkout_order_no_stmt FROM @checkout_order_no_sql;
EXECUTE checkout_order_no_stmt;
DEALLOCATE PREPARE checkout_order_no_stmt;

SET @checkout_order_no_idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'checkout_session'
    AND index_name = 'uk_checkout_session_order_no'
);
SET @checkout_order_no_idx_sql := IF(
  @checkout_order_no_idx_exists = 0,
  'ALTER TABLE `checkout_session` ADD UNIQUE KEY `uk_checkout_session_order_no` (`order_no`)',
  'SELECT 1'
);
PREPARE checkout_order_no_idx_stmt FROM @checkout_order_no_idx_sql;
EXECUTE checkout_order_no_idx_stmt;
DEALLOCATE PREPARE checkout_order_no_idx_stmt;

CREATE TABLE IF NOT EXISTS `booking_hold` (
  `booking_hold_id` INT NOT NULL AUTO_INCREMENT,
  `checkout_session_id` VARCHAR(64) NOT NULL,
  `user_id` INT NOT NULL,
  `club_id` INT NOT NULL,
  `timeslot_id` INT NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `expires_at` DATETIME NOT NULL,
  `released_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`booking_hold_id`),
  UNIQUE KEY `uk_booking_hold_checkout_session_id` (`checkout_session_id`),
  KEY `idx_booking_hold_timeslot_status` (`timeslot_id`, `status`, `expires_at`),
  CONSTRAINT `fk_booking_hold_checkout_session` FOREIGN KEY (`checkout_session_id`) REFERENCES `checkout_session` (`session_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_booking_hold_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_booking_hold_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_booking_hold_timeslot` FOREIGN KEY (`timeslot_id`) REFERENCES `timeslot` (`timeslot_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `chat_session` (
  `session_id` INT NOT NULL AUTO_INCREMENT,
  `club_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `chat_mode` VARCHAR(30) NOT NULL DEFAULT 'AI',
  `handoff_requested_at` DATETIME DEFAULT NULL,
  `handoff_reason` VARCHAR(50) DEFAULT NULL,
  `club_unread_count` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`session_id`),
  UNIQUE KEY `uk_chat_session_club_user` (`club_id`, `user_id`),
  KEY `idx_chat_session_club_mode_updated` (`club_id`, `chat_mode`, `updated_at`),
  KEY `idx_chat_session_user_mode_updated` (`user_id`, `chat_mode`, `updated_at`),
  CONSTRAINT `fk_chat_session_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_chat_session_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `chat_message` (
  `message_id` INT NOT NULL AUTO_INCREMENT,
  `club_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `sender` VARCHAR(10) NOT NULL,
  `message_text` VARCHAR(1000) NOT NULL,
  `answer_source` VARCHAR(40) DEFAULT NULL,
  `matched_faq_id` INT DEFAULT NULL,
  `handoff_suggested` TINYINT(1) NOT NULL DEFAULT 0,
  `read_by_club` TINYINT(1) NOT NULL DEFAULT 0,
  `read_by_user` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  KEY `idx_chat_message_club_user_created` (`club_id`, `user_id`, `created_at`, `message_id`),
  KEY `idx_chat_message_club_unread` (`club_id`, `sender`, `read_by_club`, `created_at`),
  KEY `idx_chat_message_user_unread` (`user_id`, `sender`, `read_by_user`, `created_at`),
  CONSTRAINT `fk_chat_message_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_chat_message_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `club_chat_kb_entry` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `club_id` INT NOT NULL,
  `question_title` VARCHAR(255) NOT NULL,
  `answer_text` TEXT NOT NULL,
  `question_embedding` LONGTEXT DEFAULT NULL,
  `embedding_model` VARCHAR(120) DEFAULT NULL,
  `embedding_dim` INT DEFAULT NULL,
  `trigger_keywords` TEXT DEFAULT NULL,
  `example_questions` TEXT DEFAULT NULL,
  `language` VARCHAR(10) NOT NULL DEFAULT 'ANY',
  `priority` INT NOT NULL DEFAULT 0,
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_club_chat_kb_entry_club_priority` (`club_id`, `enabled`, `priority`),
  KEY `idx_club_chat_kb_entry_language` (`club_id`, `language`, `enabled`),
  CONSTRAINT `fk_club_chat_kb_entry_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `club_community_question` (
  `question_id` INT NOT NULL AUTO_INCREMENT,
  `club_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `question_text` TEXT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`),
  KEY `idx_club_community_question_club_updated` (`club_id`, `updated_at`, `question_id`),
  CONSTRAINT `fk_club_community_question_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_club_community_question_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `club_community_answer` (
  `answer_id` INT NOT NULL AUTO_INCREMENT,
  `question_id` INT NOT NULL,
  `club_id` INT NOT NULL,
  `user_id` INT DEFAULT NULL,
  `responder_type` ENUM('USER', 'CLUB') NOT NULL,
  `answer_text` TEXT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`answer_id`),
  KEY `idx_club_community_answer_question_created` (`question_id`, `created_at`, `answer_id`),
  CONSTRAINT `fk_club_community_answer_question` FOREIGN KEY (`question_id`) REFERENCES `club_community_question` (`question_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_club_community_answer_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_club_community_answer_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @club_community_answer_user_fk_exists := (
  SELECT COUNT(*)
  FROM information_schema.referential_constraints
  WHERE constraint_schema = DATABASE()
    AND table_name = 'club_community_answer'
    AND constraint_name = 'fk_club_community_answer_user'
);
SET @club_community_answer_drop_fk_sql := IF(
  @club_community_answer_user_fk_exists > 0,
  'ALTER TABLE `club_community_answer` DROP FOREIGN KEY `fk_club_community_answer_user`',
  'SELECT 1'
);
PREPARE club_community_answer_drop_fk_stmt FROM @club_community_answer_drop_fk_sql;
EXECUTE club_community_answer_drop_fk_stmt;
DEALLOCATE PREPARE club_community_answer_drop_fk_stmt;

SET @club_community_answer_user_nullable := (
  SELECT IS_NULLABLE
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'club_community_answer'
    AND column_name = 'user_id'
  LIMIT 1
);
SET @club_community_answer_user_nullable_sql := IF(
  @club_community_answer_user_nullable = 'NO',
  'ALTER TABLE `club_community_answer` MODIFY COLUMN `user_id` INT NULL',
  'SELECT 1'
);
PREPARE club_community_answer_user_nullable_stmt FROM @club_community_answer_user_nullable_sql;
EXECUTE club_community_answer_user_nullable_stmt;
DEALLOCATE PREPARE club_community_answer_user_nullable_stmt;

SET @club_community_answer_user_fk_exists := (
  SELECT COUNT(*)
  FROM information_schema.referential_constraints
  WHERE constraint_schema = DATABASE()
    AND table_name = 'club_community_answer'
    AND constraint_name = 'fk_club_community_answer_user'
);
SET @club_community_answer_add_fk_sql := IF(
  @club_community_answer_user_fk_exists = 0,
  'ALTER TABLE `club_community_answer` ADD CONSTRAINT `fk_club_community_answer_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL',
  'SELECT 1'
);
PREPARE club_community_answer_add_fk_stmt FROM @club_community_answer_add_fk_sql;
EXECUTE club_community_answer_add_fk_stmt;
DEALLOCATE PREPARE club_community_answer_add_fk_stmt;

CREATE TABLE IF NOT EXISTS `registration_email_verification` (
  `verification_id` INT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(120) NOT NULL,
  `verification_code` VARCHAR(6) DEFAULT NULL,
  `sent_at` DATETIME DEFAULT NULL,
  `expires_at` DATETIME DEFAULT NULL,
  `verified_until` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`verification_id`),
  UNIQUE KEY `uk_registration_email_verification_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `password_reset_token` (
  `password_reset_id` INT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(120) NOT NULL,
  `reset_token` VARCHAR(255) NOT NULL,
  `sent_at` DATETIME NOT NULL,
  `expires_at` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`password_reset_id`),
  UNIQUE KEY `uk_password_reset_token_token` (`reset_token`),
  UNIQUE KEY `uk_password_reset_token_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `profile_email_change_verification` (
  `verification_id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `pending_email` VARCHAR(120) NOT NULL,
  `verification_code` VARCHAR(6) NOT NULL,
  `sent_at` DATETIME NOT NULL,
  `expires_at` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`verification_id`),
  UNIQUE KEY `uk_profile_email_change_user` (`user_id`),
  UNIQUE KEY `uk_profile_email_change_email` (`pending_email`),
  KEY `idx_profile_email_change_expires_at` (`expires_at`),
  CONSTRAINT `fk_profile_email_change_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
