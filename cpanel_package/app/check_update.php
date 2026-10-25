<?php
/**
 * In-App Software Update Check API Endpoint
 * Flikata API
 */

header('Content-Type: application/json; charset=UTF-8');

// Resilient DB Connection Finder
$pdo = null;
$possibleDbPaths = [
    __DIR__ . '/../config/db.php',
    __DIR__ . '/../backend/config/db.php',
    __DIR__ . '/../../backend/config/db.php',
    __DIR__ . '/../admin/includes/db.php',
    $_SERVER['DOCUMENT_ROOT'] . '/config/db.php',
    $_SERVER['DOCUMENT_ROOT'] . '/backend/config/db.php'
];

foreach ($possibleDbPaths as $p) {
    if (file_exists($p)) {
        require_once $p;
        if (function_exists('getDB')) {
            $pdo = getDB();
            break;
        }
    }
}

if (!$pdo) {
    if (isset($GLOBALS['pdo']) && $GLOBALS['pdo'] instanceof PDO) {
        $pdo = $GLOBALS['pdo'];
    } else {
        $host = defined('DB_HOST') ? DB_HOST : 'localhost';
        $dbname = defined('DB_NAME') ? DB_NAME : 'flikata_bd';
        $user = defined('DB_USER') ? DB_USER : 'flikata_bd';
        $pass = defined('DB_PASS') ? DB_PASS : 'flikata_bd';
        try {
            $pdo = new PDO("mysql:host={$host};dbname={$dbname};charset=utf8mb4", $user, $pass, [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
            ]);
        } catch (Exception $e) {}
    }
}

$currentVersionCode = (int)($_GET['current_version_code'] ?? ($_POST['current_version_code'] ?? 0));
$currentVersionName = trim((string)($_GET['current_version_name'] ?? ($_POST['current_version_name'] ?? '')));

function normalizeVer($ver) {
    $v = preg_replace('/[^0-9\.]/', '', trim((string)$ver));
    return ltrim($v, '.');
}

try {
    if (!$pdo) {
        throw new Exception("Database connection unavailable");
    }

    // Auto-create app_updates table if not exists
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

    // Fetch all active releases ordered by id DESC
    $stmt = $pdo->query("SELECT * FROM `app_updates` WHERE `is_active` = 1 ORDER BY `version_code` DESC, `id` DESC");
    $releases = $stmt ? $stmt->fetchAll(PDO::FETCH_ASSOC) : [];

    if (!empty($releases)) {
        $cleanCurrentName = normalizeVer($currentVersionName);
        $selectedRelease = null;

        // 1. Check if any active release has a higher version_code or higher semantic version_name
        foreach ($releases as $rel) {
            $relCode = (int)($rel['version_code'] ?? 0);
            $relName = normalizeVer($rel['version_name'] ?? '');

            $isCodeHigher = ($currentVersionCode > 0 && $relCode > $currentVersionCode);
            $isNameHigher = (!empty($cleanCurrentName) && !empty($relName) && version_compare($relName, $cleanCurrentName, '>'));

            if ($isCodeHigher || $isNameHigher) {
                $selectedRelease = $rel;
                break;
            }
        }

        // If no specifically higher release matched, fallback to topmost active if forced test or current is 0
        if (!$selectedRelease) {
            $top = $releases[0];
            $topCode = (int)($top['version_code'] ?? 0);
            $topName = normalizeVer($top['version_name'] ?? '');

            if ($currentVersionCode === 0 || 
                $topCode > $currentVersionCode || 
                (!empty($cleanCurrentName) && !empty($topName) && version_compare($topName, $cleanCurrentName, '>'))) {
                $selectedRelease = $top;
            }
        }

        if ($selectedRelease) {
            $apkUrl = trim((string)($selectedRelease['apk_url'] ?? ''));
            if (empty($apkUrl)) {
                $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? "https://" : "http://";
                $host = $_SERVER['HTTP_HOST'] ?? 'localhost';
                $apkUrl = $protocol . $host . '/apk/flikata_latest.apk';
            }

            echo json_encode([
                'success' => true,
                'update_available' => true,
                'is_force' => ((int)$selectedRelease['is_force_update'] === 1),
                'latest_version_code' => (int)$selectedRelease['version_code'],
                'latest_version_name' => (string)($selectedRelease['version_name'] ?? ''),
                'apk_url' => $apkUrl,
                'apk_size_mb' => (string)($selectedRelease['apk_size_mb'] ?? '30 MB'),
                'release_notes' => (string)($selectedRelease['release_notes'] ?? 'General improvements and bug fixes.'),
                'message' => 'New update available'
            ], JSON_UNESCAPED_UNICODE);
            exit;
        }
    }

    echo json_encode([
        'success' => true,
        'update_available' => false,
        'is_force' => false,
        'latest_version_code' => $currentVersionCode ?: 14,
        'latest_version_name' => $currentVersionName ?: '7.0',
        'apk_url' => '',
        'apk_size_mb' => '',
        'release_notes' => '',
        'message' => 'App is up to date'
    ], JSON_UNESCAPED_UNICODE);
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'update_available' => false,
        'message' => 'Error checking for updates: ' . $e->getMessage()
    ]);
}
