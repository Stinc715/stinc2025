SET @schema_name = DATABASE();

SET @stmt = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'club_chat_kb_entry'
      AND COLUMN_NAME = 'question_embedding'
  ),
  'SELECT 1',
  'ALTER TABLE `club_chat_kb_entry` ADD COLUMN `question_embedding` LONGTEXT NULL AFTER `answer_text`'
);
PREPARE add_question_embedding FROM @stmt;
EXECUTE add_question_embedding;
DEALLOCATE PREPARE add_question_embedding;

SET @stmt = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'club_chat_kb_entry'
      AND COLUMN_NAME = 'embedding_model'
  ),
  'SELECT 1',
  'ALTER TABLE `club_chat_kb_entry` ADD COLUMN `embedding_model` VARCHAR(120) NULL AFTER `question_embedding`'
);
PREPARE add_embedding_model FROM @stmt;
EXECUTE add_embedding_model;
DEALLOCATE PREPARE add_embedding_model;

SET @stmt = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'club_chat_kb_entry'
      AND COLUMN_NAME = 'embedding_dim'
  ),
  'SELECT 1',
  'ALTER TABLE `club_chat_kb_entry` ADD COLUMN `embedding_dim` INT NULL AFTER `embedding_model`'
);
PREPARE add_embedding_dim FROM @stmt;
EXECUTE add_embedding_dim;
DEALLOCATE PREPARE add_embedding_dim;

ALTER TABLE `club_chat_kb_entry`
  MODIFY COLUMN `enabled` TINYINT(1) NOT NULL DEFAULT 1;
