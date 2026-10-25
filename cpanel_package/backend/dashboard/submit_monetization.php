<?php
/**
 * Submit Monetization Application Endpoint
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;
$rawUserId = trim((string)($input['user_id'] ?? ($_GET['user_id'] ?? '')));
$cleanUserId = preg_replace('/[^0-9]/', '', $rawUserId);
$userId = (int)$cleanUserId;

if ($userId <= 0) {
    $authUser = getAuthUser($pdo);
    if ($authUser && !empty($authUser['id'])) {
        $userId = (int)$authUser['id'];
    }
}

if (!$userId) {
    jsonResponse(false, null, 'User ID is required.');
}

// 1. Fetch User safely
$user = null;
try {
    $uStmt = $pdo->prepare("SELECT id, full_name, email, phone, created_at, followers_count, is_monetized FROM users WHERE id = ?");
    $uStmt->execute([$userId]);
    $user = $uStmt->fetch();
} catch (Exception $e) {
    try {
        $uStmt = $pdo->prepare("SELECT id, full_name, email, phone, created_at FROM users WHERE id = ?");
        $uStmt->execute([$userId]);
        $user = $uStmt->fetch();
    } catch (Exception $e2) {}
}

if (!$user) {
    jsonResponse(false, null, 'User not found.');
}

if ((int)$user['is_monetized'] === 1) {
    jsonResponse(false, null, 'Your account is already monetized!');
}

// 2. Check if already has a pending application
$checkPending = $pdo->prepare("SELECT id, status, created_at FROM monetization_requests WHERE user_id = ? AND status = 'pending' LIMIT 1");
$checkPending->execute([$userId]);
$pending = $checkPending->fetch();

if ($pending) {
    jsonResponse(false, null, 'You already have a pending monetization request under review.');
}

// 3. Compute stats for application
$followersCount = (int)($user['followers_count'] ?? 0);
try {
    $fStmt = $pdo->prepare("SELECT COUNT(*) FROM follows WHERE following_id = ?");
    $fStmt->execute([$userId]);
    $dbFollowers = (int)$fStmt->fetchColumn();
    $followersCount = max($followersCount, $dbFollowers);
} catch (Exception $e) {}

$postsCount = 0;
try {
    $pStmt = $pdo->prepare("SELECT COUNT(*) FROM posts WHERE user_id = ?");
    $pStmt->execute([$userId]);
    $postsCount = (int)$pStmt->fetchColumn();
} catch (Exception $e) {}

$reelsCount = 0;
try {
    $rStmt = $pdo->prepare("SELECT COUNT(*) FROM reels WHERE user_id = ?");
    $rStmt->execute([$userId]);
    $reelsCount = (int)$rStmt->fetchColumn();
} catch (Exception $e) {}

$userCreatedAt = strtotime($user['created_at'] ?: 'now');
$accountAgeDays = (int)floor((time() - $userCreatedAt) / 86400);

// 4. Insert application into monetization_requests
$insert = $pdo->prepare("
    INSERT INTO monetization_requests 
    (user_id, followers_count, posts_count, reels_count, account_age_days, status, created_at)
    VALUES (?, ?, ?, ?, ?, 'pending', NOW())
");
$insert->execute([
    $userId,
    $followersCount,
    $postsCount,
    $reelsCount,
    $accountAgeDays
]);

$applicationId = $pdo->lastInsertId();
$submissionTime = date('d M Y, h:i A');

jsonResponse(true, [
    'application_id' => (string)$applicationId,
    'user_name' => $user['full_name'],
    'account_age_days' => $accountAgeDays,
    'followers_count' => $followersCount,
    'posts_count' => $postsCount,
    'reels_count' => $reelsCount,
    'submission_time' => $submissionTime,
    'status' => 'pending'
], 'Monetization request submitted successfully.');
