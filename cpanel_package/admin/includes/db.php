<?php
/**
 * Admin Database Connection & Auto-Setup
 * Flikata Admin Control Panel
 */

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

// Locate backend config/db.php
$backendDbPath = null;
$possiblePaths = [
    __DIR__ . '/../../backend/config/db.php',
    __DIR__ . '/../backend/config/db.php',
    $_SERVER['DOCUMENT_ROOT'] . '/backend/config/db.php',
    dirname(__DIR__, 2) . '/backend/config/db.php',
    dirname(__DIR__) . '/../backend/config/db.php'
];

foreach ($possiblePaths as $path) {
    if (file_exists($path)) {
        $backendDbPath = $path;
        break;
    }
}

if ($backendDbPath) {
    require_once $backendDbPath;
} else {
    // Fallback direct credentials if backend folder moved
    if (!defined('DB_HOST')) define('DB_HOST', 'localhost');
    if (!defined('DB_NAME')) define('DB_NAME', 'flikata_bd');
    if (!defined('DB_USER')) define('DB_USER', 'flikata_bd');
    if (!defined('DB_PASS')) define('DB_PASS', 'flikata_bd');
    if (!defined('DB_CHARSET')) define('DB_CHARSET', 'utf8mb4');

    function getDB() {
        static $pdo = null;
        if ($pdo === null) {
            $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
            $options = [
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES   => false,
            ];
            try {
                $pdo = new PDO($dsn, DB_USER, DB_PASS, $options);
            } catch (PDOException $e) {
                die("Database connection failed: " . htmlspecialchars($e->getMessage()));
            }
        }
        return $pdo;
    }
}

$pdo = getDB();

// 1. Ensure admins table exists
try {
    $pdo->exec("
        CREATE TABLE IF NOT EXISTS `admins` (
          `id` INT AUTO_INCREMENT PRIMARY KEY,
          `name` VARCHAR(150) NOT NULL DEFAULT 'Super Admin',
          `email` VARCHAR(150) NOT NULL UNIQUE,
          `password_hash` VARCHAR(255) NOT NULL,
          `role` VARCHAR(50) DEFAULT 'admin',
          `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
          `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    ");

    $chkAdmin = $pdo->prepare("SELECT id, password_hash FROM `admins` WHERE email = ? LIMIT 1");
    $chkAdmin->execute(['admin@gmail.com']);
    $existingAdmin = $chkAdmin->fetch();

    if (!$existingAdmin) {
        $defaultPassHash = password_hash('11223344', PASSWORD_BCRYPT);
        $ins = $pdo->prepare("INSERT INTO `admins` (`name`, `email`, `password_hash`, `role`) VALUES (?, ?, ?, ?)");
        $ins->execute(['Super Admin', 'admin@gmail.com', $defaultPassHash, 'admin']);
    }
} catch (Exception $e) {}

// 2. Ensure users table columns exist
try {
    $pdo->exec("ALTER TABLE `users` ADD COLUMN `is_banned` TINYINT(1) DEFAULT 0");
} catch (Exception $e) {}

try {
    $pdo->exec("ALTER TABLE `users` ADD COLUMN `is_verified` TINYINT(1) DEFAULT 0");
} catch (Exception $e) {}

try {
    $pdo->exec("ALTER TABLE `users` ADD COLUMN `wallet_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00");
} catch (Exception $e) {}

try {
    $pdo->exec("ALTER TABLE `users` ADD COLUMN `verified_until` DATETIME DEFAULT NULL");
} catch (Exception $e) {}

// 3. Ensure Verification & Deposit Tables exist
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

    // Real Video/Reel & Post Views (Unique views, prevent self-views)
    $pdo->exec("CREATE TABLE IF NOT EXISTS `reel_views` (
      `id` INT AUTO_INCREMENT PRIMARY KEY,
      `reel_id` INT NOT NULL,
      `user_id` INT NOT NULL,
      `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
      UNIQUE KEY `idx_reel_user_view` (`reel_id`, `user_id`),
      INDEX (`reel_id`),
      INDEX (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

    $pdo->exec("CREATE TABLE IF NOT EXISTS `post_views` (
      `id` INT AUTO_INCREMENT PRIMARY KEY,
      `post_id` INT NOT NULL,
      `user_id` INT NOT NULL,
      `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
      UNIQUE KEY `idx_post_user_view` (`post_id`, `user_id`),
      INDEX (`post_id`),
      INDEX (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

    // Monetization System Settings (Configurable from Admin Settings)
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

    $pdo->exec("INSERT IGNORE INTO `monetization_settings` (`id`, `min_followers`, `min_posts`, `min_reels`, `min_account_age_days`, `dashboard_posts_limit`, `revenue_share_percent`) VALUES (1, 1000, 10, 5, 30, 5, 55)");

    // Monetization Applications / Requests
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

    try { $pdo->exec("ALTER TABLE `users` ADD COLUMN `is_monetized` TINYINT(1) DEFAULT 0"); } catch(Exception $e) {}
    try { $pdo->exec("ALTER TABLE `users` ADD COLUMN `followers_count` INT NOT NULL DEFAULT 0"); } catch(Exception $e) {}
    try { $pdo->exec("ALTER TABLE `users` ADD COLUMN `following_count` INT NOT NULL DEFAULT 0"); } catch(Exception $e) {}
    try { $pdo->exec("ALTER TABLE `posts` ADD COLUMN `views_count` INT DEFAULT 0"); } catch(Exception $e) {}
    try { $pdo->exec("ALTER TABLE `reels` ADD COLUMN `views_count` INT DEFAULT 0"); } catch(Exception $e) {}
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
} catch (Exception $e) {}

require_once __DIR__ . '/lang.php';

// Airtight Zero-Trust Admin Authentication Guard
function checkAdminAuth() {
    if (session_status() === PHP_SESSION_NONE) {
        session_start();
    }
    if (empty($_SESSION['admin_logged_in']) || empty($_SESSION['admin_id'])) {
        if (!empty($_SERVER['HTTP_X_REQUESTED_WITH']) || (isset($_SERVER['CONTENT_TYPE']) && strpos($_SERVER['CONTENT_TYPE'], 'application/json') !== false)) {
            header('Content-Type: application/json; charset=UTF-8');
            http_response_code(401);
            echo json_encode(['success' => false, 'message' => 'Unauthorized: Admin session required']);
            exit;
        }
        header('Location: login.php');
        exit;
    }
}

// Global Execution Guard: Enforce authentication instantly across all admin endpoints
$currentAdminScript = basename($_SERVER['SCRIPT_FILENAME'] ?? ($_SERVER['PHP_SELF'] ?? ''));
if (!in_array($currentAdminScript, ['login.php', 'logout.php', 'db.php', 'check_update.php'])) {
    checkAdminAuth();
}
