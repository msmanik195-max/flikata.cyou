<?php
/**
 * Admin Settings Page
 * Change Admin Password & Configure System Variables
 * (Story Expiry, Reel Max Duration, Leaderboard Limit, Max Pages per Profile)
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'এডমিন সেটিংস - Flikata Admin';
$pageHeading = 'এডমিন ও সিস্টেম সেটিংস';
$pageSubheading = 'পাসওয়ার্ড পরিবর্তন, লিডারবোর্ড লিমিট, স্টোরি এক্সপায়ার, পেইজ লিমিট ও অ্যাপ কনফিগারেশন';

$successMsg = '';
$errorMsg = '';

// Handle Password Change
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['change_password'])) {
    $currentPass = trim($_POST['current_password'] ?? '');
    $newPass = trim($_POST['new_password'] ?? '');
    $confirmPass = trim($_POST['confirm_password'] ?? '');

    if (empty($currentPass) || empty($newPass) || empty($confirmPass)) {
        $errorMsg = 'সকল পাসওয়ার্ড ফিল্ড পূরণ করা আবশ্যক।';
    } elseif ($newPass !== $confirmPass) {
        $errorMsg = 'নতুন পাসওয়ার্ড এবং নিশ্চিতকরণ পাসওয়ার্ড মেলেনি!';
    } elseif (strlen($newPass) < 6) {
        $errorMsg = 'নতুন পাসওয়ার্ড ন্যূনতম ৬ অক্ষরের হতে হবে।';
    } else {
        $stmt = $pdo->prepare("SELECT * FROM admins WHERE id = ? LIMIT 1");
        $stmt->execute([$_SESSION['admin_id']]);
        $adm = $stmt->fetch();

        if ($adm && (password_verify($currentPass, $adm['password_hash']) || $currentPass === $adm['password_hash'])) {
            $newHash = password_hash($newPass, PASSWORD_BCRYPT);
            $up = $pdo->prepare("UPDATE admins SET password_hash = ? WHERE id = ?");
            $up->execute([$newHash, $adm['id']]);
            $successMsg = 'এডমিন পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে!';
        } else {
            $errorMsg = 'বর্তমান পাসওয়ার্ডটি সঠিক নয়!';
        }
    }
}

// Handle App Settings (Story expiry hours, Reel seconds, Leaderboard limit, Max pages per user)
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['update_app_settings'])) {
    $expiryHours = max(1, (int)($_POST['expiry_hours'] ?? 24));
    $maxDuration = max(5, (int)($_POST['max_duration'] ?? 60));
    $leaderboardLimit = max(1, (int)($_POST['leaderboard_user_limit'] ?? 20));
    $maxPages = max(1, (int)($_POST['max_pages_per_user'] ?? 5));

    try {
        // 1. Story settings
        $pdo->prepare("UPDATE story_settings SET expiry_hours = ? WHERE id = 1")->execute([$expiryHours]);

        // 2. Reel settings
        $pdo->prepare("UPDATE reel_settings SET max_duration_seconds = ? WHERE id = 1")->execute([$maxDuration]);

        // 3. Leaderboard settings
        $pdo->exec("CREATE TABLE IF NOT EXISTS `leaderboard_settings` (
            `id` INT PRIMARY KEY DEFAULT 1,
            `user_limit` INT NOT NULL DEFAULT 20,
            `description` VARCHAR(255) DEFAULT 'Max users displayed on Leaderboard',
            `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

        $pdo->prepare("INSERT INTO leaderboard_settings (id, user_limit) VALUES (1, ?) ON DUPLICATE KEY UPDATE user_limit = ?")->execute([$leaderboardLimit, $leaderboardLimit]);

        // 4. Page settings
        $pdo->exec("CREATE TABLE IF NOT EXISTS `page_settings` (
            `id` INT PRIMARY KEY DEFAULT 1,
            `max_pages_per_user` INT NOT NULL DEFAULT 5,
            `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

        $pdo->prepare("INSERT INTO page_settings (id, max_pages_per_user) VALUES (1, ?) ON DUPLICATE KEY UPDATE max_pages_per_user = ?")->execute([$maxPages, $maxPages]);

        $successMsg = 'অ্যাপ্লিকেশন ও সিস্টেম কনফিগারেশন সফলভাবে আপডেট করা হয়েছে!';
    } catch (Exception $e) {
        $errorMsg = 'কনফিগারেশন সেভ করতে ব্যর্থ: ' . $e->getMessage();
    }
}

// Fetch current app settings
$currExpiry = 24;
try {
    $currExpiry = (int)$pdo->query("SELECT expiry_hours FROM story_settings WHERE id = 1")->fetchColumn() ?: 24;
} catch (Exception $e) {}

$currReelSec = 60;
try {
    $currReelSec = (int)$pdo->query("SELECT max_duration_seconds FROM reel_settings WHERE id = 1")->fetchColumn() ?: 60;
} catch (Exception $e) {}

$currLeaderboardLimit = 20;
try {
    $stmtLb = $pdo->query("SELECT user_limit FROM leaderboard_settings WHERE id = 1 LIMIT 1");
    $lbRow = $stmtLb ? $stmtLb->fetchColumn() : null;
    if ($lbRow && (int)$lbRow > 0) {
        $currLeaderboardLimit = (int)$lbRow;
    }
} catch (Exception $e) {}

$currMaxPages = 5;
try {
    $stmtPg = $pdo->query("SELECT max_pages_per_user FROM page_settings WHERE id = 1 LIMIT 1");
    $pgRow = $stmtPg ? $stmtPg->fetchColumn() : null;
    if ($pgRow && (int)$pgRow > 0) {
        $currMaxPages = (int)$pgRow;
    }
} catch (Exception $e) {}

require_once __DIR__ . '/includes/header.php';
?>

<div class="row g-4">
    <!-- Change Admin Password Card -->
    <div class="col-12 col-lg-5">
        <div class="card-custom h-100">
            <h6 class="fw-bold text-dark mb-1"><i class="bi bi-shield-lock-fill text-primary me-2"></i>এডমিন পাসওয়ার্ড পরিবর্তন</h6>
            <small class="text-muted d-block mb-3">লগইন ক্রেডেনশিয়াল সুরক্ষিত রাখতে নিয়মিত পাসওয়ার্ড পরিবর্তন করুন</small>

            <?php if (!empty($successMsg)): ?>
                <div class="alert alert-success border-0 rounded-3 py-2 px-3 mb-3 small"><?= htmlspecialchars($successMsg) ?></div>
            <?php endif; ?>
            <?php if (!empty($errorMsg)): ?>
                <div class="alert alert-danger border-0 rounded-3 py-2 px-3 mb-3 small"><?= htmlspecialchars($errorMsg) ?></div>
            <?php endif; ?>

            <form method="POST" action="settings.php">
                <input type="hidden" name="change_password" value="1">

                <div class="mb-3">
                    <label class="form-label small fw-bold text-secondary">এডমিন ইমেইল</label>
                    <input type="text" class="form-control bg-light rounded-3" value="<?= htmlspecialchars($_SESSION['admin_email'] ?? 'admin@gmail.com') ?>" readonly>
                </div>

                <div class="mb-3">
                    <label class="form-label small fw-bold text-secondary">বর্তমান পাসওয়ার্ড</label>
                    <input type="password" name="current_password" class="form-control rounded-3" placeholder="বর্তমান পাসওয়ার্ড দিন" required>
                </div>

                <div class="mb-3">
                    <label class="form-label small fw-bold text-secondary">নতুন পাসওয়ার্ড</label>
                    <input type="password" name="new_password" class="form-control rounded-3" placeholder="নতুন শক্তিশালী পাসওয়ার্ড দিন" required>
                </div>

                <div class="mb-4">
                    <label class="form-label small fw-bold text-secondary">নতুন পাসওয়ার্ড নিশ্চিত করুন</label>
                    <input type="password" name="confirm_password" class="form-control rounded-3" placeholder="পুনরায় নতুন পাসওয়ার্ড দিন" required>
                </div>

                <button type="submit" class="btn btn-primary-custom w-100">
                    <i class="bi bi-check2-circle me-1"></i> পাসওয়ার্ড আপডেট করুন
                </button>
            </form>
        </div>
    </div>

    <!-- App System Configurations Card -->
    <div class="col-12 col-lg-7">
        <div class="card-custom h-100">
            <h6 class="fw-bold text-dark mb-1"><i class="bi bi-sliders text-info me-2"></i>অ্যাপ্লিকেশন ও ডাটাবেজ কনফিগারেশন</h6>
            <small class="text-muted d-block mb-3">লিডারবোর্ড, স্টোরি এক্সপায়ার, পেইজ তৈরির সীমা এবং রিলস নিয়ন্ত্রণ</small>

            <form method="POST" action="settings.php">
                <input type="hidden" name="update_app_settings" value="1">

                <!-- 1. Leaderboard User Limit -->
                <div class="p-3 bg-light rounded-3 mb-3 border">
                    <label class="form-label small fw-bold text-dark mb-1">
                        <i class="bi bi-trophy-fill text-warning me-1"></i> লিডার বোর্ডে মোট কতজন ইউজার শো করবে (Leaderboard User Limit)
                    </label>
                    <div class="input-group mb-1">
                        <input type="number" name="leaderboard_user_limit" class="form-control rounded-3" value="<?= $currLeaderboardLimit ?>" min="1" step="1" required>
                        <span class="input-group-text bg-white border">জন ইউজার (Users)</span>
                    </div>
                    <small class="text-muted d-block">ডিফল্ট ২০ জন। আপনি চাইলে ২০, ৩০, ৪০, ৫০, ১০০, ২০০ বা আপনার মনের মত যেকোনো সংখ্যা দিতে পারেন। লিডারবোর্ডে শুধুমাত্র প্রকৃত রিয়েল ইউজাররাই কাউন্ট হবে এবং তাদের এনগেজমেন্ট র‍্যাঙ্কিং অনুযায়ী শো করবে।</small>
                </div>

                <!-- 2. Story Expiry Hours (Unlimited Supported) -->
                <div class="p-3 bg-light rounded-3 mb-3 border">
                    <label class="form-label small fw-bold text-dark mb-1">
                        <i class="bi bi-clock-history text-primary me-1"></i> স্টোরিজ মেয়াদ / অটোমেটিক রিমুভ সময়সীমা (ঘন্টায় / Hours)
                    </label>
                    <div class="input-group mb-1">
                        <input type="number" name="expiry_hours" class="form-control rounded-3" value="<?= $currExpiry ?>" min="1" step="1" required>
                        <span class="input-group-text bg-white border">ঘন্টা (Hours)</span>
                    </div>
                    <small class="text-muted d-block">ডিফল্ট ২৪ ঘন্টা। এখানে কোনো ১৬৮ ঘন্টার সীমাবদ্ধতা নেই; আপনি ইচ্ছেমতো যেকোনো ঘন্টা (যেমন ৪৮, ৭২, ১৬৮, ৭২০ বা দীর্ঘমেয়াদে ৯৯৯৯৯) নির্ধারণ করতে পারবেন।</small>
                </div>

                <!-- 3. Max Pages Per Profile -->
                <div class="p-3 bg-light rounded-3 mb-3 border">
                    <label class="form-label small fw-bold text-dark mb-1">
                        <i class="bi bi-flag-fill text-success me-1"></i> একটি প্রোফাইল থেকে সর্বোচ্চ পেইজ ক্রিয়েট করার সীমা (Max Pages Per Profile)
                    </label>
                    <div class="input-group mb-1">
                        <input type="number" name="max_pages_per_user" class="form-control rounded-3" value="<?= $currMaxPages ?>" min="1" step="1" required>
                        <span class="input-group-text bg-white border">টি পেইজ (Pages)</span>
                    </div>
                    <small class="text-muted d-block">ডিফল্ট ৫ টি। প্রতিটি সাধারণ ইউজার প্রোফাইল থেকে সর্বোচ্চ কতগুলো পেজ তৈরি করতে পারবে তা এখান থেকে নিয়ন্ত্রণ করুন।</small>
                </div>

                <!-- 4. Reels Duration -->
                <div class="p-3 bg-light rounded-3 mb-4 border">
                    <label class="form-label small fw-bold text-dark mb-1">
                        <i class="bi bi-camera-reels-fill text-danger me-1"></i> রিলস / শর্ট ভিডিও সর্বোচ্চ সময়সীমা (সেকেন্ড)
                    </label>
                    <div class="input-group mb-1">
                        <input type="number" name="max_duration" class="form-control rounded-3" value="<?= $currReelSec ?>" min="5" step="1" required>
                        <span class="input-group-text bg-white border">সেকেন্ড</span>
                    </div>
                    <small class="text-muted d-block">ডিফল্ট ৬০ সেকেন্ড (১ মিনিট)। ব্যবহারকারীদের আপলোড করা রিলসের সর্বোচ্চ সময়সীমা।</small>
                </div>

                <div class="p-3 bg-light rounded-3 mb-4 small border">
                    <div class="fw-bold text-dark mb-1"><i class="bi bi-database-check text-success me-1"></i> ডাটাবেজ স্থিতি:</div>
                    <div class="text-muted">Host: <strong><?= defined('DB_HOST') ? DB_HOST : 'localhost' ?></strong> | Database: <strong><?= defined('DB_NAME') ? DB_NAME : 'flikata_bd' ?></strong></div>
                </div>

                <button type="submit" class="btn btn-primary-custom w-100">
                    <i class="bi bi-save2 me-1"></i> কনফিগারেশন সংরক্ষণ করুন
                </button>
            </form>
        </div>
    </div>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
