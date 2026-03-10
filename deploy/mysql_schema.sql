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
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  `plan_name` VARCHAR(120) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `duration_days` INT NOT NULL,
  `discount_percent` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
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
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_membership_id`),
  KEY `idx_user_membership_user_id` (`user_id`),
  KEY `idx_user_membership_plan_id` (`plan_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  FOREIGN KEY (`plan_id`) REFERENCES `membership_plan` (`plan_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) BookingRecord
CREATE TABLE IF NOT EXISTS `booking_record` (
  `booking_id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `timeslot_id` INT NOT NULL,
  `booking_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `price_paid` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `user_membership_id` INT NULL,
  PRIMARY KEY (`booking_id`),
  UNIQUE KEY `uk_booking_user_timeslot` (`user_id`, `timeslot_id`),
  KEY `idx_booking_record_timeslot_id` (`timeslot_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  FOREIGN KEY (`timeslot_id`) REFERENCES `timeslot` (`timeslot_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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

CREATE TABLE IF NOT EXISTS `chat_message` (
  `message_id` INT NOT NULL AUTO_INCREMENT,
  `club_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `sender` VARCHAR(10) NOT NULL,
  `message_text` VARCHAR(1000) NOT NULL,
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
