<?php
/**
 * User Login Endpoint
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$identifier = trim($input['identifier'] ?? $input['email_or_phone'] ?? $input['email'] ?? $input['phone'] ?? '');
$password   = trim($input['password'] ?? '');

// Check Admin App Toggle: allow_login
try {
    $fsStmt = $pdo->query("SELECT allow_login FROM app_feature_settings WHERE id = 1");
    $allowLogin = $fsStmt ? $fsStmt->fetchColumn() : 1;
    if ($allowLogin !== false && (int)$allowLogin === 0) {
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode([
            'status'  => 'error',
            'success' => false,
            'message' => 'লগইন সাময়িকভাবে বন্ধ আছে'
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }
} catch (Exception $e) {}

if (!$identifier || !$password) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'status'  => 'error',
        'success' => false,
        'message' => 'Email/Phone and password are required.'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$stmt = $pdo->prepare("SELECT * FROM users WHERE email = ? OR phone = ? LIMIT 1");
$stmt->execute([$identifier, $identifier]);
$user = $stmt->fetch();

if (!$user || !password_verify($password, $user['password_hash'])) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'status'  => 'error',
        'success' => false,
        'message' => 'Invalid email/phone or password.'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Check if user is banned by admin
if (!empty($user['is_banned'])) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'status'  => 'error',
        'success' => false,
        'message' => 'Your account has been suspended by the administrator. Please contact support.'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Generate new auth token & set online
$newToken = bin2hex(random_bytes(32));
$stmt = $pdo->prepare("UPDATE users SET auth_token = ?, is_online = 1 WHERE id = ?");
$stmt->execute([$newToken, $user['id']]);

unset($user['password_hash']);
$user['auth_token'] = $newToken;

$parts = explode(' ', trim($user['full_name']));
$firstName = $parts[0] ?? $user['full_name'];
$lastName = count($parts) > 1 ? implode(' ', array_slice($parts, 1)) : '';

$formattedUser = [
    'id'             => (string)$user['id'],
    'first_name'     => $firstName,
    'last_name'      => $lastName,
    'email_or_phone' => $user['email'] ?: ($user['phone'] ?: ''),
    'profile_pic'    => $user['profile_pic'],
    'cover_pic'      => $user['cover_pic'],
    'is_verified'    => (bool)$user['is_verified'],
    'bio'            => $user['bio'],
    'followersCount' => 0,
    'followingCount' => 0,
    'postsCount'     => 0,
    'joinedDate'     => 'Joined ' . date('F Y', strtotime($user['created_at']))
];

header('Content-Type: application/json; charset=utf-8');
echo json_encode([
    'success' => true,
    'status'  => 'success',
    'message' => 'Login successful!',
    'user'    => $formattedUser,
    'token'   => $newToken,
    'data'    => $formattedUser
], JSON_UNESCAPED_UNICODE);
exit;
