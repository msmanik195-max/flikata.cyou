<?php
/**
 * Send Message Endpoint (Supports Text, Cloudflare R2 Images, Videos, Audio Call notice, and Replies)
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$user = getAuthUser($pdo);
$myId = $user ? (int)$user['id'] : (!empty($input['sender_id']) ? (int)$input['sender_id'] : (!empty($input['user_id']) ? (int)$input['user_id'] : 0));

// Check Admin App Toggle: allow_chat
try {
    $fsStmt = $pdo->query("SELECT allow_chat FROM app_feature_settings WHERE id = 1");
    $allowChat = $fsStmt ? $fsStmt->fetchColumn() : 1;
    if ($allowChat !== false && (int)$allowChat === 0) {
        jsonResponse(false, null, 'চ্যাট অপশন সাময়িকভাবে বন্ধ আছে');
    }
} catch (Exception $e) {}

if (!$myId) {
    jsonResponse(false, null, 'Authentication or valid sender ID required.');
}

$convId       = !empty($input['conversation_id']) ? (int)$input['conversation_id'] : 0;
$partnerId    = !empty($input['partner_id']) ? (int)$input['partner_id'] : 0;
$text         = trim($input['text'] ?? ($input['message'] ?? ''));
$imageUrl     = trim($input['image_url'] ?? '');
$videoUrl     = trim($input['video_url'] ?? '');
$replyToId    = !empty($input['reply_to_id']) ? (int)$input['reply_to_id'] : null;
$isCallNotice = !empty($input['is_call_notice']) ? 1 : 0;
$bucketId     = !empty($input['bucket_id']) ? (int)$input['bucket_id'] : null;

// Resolve conversation
if (!$convId && $partnerId) {
    $min = min($myId, $partnerId);
    $max = max($myId, $partnerId);
    $stmt = $pdo->prepare("SELECT id FROM conversations WHERE user_one_id = ? AND user_two_id = ? LIMIT 1");
    $stmt->execute([$min, $max]);
    $conv = $stmt->fetch();
    if ($conv) {
        $convId = (int)$conv['id'];
    } else {
        $cIns = $pdo->prepare("INSERT INTO conversations (user_one_id, user_two_id) VALUES (?, ?)");
        $cIns->execute([$min, $max]);
        $convId = (int)$pdo->lastInsertId();
    }
}

if (!$convId) {
    jsonResponse(false, null, 'Valid conversation ID or partner ID required.');
}

// Find receiver ID
$stmt = $pdo->prepare("SELECT user_one_id, user_two_id FROM conversations WHERE id = ?");
$stmt->execute([$convId]);
$cRow = $stmt->fetch();
if (!$cRow) {
    jsonResponse(false, null, 'Conversation does not exist.');
}

$receiverId = ($cRow['user_one_id'] == $myId) ? $cRow['user_two_id'] : $cRow['user_one_id'];

// Insert message
$stmt = $pdo->prepare("
    INSERT INTO messages (conversation_id, sender_id, receiver_id, reply_to_id, text, image_url, video_url, bucket_id, is_call_notice)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
");
$stmt->execute([
    $convId,
    $myId,
    $receiverId,
    $replyToId,
    $text ?: null,
    $imageUrl ?: null,
    $videoUrl ?: null,
    $bucketId,
    $isCallNotice
]);

$messageId = $pdo->lastInsertId();

// Update conversation last message snippet
$snippet = $text ?: ($imageUrl ? '📷 Photo' : ($videoUrl ? '🎬 Video' : ($isCallNotice ? '📞 Audio Call' : 'New Message')));
$uStmt = $pdo->prepare("UPDATE conversations SET last_message = ?, last_message_time = NOW() WHERE id = ?");
$uStmt->execute([$snippet, $convId]);

jsonResponse(true, [
    'id'              => (string)$messageId,
    'conversation_id' => $convId,
    'text'            => $text,
    'imageUrl'        => $imageUrl ?: null,
    'videoUrl'        => $videoUrl ?: null,
    'isMe'            => true,
    'time'            => date('h:i A')
], 'Message sent successfully!');
