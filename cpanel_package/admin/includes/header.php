<?php
/**
 * Admin Panel Header & Layout Component
 * Light Mode Only, Card-Style Design
 */
require_once __DIR__ . '/db.php';
checkAdminAuth();

$currentPage = basename($_SERVER['PHP_SELF']);
$adminName = $_SESSION['admin_name'] ?? 'Super Admin';
$adminEmail = $_SESSION['admin_email'] ?? 'admin@gmail.com';

// Counts for badges
$pendingVerifCount = 0;
$pendingDepositCount = 0;
$pendingWithdrawCount = 0;
try {
    $pendingVerifCount = (int)$pdo->query("SELECT COUNT(*) FROM verification_requests WHERE status = 'pending'")->fetchColumn();
    $pendingDepositCount = (int)$pdo->query("SELECT COUNT(*) FROM deposit_requests WHERE status = 'pending'")->fetchColumn();
    $pendingWithdrawCount = (int)$pdo->query("SELECT COUNT(*) FROM withdraw_requests WHERE status = 'pending'")->fetchColumn();
} catch (Exception $e) {}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= isset($pageTitle) ? htmlspecialchars($pageTitle) . ' - Flikata Admin' : 'Flikata Admin Panel' ?></title>
    <!-- Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons & FontAwesome -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <!-- Chart.js CDN -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
    <!-- SweetAlert2 -->
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <style>
        :root {
            --primary: #4f46e5;
            --primary-hover: #4338ca;
            --primary-light: #eef2ff;
            --primary-subtle: #e0e7ff;
            --accent: #06b6d4;
            --bg-body: #f8fafc;
            --card-bg: #ffffff;
            --text-main: #0f172a;
            --text-muted: #64748b;
            --border-color: #e2e8f0;
            --sidebar-width: 270px;
            --sidebar-bg: #ffffff;
            --topbar-height: 70px;
            --shadow-card: 0 4px 20px -2px rgba(0, 0, 0, 0.05), 0 2px 6px -1px rgba(0, 0, 0, 0.02);
            --shadow-hover: 0 10px 25px -3px rgba(0, 0, 0, 0.08), 0 4px 10px -2px rgba(0, 0, 0, 0.03);
            --radius-card: 16px;
        }

        body {
            font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background-color: var(--bg-body);
            color: var(--text-main);
            overflow-x: hidden;
            min-height: 100vh;
        }

        /* Sidebar Styling */
        #sidebar {
            width: var(--sidebar-width);
            height: 100vh;
            position: fixed;
            top: 0;
            left: 0;
            background-color: var(--sidebar-bg);
            border-right: 1px solid var(--border-color);
            z-index: 1040;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            display: flex;
            flex-direction: column;
            box-shadow: 2px 0 12px rgba(0, 0, 0, 0.03);
        }

        #sidebar.collapsed {
            left: calc(-1 * var(--sidebar-width));
        }

        .sidebar-brand {
            height: var(--topbar-height);
            display: flex;
            align-items: center;
            padding: 0 1.5rem;
            border-bottom: 1px solid var(--border-color);
            background: #ffffff;
        }

        .sidebar-brand .brand-logo {
            width: 38px;
            height: 38px;
            background: linear-gradient(135deg, #4f46e5 0%, #06b6d4 100%);
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #ffffff;
            font-size: 1.25rem;
            font-weight: 800;
            margin-right: 0.85rem;
            box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
        }

        .sidebar-nav {
            padding: 1.25rem 0.85rem;
            flex-grow: 1;
            overflow-y: auto;
        }

        .nav-label {
            font-size: 0.725rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            color: #94a3b8;
            padding: 0.75rem 1rem 0.35rem 1rem;
        }

        .sidebar-link {
            display: flex;
            align-items: center;
            gap: 0.85rem;
            padding: 0.8rem 1.15rem;
            border-radius: 12px;
            font-weight: 600;
            font-size: 0.925rem;
            color: #475569;
            text-decoration: none;
            transition: all 0.2s ease;
            margin-bottom: 0.35rem;
        }

        .sidebar-link i {
            font-size: 1.15rem;
            color: #64748b;
            transition: all 0.2s ease;
            width: 24px;
            text-align: center;
        }

        .sidebar-link:hover {
            color: var(--primary);
            background-color: var(--primary-light);
        }

        .sidebar-link:hover i {
            color: var(--primary);
            transform: scale(1.1);
        }

        .sidebar-link.active {
            color: var(--primary);
            background-color: var(--primary-light);
            font-weight: 700;
            box-shadow: inset 3px 0 0 var(--primary);
        }

        .sidebar-link.active i {
            color: var(--primary);
        }

        .sidebar-footer {
            padding: 1.2rem;
            border-top: 1px solid var(--border-color);
            background: #ffffff;
        }

        /* Topbar Styling */
        #topbar {
            height: var(--topbar-height);
            position: fixed;
            top: 0;
            right: 0;
            left: var(--sidebar-width);
            background: #ffffff;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 1.75rem;
            z-index: 1030;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }

        #topbar.expanded {
            left: 0;
        }

        .hamburger-btn {
            background: #f1f5f9;
            border: none;
            width: 42px;
            height: 42px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            color: #334155;
            font-size: 1.25rem;
            transition: all 0.2s ease;
        }

        .hamburger-btn:hover {
            background: var(--primary-light);
            color: var(--primary);
        }

        /* Main Content Wrapper */
        #main-content {
            margin-left: var(--sidebar-width);
            padding-top: calc(var(--topbar-height) + 1.75rem);
            padding-bottom: 3rem;
            padding-left: 2rem;
            padding-right: 2rem;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            min-height: 100vh;
        }

        #main-content.expanded {
            margin-left: 0;
        }

        /* Modern Card Styling */
        .card-custom {
            background: var(--card-bg);
            border-radius: var(--radius-card);
            border: 1px solid var(--border-color);
            box-shadow: var(--shadow-card);
            padding: 1.5rem;
            transition: transform 0.2s ease, box-shadow 0.2s ease;
            position: relative;
        }

        .card-custom:hover {
            box-shadow: var(--shadow-hover);
        }

        .stat-icon-box {
            width: 52px;
            height: 52px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.4rem;
        }

        .badge-subtle {
            font-size: 0.75rem;
            font-weight: 700;
            padding: 0.35rem 0.65rem;
            border-radius: 20px;
        }

        /* Buttons & Forms */
        .btn-primary-custom {
            background: var(--primary);
            color: #ffffff;
            border: none;
            border-radius: 10px;
            font-weight: 600;
            padding: 0.6rem 1.25rem;
            transition: all 0.2s ease;
        }

        .btn-primary-custom:hover {
            background: var(--primary-hover);
            color: #ffffff;
            box-shadow: 0 4px 14px rgba(79, 70, 229, 0.35);
        }

        .btn-danger-custom {
            background: #ef4444;
            color: #ffffff;
            border: none;
            border-radius: 10px;
            font-weight: 600;
            padding: 0.5rem 1rem;
            transition: all 0.2s ease;
        }

        .btn-danger-custom:hover {
            background: #dc2626;
            color: #ffffff;
            box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
        }

        .table-custom {
            border-collapse: separate;
            border-spacing: 0;
            width: 100%;
        }

        .table-custom th {
            background-color: #f8fafc;
            color: #475569;
            font-weight: 700;
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            padding: 0.85rem 1.15rem;
            border-bottom: 2px solid var(--border-color);
        }

        .table-custom td {
            padding: 1.15rem;
            vertical-align: middle;
            border-bottom: 1px solid var(--border-color);
            background-color: #ffffff;
            font-size: 0.925rem;
        }

        .table-custom tr:last-child td {
            border-bottom: none;
        }

        .table-custom tbody tr:hover td {
            background-color: #f8fafc;
        }

        .user-avatar-sm {
            width: 42px;
            height: 42px;
            border-radius: 50%;
            object-fit: cover;
            border: 2px solid #e2e8f0;
        }

        /* Backdrop for mobile drawer */
        .sidebar-backdrop {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            background: rgba(15, 23, 42, 0.4);
            backdrop-filter: blur(4px);
            z-index: 1035;
        }

        @media (max-width: 991.98px) {
            #sidebar {
                left: calc(-1 * var(--sidebar-width));
            }
            #sidebar.show-mobile {
                left: 0;
            }
            #topbar {
                left: 0 !important;
            }
            #main-content {
                margin-left: 0 !important;
                padding-left: 1rem;
                padding-right: 1rem;
            }
            .sidebar-backdrop.show {
                display: block;
            }
        }
    </style>
</head>
<body>

<!-- Mobile Backdrop -->
<div class="sidebar-backdrop" id="sidebarBackdrop" onclick="toggleSidebar()"></div>

<!-- Left Sidebar Menu -->
<aside id="sidebar">
    <div class="sidebar-brand">
        <div class="brand-logo">
            <i class="bi bi-shield-lock-fill"></i>
        </div>
        <div>
            <h6 class="mb-0 fw-bold" style="font-size: 1.1rem; color: #0f172a; letter-spacing: -0.02em;"><?= __('app_name', 'Flikata Admin') ?></h6>
            <small class="text-muted fw-semibold" style="font-size: 0.75rem;"><?= __('control_panel', 'Control Panel • Light') ?></small>
        </div>
    </div>

    <div class="sidebar-nav">
        <div class="nav-label"><?= __('main_menu', 'Main Menu') ?></div>
        <a href="index.php" class="sidebar-link <?= $currentPage === 'index.php' ? 'active' : '' ?>">
            <i class="bi bi-grid-fill"></i>
            <span><?= __('dashboard', 'Dashboard') ?></span>
        </a>
        <a href="users.php" class="sidebar-link <?= $currentPage === 'users.php' ? 'active' : '' ?>">
            <i class="bi bi-people-fill"></i>
            <span><?= __('users', 'User Management') ?></span>
        </a>
        <a href="posts.php" class="sidebar-link <?= $currentPage === 'posts.php' ? 'active' : '' ?>">
            <i class="bi bi-collection-play-fill"></i>
            <span><?= __('posts', 'Posts') ?></span>
        </a>

        <div class="nav-label mt-3"><?= __('verification_wallet', 'Verification & Wallet') ?></div>
        <a href="verification_packages.php" class="sidebar-link <?= $currentPage === 'verification_packages.php' ? 'active' : '' ?>">
            <i class="bi bi-patch-check-fill text-primary"></i>
            <span><?= __('verification_packages', 'Verification Packages') ?></span>
        </a>
        <a href="verification_requests.php" class="sidebar-link <?= $currentPage === 'verification_requests.php' ? 'active' : '' ?> d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-person-check-fill text-info"></i>
                <span><?= __('verification_requests', 'Verification Requests') ?></span>
            </div>
            <?php if ($pendingVerifCount > 0): ?>
                <span class="badge bg-warning text-dark rounded-pill ms-auto"><?= $pendingVerifCount ?></span>
            <?php endif; ?>
        </a>
        <a href="deposit_methods.php" class="sidebar-link <?= $currentPage === 'deposit_methods.php' ? 'active' : '' ?>">
            <i class="bi bi-wallet2 text-success"></i>
            <span><?= __('deposit_methods', 'Deposit Methods') ?></span>
        </a>
        <a href="deposit_requests.php" class="sidebar-link <?= $currentPage === 'deposit_requests.php' ? 'active' : '' ?> d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-cash-stack text-success"></i>
                <span><?= __('deposit_requests', 'Deposit Requests') ?></span>
            </div>
            <?php if ($pendingDepositCount > 0): ?>
                <span class="badge bg-success text-white rounded-pill ms-auto"><?= $pendingDepositCount ?></span>
            <?php endif; ?>
        </a>
        <a href="withdraw_methods.php" class="sidebar-link <?= $currentPage === 'withdraw_methods.php' ? 'active' : '' ?>">
            <i class="bi bi-credit-card-2-front-fill text-warning"></i>
            <span><?= __('withdraw_methods', 'Withdraw Methods') ?></span>
        </a>
        <a href="withdraw_requests.php" class="sidebar-link <?= $currentPage === 'withdraw_requests.php' ? 'active' : '' ?> d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-arrow-up-right-circle-fill text-warning"></i>
                <span><?= __('withdraw_requests', 'Withdraw Requests') ?></span>
            </div>
            <?php if ($pendingWithdrawCount > 0): ?>
                <span class="badge bg-warning text-dark rounded-pill ms-auto"><?= $pendingWithdrawCount ?></span>
            <?php endif; ?>
        </a>

        <div class="nav-label mt-3"><?= __('communication_notices', 'Communication & Notices') ?></div>
        <a href="notifications.php" class="sidebar-link <?= $currentPage === 'notifications.php' ? 'active' : '' ?>">
            <i class="bi bi-bell-fill"></i>
            <span><?= __('page_notifications', 'Page Notifications') ?></span>
        </a>

        <div class="nav-label mt-3"><?= __('system_settings', 'System & Cloud') ?></div>
        <a href="feature_controls.php" class="sidebar-link <?= $currentPage === 'feature_controls.php' ? 'active' : '' ?>">
            <i class="bi bi-toggles2 text-success"></i>
            <span><?= __('feature_controls', 'অ্যাপ ফিচার কন্ট্রোল') ?></span>
        </a>
        <a href="cloudflare.php" class="sidebar-link <?= $currentPage === 'cloudflare.php' ? 'active' : '' ?>">
            <i class="bi bi-clouds-fill text-info"></i>
            <span><?= __('cloudflare_r2', 'Cloudflare R2 Storage') ?></span>
        </a>
        <a href="app_update.php" class="sidebar-link <?= $currentPage === 'app_update.php' ? 'active' : '' ?>">
            <i class="bi bi-cloud-arrow-up-fill text-primary"></i>
            <span><?= __('app_update', 'App Update') ?></span>
        </a>
        <a href="settings.php" class="sidebar-link <?= $currentPage === 'settings.php' ? 'active' : '' ?>">
            <i class="bi bi-sliders"></i>
            <span><?= __('admin_settings', 'Admin Settings') ?></span>
        </a>
    </div>

    <div class="sidebar-footer">
        <div class="d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center gap-2">
                <div class="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold" style="width: 38px; height: 38px; font-size: 0.9rem;">
                    A
                </div>
                <div>
                    <div class="fw-bold text-dark text-truncate" style="max-width: 120px; font-size: 0.875rem;"><?= htmlspecialchars($adminName) ?></div>
                    <small class="text-muted" style="font-size: 0.725rem;"><?= htmlspecialchars($adminEmail) ?></small>
                </div>
            </div>
            <a href="logout.php" class="btn btn-sm btn-outline-danger border-0 rounded-circle p-2" title="<?= __('logout', 'Logout') ?>" onclick="return confirm('<?= __('confirm_logout', 'Are you sure you want to log out?') ?>');">
                <i class="bi bi-box-arrow-right fs-5"></i>
            </a>
        </div>
    </div>
</aside>

<!-- Top Navbar Header with 3-Line Hamburger Menu -->
<header id="topbar">
    <div class="d-flex align-items-center gap-3">
        <!-- 3-Line Hamburger Toggle Button -->
        <button class="hamburger-btn" id="sidebarToggle" onclick="toggleSidebar()" aria-label="Toggle Navigation Menu" title="<?= __('toggle_menu', 'Toggle Menu') ?>">
            <i class="fa-solid fa-bars"></i>
        </button>
        <div>
            <h5 class="mb-0 fw-bold text-dark" style="font-size: 1.15rem;"><?= $pageHeading ?? __('dashboard_heading', 'Admin Dashboard') ?></h5>
            <small class="text-muted d-none d-sm-inline"><?= $pageSubheading ?? __('dashboard_subheading', 'Overview & Analytics') ?></small>
        </div>
    </div>

    <div class="d-flex align-items-center gap-3">
        <!-- Multi-Language Switcher (বাংলা / English) -->
        <div class="dropdown">
            <button class="btn btn-sm btn-outline-primary rounded-pill px-3 py-1.5 d-flex align-items-center gap-2 fw-bold shadow-sm dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="bi bi-translate fs-6 text-primary"></i>
                <span><?= $currentLang === 'en' ? 'English (EN)' : 'বাংলা (BN)' ?></span>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow border-0 rounded-3 p-2 mt-2">
                <li><span class="dropdown-header text-muted small"><?= $currentLang === 'en' ? 'Select Language' : 'ভাষা নির্বাচন করুন' ?></span></li>
                <li>
                    <a class="dropdown-item rounded-2 d-flex align-items-center justify-content-between py-2 <?= $currentLang === 'bn' ? 'active fw-bold' : '' ?>" href="?set_lang=bn">
                        <span><i class="bi bi-check2 text-success me-2 <?= $currentLang === 'bn' ? '' : 'invisible' ?>"></i>বাংলা (Bangla)</span>
                        <span class="badge bg-success-subtle text-success small ms-2">BN</span>
                    </a>
                </li>
                <li>
                    <a class="dropdown-item rounded-2 d-flex align-items-center justify-content-between py-2 <?= $currentLang === 'en' ? 'active fw-bold' : '' ?>" href="?set_lang=en">
                        <span><i class="bi bi-check2 text-primary me-2 <?= $currentLang === 'en' ? '' : 'invisible' ?>"></i>English</span>
                        <span class="badge bg-primary-subtle text-primary small ms-2">EN</span>
                    </a>
                </li>
            </ul>
        </div>

        <!-- Database Status Indicator -->
        <div class="badge rounded-pill bg-success-subtle text-success px-3 py-2 d-flex align-items-center gap-2 fw-semibold border border-success-subtle">
            <span class="spinner-grow spinner-grow-sm text-success" style="width: 8px; height: 8px;" role="status"></span>
            <span><?= __('database_connected', 'Database Connected') ?></span>
        </div>

        <!-- Quick Profile & Logout -->
        <div class="dropdown">
            <button class="btn btn-light rounded-pill px-3 py-1 d-flex align-items-center gap-2 border shadow-sm" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="bi bi-person-circle fs-5 text-primary"></i>
                <span class="fw-bold text-dark d-none d-md-inline"><?= htmlspecialchars($adminName) ?></span>
                <i class="bi bi-chevron-down fs-7 text-muted"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow border-0 rounded-3 p-2 mt-2">
                <li><span class="dropdown-header text-muted"><?= __('logged_in_as', 'Logged in as') ?></span></li>
                <li><span class="dropdown-item-text fw-bold"><?= htmlspecialchars($adminEmail) ?></span></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item rounded-2" href="settings.php"><i class="bi bi-shield-lock me-2 text-primary"></i> <?= __('change_password', 'Change Password') ?></a></li>
                <li><a class="dropdown-item rounded-2 text-danger" href="logout.php"><i class="bi bi-box-arrow-right me-2"></i> <?= __('logout', 'Log Out') ?></a></li>
            </ul>
        </div>
    </div>
</header>

<!-- Main Workspace Container -->
<main id="main-content">
