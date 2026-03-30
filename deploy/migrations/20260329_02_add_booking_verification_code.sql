SET @add_booking_verification_code = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'booking_record'
        AND COLUMN_NAME = 'booking_verification_code'
    ),
    'SELECT 1',
    'ALTER TABLE `booking_record` ADD COLUMN `booking_verification_code` VARCHAR(6) DEFAULT NULL AFTER `user_membership_id`'
  )
);
PREPARE stmt FROM @add_booking_verification_code;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_booking_verification_code_index = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM INFORMATION_SCHEMA.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'booking_record'
        AND INDEX_NAME = 'uk_booking_verification_code'
    ),
    'SELECT 1',
    'ALTER TABLE `booking_record` ADD UNIQUE KEY `uk_booking_verification_code` (`booking_verification_code`)'
  )
);
PREPARE stmt FROM @add_booking_verification_code_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
