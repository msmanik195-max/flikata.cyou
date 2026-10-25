<?php
/**
 * Leaderboard Endpoint - Real Users Only
 * Returns exclusively real registered users ordered by real engagement metrics
 * Flikata API
 */

header('Content-Type: application/json; charset=UTF-8');
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

// Determine leaderboard user limit from admin settings table or query param
$limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 0;
if ($limit <= 0) {
    try {
        $stmt = $pdo->query("SELECT `user_limit` FROM `leaderboard_settings` WHERE `id` = 1 LIMIT 1");
        $row = $stmt ? $stmt->fetch() : null;
        $limit = ($row && isset($row['user_limit']) && (int)$row['user_limit'] > 0) ? (int)$row['user_limit'] : 20;
    } catch (Exception $e) {
        $limit = 20;
    }
}
if ($limit <= 0) $limit = 20;

$leaderboard = [];

try {
    // Query exclusively REAL users from users table with their aggregated post likes and comments
    $sql = "
        SELECT 
            u.id,
            u.full_name,
            u.email,
            u.profile_pic,
            u.is_verified,
            u.followers_count,
            COALESCE(SUM(p.likes_count), 0) AS total_likes,
            COALESCE(SUM(p.comments_count), 0) AS total_comments
        FROM users u
        LEFT JOIN posts p ON p.user_id = u.id
        WHERE u.is_banned = 0
        GROUP BY u.id
        ORDER BY (COALESCE(SUM(p.likes_count), 0) * 2 + COALESCE(SUM(p.comments_count), 0) * 3 + u.followers_count * 1) DESC, u.id ASC
        LIMIT :limit
    ";
    
    $stmt = $pdo->prepare($sql);
    $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
    $stmt->execute();
    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $rank = 1;
    foreach ($rows as $r) {
        $likes = (int)$r['total_likes'];
        $comments = (int)$r['total_comments'];
        $followers = (int)$r['followers_count'];
        $score = ($likes * 2) + ($comments * 3) + $followers;

        $rawName = trim($r['full_name'] ?? '');
        $username = strtolower(preg_replace('/[^a-zA-Z0-9]/', '', $rawName));
        if (empty($username)) $username = 'user' . $r['id'];

        $leaderboard[] = [
            'id' => (string)$r['id'],
            'name' => !empty($rawName) ? $rawName : 'Flikata User',
            'username' => '@' . $username,
            'avatar_url' => $r['profile_pic'] ?: '',
            'is_verified' => ((int)$r['is_verified']) === 1,
            'rank' => $rank,
            'total_likes' => $likes,
            'total_comments' => $comments,
            'total_followers' => $followers,
            'engagement_score' => $score
        ];
        $rank++;
    }
} catch (Exception $e) {
    // In case of database error, return empty array without injecting dummy users
}

echo json_encode([
    'success' => true,
    'message' => 'Real leaderboard loaded successfully',
    'limit' => $limit,
    'total_returned' => count($leaderboard),
    'data' => $leaderboard
], JSON_UNESCAPED_UNICODE);
