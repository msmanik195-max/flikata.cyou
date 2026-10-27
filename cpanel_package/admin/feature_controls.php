<?php
/**
 * App Feature Controls, Feed Toggles & Daily Limits
 * Flikata Admin Control Panel
 * Strictly requires admin authentication.
 */
require_once __DIR__ . '/includes/db.php';
checkAdminAuth();

// Ensure table and columns exist
try {
    $pdo->exec("CREATE TABLE IF NOT EXISTS `app_feature_settings` (
      `id` INT PRIMARY KEY DEFAULT 1,
      `allow_page_creation` TINYINT(1) NOT NULL DEFAULT 1,
      `allow_login` TINYINT(1) NOT NULL DEFAULT 1,
      `allow_registration` TINYINT(1) NOT NULL DEFAULT 1,
      `allow_chat` TINYINT(1) NOT NULL DEFAULT 1,
      `allow_reels` TINYINT(1) NOT NULL DEFAULT 1,
      `feed_show_text_posts` TINYINT(1) NOT NULL DEFAULT 1,
      `feed_show_image_posts` TINYINT(1) NOT NULL DEFAULT 1,
      `feed_show_video_posts` TINYINT(1) NOT NULL DEFAULT 1,
      `daily_text_limit` INT NOT NULL DEFAULT 30,
      `daily_image_limit` INT NOT NULL DEFAULT 20,
      `daily_video_limit` INT NOT NULL DEFAULT 10,
      `daily_story_limit` INT NOT NULL DEFAULT 15,
      `default_page_create_limit` INT NOT NULL DEFAULT 5,
      `users_per_page` INT NOT NULL DEFAULT 20,
      `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

    $alterCols = [
        "allow_reels"           => "TINYINT(1) NOT NULL DEFAULT 1",
        "users_per_page"        => "INT NOT NULL DEFAULT 20",
        "feed_show_text_posts"  => "TINYINT(1) NOT NULL DEFAULT 1",
        "feed_show_image_posts" => "TINYINT(1) NOT NULL DEFAULT 1",
        "feed_show_video_posts" => "TINYINT(1) NOT NULL DEFAULT 1"
    ];
    foreach ($alterCols as $c => $def) {
        try { $pdo->exec("ALTER TABLE `app_feature_settings` ADD COLUMN `{$c}` {$def}"); } catch (Exception $e) {}
    }

    $pdo->exec("INSERT INTO `app_feature_settings` (`id`, `allow_page_creation`, `allow_login`, `allow_registration`, `allow_chat`, `allow_reels`, `feed_show_text_posts`, `feed_show_image_posts`, `feed_show_video_posts`, `daily_text_limit`, `daily_image_limit`, `daily_video_limit`, `daily_story_limit`, `default_page_create_limit`, `users_per_page`)
      VALUES (1, 1, 1, 1, 1, 1, 1, 1, 1, 30, 20, 10, 15, 5, 20)
      ON DUPLICATE KEY UPDATE `id` = 1;");
} catch (Exception $e) {}

$pageTitle = ($currentLang === 'en') ? 'App Feature Controls & Daily Limits - Flikata Admin' : 'ফিচার কন্ট্রোল ও লিমিট নিয়ন্ত্রণ - Flikata Admin';
$pageHeading = ($currentLang === 'en') ? 'App Feature Controls & Limits' : 'অ্যাপ ফিচার, ফিড ও লিমিট নিয়ন্ত্রণ';
$pageSubheading = ($currentLang === 'en') ? 'Manage app modules, home feed visibility, and 24-hour post/story limits' : 'অ্যাপের ফিচার অন/অফ, হোম ফিড কনটেন্ট প্রদর্শন ও ২৪ ঘণ্টার পোস্ট/স্টোরি লিমিট পরিচালনা';

$successMsg = '';
$errorMsg = '';

// Handle AJAX Single Toggle
if (isset($_GET['ajax_toggle']) && isset($_GET['feature'])) {
    header('Content-Type: application/json; charset=utf-8');
    $feature = trim($_GET['feature']);
    $allowedFeatures = [
        'allow_page_creation', 'allow_login', 'allow_registration', 'allow_chat', 'allow_reels',
        'feed_show_text_posts', 'feed_show_image_posts', 'feed_show_video_posts'
    ];
    
    if (in_array($feature, $allowedFeatures)) {
        $stmt = $pdo->query("SELECT `{$feature}` FROM app_feature_settings WHERE id = 1");
        $curr = (int)$stmt->fetchColumn();
        $newVal = ($curr === 1) ? 0 : 1;
        $pdo->prepare("UPDATE app_feature_settings SET `{$feature}` = ? WHERE id = 1")->execute([$newVal]);
        echo json_encode(['success' => true, 'feature' => $feature, 'new_value' => $newVal]);
        exit;
    }
    echo json_encode(['success' => false, 'message' => 'Invalid feature']);
    exit;
}

// Handle Form POST Submission (Save all toggles & limits)
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['save_all_settings'])) {
    $allowPageCreation     = isset($_POST['allow_page_creation']) ? 1 : 0;
    $allowLogin            = isset($_POST['allow_login']) ? 1 : 0;
    $allowRegistration     = isset($_POST['allow_registration']) ? 1 : 0;
    $allowChat             = isset($_POST['allow_chat']) ? 1 : 0;
    $allowReels            = isset($_POST['allow_reels']) ? 1 : 0;
    $feedShowTextPosts     = isset($_POST['feed_show_text_posts']) ? 1 : 0;
    $feedShowImagePosts    = isset($_POST['feed_show_image_posts']) ? 1 : 0;
    $feedShowVideoPosts    = isset($_POST['feed_show_video_posts']) ? 1 : 0;
    $dailyTextLimit        = max(0, (int)($_POST['daily_text_limit'] ?? 30));
    $dailyImageLimit       = max(0, (int)($_POST['daily_image_limit'] ?? 20));
    $dailyVideoLimit       = max(0, (int)($_POST['daily_video_limit'] ?? 10));
    $dailyStoryLimit       = max(0, (int)($_POST['daily_story_limit'] ?? 15));
    $defaultPageLimit      = max(0, (int)($_POST['default_page_create_limit'] ?? 5));

    try {
        $stmt = $pdo->prepare("UPDATE app_feature_settings 
            SET allow_page_creation = ?, 
                allow_login = ?, 
                allow_registration = ?, 
                allow_chat = ?,
                allow_reels = ?,
                feed_show_text_posts = ?,
                feed_show_image_posts = ?,
                feed_show_video_posts = ?,
                daily_text_limit = ?,
                daily_image_limit = ?,
                daily_video_limit = ?,
                daily_story_limit = ?,
                default_page_create_limit = ?
            WHERE id = 1");
        $stmt->execute([
            $allowPageCreation,
            $allowLogin,
            $allowRegistration,
            $allowChat,
            $allowReels,
            $feedShowTextPosts,
            $feedShowImagePosts,
            $feedShowVideoPosts,
            $dailyTextLimit,
            $dailyImageLimit,
            $dailyVideoLimit,
            $dailyStoryLimit,
            $defaultPageLimit
        ]);
        $successMsg = ($currentLang === 'en') ? 'All settings and limits updated successfully!' : 'সকল ফিচার সেটিংস ও লিমিট সফলভাবে সংরক্ষণ করা হয়েছে!';
    } catch (Exception $e) {
        $errorMsg = ($currentLang === 'en') ? ('Error: ' . $e->getMessage()) : ('ত্রুটি: ' . $e->getMessage());
    }
}

// Fetch Current Settings
$stmt = $pdo->query("SELECT * FROM app_feature_settings WHERE id = 1 LIMIT 1");
$settings = $stmt ? $stmt->fetch(PDO::FETCH_ASSOC) : [];
if (empty($settings)) {
    $settings = [
        'allow_page_creation'       => 1,
        'allow_login'               => 1,
        'allow_registration'        => 1,
        'allow_chat'                => 1,
        'allow_reels'               => 1,
        'feed_show_text_posts'      => 1,
        'feed_show_image_posts'     => 1,
        'feed_show_video_posts'     => 1,
        'daily_text_limit'          => 30,
        'daily_image_limit'         => 20,
        'daily_video_limit'         => 10,
        'daily_story_limit'         => 15,
        'default_page_create_limit' => 5,
        'users_per_page'            => 20
    ];
}
if (!isset($settings['allow_reels'])) {
    $settings['allow_reels'] = 1;
}

require_once __DIR__ . '/includes/header.php';
?>

<?php if ($successMsg): ?>
    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center mb-4 rounded-3 shadow-sm border-0" role="alert">
        <i class="bi bi-check-circle-fill fs-5 me-2 text-success"></i>
        <div class="fw-semibold"><?= htmlspecialchars($successMsg) ?></div>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
<?php endif; ?>

<?php if ($errorMsg): ?>
    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center mb-4 rounded-3 shadow-sm border-0" role="alert">
        <i class="bi bi-exclamation-triangle-fill fs-5 me-2 text-danger"></i>
        <div class="fw-semibold"><?= htmlspecialchars($errorMsg) ?></div>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
<?php endif; ?>

<!-- Top Info Banner -->
<div class="card-custom mb-4 p-4 border-0 shadow-sm" style="background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%); border-left: 5px solid #4f46e5 !important;">
    <div class="d-flex align-items-center gap-3">
        <div class="p-3 bg-white text-primary rounded-4 shadow-sm">
            <i class="bi bi-toggles2 fs-2"></i>
        </div>
        <div>
            <h5 class="fw-bold text-dark mb-1">
                <?= ($currentLang === 'en') ? 'Instant App Feature & Limit Control Center' : 'তাৎক্ষণিক অ্যাপ ফিচার, ফিড ও লিমিট নিয়ন্ত্রণ কেন্দ্র' ?>
            </h5>
            <p class="mb-0 text-muted small">
                <?= ($currentLang === 'en') 
                    ? 'Changes made here reflect immediately in the mobile application. When a feature is disabled or set to 0, users see an instant notice popup.' 
                    : 'এখানে যেকোনো পরিবর্তন অ্যাপে তাৎক্ষণিকভাবে কার্যকর হবে। কোনো লিমিট ০ দিলে পোস্ট বন্ধ থাকবে এবং অ্যাপে নোটিফিকেশন চলে যাবে।' ?>
            </p>
        </div>
    </div>
</div>

<form method="POST" action="feature_controls.php">
    <input type="hidden" name="save_all_settings" value="1">

    <!-- SECTION 1: CORE FEATURE TOGGLES -->
    <div class="d-flex align-items-center justify-content-between mb-3 mt-4">
        <h5 class="fw-bold text-dark mb-0 d-flex align-items-center gap-2">
            <i class="bi bi-shield-lock-fill text-primary"></i>
            <?= ($currentLang === 'en') ? '1. Core App Feature Toggles' : '১. মূল অ্যাপ ফিচার টগল (Core Toggles)' ?>
        </h5>
        <span class="badge bg-primary-subtle text-primary border small px-2 py-1">তাৎক্ষণিক কাজ করে</span>
    </div>

    <div class="row g-4 mb-5">
        <!-- 1. Page Creation Toggle Card -->
        <div class="col-12 col-md-6 col-lg-4">
            <div class="card-custom h-100 p-4 shadow-sm">
                <div class="d-flex align-items-start justify-content-between mb-3">
                    <div class="p-2.5 rounded-3 bg-primary-subtle text-primary">
                        <i class="bi bi-flag-fill fs-4"></i>
                    </div>
                    <div class="form-check form-switch fs-4 mb-0">
                        <input class="form-check-input" type="checkbox" name="allow_page_creation" value="1" id="togglePage" <?= !empty($settings['allow_page_creation']) ? 'checked' : '' ?> onchange="quickToggle('allow_page_creation', this.checked)">
                    </div>
                </div>
                <h6 class="fw-bold text-dark mb-1"><?= ($currentLang === 'en') ? 'Page Creation' : 'পেজ তৈরি করার অনুমতি' ?></h6>
                <span class="badge <?= !empty($settings['allow_page_creation']) ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger' ?> border mb-2">
                    <?= !empty($settings['allow_page_creation']) ? 'চালু আছে' : 'বন্ধ আছে' ?>
                </span>
                <p class="text-muted small mb-2">
                    বন্ধ থাকলে পেজ তৈরি করতে গেলে পপ-আপ দেখাবে:
                </p>
                <div class="p-2 rounded-2 bg-light border small text-dark fw-bold">
                    "পেজ তৈরি করার সুবিধা সাময়িকভাবে বন্ধ আছে।"
                </div>
            </div>
        </div>

        <!-- 2. User Login Toggle Card -->
        <div class="col-12 col-md-6 col-lg-4">
            <div class="card-custom h-100 p-4 shadow-sm">
                <div class="d-flex align-items-start justify-content-between mb-3">
                    <div class="p-2.5 rounded-3 bg-info-subtle text-info">
                        <i class="bi bi-box-arrow-in-right fs-4"></i>
                    </div>
                    <div class="form-check form-switch fs-4 mb-0">
                        <input class="form-check-input" type="checkbox" name="allow_login" value="1" id="toggleLogin" <?= !empty($settings['allow_login']) ? 'checked' : '' ?> onchange="quickToggle('allow_login', this.checked)">
                    </div>
                </div>
                <h6 class="fw-bold text-dark mb-1"><?= ($currentLang === 'en') ? 'User Login System' : 'ইউজার লগইন সুবিধা' ?></h6>
                <span class="badge <?= !empty($settings['allow_login']) ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger' ?> border mb-2">
                    <?= !empty($settings['allow_login']) ? 'চালু আছে' : 'বন্ধ আছে' ?>
                </span>
                <p class="text-muted small mb-2">
                    বন্ধ থাকলে লগইন করতে গেলে পপ-আপ মেসেজ আসবে:
                </p>
                <div class="p-2 rounded-2 bg-light border small text-dark fw-bold">
                    "লগইন সাময়িকভাবে বন্ধ আছে"
                </div>
            </div>
        </div>

        <!-- 3. User Registration Toggle Card -->
        <div class="col-12 col-md-6 col-lg-4">
            <div class="card-custom h-100 p-4 shadow-sm">
                <div class="d-flex align-items-start justify-content-between mb-3">
                    <div class="p-2.5 rounded-3 bg-warning-subtle text-warning">
                        <i class="bi bi-person-plus-fill fs-4"></i>
                    </div>
                    <div class="form-check form-switch fs-4 mb-0">
                        <input class="form-check-input" type="checkbox" name="allow_registration" value="1" id="toggleReg" <?= !empty($settings['allow_registration']) ? 'checked' : '' ?> onchange="quickToggle('allow_registration', this.checked)">
                    </div>
                </div>
                <h6 class="fw-bold text-dark mb-1"><?= ($currentLang === 'en') ? 'New Registration' : 'নতুন রেজিস্ট্রেশন' ?></h6>
                <span class="badge <?= !empty($settings['allow_registration']) ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger' ?> border mb-2">
                    <?= !empty($settings['allow_registration']) ? 'চালু আছে' : 'বন্ধ আছে' ?>
                </span>
                <p class="text-muted small mb-2">
                    বন্ধ থাকলে সাইন আপে ক্লিক করলে পপ-আপ আসবে:
                </p>
                <div class="p-2 rounded-2 bg-light border small text-dark fw-bold">
                    "রেজিস্ট্রেশন সাময়িকভাবে বন্ধ আছে"
                </div>
            </div>
        </div>

        <!-- 4. Chat System Toggle Card -->
        <div class="col-12 col-md-6 col-lg-4">
            <div class="card-custom h-100 p-4 shadow-sm">
                <div class="d-flex align-items-start justify-content-between mb-3">
                    <div class="p-2.5 rounded-3 bg-success-subtle text-success">
                        <i class="bi bi-chat-dots-fill fs-4"></i>
                    </div>
                    <div class="form-check form-switch fs-4 mb-0">
                        <input class="form-check-input" type="checkbox" name="allow_chat" value="1" id="toggleChat" <?= !empty($settings['allow_chat']) ? 'checked' : '' ?> onchange="quickToggle('allow_chat', this.checked)">
                    </div>
                </div>
                <h6 class="fw-bold text-dark mb-1"><?= ($currentLang === 'en') ? 'Chat Option' : 'চ্যাট অপশন সুবিধা' ?></h6>
                <span class="badge <?= !empty($settings['allow_chat']) ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger' ?> border mb-2">
                    <?= !empty($settings['allow_chat']) ? 'চালু আছে' : 'বন্ধ আছে' ?>
                </span>
                <p class="text-muted small mb-2">
                    বন্ধ থাকলে চ্যাটে ঢুকতে বা মেসেজ পাঠাতে গেলে দেখাবে:
                </p>
                <div class="p-2 rounded-2 bg-light border small text-dark fw-bold">
                    "চ্যাট অপশন সাময়িকভাবে বন্ধ আছে"
                </div>
            </div>
        </div>
    </div>

    <!-- SECTION 2: CONTENT & REELS VISIBILITY TOGGLES -->
    <div class="d-flex align-items-center justify-content-between mb-3">
        <div>
            <h5 class="fw-bold text-dark mb-1 d-flex align-items-center gap-2">
                <i class="bi bi-collection-play-fill text-primary"></i>
                <?= ($currentLang === 'en') ? '2. Reels & Home Feed Video Visibility Controls' : '২. রিলস ও হোম ফিড কনটেন্ট প্রদর্শন নিয়ন্ত্রণ (Reels & Feed Toggles)' ?>
            </h5>
            <small class="text-muted">
                রিলস ভিডিও পেজ চালু/বন্ধ রাখুন এবং হোম ফিডে ভিডিও প্রদর্শন আলাদাভাবে নিয়ন্ত্রণ করুন।
            </small>
        </div>
        <span class="badge bg-success-subtle text-success border px-2 py-1">লাইভ সিঙ্ক</span>
    </div>

    <div class="row g-4 mb-5">
        <!-- 1. Reels Video Page Toggle Card -->
        <div class="col-12 col-md-6 col-lg-3">
            <div class="card-custom h-100 p-4 shadow-sm" style="border-top: 3px solid #9333ea !important;">
                <div class="d-flex align-items-start justify-content-between mb-3">
                    <div class="p-2.5 rounded-3" style="background-color: #f3e8ff; color: #9333ea;">
                        <i class="bi bi-camera-reels-fill fs-4"></i>
                    </div>
                    <div class="form-check form-switch fs-4 mb-0">
                        <input class="form-check-input" type="checkbox" name="allow_reels" value="1" id="toggleReels" <?= !empty($settings['allow_reels']) ? 'checked' : '' ?> onchange="quickToggle('allow_reels', this.checked)">
                    </div>
                </div>
                <h6 class="fw-bold text-dark mb-1">রিলস (Reels) ভিডিও পেজ</h6>
                <span id="badge_allow_reels" class="badge <?= !empty($settings['allow_reels']) ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger' ?> border mb-2">
                    <?= !empty($settings['allow_reels']) ? 'চালু আছে' : 'বন্ধ আছে' ?>
                </span>
                <p class="text-muted small mb-2">
                    রিয়েলস পেজে ভিডিও দেখাবে নাকি বন্ধ থাকবে তা অন/অফ করুন:
                </p>
                <div class="p-2 rounded-2 bg-light border small text-dark fw-bold">
                    বন্ধ থাকলে রিয়েলস পেজে দেখাবে: "রিলস সুবিধা সাময়িকভাবে বন্ধ আছে।"
                </div>
            </div>
        </div>

        <!-- 2. Home Feed Video Posts Toggle Card (Home Feed Only) -->
        <div class="col-12 col-md-6 col-lg-3">
            <div class="card-custom h-100 p-4 shadow-sm" style="border-top: 3px solid #ef4444 !important;">
                <div class="d-flex align-items-start justify-content-between mb-3">
                    <div class="p-2.5 rounded-3 bg-danger-subtle text-danger">
                        <i class="bi bi-play-circle-fill fs-4"></i>
                    </div>
                    <div class="form-check form-switch fs-4 mb-0">
                        <input class="form-check-input" type="checkbox" name="feed_show_video_posts" value="1" id="toggleFeedVideo" <?= !empty($settings['feed_show_video_posts']) ? 'checked' : '' ?> onchange="quickToggle('feed_show_video_posts', this.checked)">
                    </div>
                </div>
                <h6 class="fw-bold text-dark mb-1">হোম ফিডে ভিডিও পোস্ট</h6>
                <span id="badge_feed_show_video_posts" class="badge <?= !empty($settings['feed_show_video_posts']) ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary' ?> border mb-2">
                    <?= !empty($settings['feed_show_video_posts']) ? 'হোম ফিডে প্রদর্শিত হবে' : 'হোম ফিডে গোপন থাকবে' ?>
                </span>
                <p class="text-muted small mb-2">
                    বন্ধ থাকলে <strong>শুধুমাত্র হোম ফিড</strong> থেকে ভিডিও লুকানো থাকবে:
                </p>
                <div class="p-2 rounded-2 bg-light border small text-dark fw-bold">
                    রিয়েলস পেজের ভিডিও কখনোই কাটবে না, রিয়েলস পেজে ভিডিও স্বাভাবিক থাকবে।
                </div>
            </div>
        </div>

        <!-- 3. Feed Image Posts Toggle -->
        <div class="col-12 col-md-6 col-lg-3">
            <div class="card-custom h-100 p-4 shadow-sm">
                <div class="d-flex align-items-start justify-content-between mb-3">
                    <div class="p-2.5 rounded-3 bg-info-subtle text-info">
                        <i class="bi bi-image-fill fs-4"></i>
                    </div>
                    <div class="form-check form-switch fs-4 mb-0">
                        <input class="form-check-input" type="checkbox" name="feed_show_image_posts" value="1" id="toggleFeedImage" <?= !empty($settings['feed_show_image_posts']) ? 'checked' : '' ?> onchange="quickToggle('feed_show_image_posts', this.checked)">
                    </div>
                </div>
                <h6 class="fw-bold text-dark mb-1">হোম ফিডে ইমেজ পোস্ট</h6>
                <span id="badge_feed_show_image_posts" class="badge <?= !empty($settings['feed_show_image_posts']) ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary' ?> border mb-2">
                    <?= !empty($settings['feed_show_image_posts']) ? 'ফিডে প্রদর্শিত হবে' : 'ফিডে গোপন থাকবে' ?>
                </span>
                <p class="text-muted small mb-0">
                    চালু থাকলে ছবিযুক্ত পোস্টগুলো হোম ফিডে দেখা যাবে। বন্ধ থাকলে ছবি লুকানো থাকবে।
                </p>
            </div>
        </div>

        <!-- 4. Feed Text Posts Toggle -->
        <div class="col-12 col-md-6 col-lg-3">
            <div class="card-custom h-100 p-4 shadow-sm">
                <div class="d-flex align-items-start justify-content-between mb-3">
                    <div class="p-2.5 rounded-3 bg-primary-subtle text-primary">
                        <i class="bi bi-file-text-fill fs-4"></i>
                    </div>
                    <div class="form-check form-switch fs-4 mb-0">
                        <input class="form-check-input" type="checkbox" name="feed_show_text_posts" value="1" id="toggleFeedText" <?= !empty($settings['feed_show_text_posts']) ? 'checked' : '' ?> onchange="quickToggle('feed_show_text_posts', this.checked)">
                    </div>
                </div>
                <h6 class="fw-bold text-dark mb-1">হোম ফিডে টেক্সট পোস্ট</h6>
                <span id="badge_feed_show_text_posts" class="badge <?= !empty($settings['feed_show_text_posts']) ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary' ?> border mb-2">
                    <?= !empty($settings['feed_show_text_posts']) ? 'ফিডে প্রদর্শিত হবে' : 'ফিডে গোপন থাকবে' ?>
                </span>
                <p class="text-muted small mb-0">
                    চালু থাকলে হোম ফিডে শুধু টেক্সট সংবলিত পোস্টগুলো দেখা যাবে।
                </p>
            </div>
        </div>
    </div>

    <!-- SECTION 3: GLOBAL 24-HOUR POST & STORY LIMITS -->
    <div class="d-flex align-items-center justify-content-between mb-3">
        <div>
            <h5 class="fw-bold text-dark mb-1 d-flex align-items-center gap-2">
                <i class="bi bi-speedometer2 text-warning"></i>
                <?= ($currentLang === 'en') ? '3. Global Daily 24-Hour Limits & Restrictions' : '৩. দৈনিক পোস্ট, স্টোরি ও পেজ লিমিট (২৪ ঘণ্টার সীমা)' ?>
            </h5>
            <small class="text-muted">
                একটি আইডি থেকে ২৪ ঘণ্টার মধ্যে সর্বোচ্চ কতটি করে পোস্ট বা স্টোরি করা যাবে। <strong>০ দিলে সাময়িকভাবে বন্ধ থাকবে এবং নোটিফিকেশন যাবে।</strong>
            </small>
        </div>
    </div>

    <!-- Zero Warning Alert -->
    <div class="alert alert-warning border-0 rounded-4 shadow-sm mb-4 d-flex align-items-center gap-3">
        <i class="bi bi-exclamation-octagon-fill fs-3 text-warning"></i>
        <div>
            <div class="fw-bold text-dark">গুরুত্বপূর্ণ নিয়ম (Zero Limit Rule):</div>
            <div class="small text-secondary">
                যেকোনো লিমিটে <code>0</code> দিলে ব্যবহারকারী উক্ত পোস্ট/স্টোরি/পেজ তৈরি করতে গেলে সরাসরি দেখাবে <strong>"পোস্ট করা সাময়িকভাবে বন্ধ আছে"</strong> এবং তাৎক্ষণিকভাবে তার নোটিফিকেশন পেজে নোটিফিকেশন পৌঁছে যাবে।
            </div>
        </div>
    </div>

    <div class="row g-4 mb-4">
        <!-- Daily Text Post Limit -->
        <div class="col-12 col-md-6 col-lg-4">
            <div class="card-custom p-4 shadow-sm h-100">
                <label class="form-label fw-bold text-dark d-flex align-items-center justify-content-between">
                    <span><i class="bi bi-chat-left-quote text-primary me-2"></i>দৈনিক টেক্সট পোস্ট লিমিট</span>
                    <span class="badge bg-light border text-muted">২৪ ঘণ্টা</span>
                </label>
                <div class="input-group mb-2">
                    <input type="number" name="daily_text_limit" min="0" max="1000" class="form-control form-control-lg fw-bold text-primary" value="<?= htmlspecialchars($settings['daily_text_limit'] ?? 30) ?>" required>
                    <span class="input-group-text bg-light">টি / দিন</span>
                </div>
                <small class="text-muted">ডিফল্ট: ৩০। ০ দিলে টেক্সট পোস্ট বন্ধ থাকবে।</small>
            </div>
        </div>

        <!-- Daily Image Post Limit -->
        <div class="col-12 col-md-6 col-lg-4">
            <div class="card-custom p-4 shadow-sm h-100">
                <label class="form-label fw-bold text-dark d-flex align-items-center justify-content-between">
                    <span><i class="bi bi-images text-info me-2"></i>দৈনিক ইমেজ পোস্ট লিমিট</span>
                    <span class="badge bg-light border text-muted">২৪ ঘণ্টা</span>
                </label>
                <div class="input-group mb-2">
                    <input type="number" name="daily_image_limit" min="0" max="1000" class="form-control form-control-lg fw-bold text-info" value="<?= htmlspecialchars($settings['daily_image_limit'] ?? 20) ?>" required>
                    <span class="input-group-text bg-light">টি / দিন</span>
                </div>
                <small class="text-muted">ডিফল্ট: ২০। ০ দিলে ইমেজ পোস্ট বন্ধ থাকবে।</small>
            </div>
        </div>

        <!-- Daily Video Post Limit -->
        <div class="col-12 col-md-6 col-lg-4">
            <div class="card-custom p-4 shadow-sm h-100">
                <label class="form-label fw-bold text-dark d-flex align-items-center justify-content-between">
                    <span><i class="bi bi-camera-video-fill text-danger me-2"></i>দৈনিক ভিডিও পোস্ট লিমিট</span>
                    <span class="badge bg-light border text-muted">২৪ ঘণ্টা</span>
                </label>
                <div class="input-group mb-2">
                    <input type="number" name="daily_video_limit" min="0" max="1000" class="form-control form-control-lg fw-bold text-danger" value="<?= htmlspecialchars($settings['daily_video_limit'] ?? 10) ?>" required>
                    <span class="input-group-text bg-light">টি / দিন</span>
                </div>
                <small class="text-muted">ডিফল্ট: ১০। ০ দিলে ভিডিও পোস্ট বন্ধ থাকবে।</small>
            </div>
        </div>

        <!-- Daily Story Post Limit -->
        <div class="col-12 col-md-6 col-lg-4">
            <div class="card-custom p-4 shadow-sm h-100">
                <label class="form-label fw-bold text-dark d-flex align-items-center justify-content-between">
                    <span><i class="bi bi-circle-half text-success me-2"></i>দৈনিক স্টোরি পোস্ট লিমিট</span>
                    <span class="badge bg-light border text-muted">২৪ ঘণ্টা</span>
                </label>
                <div class="input-group mb-2">
                    <input type="number" name="daily_story_limit" min="0" max="1000" class="form-control form-control-lg fw-bold text-success" value="<?= htmlspecialchars($settings['daily_story_limit'] ?? 15) ?>" required>
                    <span class="input-group-text bg-light">টি / দিন</span>
                </div>
                <small class="text-muted">ডিফল্ট: ১৫। ০ দিলে স্টোরি দেওয়া সাময়িকভাবে বন্ধ থাকবে।</small>
            </div>
        </div>

        <!-- Default Page Create Limit -->
        <div class="col-12 col-md-6 col-lg-4">
            <div class="card-custom p-4 shadow-sm h-100">
                <label class="form-label fw-bold text-dark d-flex align-items-center justify-content-between">
                    <span><i class="bi bi-flag-fill text-primary me-2"></i>ডিফল্ট পেজ ক্রিয়েট লিমিট</span>
                    <span class="badge bg-light border text-muted">আইডি প্রতি</span>
                </label>
                <div class="input-group mb-2">
                    <input type="number" name="default_page_create_limit" min="0" max="100" class="form-control form-control-lg fw-bold text-primary" value="<?= htmlspecialchars($settings['default_page_create_limit'] ?? 5) ?>" required>
                    <span class="input-group-text bg-light">টি পেজ</span>
                </div>
                <small class="text-muted">ডিফল্ট: ৫টি। ০ দিলে কোনো ইউজার পেজ তৈরি করতে পারবে না।</small>
            </div>
        </div>
    </div>

    <!-- Save Button -->
    <div class="card-custom p-4 shadow-sm mb-5 d-flex align-items-center justify-content-between">
        <div>
            <div class="fw-bold text-dark">সব পরিবর্তন একসাথে সেভ করতে চান?</div>
            <div class="small text-muted">নিচের বাটনে ক্লিক করলে সকল টগল এবং লিমিট একসাথে সংরক্ষণ হবে।</div>
        </div>
        <button type="submit" class="btn btn-primary px-5 py-2.5 rounded-pill fw-bold shadow-sm">
            <i class="bi bi-save2-fill me-2"></i><?= ($currentLang === 'en') ? 'Save All Settings & Limits' : 'সকল সেটিংস সংরক্ষণ করুন' ?>
        </button>
    </div>
</form>

<script>
function quickToggle(featureName, isChecked) {
    const badge = document.getElementById('badge_' + featureName);
    fetch('feature_controls.php?ajax_toggle=1&feature=' + encodeURIComponent(featureName))
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const isEnabled = data.new_value === 1;
                if (badge) {
                    if (featureName === 'feed_show_video_posts') {
                        badge.className = 'badge border mb-2 ' + (isEnabled ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary');
                        badge.textContent = isEnabled ? 'হোম ফিডে প্রদর্শিত হবে' : 'হোম ফিডে গোপন থাকবে';
                    } else if (featureName.startsWith('feed_show_')) {
                        badge.className = 'badge border mb-2 ' + (isEnabled ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary');
                        badge.textContent = isEnabled ? 'ফিডে প্রদর্শিত হবে' : 'ফিডে গোপন থাকবে';
                    } else {
                        badge.className = 'badge border mb-2 ' + (isEnabled ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger');
                        badge.textContent = isEnabled ? 'চালু আছে' : 'বন্ধ আছে';
                    }
                }
                Swal.fire({
                    toast: true,
                    position: 'top-end',
                    icon: 'success',
                    title: '<?= ($currentLang === 'en') ? 'Setting updated!' : 'সেটিংস আপডেট হয়েছে!' ?>',
                    showConfirmButton: false,
                    timer: 1500
                });
            }
        })
        .catch(err => {
            console.error('Toggle error:', err);
        });
}
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
