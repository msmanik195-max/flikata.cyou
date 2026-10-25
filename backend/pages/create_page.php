<?php
/**
 * Create New Page Endpoint
 * Enforces admin-configurable limit on maximum pages per user
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$rawUserId = trim((string)($input['user_id'] ?? ''));
$userId = (int)preg_replace('/[^0-9]/', '', $rawUserId);

if ($userId <= 0) {
    $authUser = getAuthUser($pdo);
    if ($authUser && !empty($authUser['id'])) {
        $userId = (int)$authUser['id'];
    } else {
        $userId = 1;
    }
}

// Check Admin App Toggle: allow_page_creation
try {
    $fsStmt = $pdo->query("SELECT allow_page_creation FROM app_feature_settings WHERE id = 1");
    $allowPage = $fsStmt ? $fsStmt->fetchColumn() : 1;
    if ($allowPage !== false && (int)$allowPage === 0) {
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode([
            'status'  => 'error',
            'success' => false,
            'message' => 'পেজ তৈরি করার সুবিধা সাময়িকভাবে বন্ধ আছে।'
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }
} catch (Exception $e) {}

// 1. Fetch Admin Setting: max_pages_per_user
$setStmt = $pdo->query("SELECT `max_pages_per_user` FROM `page_settings` WHERE `id` = 1 LIMIT 1");
$setRow = $setStmt ? $setStmt->fetch() : null;
$maxPagesAllowed = $setRow ? (int)$setRow['max_pages_per_user'] : 5;

// 2. Count existing pages created by this user
$cntStmt = $pdo->prepare("SELECT COUNT(*) FROM `pages` WHERE `user_id` = ?");
$cntStmt->execute([$userId]);
$userPagesCount = (int)$cntStmt->fetchColumn();

if ($userPagesCount >= $maxPagesAllowed) {
    jsonResponse(false, null, "You have reached the maximum allowed limit of {$maxPagesAllowed} pages per profile set by administrator.");
}

$name = trim((string)($input['name'] ?? ''));
if (empty($name)) {
    jsonResponse(false, null, 'Page name is required.');
}

$category = trim((string)($input['category'] ?? 'Digital Creator'));
$bio = trim((string)($input['bio'] ?? ''));
$logoUrl = trim((string)($input['logo_url'] ?? ''));
$bannerUrl = trim((string)($input['banner_url'] ?? ''));
$email = trim((string)($input['email'] ?? ''));
$phone = trim((string)($input['phone'] ?? ''));
$website = trim((string)($input['website'] ?? ''));

try {
    $stmt = $pdo->prepare("
        INSERT INTO `pages` 
        (user_id, name, category, bio, logo_url, banner_url, email, phone, website, followers_count, likes_count, is_verified, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 1, 0, NOW())
    ");
    $stmt->execute([
        $userId,
        $name,
        $category,
        $bio ?: null,
        $logoUrl ?: null,
        $bannerUrl ?: null,
        $email ?: null,
        $phone ?: null,
        $website ?: null
    ]);

    $newPageId = (string)$pdo->lastInsertId();

    $pageData = [
        'id'             => $newPageId,
        'userId'         => (string)$userId,
        'user_id'        => (string)$userId,
        'name'           => $name,
        'category'       => $category,
        'bio'            => $bio ?: null,
        'logoUrl'        => $logoUrl ?: null,
        'logo_url'       => $logoUrl ?: null,
        'bannerUrl'      => $bannerUrl ?: null,
        'banner_url'     => $bannerUrl ?: null,
        'email'          => $email ?: null,
        'phone'          => $phone ?: null,
        'website'        => $website ?: null,
        'followersCount' => 1,
        'followers_count'=> 1,
        'likesCount'     => 1,
        'likes_count'    => 1,
        'isVerified'     => false,
        'is_verified'    => false,
        'createdAt'      => 'Just now',
        'created_at'     => date('Y-m-d H:i:s')
    ];

    jsonResponse(true, $pageData, 'Page created successfully.');
} catch (Exception $e) {
    jsonResponse(false, null, 'Database error: ' . $e->getMessage());
}
