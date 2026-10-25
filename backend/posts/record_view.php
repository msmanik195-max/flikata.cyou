<?php
/**
 * Record View on Post Endpoint
 * Flikata API
 * Increment views_count accurately on each view
 */

header('Content-Type: application/json; charset=UTF-8');
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$postId = (int)($input['post_id'] ?? ($_GET['post_id'] ?? 0));
$userId = (int)($input['user_id'] ?? ($_GET['user_id'] ?? 0));

if (!$postId) {
    echo json_encode([
        'success' => false,
        'message' => 'Invalid post ID'
    ]);
    exit;
}

// Increment real view count on post
$updateStmt = $pdo->prepare("UPDATE posts SET views_count = views_count + 1 WHERE id = ?");
$updateStmt->execute([$postId]);

$cntStmt = $pdo->prepare("SELECT views_count FROM posts WHERE id = ?");
$cntStmt->execute([$postId]);
$newViews = (int)$cntStmt->fetchColumn();

// Optional log
if ($userId > 0) {
    try {
        $ins = $pdo->prepare("INSERT INTO post_views (post_id, user_id, created_at) VALUES (?, ?, NOW())");
        $ins->execute([$postId, $userId]);
    } catch (Exception $e) {}
}

echo json_encode([
    'success' => true,
    'message' => 'Post view counted successfully',
    'data' => [
        'views_count' => $newViews,
        'counted' => true
    ]
]);
