<?php
/**
 * Leaderboard Settings Endpoint
 * Flikata API
 */

header('Content-Type: application/json; charset=UTF-8');
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;
    $userLimit = isset($input['user_limit']) ? (int)$input['user_limit'] : (isset($input['limit']) ? (int)$input['limit'] : null);

    if ($userLimit !== null && $userLimit > 0) {
        $stmt = $pdo->prepare("INSERT INTO `leaderboard_settings` (`id`, `user_limit`) VALUES (1, ?) ON DUPLICATE KEY UPDATE `user_limit` = ?");
        $stmt->execute([$userLimit, $userLimit]);
        echo json_encode([
            'success' => true,
            'user_limit' => $userLimit,
            'message' => 'Leaderboard limit setting updated successfully.'
        ]);
        exit;
    } else {
        echo json_encode([
            'success' => false,
            'user_limit' => 20,
            'message' => 'Invalid user_limit value.'
        ]);
        exit;
    }
}

// Default GET: Return current leaderboard settings
$userLimit = 20;
try {
    $stmt = $pdo->query("SELECT `user_limit` FROM `leaderboard_settings` WHERE `id` = 1 LIMIT 1");
    $row = $stmt ? $stmt->fetch() : null;
    if ($row && isset($row['user_limit'])) {
        $userLimit = (int)$row['user_limit'];
    }
} catch (Exception $e) {
    $userLimit = 20;
}

echo json_encode([
    'success' => true,
    'user_limit' => $userLimit,
    'message' => 'Leaderboard settings loaded successfully.'
]);
