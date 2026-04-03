CREATE TABLE IF NOT EXISTS `club_community_question` (
  `question_id` INT NOT NULL AUTO_INCREMENT,
  `club_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `question_text` TEXT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`),
  KEY `idx_club_community_question_club_updated` (`club_id`, `updated_at`, `question_id`),
  KEY `idx_club_community_question_user_created` (`user_id`, `created_at`),
  CONSTRAINT `fk_club_community_question_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_club_community_question_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `club_community_answer` (
  `answer_id` INT NOT NULL AUTO_INCREMENT,
  `question_id` INT NOT NULL,
  `club_id` INT NOT NULL,
  `user_id` INT DEFAULT NULL,
  `responder_type` ENUM('USER','CLUB') NOT NULL DEFAULT 'USER',
  `answer_text` TEXT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`answer_id`),
  KEY `idx_club_community_answer_question_created` (`question_id`, `created_at`, `answer_id`),
  KEY `idx_club_community_answer_club_created` (`club_id`, `created_at`),
  CONSTRAINT `fk_club_community_answer_question` FOREIGN KEY (`question_id`) REFERENCES `club_community_question` (`question_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_club_community_answer_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_club_community_answer_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
