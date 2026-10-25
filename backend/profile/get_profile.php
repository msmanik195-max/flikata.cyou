<?php
/**
 * Get User Profile Details & Statistics
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$auth = getAuthUser($pdo);
$targetUserId = !empty($_GET['user_id']) ? (int)$_GET['user_id'] : ($auth ? (int)$auth['id'] : 0);

if (!$targetUserId) {
    jsonResponse(false, null, 'User ID or authorization required.');
}

// Ensure about_settings column exists
try {
    $pdo->exec("ALTER TABLE users ADD COLUMN `about_settings` TEXT DEFAULT NULL");
} catch (Exception $e) {}

$stmt = $pdo->prepare("
    SELECT id, full_name, email, phone, profile_pic, cover_pic, bio, occupation, location, gender, dob, is_verified, is_online, wallet_balance, verified_until, about_settings, created_at, updated_at 
    FROM users 
    WHERE id = ?
");
$stmt->execute([$targetUserId]);
$user = $stmt->fetch();

if (!$user) {
    jsonResponse(false, null, 'User not found.');
}

// User posts count
$pStmt = $pdo->prepare("SELECT COUNT(*) FROM posts WHERE user_id = ?");
$pStmt->execute([$targetUserId]);
$user['posts_count'] = (int)$pStmt->fetchColumn();

// User photos count
$imgStmt = $pdo->prepare("SELECT COUNT(*) FROM posts WHERE user_id = ? AND image_url IS NOT NULL");
$imgStmt->execute([$targetUserId]);
$user['photos_count'] = (int)$imgStmt->fetchColumn();

// User reels count
$rStmt = $pdo->prepare("SELECT COUNT(*) FROM reels WHERE user_id = ?");
$rStmt->execute([$targetUserId]);
$user['reels_count'] = (int)$rStmt->fetchColumn();

// Real followers count
$fStmt = $pdo->prepare("SELECT COUNT(*) FROM follows WHERE following_id = ?");
$fStmt->execute([$targetUserId]);
$user['followers_count'] = (int)$fStmt->fetchColumn();

// Real following count
$fingStmt = $pdo->prepare("SELECT COUNT(*) FROM follows WHERE follower_id = ?");
$fingStmt->execute([$targetUserId]);
$user['following_count'] = (int)$fingStmt->fetchColumn();

// Check if viewer is following this user
$isFollowing = false;
if ($auth && !empty($auth['id']) && (int)$auth['id'] !== $targetUserId) {
    $isFStmt = $pdo->prepare("SELECT COUNT(*) FROM follows WHERE follower_id = ? AND following_id = ?");
    $isFStmt->execute([(int)$auth['id'], $targetUserId]);
    $isFollowing = ($isFStmt->fetchColumn() > 0);
}
$user['is_following'] = $isFollowing;

// Parse about_settings
$aboutSettings = [
    'show_phone' => true,
    'show_email' => true,
    'show_dob' => true,
    'show_gender' => true,
    'show_location' => true,
    'show_occupation' => true,
    'show_education' => true,
    'show_website' => true
];
if (!empty($user['about_settings'])) {
    $decoded = json_decode($user['about_settings'], true);
    if (is_array($decoded)) {
        foreach ($decoded as $k => $v) {
            $aboutSettings[$k] = (bool)$v;
        }
    }
}
$user['about_settings'] = $aboutSettings;

$secondsAgo = !empty($user['updated_at']) ? (time() - strtotime($user['updated_at'])) : 99999;
if ((int)($user['is_verified'] ?? 0) === 1 && !empty($user['verified_until'])) {
    $exp = strtotime($user['verified_until']);
    if ($exp < strtotime('2090-01-01') && time() > ($exp + 3 * 86400)) {
        $pdo->prepare("UPDATE users SET is_verified = 0 WHERE id = ?")->execute([$targetUserId]);
        $user['is_verified'] = 0;
    }
}
$user['is_verified'] = (int)($user['is_verified'] ?? 0);
$user['wallet_balance'] = (float)($user['wallet_balance'] ?? 0.00);
$user['is_online'] = ((int)($user['is_online'] ?? 0) === 1 && $secondsAgo <= 60) ? 1 : 0;
$user['joinedDate'] = 'Joined ' . date('F Y', strtotime($user['created_at']));

jsonResponse(true, $user, 'Profile loaded.');
