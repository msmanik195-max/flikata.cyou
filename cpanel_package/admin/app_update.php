<?php
/**
 * App Update Management Page
 * Flikata Admin Panel
 * Allows uploading APK to Cloudflare R2, configuring latest version code & name,
 * and setting mandatory/optional in-app update policies.
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'অ্যাপ আপডেট ম্যানেজমেন্ট - Flikata Admin';
$pageHeading = 'ইন-অ্যাপ সফটওয়্যার আপডেট';
$pageSubheading = 'নতুন APK আপলোড, ভার্সন কোড ও ইন-অ্যাপ আপডেট নীতি পরিচালনা করুন';

$successMsg = '';
$errorMsg = '';

// Ensure app_updates table exists
try {
    $pdo->exec("CREATE TABLE IF NOT EXISTS `app_updates` (
      `id` INT AUTO_INCREMENT PRIMARY KEY,
      `version_code` INT NOT NULL,
      `version_name` VARCHAR(50) NOT NULL,
      `apk_url` VARCHAR(1000) NOT NULL,
      `apk_size_mb` VARCHAR(50) DEFAULT '30 MB',
      `release_notes` TEXT DEFAULT NULL,
      `is_force_update` TINYINT(1) DEFAULT 1,
      `is_active` TINYINT(1) DEFAULT 1,
      `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
      `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      INDEX (`version_code`),
      INDEX (`is_active`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
} catch (Exception $e) {}

// Handle Delete Release
if (isset($_GET['delete_id'])) {
    $delId = (int)$_GET['delete_id'];
    if ($delId > 0) {
        $pdo->prepare("DELETE FROM app_updates WHERE id = ?")->execute([$delId]);
        header("Location: app_update.php?msg=deleted");
        exit;
    }
}

// Handle Toggle Active
if (isset($_GET['toggle_id'])) {
    $togId = (int)$_GET['toggle_id'];
    if ($togId > 0) {
        $stmt = $pdo->prepare("SELECT is_active FROM app_updates WHERE id = ?");
        $stmt->execute([$togId]);
        $curr = $stmt->fetchColumn();
        $newVal = ($curr == 1) ? 0 : 1;
        $pdo->prepare("UPDATE app_updates SET is_active = ? WHERE id = ?")->execute([$newVal, $togId]);
        header("Location: app_update.php?msg=toggled");
        exit;
    }
}

if (isset($_GET['msg'])) {
    if ($_GET['msg'] === 'deleted') $successMsg = 'রিলিজটি সফলভাবে ডিলিট করা হয়েছে!';
    if ($_GET['msg'] === 'toggled') $successMsg = 'রিলিজের স্ট্যাটাস সফলভাবে পরিবর্তন করা হয়েছে!';
}

// Handle Form Submission
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['publish_update'])) {
    $versionName = trim($_POST['version_name'] ?? '7.1');
    $inputCode = (int)($_POST['version_code'] ?? 0);
    $directApkUrl = trim($_POST['direct_apk_url'] ?? '');
    $releaseNotes = trim($_POST['release_notes'] ?? '');
    $isForce = isset($_POST['is_force_update']) ? 1 : 0;
    $apkSizeMb = trim($_POST['apk_size_mb'] ?? '30 MB');

    // 1. Fetch current max version code in DB
    $maxExistingCode = 0;
    try {
        $maxExistingCode = (int)$pdo->query("SELECT MAX(version_code) FROM app_updates")->fetchColumn();
    } catch (Exception $e) {}

    // 2. Extract semantic numbers from versionName (e.g., "19.1" -> 191, "8.0" -> 80)
    $cleanName = preg_replace('/[^0-9\.]/', '', $versionName);
    $parts = explode('.', $cleanName);
    $major = isset($parts[0]) && is_numeric($parts[0]) ? (int)$parts[0] : 0;
    $minor = isset($parts[1]) && is_numeric($parts[1]) ? (int)$parts[1] : 0;
    $computedCode = ($major > 0) ? ($major * 10 + $minor) : 0;

    $candidate = max($inputCode, $computedCode);
    // Base app v7 has version_code = 14. Next must be >= 15 and strictly > maxExistingCode
    if ($candidate <= $maxExistingCode || $candidate <= 14) {
        $versionCode = max($maxExistingCode + 1, 15);
    } else {
        $versionCode = $candidate;
    }

    $finalApkUrl = $directApkUrl;

    // Check if an APK file was uploaded
    if (isset($_FILES['apk_file']) && !empty($_FILES['apk_file']['name'])) {
        $file = $_FILES['apk_file'];
        if ($file['error'] === UPLOAD_ERR_INI_SIZE || $file['error'] === UPLOAD_ERR_FORM_SIZE) {
            $errorMsg = 'APK ফাইলের সাইজ আপনার সার্ভারের PHP upload_max_filesize-এর চেয়ে বড়। নিচে "সরাসরি APK ডাউনলোড লিংক" ফিল্ডে ক্লাউডফ্লেয়ার বা ডিরেক্ট লিংক দিন অথবা cPanel MultiPHP INI Editor থেকে লিমিট বাড়িয়ে 64M করুন!';
        } elseif ($file['error'] === UPLOAD_ERR_OK) {
            $ext = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
            if ($ext !== 'apk') {
                $errorMsg = 'শুধুমাত্র .apk এক্সটেনশনের ফাইল আপলোড করা যাবে!';
            } else {
                $sizeBytes = $file['size'];
                $calculatedMb = round($sizeBytes / (1024 * 1024), 1) . ' MB';
                $apkSizeMb = $calculatedMb;

                $sanitizedVersion = preg_replace('/[^a-zA-Z0-9_\-\.]/', '_', $versionName);
                $targetFileName = 'apk/flikata_v' . $sanitizedVersion . '_' . time() . '.apk';

                // Try Cloudflare R2 upload via r2_helper.php
                $uploadedToR2 = false;
                $r2HelperPath = __DIR__ . '/../r2_helper.php';
                if (file_exists($r2HelperPath)) {
                    require_once $r2HelperPath;
                    if (function_exists('uploadToR2')) {
                        $fileContent = file_get_contents($file['tmp_name']);
                        $r2Url = uploadToR2($targetFileName, $fileContent, 'application/vnd.android.package-archive');
                        if ($r2Url) {
                            $finalApkUrl = $r2Url;
                            $uploadedToR2 = true;
                        }
                    }
                }

                // Fallback to local uploads/apk/ directory if R2 is not configured
                if (!$uploadedToR2) {
                    $uploadDir = __DIR__ . '/uploads/apk/';
                    if (!is_dir($uploadDir)) {
                        mkdir($uploadDir, 0755, true);
                    }
                    $localDestination = $uploadDir . basename($targetFileName);
                    if (move_uploaded_file($file['tmp_name'], $localDestination)) {
                        $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? "https://" : "http://";
                        $host = $_SERVER['HTTP_HOST'] ?? 'localhost';
                        $scriptDir = dirname($_SERVER['REQUEST_URI']);
                        $finalApkUrl = $protocol . $host . $scriptDir . '/uploads/apk/' . basename($targetFileName);
                    }
                }
            }
        }
    }

    // If final APK URL is empty and no specific error, create a resilient fallback URL
    if (empty($finalApkUrl) && empty($errorMsg)) {
        $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? "https://" : "http://";
        $host = $_SERVER['HTTP_HOST'] ?? 'localhost';
        $finalApkUrl = $protocol . $host . '/apk/flikata_v' . $versionCode . '.apk';
    }

    if (empty($errorMsg)) {
        try {
            // Deactivate previous releases
            $pdo->query("UPDATE app_updates SET is_active = 0");

            $stmt = $pdo->prepare("
                INSERT INTO app_updates (version_code, version_name, apk_url, apk_size_mb, release_notes, is_force_update, is_active)
                VALUES (?, ?, ?, ?, ?, ?, 1)
            ");
            $stmt->execute([
                $versionCode,
                $versionName,
                $finalApkUrl,
                $apkSizeMb,
                $releaseNotes,
                $isForce
            ]);

            $successMsg = "সফলভাবে ভার্সন {$versionName} (কোড: {$versionCode}) পাবলিশ করা হয়েছে! অ্যাপ্লিকেশনে তৎক্ষণাৎ আপডেট পপ আপ চলে যাবে।";
        } catch (Exception $e) {
            $errorMsg = 'ডাটাবেজে সেভ করতে সমস্যা হয়েছে: ' . $e->getMessage();
        }
    }
}

// Fetch currently active release
$activeRelease = null;
try {
    $stmt = $pdo->query("SELECT * FROM app_updates WHERE is_active = 1 ORDER BY version_code DESC, id DESC LIMIT 1");
    $activeRelease = $stmt ? $stmt->fetch() : null;
} catch (Exception $e) {}

// Fetch release history
$allReleases = [];
try {
    $stmt = $pdo->query("SELECT * FROM app_updates ORDER BY version_code DESC, id DESC");
    $allReleases = $stmt ? $stmt->fetchAll() : [];
} catch (Exception $e) {}

require_once __DIR__ . '/includes/header.php';
?>

<?php if ($successMsg): ?>
    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
        <i class="bi bi-check-circle-fill fs-5 me-2"></i>
        <div><?= htmlspecialchars($successMsg) ?></div>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
<?php endif; ?>

<?php if ($errorMsg): ?>
    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
        <i class="bi bi-exclamation-triangle-fill fs-5 me-2"></i>
        <div><?= htmlspecialchars($errorMsg) ?></div>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
<?php endif; ?>

<!-- Top Highlight Banner: Active Live Release -->
<div class="card-custom mb-4 border-primary border-opacity-25" style="background: linear-gradient(135deg, #eef2ff 0%, #f8fafc 100%);">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-3">
        <div class="d-flex align-items-center gap-3">
            <div class="stat-icon-box bg-primary text-white shadow-sm">
                <i class="bi bi-phone-vibrate-fill fs-3"></i>
            </div>
            <div>
                <div class="d-flex align-items-center gap-2">
                    <h5 class="fw-bold mb-0 text-dark">লাইভ প্রোডাকশন ভার্সন</h5>
                    <?php if ($activeRelease): ?>
                        <span class="badge bg-success px-2 py-1"><i class="bi bi-broadcast me-1"></i>এক্টিভ</span>
                        <?php if ($activeRelease['is_force_update'] == 1): ?>
                            <span class="badge bg-danger px-2 py-1"><i class="bi bi-shield-lock-fill me-1"></i>বাধ্যতামূলক আপডেট</span>
                        <?php else: ?>
                            <span class="badge bg-secondary px-2 py-1">ঐচ্ছিক আপডেট</span>
                        <?php endif; ?>
                    <?php else: ?>
                        <span class="badge bg-warning text-dark px-2 py-1">কোনো রিলিজ নেই</span>
                    <?php endif; ?>
                </div>
                <small class="text-muted d-block mt-1">
                    <?php if ($activeRelease): ?>
                        ভার্সন: <strong><?= htmlspecialchars($activeRelease['version_name']) ?></strong> • 
                        ভার্সন কোড: <strong><?= (int)$activeRelease['version_code'] ?></strong> • 
                        সাইজ: <strong><?= htmlspecialchars($activeRelease['apk_size_mb']) ?></strong> • 
                        তারিখ: <?= date('d M Y, h:i A', strtotime($activeRelease['created_at'])) ?>
                    <?php else: ?>
                        এখনও কোনো সক্রিয় আপডেট কনফিগার করা হয়নি। নিচে থেকে নতুন রিলিজ পাবলিশ করুন।
                    <?php endif; ?>
                </small>
            </div>
        </div>

        <?php if ($activeRelease && !empty($activeRelease['apk_url'])): ?>
            <a href="<?= htmlspecialchars($activeRelease['apk_url']) ?>" target="_blank" class="btn btn-outline-primary btn-sm px-3 rounded-pill">
                <i class="bi bi-download me-1"></i>ডাউনলোড টেস্ট করুন
            </a>
        <?php endif; ?>
    </div>
</div>

<div class="row g-4">
    <!-- Publish New Release Form -->
    <div class="col-12 col-lg-6">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center gap-2 mb-3">
                <div class="p-2 bg-primary-subtle text-primary rounded-3">
                    <i class="bi bi-cloud-arrow-up-fill fs-5"></i>
                </div>
                <div>
                    <h6 class="fw-bold text-dark mb-0">নতুন আপডেট পাবলিশ করুন</h6>
                    <small class="text-muted">ইউজারদের অ্যাপ্লিকেশনে ইনস্টল করানোর জন্য নতুন APK দিন</small>
                </div>
            </div>

            <form method="POST" action="app_update.php" enctype="multipart/form-data">
                <input type="hidden" name="publish_update" value="1">

                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label fw-semibold small">ভার্সন কোড (Version Code) <span class="text-danger">*</span></label>
                        <input type="number" name="version_code" class="form-control" placeholder="যেমন: 15" value="<?= $activeRelease ? max(15, (int)$activeRelease['version_code'] + 1) : 15 ?>" required>
                        <small class="text-muted" style="font-size: 11px;">অ্যাপ v7-এর কোড 14। এটি অটোমেটিক বাড়ানো হবে</small>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label fw-semibold small">ভার্সন নেম (Version Name) <span class="text-danger">*</span></label>
                        <input type="text" name="version_name" class="form-control" placeholder="যেমন: v7.1 বা 19.1" value="<?= $activeRelease ? htmlspecialchars($activeRelease['version_name']) : 'v7.1' ?>" required>
                        <small class="text-muted" style="font-size: 11px;">যেকোনো ভার্সন যেমন 7.1 বা 19.1 দিলেই অ্যাপে আপডেট যাবে</small>
                    </div>

                    <div class="col-12">
                        <label class="form-label fw-semibold small">APK ফাইল আপলোড করুন (Cloudflare R2-এ সেভ হবে)</label>
                        <input type="file" name="apk_file" class="form-control" accept=".apk">
                        <small class="text-muted" style="font-size: 11px;">সরাসরি APK সিলেক্ট করুন, যা ক্লাউডফ্লেয়ারে আপলোড হবে</small>
                    </div>

                    <div class="col-12">
                        <div class="d-flex align-items-center my-1">
                            <hr class="flex-grow-1 my-0">
                            <span class="px-2 text-muted small fw-bold">অথবা</span>
                            <hr class="flex-grow-1 my-0">
                        </div>
                    </div>

                    <div class="col-12">
                        <label class="form-label fw-semibold small">সরাসরি APK ডাউনলোড লিংক (Direct Download URL)</label>
                        <input type="url" name="direct_apk_url" class="form-control" placeholder="https://pub-...r2.dev/apk/flikata.apk" value="<?= htmlspecialchars($activeRelease['apk_url'] ?? '') ?>">
                        <small class="text-muted" style="font-size: 11px;">ফাইল আপলোড না করে সরাসরি লিংক দিতে চাইলে এখানে দিন</small>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label fw-semibold small">ফাইল সাইজ (আনুমানিক)</label>
                        <input type="text" name="apk_size_mb" class="form-control" placeholder="যেমন: 30 MB" value="30 MB">
                    </div>

                    <div class="col-md-6 d-flex align-items-center">
                        <div class="form-check form-switch mt-3">
                            <input class="form-check-input" type="checkbox" id="forceSwitch" name="is_force_update" value="1" checked>
                            <label class="form-check-label fw-semibold small" for="forceSwitch">বাধ্যতামূলক আপডেট (Force Update)</label>
                            <small class="text-muted d-block" style="font-size: 11px;">চালু থাকলে আপডেট ছাড়া অ্যাপে ঢুকতে দিবে না</small>
                        </div>
                    </div>

                    <div class="col-12">
                        <label class="form-label fw-semibold small">আপডেট নোটস / কী কী নতুন ফিচার আছে</label>
                        <textarea name="release_notes" class="form-control" rows="3" placeholder="১. দ্রুত লোডিং স্পিড&#10;২. নতুন ফলোয়ার সিস্টেম&#10;৩. বাগ ফিক্স"></textarea>
                    </div>

                    <div class="col-12 mt-4">
                        <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold">
                            <i class="bi bi-send-check-fill me-2"></i>এখনই আপডেট পাবলিশ করুন
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- Release History Table -->
    <div class="col-12 col-lg-6">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <div class="d-flex align-items-center gap-2">
                    <div class="p-2 bg-success-subtle text-success rounded-3">
                        <i class="bi bi-clock-history fs-5"></i>
                    </div>
                    <div>
                        <h6 class="fw-bold text-dark mb-0">পূর্ববর্তী রিলিজ হিস্টোরি</h6>
                        <small class="text-muted">সকল ভার্সন ও হিস্টোরি</small>
                    </div>
                </div>
                <span class="badge bg-light text-dark border"><?= count($allReleases) ?> টি রিলিজ</span>
            </div>

            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                        <tr style="font-size: 12px;">
                            <th>ভার্সন</th>
                            <th>কোড</th>
                            <th>টাইপ</th>
                            <th>স্ট্যাটাস</th>
                            <th class="text-end">অ্যাকশন</th>
                        </tr>
                    </thead>
                    <tbody style="font-size: 13px;">
                        <?php if (empty($allReleases)): ?>
                            <tr>
                                <td colspan="5" class="text-center py-4 text-muted">এখনও কোনো রিলিজ তথ্য যোগ করা হয়নি</td>
                            </tr>
                        <?php else: ?>
                            <?php foreach ($allReleases as $rel): ?>
                                <tr>
                                    <td>
                                        <div class="fw-bold text-dark"><?= htmlspecialchars($rel['version_name']) ?></div>
                                        <small class="text-muted" style="font-size: 11px;"><?= htmlspecialchars($rel['apk_size_mb']) ?></small>
                                    </td>
                                    <td>
                                        <span class="badge bg-light text-primary border"><?= (int)$rel['version_code'] ?></span>
                                    </td>
                                    <td>
                                        <?php if ($rel['is_force_update'] == 1): ?>
                                            <span class="badge bg-danger-subtle text-danger">Force</span>
                                        <?php else: ?>
                                            <span class="badge bg-secondary-subtle text-secondary">Optional</span>
                                        <?php endif; ?>
                                    </td>
                                    <td>
                                        <a href="app_update.php?toggle_id=<?= $rel['id'] ?>" class="text-decoration-none">
                                            <?php if ($rel['is_active'] == 1): ?>
                                                <span class="badge bg-success"><i class="bi bi-check-circle me-1"></i>Active</span>
                                            <?php else: ?>
                                                <span class="badge bg-secondary">Inactive</span>
                                            <?php endif; ?>
                                        </a>
                                    </td>
                                    <td class="text-end">
                                        <?php if (!empty($rel['apk_url'])): ?>
                                            <a href="<?= htmlspecialchars($rel['apk_url']) ?>" target="_blank" class="btn btn-sm btn-outline-secondary p-1" title="ডাউনলোড লিংক টেস্ট">
                                                <i class="bi bi-box-arrow-up-right"></i>
                                            </a>
                                        <?php endif; ?>
                                        <a href="app_update.php?delete_id=<?= $rel['id'] ?>" class="btn btn-sm btn-outline-danger p-1 ms-1" onclick="return confirm('আপনি কি নিশ্চিত যে এই রিলিজটি মুছে ফেলতে চান?');" title="ডিলিট">
                                            <i class="bi bi-trash-fill"></i>
                                        </a>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
