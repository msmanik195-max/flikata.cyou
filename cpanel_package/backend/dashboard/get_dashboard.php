<?php
/**
 * Professional Dashboard API Endpoint
 * Flikata API
 * Returns real dynamic performance overview, monetization criteria, and recent content.
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;
$rawUserId = trim((string)($input['user_id'] ?? ($_GET['user_id'] ?? '')));
$cleanUserId = preg_replace('/[^0-9]/', '', $rawUserId);
$userId = (int)$cleanUserId;
$period = trim((string)($input['period'] ?? ($_GET['period'] ?? '7d')));

if ($userId <= 0) {
    $authUser = getAuthUser($pdo);
    if ($authUser && !empty($authUser['id'])) {
        $userId = (int)$authUser['id'];
    } else {
        $userId = 1;
    }
}

// 1. Fetch user data safely
$user = null;
try {
    $uStmt = $pdo->prepare("SELECT id, full_name, profile_pic, is_verified, is_monetized, created_at, followers_count FROM users WHERE id = ?");
    $uStmt->execute([$userId]);
    $user = $uStmt->fetch();
} catch (Exception $e) {
    try {
        $uStmt = $pdo->prepare("SELECT id, full_name, profile_pic, is_verified, created_at FROM users WHERE id = ?");
        $uStmt->execute([$userId]);
        $user = $uStmt->fetch();
    } catch (Exception $e2) {}
}

if (!$user) {
    try {
        $firstUser = $pdo->query("SELECT * FROM users ORDER BY id ASC LIMIT 1")->fetch();
        if ($firstUser) {
            $user = $firstUser;
            $userId = (int)$firstUser['id'];
        }
    } catch (Exception $e3) {}
}

if (!$user) {
    $user = [
        'id' => $userId > 0 ? $userId : 1,
        'full_name' => 'Creator',
        'profile_pic' => '',
        'is_verified' => 0,
        'is_monetized' => 0,
        'created_at' => date('Y-m-d H:i:s'),
        'followers_count' => 0
    ];
}

// 2. Fetch monetization settings from DB
$settings = [
    'min_followers' => 1000,
    'min_posts' => 10,
    'min_reels' => 5,
    'min_account_age_days' => 30,
    'dashboard_posts_limit' => 5,
    'revenue_share_percent' => 55
];

try {
    $sRow = $pdo->query("SELECT * FROM monetization_settings WHERE id = 1 LIMIT 1")->fetch();
    if ($sRow) {
        $settings['min_followers'] = (int)($sRow['min_followers'] ?? 1000);
        $settings['min_posts'] = (int)($sRow['min_posts'] ?? 10);
        $settings['min_reels'] = (int)($sRow['min_reels'] ?? 5);
        $settings['min_account_age_days'] = (int)($sRow['min_account_age_days'] ?? 30);
        $settings['dashboard_posts_limit'] = (int)($sRow['dashboard_posts_limit'] ?? 5);
        $settings['revenue_share_percent'] = (int)($sRow['revenue_share_percent'] ?? 55);
    }
} catch (Exception $e) {}

// 3. Calculate Date Cutoff based on selected period
$cutoff = '1970-01-01 00:00:00';
switch ($period) {
    case '24h':
        $cutoff = date('Y-m-d H:i:s', strtotime('-24 hours'));
        break;
    case '48h':
        $cutoff = date('Y-m-d H:i:s', strtotime('-48 hours'));
        break;
    case '3d':
        $cutoff = date('Y-m-d H:i:s', strtotime('-3 days'));
        break;
    case '7d':
        $cutoff = date('Y-m-d H:i:s', strtotime('-7 days'));
        break;
    case '30d':
    case '1m':
        $cutoff = date('Y-m-d H:i:s', strtotime('-30 days'));
        break;
    case 'all':
    default:
        $cutoff = '1970-01-01 00:00:00';
        break;
}

// 4. Performance Overview Queries for the Selected Period
// A. Total Views (Reels views + Post views)
$reelViews = 0;
$postViews = 0;
try {
    $rvStmt = $pdo->prepare("
        SELECT COUNT(*) 
        FROM reel_views rv 
        JOIN reels r ON rv.reel_id = r.id 
        WHERE r.user_id = ? AND rv.created_at >= ?
    ");
    $rvStmt->execute([$userId, $cutoff]);
    $reelViews = (int)$rvStmt->fetchColumn();
} catch (Exception $e) {}

try {
    $pvStmt = $pdo->prepare("
        SELECT COUNT(*) 
        FROM post_views pv 
        JOIN posts p ON pv.post_id = p.id 
        WHERE p.user_id = ? AND pv.created_at >= ?
    ");
    $pvStmt->execute([$userId, $cutoff]);
    $postViews = (int)$pvStmt->fetchColumn();
} catch (Exception $e) {}

// If no separate view events in period, sum content views published in period
if ($reelViews === 0) {
    try {
        $q = $pdo->prepare("SELECT COALESCE(SUM(views_count), 0) FROM reels WHERE user_id = ? AND created_at >= ?");
        $q->execute([$userId, $cutoff]);
        $reelViews = (int)$q->fetchColumn();
    } catch (Exception $e) {}
}
if ($postViews === 0) {
    try {
        $q = $pdo->prepare("SELECT COALESCE(SUM(views_count), 0) FROM posts WHERE user_id = ? AND created_at >= ?");
        $q->execute([$userId, $cutoff]);
        $postViews = (int)$q->fetchColumn();
    } catch (Exception $e) {}
}

$totalViews = $reelViews + $postViews;

// B. Engagement (Likes + Comments + Shares in this period)
$totalLikes = 0;
$totalComments = 0;
$totalShares = 0;

try {
    $plStmt = $pdo->prepare("SELECT COUNT(*) FROM post_likes pl JOIN posts p ON pl.post_id = p.id WHERE p.user_id = ? AND pl.created_at >= ?");
    $plStmt->execute([$userId, $cutoff]);
    $pLikes = (int)$plStmt->fetchColumn();

    $rlStmt = $pdo->prepare("SELECT COUNT(*) FROM reel_likes rl JOIN reels r ON rl.reel_id = r.id WHERE r.user_id = ? AND rl.created_at >= ?");
    $rlStmt->execute([$userId, $cutoff]);
    $rLikes = (int)$rlStmt->fetchColumn();

    $totalLikes = $pLikes + $rLikes;
} catch (Exception $e) {}

try {
    $cStmt = $pdo->prepare("
        SELECT COUNT(*) FROM comments c 
        WHERE ((c.post_id IN (SELECT id FROM posts WHERE user_id = ?)) 
            OR (c.reel_id IN (SELECT id FROM reels WHERE user_id = ?))) 
        AND c.created_at >= ?
    ");
    $cStmt->execute([$userId, $userId, $cutoff]);
    $totalComments = (int)$cStmt->fetchColumn();
} catch (Exception $e) {}

try {
    $psStmt = $pdo->prepare("SELECT COALESCE(SUM(shares_count), 0) FROM posts WHERE user_id = ? AND created_at >= ?");
    $psStmt->execute([$userId, $cutoff]);
    $pShares = (int)$psStmt->fetchColumn();

    $rsStmt = $pdo->prepare("SELECT COALESCE(SUM(shares_count), 0) FROM reels WHERE user_id = ? AND created_at >= ?");
    $rsStmt->execute([$userId, $cutoff]);
    $rShares = (int)$rsStmt->fetchColumn();

    $totalShares = $pShares + $rShares;
} catch (Exception $e) {}

$totalEngagement = $totalLikes + $totalComments + $totalShares;

// C. New Followers Gained in Period
$newFollowers = 0;
try {
    $fStmt = $pdo->prepare("SELECT COUNT(*) FROM follows WHERE following_id = ? AND created_at >= ?");
    $fStmt->execute([$userId, $cutoff]);
    $newFollowers = (int)$fStmt->fetchColumn();
} catch (Exception $e) {}

// D. Total Posts Published in Period
$totalPosts = 0;
try {
    $tpStmt = $pdo->prepare("SELECT COUNT(*) FROM posts WHERE user_id = ? AND created_at >= ?");
    $tpStmt->execute([$userId, $cutoff]);
    $totalPosts = (int)$tpStmt->fetchColumn();
} catch (Exception $e) {}

// E. Total Reels Published in Period
$totalReels = 0;
try {
    $trStmt = $pdo->prepare("SELECT COUNT(*) FROM reels WHERE user_id = ? AND created_at >= ?");
    $trStmt->execute([$userId, $cutoff]);
    $totalReels = (int)$trStmt->fetchColumn();
} catch (Exception $e) {}

// 5. Monetization Requirements (Lifetime user stats vs targets)
// Total Lifetime Followers
$lifetimeFollowers = (int)($user['followers_count'] ?? 0);
try {
    $lfStmt = $pdo->prepare("SELECT COUNT(*) FROM follows WHERE following_id = ?");
    $lfStmt->execute([$userId]);
    $dbFollowers = (int)$lfStmt->fetchColumn();
    $lifetimeFollowers = max($lifetimeFollowers, $dbFollowers);
} catch (Exception $e) {}

// Total Lifetime Posts
$lifetimePosts = 0;
try {
    $lpStmt = $pdo->prepare("SELECT COUNT(*) FROM posts WHERE user_id = ?");
    $lpStmt->execute([$userId]);
    $lifetimePosts = (int)$lpStmt->fetchColumn();
} catch (Exception $e) {}

// Total Lifetime Reels
$lifetimeReels = 0;
try {
    $lrStmt = $pdo->prepare("SELECT COUNT(*) FROM reels WHERE user_id = ?");
    $lrStmt->execute([$userId]);
    $lifetimeReels = (int)$lrStmt->fetchColumn();
} catch (Exception $e) {}

// Account Age in Days
$userCreatedAt = strtotime($user['created_at'] ?: 'now');
$accountAgeDays = (int)floor((time() - $userCreatedAt) / 86400);

// Check 4 Criteria:
$followersMet = $lifetimeFollowers >= $settings['min_followers'];
$postsMet = $lifetimePosts >= $settings['min_posts'];
$reelsMet = $lifetimeReels >= $settings['min_reels'];
$ageMet = $accountAgeDays >= $settings['min_account_age_days'];

$fulfilledCount = 0;
if ($followersMet) $fulfilledCount++;
if ($postsMet) $fulfilledCount++;
if ($reelsMet) $fulfilledCount++;
if ($ageMet) $fulfilledCount++;

$allCriteriaMet = ($fulfilledCount === 4);

// 6. Check Monetization Application Status
$appStatus = 'none'; // 'none', 'pending', 'approved', 'rejected'
$lastApp = null;
try {
    $appStmt = $pdo->prepare("SELECT * FROM monetization_requests WHERE user_id = ? ORDER BY id DESC LIMIT 1");
    $appStmt->execute([$userId]);
    $lastApp = $appStmt->fetch(PDO::FETCH_ASSOC);

    if ($lastApp) {
        $appStatus = $lastApp['status'];
    }
} catch (Exception $e) {}

$isMonetized = ((int)$user['is_monetized'] === 1) || ($appStatus === 'approved');

// 7. Recent Content for Dashboard
$limit = max(1, $settings['dashboard_posts_limit']);
$recentContent = [];
$allContent = [];

try {
    // Fetch posts
    $pList = $pdo->prepare("
        SELECT id, 'post' AS type, content, image_url, video_url, 
               views_count, likes_count, comments_count, shares_count, created_at 
        FROM posts WHERE user_id = ? 
        ORDER BY created_at DESC LIMIT 50
    ");
    $pList->execute([$userId]);
    $postsData = $pList->fetchAll(PDO::FETCH_ASSOC);

    // Fetch reels
    $rList = $pdo->prepare("
        SELECT id, 'reel' AS type, caption AS content, media_url, thumbnail_url, 
               views_count, likes_count, comments_count, shares_count, created_at 
        FROM reels WHERE user_id = ? 
        ORDER BY created_at DESC LIMIT 50
    ");
    $rList->execute([$userId]);
    $reelsData = $rList->fetchAll(PDO::FETCH_ASSOC);

    // Combine and sort by date descending
    $combined = array_merge($postsData, $reelsData);
    usort($combined, function($a, $b) {
        return strtotime($b['created_at']) - strtotime($a['created_at']);
    });

    foreach ($combined as $item) {
        $media = !empty($item['video_url']) ? $item['video_url'] : (!empty($item['image_url']) ? $item['image_url'] : ($item['media_url'] ?? ''));
        $thumb = !empty($item['thumbnail_url']) ? $item['thumbnail_url'] : $media;

        $formatted = [
            'id' => (string)$item['id'],
            'type' => $item['type'],
            'content' => $item['content'] ?: ($item['type'] === 'reel' ? 'Reel Video' : 'Post Content'),
            'mediaUrl' => $media,
            'thumbnailUrl' => $thumb,
            'viewsCount' => (int)$item['views_count'],
            'likesCount' => (int)$item['likes_count'],
            'commentsCount' => (int)$item['comments_count'],
            'sharesCount' => (int)$item['shares_count'],
            'createdAt' => $item['created_at']
        ];

        $allContent[] = $formatted;
    }

    $recentContent = array_slice($allContent, 0, $limit);
} catch (Exception $e) {}

jsonResponse(true, [
    'period' => $period,
    'user' => [
        'id' => (string)$user['id'],
        'fullName' => $user['full_name'],
        'profilePic' => $user['profile_pic'],
        'isVerified' => (bool)$user['is_verified'],
        'isMonetized' => $isMonetized
    ],
    'performance' => [
        'totalViews' => $totalViews,
        'engagement' => $totalEngagement,
        'newFollowers' => $newFollowers,
        'totalPosts' => $totalPosts,
        'totalReels' => $totalReels
    ],
    'monetization' => [
        'revenueShareText' => 'Earn ' . $settings['revenue_share_percent'] . '% revenue share from ad views on your content',
        'revenueSharePercent' => $settings['revenue_share_percent'],
        'isMonetized' => $isMonetized,
        'applicationStatus' => $appStatus,
        'allCriteriaMet' => $allCriteriaMet,
        'fulfilledCount' => $fulfilledCount,
        'totalCriteria' => 4,
        'criteria' => [
            'followers' => [
                'current' => $lifetimeFollowers,
                'required' => $settings['min_followers'],
                'isMet' => $followersMet,
                'title' => 'Followers'
            ],
            'posts' => [
                'current' => $lifetimePosts,
                'required' => $settings['min_posts'],
                'isMet' => $postsMet,
                'title' => 'Posts'
            ],
            'reels' => [
                'current' => $lifetimeReels,
                'required' => $settings['min_reels'],
                'isMet' => $reelsMet,
                'title' => 'Reels Videos'
            ],
            'accountAge' => [
                'current' => $accountAgeDays,
                'required' => $settings['min_account_age_days'],
                'isMet' => $ageMet,
                'title' => 'Account Age (Days)'
            ]
        ],
        'lastApplication' => $lastApp ? [
            'id' => (string)$lastApp['id'],
            'status' => $lastApp['status'],
            'rejectionReason' => $lastApp['rejection_reason'] ?? '',
            'createdAt' => $lastApp['created_at']
        ] : null
    ],
    'recentContent' => $recentContent,
    'allContent' => $allContent
], 'Dashboard data retrieved successfully.');
