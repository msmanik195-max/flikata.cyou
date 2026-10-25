<?php
/**
 * Update Profile Details
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
    jsonResponse(false, null, 'Authentication required.');
}

// Ensure about_settings column exists
try {
    $pdo->exec("ALTER TABLE users ADD COLUMN `about_settings` TEXT DEFAULT NULL");
} catch (Exception $e) {}

$fullName   = trim($input['full_name'] ?? $user['full_name']);
$bio        = trim($input['bio'] ?? $user['bio']);
$occupation = trim($input['occupation'] ?? $user['occupation']);
$location   = trim($input['location'] ?? $user['location']);
$gender     = trim($input['gender'] ?? ($user['gender'] ?? ''));
$dob        = trim($input['dob'] ?? ($user['dob'] ?? ''));
$profilePic = trim($input['profile_pic'] ?? $user['profile_pic']);
$coverPic   = trim($input['cover_pic'] ?? $user['cover_pic']);

$aboutSettingsJson = null;
if (isset($input['about_settings'])) {
    if (is_array($input['about_settings'])) {
        $aboutSettingsJson = json_encode($input['about_settings']);
    } else {
        $aboutSettingsJson = (string)$input['about_settings'];
    }
}

$stmt = $pdo->prepare("
    UPDATE users 
    SET full_name = ?, bio = ?, occupation = ?, location = ?, gender = ?, dob = ?, profile_pic = ?, cover_pic = ?,
        about_settings = COALESCE(?, about_settings)
    WHERE id = ?
");
$stmt->execute([
    $fullName,
    $bio,
    $occupation,
    $location,
    $gender ?: null,
    $dob ?: null,
    $profilePic ?: null,
    $coverPic ?: null,
    $aboutSettingsJson,
    $user['id']
]);

$stmt = $pdo->prepare("SELECT id, full_name, email, phone, profile_pic, cover_pic, bio, occupation, location, gender, dob, is_verified, auth_token FROM users WHERE id = ?");
$stmt->execute([$user['id']]);
$updatedUser = $stmt->fetch();

jsonResponse(true, $updatedUser, 'Profile updated successfully!');
