<?php
/**
 * Cloudflare R2 Storage Management Dashboard
 * Strictly secured: Requires Admin Authentication.
 */
if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

// Strict Admin Authentication Check
if (empty($_SESSION['admin_logged_in'])) {
    header("Location: ../admin/login.php");
    exit;
}

// Redirect authenticated admin to central admin panel's Cloudflare manager
header("Location: ../admin/cloudflare.php");
exit;


// Handle AJAX actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';

    // 1. Add New Bucket
    if ($action === 'add') {
        $label       = trim($_POST['account_label'] ?? '');
        $bucketName  = trim($_POST['bucket_name'] ?? '');
        $accountId   = trim($_POST['account_id'] ?? '');
        $accessKey   = trim($_POST['access_key_id'] ?? '');
        $secretKey   = trim($_POST['secret_access_key'] ?? '');
        $publicUrl   = trim($_POST['public_url'] ?? '');
        $limitGb     = (float)($_POST['account_limit_gb'] ?? 10.0);

        if (!$label || !$bucketName || !$accountId || !$accessKey || !$secretKey || !$publicUrl) {
            jsonResponse(false, null, 'Please fill in all required fields!');
        }

        $stmt = $pdo->prepare("
            INSERT INTO r2_buckets (account_label, bucket_name, account_id, access_key_id, secret_access_key, public_url, account_limit_gb, used_bytes, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, 0, 1)
        ");
        $stmt->execute([$label, $bucketName, $accountId, $accessKey, $secretKey, $publicUrl, $limitGb]);

        jsonResponse(true, ['id' => $pdo->lastInsertId()], 'Cloudflare R2 Bucket added successfully!');
    }

    // 2. Edit Bucket
    if ($action === 'edit') {
        $id          = (int)($_POST['id'] ?? 0);
        $label       = trim($_POST['account_label'] ?? '');
        $bucketName  = trim($_POST['bucket_name'] ?? '');
        $accountId   = trim($_POST['account_id'] ?? '');
        $accessKey   = trim($_POST['access_key_id'] ?? '');
        $secretKey   = trim($_POST['secret_access_key'] ?? '');
        $publicUrl   = trim($_POST['public_url'] ?? '');
        $limitGb     = (float)($_POST['account_limit_gb'] ?? 10.0);

        if (!$id || !$label || !$bucketName || !$accountId || !$accessKey || !$publicUrl) {
            jsonResponse(false, null, 'Please fill in all required fields!');
        }

        if ($secretKey) {
            $stmt = $pdo->prepare("
                UPDATE r2_buckets 
                SET account_label = ?, bucket_name = ?, account_id = ?, access_key_id = ?, secret_access_key = ?, public_url = ?, account_limit_gb = ?
                WHERE id = ?
            ");
            $stmt->execute([$label, $bucketName, $accountId, $accessKey, $secretKey, $publicUrl, $limitGb, $id]);
        } else {
            $stmt = $pdo->prepare("
                UPDATE r2_buckets 
                SET account_label = ?, bucket_name = ?, account_id = ?, access_key_id = ?, public_url = ?, account_limit_gb = ?
                WHERE id = ?
            ");
            $stmt->execute([$label, $bucketName, $accountId, $accessKey, $publicUrl, $limitGb, $id]);
        }

        jsonResponse(true, null, 'Bucket updated successfully!');
    }

    // 3. Toggle Active/Inactive Status
    if ($action === 'toggle_active') {
        $id = (int)($_POST['id'] ?? 0);
        $isActive = (int)($_POST['is_active'] ?? 0);

        $stmt = $pdo->prepare("UPDATE r2_buckets SET is_active = ? WHERE id = ?");
        $stmt->execute([$isActive, $id]);

        jsonResponse(true, ['is_active' => $isActive], 'Bucket status updated.');
    }

    // 4. Delete Bucket
    if ($action === 'delete') {
        $id = (int)($_POST['id'] ?? 0);
        $stmt = $pdo->prepare("DELETE FROM r2_buckets WHERE id = ?");
        $stmt->execute([$id]);

        jsonResponse(true, null, 'Bucket deleted successfully.');
    }

    // 5. Test Bucket Connection
    if ($action === 'test') {
        $id = (int)($_POST['id'] ?? 0);
        $stmt = $pdo->prepare("SELECT * FROM r2_buckets WHERE id = ?");
        $stmt->execute([$id]);
        $bucket = $stmt->fetch();

        if (!$bucket) {
            jsonResponse(false, null, 'Bucket not found!');
        }

        $result = CloudflareR2::testBucketConnection($bucket);
        jsonResponse($result['success'], null, $result['message']);
    }

    exit;
}

// Send HTML content-type for browser rendering
header('Content-Type: text/html; charset=utf-8');

$buckets = [];
$dbError = null;

try {
    $stmt = $pdo->query("SELECT * FROM r2_buckets ORDER BY id DESC");
    $buckets = $stmt->fetchAll();
} catch (Exception $e) {
    $dbError = $e->getMessage();
}

// Calculate total statistics
$totalBuckets = count($buckets);
$activeBuckets = 0;
$totalLimitGb = 0;
$totalUsedBytes = 0;

foreach ($buckets as $b) {
    if (!empty($b['is_active'])) $activeBuckets++;
    $totalLimitGb += (float)($b['account_limit_gb'] ?? 0);
    $totalUsedBytes += (float)($b['used_bytes'] ?? 0);
}
$totalUsedGb = round($totalUsedBytes / (1024 * 1024 * 1024), 2);
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cloudflare R2 Storage Manager - Flikata</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <style>
        :root {
            --flikata-primary: #1877F2;
            --flikata-dark: #0F172A;
            --flikata-surface: #F8FAFC;
        }
        body {
            background-color: #F0F2F5;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            color: #1E293B;
            padding-bottom: 100px;
        }
        .navbar-flikata {
            background: linear-gradient(135deg, #1877F2 0%, #0D5AC4 100%);
            color: white;
            box-shadow: 0 4px 12px rgba(24, 119, 242, 0.2);
        }
        .stat-card {
            background: white;
            border-radius: 16px;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.04);
            border: 1px solid #E2E8F0;
            transition: transform 0.2s ease;
        }
        .stat-card:hover {
            transform: translateY(-2px);
        }
        .bucket-card {
            background: white;
            border-radius: 18px;
            padding: 22px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.05);
            border: 1px solid #E2E8F0;
            margin-bottom: 20px;
            transition: all 0.3s ease;
            position: relative;
        }
        .bucket-card.active-bucket {
            border-left: 5px solid #10B981;
        }
        .bucket-card.inactive-bucket {
            border-left: 5px solid #94A3B8;
            opacity: 0.85;
        }
        .bucket-card.full-bucket {
            border-left: 5px solid #F59E0B;
        }
        .progress {
            height: 8px;
            border-radius: 6px;
            background-color: #E2E8F0;
        }
        .fab-add {
            position: fixed;
            bottom: 30px;
            right: 30px;
            width: 62px;
            height: 62px;
            border-radius: 50%;
            background-color: var(--flikata-primary);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
            box-shadow: 0 8px 24px rgba(24, 119, 242, 0.4);
            cursor: pointer;
            z-index: 1000;
            border: none;
            transition: transform 0.2s, background-color 0.2s;
        }
        .fab-add:hover {
            transform: scale(1.08);
            background-color: #0D5AC4;
            color: white;
        }
        .badge-active {
            background-color: #D1FAE5;
            color: #065F46;
            font-weight: 600;
            padding: 6px 12px;
            border-radius: 20px;
        }
        .badge-inactive {
            background-color: #F1F5F9;
            color: #475569;
            font-weight: 600;
            padding: 6px 12px;
            border-radius: 20px;
        }
        .badge-full {
            background-color: #FEF3C7;
            color: #92400E;
            font-weight: 600;
            padding: 6px 12px;
            border-radius: 20px;
        }
        .key-text {
            font-family: monospace;
            font-size: 13px;
            color: #64748B;
            background: #F8FAFC;
            padding: 2px 6px;
            border-radius: 4px;
        }
    </style>
</head>
<body>

<!-- Navbar -->
<nav class="navbar navbar-flikata py-3 mb-4">
    <div class="container d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center">
            <span class="fs-3 fw-bold me-2">⚡ Flikata</span>
            <span class="badge bg-white text-primary rounded-pill px-3 py-2 ms-2">Cloudflare R2 Storage</span>
        </div>
        <div>
            <button class="btn btn-light fw-bold text-primary px-3 rounded-pill" onclick="openAddModal()">
                <i class="bi bi-plus-circle me-1"></i> Add Bucket
            </button>
        </div>
    </div>
</nav>

<div class="container">

    <?php if ($dbError): ?>
        <div class="alert alert-danger rounded-4 shadow-sm mb-4">
            <h5 class="fw-bold mb-1"><i class="bi bi-exclamation-octagon-fill me-2"></i>Database Connection Notice</h5>
            <p class="mb-2">Could not connect to MySQL database or <code>r2_buckets</code> table is missing.</p>
            <p class="mb-0 small text-dark"><strong>Action required:</strong> 1. Open <code>config/db.php</code> and set your database user & password. 2. Import <code>database.sql</code> into phpMyAdmin.</p>
            <div class="mt-2 text-muted small"><strong>Error details:</strong> <?= htmlspecialchars($dbError) ?></div>
        </div>
    <?php endif; ?>

    <!-- Top Statistics Overview -->
    <div class="row g-3 mb-4">
        <div class="col-6 col-md-3">
            <div class="stat-card">
                <div class="text-secondary small fw-bold">TOTAL BUCKETS</div>
                <div class="fs-3 fw-bold text-dark mt-1"><?= $totalBuckets ?></div>
                <div class="small text-muted">Configured in system</div>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="stat-card">
                <div class="text-secondary small fw-bold">ACTIVE BUCKETS</div>
                <div class="fs-3 fw-bold text-success mt-1"><?= $activeBuckets ?></div>
                <div class="small text-muted">Ready for uploads</div>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="stat-card">
                <div class="text-secondary small fw-bold">STORAGE USED</div>
                <div class="fs-3 fw-bold text-primary mt-1"><?= $totalUsedGb ?> GB</div>
                <div class="small text-muted">Images & Videos uploaded</div>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="stat-card">
                <div class="text-secondary small fw-bold">TOTAL CAPACITY</div>
                <div class="fs-3 fw-bold text-dark mt-1"><?= $totalLimitGb ?> GB</div>
                <div class="small text-muted">Multi-bucket quota sum</div>
            </div>
        </div>
    </div>

    <!-- Info Banner -->
    <div class="alert alert-primary bg-white border-primary shadow-sm rounded-4 mb-4 p-3 d-flex align-items-center justify-content-between">
        <div class="d-flex align-items-center">
            <i class="bi bi-shield-check fs-2 text-primary me-3"></i>
            <div>
                <strong>Automatic Failover & Multi-Bucket Rollover:</strong>
                <p class="mb-0 text-muted small">
                    When an active bucket reaches its specified account limit (GB), Flikata automatically switches to the next active bucket seamlessly without any interruption!
                </p>
            </div>
        </div>
        <a href="#bucketsList" class="btn btn-sm btn-outline-primary rounded-pill px-3">View Buckets</a>
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
        <h4 class="fw-bold mb-0">Managed R2 Buckets (<?= count($buckets) ?>)</h4>
        <button class="btn btn-primary rounded-pill px-3" onclick="openAddModal()">
            <i class="bi bi-plus-lg me-1"></i> Add Cloudflare Bucket
        </button>
    </div>

    <!-- Buckets List Grid -->
    <div class="row" id="bucketsList">
        <?php if (empty($buckets)): ?>
            <div class="col-12 text-center py-5">
                <div class="p-5 bg-white rounded-4 shadow-sm">
                    <i class="bi bi-cloud-arrow-up display-1 text-primary"></i>
                    <h4 class="mt-3 fw-bold">No Cloudflare R2 Buckets Added Yet</h4>
                    <p class="text-muted">Click the button below or the "+" button on the bottom right to connect your first Cloudflare R2 bucket.</p>
                    <button class="btn btn-primary rounded-pill px-4 py-2 mt-2" onclick="openAddModal()">
                        <i class="bi bi-plus-lg me-1"></i> Connect R2 Bucket
                    </button>
                </div>
            </div>
        <?php else: ?>
            <?php foreach ($buckets as $b): 
                $limitBytes = (float)$b['account_limit_gb'] * 1024 * 1024 * 1024;
                $usedBytes = (float)$b['used_bytes'];
                $usedGb = round($usedBytes / (1024 * 1024 * 1024), 2);
                $percent = ($limitBytes > 0) ? min(100, round(($usedBytes / $limitBytes) * 100, 1)) : 0;
                $isFull = ($usedBytes >= $limitBytes && $limitBytes > 0);

                $statusClass = $isFull ? 'full-bucket' : ($b['is_active'] ? 'active-bucket' : 'inactive-bucket');
            ?>
                <div class="col-12 col-lg-6">
                    <div class="bucket-card <?= $statusClass ?>">
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <div>
                                <h5 class="fw-bold mb-1 text-dark"><?= htmlspecialchars($b['account_label']) ?></h5>
                                <div class="text-muted small">
                                    <i class="bi bi-folder2-open text-primary me-1"></i>
                                    Bucket: <strong class="text-dark"><?= htmlspecialchars($b['bucket_name']) ?></strong>
                                </div>
                            </div>
                            <div>
                                <?php if ($isFull): ?>
                                    <span class="badge-full"><i class="bi bi-exclamation-triangle me-1"></i> Quota Full</span>
                                <?php elseif ($b['is_active']): ?>
                                    <span class="badge-active"><i class="bi bi-check-circle-fill me-1"></i> Active</span>
                                <?php else: ?>
                                    <span class="badge-inactive"><i class="bi bi-pause-circle me-1"></i> Inactive</span>
                                <?php endif; ?>
                            </div>
                        </div>

                        <!-- Progress Bar for Storage Limit -->
                        <div class="my-3">
                            <div class="d-flex justify-content-between small text-muted mb-1">
                                <span>Used: <strong><?= $usedGb ?> GB</strong> / <?= $b['account_limit_gb'] ?> GB</span>
                                <span class="fw-bold <?= $percent > 90 ? 'text-danger' : 'text-primary' ?>"><?= $percent ?>%</span>
                            </div>
                            <div class="progress">
                                <div class="progress-bar <?= $percent > 90 ? 'bg-danger' : ($percent > 75 ? 'bg-warning' : 'bg-primary') ?>" 
                                     role="progressbar" style="width: <?= $percent ?>%"></div>
                            </div>
                        </div>

                        <!-- Bucket Details -->
                        <div class="bg-light p-3 rounded-3 small mb-3">
                            <div class="row g-2">
                                <div class="col-12">
                                    <span class="text-muted">Account ID:</span> 
                                    <span class="key-text"><?= htmlspecialchars(substr($b['account_id'], 0, 10)) ?>••••••</span>
                                </div>
                                <div class="col-12">
                                    <span class="text-muted">Access Key:</span> 
                                    <span class="key-text"><?= htmlspecialchars(substr($b['access_key_id'], 0, 8)) ?>••••••</span>
                                </div>
                                <div class="col-12">
                                    <span class="text-muted">Public Endpoint:</span> 
                                    <a href="<?= htmlspecialchars($b['public_url']) ?>" target="_blank" class="text-truncate d-inline-block align-middle" style="max-width: 250px;">
                                        <?= htmlspecialchars($b['public_url']) ?>
                                    </a>
                                </div>
                            </div>
                        </div>

                        <!-- Actions Row -->
                        <div class="d-flex justify-content-between align-items-center pt-2 border-top">
                            <!-- Active Toggle Switch -->
                            <div class="form-check form-switch mb-0">
                                <input class="form-check-input" type="checkbox" role="switch" id="switch_<?= $b['id'] ?>" 
                                       <?= $b['is_active'] ? 'checked' : '' ?> 
                                       onchange="toggleBucketStatus(<?= $b['id'] ?>, this.checked)">
                                <label class="form-check-label small fw-semibold" for="switch_<?= $b['id'] ?>">
                                    <?= $b['is_active'] ? 'Enabled' : 'Disabled' ?>
                                </label>
                            </div>

                            <!-- Buttons -->
                            <div class="btn-group">
                                <button class="btn btn-sm btn-outline-success rounded-pill px-3 me-2" onclick="testConnection(<?= $b['id'] ?>, this)">
                                    <i class="bi bi-hdd-network me-1"></i> Test
                                </button>
                                <button class="btn btn-sm btn-outline-primary rounded-pill px-3 me-2" onclick='openEditModal(<?= json_encode($b) ?>)'>
                                    <i class="bi bi-pencil me-1"></i> Edit
                                </button>
                                <button class="btn btn-sm btn-outline-danger rounded-pill px-2" onclick="deleteBucket(<?= $b['id'] ?>)">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            <?php endforeach; ?>
        <?php endif; ?>
    </div>
</div>

<!-- Floating Action Button + -->
<button class="fab-add" onclick="openAddModal()" title="Add New Cloudflare R2 Bucket">
    <i class="bi bi-plus-lg"></i>
</button>

<!-- Add / Edit Modal -->
<div class="modal fade" id="bucketModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0 shadow">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold" id="modalTitle">Add Cloudflare R2 Bucket</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body pt-2">
                <p class="text-muted small">Enter your Cloudflare R2 storage credentials to connect this bucket.</p>
                <form id="bucketForm">
                    <input type="hidden" name="action" id="formAction" value="add">
                    <input type="hidden" name="id" id="bucketId" value="">

                    <div class="mb-3">
                        <label class="form-label small fw-bold">Account Label</label>
                        <input type="text" class="form-control rounded-3" name="account_label" id="account_label" 
                               placeholder="e.g. Primary R2 Bucket, Videos CDN" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-bold">Bucket Name</label>
                        <input type="text" class="form-control rounded-3" name="bucket_name" id="bucket_name" 
                               placeholder="e.g. flikata-media-storage" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-bold">Cloudflare Account ID</label>
                        <input type="text" class="form-control rounded-3" name="account_id" id="account_id" 
                               placeholder="e.g. d6f8a42... (Found in R2 dashboard overview)" required>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label small fw-bold">Access Key ID</label>
                            <input type="text" class="form-control rounded-3" name="access_key_id" id="access_key_id" 
                                   placeholder="R2 API Token Access Key" required>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label small fw-bold">Secret Access Key</label>
                            <input type="password" class="form-control rounded-3" name="secret_access_key" id="secret_access_key" 
                                   placeholder="Leave blank to keep current" required>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-bold">Public URL Endpoint (CDN/R2)</label>
                        <input type="url" class="form-control rounded-3" name="public_url" id="public_url" 
                               placeholder="e.g. https://pub-xxx.r2.dev or https://media.yourdomain.com" required>
                        <div class="form-text">The public custom domain or r2.dev domain linked to this bucket.</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-bold">Account Limit (GB)</label>
                        <div class="input-group">
                            <input type="number" step="0.1" class="form-control rounded-start-3" name="account_limit_gb" id="account_limit_gb" 
                                   value="10.0" min="0.1" required>
                            <span class="input-group-text rounded-end-3">GB</span>
                        </div>
                        <div class="form-text">When uploads reach this limit, system will automatically upload to the next active bucket.</div>
                    </div>

                    <div class="d-flex justify-content-end gap-2 mt-4">
                        <button type="button" class="btn btn-light rounded-pill px-4" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary rounded-pill px-4" id="submitBtn">Save Bucket</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- Test Connection Toast/Alert Modal -->
<div class="modal fade" id="resultModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content rounded-4 border-0 shadow text-center p-4">
            <div id="resultIcon" class="mb-2"></div>
            <h5 class="fw-bold mb-2" id="resultTitle">Test Result</h5>
            <p class="text-muted small mb-4" id="resultMsg"></p>
            <button type="button" class="btn btn-primary rounded-pill w-100" data-bs-dismiss="modal">OK</button>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const bucketModal = new bootstrap.Modal(document.getElementById('bucketModal'));
    const resultModal = new bootstrap.Modal(document.getElementById('resultModal'));

    function openAddModal() {
        document.getElementById('formAction').value = 'add';
        document.getElementById('bucketId').value = '';
        document.getElementById('modalTitle').innerText = 'Add Cloudflare R2 Bucket';
        document.getElementById('bucketForm').reset();
        document.getElementById('account_limit_gb').value = '10.0';
        document.getElementById('secret_access_key').required = true;
        document.getElementById('secret_access_key').placeholder = 'R2 API Token Secret Key';
        bucketModal.show();
    }

    function openEditModal(bucket) {
        document.getElementById('formAction').value = 'edit';
        document.getElementById('bucketId').value = bucket.id;
        document.getElementById('modalTitle').innerText = 'Edit Cloudflare R2 Bucket';
        document.getElementById('account_label').value = bucket.account_label;
        document.getElementById('bucket_name').value = bucket.bucket_name;
        document.getElementById('account_id').value = bucket.account_id;
        document.getElementById('access_key_id').value = bucket.access_key_id;
        document.getElementById('secret_access_key').value = '';
        document.getElementById('secret_access_key').required = false;
        document.getElementById('secret_access_key').placeholder = 'Leave blank to keep existing secret';
        document.getElementById('public_url').value = bucket.public_url;
        document.getElementById('account_limit_gb').value = bucket.account_limit_gb;
        bucketModal.show();
    }

    // Handle Form Submit
    document.getElementById('bucketForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const submitBtn = document.getElementById('submitBtn');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Saving...';

        const formData = new FormData(this);

        fetch('storage.php', {
            method: 'POST',
            body: formData
        })
        .then(res => res.json())
        .then(data => {
            submitBtn.disabled = false;
            submitBtn.innerText = 'Save Bucket';
            if (data.success) {
                bucketModal.hide();
                window.location.reload();
            } else {
                alert('Error: ' + data.message);
            }
        })
        .catch(err => {
            submitBtn.disabled = false;
            submitBtn.innerText = 'Save Bucket';
            alert('Request failed: ' + err);
        });
    });

    // Toggle Active Status
    function toggleBucketStatus(id, isActive) {
        const formData = new FormData();
        formData.append('action', 'toggle_active');
        formData.append('id', id);
        formData.append('is_active', isActive ? 1 : 0);

        fetch('storage.php', {
            method: 'POST',
            body: formData
        })
        .then(res => res.json())
        .then(data => {
            if (!data.success) {
                alert('Error updating status: ' + data.message);
            } else {
                window.location.reload();
            }
        });
    }

    // Delete Bucket
    function deleteBucket(id) {
        if (!confirm('Are you sure you want to delete this Cloudflare R2 bucket configuration?')) {
            return;
        }

        const formData = new FormData();
        formData.append('action', 'delete');
        formData.append('id', id);

        fetch('storage.php', {
            method: 'POST',
            body: formData
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                window.location.reload();
            } else {
                alert('Error: ' + data.message);
            }
        });
    }

    // Test Bucket Connection
    function testConnection(id, btn) {
        const originalText = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Testing...';

        const formData = new FormData();
        formData.append('action', 'test');
        formData.append('id', id);

        fetch('storage.php', {
            method: 'POST',
            body: formData
        })
        .then(res => res.json())
        .then(data => {
            btn.disabled = false;
            btn.innerHTML = originalText;

            const icon = document.getElementById('resultIcon');
            const title = document.getElementById('resultTitle');
            const msg = document.getElementById('resultMsg');

            if (data.success) {
                icon.innerHTML = '<i class="bi bi-check-circle-fill text-success display-4"></i>';
                title.innerText = 'Connection Successful!';
                title.className = 'fw-bold mb-2 text-success';
                msg.innerText = data.message;
            } else {
                icon.innerHTML = '<i class="bi bi-x-circle-fill text-danger display-4"></i>';
                title.innerText = 'Connection Failed';
                title.className = 'fw-bold mb-2 text-danger';
                msg.innerText = data.message;
            }
            resultModal.show();
        })
        .catch(err => {
            btn.disabled = false;
            btn.innerHTML = originalText;
            alert('Test request error: ' + err);
        });
    }
</script>
</body>
</html>
