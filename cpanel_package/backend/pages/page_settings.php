<?php
/**
 * Page Settings Endpoint (Admin configurable max pages per profile)
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;
    $maxPages = isset($input['max_pages_per_user']) ? (int)$input['max_pages_per_user'] : null;

    if ($maxPages !== null && $maxPages > 0) {
        $stmt = $pdo->prepare("UPDATE `page_settings` SET `max_pages_per_user` = ? WHERE `id` = 1");
        $stmt->execute([$maxPages]);
        jsonResponse(true, ['max_pages_per_user' => $maxPages], 'Page limit setting updated successfully.');
    } else {
        jsonResponse(false, null, 'Invalid max_pages_per_user value.');
    }
}

// Default GET: Return current page settings
$stmt = $pdo->query("SELECT `max_pages_per_user` FROM `page_settings` WHERE `id` = 1 LIMIT 1");
$row = $stmt ? $stmt->fetch() : null;
$maxPages = $row ? (int)$row['max_pages_per_user'] : 5;

jsonResponse(true, [
    'max_pages_per_user' => $maxPages,
    'description' => 'Maximum pages allowed per user profile'
], 'Page settings loaded successfully.');
