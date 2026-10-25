<?php
/**
 * Get App Feature Settings (Page Creation, Login, Registration, Chat)
 * Flikata API
 */
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

try {
    $pdo->exec("CREATE TABLE IF NOT EXISTS `app_feature_settings` (
      `id` INT PRIMARY KEY DEFAULT 1,
      `allow_page_creation` TINYINT(1) NOT NULL DEFAULT 1,
      `allow_login` TINYINT(1) NOT NULL DEFAULT 1,
      `allow_registration` TINYINT(1) NOT NULL DEFAULT 1,
      `allow_chat` TINYINT(1) NOT NULL DEFAULT 1,
      `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

    $stmt = $pdo->query("SELECT * FROM app_feature_settings WHERE id = 1 LIMIT 1");
    $row = $stmt ? $stmt->fetch(PDO::FETCH_ASSOC) : null;
    
    if (!$row) {
        $pdo->exec("INSERT INTO app_feature_settings (id, allow_page_creation, allow_login, allow_registration, allow_chat) VALUES (1, 1, 1, 1, 1)");
        $row = [
            'allow_page_creation' => 1,
            'allow_login'         => 1,
            'allow_registration'  => 1,
            'allow_chat'          => 1
        ];
    }

    echo json_encode([
        'success'             => true,
        'allow_page_creation' => (bool)$row['allow_page_creation'],
        'allow_login'         => (bool)$row['allow_login'],
        'allow_registration'  => (bool)$row['allow_registration'],
        'allow_chat'          => (bool)$row['allow_chat']
    ], JSON_UNESCAPED_UNICODE);
} catch (Exception $e) {
    echo json_encode([
        'success'             => true,
        'allow_page_creation' => true,
        'allow_login'         => true,
        'allow_registration'  => true,
        'allow_chat'          => true
    ], JSON_UNESCAPED_UNICODE);
}
exit;
