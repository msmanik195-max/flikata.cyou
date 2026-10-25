<?php
/**
 * Get Pages Endpoint
 * Returns user managed pages and discover pages
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$rawUserId = trim((string)($_GET['user_id'] ?? ''));
$userId = (int)preg_replace('/[^0-9]/', '', $rawUserId);

if ($userId <= 0) {
    $authUser = getAuthUser($pdo);
    if ($authUser && !empty($authUser['id'])) {
        $userId = (int)$authUser['id'];
    } else {
        $userId = 1;
    }
}

// Fetch user owned pages
$stmt = $pdo->prepare("SELECT * FROM `pages` WHERE user_id = ? ORDER BY id DESC");
$stmt->execute([$userId]);
$userPages = $stmt->fetchAll();

// Fetch other discover pages
$dStmt = $pdo->prepare("SELECT * FROM `pages` WHERE user_id != ? ORDER BY followers_count DESC, id DESC LIMIT 20");
$dStmt->execute([$userId]);
$discoverPages = $dStmt->fetchAll();

function formatPage($row) {
    return [
        'id'             => (string)$row['id'],
        'userId'         => (string)$row['user_id'],
        'user_id'        => (string)$row['user_id'],
        'name'           => $row['name'],
        'category'       => $row['category'],
        'bio'            => $row['bio'] ?: null,
        'logoUrl'        => $row['logo_url'] ?: null,
        'logo_url'       => $row['logo_url'] ?: null,
        'bannerUrl'      => $row['banner_url'] ?: null,
        'banner_url'     => $row['banner_url'] ?: null,
        'email'          => $row['email'] ?: null,
        'phone'          => $row['phone'] ?: null,
        'website'        => $row['website'] ?: null,
        'followersCount' => (int)$row['followers_count'],
        'followers_count'=> (int)$row['followers_count'],
        'likesCount'     => (int)$row['likes_count'],
        'likes_count'    => (int)$row['likes_count'],
        'isVerified'     => (bool)$row['is_verified'],
        'is_verified'    => (bool)$row['is_verified'],
        'createdAt'      => $row['created_at'],
        'created_at'     => $row['created_at']
    ];
}

$formattedUserPages = array_map('formatPage', $userPages);
$formattedDiscover = array_map('formatPage', $discoverPages);

jsonResponse(true, [
    'user_pages'     => $formattedUserPages,
    'discover_pages' => $formattedDiscover,
    'pages'          => array_merge($formattedUserPages, $formattedDiscover)
], 'Pages retrieved successfully.');
