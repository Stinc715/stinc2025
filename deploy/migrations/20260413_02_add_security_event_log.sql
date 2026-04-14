CREATE TABLE IF NOT EXISTS `security_event_log` (
  `event_id` INT NOT NULL AUTO_INCREMENT,
  `event_type` VARCHAR(80) NOT NULL,
  `severity` VARCHAR(16) NOT NULL,
  `user_id` INT DEFAULT NULL,
  `email_snapshot` VARCHAR(120) DEFAULT NULL,
  `source_ip` VARCHAR(64) DEFAULT NULL,
  `user_agent` VARCHAR(255) DEFAULT NULL,
  `request_method` VARCHAR(16) DEFAULT NULL,
  `request_path` VARCHAR(255) DEFAULT NULL,
  `details_json` TEXT DEFAULT NULL,
  `alert_dispatched` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`event_id`),
  KEY `idx_security_event_created` (`created_at`),
  KEY `idx_security_event_type_created` (`event_type`, `created_at`),
  KEY `idx_security_event_user_created` (`user_id`, `created_at`),
  CONSTRAINT `fk_security_event_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
