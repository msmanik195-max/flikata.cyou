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

// Check Admin App Toggle & Page Limits
$uStmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
$uStmt->execute([$userId]);
$userRow = $uStmt->fetch(PDO::FETCH_ASSOC);

$fsStmt = $pdo->query("SELECT allow_page_creation, default_page_create_limit FROM app_feature_settings WHERE id = 1 LIMIT 1");
$fsRow = $fsStmt ? $fsStmt->fetch(PDO::FETCH_ASSOC) : null;
$allowPage = isset($fsRow['allow_page_creation']) ? (int)$fsRow['allow_page_creation'] : 1;
$defaultPageLimit = isset($fsRow['default_page_create_limit']) ? (int)$fsRow['default_page_create_limit'] : 5;

if ($allowPage === 0) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'status'  => 'error',
        'success' => false,
        'message' => 'পেজ তৈরি করার সুবিধা সাময়িকভাবে বন্ধ আছে।'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$effectivePageLimit = ($userRow && $userRow['custom_page_limit'] !== null && $userRow['custom_page_limit'] !== '') ? (int)$userRow['custom_page_limit'] : $defaultPageLimit;

if ($effectivePageLimit === 0) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'status'  => 'error',
        'success' => false,
        'message' => 'পেজ তৈরি করার সুবিধা সাময়িকভাবে বন্ধ আছে।'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Count existing pages created by this user
$cntStmt = $pdo->prepare("SELECT COUNT(*) FROM `pages` WHERE `user_id` = ?");
$cntStmt->execute([$userId]);
$userPagesCount = (int)$cntStmt->fetchColumn();

if ($userPagesCount >= $effectivePageLimit) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'status'  => 'error',
        'success' => false,
        'message' => "আপনার পেজ তৈরির সর্বোচ্চ সীমা ({$effectivePageLimit} টি) শেষ হয়েছে।"
    ], JSON_UNESCAPED_UNICODE);
    exit;
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
