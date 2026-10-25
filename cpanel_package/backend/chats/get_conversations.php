<?php
/**
 * Get User Chat Conversations & Active Contacts
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$user = getAuthUser($pdo);
$myId = !empty($_GET['user_id']) ? (int)$_GET['user_id'] : ($user ? (int)$user['id'] : 0);

if (!$myId) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([], JSON_UNESCAPED_UNICODE);
    exit;
}

// Check allow_chat
try {
    $fsStmt = $pdo->query("SELECT allow_chat FROM app_feature_settings WHERE id = 1");
    $allowChat = $fsStmt ? $fsStmt->fetchColumn() : 1;
    if ($allowChat !== false && (int)$allowChat === 0) {
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode([], JSON_UNESCAPED_UNICODE);
        exit;
    }
} catch (Exception $e) {}

// 1. Fetch conversations involving the user
$stmt = $pdo->prepare("
    SELECT 
        c.id AS conversation_id,
        c.last_message,
        c.last_message_time,
        CASE WHEN c.user_one_id = ? THEN c.user_two_id ELSE c.user_one_id END AS partner_id
    FROM conversations c
    WHERE c.user_one_id = ? OR c.user_two_id = ?
    ORDER BY c.last_message_time DESC
");
$stmt->execute([$myId, $myId, $myId]);
$convs = $stmt->fetchAll();

$formatted = [];
foreach ($convs as $cv) {
    $uStmt = $pdo->prepare("SELECT id, full_name, profile_pic, is_verified, is_online, updated_at FROM users WHERE id = ?");
    $uStmt->execute([$cv['partner_id']]);
    $partner = $uStmt->fetch();

    if ($partner) {
        $unreadStmt = $pdo->prepare("
            SELECT COUNT(*) FROM messages 
            WHERE conversation_id = ? AND receiver_id = ? AND is_read = 0
        ");
        $unreadStmt->execute([$cv['conversation_id'], $myId]);
        $unreadCount = (int)$unreadStmt->fetchColumn();

        $secondsAgo = !empty($partner['updated_at']) ? (time() - strtotime($partner['updated_at'])) : 99999;
        $isOnline = ((int)($partner['is_online'] ?? 0) === 1 && $secondsAgo <= 60);

        $formatted[] = [
            'id'             => (string)$cv['conversation_id'],
            'threadId'       => (string)$cv['conversation_id'],
            'senderId'       => (string)$partner['id'],
            'senderName'     => $partner['full_name'],
            'senderAvatar'   => $partner['profile_pic'] ?: '',
            'isVerified'     => (bool)$partner['is_verified'],
            'isOnline'       => (bool)$isOnline,
            'message'        => $cv['last_message'] ?: 'Conversation started',
            'timestamp'      => $cv['last_message_time'],
            'isVoiceMessage' => false,
            'isMissedCall'   => false,
            'unreadCount'    => $unreadCount
        ];
    }
}

// 2. Fetch Active Online Users (for top serial list)
$actStmt = $pdo->prepare("
    SELECT id, full_name, profile_pic, is_verified 
    FROM users 
    WHERE is_online = 1 AND id != ? 
    ORDER BY updated_at DESC 
    LIMIT 20
");
$actStmt->execute([$myId]);
$activeUsers = array_map(function($u) {
    return [
        'id'         => (string)$u['id'],
        'name'       => $u['full_name'],
        'avatar'     => $u['profile_pic'] ?: '',
        'isVerified' => (bool)$u['is_verified']
    ];
}, $actStmt->fetchAll());

header('Content-Type: application/json; charset=utf-8');
echo json_encode($formatted, JSON_UNESCAPED_UNICODE);
exit;
