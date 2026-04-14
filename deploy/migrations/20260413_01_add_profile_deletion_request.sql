CREATE TABLE IF NOT EXISTS `profile_deletion_request` (
  `request_id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `email_snapshot` VARCHAR(120) NOT NULL,
  `display_name_snapshot` VARCHAR(120) NOT NULL,
  `role_snapshot` VARCHAR(20) NOT NULL,
  `reason` VARCHAR(500) DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `requested_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `resolved_at` DATETIME DEFAULT NULL,
  `resolution_note` VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`request_id`),
  KEY `idx_profile_deletion_request_user_status` (`user_id`, `status`, `requested_at`),
  CONSTRAINT `fk_profile_deletion_request_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
