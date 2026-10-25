<?php
/**
 * Admin AJAX Action Handler
 * Flikata Admin Control Panel
 */
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/includes/db.php';

// Ensure user is authorized
if (empty($_SESSION['admin_logged_in']) || empty($_SESSION['admin_id'])) {
    echo json_encode(['success' => false, 'message' => 'Unauthorized access.']);
    exit;
}

$action = trim($_POST['action'] ?? $_GET['action'] ?? '');

switch ($action) {
    case 'delete_post':
        $postId = (int)($_POST['post_id'] ?? 0);
        if ($postId <= 0) {
            echo json_encode(['success' => false, 'message' => 'Invalid post ID.']);
            exit;
        }

        try {
            // Check post
            $stmt = $pdo->prepare("SELECT * FROM posts WHERE id = ?");
            $stmt->execute([$postId]);
            $post = $stmt->fetch();

            if (!$post) {
                echo json_encode(['success' => true, 'message' => 'Post already deleted.']);
                exit;
            }

            // Clean child relations
            $pdo->prepare("DELETE FROM post_likes WHERE post_id = ?")->execute([$postId]);
            $pdo->prepare("DELETE FROM post_reactions WHERE post_id = ?")->execute([$postId]);
            $pdo->prepare("DELETE FROM comments WHERE post_id = ?")->execute([$postId]);

            // If it had a video URL, clean associated reels
            if (!empty($post['video_url'])) {
                $pdo->prepare("DELETE FROM reels WHERE media_url = ?")->execute([$post['video_url']]);
            }

            // Permanently delete post
            $del = $pdo->prepare("DELETE FROM posts WHERE id = ?");
            $del->execute([$postId]);

            echo json_encode(['success' => true, 'message' => 'পোস্টটি ডাটাবেজ থেকে স্থায়ীভাবে মুছে ফেলা হয়েছে।']);
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => 'Delete failed: ' . $e->getMessage()]);
        }
        break;

    case 'delete_story':
        $storyId = (int)($_POST['story_id'] ?? 0);
        if ($storyId <= 0) {
            echo json_encode(['success' => false, 'message' => 'Invalid story ID.']);
            exit;
        }

        try {
            $del = $pdo->prepare("DELETE FROM stories WHERE id = ?");
            $del->execute([$storyId]);
            echo json_encode(['success' => true, 'message' => 'স্টোরিটি স্থায়ীভাবে মুছে ফেলা হয়েছে।']);
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => 'Delete failed: ' . $e->getMessage()]);
        }
        break;

    case 'toggle_ban':
        $userId = (int)($_POST['user_id'] ?? 0);
        $newStatus = (int)($_POST['is_banned'] ?? 0);

        if ($userId <= 0) {
            echo json_encode(['success' => false, 'message' => 'Invalid user ID.']);
            exit;
        }

        try {
            if ($newStatus === 1) {
                // When banning user, clear auth_token to force immediate logout
                $stmt = $pdo->prepare("UPDATE users SET is_banned = 1, auth_token = NULL, is_online = 0 WHERE id = ?");
            } else {
                $stmt = $pdo->prepare("UPDATE users SET is_banned = 0 WHERE id = ?");
            }
            $stmt->execute([$userId]);

            echo json_encode(['success' => true, 'message' => 'User status updated successfully.']);
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => 'Operation failed: ' . $e->getMessage()]);
        }
        break;

    case 'toggle_verify':
        $userId = (int)($_POST['user_id'] ?? 0);
        $newStatus = (int)($_POST['is_verified'] ?? 0);

        if ($userId <= 0) {
            echo json_encode(['success' => false, 'message' => 'Invalid user ID.']);
            exit;
        }

        try {
            $stmt = $pdo->prepare("UPDATE users SET is_verified = ? WHERE id = ?");
            $stmt->execute([$newStatus ? 1 : 0, $userId]);

            echo json_encode([
                'success' => true, 
                'message' => $newStatus ? 'ইউজারের ফেসবুক ব্লু ভেরিফিকেশন ব্যাচ চালু করা হয়েছে।' : 'ভেরিফিকেশন ব্যাচ বন্ধ করা হয়েছে।'
            ]);
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => 'Operation failed: ' . $e->getMessage()]);
        }
        break;

    case 'delete_user':
        $userId = (int)($_POST['user_id'] ?? 0);
        if ($userId <= 0) {
            echo json_encode(['success' => false, 'message' => 'Invalid user ID.']);
            exit;
        }

        try {
            // Delete user's posts, reactions, comments, stories, reels, messages
            $pdo->prepare("DELETE FROM posts WHERE user_id = ?")->execute([$userId]);
            $pdo->prepare("DELETE FROM stories WHERE user_id = ?")->execute([$userId]);
            $pdo->prepare("DELETE FROM reels WHERE user_id = ?")->execute([$userId]);
            $pdo->prepare("DELETE FROM comments WHERE user_id = ?")->execute([$userId]);
            $pdo->prepare("DELETE FROM post_likes WHERE user_id = ?")->execute([$userId]);
            $pdo->prepare("DELETE FROM post_reactions WHERE user_id = ?")->execute([$userId]);
            $pdo->prepare("DELETE FROM notifications WHERE user_id = ? OR from_user_id = ?")->execute([$userId, $userId]);
            $pdo->prepare("DELETE FROM messages WHERE sender_id = ? OR receiver_id = ?")->execute([$userId, $userId]);
            $pdo->prepare("DELETE FROM conversations WHERE user_one_id = ? OR user_two_id = ?")->execute([$userId, $userId]);

            $delUser = $pdo->prepare("DELETE FROM users WHERE id = ?");
            $delUser->execute([$userId]);

            echo json_encode(['success' => true, 'message' => 'ইউজারটি ডাটাবেজ থেকে সম্পূর্ণ মুছে ফেলা হয়েছে।']);
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => 'User delete failed: ' . $e->getMessage()]);
        }
        break;

    case 'edit_user':
        $userId = (int)($_POST['user_id'] ?? 0);
        $fullName = trim($_POST['full_name'] ?? '');
        $email = trim($_POST['email'] ?? '');
        $phone = trim($_POST['phone'] ?? '');
        $occupation = trim($_POST['occupation'] ?? '');
        $location = trim($_POST['location'] ?? '');
        $bio = trim($_POST['bio'] ?? '');
        $isVerified = !empty($_POST['is_verified']) ? 1 : 0;

        if ($userId <= 0 || empty($fullName)) {
            echo json_encode(['success' => false, 'message' => 'নাম এবং ভ্যালিড আইডি প্রয়োজন।']);
            exit;
        }

        try {
            $up = $pdo->prepare("
                UPDATE users 
                SET full_name = ?, email = ?, phone = ?, occupation = ?, location = ?, bio = ?, is_verified = ?
                WHERE id = ?
            ");
            $up->execute([$fullName, $email ?: null, $phone ?: null, $occupation, $location, $bio, $isVerified, $userId]);

            echo json_encode(['success' => true, 'message' => 'ইউজার তথ্য সফলভাবে আপডেট হয়েছে।']);
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => 'Update failed: ' . $e->getMessage()]);
        }
        break;

    case 'send_single_notification':
        $userId = (int)($_POST['user_id'] ?? 0);
        $title = trim($_POST['title'] ?? '');
        $message = trim($_POST['message'] ?? '');
        $type = trim($_POST['type'] ?? 'general');

        if ($userId <= 0 || empty($title) || empty($message)) {
            echo json_encode(['success' => false, 'message' => 'প্রাপক ইউজার, শিরোনাম ও বার্তা ফিল্ড আবশ্যক।']);
            exit;
        }

        try {
            $ins = $pdo->prepare("
                INSERT INTO notifications (user_id, from_user_id, title, message, type, is_read, created_at)
                VALUES (?, NULL, ?, ?, ?, 0, NOW())
            ");
            $ins->execute([$userId, $title, $message, $type]);

            echo json_encode(['success' => true, 'message' => 'একক নোটিফিকেশন সফলভাবে পাঠানো হয়েছে।']);
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => 'Notification failed: ' . $e->getMessage()]);
        }
        break;

    case 'update_user_limits':
        $userId = (int)($_POST['user_id'] ?? 0);
        if ($userId <= 0) {
            echo json_encode(['success' => false, 'message' => 'Invalid user ID.']);
            exit;
        }

        // null means inherit default, 0 means blocked, positive int means custom limit
        $customPageLimit  = (isset($_POST['custom_page_limit']) && $_POST['custom_page_limit'] !== '' && $_POST['custom_page_limit'] !== '-1') ? max(0, (int)$_POST['custom_page_limit']) : null;
        $customTextLimit  = (isset($_POST['custom_text_limit']) && $_POST['custom_text_limit'] !== '' && $_POST['custom_text_limit'] !== '-1') ? max(0, (int)$_POST['custom_text_limit']) : null;
        $customImageLimit = (isset($_POST['custom_image_limit']) && $_POST['custom_image_limit'] !== '' && $_POST['custom_image_limit'] !== '-1') ? max(0, (int)$_POST['custom_image_limit']) : null;
        $customVideoLimit = (isset($_POST['custom_video_limit']) && $_POST['custom_video_limit'] !== '' && $_POST['custom_video_limit'] !== '-1') ? max(0, (int)$_POST['custom_video_limit']) : null;
        $customStoryLimit = (isset($_POST['custom_story_limit']) && $_POST['custom_story_limit'] !== '' && $_POST['custom_story_limit'] !== '-1') ? max(0, (int)$_POST['custom_story_limit']) : null;

        try {
            $up = $pdo->prepare("
                UPDATE users 
                SET custom_page_limit = ?,
                    custom_text_limit = ?,
                    custom_image_limit = ?,
                    custom_video_limit = ?,
                    custom_story_limit = ?
                WHERE id = ?
            ");
            $up->execute([$customPageLimit, $customTextLimit, $customImageLimit, $customVideoLimit, $customStoryLimit, $userId]);

            echo json_encode(['success' => true, 'message' => 'ইউজারের কাস্টম লিমিট সফলভাবে সংরক্ষণ করা হয়েছে।']);
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => 'Update failed: ' . $e->getMessage()]);
        }
        break;

    case 'save_users_per_page':
        $perPage = max(1, min(1000, (int)($_POST['per_page'] ?? 20)));
        try {
            $pdo->prepare("UPDATE app_feature_settings SET users_per_page = ? WHERE id = 1")->execute([$perPage]);
            echo json_encode(['success' => true, 'message' => 'প্রতি পেজে ইউজার সংখ্যা সফলভাবে সেভ করা হয়েছে!', 'per_page' => $perPage]);
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => 'Failed to save: ' . $e->getMessage()]);
        }
        break;

    default:
        echo json_encode(['success' => false, 'message' => 'Invalid action request.']);
        break;
}
