<?php
/**
 * Auto Schema Migration for Verification & Wallet System
 */

function ensureVerificationAndWalletSchema($pdo) {
    static $migrated = false;
    if ($migrated) return;

    try {
        // Users columns
        $pdo->exec("ALTER TABLE `users` ADD COLUMN `wallet_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00");
    } catch (Exception $e) {}

    try {
        $pdo->exec("ALTER TABLE `users` ADD COLUMN `verified_until` DATETIME DEFAULT NULL");
    } catch (Exception $e) {}

    try {
        $pdo->exec("CREATE TABLE IF NOT EXISTS `verification_packages` (
          `id` INT AUTO_INCREMENT PRIMARY KEY,
          `title` VARCHAR(150) NOT NULL,
          `description` TEXT DEFAULT NULL,
          `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
          `duration_type` ENUM('day', 'month', 'year', 'lifetime') DEFAULT 'month',
          `duration_value` INT NOT NULL DEFAULT 1,
          `is_active` TINYINT(1) DEFAULT 1,
          `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
          `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

        $pdo->exec("CREATE TABLE IF NOT EXISTS `verification_settings` (
          `id` INT PRIMARY KEY DEFAULT 1,
          `id_card_required` TINYINT(1) DEFAULT 0,
          `selfie_required` TINYINT(1) DEFAULT 0,
          `grace_period_days` INT DEFAULT 3,
          `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

        $pdo->exec("INSERT INTO `verification_settings` (`id`, `id_card_required`, `selfie_required`, `grace_period_days`)
          VALUES (1, 0, 0, 3)
          ON DUPLICATE KEY UPDATE `id` = 1;");

        $pdo->exec("CREATE TABLE IF NOT EXISTS `verification_requests` (
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
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

        $pdo->exec("CREATE TABLE IF NOT EXISTS `deposit_methods` (
          `id` INT AUTO_INCREMENT PRIMARY KEY,
          `name` VARCHAR(100) NOT NULL,
          `number` VARCHAR(100) NOT NULL,
          `image_url` VARCHAR(500) DEFAULT NULL,
          `description` TEXT DEFAULT NULL,
          `is_active` TINYINT(1) DEFAULT 1,
          `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
          `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

        $pdo->exec("CREATE TABLE IF NOT EXISTS `deposit_requests` (
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
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

        $pdo->exec("CREATE TABLE IF NOT EXISTS `wallet_transactions` (
          `id` INT AUTO_INCREMENT PRIMARY KEY,
          `user_id` INT NOT NULL,
          `type` VARCHAR(50) NOT NULL,
          `amount` DECIMAL(10,2) NOT NULL,
          `description` VARCHAR(255) NOT NULL,
          `status` VARCHAR(50) DEFAULT 'completed',
          `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
          INDEX (`user_id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

        $pdo->exec("CREATE TABLE IF NOT EXISTS `withdraw_methods` (
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
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

        $pdo->exec("CREATE TABLE IF NOT EXISTS `withdraw_requests` (
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
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

        // Seed sample default payment methods if empty
        $countMethods = (int)$pdo->query("SELECT COUNT(*) FROM `deposit_methods`")->fetchColumn();
        if ($countMethods === 0) {
            $pdo->exec("INSERT INTO `deposit_methods` (`name`, `number`, `image_url`, `description`) VALUES
              ('bKash Personal', '01712345678', '', 'Send Money to this personal bKash number. Copy the TrxID and submit below.'),
              ('Nagad Personal', '01812345678', '', 'Send Money to this personal Nagad number. Copy the TrxID and submit below.'),
              ('Rocket Personal', '01912345678', '', 'Send Money to this personal Rocket number. Copy the TrxID and submit below.');");
        }

        // Seed sample default withdraw methods if empty
        $countWithdrawMethods = (int)$pdo->query("SELECT COUNT(*) FROM `withdraw_methods`")->fetchColumn();
        if ($countWithdrawMethods === 0) {
            $pdo->exec("INSERT INTO `withdraw_methods` (`name`, `number`, `image_url`, `description`, `min_amount`, `max_amount`) VALUES
              ('bKash Personal', '01700000000', '', 'Withdraw to personal bKash number. Minimum ৳ 50, Maximum ৳ 25,000.', 50.00, 25000.00),
              ('Nagad Personal', '01800000000', '', 'Withdraw to personal Nagad number. Minimum ৳ 50, Maximum ৳ 25,000.', 50.00, 25000.00),
              ('Rocket Personal', '01900000000', '', 'Withdraw to 12 digit Rocket number. Minimum ৳ 50, Maximum ৳ 25,000.', 50.00, 25000.00),
              ('Bank Transfer', 'Bank Info', '', 'Provide your Bank Name, Branch, Account Holder Name, and Account Number.', 500.00, 50000.00);");
        }
        // Users columns
        try { $pdo->exec("ALTER TABLE `users` ADD COLUMN `is_monetized` TINYINT(1) DEFAULT 0"); } catch (Exception $e) {}
        try { $pdo->exec("ALTER TABLE `users` ADD COLUMN `followers_count` INT NOT NULL DEFAULT 0"); } catch (Exception $e) {}
        try { $pdo->exec("ALTER TABLE `users` ADD COLUMN `following_count` INT NOT NULL DEFAULT 0"); } catch (Exception $e) {}
        try { $pdo->exec("ALTER TABLE `users` ADD COLUMN `is_verified` TINYINT(1) DEFAULT 0"); } catch (Exception $e) {}
        try { $pdo->exec("ALTER TABLE `users` ADD COLUMN `is_banned` TINYINT(1) DEFAULT 0"); } catch (Exception $e) {}

        // Posts & Reels views column
        try { $pdo->exec("ALTER TABLE `posts` ADD COLUMN `views_count` INT NOT NULL DEFAULT 0"); } catch (Exception $e) {}
        try { $pdo->exec("ALTER TABLE `reels` ADD COLUMN `views_count` INT NOT NULL DEFAULT 0"); } catch (Exception $e) {}

        // Follows Table
        try {
            $pdo->exec("CREATE TABLE IF NOT EXISTS `follows` (
              `id` INT AUTO_INCREMENT PRIMARY KEY,
              `follower_id` INT NOT NULL,
              `following_id` INT NOT NULL,
              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
              UNIQUE KEY `idx_follow_unique` (`follower_id`, `following_id`),
              FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
              FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        } catch (Exception $e) {}

        // Real Reel Views Table (Unique per user, no self-views)
        try {
            $pdo->exec("CREATE TABLE IF NOT EXISTS `reel_views` (
              `id` INT AUTO_INCREMENT PRIMARY KEY,
              `reel_id` INT NOT NULL,
              `user_id` INT NOT NULL,
              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
              UNIQUE KEY `idx_reel_user_view` (`reel_id`, `user_id`),
              INDEX (`reel_id`),
              INDEX (`user_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        } catch (Exception $e) {}

        // Real Post Views Table (Unique per user, no self-views)
        try {
            $pdo->exec("CREATE TABLE IF NOT EXISTS `post_views` (
              `id` INT AUTO_INCREMENT PRIMARY KEY,
              `post_id` INT NOT NULL,
              `user_id` INT NOT NULL,
              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
              UNIQUE KEY `idx_post_user_view` (`post_id`, `user_id`),
              INDEX (`post_id`),
              INDEX (`user_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        } catch (Exception $e) {}

        // Monetization Settings Table
        try {
            $pdo->exec("CREATE TABLE IF NOT EXISTS `monetization_settings` (
              `id` INT PRIMARY KEY DEFAULT 1,
              `min_followers` INT DEFAULT 1000,
              `min_posts` INT DEFAULT 10,
              `min_reels` INT DEFAULT 5,
              `min_account_age_days` INT DEFAULT 30,
              `dashboard_posts_limit` INT DEFAULT 5,
              `revenue_share_percent` INT DEFAULT 55,
              `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            $pdo->exec("INSERT INTO `monetization_settings` (`id`, `min_followers`, `min_posts`, `min_reels`, `min_account_age_days`, `dashboard_posts_limit`, `revenue_share_percent`) 
              VALUES (1, 1000, 10, 5, 30, 5, 55)
              ON DUPLICATE KEY UPDATE `id` = `id`;");
        } catch (Exception $e) {}

        // Monetization Requests Table
        try {
            $pdo->exec("CREATE TABLE IF NOT EXISTS `monetization_requests` (
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        } catch (Exception $e) {}

        // Marketplace Products Table
        try {
            $pdo->exec("CREATE TABLE IF NOT EXISTS `marketplace_products` (
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        } catch (Exception $e) {}

        // Pages Table
        try {
            $pdo->exec("CREATE TABLE IF NOT EXISTS `pages` (
              `id` INT AUTO_INCREMENT PRIMARY KEY,
              `user_id` INT NOT NULL,
              `name` VARCHAR(255) NOT NULL,
              `category` VARCHAR(100) NOT NULL,
              `bio` TEXT DEFAULT NULL,
              `logo_url` TEXT DEFAULT NULL,
              `banner_url` TEXT DEFAULT NULL,
              `email` VARCHAR(255) DEFAULT NULL,
              `phone` VARCHAR(50) DEFAULT NULL,
              `website` VARCHAR(255) DEFAULT NULL,
              `followers_count` INT DEFAULT 0,
              `likes_count` INT DEFAULT 0,
              `is_verified` TINYINT(1) DEFAULT 0,
              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
              `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              INDEX (`user_id`),
              INDEX (`name`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        } catch (Exception $e) {}

        // Page Settings Table (Configurable max pages allowed per profile)
        try {
            $pdo->exec("CREATE TABLE IF NOT EXISTS `page_settings` (
              `id` INT PRIMARY KEY DEFAULT 1,
              `max_pages_per_user` INT DEFAULT 5,
              `description` VARCHAR(255) DEFAULT 'Max pages allowed per user profile',
              `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            $pdo->exec("INSERT INTO `page_settings` (`id`, `max_pages_per_user`, `description`)
              VALUES (1, 5, 'Max pages allowed per user profile')
              ON DUPLICATE KEY UPDATE `id` = 1;");
        } catch (Exception $e) {}

        // Ensure posts table has page_id column
        try {
            $pdo->exec("ALTER TABLE `posts` ADD COLUMN `page_id` INT DEFAULT NULL");
        } catch (Exception $e) {}

        // Leaderboard Settings Table (Configurable user limit for Leaderboard)
        try {
            $pdo->exec("CREATE TABLE IF NOT EXISTS `leaderboard_settings` (
              `id` INT PRIMARY KEY DEFAULT 1,
              `user_limit` INT DEFAULT 20,
              `description` VARCHAR(255) DEFAULT 'Max users displayed on Leaderboard',
              `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            $pdo->exec("INSERT INTO `leaderboard_settings` (`id`, `user_limit`, `description`)
              VALUES (1, 20, 'Max users displayed on Leaderboard')
              ON DUPLICATE KEY UPDATE `id` = 1;");
        } catch (Exception $e) {}

        // App Updates Table
        try {
            $pdo->exec("CREATE TABLE IF NOT EXISTS `app_updates` (
              `id` INT AUTO_INCREMENT PRIMARY KEY,
              `version_code` INT NOT NULL,
              `version_name` VARCHAR(50) NOT NULL,
              `apk_url` VARCHAR(1000) NOT NULL,
              `apk_size_mb` VARCHAR(50) DEFAULT '30 MB',
              `release_notes` TEXT DEFAULT NULL,
              `is_force_update` TINYINT(1) DEFAULT 1,
              `is_active` TINYINT(1) DEFAULT 1,
              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
              `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              INDEX (`version_code`),
              INDEX (`is_active`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // Seed default current version if table empty
            $cnt = (int)$pdo->query("SELECT COUNT(*) FROM `app_updates`")->fetchColumn();
            if ($cnt === 0) {
                $pdo->prepare("INSERT INTO `app_updates` (`version_code`, `version_name`, `apk_url`, `apk_size_mb`, `release_notes`, `is_force_update`, `is_active`)
                    VALUES (13, '6.0', '', '30 MB', 'Welcome to Flikata v6.0! Enjoy pure follow/unfollow, live views tracking, and persistent profile settings.', 0, 1)
                ")->execute();
            }
        } catch (Exception $e) {}
    } catch (Exception $e) {}

    $migrated = true;
}
