<?php
/**
 * Get Posts Feed
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$currentUser = getAuthUser($pdo);
$currentUserId = $currentUser ? (int)$currentUser['id'] : 0;
$userParam = (int)($_GET['user_id'] ?? 0);
if (!$currentUserId && $userParam) {
    $currentUserId = $userParam;
}

$page = max(1, (int)($_GET['page'] ?? 1));
$limit = min(50, max(1, (int)($_GET['limit'] ?? 20)));
$offset = ($page - 1) * $limit;

$pageIdParam = (int)($_GET['page_id'] ?? 0);

if ($pageIdParam > 0) {
    $stmt = $pdo->prepare("
        SELECT 
            p.*,
            COALESCE(pg.name, u.full_name) AS author_name,
            COALESCE(pg.logo_url, u.profile_pic) AS author_avatar,
            COALESCE(pg.is_verified, u.is_verified) AS author_verified,
            (SELECT COUNT(*) FROM post_likes WHERE post_id = p.id AND user_id = ?) AS is_liked,
            (SELECT emoji FROM post_reactions WHERE post_id = p.id AND user_id = ? LIMIT 1) AS user_reaction,
            (SELECT COUNT(*) FROM comments WHERE post_id = p.id) AS real_comments_count
        FROM posts p
        JOIN users u ON p.user_id = u.id
        LEFT JOIN pages pg ON p.page_id = pg.id
        WHERE p.page_id = ?
        ORDER BY p.id DESC
        LIMIT ? OFFSET ?
    ");
    $stmt->execute([$currentUserId, $currentUserId, $pageIdParam, $limit, $offset]);
} else {
    $stmt = $pdo->prepare("
        SELECT 
            p.*,
            COALESCE(pg.name, u.full_name) AS author_name,
            COALESCE(pg.logo_url, u.profile_pic) AS author_avatar,
            COALESCE(pg.is_verified, u.is_verified) AS author_verified,
            (SELECT COUNT(*) FROM post_likes WHERE post_id = p.id AND user_id = ?) AS is_liked,
            (SELECT emoji FROM post_reactions WHERE post_id = p.id AND user_id = ? LIMIT 1) AS user_reaction,
            (SELECT COUNT(*) FROM comments WHERE post_id = p.id) AS real_comments_count
        FROM posts p
        JOIN users u ON p.user_id = u.id
        LEFT JOIN pages pg ON p.page_id = pg.id
        WHERE (
            p.privacy = 'Public' 
            OR p.privacy IS NULL 
            OR p.privacy = '' 
            OR p.user_id = ?
        )
        ORDER BY p.id DESC
        LIMIT ? OFFSET ?
    ");
    $stmt->execute([$currentUserId, $currentUserId, $currentUserId, $limit, $offset]);
}
$posts = $stmt->fetchAll();

$formattedPosts = [];
foreach ($posts as $row) {
    // Fetch latest 3 preview comments for this post
    $cStmt = $pdo->prepare("
        SELECT c.*, cu.full_name AS author_name, cu.profile_pic AS author_avatar, cu.is_verified
        FROM comments c
        JOIN users cu ON c.user_id = cu.id
        WHERE c.post_id = ? AND c.parent_id IS NULL
        ORDER BY c.id DESC
        LIMIT 3
    ");
    $cStmt->execute([$row['id']]);
    $comments = $cStmt->fetchAll();

    $postOwnerId = !empty($row['page_id']) ? "page_" . $row['page_id'] : (string)$row['user_id'];

    $formattedPosts[] = [
        'id'               => (string)$row['id'],
        'user_id'          => $postOwnerId,
        'userId'           => $postOwnerId,
        'page_id'          => !empty($row['page_id']) ? (string)$row['page_id'] : null,
        'pageId'           => !empty($row['page_id']) ? (string)$row['page_id'] : null,
        'author_name'      => $row['author_name'],
        'authorName'       => $row['author_name'],
        'author_avatar'    => $row['author_avatar'] ?: '',
        'authorAvatar'     => $row['author_avatar'] ?: '',
        'is_verified'      => (bool)$row['author_verified'],
        'isVerified'       => (bool)$row['author_verified'],
        'content'          => $row['content'] ?: '',
        'image_url'        => $row['image_url'] ?: null,
        'imageUrl'         => $row['image_url'] ?: null,
        'video_url'        => $row['video_url'] ?: null,
        'videoUrl'         => $row['video_url'] ?: null,
        'is_video'         => !empty($row['video_url']),
        'isVideo'          => !empty($row['video_url']),
        'privacy'          => $row['privacy'] ?: 'Public',
        'background_style' => $row['bg_color'] ?: null,
        'bgColor'          => $row['bg_color'] ?: null,
        'likes_count'      => (int)$row['likes_count'],
        'likesCount'       => (int)$row['likes_count'],
        'comments_count'   => max((int)$row['comments_count'], (int)$row['real_comments_count']),
        'commentsCount'    => max((int)$row['comments_count'], (int)$row['real_comments_count']),
        'shares_count'     => (int)$row['shares_count'],
        'sharesCount'      => (int)$row['shares_count'],
        'views_count'      => (int)($row['views_count'] ?? 0),
        'viewsCount'       => (int)($row['views_count'] ?? 0),
        'is_liked'         => (bool)$row['is_liked'],
        'isLiked'          => (bool)$row['is_liked'],
        'user_reaction'    => $row['user_reaction'],
        'userReaction'     => $row['user_reaction'],
        'reactions'        => [],
        'timestamp'        => $row['created_at'],
        'commentsList'     => array_map(function($c) {
            return [
                'id'            => (string)$c['id'],
                'postId'        => (string)$c['post_id'],
                'post_id'       => (string)$c['post_id'],
                'authorName'    => $c['author_name'],
                'author_name'   => $c['author_name'],
                'authorAvatar'  => $c['author_avatar'] ?: '',
                'author_avatar' => $c['author_avatar'] ?: '',
                'text'          => $c['text'],
                'timestamp'     => $c['created_at'],
                'reactionEmoji' => $c['reaction_emoji'],
                'likesCount'    => (int)$c['likes_count'],
                'likes_count'   => (int)$c['likes_count'],
                'isLiked'       => false,
                'replies'       => []
            ];
        }, $comments)
    ];
}

header('Content-Type: application/json; charset=utf-8');
echo json_encode($formattedPosts, JSON_UNESCAPED_UNICODE);
exit;
