<?php
/**
 * Record View on Reel Endpoint
 * Flikata API - Real & Verified Views Only
 * Rules:
 * 1. Self views (author viewing own video) are NEVER counted.
 * 2. Only 1 unique view per user per video is counted.
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$rawReelId = trim((string)($input['reel_id'] ?? ($_GET['reel_id'] ?? '')));
$userId = (int)($input['user_id'] ?? ($_GET['user_id'] ?? 0));

if (strpos($rawReelId, 'post_') === 0) {
    $postId = (int)substr($rawReelId, 5);
    if ($postId > 0) {
        $stmt = $pdo->prepare("SELECT user_id, views_count FROM posts WHERE id = ?");
        $stmt->execute([$postId]);
        $post = $stmt->fetch();
        if ($post) {
            $authorId = (int)$post['user_id'];
            $currentViews = (int)$post['views_count'];
            if ($userId > 0 && $userId === $authorId) {
                jsonResponse(true, ['views_count' => $currentViews, 'counted' => false, 'reason' => 'self_view_excluded'], 'Self views are not counted.');
            }
            if ($userId > 0) {
                $checkStmt = $pdo->prepare("SELECT id FROM post_views WHERE post_id = ? AND user_id = ? LIMIT 1");
                $checkStmt->execute([$postId, $userId]);
                if ($checkStmt->fetchColumn()) {
                    jsonResponse(true, ['views_count' => $currentViews, 'counted' => false, 'reason' => 'already_viewed'], 'View already counted for this user.');
                }
                try {
                    $insert = $pdo->prepare("INSERT INTO post_views (post_id, user_id, created_at) VALUES (?, ?, NOW())");
                    $insert->execute([$postId, $userId]);
                    $cntStmt = $pdo->prepare("SELECT COUNT(DISTINCT user_id) FROM post_views WHERE post_id = ?");
                    $cntStmt->execute([$postId]);
                    $realViews = (int)$cntStmt->fetchColumn();
                    $updateStmt = $pdo->prepare("UPDATE posts SET views_count = ? WHERE id = ?");
                    $updateStmt->execute([$realViews, $postId]);
                    jsonResponse(true, ['views_count' => $realViews, 'counted' => true], 'Unique post view recorded.');
                } catch (Exception $e) {
                    jsonResponse(true, ['views_count' => $currentViews, 'counted' => false], 'Duplicate view ignored.');
                }
            }
            jsonResponse(true, ['views_count' => $currentViews, 'counted' => false], 'User ID required.');
        }
    }
}

$reelId = (int)$rawReelId;

if (!$reelId) {
    jsonResponse(false, null, 'Invalid reel ID.');
}

// 1. Fetch the reel author
$stmt = $pdo->prepare("SELECT user_id, views_count FROM reels WHERE id = ?");
$stmt->execute([$reelId]);
$reel = $stmt->fetch();

if (!$reel) {
    jsonResponse(false, null, 'Reel not found.');
}

$authorId = (int)$reel['user_id'];
$currentViews = (int)$reel['views_count'];

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
    $checkStmt = $pdo->prepare("SELECT id FROM reel_views WHERE reel_id = ? AND user_id = ? LIMIT 1");
    $checkStmt->execute([$reelId, $userId]);
    $alreadyViewed = $checkStmt->fetchColumn();

    if ($alreadyViewed) {
        jsonResponse(true, [
            'views_count' => $currentViews,
            'counted' => false,
            'reason' => 'already_viewed'
        ], 'View already counted for this user.');
    }

    // Insert unique view record
    try {
        $insert = $pdo->prepare("INSERT INTO reel_views (reel_id, user_id, created_at) VALUES (?, ?, NOW())");
        $insert->execute([$reelId, $userId]);

        // Update real view count based on unique viewer count
        $cntStmt = $pdo->prepare("SELECT COUNT(DISTINCT user_id) FROM reel_views WHERE reel_id = ?");
        $cntStmt->execute([$reelId]);
        $realViews = (int)$cntStmt->fetchColumn();

        $updateStmt = $pdo->prepare("UPDATE reels SET views_count = ? WHERE id = ?");
        $updateStmt->execute([$realViews, $reelId]);

        jsonResponse(true, [
            'views_count' => $realViews,
            'counted' => true
        ], 'Unique view recorded.');
    } catch (Exception $e) {
        jsonResponse(true, ['views_count' => $currentViews, 'counted' => false], 'Duplicate view ignored.');
    }
} else {
    // Guest or unknown user without ID - do not inflate fake views
    jsonResponse(true, [
        'views_count' => $currentViews,
        'counted' => false
    ], 'User ID required for verified view.');
}
