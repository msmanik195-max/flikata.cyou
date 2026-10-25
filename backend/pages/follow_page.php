<?php
/**
 * Follow Page Endpoint
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$pageId = (int)($input['page_id'] ?? 0);
if ($pageId <= 0) {
    jsonResponse(false, null, 'Valid page_id is required.');
}

try {
    $stmt = $pdo->prepare("UPDATE `pages` SET followers_count = followers_count + 1 WHERE id = ?");
    $stmt->execute([$pageId]);
    jsonResponse(true, ['page_id' => $pageId], 'Page followed successfully.');
} catch (Exception $e) {
    jsonResponse(false, null, 'Database error: ' . $e->getMessage());
}
