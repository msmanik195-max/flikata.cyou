<?php
/**
 * Monetization Requests Management
 * Flikata Admin Control Panel
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'মনিটাইজেশন রিকোয়েস্ট - Flikata Admin';
$pageHeading = 'মনিটাইজেশন রিকোয়েস্ট (Monetization Requests)';
$pageSubheading = 'কনটেন্ট ক্রিয়েটরদের মনিটাইজেশন আবেদন পর্যালোচনা ও অনুমোদন করুন';

$msg = '';
$err = '';

// Handle Approve / Reject
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action'])) {
    $reqId = intval($_POST['request_id'] ?? 0);
    $action = $_POST['action'];

    if ($action === 'approve') {
        try {
            $stmt = $pdo->prepare("SELECT * FROM monetization_requests WHERE id = ?");
            $stmt->execute([$reqId]);
            $req = $stmt->fetch();

            if ($req) {
                $userId = $req['user_id'];

                $pdo->beginTransaction();
                // 1. Update request status
                $upReq = $pdo->prepare("UPDATE monetization_requests SET status = 'approved', updated_at = NOW() WHERE id = ?");
                $upReq->execute([$reqId]);

                // 2. Enable monetization for user
                $upUser = $pdo->prepare("UPDATE users SET is_monetized = 1 WHERE id = ?");
                $upUser->execute([$userId]);

                // 3. Notify user
                try {
                    $notif = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, message, created_at) VALUES (?, ?, 'monetization', 'অভিনন্দন! আপনার মনিটাইজেশন আবেদন সফলভাবে অনুমোদন (Approved) করা হয়েছে। আপনার কনটেন্টে এখন থেকে ৫৫% রেভিনিউ আর্নিং চালু হয়েছে!', NOW())");
                    $notif->execute([$userId, 1]);
                } catch (Exception $e) {}

                $pdo->commit();
                $msg = 'মনিটাইজেশন আবেদন সফলভাবে অনুমোদন (Approved) করা হয়েছে এবং ব্যবহারকারীর একাউন্টে মনিটাইজেশন চালু হয়েছে!';
            }
        } catch (Exception $e) {
            if ($pdo->inTransaction()) $pdo->rollBack();
            $err = 'অনুমোদন ব্যর্থ হয়েছে: ' . $e->getMessage();
        }
    } elseif ($action === 'reject') {
        $reason = trim($_POST['rejection_reason'] ?? 'রিকোয়ারমেন্ট অসম্পূর্ণ বা কমিউনিটি স্ট্যান্ডার্ড পূরণ হয়নি');
        try {
            $stmt = $pdo->prepare("SELECT * FROM monetization_requests WHERE id = ?");
            $stmt->execute([$reqId]);
            $req = $stmt->fetch();

            if ($req) {
                $userId = $req['user_id'];

                $pdo->beginTransaction();
                $upReq = $pdo->prepare("UPDATE monetization_requests SET status = 'rejected', rejection_reason = ?, updated_at = NOW() WHERE id = ?");
                $upReq->execute([$reason, $reqId]);

                // Notify user
                try {
                    $msgText = 'আপনার মনিটাইজেশন আবেদনটি বাতিল করা হয়েছে।' . ($reason ? " কারণ: {$reason}" : '');
                    $notif = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, message, created_at) VALUES (?, ?, 'monetization_rejected', ?, NOW())");
                    $notif->execute([$userId, 1, $msgText]);
                } catch (Exception $e) {}

                $pdo->commit();
                $msg = 'মনিটাইজেশন আবেদনটি বাতিল (Rejected) করা হয়েছে।';
            }
        } catch (Exception $e) {
            if ($pdo->inTransaction()) $pdo->rollBack();
            $err = 'বাতিল করতে সমস্যা হয়েছে: ' . $e->getMessage();
        }
    }
}

// Fetch Filter
$filter = $_GET['filter'] ?? 'all';
$sql = "SELECT mr.*, u.full_name, u.email, u.phone, u.profile_pic, u.is_verified, u.created_at AS user_registered_at 
        FROM monetization_requests mr 
        LEFT JOIN users u ON mr.user_id = u.id ";

if ($filter === 'pending') {
    $sql .= " WHERE mr.status = 'pending' ";
} elseif ($filter === 'approved') {
    $sql .= " WHERE mr.status = 'approved' ";
} elseif ($filter === 'rejected') {
    $sql .= " WHERE mr.status = 'rejected' ";
}
$sql .= " ORDER BY mr.created_at DESC";

$stmt = $pdo->query($sql);
$requests = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Counts
$totalPending = 0;
$totalApproved = 0;
$totalRejected = 0;
try {
    $totalPending = (int)$pdo->query("SELECT COUNT(*) FROM monetization_requests WHERE status = 'pending'")->fetchColumn();
    $totalApproved = (int)$pdo->query("SELECT COUNT(*) FROM monetization_requests WHERE status = 'approved'")->fetchColumn();
    $totalRejected = (int)$pdo->query("SELECT COUNT(*) FROM monetization_requests WHERE status = 'rejected'")->fetchColumn();
} catch (Exception $e) {}

require_once __DIR__ . '/includes/header.php';
?>

<div class="row g-3 mb-4">
    <div class="col-12 col-md-4">
        <div class="card-custom border-start border-4 border-warning p-3">
            <div class="d-flex align-items-center justify-content-between">
                <div>
                    <span class="text-muted small fw-bold text-uppercase">অপেক্ষমান আবেদন (Pending)</span>
                    <h3 class="fw-bold mb-0 text-warning mt-1"><?= $totalPending ?></h3>
                </div>
                <div class="bg-warning-subtle text-warning p-3 rounded-circle">
                    <i class="bi bi-hourglass-split fs-4"></i>
                </div>
            </div>
        </div>
    </div>
    <div class="col-12 col-md-4">
        <div class="card-custom border-start border-4 border-success p-3">
            <div class="d-flex align-items-center justify-content-between">
                <div>
                    <span class="text-muted small fw-bold text-uppercase">অনুমোদিত পার্টনার (Approved)</span>
                    <h3 class="fw-bold mb-0 text-success mt-1"><?= $totalApproved ?></h3>
                </div>
                <div class="bg-success-subtle text-success p-3 rounded-circle">
                    <i class="bi bi-patch-check-fill fs-4"></i>
                </div>
            </div>
        </div>
    </div>
    <div class="col-12 col-md-4">
        <div class="card-custom border-start border-4 border-danger p-3">
            <div class="d-flex align-items-center justify-content-between">
                <div>
                    <span class="text-muted small fw-bold text-uppercase">প্রত্যাখ্যাত আবেদন (Rejected)</span>
                    <h3 class="fw-bold mb-0 text-danger mt-1"><?= $totalRejected ?></h3>
                </div>
                <div class="bg-danger-subtle text-danger p-3 rounded-circle">
                    <i class="bi bi-x-circle-fill fs-4"></i>
                </div>
            </div>
        </div>
    </div>
</div>

<?php if ($msg): ?>
    <div class="alert alert-success border-0 shadow-sm d-flex align-items-center mb-4">
        <i class="bi bi-check-circle-fill fs-5 me-2"></i>
        <div><?= htmlspecialchars($msg) ?></div>
    </div>
<?php endif; ?>

<?php if ($err): ?>
    <div class="alert alert-danger border-0 shadow-sm d-flex align-items-center mb-4">
        <i class="bi bi-exclamation-triangle-fill fs-5 me-2"></i>
        <div><?= htmlspecialchars($err) ?></div>
    </div>
<?php endif; ?>

<div class="card-custom">
    <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <div>
            <h5 class="fw-bold mb-1">আবেদন তালিকা (Applications)</h5>
            <small class="text-muted">কনটেন্ট নির্মাতাদের মনিটাইজেশন রিকোয়েস্ট পর্যালোচনা করুন</small>
        </div>
        <div class="btn-group shadow-sm">
            <a href="?filter=all" class="btn btn-sm <?= $filter === 'all' ? 'btn-primary' : 'btn-outline-secondary' ?>">সকল (All)</a>
            <a href="?filter=pending" class="btn btn-sm <?= $filter === 'pending' ? 'btn-warning text-dark' : 'btn-outline-secondary' ?>">
                পেন্ডিং (<?= $totalPending ?>)
            </a>
            <a href="?filter=approved" class="btn btn-sm <?= $filter === 'approved' ? 'btn-success' : 'btn-outline-secondary' ?>">অনুমোদিত (<?= $totalApproved ?>)</a>
            <a href="?filter=rejected" class="btn btn-sm <?= $filter === 'rejected' ? 'btn-danger' : 'btn-outline-secondary' ?>">প্রত্যাখ্যাত (<?= $totalRejected ?>)</a>
        </div>
    </div>

    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
                <tr>
                    <th class="py-3 ps-3">আবেদনকারী (User)</th>
                    <th class="py-3">ফলোয়ার্স</th>
                    <th class="py-3">পোস্ট / রিলস</th>
                    <th class="py-3">একাউন্টের বয়স</th>
                    <th class="py-3">আবেদনের তারিখ</th>
                    <th class="py-3">স্ট্যাটাস</th>
                    <th class="py-3 text-end pe-3">অ্যাকশন</th>
                </tr>
            </thead>
            <tbody>
                <?php if (empty($requests)): ?>
                    <tr>
                        <td colspan="7" class="text-center py-5 text-muted">
                            <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                            কোনো মনিটাইজেশন আবেদন পাওয়া যায়নি।
                        </td>
                    </tr>
                <?php else: ?>
                    <?php foreach ($requests as $r): ?>
                        <tr>
                            <td class="ps-3">
                                <div class="d-flex align-items-center gap-2">
                                    <?php if (!empty($r['profile_pic'])): ?>
                                        <img src="<?= htmlspecialchars($r['profile_pic']) ?>" class="rounded-circle border" style="width: 42px; height: 42px; object-fit: cover;">
                                    <?php else: ?>
                                        <div class="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold" style="width: 42px; height: 42px;">
                                            <?= strtoupper(substr($r['full_name'] ?: 'U', 0, 1)) ?>
                                        </div>
                                    <?php endif; ?>
                                    <div>
                                        <div class="fw-bold text-dark">
                                            <?= htmlspecialchars($r['full_name'] ?: 'Unknown User') ?>
                                            <?php if ($r['is_verified']): ?>
                                                <i class="bi bi-patch-check-fill text-primary ms-1" title="Verified"></i>
                                            <?php endif; ?>
                                        </div>
                                        <small class="text-muted"><?= htmlspecialchars($r['email'] ?: $r['phone']) ?></small>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <span class="badge bg-light text-dark border px-2 py-1 fs-6">
                                    <i class="bi bi-people-fill text-primary me-1"></i> <?= number_format($r['followers_count']) ?>
                                </span>
                            </td>
                            <td>
                                <div class="small">
                                    <div><i class="bi bi-file-text text-secondary me-1"></i> পোস্ট: <strong><?= $r['posts_count'] ?></strong></div>
                                    <div><i class="bi bi-play-btn text-danger me-1"></i> রিলস: <strong><?= $r['reels_count'] ?></strong></div>
                                </div>
                            </td>
                            <td>
                                <span class="badge bg-light text-secondary border">
                                    <i class="bi bi-calendar-event me-1"></i> <?= $r['account_age_days'] ?> দিন (Days)
                                </span>
                            </td>
                            <td>
                                <small class="text-muted"><?= date('d M Y, h:i A', strtotime($r['created_at'])) ?></small>
                            </td>
                            <td>
                                <?php if ($r['status'] === 'pending'): ?>
                                    <span class="badge bg-warning-subtle text-warning border border-warning px-2 py-1">
                                        <i class="bi bi-hourglass-split me-1"></i> অপেক্ষমান
                                    </span>
                                <?php elseif ($r['status'] === 'approved'): ?>
                                    <span class="badge bg-success-subtle text-success border border-success px-2 py-1">
                                        <i class="bi bi-check-circle-fill me-1"></i> অনুমোদিত
                                    </span>
                                <?php else: ?>
                                    <span class="badge bg-danger-subtle text-danger border border-danger px-2 py-1" title="<?= htmlspecialchars($r['rejection_reason'] ?? '') ?>">
                                        <i class="bi bi-x-circle-fill me-1"></i> বাতিল
                                    </span>
                                <?php endif; ?>
                            </td>
                            <td class="text-end pe-3">
                                <?php if ($r['status'] === 'pending'): ?>
                                    <div class="d-flex justify-content-end gap-2">
                                        <form method="POST" onsubmit="return confirm('আপনি কি নিশ্চিত এই ব্যবহারকারীর মনিটাইজেশন অনুমোদন করতে চান?');">
                                            <input type="hidden" name="request_id" value="<?= $r['id'] ?>">
                                            <input type="hidden" name="action" value="approve">
                                            <button type="submit" class="btn btn-sm btn-success px-3">
                                                <i class="bi bi-check-lg me-1"></i> এপ্রুভ
                                            </button>
                                        </form>
                                        <button type="button" class="btn btn-sm btn-outline-danger" data-bs-toggle="modal" data-bs-target="#rejectModal<?= $r['id'] ?>">
                                            <i class="bi bi-x-lg"></i> রিজেক্ট
                                        </button>
                                    </div>

                                    <!-- Rejection Modal -->
                                    <div class="modal fade" id="rejectModal<?= $r['id'] ?>" tabindex="-1" aria-hidden="true">
                                        <div class="modal-dialog modal-dialog-centered">
                                            <div class="modal-content text-start border-0 shadow">
                                                <form method="POST">
                                                    <input type="hidden" name="request_id" value="<?= $r['id'] ?>">
                                                    <input type="hidden" name="action" value="reject">
                                                    <div class="modal-header">
                                                        <h6 class="modal-title fw-bold">আবেদন বাতিলের কারণ দিন</h6>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <div class="mb-3">
                                                            <label class="form-label small fw-bold">ব্যবহারকারী: <?= htmlspecialchars($r['full_name']) ?></label>
                                                            <textarea name="rejection_reason" class="form-control rounded-3" rows="3" placeholder="বাতিলের কারণ লিখুন (যেমন: কনটেন্ট কোয়ালিটি বা রিকোয়ারমেন্ট কম)" required></textarea>
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-light" data-bs-dismiss="modal">বন্ধ করুন</button>
                                                        <button type="submit" class="btn btn-danger">বাতিল নিশ্চিত করুন</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                <?php else: ?>
                                    <span class="text-muted small">সম্পন্ন</span>
                                <?php endif; ?>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                <?php endif; ?>
            </tbody>
        </table>
    </div>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
