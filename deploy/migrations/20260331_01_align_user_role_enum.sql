SET @user_role_column_type := (
  SELECT COLUMN_TYPE
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND column_name = 'role'
  LIMIT 1
);

SET @user_role_enum_sql := IF(
  @user_role_column_type IS NULL,
  'SELECT 1',
  IF(
    @user_role_column_type <> 'enum(''USER'',''CLUB'',''ADMIN'')',
    'ALTER TABLE `user` MODIFY COLUMN `role` ENUM(''USER'',''CLUB'',''ADMIN'') NOT NULL DEFAULT ''USER''',
    'SELECT 1'
  )
);

PREPARE user_role_enum_stmt FROM @user_role_enum_sql;
EXECUTE user_role_enum_stmt;
DEALLOCATE PREPARE user_role_enum_stmt;
