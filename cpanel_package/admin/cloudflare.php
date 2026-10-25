<?php
/**
 * Cloudflare R2 Multi-Bucket & Storage Management
 * Flikata Admin Control Panel
 * Strictly requires admin authentication.
 * Uses the primary `r2_buckets` table to preserve all previously configured Cloudflare accounts.
 */
require_once __DIR__ . '/includes/db.php';
checkAdminAuth();

// Ensure r2_buckets table exists with correct schema
try {
    $pdo->exec("CREATE TABLE IF NOT EXISTS `r2_buckets` (
      `id` INT AUTO_INCREMENT PRIMARY KEY,
      `account_label` VARCHAR(150) NOT NULL,
      `bucket_name` VARCHAR(150) NOT NULL,
      `account_id` VARCHAR(150) NOT NULL,
      `access_key_id` VARCHAR(150) NOT NULL,
      `secret_access_key` VARCHAR(255) NOT NULL,
      `public_url` VARCHAR(255) NOT NULL,
      `account_limit_gb` DECIMAL(10, 2) NOT NULL DEFAULT 10.00,
      `used_bytes` BIGINT UNSIGNED NOT NULL DEFAULT 0,
      `is_active` TINYINT(1) NOT NULL DEFAULT 1,
      `priority` INT NOT NULL DEFAULT 1,
      `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
      `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
} catch (Exception $e) {}

$pageTitle = ($currentLang === 'en') ? 'Cloudflare R2 Storage - Flikata Admin' : 'ক্লাউডফ্লেয়ার R2 স্টোরেজ - Flikata Admin';
$pageHeading = ($currentLang === 'en') ? 'Cloudflare R2 Storage Management' : 'ক্লাউডফ্লেয়ার R2 স্টোরেজ ব্যবস্থাপনা';
$pageSubheading = ($currentLang === 'en') ? 'Manage Cloudflare R2 multi-bucket failover, credentials and live connectivity' : 'ক্লাউডফ্লেয়ার R2 মাল্টি-বাকেট ফেইলওভার, একাউন্ট ক্রেডেনশিয়াল এবং লাইভ সংযোগ পরিচালনা';

$successMsg = '';
$errorMsg = '';
$testResult = null;

// Handle Delete Bucket
if (isset($_GET['delete_id'])) {
    $delId = (int)$_GET['delete_id'];
    if ($delId > 0) {
        $pdo->prepare("DELETE FROM `r2_buckets` WHERE id = ?")->execute([$delId]);
        header("Location: cloudflare.php?msg=deleted");
        exit;
    }
}

// Handle Toggle Active/Inactive
if (isset($_GET['toggle_id'])) {
    $togId = (int)$_GET['toggle_id'];
    if ($togId > 0) {
        $stmt = $pdo->prepare("SELECT is_active FROM `r2_buckets` WHERE id = ?");
        $stmt->execute([$togId]);
        $curr = $stmt->fetchColumn();
        $newVal = ($curr == 1) ? 0 : 1;
        $pdo->prepare("UPDATE `r2_buckets` SET is_active = ? WHERE id = ?")->execute([$newVal, $togId]);
        header("Location: cloudflare.php?msg=toggled");
        exit;
    }
}

if (isset($_GET['msg'])) {
    if ($_GET['msg'] === 'deleted') {
        $successMsg = ($currentLang === 'en') ? 'Cloudflare R2 bucket deleted successfully!' : 'ক্লাউডফ্লেয়ার R2 বাকেট সফলভাবে মুছে ফেলা হয়েছে!';
    } elseif ($_GET['msg'] === 'toggled') {
        $successMsg = ($currentLang === 'en') ? 'Bucket active status changed successfully!' : 'বাকেটের সক্রিয় স্ট্যাটাস সফলভাবে পরিবর্তন করা হয়েছে!';
    }
}

// Handle Save or Update Bucket Form
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['save_bucket'])) {
    $bucketId       = (int)($_POST['bucket_id'] ?? 0);
    $accountLabel   = trim($_POST['account_label'] ?? '');
    $bucketName     = trim($_POST['bucket_name'] ?? '');
    $accountId      = trim($_POST['account_id'] ?? '');
    $accessKey      = trim($_POST['access_key_id'] ?? '');
    $secretKey      = trim($_POST['secret_access_key'] ?? '');
    $publicUrl      = rtrim(trim($_POST['public_url'] ?? ''), '/');
    $accountLimitGb = max(0.5, (float)($_POST['account_limit_gb'] ?? 10.0));
    $priority       = max(1, (int)($_POST['priority'] ?? 1));
    $isActive       = isset($_POST['is_active']) ? 1 : 0;

    if (empty($accountLabel)) {
        $accountLabel = !empty($bucketName) ? $bucketName : 'R2 Storage';
    }

    if (empty($bucketName) || empty($accountId) || empty($accessKey) || empty($publicUrl)) {
        $errorMsg = ($currentLang === 'en') ? 'Please fill in all required fields (Bucket Name, Account ID, Access Key, and Public URL).' : 'অনুগ্রহ করে সকল আবশ্যকীয় তথ্য (বাকেটের নাম, একাউন্ট আইডি, এক্সেস কি এবং পাবলিক URL) প্রদান করুন।';
    } elseif ($bucketId == 0 && empty($secretKey)) {
        $errorMsg = ($currentLang === 'en') ? 'Secret Access Key is required for new buckets.' : 'নতুন বাকেট যুক্ত করার সময় সিক্রেট এক্সেস কি প্রদান করা আবশ্যক।';
    } else {
        try {
            if ($bucketId > 0) {
                if (!empty($secretKey)) {
                    $stmt = $pdo->prepare("UPDATE `r2_buckets` SET account_label = ?, bucket_name = ?, account_id = ?, access_key_id = ?, secret_access_key = ?, public_url = ?, account_limit_gb = ?, priority = ?, is_active = ? WHERE id = ?");
                    $stmt->execute([$accountLabel, $bucketName, $accountId, $accessKey, $secretKey, $publicUrl, $accountLimitGb, $priority, $isActive, $bucketId]);
                } else {
                    $stmt = $pdo->prepare("UPDATE `r2_buckets` SET account_label = ?, bucket_name = ?, account_id = ?, access_key_id = ?, public_url = ?, account_limit_gb = ?, priority = ?, is_active = ? WHERE id = ?");
                    $stmt->execute([$accountLabel, $bucketName, $accountId, $accessKey, $publicUrl, $accountLimitGb, $priority, $isActive, $bucketId]);
                }
                $successMsg = ($currentLang === 'en') ? 'Cloudflare R2 bucket updated successfully!' : 'ক্লাউডফ্লেয়ার R2 বাকেট তথ্য সফলভাবে আপডেট করা হয়েছে!';
            } else {
                $stmt = $pdo->prepare("INSERT INTO `r2_buckets` (account_label, bucket_name, account_id, access_key_id, secret_access_key, public_url, account_limit_gb, used_bytes, is_active, priority) VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?)");
                $stmt->execute([$accountLabel, $bucketName, $accountId, $accessKey, $secretKey, $publicUrl, $accountLimitGb, $isActive, $priority]);
                $successMsg = ($currentLang === 'en') ? 'New Cloudflare R2 bucket registered successfully!' : 'নতুন ক্লাউডফ্লেয়ার R2 বাকেট সফলভাবে নিবন্ধিত হয়েছে!';
            }
        } catch (Exception $e) {
            $errorMsg = ($currentLang === 'en') ? ('Database Error: ' . $e->getMessage()) : ('ডাটাবেজ ত্রুটি: ' . $e->getMessage());
        }
    }
}

// Handle Test Connection Action
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['test_connection'])) {
    $testId = (int)($_POST['test_id'] ?? 0);
    $bucketData = null;

    if ($testId > 0) {
        $stmt = $pdo->prepare("SELECT * FROM `r2_buckets` WHERE id = ?");
        $stmt->execute([$testId]);
        $bucketData = $stmt->fetch(PDO::FETCH_ASSOC);
    } else {
        $bucketData = [
            'bucket_name' => trim($_POST['test_bucket_name'] ?? ''),
            'account_id' => trim($_POST['test_account_id'] ?? ''),
            'access_key_id' => trim($_POST['test_access_key_id'] ?? ''),
            'secret_access_key' => trim($_POST['test_secret_access_key'] ?? '')
        ];
    }

    if ($bucketData) {
        $r2HelperPath = __DIR__ . '/../backend/r2_helper.php';
        if (!file_exists($r2HelperPath)) {
            $r2HelperPath = __DIR__ . '/r2_helper.php';
        }

        if (file_exists($r2HelperPath)) {
            require_once $r2HelperPath;
            if (class_exists('CloudflareR2')) {
                $testResult = CloudflareR2::testBucketConnection($bucketData);
            } else {
                $testResult = ['success' => false, 'message' => 'CloudflareR2 helper class not found.'];
            }
        } else {
            $testResult = ['success' => false, 'message' => 'r2_helper.php file not found.'];
        }
    }
}

// Fetch all configured buckets from primary `r2_buckets` table
$buckets = [];
$totalLimitGb = 0;
$totalUsedBytes = 0;
$activeCount = 0;

try {
    $stmt = $pdo->query("SELECT * FROM `r2_buckets` ORDER BY `priority` ASC, `id` ASC");
    $buckets = $stmt ? $stmt->fetchAll(PDO::FETCH_ASSOC) : [];
    foreach ($buckets as $b) {
        if (!empty($b['is_active'])) $activeCount++;
        $totalLimitGb += (float)($b['account_limit_gb'] ?? 0);
        $totalUsedBytes += (float)($b['used_bytes'] ?? 0);
    }
} catch (Exception $e) {}

$totalUsedGb = round($totalUsedBytes / (1024 * 1024 * 1024), 2);

// Edit bucket preparation if requested
$editBucket = null;
if (isset($_GET['edit_id'])) {
    $editId = (int)$_GET['edit_id'];
    foreach ($buckets as $b) {
        if ($b['id'] == $editId) {
            $editBucket = $b;
            break;
        }
    }
}

require_once __DIR__ . '/includes/header.php';
?>

<!-- Alert Feedback Messages -->
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

<?php if ($testResult): ?>
    <div class="alert <?= $testResult['success'] ? 'alert-success' : 'alert-danger' ?> alert-dismissible fade show d-flex align-items-center mb-4 rounded-3 shadow-sm border-0" role="alert">
        <i class="bi <?= $testResult['success'] ? 'bi-cloud-check-fill text-success' : 'bi-x-circle-fill text-danger' ?> fs-4 me-3"></i>
        <div>
            <h6 class="fw-bold mb-1"><?= $testResult['success'] ? (($currentLang === 'en') ? 'Cloudflare R2 Connection Succeeded!' : 'ক্লাউডফ্লেয়ার R2 সংযোগ সফল হয়েছে!') : (($currentLang === 'en') ? 'Cloudflare R2 Connection Failed!' : 'ক্লাউডফ্লেয়ার R2 সংযোগ ব্যর্থ হয়েছে!') ?></h6>
            <div class="small"><?= htmlspecialchars($testResult['message']) ?></div>
        </div>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
<?php endif; ?>

<!-- Top 4 Statistics Cards -->
<div class="row g-3 mb-4">
    <div class="col-6 col-lg-3">
        <div class="card-custom text-center p-3">
            <div class="text-muted small fw-bold text-uppercase"><?= ($currentLang === 'en') ? 'Total Buckets' : 'মোট বাকেট' ?></div>
            <div class="fs-3 fw-bold text-dark mt-1"><?= count($buckets) ?></div>
            <small class="text-muted"><?= ($currentLang === 'en') ? 'Configured in R2' : 'সিস্টেমে সংরক্ষিত' ?></small>
        </div>
    </div>
    <div class="col-6 col-lg-3">
        <div class="card-custom text-center p-3">
            <div class="text-muted small fw-bold text-uppercase"><?= ($currentLang === 'en') ? 'Active Buckets' : 'সক্রিয় বাকেট' ?></div>
            <div class="fs-3 fw-bold text-success mt-1"><?= $activeCount ?></div>
            <small class="text-muted"><?= ($currentLang === 'en') ? 'Ready for uploads' : 'আপলোডের জন্য প্রস্তুত' ?></small>
        </div>
    </div>
    <div class="col-6 col-lg-3">
        <div class="card-custom text-center p-3">
            <div class="text-muted small fw-bold text-uppercase"><?= ($currentLang === 'en') ? 'Used Storage' : 'ব্যবহৃত স্টোরেজ' ?></div>
            <div class="fs-3 fw-bold text-primary mt-1"><?= $totalUsedGb ?> GB</div>
            <small class="text-muted"><?= ($currentLang === 'en') ? 'Media & APKs uploaded' : 'ছবি, ভিডিও ও APK' ?></small>
        </div>
    </div>
    <div class="col-6 col-lg-3">
        <div class="card-custom text-center p-3">
            <div class="text-muted small fw-bold text-uppercase"><?= ($currentLang === 'en') ? 'Total Capacity' : 'মোট ধারণক্ষমতা' ?></div>
            <div class="fs-3 fw-bold text-dark mt-1"><?= $totalLimitGb ?> GB</div>
            <small class="text-muted"><?= ($currentLang === 'en') ? 'Multi-bucket quota sum' : 'সকল বাকেটের মোট লিমিট' ?></small>
        </div>
    </div>
</div>

<div class="row g-4">
    <!-- R2 Bucket Configuration Form Card -->
    <div class="col-12 col-lg-5">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center gap-2 mb-3">
                <div class="p-2 bg-info-subtle text-info rounded-3">
                    <i class="bi bi-cloud-arrow-up-fill fs-5"></i>
                </div>
                <div>
                    <h6 class="fw-bold text-dark mb-0">
                        <?= $editBucket ? (($currentLang === 'en') ? 'Edit R2 Bucket' : 'বাকেট সম্পাদনা করুন') : (($currentLang === 'en') ? 'Add Cloudflare R2 Bucket' : 'ক্লাউডফ্লেয়ার R2 বাকেট যুক্ত করুন') ?>
                    </h6>
                    <small class="text-muted"><?= ($currentLang === 'en') ? 'Save credentials to r2_buckets' : 'তথ্যগুলো সরাসরি r2_buckets টেবিলে সংরক্ষিত হবে' ?></small>
                </div>
            </div>

            <form method="POST" action="cloudflare.php">
                <input type="hidden" name="save_bucket" value="1">
                <?php if ($editBucket): ?>
                    <input type="hidden" name="bucket_id" value="<?= (int)$editBucket['id'] ?>">
                <?php endif; ?>

                <div class="row g-3">
                    <div class="col-12">
                        <label class="form-label fw-semibold small"><?= ($currentLang === 'en') ? 'Account Label / Name' : 'অ্যাকাউন্ট লেবেল / নাম' ?> <span class="text-danger">*</span></label>
                        <input type="text" name="account_label" class="form-control" placeholder="e.g. Primary Account 1" value="<?= htmlspecialchars($editBucket['account_label'] ?? '') ?>" required>
                    </div>

                    <div class="col-12">
                        <label class="form-label fw-semibold small"><?= ($currentLang === 'en') ? 'R2 Bucket Name' : 'বাকেটের নাম (Bucket Name)' ?> <span class="text-danger">*</span></label>
                        <input type="text" name="bucket_name" class="form-control" placeholder="e.g. flikata-media" value="<?= htmlspecialchars($editBucket['bucket_name'] ?? '') ?>" required>
                    </div>

                    <div class="col-12">
                        <label class="form-label fw-semibold small"><?= ($currentLang === 'en') ? 'Cloudflare Account ID' : 'ক্লাউডফ্লেয়ার একাউন্ট আইডি (Account ID)' ?> <span class="text-danger">*</span></label>
                        <input type="text" name="account_id" class="form-control" placeholder="e.g. 8a34b29c01f65..." value="<?= htmlspecialchars($editBucket['account_id'] ?? '') ?>" required>
                    </div>

                    <div class="col-12">
                        <label class="form-label fw-semibold small"><?= ($currentLang === 'en') ? 'Access Key ID' : 'এক্সেস কি আইডি (Access Key ID)' ?> <span class="text-danger">*</span></label>
                        <input type="text" name="access_key_id" class="form-control" placeholder="e.g. 74b8686f05e9..." value="<?= htmlspecialchars($editBucket['access_key_id'] ?? '') ?>" required>
                    </div>

                    <div class="col-12">
                        <label class="form-label fw-semibold small"><?= ($currentLang === 'en') ? 'Secret Access Key' : 'সিক্রেট এক্সেস কি (Secret Access Key)' ?> <?= $editBucket ? '<span class="text-muted small">(' . (($currentLang === 'en') ? 'Leave blank to keep current' : 'পরিবর্তন না করতে চাইলে ফাঁকা রাখুন') . ')</span>' : '<span class="text-danger">*</span>' ?></label>
                        <input type="password" name="secret_access_key" class="form-control" placeholder="<?= $editBucket ? '••••••••••••••••••••••••' : 'Cloudflare R2 Secret Access Key' ?>" <?= $editBucket ? '' : 'required' ?>>
                    </div>

                    <div class="col-12">
                        <label class="form-label fw-semibold small"><?= ($currentLang === 'en') ? 'Public Domain / Endpoint URL' : 'পাবলিক ডোমেইন বা ডেভ URL (Public URL)' ?> <span class="text-danger">*</span></label>
                        <input type="url" name="public_url" class="form-control" placeholder="https://pub-...r2.dev or https://media.yourdomain.com" value="<?= htmlspecialchars($editBucket['public_url'] ?? '') ?>" required>
                        <small class="text-muted" style="font-size: 11px;"><?= ($currentLang === 'en') ? 'Public URL used for serving media, avatars, videos and APK updates' : 'মিডিয়া, ছবি, ভিডিও এবং APK আপডেটের লিংক প্রদর্শনের জন্য পাবলিক ডোমেইন' ?></small>
                    </div>

                    <div class="col-6">
                        <label class="form-label fw-semibold small"><?= ($currentLang === 'en') ? 'Storage Limit (GB)' : 'স্টোরেজ লিমিট (GB)' ?></label>
                        <input type="number" step="0.5" name="account_limit_gb" class="form-control" value="<?= htmlspecialchars($editBucket['account_limit_gb'] ?? '10.0') ?>" min="0.5" required>
                    </div>

                    <div class="col-6">
                        <label class="form-label fw-semibold small"><?= ($currentLang === 'en') ? 'Priority Order' : 'অগ্রাধিকার ক্রম (Priority)' ?></label>
                        <input type="number" name="priority" class="form-control" value="<?= (int)($editBucket['priority'] ?? 1) ?>" min="1" required>
                    </div>

                    <div class="col-12 d-flex align-items-center">
                        <div class="form-check form-switch mt-2">
                            <input class="form-check-input" type="checkbox" id="activeSwitch" name="is_active" value="1" <?= (!isset($editBucket) || $editBucket['is_active'] == 1) ? 'checked' : '' ?>>
                            <label class="form-check-label fw-semibold small" for="activeSwitch"><?= ($currentLang === 'en') ? 'Active (Accept Uploads)' : 'সক্রিয় (আপলোড গ্রহণ করবে)' ?></label>
                        </div>
                    </div>

                    <div class="col-12 mt-4 d-flex gap-2">
                        <button type="submit" class="btn btn-primary flex-grow-1 py-2 fw-semibold">
                            <i class="bi bi-save-fill me-2"></i><?= $editBucket ? (($currentLang === 'en') ? 'Update Bucket' : 'বাকেট আপডেট করুন') : (($currentLang === 'en') ? 'Save Bucket' : 'বাকেট সংরক্ষণ করুন') ?>
                        </button>
                        <?php if ($editBucket): ?>
                            <a href="cloudflare.php" class="btn btn-outline-secondary py-2 px-3"><?= ($currentLang === 'en') ? 'Cancel' : 'বাতিল' ?></a>
                        <?php endif; ?>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- Configured Buckets List -->
    <div class="col-12 col-lg-7">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <div class="d-flex align-items-center gap-2">
                    <div class="p-2 bg-primary-subtle text-primary rounded-3">
                        <i class="bi bi-server fs-5"></i>
                    </div>
                    <div>
                        <h6 class="fw-bold text-dark mb-0"><?= ($currentLang === 'en') ? 'Configured R2 Accounts' : 'সংযুক্ত ক্লাউডফ্লেয়ার বাকেটসমূহ' ?></h6>
                        <small class="text-muted"><?= count($buckets) ?> <?= ($currentLang === 'en') ? 'buckets found in r2_buckets' : 'টি অ্যাকাউন্ট সংরক্ষিত আছে' ?></small>
                    </div>
                </div>
                <?php if ($editBucket): ?>
                    <a href="cloudflare.php" class="btn btn-sm btn-outline-primary rounded-pill">
                        <i class="bi bi-plus-lg me-1"></i><?= ($currentLang === 'en') ? 'Add New' : 'নতুন বাকেট যোগ করুন' ?>
                    </a>
                <?php endif; ?>
            </div>

            <?php if (empty($buckets)): ?>
                <div class="text-center py-5">
                    <i class="bi bi-clouds fs-1 text-muted d-block mb-2"></i>
                    <h6 class="fw-bold text-secondary"><?= ($currentLang === 'en') ? 'No Cloudflare R2 Buckets Found' : 'এখনও কোনো ক্লাউডফ্লেয়ার বাকেট যুক্ত করা হয়নি' ?></h6>
                    <p class="text-muted small"><?= ($currentLang === 'en') ? 'Add your Cloudflare R2 bucket details using the form on the left.' : 'বাম পাশের ফর্মটি পূরণ করে আপনার ক্লাউডফ্লেয়ার বাকেট যুক্ত করুন।' ?></p>
                </div>
            <?php else: ?>
                <div class="d-flex flex-column gap-3">
                    <?php foreach ($buckets as $b): 
                        $limitBytes = (float)($b['account_limit_gb'] ?? 10) * 1024 * 1024 * 1024;
                        $usedBytes = (float)($b['used_bytes'] ?? 0);
                        $usedGb = round($usedBytes / (1024 * 1024 * 1024), 2);
                        $percent = ($limitBytes > 0) ? min(100, round(($usedBytes / $limitBytes) * 100, 1)) : 0;
                        $isFull = ($usedBytes >= $limitBytes && $limitBytes > 0);
                    ?>
                        <div class="border rounded-3 p-3 bg-white shadow-sm" style="border-left: 4px solid <?= $b['is_active'] ? '#10b981' : '#94a3b8' ?> !important;">
                            <div class="d-flex align-items-start justify-content-between mb-2">
                                <div>
                                    <div class="d-flex align-items-center gap-2">
                                        <h6 class="fw-bold text-dark mb-0"><?= htmlspecialchars($b['account_label']) ?></h6>
                                        <span class="badge bg-light text-muted border" style="font-size: 11px;">#<?= $b['id'] ?></span>
                                    </div>
                                    <small class="text-muted d-block mt-1">
                                        <i class="bi bi-folder2-open text-primary me-1"></i><?= ($currentLang === 'en') ? 'Bucket:' : 'বাকেট:' ?> <strong><?= htmlspecialchars($b['bucket_name']) ?></strong>
                                    </small>
                                </div>
                                <div class="d-flex align-items-center gap-2">
                                    <a href="cloudflare.php?toggle_id=<?= (int)$b['id'] ?>" class="text-decoration-none">
                                        <?php if ($b['is_active'] == 1): ?>
                                            <span class="badge bg-success-subtle text-success border border-success-subtle">
                                                <i class="bi bi-check-circle me-1"></i><?= ($currentLang === 'en') ? 'Active' : 'সক্রিয়' ?>
                                            </span>
                                        <?php else: ?>
                                            <span class="badge bg-secondary-subtle text-secondary border">
                                                <i class="bi bi-pause-circle me-1"></i><?= ($currentLang === 'en') ? 'Inactive' : 'নিষ্ক্রিয়' ?>
                                            </span>
                                        <?php endif; ?>
                                    </a>
                                </div>
                            </div>

                            <!-- Storage Usage Progress Bar -->
                            <div class="my-2">
                                <div class="d-flex justify-content-between small text-muted mb-1" style="font-size: 11.5px;">
                                    <span><?= ($currentLang === 'en') ? 'Used:' : 'ব্যবহৃত:' ?> <strong><?= $usedGb ?> GB</strong> / <?= htmlspecialchars($b['account_limit_gb']) ?> GB</span>
                                    <span class="fw-bold <?= $percent > 90 ? 'text-danger' : 'text-primary' ?>"><?= $percent ?>%</span>
                                </div>
                                <div class="progress" style="height: 6px;">
                                    <div class="progress-bar <?= $percent > 90 ? 'bg-danger' : ($percent > 75 ? 'bg-warning' : 'bg-primary') ?>" role="progressbar" style="width: <?= $percent ?>%"></div>
                                </div>
                            </div>

                            <!-- Credential Details -->
                            <div class="bg-light p-2 rounded-2 small mt-2" style="font-size: 11.5px;">
                                <div class="row g-1">
                                    <div class="col-md-6 text-truncate">
                                        <span class="text-muted"><?= ($currentLang === 'en') ? 'Account ID:' : 'একাউন্ট আইডি:' ?></span>
                                        <code class="text-dark"><?= htmlspecialchars(substr($b['account_id'], 0, 10)) ?>••••••</code>
                                    </div>
                                    <div class="col-md-6 text-truncate">
                                        <span class="text-muted"><?= ($currentLang === 'en') ? 'Access Key:' : 'এক্সেস কি:' ?></span>
                                        <code class="text-dark"><?= htmlspecialchars(substr($b['access_key_id'], 0, 8)) ?>••••••</code>
                                    </div>
                                    <div class="col-12 text-truncate mt-1">
                                        <span class="text-muted"><?= ($currentLang === 'en') ? 'Public Domain:' : 'পাবলিক URL:' ?></span>
                                        <a href="<?= htmlspecialchars($b['public_url']) ?>" target="_blank" class="text-decoration-none">
                                            <?= htmlspecialchars($b['public_url']) ?>
                                        </a>
                                    </div>
                                </div>
                            </div>

                            <!-- Action Buttons -->
                            <div class="d-flex align-items-center justify-content-between mt-3 pt-2 border-top">
                                <div>
                                    <form method="POST" action="cloudflare.php" class="d-inline">
                                        <input type="hidden" name="test_connection" value="1">
                                        <input type="hidden" name="test_id" value="<?= (int)$b['id'] ?>">
                                        <button type="submit" class="btn btn-outline-info btn-sm rounded-pill px-3 py-1" style="font-size: 11.5px;" title="<?= ($currentLang === 'en') ? 'Test Ping Connection' : 'লাইভ কানেকশন টেস্ট' ?>">
                                            <i class="bi bi-broadcast me-1"></i><?= ($currentLang === 'en') ? 'Ping Test' : 'টেস্ট করুন' ?>
                                        </button>
                                    </form>
                                </div>
                                <div class="d-flex align-items-center gap-1">
                                    <a href="cloudflare.php?edit_id=<?= (int)$b['id'] ?>" class="btn btn-sm btn-outline-primary px-2 py-1" title="<?= ($currentLang === 'en') ? 'Edit Bucket' : 'সম্পাদনা' ?>">
                                        <i class="bi bi-pencil-fill me-1"></i><?= ($currentLang === 'en') ? 'Edit' : 'এডিট' ?>
                                    </a>
                                    <a href="cloudflare.php?delete_id=<?= (int)$b['id'] ?>" class="btn btn-sm btn-outline-danger px-2 py-1" title="<?= ($currentLang === 'en') ? 'Delete Bucket' : 'মুছে ফেলুন' ?>" onclick="return confirm('<?= ($currentLang === 'en') ? 'Are you sure you want to delete this bucket configuration?' : 'আপনি কি নিশ্চিতভাবে এই ক্লাউডফ্লেয়ার বাকেটটি মুছে ফেলতে চান?' ?>');">
                                        <i class="bi bi-trash-fill"></i>
                                    </a>
                                </div>
                            </div>
                        </div>
                    <?php endforeach; ?>
                </div>
            <?php endif; ?>
        </div>
    </div>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
