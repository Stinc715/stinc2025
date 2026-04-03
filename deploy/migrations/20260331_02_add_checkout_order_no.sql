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

UPDATE `checkout_session`
SET `order_no` = CONCAT(
  CASE
    WHEN UPPER(TRIM(COALESCE(`type`, ''))) = 'MEMBERSHIP' THEN 'MB'
    ELSE 'BK'
  END,
  '-',
  DATE_FORMAT(COALESCE(`created_at`, NOW()), '%Y%m%d%H%i%s'),
  '-',
  UPPER(LPAD(HEX(`checkout_session_pk`), 6, '0'))
)
WHERE (`order_no` IS NULL OR TRIM(`order_no`) = '');

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
