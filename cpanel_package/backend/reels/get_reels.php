<?php
/**
 * Get Reels Feed
 * Flikata API - Backfills all videos from posts and serves reels
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$currentUser = getAuthUser($pdo);
$currentUserId = $currentUser ? $currentUser['id'] : 0;
if (!$currentUserId && !empty($_GET['user_id'])) {
    $currentUserId = (int)$_GET['user_id'];
}

// Check allow_reels setting
try {
    $fsStmt = $pdo->query("SELECT allow_reels FROM app_feature_settings WHERE id = 1 LIMIT 1");
    $allowReels = $fsStmt ? $fsStmt->fetchColumn() : 1;
    if ($allowReels !== false && (int)$allowReels === 0) {
        jsonResponse(true, [], 'Reels are currently disabled');
        exit;
    }
} catch (Exception $e) {}

// 1. Backfill any videos from posts table into reels table so every video is available in Reels
try {
    $pdo->exec("
        INSERT INTO reels (user_id, media_url, caption, audio_title, created_at)
        SELECT p.user_id, p.video_url, 
               COALESCE(NULLIF(p.content, ''), 'Video Reel'), 
               CONCAT('Original Audio - ', COALESCE(u.full_name, 'Creator')), 
               COALESCE(p.created_at, NOW())
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE p.video_url IS NOT NULL AND p.video_url != ''
          AND NOT EXISTS (
              SELECT 1 FROM reels r WHERE r.media_url = p.video_url
          )
    ");
} catch (Exception $e) {
    error_log("Backfill error: " . $e->getMessage());
}

// 2. Fetch all reels ordered by newest
$stmt = $pdo->prepare("
    SELECT 
        r.*,
        COALESCE(u.full_name, 'Creator') AS author_name,
        COALESCE(u.profile_pic, '') AS author_avatar,
        COALESCE(u.is_verified, 0) AS author_verified,
        (SELECT COUNT(*) FROM reel_likes WHERE reel_id = r.id AND user_id = ?) AS is_liked,
        (SELECT reaction FROM reel_likes WHERE reel_id = r.id AND user_id = ? LIMIT 1) AS user_reaction
    FROM reels r
    LEFT JOIN users u ON r.user_id = u.id
    ORDER BY r.id DESC
    LIMIT 100
");
$stmt->execute([$currentUserId, $currentUserId]);
$reels = $stmt->fetchAll();

$formatted = [];
$seenMediaUrls = [];

foreach ($reels as $r) {
    // Fetch comments for this reel safely
    $comments = [];
    try {
        $cStmt = $pdo->prepare("
            SELECT c.*, COALESCE(u.full_name, 'User') AS author_name, COALESCE(u.profile_pic, '') AS author_avatar, COALESCE(u.is_verified, 0) AS is_verified
            FROM comments c
            LEFT JOIN users u ON c.user_id = u.id
            WHERE c.reel_id = ? OR (c.post_id > 0 AND c.post_id = ?)
            ORDER BY c.id DESC
            LIMIT 30
        ");
        $cStmt->execute([$r['id'], $r['id']]);
        $comments = $cStmt->fetchAll() ?: [];
    } catch (Exception $ce) {}

    $seenMediaUrls[] = $r['media_url'];

    $formatted[] = [
        'id'            => (string)$r['id'],
        'userId'        => (string)$r['user_id'],
        'user_id'       => (string)$r['user_id'],
        'authorName'    => $r['author_name'],
        'authorAvatar'  => $r['author_avatar'] ?: '',
        'isVerified'    => (bool)$r['author_verified'],
        'mediaUrl'      => $r['media_url'],
        'caption'       => $r['caption'] ?: '',
        'audioTitle'    => $r['audio_title'] ?: 'Original Audio',
        'likesCount'    => (int)$r['likes_count'],
        'commentsCount' => (int)$r['comments_count'],
        'sharesCount'   => (int)$r['shares_count'],
        'viewsCount'    => (string)$r['views_count'],
        'isLiked'       => (bool)$r['is_liked'],
        'userReaction'  => $r['user_reaction'] ?: null,
        'comments'      => array_map(function($c) {
            return [
                'id'            => (string)$c['id'],
                'authorName'    => $c['author_name'],
                'authorAvatar'  => $c['author_avatar'] ?: '',
                'text'          => $c['text'],
                'timestamp'     => $c['created_at'],
                'reactionEmoji' => $c['reaction_emoji'] ?? null,
                'isVerified'    => (bool)$c['is_verified']
            ];
        }, $comments)
    ];
}

// 3. Fallback: If any post has video that is not in reels yet, add it directly
try {
    $pStmt = $pdo->prepare("
        SELECT p.*, 
               COALESCE(u.full_name, 'Creator') AS author_name,
               COALESCE(u.profile_pic, '') AS author_avatar,
               COALESCE(u.is_verified, 0) AS author_verified
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE p.video_url IS NOT NULL AND p.video_url != ''
        ORDER BY p.id DESC
        LIMIT 50
    ");
    $pStmt->execute();
    $videoPosts = $pStmt->fetchAll() ?: [];
    foreach ($videoPosts as $vp) {
        if (!in_array($vp['video_url'], $seenMediaUrls)) {
            $vpComments = [];
            try {
                $cStmt = $pdo->prepare("
                    SELECT c.*, COALESCE(u.full_name, 'User') AS author_name, COALESCE(u.profile_pic, '') AS author_avatar, COALESCE(u.is_verified, 0) AS is_verified
                    FROM comments c
                    LEFT JOIN users u ON c.user_id = u.id
                    WHERE c.post_id = ?
                    ORDER BY c.id DESC
                    LIMIT 30
                ");
                $cStmt->execute([$vp['id']]);
                $vpComments = $cStmt->fetchAll() ?: [];
            } catch (Exception $ce) {}

            $formatted[] = [
                'id'            => 'post_' . $vp['id'],
                'userId'        => (string)$vp['user_id'],
                'user_id'       => (string)$vp['user_id'],
                'authorName'    => $vp['author_name'],
                'authorAvatar'  => $vp['author_avatar'] ?: '',
                'isVerified'    => (bool)$vp['author_verified'],
                'mediaUrl'      => $vp['video_url'],
                'caption'       => $vp['content'] ?: 'Video Reel',
                'audioTitle'    => 'Original Audio - ' . $vp['author_name'],
                'likesCount'    => (int)$vp['likes_count'],
                'commentsCount' => (int)$vp['comments_count'],
                'sharesCount'   => (int)$vp['shares_count'],
                'viewsCount'    => (string)($vp['views_count'] ?? '0'),
                'isLiked'       => false,
                'userReaction'  => null,
                'comments'      => array_map(function($c) {
                    return [
                        'id'            => (string)$c['id'],
                        'authorName'    => $c['author_name'],
                        'authorAvatar'  => $c['author_avatar'] ?: '',
                        'text'          => $c['text'],
                        'timestamp'     => $c['created_at'],
                        'reactionEmoji' => $c['reaction_emoji'] ?? null,
                        'isVerified'    => (bool)$c['is_verified']
                    ];
                }, $vpComments)
            ];
            $seenMediaUrls[] = $vp['video_url'];
        }
    }
} catch (Exception $pe) {}

jsonResponse(true, $formatted, 'Reels loaded.');
