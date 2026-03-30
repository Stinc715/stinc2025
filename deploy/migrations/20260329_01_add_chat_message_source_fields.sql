SET @add_answer_source = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'chat_message'
        AND COLUMN_NAME = 'answer_source'
    ),
    'SELECT 1',
    'ALTER TABLE `chat_message` ADD COLUMN `answer_source` VARCHAR(40) DEFAULT NULL AFTER `message_text`'
  )
);
PREPARE stmt FROM @add_answer_source;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_matched_faq_id = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'chat_message'
        AND COLUMN_NAME = 'matched_faq_id'
    ),
    'SELECT 1',
    'ALTER TABLE `chat_message` ADD COLUMN `matched_faq_id` INT DEFAULT NULL AFTER `answer_source`'
  )
);
PREPARE stmt FROM @add_matched_faq_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_handoff_suggested = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'chat_message'
        AND COLUMN_NAME = 'handoff_suggested'
    ),
    'SELECT 1',
    'ALTER TABLE `chat_message` ADD COLUMN `handoff_suggested` TINYINT(1) NOT NULL DEFAULT 0 AFTER `matched_faq_id`'
  )
);
PREPARE stmt FROM @add_handoff_suggested;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
