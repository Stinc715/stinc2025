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
