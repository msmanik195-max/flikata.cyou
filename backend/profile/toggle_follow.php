<?php
/**
 * Toggle Follow User Endpoint
 * Flikata API - Real Followers Only
 */

header('Content-Type: application/json; charset=UTF-8');
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$auth = getAuthUser($pdo);
$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$followerId = $auth ? (int)$auth['id'] : 0;
if ($followerId <= 0 && !empty($input['my_id'])) {
    $followerId = (int)$input['my_id'];
}
$targetUserId = !empty($input['user_id']) ? (int)$input['user_id'] : (!empty($_GET['user_id']) ? (int)$_GET['user_id'] : 0);

if ($followerId <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Authentication required to follow users.'
    ]);
    exit;
}

if ($targetUserId <= 0 || $targetUserId === $followerId) {
    echo json_encode([
        'success' => false,
        'message' => 'Invalid target user ID.'
    ]);
    exit;
}

try {
    // Check if already following
    $checkStmt = $pdo->prepare("SELECT id FROM follows WHERE follower_id = ? AND following_id = ? LIMIT 1");
    $checkStmt->execute([$followerId, $targetUserId]);
    $followId = $checkStmt->fetchColumn();

    $isFollowing = false;
    if ($followId) {
        // Unfollow
        $del = $pdo->prepare("DELETE FROM follows WHERE id = ?");
        $del->execute([$followId]);
        $isFollowing = false;
    } else {
        // Follow
        $ins = $pdo->prepare("INSERT INTO follows (follower_id, following_id, created_at) VALUES (?, ?, NOW())");
        $ins->execute([$followerId, $targetUserId]);
        $isFollowing = true;
    }

    // Recalculate and sync followers count in DB
    $cntStmt = $pdo->prepare("SELECT COUNT(*) FROM follows WHERE following_id = ?");
    $cntStmt->execute([$targetUserId]);
    $followersCount = (int)$cntStmt->fetchColumn();

    $upd = $pdo->prepare("UPDATE users SET followers_count = ? WHERE id = ?");
    $upd->execute([$followersCount, $targetUserId]);

    // Also update following count for follower
    $fingStmt = $pdo->prepare("SELECT COUNT(*) FROM follows WHERE follower_id = ?");
    $fingStmt->execute([$followerId]);
    $followingCount = (int)$fingStmt->fetchColumn();
    $updFing = $pdo->prepare("UPDATE users SET following_count = ? WHERE id = ?");
    $updFing->execute([$followingCount, $followerId]);

    echo json_encode([
        'success' => true,
        'message' => $isFollowing ? 'Followed successfully' : 'Unfollowed successfully',
        'data' => [
            'is_following' => $isFollowing,
            'followers_count' => $followersCount
        ]
    ]);
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error toggling follow: ' . $e->getMessage()
    ]);
}
