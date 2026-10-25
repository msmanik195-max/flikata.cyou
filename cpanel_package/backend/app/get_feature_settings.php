<?php
/**
 * Get App Feature Settings (Page Creation, Login, Registration, Chat, Feed Filters & Limits)
 * Flikata API
 */
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

try {
    $stmt = $pdo->query("SELECT * FROM app_feature_settings WHERE id = 1 LIMIT 1");
    $row = $stmt ? $stmt->fetch(PDO::FETCH_ASSOC) : null;
    
    if (!$row) {
        $row = [
            'allow_page_creation'       => 1,
            'allow_login'               => 1,
            'allow_registration'        => 1,
            'allow_chat'                => 1,
            'feed_show_text_posts'      => 1,
            'feed_show_image_posts'     => 1,
            'feed_show_video_posts'     => 1,
            'daily_text_limit'          => 30,
            'daily_image_limit'         => 20,
            'daily_video_limit'         => 10,
            'daily_story_limit'         => 15,
            'default_page_create_limit' => 5
        ];
    }

    echo json_encode([
        'success'                   => true,
        'allow_page_creation'       => (bool)($row['allow_page_creation'] ?? 1),
        'allow_login'               => (bool)($row['allow_login'] ?? 1),
        'allow_registration'        => (bool)($row['allow_registration'] ?? 1),
        'allow_chat'                => (bool)($row['allow_chat'] ?? 1),
        'allow_reels'               => (bool)($row['allow_reels'] ?? 1),
        'feed_show_text_posts'      => (bool)($row['feed_show_text_posts'] ?? 1),
        'feed_show_image_posts'     => (bool)($row['feed_show_image_posts'] ?? 1),
        'feed_show_video_posts'     => (bool)($row['feed_show_video_posts'] ?? 1),
        'daily_text_limit'          => (int)($row['daily_text_limit'] ?? 30),
        'daily_image_limit'         => (int)($row['daily_image_limit'] ?? 20),
        'daily_video_limit'         => (int)($row['daily_video_limit'] ?? 10),
        'daily_story_limit'         => (int)($row['daily_story_limit'] ?? 15),
        'default_page_create_limit' => (int)($row['default_page_create_limit'] ?? 5)
    ], JSON_UNESCAPED_UNICODE);
} catch (Exception $e) {
    echo json_encode([
        'success'                   => true,
        'allow_page_creation'       => true,
        'allow_login'               => true,
        'allow_registration'        => true,
        'allow_chat'                => true,
        'feed_show_text_posts'      => true,
        'feed_show_image_posts'     => true,
        'feed_show_video_posts'     => true,
        'daily_text_limit'          => 30,
        'daily_image_limit'         => 20,
        'daily_video_limit'         => 10,
        'daily_story_limit'         => 15,
        'default_page_create_limit' => 5
    ], JSON_UNESCAPED_UNICODE);
}
exit;
