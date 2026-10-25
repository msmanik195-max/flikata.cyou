<?php
/**
 * Admin Dashboard
 * Light Mode, Card-Style with Modern Analytics Charts
 */
require_once __DIR__ . '/includes/db.php';
checkAdminAuth();

$pageTitle = ($currentLang === 'en') ? 'Dashboard - Flikata Admin' : 'ড্যাশবোর্ড - Flikata Admin';
$pageHeading = ($currentLang === 'en') ? 'Admin Dashboard' : 'এডমিন ড্যাশবোর্ড';
$pageSubheading = ($currentLang === 'en') ? 'Overall platform statistics and activity summary' : 'প্ল্যাটফর্মের সার্বিক পরিসংখ্যান ও কার্যক্রমের সারসংক্ষেপ';

// 1. Fetch User Counts safely
$totalUsers = 0;
$bannedUsers = 0;
$activeUsers = 0;
$verifiedUsers = 0;
try {
    $totalUsers = (int)$pdo->query("SELECT COUNT(*) FROM users")->fetchColumn();
    $bannedUsers = (int)$pdo->query("SELECT COUNT(*) FROM users WHERE is_banned = 1")->fetchColumn();
    $activeUsers = max(0, $totalUsers - $bannedUsers);
    $verifiedUsers = (int)$pdo->query("SELECT COUNT(*) FROM users WHERE is_verified = 1")->fetchColumn();
} catch (Exception $e) {}

// 2. Fetch Post Counts by Category safely
$totalPosts = 0;
$textPosts = 0;
$imagePosts = 0;
$videoPosts = 0;
try {
    $totalPosts = (int)$pdo->query("SELECT COUNT(*) FROM posts")->fetchColumn();
    $textPosts = (int)$pdo->query("SELECT COUNT(*) FROM posts WHERE (image_url IS NULL OR image_url = '') AND (video_url IS NULL OR video_url = '')")->fetchColumn();
    $imagePosts = (int)$pdo->query("SELECT COUNT(*) FROM posts WHERE image_url IS NOT NULL AND image_url != ''")->fetchColumn();
    $videoPosts = (int)$pdo->query("SELECT COUNT(*) FROM posts WHERE video_url IS NOT NULL AND video_url != ''")->fetchColumn();
} catch (Exception $e) {}

// 3. Fetch Stories & Reels
$totalStories = 0;
try {
    $totalStories = (int)$pdo->query("SELECT COUNT(*) FROM stories")->fetchColumn();
} catch (Exception $e) {}

$totalReels = 0;
try {
    $totalReels = (int)$pdo->query("SELECT COUNT(*) FROM reels")->fetchColumn();
} catch (Exception $e) {}

// 4. Fetch Notifications Count
$totalNotifications = 0;
try {
    $totalNotifications = (int)$pdo->query("SELECT COUNT(*) FROM notifications")->fetchColumn();
} catch (Exception $e) {}

// 5. Fetch Recent 5 Users
$recentUsers = [];
try {
    $rUsersStmt = $pdo->query("SELECT * FROM users ORDER BY id DESC LIMIT 5");
    $recentUsers = $rUsersStmt->fetchAll();
} catch (Exception $e) {}

// 6. Fetch Recent 5 Posts
$recentPosts = [];
try {
    $rPostsStmt = $pdo->query("
        SELECT p.*, u.full_name AS author_name, u.profile_pic AS author_avatar
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        ORDER BY p.id DESC
        LIMIT 5
    ");
    $recentPosts = $rPostsStmt->fetchAll();
} catch (Exception $e) {}

// 7. Last 7 Days User Registration Trend
$last7Days = [];
$regCounts = [];
for ($i = 6; $i >= 0; $i--) {
    $day = date('Y-m-d', strtotime("-$i days"));
    $dayLabel = date('D (d M)', strtotime("-$i days"));
    $last7Days[] = $dayLabel;
    
    $cntStmt = $pdo->prepare("SELECT COUNT(*) FROM users WHERE DATE(created_at) = ?");
    $cntStmt->execute([$day]);
    $regCounts[] = (int)$cntStmt->fetchColumn();
}

require_once __DIR__ . '/includes/header.php';
?>

<!-- Statistics Overview Cards -->
<div class="row g-4 mb-4">
    <!-- Total Users Card -->
    <div class="col-12 col-sm-6 col-xl-3">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <span class="text-muted fw-bold small text-uppercase">সর্বমোট ইউজার (Users)</span>
                <div class="stat-icon-box bg-primary-subtle text-primary">
                    <i class="bi bi-people-fill"></i>
                </div>
            </div>
            <h2 class="fw-bold mb-1" style="color: #0f172a;"><?= number_format($totalUsers) ?></h2>
            <div class="d-flex align-items-center gap-2 small">
                <span class="badge bg-success-subtle text-success fw-bold"><?= $activeUsers ?> সচল</span>
                <span class="badge bg-danger-subtle text-danger fw-bold"><?= $bannedUsers ?> ব্যান</span>
                <span class="text-muted ms-auto"><i class="bi bi-patch-check-fill text-primary"></i> <?= $verifiedUsers ?> ভেরিফায়েড</span>
            </div>
        </div>
    </div>

    <!-- Total Posts Card -->
    <div class="col-12 col-sm-6 col-xl-3">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <span class="text-muted fw-bold small text-uppercase">সর্বমোট পোস্ট (Posts)</span>
                <div class="stat-icon-box bg-info-subtle text-info">
                    <i class="bi bi-file-earmark-post-fill"></i>
                </div>
            </div>
            <h2 class="fw-bold mb-1" style="color: #0f172a;"><?= number_format($totalPosts) ?></h2>
            <div class="d-flex align-items-center gap-1 small text-muted">
                <span>টেক্সট: <strong><?= $textPosts ?></strong></span> •
                <span>ইমেজ: <strong><?= $imagePosts ?></strong></span> •
                <span>ভিডিও: <strong><?= $videoPosts ?></strong></span>
            </div>
        </div>
    </div>

    <!-- Total Stories & Reels -->
    <div class="col-12 col-sm-6 col-xl-3">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <span class="text-muted fw-bold small text-uppercase">স্টোরিজ ও রিলস (Stories/Reels)</span>
                <div class="stat-icon-box bg-warning-subtle text-warning">
                    <i class="bi bi-camera-reels-fill"></i>
                </div>
            </div>
            <h2 class="fw-bold mb-1" style="color: #0f172a;"><?= number_format($totalStories + $totalReels) ?></h2>
            <div class="d-flex align-items-center gap-2 small">
                <span class="badge bg-warning-subtle text-dark fw-bold"><?= $totalStories ?> স্টোরিজ</span>
                <span class="badge bg-secondary-subtle text-dark fw-bold"><?= $totalReels ?> রিলস</span>
                <span class="text-muted ms-auto"><i class="bi bi-hourglass-split"></i> 24h Auto</span>
            </div>
        </div>
    </div>

    <!-- Total Notifications Card -->
    <div class="col-12 col-sm-6 col-xl-3">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <span class="text-muted fw-bold small text-uppercase">নোটিফিকেশন পাঠানো</span>
                <div class="stat-icon-box bg-danger-subtle text-danger">
                    <i class="bi bi-bell-fill"></i>
                </div>
            </div>
            <h2 class="fw-bold mb-1" style="color: #0f172a;"><?= number_format($totalNotifications) ?></h2>
            <div class="d-flex align-items-center justify-content-between small">
                <span class="text-muted">ইউজার ইনবক্স আপডেট</span>
                <a href="notifications.php" class="text-primary fw-bold text-decoration-none">নতুন পাঠান &rarr;</a>
            </div>
        </div>
    </div>
</div>

<!-- Charts Row -->
<div class="row g-4 mb-4">
    <!-- Chart 1: User Growth Line Chart -->
    <div class="col-12 col-xl-8">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center justify-content-between mb-4">
                <div>
                    <h6 class="fw-bold mb-1 text-dark">দৈনিক নতুন ইউজার নিবন্ধন (গত ৭ দিন)</h6>
                    <small class="text-muted">সিস্টেমে প্রতিদিন নতুন একাউন্ট খোলার পরিসংখ্যান</small>
                </div>
                <span class="badge bg-primary-subtle text-primary fw-bold px-3 py-2">রেজিস্ট্রেশন গ্রোথ</span>
            </div>
            <div style="height: 300px; position: relative;">
                <canvas id="userGrowthChart"></canvas>
            </div>
        </div>
    </div>

    <!-- Chart 2: Posts Category Breakdown Doughnut -->
    <div class="col-12 col-xl-4">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center justify-content-between mb-4">
                <div>
                    <h6 class="fw-bold mb-1 text-dark">কনটেন্ট ক্যাটাগরি বিশ্লেষণ</h6>
                    <small class="text-muted">পোস্ট এবং স্টোরিজের অনুপাত</small>
                </div>
                <span class="badge bg-info-subtle text-info fw-bold">কনটেন্ট ডিস্ট্রিবিউশন</span>
            </div>
            <div style="height: 250px; position: relative;" class="d-flex align-items-center justify-content-center">
                <canvas id="contentDoughnutChart"></canvas>
            </div>
            <div class="d-flex justify-content-around text-center mt-3 pt-3 border-top small">
                <div>
                    <div class="fw-bold text-primary"><?= $textPosts ?></div>
                    <small class="text-muted">টেক্সট</small>
                </div>
                <div>
                    <div class="fw-bold text-success"><?= $imagePosts ?></div>
                    <small class="text-muted">ইমেজ</small>
                </div>
                <div>
                    <div class="fw-bold text-info"><?= $videoPosts ?></div>
                    <small class="text-muted">ভিডিও</small>
                </div>
                <div>
                    <div class="fw-bold text-warning"><?= $totalStories ?></div>
                    <small class="text-muted">স্টোরিজ</small>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Recent Users & Recent Posts Row -->
<div class="row g-4">
    <!-- Recent Users Table -->
    <div class="col-12 col-xl-6">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <div>
                    <h6 class="fw-bold mb-1 text-dark">সাম্প্রতিক ইউজারসমূহ (Recent Users)</h6>
                    <small class="text-muted">সর্বশেষ নিবন্ধিত ৫ জন ইউজার</small>
                </div>
                <a href="users.php" class="btn btn-sm btn-outline-primary rounded-pill px-3 fw-bold">সকল ইউজার দেখুন</a>
            </div>

            <div class="table-responsive">
                <table class="table table-custom align-middle mb-0">
                    <thead>
                        <tr>
                            <th>ইউজার</th>
                            <th>যোগাযোগ</th>
                            <th>স্ট্যাটাস</th>
                            <th class="text-end">অ্যাকশন</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php if (empty($recentUsers)): ?>
                            <tr>
                                <td colspan="4" class="text-center py-4 text-muted">কোনো ইউজার পাওয়া যায়নি।</td>
                            </tr>
                        <?php else: ?>
                            <?php foreach ($recentUsers as $u): ?>
                                <tr>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <img src="<?= !empty($u['profile_pic']) ? htmlspecialchars($u['profile_pic']) : 'https://ui-avatars.com/api/?name=' . urlencode($u['full_name']) . '&background=4f46e5&color=fff' ?>" class="user-avatar-sm" alt="Avatar">
                                            <div>
                                                <div class="fw-bold text-dark text-truncate" style="max-width: 140px;">
                                                    <?= htmlspecialchars($u['full_name']) ?>
                                                    <?php if ($u['is_verified']): ?>
                                                        <i class="bi bi-patch-check-fill text-primary" title="Verified"></i>
                                                    <?php endif; ?>
                                                </div>
                                                <small class="text-muted">ID: #<?= $u['id'] ?></small>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="small fw-semibold text-truncate" style="max-width: 160px;">
                                            <?= htmlspecialchars($u['email'] ?: ($u['phone'] ?: 'N/A')) ?>
                                        </div>
                                    </td>
                                    <td>
                                        <?php if (!empty($u['is_banned'])): ?>
                                            <span class="badge bg-danger-subtle text-danger fw-bold">ব্যানড</span>
                                        <?php else: ?>
                                            <span class="badge bg-success-subtle text-success fw-bold">সচল</span>
                                        <?php endif; ?>
                                    </td>
                                    <td class="text-end">
                                        <a href="users.php?search=<?= urlencode($u['email'] ?: $u['full_name']) ?>" class="btn btn-sm btn-light border rounded-pill" title="ইউজার পরিচালনা">
                                            <i class="bi bi-arrow-right-short fs-6"></i>
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

    <!-- Recent Posts List -->
    <div class="col-12 col-xl-6">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <div>
                    <h6 class="fw-bold mb-1 text-dark">সাম্প্রতিক পোস্টসমূহ (Recent Posts)</h6>
                    <small class="text-muted">সর্বশেষ আপলোডকৃত পোস্ট</small>
                </div>
                <a href="posts.php" class="btn btn-sm btn-outline-primary rounded-pill px-3 fw-bold">পোস্ট ম্যানেজমেন্ট</a>
            </div>

            <div class="table-responsive">
                <table class="table table-custom align-middle mb-0">
                    <thead>
                        <tr>
                            <th>লেখক ও কনটেন্ট</th>
                            <th>ক্যাটাগরি</th>
                            <th>তারিখ</th>
                            <th class="text-end">অ্যাকশন</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php if (empty($recentPosts)): ?>
                            <tr>
                                <td colspan="4" class="text-center py-4 text-muted">কোনো পোস্ট পোস্ট করা হয়নি।</td>
                            </tr>
                        <?php else: ?>
                            <?php foreach ($recentPosts as $p): ?>
                                <?php
                                    $catBadge = 'টেক্সট পোস্ট';
                                    $catColor = 'bg-primary-subtle text-primary';
                                    if (!empty($p['video_url'])) {
                                        $catBadge = 'ভিডিও পোস্ট';
                                        $catColor = 'bg-info-subtle text-info';
                                    } elseif (!empty($p['image_url'])) {
                                        $catBadge = 'ইমেজ পোস্ট';
                                        $catColor = 'bg-success-subtle text-success';
                                    }
                                ?>
                                <tr id="row-post-<?= $p['id'] ?>">
                                    <td>
                                        <div class="fw-bold text-dark text-truncate" style="max-width: 180px;">
                                            <?= htmlspecialchars($p['author_name'] ?? 'User #' . $p['user_id']) ?>
                                        </div>
                                        <div class="small text-muted text-truncate" style="max-width: 220px;">
                                            <?= htmlspecialchars($p['content'] ?: (!empty($p['image_url']) ? '[ছবি যুক্ত পোস্ট]' : '[ভিডিও পোস্ট]')) ?>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge <?= $catColor ?> fw-bold"><?= $catBadge ?></span>
                                    </td>
                                    <td class="small text-muted">
                                        <?= date('d M, h:i A', strtotime($p['created_at'])) ?>
                                    </td>
                                    <td class="text-end">
                                        <button class="btn btn-sm btn-outline-danger border-0 rounded-circle p-2" title="পোস্ট ডিলিট করুন" onclick="deletePostDirect(<?= $p['id'] ?>)">
                                            <i class="bi bi-trash3-fill"></i>
                                        </button>
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

<script>
    // Initialize User Registration Chart
    const ctxGrowth = document.getElementById('userGrowthChart').getContext('2d');
    new Chart(ctxGrowth, {
        type: 'line',
        data: {
            labels: <?= json_encode($last7Days) ?>,
            datasets: [{
                label: 'নতুন ইউজার',
                data: <?= json_encode($regCounts) ?>,
                borderColor: '#4f46e5',
                backgroundColor: 'rgba(79, 70, 229, 0.1)',
                fill: true,
                tension: 0.4,
                borderWidth: 3,
                pointBackgroundColor: '#4f46e5',
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0, color: '#94a3b8' },
                    grid: { color: '#f1f5f9' }
                },
                x: {
                    ticks: { color: '#94a3b8' },
                    grid: { display: false }
                }
            }
        }
    });

    // Initialize Content Breakdown Doughnut Chart
    const ctxDoughnut = document.getElementById('contentDoughnutChart').getContext('2d');
    new Chart(ctxDoughnut, {
        type: 'doughnut',
        data: {
            labels: ['টেক্সট পোস্ট', 'ইমেজ পোস্ট', 'ভিডিও পোস্ট', 'স্টোরিজ'],
            datasets: [{
                data: [<?= $textPosts ?>, <?= $imagePosts ?>, <?= $videoPosts ?>, <?= $totalStories ?>],
                backgroundColor: ['#4f46e5', '#10b981', '#06b6d4', '#f59e0b'],
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { boxWidth: 12, font: { size: 11 }, color: '#475569' }
                }
            },
            cutout: '72%'
        }
    });

    // Delete post helper with instant visual update
    function deletePostDirect(postId) {
        Swal.fire({
            title: 'পোস্টটি ডিলিট করতে চান?',
            text: 'এই পোস্টটি ডাটাবেজ থেকে সম্পূর্ণ মুছে ফেলা হবে!',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#64748b',
            confirmButtonText: 'হ্যাঁ, ডিলিট করুন',
            cancelButtonText: 'বাতিল'
        }).then((result) => {
            if (result.isConfirmed) {
                fetch('api.php', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ action: 'delete_post', post_id: postId })
                })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        const row = document.getElementById('row-post-' + postId);
                        if (row) row.remove();
                        Swal.fire('ডিলিট সম্পন্ন!', 'পোস্টটি স্থায়ীভাবে মুছে ফেলা হয়েছে।', 'success');
                    } else {
                        Swal.fire('ব্যর্থ!', res.message || 'ডিলিট করা সম্ভব হয়নি।', 'error');
                    }
                })
                .catch(() => {
                    Swal.fire('ত্রুটি!', 'সার্ভারে যোগাযোগ করা যায়নি।', 'error');
                });
            }
        });
    }
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
