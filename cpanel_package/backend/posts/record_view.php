<?php
/**
 * Record View on Post Endpoint
 * Flikata API - Real & Verified Views Only
 * Rules:
 * 1. Self views (author viewing own post) are NEVER counted.
 * 2. Only 1 unique view per user per post is counted.
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$postId = (int)($input['post_id'] ?? ($_GET['post_id'] ?? 0));
$userId = (int)($input['user_id'] ?? ($_GET['user_id'] ?? 0));

if (!$postId) {
    jsonResponse(false, null, 'Invalid post ID.');
}

// 1. Fetch post author
$stmt = $pdo->prepare("SELECT user_id, views_count FROM posts WHERE id = ?");
$stmt->execute([$postId]);
$post = $stmt->fetch();

if (!$post) {
    jsonResponse(false, null, 'Post not found.');
}

$authorId = (int)$post['user_id'];
$currentViews = (int)$post['views_count'];

// 2. Prevent self-views
if ($userId > 0 && $userId === $authorId) {
    jsonResponse(true, [
        'views_count' => $currentViews,
        'counted' => false,
        'reason' => 'self_view_excluded'
    ], 'Self views are not counted.');
}

// 3. Prevent duplicate views from the same user (Strictly 1 view per user)
if ($userId > 0) {
    $checkStmt = $pdo->prepare("SELECT id FROM post_views WHERE post_id = ? AND user_id = ? LIMIT 1");
    $checkStmt->execute([$postId, $userId]);
    $alreadyViewed = $checkStmt->fetchColumn();

    if ($alreadyViewed) {
        jsonResponse(true, [
            'views_count' => $currentViews,
            'counted' => false,
            'reason' => 'already_viewed'
        ], 'View already counted for this user.');
    }

    try {
        $insert = $pdo->prepare("INSERT INTO post_views (post_id, user_id, created_at) VALUES (?, ?, NOW())");
        $insert->execute([$postId, $userId]);

        $cntStmt = $pdo->prepare("SELECT COUNT(DISTINCT user_id) FROM post_views WHERE post_id = ?");
        $cntStmt->execute([$postId]);
        $realViews = (int)$cntStmt->fetchColumn();

        $updateStmt = $pdo->prepare("UPDATE posts SET views_count = ? WHERE id = ?");
        $updateStmt->execute([$realViews, $postId]);

        jsonResponse(true, [
            'views_count' => $realViews,
            'counted' => true
        ], 'Unique post view recorded.');
    } catch (Exception $e) {
        jsonResponse(true, ['views_count' => $currentViews, 'counted' => false], 'Duplicate view ignored.');
    }
} else {
    jsonResponse(true, [
        'views_count' => $currentViews,
        'counted' => false
    ], 'User ID required for verified view.');
}
