-- Flikata Social App Database Schema
-- MySQL 5.7+ / 8.0+ / MariaDB compatible
-- Character set: utf8mb4

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `full_name` VARCHAR(150) NOT NULL,
  `email` VARCHAR(150) DEFAULT NULL,
  `phone` VARCHAR(50) DEFAULT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `profile_pic` VARCHAR(500) DEFAULT NULL,
  `cover_pic` VARCHAR(500) DEFAULT NULL,
  `bio` VARCHAR(300) DEFAULT 'Hey there! I am using Flikata.',
  `occupation` VARCHAR(150) DEFAULT 'Software Engineer',
  `location` VARCHAR(150) DEFAULT 'Dhaka, Bangladesh',
  `gender` VARCHAR(20) DEFAULT 'Male',
  `dob` DATE DEFAULT NULL,
  `is_verified` TINYINT(1) DEFAULT 0,
  `verified_until` DATETIME DEFAULT NULL,
  `wallet_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `is_monetized` TINYINT(1) DEFAULT 0,
  `followers_count` INT NOT NULL DEFAULT 0,
  `following_count` INT NOT NULL DEFAULT 0,
  `is_banned` TINYINT(1) DEFAULT 0,
  `is_online` TINYINT(1) DEFAULT 1,
  `auth_token` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_email` (`email`),
  UNIQUE KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.1 Follows Table (Followers and Following)
CREATE TABLE IF NOT EXISTS `follows` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `follower_id` INT NOT NULL,
  `following_id` INT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_follow_unique` (`follower_id`, `following_id`),
  FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Cloudflare R2 Buckets Management Table
CREATE TABLE IF NOT EXISTS `r2_buckets` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `account_label` VARCHAR(150) NOT NULL,
  `bucket_name` VARCHAR(150) NOT NULL,
  `account_id` VARCHAR(150) NOT NULL,
  `access_key_id` VARCHAR(150) NOT NULL,
  `secret_access_key` VARCHAR(255) NOT NULL,
  `public_url` VARCHAR(255) NOT NULL,
  `account_limit_gb` DECIMAL(10, 2) NOT NULL DEFAULT 10.00,
  `used_bytes` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `priority` INT NOT NULL DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Posts Table
CREATE TABLE IF NOT EXISTS `posts` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `page_id` INT DEFAULT NULL,
  `content` TEXT DEFAULT NULL,
  `image_url` VARCHAR(500) DEFAULT NULL,
  `video_url` VARCHAR(500) DEFAULT NULL,
  `privacy` VARCHAR(30) DEFAULT 'Public',
  `bg_color` VARCHAR(50) DEFAULT NULL,
  `bucket_id` INT DEFAULT NULL,
  `likes_count` INT DEFAULT 0,
  `comments_count` INT DEFAULT 0,
  `shares_count` INT DEFAULT 0,
  `views_count` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX (`page_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Post Likes Table
CREATE TABLE IF NOT EXISTS `post_likes` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `post_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_post_user_like` (`post_id`, `user_id`),
  FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Post Reactions Table (Emoji reactions like Like, Love, Haha, Wow, Sad, Angry)
CREATE TABLE IF NOT EXISTS `post_reactions` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `post_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `emoji` VARCHAR(50) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_post_user_reaction` (`post_id`, `user_id`),
  FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Comments Table
CREATE TABLE IF NOT EXISTS `comments` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `post_id` INT DEFAULT NULL,
  `reel_id` INT DEFAULT NULL,
  `parent_id` INT DEFAULT NULL,
  `user_id` INT NOT NULL,
  `text` TEXT NOT NULL,
  `reaction_emoji` VARCHAR(50) DEFAULT NULL,
  `likes_count` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Comment Likes Table
CREATE TABLE IF NOT EXISTS `comment_likes` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `comment_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_comment_user_like` (`comment_id`, `user_id`),
  FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Stories Table
CREATE TABLE IF NOT EXISTS `stories` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `story_image` VARCHAR(500) DEFAULT NULL,
  `text_content` TEXT DEFAULT NULL,
  `background_color` VARCHAR(50) DEFAULT NULL,
  `bucket_id` INT DEFAULT NULL,
  `expires_at` DATETIME NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Reels Table
CREATE TABLE IF NOT EXISTS `reels` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `media_url` VARCHAR(500) NOT NULL,
  `thumbnail_url` VARCHAR(500) DEFAULT NULL,
  `caption` TEXT DEFAULT NULL,
  `audio_title` VARCHAR(255) DEFAULT 'Original Audio',
  `bucket_id` INT DEFAULT NULL,
  `likes_count` INT DEFAULT 0,
  `comments_count` INT DEFAULT 0,
  `shares_count` INT DEFAULT 0,
  `views_count` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Reel Likes Table
CREATE TABLE IF NOT EXISTS `reel_likes` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `reel_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `reaction` VARCHAR(50) DEFAULT 'Like',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_reel_user_like` (`reel_id`, `user_id`),
  FOREIGN KEY (`reel_id`) REFERENCES `reels` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Conversations Table (Direct message threads between users)
CREATE TABLE IF NOT EXISTS `conversations` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_one_id` INT NOT NULL,
  `user_two_id` INT NOT NULL,
  `last_message` TEXT DEFAULT NULL,
  `last_message_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_conversation_users` (`user_one_id`, `user_two_id`),
  FOREIGN KEY (`user_one_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_two_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Messages Table
CREATE TABLE IF NOT EXISTS `messages` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `conversation_id` INT NOT NULL,
  `sender_id` INT NOT NULL,
  `receiver_id` INT NOT NULL,
  `reply_to_id` INT DEFAULT NULL,
  `text` TEXT DEFAULT NULL,
  `image_url` VARCHAR(500) DEFAULT NULL,
  `video_url` VARCHAR(500) DEFAULT NULL,
  `bucket_id` INT DEFAULT NULL,
  `is_read` TINYINT(1) DEFAULT 0,
  `is_edited` TINYINT(1) DEFAULT 0,
  `is_call_notice` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Notifications Table
CREATE TABLE IF NOT EXISTS `notifications` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `from_user_id` INT DEFAULT NULL,
  `title` VARCHAR(255) NOT NULL,
  `message` TEXT NOT NULL,
  `type` VARCHAR(50) DEFAULT 'general',
  `target_id` INT DEFAULT NULL,
  `is_read` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Story Settings Table (Customizable expiration in hours)
CREATE TABLE IF NOT EXISTS `story_settings` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `expiry_hours` INT NOT NULL DEFAULT 24,
  `description` VARCHAR(255) DEFAULT 'Story expiration duration in hours (e.g. 24 for 1 day, 12, 48, etc.)',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `story_settings` (`id`, `expiry_hours`, `description`) 
VALUES (1, 24, 'Story auto-deletion duration in hours. Change this value to adjust story lifetime for all stories.')
ON DUPLICATE KEY UPDATE `id` = `id`;

-- 15. Sample Default User
INSERT INTO `users` (`id`, `full_name`, `email`, `phone`, `password_hash`, `bio`, `occupation`, `location`, `is_verified`, `is_online`)
VALUES (1, 'Maruf Hossain', 'maruf@example.com', '01700000000', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Creator & Developer of Flikata.', 'Software Engineer', 'Dhaka, Bangladesh', 1, 1)
ON DUPLICATE KEY UPDATE `id` = `id`;

-- 16. Reel Settings Table (Customizable maximum video duration in seconds)
CREATE TABLE IF NOT EXISTS `reel_settings` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `max_duration_seconds` INT NOT NULL DEFAULT 60,
  `description` VARCHAR(255) DEFAULT 'Maximum allowed video duration for reels in seconds (default 60s). Change this to any limit.',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `reel_settings` (`id`, `max_duration_seconds`, `description`)
VALUES (1, 60, 'Maximum allowed duration in seconds for reels/videos. Change this value to adjust the duration limit.')
ON DUPLICATE KEY UPDATE `id` = `id`;

-- 17. Admins Table
CREATE TABLE IF NOT EXISTS `admins` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(150) NOT NULL DEFAULT 'Super Admin',
  `email` VARCHAR(150) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `admins` (`id`, `name`, `email`, `password_hash`)
VALUES (1, 'Super Admin', 'admin@gmail.com', '11223344')
ON DUPLICATE KEY UPDATE `email` = 'admin@gmail.com';

-- 18. Calls Table (Audio Call Signalling & Records)
CREATE TABLE IF NOT EXISTS `calls` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `caller_id` INT NOT NULL,
  `receiver_id` INT NOT NULL,
  `channel_name` VARCHAR(100) NOT NULL,
  `status` ENUM('ringing', 'connected', 'declined', 'ended', 'missed') DEFAULT 'ringing',
  `duration_seconds` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX (`caller_id`),
  INDEX (`receiver_id`),
  INDEX (`status`),
  FOREIGN KEY (`caller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. Verification Packages Table
CREATE TABLE IF NOT EXISTS `verification_packages` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `title` VARCHAR(150) NOT NULL,
  `description` TEXT DEFAULT NULL,
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `duration_type` ENUM('day', 'month', 'year', 'lifetime') DEFAULT 'month',
  `duration_value` INT NOT NULL DEFAULT 1,
  `is_active` TINYINT(1) DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. Verification Settings Table
CREATE TABLE IF NOT EXISTS `verification_settings` (
  `id` INT PRIMARY KEY DEFAULT 1,
  `id_card_required` TINYINT(1) DEFAULT 0,
  `selfie_required` TINYINT(1) DEFAULT 0,
  `grace_period_days` INT DEFAULT 3,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `verification_settings` (`id`, `id_card_required`, `selfie_required`, `grace_period_days`)
VALUES (1, 0, 0, 3)
ON DUPLICATE KEY UPDATE `id` = 1;

-- 21. Verification Requests Table
CREATE TABLE IF NOT EXISTS `verification_requests` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `package_id` INT NOT NULL,
  `package_title` VARCHAR(150) DEFAULT NULL,
  `amount` DECIMAL(10,2) DEFAULT 0.00,
  `id_front_url` VARCHAR(500) DEFAULT NULL,
  `id_back_url` VARCHAR(500) DEFAULT NULL,
  `selfie_url` VARCHAR(500) DEFAULT NULL,
  `status` ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
  `rejection_reason` TEXT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX (`user_id`),
  INDEX (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 22. Deposit Payment Methods Table
CREATE TABLE IF NOT EXISTS `deposit_methods` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `number` VARCHAR(100) NOT NULL,
  `image_url` VARCHAR(500) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `is_active` TINYINT(1) DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `deposit_methods` (`id`, `name`, `number`, `image_url`, `description`)
VALUES 
  (1, 'bKash Personal', '01700000000', 'https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=150', 'Send money to personal number, copy transaction ID and submit.'),
  (2, 'Nagad Personal', '01800000000', 'https://images.unsplash.com/photo-1563013544-824ae1b704d3?w=150', 'Send money to personal number, copy transaction ID and submit.'),
  (3, 'Rocket', '019000000009', 'https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=150', 'Send money to 12 digit personal number and enter transaction ID.')
ON DUPLICATE KEY UPDATE `id` = `id`;

-- 23. Deposit Requests Table
CREATE TABLE IF NOT EXISTS `deposit_requests` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `method_id` INT NOT NULL,
  `method_name` VARCHAR(100) NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `transaction_id` VARCHAR(150) NOT NULL,
  `status` ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
  `rejection_reason` TEXT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX (`user_id`),
  INDEX (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 24. Wallet Transactions Table
CREATE TABLE IF NOT EXISTS `wallet_transactions` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `description` VARCHAR(255) NOT NULL,
  `status` VARCHAR(50) DEFAULT 'completed',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 25. Withdraw Payment Methods Table
CREATE TABLE IF NOT EXISTS `withdraw_methods` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `number` VARCHAR(100) NOT NULL,
  `image_url` VARCHAR(500) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `min_amount` DECIMAL(10,2) DEFAULT 50.00,
  `max_amount` DECIMAL(10,2) DEFAULT 25000.00,
  `is_active` TINYINT(1) DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `withdraw_methods` (`id`, `name`, `number`, `image_url`, `description`, `min_amount`, `max_amount`)
VALUES 
  (1, 'bKash Personal', '01700000000', '', 'Withdraw to personal bKash number. Minimum ৳ 50, Maximum ৳ 25,000.', 50.00, 25000.00),
  (2, 'Nagad Personal', '01800000000', '', 'Withdraw to personal Nagad number. Minimum ৳ 50, Maximum ৳ 25,000.', 50.00, 25000.00),
  (3, 'Rocket Personal', '01900000000', '', 'Withdraw to 12 digit Rocket number. Minimum ৳ 50, Maximum ৳ 25,000.', 50.00, 25000.00),
  (4, 'Bank Transfer', 'Bank Info', '', 'Provide your Bank Name, Branch, Account Holder Name, and Account Number.', 500.00, 50000.00)
ON DUPLICATE KEY UPDATE `id` = `id`;

-- 26. Withdraw Requests Table
CREATE TABLE IF NOT EXISTS `withdraw_requests` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `method_id` INT NOT NULL,
  `method_name` VARCHAR(100) NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `account_name` VARCHAR(150) NOT NULL,
  `account_number` VARCHAR(100) NOT NULL,
  `status` ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
  `rejection_reason` TEXT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX (`user_id`),
  INDEX (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 27. Reel Views Table (Unique views, prevent self-views)
CREATE TABLE IF NOT EXISTS `reel_views` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `reel_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_reel_user_view` (`reel_id`, `user_id`),
  INDEX (`reel_id`),
  INDEX (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 28. Post Views Table (Unique views, prevent self-views)
CREATE TABLE IF NOT EXISTS `post_views` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `post_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_post_user_view` (`post_id`, `user_id`),
  INDEX (`post_id`),
  INDEX (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 29. Monetization Settings Table
CREATE TABLE IF NOT EXISTS `monetization_settings` (
  `id` INT PRIMARY KEY DEFAULT 1,
  `min_followers` INT DEFAULT 1000,
  `min_posts` INT DEFAULT 10,
  `min_reels` INT DEFAULT 5,
  `min_account_age_days` INT DEFAULT 30,
  `dashboard_posts_limit` INT DEFAULT 5,
  `revenue_share_percent` INT DEFAULT 55,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `monetization_settings` (`id`, `min_followers`, `min_posts`, `min_reels`, `min_account_age_days`, `dashboard_posts_limit`, `revenue_share_percent`) 
VALUES (1, 1000, 10, 5, 30, 5, 55);

-- 30. Monetization Applications Table
CREATE TABLE IF NOT EXISTS `monetization_requests` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `followers_count` INT DEFAULT 0,
  `posts_count` INT DEFAULT 0,
  `reels_count` INT DEFAULT 0,
  `account_age_days` INT DEFAULT 0,
  `status` ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
  `rejection_reason` TEXT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX (`user_id`),
  INDEX (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 31. Marketplace Products Table
CREATE TABLE IF NOT EXISTS `marketplace_products` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `currency` VARCHAR(10) NOT NULL DEFAULT 'BDT',
  `category` VARCHAR(100) NOT NULL DEFAULT 'General',
  `condition_state` VARCHAR(50) NOT NULL DEFAULT 'New',
  `location` VARCHAR(150) NOT NULL DEFAULT 'Dhaka, Bangladesh',
  `description` TEXT DEFAULT NULL,
  `image_url` TEXT DEFAULT NULL,
  `is_sold` TINYINT(1) DEFAULT 0,
  `views_count` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX (`user_id`),
  INDEX (`category`),
  INDEX (`is_sold`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 32. Pages Table
CREATE TABLE IF NOT EXISTS `pages` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `name` VARCHAR(150) NOT NULL,
  `category` VARCHAR(100) DEFAULT 'Digital Creator',
  `bio` TEXT DEFAULT NULL,
  `logo_url` TEXT DEFAULT NULL,
  `banner_url` TEXT DEFAULT NULL,
  `email` VARCHAR(150) DEFAULT NULL,
  `phone` VARCHAR(50) DEFAULT NULL,
  `website` VARCHAR(255) DEFAULT NULL,
  `followers_count` INT DEFAULT 1,
  `likes_count` INT DEFAULT 1,
  `is_verified` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX (`user_id`),
  INDEX (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 33. Page Settings Table
CREATE TABLE IF NOT EXISTS `page_settings` (
  `id` INT PRIMARY KEY DEFAULT 1,
  `max_pages_per_user` INT NOT NULL DEFAULT 5,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `page_settings` (`id`, `max_pages_per_user`)
VALUES (1, 5)
ON DUPLICATE KEY UPDATE `id` = 1;

-- 34. Leaderboard Settings Table
CREATE TABLE IF NOT EXISTS `leaderboard_settings` (
  `id` INT PRIMARY KEY DEFAULT 1,
  `user_limit` INT NOT NULL DEFAULT 20,
  `description` VARCHAR(255) DEFAULT 'Max users displayed on Leaderboard',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 35. App Feature Settings & Daily Limits Table
CREATE TABLE IF NOT EXISTS `app_feature_settings` (
  `id` INT PRIMARY KEY DEFAULT 1,
  `allow_page_creation` TINYINT(1) NOT NULL DEFAULT 1,
  `allow_login` TINYINT(1) NOT NULL DEFAULT 1,
  `allow_registration` TINYINT(1) NOT NULL DEFAULT 1,
  `allow_chat` TINYINT(1) NOT NULL DEFAULT 1,
  `allow_reels` TINYINT(1) NOT NULL DEFAULT 1,
  `feed_show_text_posts` TINYINT(1) NOT NULL DEFAULT 1,
  `feed_show_image_posts` TINYINT(1) NOT NULL DEFAULT 1,
  `feed_show_video_posts` TINYINT(1) NOT NULL DEFAULT 1,
  `daily_text_limit` INT NOT NULL DEFAULT 30,
  `daily_image_limit` INT NOT NULL DEFAULT 20,
  `daily_video_limit` INT NOT NULL DEFAULT 10,
  `daily_story_limit` INT NOT NULL DEFAULT 15,
  `default_page_create_limit` INT NOT NULL DEFAULT 5,
  `users_per_page` INT NOT NULL DEFAULT 20,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `app_feature_settings` (`id`, `allow_page_creation`, `allow_login`, `allow_registration`, `allow_chat`, `allow_reels`, `feed_show_text_posts`, `feed_show_image_posts`, `feed_show_video_posts`, `daily_text_limit`, `daily_image_limit`, `daily_video_limit`, `daily_story_limit`, `default_page_create_limit`, `users_per_page`)
VALUES (1, 1, 1, 1, 1, 1, 1, 1, 1, 30, 20, 10, 15, 5, 20)
ON DUPLICATE KEY UPDATE `id` = 1;

SET FOREIGN_KEY_CHECKS = 1;


