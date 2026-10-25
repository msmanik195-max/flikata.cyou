<?php
/**
 * User Registration Endpoint
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$firstName    = trim($input['first_name'] ?? '');
$lastName     = trim($input['last_name'] ?? '');
$fullName     = trim($input['full_name'] ?? trim("$firstName $lastName"));
$emailOrPhone = trim($input['email_or_phone'] ?? '');
$accountType  = trim($input['account_type'] ?? '');
$email        = trim($input['email'] ?? ($accountType === 'email' || strpos($emailOrPhone, '@') !== false ? $emailOrPhone : ''));
$phone        = trim($input['phone'] ?? ($accountType === 'phone' || (strpos($emailOrPhone, '@') === false && !empty($emailOrPhone)) ? $emailOrPhone : ''));
$password     = trim($input['password'] ?? '');
$gender       = trim($input['gender'] ?? 'Male');
$dob          = trim($input['dob'] ?? null);

// Check Admin App Toggle: allow_registration
try {
    $fsStmt = $pdo->query("SELECT allow_registration FROM app_feature_settings WHERE id = 1");
    $allowReg = $fsStmt ? $fsStmt->fetchColumn() : 1;
    if ($allowReg !== false && (int)$allowReg === 0) {
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode([
            'status'  => 'error',
            'success' => false,
            'message' => 'রেজিস্ট্রেশন সাময়িকভাবে বন্ধ আছে'
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }
} catch (Exception $e) {}

if (!$fullName || (!$email && !$phone) || !$password) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'status'  => 'error',
        'success' => false,
        'message' => 'Name, email/phone, and password are required.'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if (strlen($password) < 6) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'status'  => 'error',
        'success' => false,
        'message' => 'Password must be at least 6 characters.'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Check duplicate
if ($email) {
    $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ? LIMIT 1");
    $stmt->execute([$email]);
    if ($stmt->fetch()) {
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode([
            'status'  => 'error',
            'success' => false,
            'message' => 'An account with this email already exists.'
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }
}

if ($phone) {
    $stmt = $pdo->prepare("SELECT id FROM users WHERE phone = ? LIMIT 1");
    $stmt->execute([$phone]);
    if ($stmt->fetch()) {
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode([
            'status'  => 'error',
            'success' => false,
            'message' => 'An account with this phone number already exists.'
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }
}

$passwordHash = password_hash($password, PASSWORD_BCRYPT);
$authToken = bin2hex(random_bytes(32));

$stmt = $pdo->prepare("
    INSERT INTO users (full_name, email, phone, password_hash, gender, dob, auth_token, is_online)
    VALUES (?, ?, ?, ?, ?, ?, ?, 1)
");
$stmt->execute([
    $fullName,
    $email ?: null,
    $phone ?: null,
    $passwordHash,
    $gender,
    $dob ?: null,
    $authToken
]);

$userId = $pdo->lastInsertId();

$stmt = $pdo->prepare("SELECT id, full_name, email, phone, profile_pic, cover_pic, bio, occupation, location, is_verified, auth_token, created_at FROM users WHERE id = ?");
$stmt->execute([$userId]);
$user = $stmt->fetch();

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
    'message' => 'Registration successful!',
    'user'    => $formattedUser,
    'token'   => $authToken,
    'data'    => $formattedUser
], JSON_UNESCAPED_UNICODE);
exit;
