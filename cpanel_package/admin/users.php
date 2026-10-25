<?php
/**
 * User Management Page
 * Search by Name, Email, Phone. Edit, Delete, Ban/Unban, Single User Notification.
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'ইউজার ম্যানেজমেন্ট - Flikata Admin';
$pageHeading = 'ইউজার ম্যানেজমেন্ট (User Management)';
$pageSubheading = 'ইউজার অনুসন্ধান, প্রোফাইল সম্পাদনা, ব্যান/আনব্যান ও একক নোটিফিকেশন প্রেরণ';

// Search and filter parameters
$search = trim($_GET['search'] ?? '');
$statusFilter = trim($_GET['status'] ?? 'all');

// Retrieve saved per-page limit from app_feature_settings
$perPage = 20;
try {
    $stmtF = $pdo->query("SELECT users_per_page FROM app_feature_settings WHERE id = 1 LIMIT 1");
    if ($stmtF && ($saved = $stmtF->fetchColumn()) !== false && (int)$saved > 0) {
        $perPage = (int)$saved;
    }
} catch (Exception $e) {}

if (isset($_GET['per_page']) && (int)$_GET['per_page'] > 0) {
    $perPage = max(1, min(1000, (int)$_GET['per_page']));
}

$page = max(1, (int)($_GET['page'] ?? 1));

// Count total users matching filters
$countSql = "SELECT COUNT(*) FROM users WHERE 1=1";
$filterParams = [];

if (!empty($search)) {
    $countSql .= " AND (full_name LIKE ? OR email LIKE ? OR phone LIKE ?)";
    $like = "%$search%";
    $filterParams[] = $like;
    $filterParams[] = $like;
    $filterParams[] = $like;
}

if ($statusFilter === 'banned') {
    $countSql .= " AND is_banned = 1";
} elseif ($statusFilter === 'active') {
    $countSql .= " AND (is_banned = 0 OR is_banned IS NULL)";
} elseif ($statusFilter === 'verified') {
    $countSql .= " AND is_verified = 1";
}

$cStmt = $pdo->prepare($countSql);
$cStmt->execute($filterParams);
$totalUsers = (int)$cStmt->fetchColumn();

$totalPages = max(1, (int)ceil($totalUsers / $perPage));
if ($page > $totalPages) {
    $page = $totalPages;
}
$offset = ($page - 1) * $perPage;

// Fetch Paginated Users for current page
$sql = "SELECT * FROM users WHERE 1=1";
$queryParams = [];
if (!empty($search)) {
    $sql .= " AND (full_name LIKE ? OR email LIKE ? OR phone LIKE ?)";
    $like = "%$search%";
    $queryParams[] = $like;
    $queryParams[] = $like;
    $queryParams[] = $like;
}
if ($statusFilter === 'banned') {
    $sql .= " AND is_banned = 1";
} elseif ($statusFilter === 'active') {
    $sql .= " AND (is_banned = 0 OR is_banned IS NULL)";
} elseif ($statusFilter === 'verified') {
    $sql .= " AND is_verified = 1";
}
$sql .= " ORDER BY id DESC LIMIT ? OFFSET ?";
$queryParams[] = $perPage;
$queryParams[] = $offset;

$stmt = $pdo->prepare($sql);
$stmt->execute($queryParams);
$users = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Fetch All Matching Users for Complete Print / Export
$allMatchingUsers = [];
try {
    $allSql = "SELECT id, full_name, username, email, phone, occupation, location, is_banned, is_verified, joined_date, created_at FROM users WHERE 1=1";
    $allParams = [];
    if (!empty($search)) {
        $allSql .= " AND (full_name LIKE ? OR email LIKE ? OR phone LIKE ?)";
        $like = "%$search%";
        $allParams[] = $like;
        $allParams[] = $like;
        $allParams[] = $like;
    }
    if ($statusFilter === 'banned') {
        $allSql .= " AND is_banned = 1";
    } elseif ($statusFilter === 'active') {
        $allSql .= " AND (is_banned = 0 OR is_banned IS NULL)";
    } elseif ($statusFilter === 'verified') {
        $allSql .= " AND is_verified = 1";
    }
    $allSql .= " ORDER BY id DESC";
    $allStmt = $pdo->prepare($allSql);
    $allStmt->execute($allParams);
    $allMatchingUsers = $allStmt->fetchAll(PDO::FETCH_ASSOC);
} catch (Exception $e) {}

// Helper to construct pagination URLs
function buildPageUrl($p, $search, $statusFilter, $perPage) {
    $params = ['page' => $p];
    if (!empty($search)) $params['search'] = $search;
    if (!empty($statusFilter) && $statusFilter !== 'all') $params['status'] = $statusFilter;
    if ($perPage != 20) $params['per_page'] = $perPage;
    return 'users.php?' . http_build_query($params);
}

require_once __DIR__ . '/includes/header.php';
?>

<!-- TOP ACTION BAR: PRINT / EXPORT & PER-PAGE CONFIG -->
<div class="card-custom mb-4 p-3 shadow-sm border-0" style="background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%); border-left: 5px solid #4f46e5 !important;">
    <div class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3">
        <!-- Print & Multi-Format Export Button -->
        <div class="d-flex align-items-center gap-2">
            <button type="button" class="btn btn-primary fw-bold px-4 py-2.5 rounded-pill shadow-sm d-flex align-items-center gap-2" onclick="openPrintExportModal()">
                <i class="bi bi-printer-fill fs-5"></i>
                <span>ইউজার লিস্ট প্রিন্ট / ডাউনলোড (Print / PDF / Image)</span>
            </button>
            <span class="badge bg-white text-primary border px-3 py-2 rounded-pill shadow-xs d-none d-lg-inline-block">
                <i class="bi bi-people-fill me-1"></i>মোট ইউজার: <?= $totalUsers ?> জন
            </span>
        </div>

        <!-- Per-Page Display Limit Selector & Permanent Save Button -->
        <div class="d-flex align-items-center gap-2 bg-white p-1.5 px-3 rounded-pill shadow-sm border">
            <label for="perPageInput" class="small fw-bold text-secondary mb-0 text-nowrap">
                <i class="bi bi-list-ol text-primary me-1"></i>প্রতি পেজে শো করবে:
            </label>
            <input type="number" id="perPageInput" min="1" max="1000" class="form-control form-control-sm text-center fw-bold text-primary border-primary-subtle" style="width: 75px; border-radius: 20px;" value="<?= $perPage ?>">
            <button type="button" class="btn btn-sm btn-primary rounded-pill px-3 fw-bold shadow-xs text-nowrap" onclick="savePerPageSetting()">
                <i class="bi bi-check2-circle me-1"></i>সেভ করুন
            </button>
        </div>
    </div>
</div>

<!-- Search Bar & Filters Card -->
<div class="card-custom mb-4">
    <form method="GET" action="users.php" class="row g-3 align-items-center">
        <!-- Search Input -->
        <div class="col-12 col-md-6 col-lg-7">
            <div class="input-group">
                <span class="input-group-text bg-light border-end-0 border-1" style="border-radius: 12px 0 0 12px; border-color: var(--border-color);">
                    <i class="bi bi-search text-muted"></i>
                </span>
                <input type="text" name="search" class="form-control bg-light border-start-0 border-1" style="border-radius: 0 12px 12px 0; border-color: var(--border-color);" placeholder="নাম, ইমেইল বা ফোন নাম্বার দিয়ে খুঁজুন..." value="<?= htmlspecialchars($search) ?>">
            </div>
        </div>

        <!-- Filter Dropdown -->
        <div class="col-6 col-md-3 col-lg-3">
            <select name="status" class="form-select bg-light border-1" style="border-radius: 12px; border-color: var(--border-color);">
                <option value="all" <?= $statusFilter === 'all' ? 'selected' : '' ?>>সকল ইউজার</option>
                <option value="active" <?= $statusFilter === 'active' ? 'selected' : '' ?>>সচল (Active)</option>
                <option value="banned" <?= $statusFilter === 'banned' ? 'selected' : '' ?>>ব্যানড (Banned)</option>
                <option value="verified" <?= $statusFilter === 'verified' ? 'selected' : '' ?>>ভেরিফায়েড (Verified)</option>
            </select>
        </div>

        <!-- Search Button & Reset -->
        <div class="col-6 col-md-3 col-lg-2 d-flex gap-2">
            <button type="submit" class="btn btn-primary-custom w-100 d-flex align-items-center justify-content-center gap-2">
                <i class="bi bi-funnel-fill"></i> ফিল্টার
            </button>
            <?php if (!empty($search) || $statusFilter !== 'all'): ?>
                <a href="users.php" class="btn btn-light border rounded-3 p-2" title="রিসেট">
                    <i class="bi bi-x-lg"></i>
                </a>
            <?php endif; ?>
        </div>
    </form>
</div>

<!-- Users List Table Card -->
<div class="card-custom">
    <div class="d-flex align-items-center justify-content-between mb-3">
        <div>
            <h6 class="fw-bold mb-0 text-dark">ইউজার তালিকা (মোট: <?= $totalUsers ?> জন<?= ($totalPages > 1) ? " | পেজ {$page}/{$totalPages}" : "" ?>)</h6>
            <small class="text-muted">প্রত্যেক ইউজারের তথ্যাদি ও অ্যাকশন বাটন</small>
        </div>
    </div>

    <div class="table-responsive">
        <table class="table table-custom align-middle mb-0">
            <thead>
                <tr>
                    <th>ইউজার ও আইডি</th>
                    <th>ইমেইল ও ফোন</th>
                    <th>পেশা ও লোকেশন</th>
                    <th>স্ট্যাটাস</th>
                    <th>যোগদানের তারিখ</th>
                    <th class="text-end" style="min-width: 180px;">অ্যাকশন (Actions)</th>
                </tr>
            </thead>
            <tbody>
                <?php if (empty($users)): ?>
                    <tr>
                        <td colspan="6" class="text-center py-5 text-muted">
                            <i class="bi bi-people display-4 d-block mb-2 text-muted"></i>
                            কোনো ইউজার খুঁজে পাওয়া যায়নি।
                        </td>
                    </tr>
                <?php else: ?>
                    <?php foreach ($users as $u): ?>
                        <tr id="user-row-<?= $u['id'] ?>">
                            <!-- User & Avatar -->
                            <td>
                                <div class="d-flex align-items-center gap-3">
                                    <img src="<?= !empty($u['profile_pic']) ? htmlspecialchars($u['profile_pic']) : 'https://ui-avatars.com/api/?name=' . urlencode($u['full_name']) . '&background=4f46e5&color=fff' ?>" class="user-avatar-sm" alt="Profile">
                                    <div>
                                        <div class="fw-bold text-dark d-flex align-items-center gap-1">
                                            <?= htmlspecialchars($u['full_name']) ?>
                                            <span id="verify-icon-<?= $u['id'] ?>">
                                                <?php if (!empty($u['is_verified'])): ?>
                                                    <i class="bi bi-patch-check-fill text-primary" title="ভেরিফায়েড একাউন্ট"></i>
                                                <?php endif; ?>
                                            </span>
                                        </div>
                                        <small class="text-muted">UID: #<?= $u['id'] ?></small>
                                    </div>
                                </div>
                            </td>

                            <!-- Email & Phone -->
                            <td>
                                <div class="small fw-semibold text-dark"><i class="bi bi-envelope me-1 text-muted"></i><?= htmlspecialchars($u['email'] ?: 'N/A') ?></div>
                                <div class="small text-muted"><i class="bi bi-telephone me-1 text-muted"></i><?= htmlspecialchars($u['phone'] ?: 'N/A') ?></div>
                            </td>

                            <!-- Occupation & Location -->
                            <td>
                                <div class="small fw-semibold text-dark"><?= htmlspecialchars($u['occupation'] ?: 'User') ?></div>
                                <small class="text-muted"><?= htmlspecialchars($u['location'] ?: 'Bangladesh') ?></small>
                            </td>

                            <!-- Status Badge -->
                            <td id="status-cell-<?= $u['id'] ?>">
                                <?php if (!empty($u['is_banned'])): ?>
                                    <span class="badge bg-danger-subtle text-danger fw-bold px-2 py-1"><i class="bi bi-slash-circle me-1"></i>ব্যানড</span>
                                <?php else: ?>
                                    <span class="badge bg-success-subtle text-success fw-bold px-2 py-1"><i class="bi bi-check-circle me-1"></i>সচল</span>
                                <?php endif; ?>
                            </td>

                            <!-- Date -->
                            <td class="small text-muted">
                                <?= date('d M, Y', strtotime($u['created_at'])) ?>
                            </td>

                            <!-- Actions -->
                            <td class="text-end">
                                <div class="d-flex align-items-center justify-content-end gap-1">
                                    <!-- Blue Verification Badge Toggle Button -->
                                    <button id="verify-btn-<?= $u['id'] ?>" class="btn btn-sm <?= !empty($u['is_verified']) ? 'btn-primary' : 'btn-light border text-primary' ?>" title="<?= !empty($u['is_verified']) ? 'ব্লু ভেরিফিকেশন বন্ধ করুন (Unverify)' : 'ফেসবুক ব্লু ভেরিফিকেশন চালু করুন (Verify)' ?>" onclick="toggleVerifyUser(<?= $u['id'] ?>, <?= !empty($u['is_verified']) ? 0 : 1 ?>)">
                                        <i class="bi <?= !empty($u['is_verified']) ? 'bi-patch-check-fill' : 'bi-patch-check' ?>"></i>
                                    </button>

                                    <!-- User Custom Limits Settings Button -->
                                    <button class="btn btn-sm btn-light border" style="color: #7c3aed !important;" title="কাস্টম পোস্ট ও পেজ লিমিট সেট করুন" onclick='openUserLimitsModal(<?= json_encode($u) ?>)'>
                                        <i class="bi bi-sliders"></i>
                                    </button>

                                    <!-- Send Single Notification -->
                                    <button class="btn btn-sm btn-light border text-primary" title="একক নোটিফিকেশন পাঠান" onclick="openNotifyModal(<?= $u['id'] ?>, '<?= htmlspecialchars(addslashes($u['full_name'])) ?>')">
                                        <i class="bi bi-bell-fill"></i>
                                    </button>

                                    <!-- Edit User Modal Trigger -->
                                    <button class="btn btn-sm btn-light border text-dark" title="এডিট করুন" onclick='openEditModal(<?= json_encode($u) ?>)'>
                                        <i class="bi bi-pencil-square"></i>
                                    </button>

                                    <!-- Ban / Unban Toggle Button -->
                                    <button id="ban-btn-<?= $u['id'] ?>" class="btn btn-sm <?= !empty($u['is_banned']) ? 'btn-success' : 'btn-warning text-dark' ?>" title="<?= !empty($u['is_banned']) ? 'আনব্যান করুন' : 'ব্যান করুন' ?>" onclick="toggleBanUser(<?= $u['id'] ?>, <?= !empty($u['is_banned']) ? 0 : 1 ?>)">
                                        <i class="bi <?= !empty($u['is_banned']) ? 'bi-shield-check' : 'bi-slash-circle' ?>"></i>
                                    </button>

                                    <!-- Delete User Button -->
                                    <button class="btn btn-sm btn-danger-custom" title="স্থায়ীভাবে ডিলিট করুন" onclick="deleteUserConfirm(<?= $u['id'] ?>, '<?= htmlspecialchars(addslashes($u['full_name'])) ?>')">
                                        <i class="bi bi-trash3-fill"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                <?php endif; ?>
            </tbody>
        </table>
    </div>

    <!-- Bottom Pagination Controls (1, 2, 3...) -->
    <?php if ($totalPages > 1): ?>
        <div class="d-flex flex-column flex-md-row align-items-center justify-content-between pt-4 mt-3 border-top gap-3">
            <div class="text-muted small">
                পৃষ্ঠা <strong class="text-dark"><?= $page ?></strong> এর <strong><?= $totalPages ?></strong> (মোট <strong class="text-primary"><?= $totalUsers ?></strong> জন ইউজারের মধ্যে <?= min($totalUsers, $offset + 1) ?>-<?= min($totalUsers, $offset + count($users)) ?> জন দেখানো হচ্ছে)
            </div>
            <nav aria-label="Users pagination">
                <ul class="pagination pagination-sm mb-0 gap-1 flex-wrap justify-content-center">
                    <!-- Previous Page -->
                    <li class="page-item <?= ($page <= 1) ? 'disabled' : '' ?>">
                        <a class="page-link rounded-3 px-3 py-1.5 <?= ($page <= 1) ? 'text-muted' : 'text-primary fw-bold' ?>" href="<?= buildPageUrl(max(1, $page - 1), $search, $statusFilter, $perPage) ?>">
                            <i class="bi bi-chevron-left me-1"></i>পূর্ববর্তী
                        </a>
                    </li>

                    <!-- Page Numbers -->
                    <?php
                    $startPage = max(1, $page - 2);
                    $endPage = min($totalPages, $page + 2);

                    if ($startPage > 1): ?>
                        <li class="page-item"><a class="page-link rounded-3 px-3 py-1.5" href="<?= buildPageUrl(1, $search, $statusFilter, $perPage) ?>">1</a></li>
                        <?php if ($startPage > 2): ?><li class="page-item disabled"><span class="page-link px-2">...</span></li><?php endif; ?>
                    <?php endif; ?>

                    <?php for ($p = $startPage; $p <= $endPage; $p++): ?>
                        <li class="page-item <?= ($p == $page) ? 'active' : '' ?>">
                            <a class="page-link rounded-3 px-3 py-1.5 <?= ($p == $page) ? 'bg-primary border-primary text-white fw-bold shadow-sm' : '' ?>" href="<?= buildPageUrl($p, $search, $statusFilter, $perPage) ?>">
                                <?= $p ?>
                            </a>
                        </li>
                    <?php endfor; ?>

                    <?php if ($endPage < $totalPages): ?>
                        <?php if ($endPage < $totalPages - 1): ?><li class="page-item disabled"><span class="page-link px-2">...</span></li><?php endif; ?>
                        <li class="page-item"><a class="page-link rounded-3 px-3 py-1.5" href="<?= buildPageUrl($totalPages, $search, $statusFilter, $perPage) ?>"><?= $totalPages ?></a></li>
                    <?php endif; ?>

                    <!-- Next Page -->
                    <li class="page-item <?= ($page >= $totalPages) ? 'disabled' : '' ?>">
                        <a class="page-link rounded-3 px-3 py-1.5 <?= ($page >= $totalPages) ? 'text-muted' : 'text-primary fw-bold' ?>" href="<?= buildPageUrl(min($totalPages, $page + 1), $search, $statusFilter, $perPage) ?>">
                            পরবর্তী<i class="bi bi-chevron-right ms-1"></i>
                        </a>
                    </li>
                </ul>
            </nav>
        </div>
    <?php endif; ?>
</div>

<!-- Modal 1: Edit User Modal -->
<div class="modal fade" id="editUserModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow rounded-4">
            <div class="modal-header border-bottom py-3">
                <h6 class="modal-title fw-bold text-dark"><i class="bi bi-person-gear text-primary me-2"></i>ইউজার তথ্য সম্পাদনা</h6>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="editUserForm" onsubmit="submitUserEdit(event)">
                <input type="hidden" name="action" value="edit_user">
                <input type="hidden" id="editUserId" name="user_id">
                <div class="modal-body p-4">
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">পুরো নাম (Full Name)</label>
                        <input type="text" id="editFullName" name="full_name" class="form-control rounded-3" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">ইমেইল (Email)</label>
                        <input type="email" id="editEmail" name="email" class="form-control rounded-3">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">ফোন নাম্বার (Phone)</label>
                        <input type="text" id="editPhone" name="phone" class="form-control rounded-3">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">পেশা (Occupation)</label>
                        <input type="text" id="editOccupation" name="occupation" class="form-control rounded-3">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">ঠিকানা (Location)</label>
                        <input type="text" id="editLocation" name="location" class="form-control rounded-3">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">বায়ো (Bio)</label>
                        <textarea id="editBio" name="bio" rows="2" class="form-control rounded-3"></textarea>
                    </div>
                    <div class="form-check form-switch mt-3">
                        <input class="form-check-input" type="checkbox" id="editVerified" name="is_verified" value="1">
                        <label class="form-check-label fw-bold text-dark small" for="editVerified">ভেরিফায়েড ব্যাজ (Blue Tick Badge)</label>
                    </div>
                </div>
                <div class="modal-footer border-top py-3">
                    <button type="button" class="btn btn-light rounded-pill px-3" data-bs-dismiss="modal">বাতিল</button>
                    <button type="submit" class="btn btn-primary-custom px-4">সংরক্ষণ করুন</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal 2: Send Single User Notification Modal -->
<div class="modal fade" id="notifyUserModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow rounded-4">
            <div class="modal-header border-bottom py-3">
                <h6 class="modal-title fw-bold text-dark">
                    <i class="bi bi-bell-fill text-primary me-2"></i>একক নোটিফিকেশন প্রেরণ
                </h6>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="notifyUserForm" onsubmit="submitSingleNotification(event)">
                <input type="hidden" name="action" value="send_single_notification">
                <input type="hidden" id="notifyUserId" name="user_id">
                <div class="modal-body p-4">
                    <div class="alert alert-primary-subtle border-0 rounded-3 py-2 px-3 small mb-3">
                        প্রাপক: <strong id="notifyUserName" class="text-dark">User</strong>
                        <div class="text-muted mt-1" style="font-size: 0.785rem;">এই নোটিফিকেশনটি শুধুমাত্র এই ইউজারের নোটিফিকেশন পেজে যাবে।</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">শিরোনাম (Notification Title)</label>
                        <input type="text" name="title" class="form-control rounded-3" placeholder="যেমন: আপনার একাউন্ট সংক্রান্ত নোটিশ" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">বার্তা (Message Body)</label>
                        <textarea name="message" rows="3" class="form-control rounded-3" placeholder="ইউজারকে জানানোর সম্পূর্ণ বার্তা লিখুন..." required></textarea>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">নোটিফিকেশনের ধরন (Type)</label>
                        <select name="type" class="form-select rounded-3">
                            <option value="general">সাধারণ বার্তা (General)</option>
                            <option value="alert">সতর্কবার্তা (Alert)</option>
                            <option value="reward">পুরস্কার/অভিনন্দন (Reward)</option>
                            <option value="admin_notice">অফিশিয়াল নোটিশ (Official Notice)</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer border-top py-3">
                    <button type="button" class="btn btn-light rounded-pill px-3" data-bs-dismiss="modal">বাতিল</button>
                    <button type="submit" class="btn btn-primary-custom px-4">
                        <i class="bi bi-send-fill me-1"></i> নোটিফিকেশন পাঠান
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal 3: User Custom Limits Modal -->
<div class="modal fade" id="userLimitsModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow rounded-4">
            <div class="modal-header border-bottom py-3">
                <h6 class="modal-title fw-bold text-dark d-flex align-items-center gap-2">
                    <i class="bi bi-sliders text-purple" style="color: #7c3aed !important;"></i>
                    ইউজার কাস্টম লিমিট সেটিংস (Custom Limits)
                </h6>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="userLimitsForm" onsubmit="submitUserLimits(event)">
                <input type="hidden" name="action" value="update_user_limits">
                <input type="hidden" id="limitUserId" name="user_id">
                <div class="modal-body p-4">
                    <div class="alert alert-light border rounded-3 py-2 px-3 small mb-3 d-flex align-items-center justify-content-between">
                        <div>ইউজার: <strong id="limitUserName" class="text-dark">User</strong></div>
                        <button type="button" class="btn btn-xs btn-outline-secondary rounded-pill py-0 px-2" onclick="resetAllLimitInputs()">ডিফল্ট করুন</button>
                    </div>

                    <div class="alert alert-warning border-0 rounded-3 p-2 small mb-3">
                        <i class="bi bi-info-circle me-1"></i> ফাঁকা রাখলে সিস্টেমের <strong>ডিফল্ট লিমিট</strong> প্রযোজ্য হবে। <strong>০ দিলে</strong> পোস্ট/স্টোরি/পেজ বন্ধ থাকবে এবং নোটিফিকেশন যাবে।
                    </div>

                    <div class="row g-3">
                        <!-- Custom Page Limit -->
                        <div class="col-6">
                            <label class="form-label small fw-bold text-secondary mb-1">পেজ তৈরি লিমিট</label>
                            <input type="number" id="customPageLimit" name="custom_page_limit" min="0" max="500" class="form-control rounded-3" placeholder="ডিফল্ট (খালি রাখুন)">
                        </div>

                        <!-- Custom Daily Story Limit -->
                        <div class="col-6">
                            <label class="form-label small fw-bold text-secondary mb-1">ডেইলি স্টোরি লিমিট</label>
                            <input type="number" id="customStoryLimit" name="custom_story_limit" min="0" max="1000" class="form-control rounded-3" placeholder="ডিফল্ট (খালি রাখুন)">
                        </div>

                        <!-- Custom Daily Text Post Limit -->
                        <div class="col-12 col-md-4">
                            <label class="form-label small fw-bold text-secondary mb-1">ডেইলি টেক্সট লিমিট</label>
                            <input type="number" id="customTextLimit" name="custom_text_limit" min="0" max="1000" class="form-control rounded-3" placeholder="ডিফল্ট">
                        </div>

                        <!-- Custom Daily Image Post Limit -->
                        <div class="col-12 col-md-4">
                            <label class="form-label small fw-bold text-secondary mb-1">ডেইলি ইমেজ লিমিট</label>
                            <input type="number" id="customImageLimit" name="custom_image_limit" min="0" max="1000" class="form-control rounded-3" placeholder="ডিফল্ট">
                        </div>

                        <!-- Custom Daily Video Post Limit -->
                        <div class="col-12 col-md-4">
                            <label class="form-label small fw-bold text-secondary mb-1">ডেইলি ভিডিও লিমিট</label>
                            <input type="number" id="customVideoLimit" name="custom_video_limit" min="0" max="1000" class="form-control rounded-3" placeholder="ডিফল্ট">
                        </div>
                    </div>
                </div>
                <div class="modal-footer border-top py-3">
                    <button type="button" class="btn btn-light rounded-pill px-3" data-bs-dismiss="modal">বাতিল</button>
                    <button type="submit" class="btn btn-primary px-4 rounded-pill fw-bold" style="background: #7c3aed; border-color: #7c3aed;">
                        <i class="bi bi-check2-circle me-1"></i> লিমিট সংরক্ষণ করুন
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    // Edit User Modal Initialization
    let editModalInstance = null;
    function openEditModal(userData) {
        document.getElementById('editUserId').value = userData.id;
        document.getElementById('editFullName').value = userData.full_name || '';
        document.getElementById('editEmail').value = userData.email || '';
        document.getElementById('editPhone').value = userData.phone || '';
        document.getElementById('editOccupation').value = userData.occupation || '';
        document.getElementById('editLocation').value = userData.location || '';
        document.getElementById('editBio').value = userData.bio || '';
        document.getElementById('editVerified').checked = (userData.is_verified == 1);

        if (!editModalInstance) {
            editModalInstance = new bootstrap.Modal(document.getElementById('editUserModal'));
        }
        editModalInstance.show();
    }

    function submitUserEdit(e) {
        e.preventDefault();
        const form = document.getElementById('editUserForm');
        const formData = new FormData(form);

        fetch('api.php', {
            method: 'POST',
            body: formData
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                editModalInstance.hide();
                Swal.fire('সম্পন্ন!', 'ইউজারের তথ্য সফলভাবে আপডেট হয়েছে।', 'success')
                    .then(() => location.reload());
            } else {
                Swal.fire('ব্যর্থ!', res.message || 'আপডেট করা সম্ভব হয়নি।', 'error');
            }
        })
        .catch(() => Swal.fire('ত্রুটি!', 'সার্ভারের সাথে যোগাযোগ করা যায়নি।', 'error'));
    }

    // Toggle Facebook Blue Verification Badge
    function toggleVerifyUser(userId, newVerifyStatus) {
        const actionText = newVerifyStatus === 1 ? 'ব্লু ভেরিফিকেশন চালু (Verify)' : 'ভেরিফিকেশন বন্ধ (Unverify)';
        Swal.fire({
            title: `ইউজারের ${actionText} করতে চান?`,
            text: newVerifyStatus === 1 ? 'চালু করলে ইউজারের নামের পাশে ফেসবুকের মতো কাটা কাটা ব্লু ভেরিফিকেশন ব্যাচ প্রদর্শিত হবে।' : 'বন্ধ করলে ভেরিফিকেশন ব্যাচ মুছে যাবে।',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: newVerifyStatus === 1 ? '#0866ff' : '#64748b',
            cancelButtonColor: '#94a3b8',
            confirmButtonText: `হ্যাঁ, ${actionText} করুন`,
            cancelButtonText: 'বাতিল'
        }).then((result) => {
            if (result.isConfirmed) {
                fetch('api.php', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ action: 'toggle_verify', user_id: userId, is_verified: newVerifyStatus })
                })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        Swal.fire('সফল!', res.message, 'success')
                            .then(() => location.reload());
                    } else {
                        Swal.fire('ব্যর্থ!', res.message || 'অপারেশন ব্যর্থ হয়েছে।', 'error');
                    }
                })
                .catch(() => Swal.fire('ত্রুটি!', 'সার্ভারে সংযোগ ব্যর্থ হয়েছে।', 'error'));
            }
        });
    }

    // Toggle Ban / Unban User
    function toggleBanUser(userId, newBanStatus) {
        const actionText = newBanStatus === 1 ? 'ব্যান (Ban)' : 'আনব্যান (Unban)';
        Swal.fire({
            title: `ইউজারকে ${actionText} করতে চান?`,
            text: newBanStatus === 1 ? 'ব্যান করলে ইউজার অ্যাপে লগইন করতে পারবে না।' : 'আনব্যান করলে ইউজার স্বাভাবিকভাবে অ্যাপ ব্যবহার করতে পারবে।',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: newBanStatus === 1 ? '#eab308' : '#10b981',
            cancelButtonColor: '#64748b',
            confirmButtonText: `হ্যাঁ, ${actionText} করুন`,
            cancelButtonText: 'বাতিল'
        }).then((result) => {
            if (result.isConfirmed) {
                fetch('api.php', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ action: 'toggle_ban', user_id: userId, is_banned: newBanStatus })
                })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        Swal.fire('সফল!', `ইউজার সফলভাবে ${actionText} করা হয়েছে।`, 'success')
                            .then(() => location.reload());
                    } else {
                        Swal.fire('ব্যর্থ!', res.message || 'অপারেশন ব্যর্থ হয়েছে।', 'error');
                    }
                })
                .catch(() => Swal.fire('ত্রুটি!', 'সার্ভারে সংযোগ ব্যর্থ হয়েছে।', 'error'));
            }
        });
    }

    // Delete User Confirm
    function deleteUserConfirm(userId, userName) {
        Swal.fire({
            title: `ইউজার "${userName}" স্থায়ীভাবে ডিলিট করতে চান?`,
            text: 'সতর্কতা: ইউজারের পোস্ট, মেসেজ ও কমেন্ট সহ সকল ডাটা সম্পূর্ণ মুছে যাবে!',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#64748b',
            confirmButtonText: 'হ্যাঁ, স্থায়ীভাবে ডিলিট করুন',
            cancelButtonText: 'বাতিল'
        }).then((result) => {
            if (result.isConfirmed) {
                fetch('api.php', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ action: 'delete_user', user_id: userId })
                })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        const row = document.getElementById('user-row-' + userId);
                        if (row) row.remove();
                        Swal.fire('ডিলিট সম্পন্ন!', 'ইউজারকে ডাটাবেজ থেকে মুছে ফেলা হয়েছে।', 'success');
                    } else {
                        Swal.fire('ব্যর্থ!', res.message || 'ডিলিট সম্পন্ন হয়নি।', 'error');
                    }
                })
                .catch(() => Swal.fire('ত্রুটি!', 'সার্ভারে সংযোগ ব্যর্থ হয়েছে।', 'error'));
            }
        });
    }

    // Open Single Notification Modal
    let notifyModalInstance = null;
    function openNotifyModal(userId, userName) {
        document.getElementById('notifyUserId').value = userId;
        document.getElementById('notifyUserName').innerText = userName + ' (ID: #' + userId + ')';

        if (!notifyModalInstance) {
            notifyModalInstance = new bootstrap.Modal(document.getElementById('notifyUserModal'));
        }
        notifyModalInstance.show();
    }

    function submitSingleNotification(e) {
        e.preventDefault();
        const form = document.getElementById('notifyUserForm');
        const formData = new FormData(form);

        fetch('api.php', {
            method: 'POST',
            body: formData
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                notifyModalInstance.hide();
                form.reset();
                Swal.fire('নোটিফিকেশন পাঠানো হয়েছে!', 'শুধুমাত্র এই নির্দিষ্ট ইউজারের ইনবক্সে বার্তা পৌঁছে গেছে।', 'success');
            } else {
                Swal.fire('ব্যর্থ!', res.message || 'নোটিফিকেশন পাঠানো সম্ভব হয়নি।', 'error');
            }
        })
        .catch(() => Swal.fire('ত্রুটি!', 'সার্ভার সংযোগ সমস্যা।', 'error'));
    }

    // User Custom Limits Modal
    let limitsModalInstance = null;
    function openUserLimitsModal(userData) {
        document.getElementById('limitUserId').value = userData.id;
        document.getElementById('limitUserName').innerText = userData.full_name + ' (#' + userData.id + ')';

        document.getElementById('customPageLimit').value = (userData.custom_page_limit !== null && userData.custom_page_limit !== undefined) ? userData.custom_page_limit : '';
        document.getElementById('customStoryLimit').value = (userData.custom_story_limit !== null && userData.custom_story_limit !== undefined) ? userData.custom_story_limit : '';
        document.getElementById('customTextLimit').value = (userData.custom_text_limit !== null && userData.custom_text_limit !== undefined) ? userData.custom_text_limit : '';
        document.getElementById('customImageLimit').value = (userData.custom_image_limit !== null && userData.custom_image_limit !== undefined) ? userData.custom_image_limit : '';
        document.getElementById('customVideoLimit').value = (userData.custom_video_limit !== null && userData.custom_video_limit !== undefined) ? userData.custom_video_limit : '';

        if (!limitsModalInstance) {
            limitsModalInstance = new bootstrap.Modal(document.getElementById('userLimitsModal'));
        }
        limitsModalInstance.show();
    }

    function resetAllLimitInputs() {
        document.getElementById('customPageLimit').value = '';
        document.getElementById('customStoryLimit').value = '';
        document.getElementById('customTextLimit').value = '';
        document.getElementById('customImageLimit').value = '';
        document.getElementById('customVideoLimit').value = '';
    }

    function submitUserLimits(e) {
        e.preventDefault();
        const form = document.getElementById('userLimitsForm');
        const formData = new FormData(form);

        fetch('api.php', {
            method: 'POST',
            body: formData
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                limitsModalInstance.hide();
                Swal.fire({
                    icon: 'success',
                    title: 'লিমিট সংরক্ষিত!',
                    text: res.message || 'ইউজারের কাস্টম লিমিট সফলভাবে সেট করা হয়েছে।'
                }).then(() => location.reload());
            } else {
                Swal.fire('ব্যর্থ!', res.message || 'লিমিট সংরক্ষণ করা যায়নি।', 'error');
            }
        })
        .catch(() => Swal.fire('ত্রুটি!', 'সার্ভারের সাথে সংযোগ স্থাপন করা যায়নি।', 'error'));
    }
</script>

<!-- CDN LIBRARIES FOR PDF & IMAGE GENERATION -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js"></script>

<!-- MODAL: PRINT & MULTI-FORMAT EXPORT MODAL -->
<div class="modal fade" id="printExportModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content border-0 shadow-lg rounded-4">
            <div class="modal-header border-bottom py-3 bg-light">
                <div class="d-flex align-items-center gap-2">
                    <div class="p-2 bg-primary text-white rounded-3 shadow-xs">
                        <i class="bi bi-printer-fill fs-5"></i>
                    </div>
                    <div>
                        <h6 class="modal-title fw-bold text-dark mb-0">ইউজার তালিকা প্রিন্ট ও ডাউনলোড হাব (Print & Export)</h6>
                        <small class="text-muted">সরাসরি প্রিন্ট অথবা PDF, PNG, JPG ইমেজ ফরম্যাটে ডাউনলোড করুন</small>
                    </div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            
            <div class="modal-body p-4 bg-light">
                <!-- Action Buttons Toolbar Inside Popup -->
                <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 p-3 bg-white rounded-3 border shadow-xs mb-4">
                    <div class="d-flex flex-wrap align-items-center gap-2">
                        <!-- 1. Direct Print -->
                        <button type="button" class="btn btn-dark fw-bold rounded-pill px-3 py-2 shadow-xs d-flex align-items-center gap-2" onclick="executePrint()">
                            <i class="bi bi-printer-fill fs-6"></i>
                            <span>সরাসরি প্রিন্ট (Print)</span>
                        </button>
                        
                        <!-- 2. PDF Download -->
                        <button type="button" class="btn btn-danger fw-bold rounded-pill px-3 py-2 shadow-xs d-flex align-items-center gap-2" onclick="exportToPDF()">
                            <i class="bi bi-file-earmark-pdf-fill fs-6"></i>
                            <span>PDF ডাউনলোড (PDF)</span>
                        </button>
                        
                        <!-- 3. PNG Download -->
                        <button type="button" class="btn btn-success fw-bold rounded-pill px-3 py-2 shadow-xs d-flex align-items-center gap-2" onclick="exportToImage('png')">
                            <i class="bi bi-file-earmark-image-fill fs-6"></i>
                            <span>PNG ডাউনলোড (PNG)</span>
                        </button>
                        
                        <!-- 4. JPG Download -->
                        <button type="button" class="btn btn-warning text-dark fw-bold rounded-pill px-3 py-2 shadow-xs d-flex align-items-center gap-2" onclick="exportToImage('jpeg')">
                            <i class="bi bi-file-image fs-6"></i>
                            <span>JPG ডাউনলোড (JPG)</span>
                        </button>
                    </div>

                    <!-- Scope Switch (All vs Current Page) -->
                    <div class="btn-group btn-group-sm p-1 bg-light rounded-pill border" role="group">
                        <input type="radio" class="btn-check" name="printScope" id="scopeAll" value="all" checked onchange="switchPrintScope('all')">
                        <label class="btn btn-sm btn-outline-primary rounded-pill px-3 fw-bold" for="scopeAll">
                            সকল ইউজার (<?= $totalUsers ?>)
                        </label>

                        <input type="radio" class="btn-check" name="printScope" id="scopePage" value="page" onchange="switchPrintScope('page')">
                        <label class="btn btn-sm btn-outline-primary rounded-pill px-3 fw-bold" for="scopePage">
                            বর্তমান পেজ (<?= count($users) ?>)
                        </label>
                    </div>
                </div>

                <!-- Printable Document Area -->
                <div class="bg-white border rounded-3 p-4 shadow-xs" style="max-height: 520px; overflow-y: auto;">
                    <div id="printableArea" style="background: #ffffff; color: #0f172a; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Hind Siliguri', sans-serif; padding: 25px;">
                        <!-- Report Header -->
                        <div style="border-bottom: 2px solid #2563eb; padding-bottom: 16px; margin-bottom: 20px;" class="d-flex align-items-center justify-content-between">
                            <div>
                                <h2 style="margin: 0; color: #1d4ed8; font-weight: 800; font-size: 26px; letter-spacing: -0.5px;">FLIKATA</h2>
                                <div style="font-size: 14px; font-weight: 600; color: #475569; margin-top: 3px;">অফিসিয়াল ইউজার ডিরেক্টরি রিপোর্ট (User Management Directory)</div>
                            </div>
                            <div style="text-align: right; font-size: 12px; color: #64748b; line-height: 1.5;">
                                <div><strong>প্রিন্ট / রিপোর্ট তারিখ:</strong> <?= date('d M Y, h:i A') ?></div>
                                <div><strong>ফিল্টার স্ট্যাটাস:</strong> <?= htmlspecialchars(ucfirst($statusFilter)) ?></div>
                                <div><strong>মোট প্রদর্শিত ইউজার:</strong> <span id="reportTotalCount" class="fw-bold text-primary"><?= $totalUsers ?></span> জন</div>
                            </div>
                        </div>

                        <!-- User Table -->
                        <table class="table table-bordered table-sm align-middle" style="font-size: 11.5px; width: 100%; border-collapse: collapse;">
                            <thead style="background-color: #f8fafc; color: #1e293b; font-weight: 700; border-bottom: 2px solid #cbd5e1;">
                                <tr>
                                    <th style="width: 40px; text-align: center; padding: 7px 5px;">#</th>
                                    <th style="width: 55px; text-align: center; padding: 7px 5px;">আইডি</th>
                                    <th style="padding: 7px 8px;">নাম ও ভেরিফিকেশন</th>
                                    <th style="padding: 7px 8px;">ইমেইল ও ফোন</th>
                                    <th style="padding: 7px 8px;">পেশা ও ঠিকানা</th>
                                    <th style="width: 75px; text-align: center; padding: 7px 5px;">স্ট্যাটাস</th>
                                    <th style="width: 95px; text-align: center; padding: 7px 5px;">যোগদান</th>
                                </tr>
                            </thead>
                            <tbody id="printTableBody">
                                <!-- Populated dynamically by JavaScript -->
                            </tbody>
                        </table>

                        <!-- Report Footer -->
                        <div style="margin-top: 25px; padding-top: 12px; border-top: 1px dashed #cbd5e1; font-size: 11px; color: #94a3b8; display: flex; justify-content: space-between;">
                            <span>Flikata Platform &copy; <?= date('Y') ?> | গোপনীয় ও অভ্যন্তরীণ ব্যবহারের জন্য</span>
                            <span>Generated from Flikata Admin Control Panel</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer border-top py-2.5 bg-light">
                <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">বন্ধ করুন</button>
            </div>
        </div>
    </div>
</div>

<script>
    // In-memory data for instant export without re-fetching
    const allUsersData = <?= json_encode($allMatchingUsers, JSON_UNESCAPED_UNICODE) ?>;
    const currentPageUsersData = <?= json_encode($users, JSON_UNESCAPED_UNICODE) ?>;

    function escapeHtml(text) {
        if (!text) return '';
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.toString().replace(/[&<>"']/g, function(m) { return map[m]; });
    }

    function renderPrintTable(usersList) {
        const tbody = document.getElementById('printTableBody');
        const countSpan = document.getElementById('reportTotalCount');
        if (countSpan) countSpan.innerText = usersList ? usersList.length : 0;

        if (!usersList || usersList.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding: 25px; color: #94a3b8; font-size: 13px;">কোনো ইউজার ডাটা খুঁজে পাওয়া যায়নি।</td></tr>';
            return;
        }

        let html = '';
        usersList.forEach((u, idx) => {
            const isBanned = (u.is_banned == 1);
            const isVerified = (u.is_verified == 1);
            const statusBadge = isBanned 
                ? '<span style="color: #dc2626; font-weight: 700; background: #fee2e2; padding: 2px 6px; border-radius: 4px; font-size: 10px;">ব্যানড</span>' 
                : '<span style="color: #16a34a; font-weight: 700; background: #dcfce7; padding: 2px 6px; border-radius: 4px; font-size: 10px;">সচল</span>';
            const verifyBadge = isVerified 
                ? '<span style="color: #2563eb; font-weight: 700; margin-left: 4px;">✓ Verified</span>' 
                : '';
            const joined = u.joined_date || (u.created_at ? u.created_at.slice(0, 10) : 'N/A');
            const occupationLoc = [u.occupation, u.location].filter(Boolean).join(' • ') || '-';
            const emailPhone = [u.email, u.phone].filter(Boolean).join('<br>') || '-';

            html += `
                <tr style="border-bottom: 1px solid #e2e8f0;">
                    <td style="text-align: center; padding: 6px 4px; color: #64748b;">${idx + 1}</td>
                    <td style="text-align: center; font-weight: 700; padding: 6px 4px; color: #1e293b;">#${u.id}</td>
                    <td style="padding: 6px 8px; font-weight: 600; color: #0f172a;">${escapeHtml(u.full_name || 'User')}${verifyBadge}</td>
                    <td style="padding: 6px 8px; font-size: 11px; line-height: 1.35;">${emailPhone}</td>
                    <td style="padding: 6px 8px; font-size: 11px; color: #475569;">${escapeHtml(occupationLoc)}</td>
                    <td style="text-align: center; padding: 6px 4px;">${statusBadge}</td>
                    <td style="text-align: center; padding: 6px 4px; font-size: 11px; color: #64748b;">${joined}</td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    let printExportModalInstance = null;
    function openPrintExportModal() {
        renderPrintTable(allUsersData);
        document.getElementById('scopeAll').checked = true;
        if (!printExportModalInstance) {
            printExportModalInstance = new bootstrap.Modal(document.getElementById('printExportModal'));
        }
        printExportModalInstance.show();
    }

    function switchPrintScope(scope) {
        if (scope === 'page') {
            renderPrintTable(currentPageUsersData);
        } else {
            renderPrintTable(allUsersData);
        }
    }

    // 1. Direct Print Handler
    function executePrint() {
        const printContent = document.getElementById('printableArea').innerHTML;
        const printWindow = window.open('', '_blank', 'width=950,height=750');
        printWindow.document.write(`
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Flikata_Users_List_${new Date().toISOString().slice(0, 10)}</title>
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
                <style>
                    @media print {
                        body { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
                        @page { margin: 12mm; size: auto; }
                    }
                    body { padding: 15px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #0f172a; }
                    table { width: 100%; border-collapse: collapse; font-size: 11px; }
                    th, td { border: 1px solid #cbd5e1 !important; padding: 6px 8px; }
                    th { background-color: #f8fafc !important; font-weight: 700; }
                </style>
            </head>
            <body>
                ${printContent}
                <script>
                    window.onload = function() {
                        window.print();
                        setTimeout(function() { window.close(); }, 600);
                    };
                <\/script>
            </body>
            </html>
        `);
        printWindow.document.close();
    }

    // 2. Download as PDF
    function exportToPDF() {
        const element = document.getElementById('printableArea');
        const opt = {
            margin:       [8, 8, 8, 8],
            filename:     `Flikata_Users_List_${new Date().toISOString().slice(0, 10)}.pdf`,
            image:        { type: 'jpeg', quality: 0.98 },
            html2canvas:  { scale: 2, useCORS: true, letterRendering: true },
            jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
        };

        Swal.fire({
            title: 'PDF তৈরি হচ্ছে...',
            text: 'দয়া করে কয়েক মুহূর্ত অপেক্ষা করুন',
            allowOutsideClick: false,
            didOpen: () => { Swal.showLoading(); }
        });

        html2pdf().set(opt).from(element).save().then(() => {
            Swal.fire({
                icon: 'success',
                title: 'PDF ডাউনলোড সম্পন্ন!',
                timer: 1800,
                showConfirmButton: false
            });
        }).catch(err => {
            console.error('PDF error:', err);
            Swal.fire('ত্রুটি!', 'PDF তৈরিতে সমস্যা হয়েছে: ' + err.message, 'error');
        });
    }

    // 3. Download as PNG or JPG
    function exportToImage(format) {
        const element = document.getElementById('printableArea');
        const isJpeg = format === 'jpeg' || format === 'jpg';
        const mime = isJpeg ? 'image/jpeg' : 'image/png';
        const ext = isJpeg ? 'jpg' : 'png';

        Swal.fire({
            title: `${ext.toUpperCase()} তৈরি হচ্ছে...`,
            text: 'দয়া করে কয়েক মুহূর্ত অপেক্ষা করুন',
            allowOutsideClick: false,
            didOpen: () => { Swal.showLoading(); }
        });

        html2canvas(element, {
            scale: 2,
            backgroundColor: '#ffffff',
            useCORS: true
        }).then(canvas => {
            const link = document.createElement('a');
            link.download = `Flikata_Users_List_${new Date().toISOString().slice(0, 10)}.${ext}`;
            link.href = canvas.toDataURL(mime, 0.95);
            link.click();

            Swal.fire({
                icon: 'success',
                title: `${ext.toUpperCase()} ডাউনলোড সম্পন্ন!`,
                timer: 1800,
                showConfirmButton: false
            });
        }).catch(err => {
            console.error('Image error:', err);
            Swal.fire('ত্রুটি!', 'ইমেজ তৈরিতে সমস্যা হয়েছে: ' + err.message, 'error');
        });
    }

    // 4. Save Per Page Setting Permanently
    function savePerPageSetting() {
        const inputVal = parseInt(document.getElementById('perPageInput').value, 10);
        if (!inputVal || inputVal < 1) {
            Swal.fire('সতর্কতা', 'অনুগ্রহ করে সঠিক সংখ্যা লিখুন (কমপক্ষে ১)।', 'warning');
            return;
        }

        fetch('api.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ action: 'save_users_per_page', per_page: inputVal })
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                Swal.fire({
                    icon: 'success',
                    title: 'সেভ করা হয়েছে!',
                    text: `প্রতি পেজে ${inputVal} জন ইউজার প্রদর্শনের সেটিং সফলভাবে সংরক্ষণ করা হয়েছে।`,
                    timer: 1500,
                    showConfirmButton: false
                }).then(() => {
                    const url = new URL(window.location.href);
                    url.searchParams.set('per_page', inputVal);
                    url.searchParams.set('page', '1');
                    window.location.href = url.toString();
                });
            } else {
                Swal.fire('ব্যর্থ!', res.message || 'সেভ করা সম্ভব হয়নি।', 'error');
            }
        })
        .catch(() => Swal.fire('ত্রুটি!', 'সার্ভারে সংযোগ ব্যর্থ হয়েছে।', 'error'));
    }
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
