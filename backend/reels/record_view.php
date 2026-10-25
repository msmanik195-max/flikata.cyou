<?php
/**
 * Record View on Reel Endpoint
 * Flikata API
 * Increment views_count accurately on each view
 */

header('Content-Type: application/json; charset=UTF-8');
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$rawReelId = trim((string)($input['reel_id'] ?? ($_GET['reel_id'] ?? '')));
$userId = (int)($input['user_id'] ?? ($_GET['user_id'] ?? 0));

if (strpos($rawReelId, 'post_') === 0) {
    $postId = (int)substr($rawReelId, 5);
    if ($postId > 0) {
        $updateStmt = $pdo->prepare("UPDATE posts SET views_count = views_count + 1 WHERE id = ?");
        $updateStmt->execute([$postId]);

        $cntStmt = $pdo->prepare("SELECT views_count FROM posts WHERE id = ?");
        $cntStmt->execute([$postId]);
        $newViews = (int)$cntStmt->fetchColumn();

        if ($userId > 0) {
            try {
                $ins = $pdo->prepare("INSERT INTO post_views (post_id, user_id, created_at) VALUES (?, ?, NOW())");
                $ins->execute([$postId, $userId]);
            } catch (Exception $e) {}
        }

        echo json_encode([
            'success' => true,
            'message' => 'Post view counted',
            'data' => [
                'views_count' => $newViews,
                'counted' => true
            ]
        ]);
        exit;
    }
}

$reelId = (int)$rawReelId;

if (!$reelId) {
    echo json_encode([
        'success' => false,
        'message' => 'Invalid reel ID'
    ]);
    exit;
}

// Increment real view count on reel
$updateStmt = $pdo->prepare("UPDATE reels SET views_count = views_count + 1 WHERE id = ?");
$updateStmt->execute([$reelId]);

$cntStmt = $pdo->prepare("SELECT views_count FROM reels WHERE id = ?");
$cntStmt->execute([$reelId]);
$newViews = (int)$cntStmt->fetchColumn();

// Optional log
if ($userId > 0) {
    try {
        $ins = $pdo->prepare("INSERT INTO reel_views (reel_id, user_id, created_at) VALUES (?, ?, NOW())");
        $ins->execute([$reelId, $userId]);
    } catch (Exception $e) {}
}

echo json_encode([
    'success' => true,
    'message' => 'Reel view counted successfully',
    'data' => [
        'views_count' => $newViews,
        'counted' => true
    ]
]);
