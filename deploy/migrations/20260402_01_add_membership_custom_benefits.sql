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

UPDATE `membership_plan`
SET `benefit_type` = 'DISCOUNT'
WHERE `benefit_type` IS NULL OR TRIM(`benefit_type`) = '';

UPDATE `membership_plan`
SET `included_bookings` = 0
WHERE `included_bookings` IS NULL;

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

UPDATE `user_membership` um
JOIN `membership_plan` mp ON mp.`plan_id` = um.`plan_id`
SET um.`included_bookings_total` = CASE
      WHEN mp.`benefit_type` = 'BOOKING_PACK' THEN COALESCE(mp.`included_bookings`, 0)
      ELSE 0
    END,
    um.`remaining_bookings` = CASE
      WHEN mp.`benefit_type` = 'BOOKING_PACK' THEN COALESCE(um.`remaining_bookings`, COALESCE(mp.`included_bookings`, 0))
      ELSE 0
    END
WHERE um.`included_bookings_total` IS NULL
   OR um.`remaining_bookings` IS NULL;

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
