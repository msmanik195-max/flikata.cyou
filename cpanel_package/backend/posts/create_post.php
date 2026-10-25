<?php
/**
 * Create New Post Endpoint
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$user = getAuthUser($pdo);
if (!$user && !empty($input['user_id'])) {
    $uStmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
    $uStmt->execute([$input['user_id']]);
    $user = $uStmt->fetch() ?: null;
}

if (!$user) {
    header('Content-Type: application/json; charset=utf-8');
    http_response_code(401);
    echo json_encode(['error' => 'Authentication required.'], JSON_UNESCAPED_UNICODE);
    exit;
}

$content  = trim($input['content'] ?? '');
$imageUrl = trim($input['image_url'] ?? '');
$videoUrl = trim($input['video_url'] ?? '');
$privacy  = trim($input['privacy'] ?? 'Public');
$bgColor  = trim($input['bg_color'] ?? $input['background_style'] ?? '');
$bucketId = !empty($input['bucket_id']) ? (int)$input['bucket_id'] : null;

if (!$content && !$imageUrl && !$videoUrl) {
    header('Content-Type: application/json; charset=utf-8');
    http_response_code(400);
    echo json_encode(['error' => 'Post content, image, or video is required.'], JSON_UNESCAPED_UNICODE);
    exit;
}

// -------------------------------------------------------------
// Check 24-Hour Limits & 0-Disable Rule
// -------------------------------------------------------------
$settingsStmt = $pdo->query("SELECT daily_text_limit, daily_image_limit, daily_video_limit FROM app_feature_settings WHERE id = 1 LIMIT 1");
$settingsRow = $settingsStmt ? $settingsStmt->fetch(PDO::FETCH_ASSOC) : null;
$defaultTextLimit  = isset($settingsRow['daily_text_limit']) ? (int)$settingsRow['daily_text_limit'] : 30;
$defaultImageLimit = isset($settingsRow['daily_image_limit']) ? (int)$settingsRow['daily_image_limit'] : 20;
$defaultVideoLimit = isset($settingsRow['daily_video_limit']) ? (int)$settingsRow['daily_video_limit'] : 10;

$postType = 'text';
$effectiveLimit = $defaultTextLimit;
$countSql = "SELECT COUNT(*) FROM posts WHERE user_id = ? AND (image_url IS NULL OR image_url = '') AND (video_url IS NULL OR video_url = '') AND created_at >= NOW() - INTERVAL 1 DAY";

if ($videoUrl) {
    $postType = 'video';
    $effectiveLimit = ($user['custom_video_limit'] !== null && $user['custom_video_limit'] !== '') ? (int)$user['custom_video_limit'] : $defaultVideoLimit;
    $countSql = "SELECT COUNT(*) FROM posts WHERE user_id = ? AND video_url IS NOT NULL AND video_url != '' AND created_at >= NOW() - INTERVAL 1 DAY";
} elseif ($imageUrl) {
    $postType = 'image';
    $effectiveLimit = ($user['custom_image_limit'] !== null && $user['custom_image_limit'] !== '') ? (int)$user['custom_image_limit'] : $defaultImageLimit;
    $countSql = "SELECT COUNT(*) FROM posts WHERE user_id = ? AND image_url IS NOT NULL AND image_url != '' AND (video_url IS NULL OR video_url = '') AND created_at >= NOW() - INTERVAL 1 DAY";
} else {
    $postType = 'text';
    $effectiveLimit = ($user['custom_text_limit'] !== null && $user['custom_text_limit'] !== '') ? (int)$user['custom_text_limit'] : $defaultTextLimit;
}

// Rule 1: Zero Limit = Feature Disabled
if ($effectiveLimit === 0) {
    try {
        $nStmt = $pdo->prepare("INSERT INTO notifications (user_id, from_user_id, title, message, type, is_read, created_at) VALUES (?, NULL, 'পোস্ট সংক্রান্ত নোটিশ', 'পোস্ট করা সাময়িকভাবে বন্ধ আছে', 'system', 0, NOW())");
        $nStmt->execute([$user['id']]);
    } catch (Exception $e) {}

    header('Content-Type: application/json; charset=utf-8');
    http_response_code(403);
    echo json_encode([
        'status'  => 'error',
        'success' => false,
        'error'   => 'পোস্ট করা সাময়িকভাবে বন্ধ আছে',
        'message' => 'পোস্ট করা সাময়িকভাবে বন্ধ আছে'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Rule 2: 24-Hour Limit Reached
try {
    $cStmt = $pdo->prepare($countSql);
    $cStmt->execute([$user['id']]);
    $recentCount = (int)$cStmt->fetchColumn();

    if ($recentCount >= $effectiveLimit) {
        $nStmt = $pdo->prepare("INSERT INTO notifications (user_id, from_user_id, title, message, type, is_read, created_at) VALUES (?, NULL, 'দৈনিক লিমিট শেষ', 'আপনার আজকের পোস্ট করার সীমা শেষ হয়েছে। আগামী ২৪ ঘণ্টা পর আবার পোস্ট করতে পারবেন।', 'alert', 0, NOW())");
        $nStmt->execute([$user['id']]);

        header('Content-Type: application/json; charset=utf-8');
        http_response_code(403);
        echo json_encode([
            'status'  => 'error',
            'success' => false,
            'error'   => 'আপনার আজকের পোস্ট করার সীমা শেষ হয়েছে।',
            'message' => 'আপনার আজকের পোস্ট করার সীমা শেষ হয়েছে।'
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }
} catch (Exception $e) {}

$pageId = !empty($input['page_id']) ? (int)preg_replace('/[^0-9]/', '', (string)$input['page_id']) : null;
$pageRow = null;
if ($pageId) {
    $pStmt = $pdo->prepare("SELECT * FROM pages WHERE id = ?");
    $pStmt->execute([$pageId]);
    $pageRow = $pStmt->fetch() ?: null;
    if (!$pageRow) {
        $pageId = null;
    }
}

$effectiveAuthorName   = $pageRow ? $pageRow['name'] : $user['full_name'];
$effectiveAuthorAvatar = $pageRow ? ($pageRow['logo_url'] ?: '') : ($user['profile_pic'] ?: '');
$effectiveIsVerified   = $pageRow ? (bool)$pageRow['is_verified'] : (bool)$user['is_verified'];

$stmt = $pdo->prepare("
    INSERT INTO posts (user_id, page_id, content, image_url, video_url, privacy, bg_color, bucket_id)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
");
$stmt->execute([
    $user['id'],
    $pageId,
    $content ?: null,
    $imageUrl ?: null,
    $videoUrl ?: null,
    $privacy,
    $bgColor ?: null,
    $bucketId
]);

$postId = $pdo->lastInsertId();

// If this post has a video, automatically make it available in Reels as well
if ($videoUrl) {
    $chkStmt = $pdo->prepare("SELECT id FROM reels WHERE media_url = ? LIMIT 1");
    $chkStmt->execute([$videoUrl]);
    if (!$chkStmt->fetch()) {
        $rStmt = $pdo->prepare("
            INSERT INTO reels (user_id, media_url, caption, audio_title, bucket_id)
            VALUES (?, ?, ?, ?, ?)
        ");
        $rStmt->execute([
            $user['id'],
            $videoUrl,
            $content ?: ('New Reel by ' . $effectiveAuthorName),
            'Original Audio - ' . $effectiveAuthorName,
            $bucketId
        ]);
    }
}

$postObj = [
    'id'               => (string)$postId,
    'user_id'          => $pageId ? "page_{$pageId}" : (string)$user['id'],
    'page_id'          => $pageId ? (string)$pageId : null,
    'author_name'      => $effectiveAuthorName,
    'author_avatar'    => $effectiveAuthorAvatar,
    'is_verified'      => $effectiveIsVerified,
    'timestamp'        => 'Just now',
    'content'          => $content ?: '',
    'background_style' => $bgColor ?: null,
    'image_url'        => $imageUrl ?: null,
    'video_url'        => $videoUrl ?: null,
    'is_video'         => !empty($videoUrl),
    'likes_count'      => 0,
    'comments_count'   => 0,
    'shares_count'     => 0,
    'is_liked'         => false,
    'privacy'          => $privacy
];

header('Content-Type: application/json; charset=utf-8');
echo json_encode($postObj, JSON_UNESCAPED_UNICODE);
exit;
