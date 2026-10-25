<?php
/**
 * Admin Login Page
 * Light Mode Only, Clean Card Style
 */
require_once __DIR__ . '/includes/db.php';

// If already logged in, redirect to dashboard
if (!empty($_SESSION['admin_logged_in']) && !empty($_SESSION['admin_id'])) {
    header('Location: index.php');
    exit;
}

$errorMsg = '';
$successMsg = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = trim($_POST['email'] ?? '');
    $password = trim($_POST['password'] ?? '');

    if (empty($email) || empty($password)) {
        $errorMsg = 'অনুগ্রহ করে ইমেইল এবং পাসওয়ার্ড প্রদান করুন।';
    } else {
        $stmt = $pdo->prepare("SELECT * FROM `admins` WHERE email = ? LIMIT 1");
        $stmt->execute([$email]);
        $admin = $stmt->fetch();

        if ($admin) {
            $isPasswordValid = false;
            // Check native password_verify
            if (password_verify($password, $admin['password_hash'])) {
                $isPasswordValid = true;
            } elseif ($password === $admin['password_hash']) {
                // In case it was stored as plaintext during initial SQL setup
                $isPasswordValid = true;
                // Automatically upgrade to strong bcrypt hash
                $newHash = password_hash($password, PASSWORD_BCRYPT);
                $pdo->prepare("UPDATE `admins` SET password_hash = ? WHERE id = ?")->execute([$newHash, $admin['id']]);
            }

            if ($isPasswordValid) {
                $_SESSION['admin_logged_in'] = true;
                $_SESSION['admin_id'] = $admin['id'];
                $_SESSION['admin_name'] = $admin['name'] ?? 'Super Admin';
                $_SESSION['admin_email'] = $admin['email'];
                $_SESSION['admin_role'] = $admin['role'] ?? 'admin';

                header('Location: index.php?msg_success=' . urlencode('এডমিন প্যানেলে স্বাগতম!'));
                exit;
            } else {
                $errorMsg = 'ভুল পাসওয়ার্ড! আবার চেষ্টা করুন।';
            }
        } else {
            $errorMsg = 'এই ইমেইল দিয়ে কোনো এডমিন পাওয়া যায়নি।';
        }
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>লগইন - Flikata Admin Portal</title>
    <!-- Google Font -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        :root {
            --primary: #4f46e5;
            --primary-hover: #4338ca;
            --primary-light: #eef2ff;
            --bg-color: #f8fafc;
            --card-border: #e2e8f0;
        }

        body {
            font-family: 'Plus Jakarta Sans', sans-serif;
            background: linear-gradient(135deg, #f8fafc 0%, #edf2f7 100%);
            color: #0f172a;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1.5rem;
        }

        .login-card {
            background: #ffffff;
            border-radius: 24px;
            border: 1px solid var(--card-border);
            box-shadow: 0 10px 40px -10px rgba(0, 0, 0, 0.08), 0 4px 12px rgba(0, 0, 0, 0.03);
            width: 100%;
            max-width: 440px;
            padding: 2.75rem 2.25rem;
            position: relative;
            overflow: hidden;
        }

        .brand-badge {
            width: 58px;
            height: 58px;
            background: linear-gradient(135deg, #4f46e5 0%, #06b6d4 100%);
            border-radius: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #ffffff;
            font-size: 1.75rem;
            margin: 0 auto 1.5rem auto;
            box-shadow: 0 8px 20px rgba(79, 70, 229, 0.35);
        }

        .form-control-custom {
            background-color: #f8fafc;
            border: 1.5px solid #e2e8f0;
            border-radius: 12px;
            padding: 0.85rem 1.15rem;
            font-size: 0.95rem;
            color: #0f172a;
            transition: all 0.2s ease;
        }

        .form-control-custom:focus {
            background-color: #ffffff;
            border-color: var(--primary);
            box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.12);
            color: #0f172a;
        }

        .btn-login {
            background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
            color: #ffffff;
            border: none;
            border-radius: 14px;
            padding: 0.9rem;
            font-weight: 700;
            font-size: 1rem;
            letter-spacing: -0.01em;
            transition: all 0.25s ease;
            box-shadow: 0 4px 14px rgba(79, 70, 229, 0.3);
        }

        .btn-login:hover {
            background: linear-gradient(135deg, #4338ca 0%, #4f46e5 100%);
            color: #ffffff;
            transform: translateY(-1px);
            box-shadow: 0 8px 22px rgba(79, 70, 229, 0.4);
        }

        .credential-box {
            background-color: #f1f5f9;
            border: 1px dashed #cbd5e1;
            border-radius: 12px;
            padding: 0.9rem 1rem;
            font-size: 0.85rem;
        }
    </style>
</head>
<body>

<div class="login-card position-relative">
    <!-- Top Right Language Switcher on Login Screen -->
    <div class="position-absolute top-0 end-0 p-3">
        <div class="dropdown">
            <button class="btn btn-sm btn-outline-secondary rounded-pill px-2 py-1 dropdown-toggle small" type="button" data-bs-toggle="dropdown" aria-expanded="false" style="font-size: 11.5px;">
                <i class="bi bi-translate me-1"></i><?= $currentLang === 'en' ? 'English' : 'বাংলা' ?>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 rounded-3 p-1">
                <li><a class="dropdown-item rounded-2 small <?= $currentLang === 'bn' ? 'active fw-bold' : '' ?>" href="?set_lang=bn">বাংলা (Bengali)</a></li>
                <li><a class="dropdown-item rounded-2 small <?= $currentLang === 'en' ? 'active fw-bold' : '' ?>" href="?set_lang=en">English</a></li>
            </ul>
        </div>
    </div>

    <div class="text-center mb-4 pt-2">
        <div class="brand-badge">
            <i class="bi bi-shield-lock-fill"></i>
        </div>
        <h3 class="fw-bold mb-1" style="color: #0f172a; letter-spacing: -0.02em;"><?= __('app_name', 'Flikata Admin') ?></h3>
        <p class="text-muted small mb-0"><?= __('admin_portal', 'Administrator Portal - Secure Login') ?></p>
    </div>

    <?php if (!empty($errorMsg)): ?>
        <div class="alert alert-danger d-flex align-items-center gap-2 border-0 rounded-3 py-2 px-3 mb-3 small" role="alert">
            <i class="bi bi-exclamation-triangle-fill fs-6 text-danger"></i>
            <div><?= htmlspecialchars($errorMsg) ?></div>
        </div>
    <?php endif; ?>

    <form method="POST" action="login.php" autocomplete="off">
        <div class="mb-3">
            <label class="form-label small fw-bold text-secondary mb-1"><?= __('email_label', 'Admin Email') ?></label>
            <div class="input-group">
                <span class="input-group-text bg-white border-end-0 border" style="border-radius: 12px 0 0 12px; border-color: #e2e8f0 !important;">
                    <i class="bi bi-envelope text-muted"></i>
                </span>
                <input type="email" name="email" class="form-control form-control-custom border-start-0" style="border-radius: 0 12px 12px 0;" placeholder="<?= __('email_placeholder', 'Enter Admin Email') ?>" value="" required autofocus>
            </div>
        </div>

        <div class="mb-4">
            <div class="d-flex justify-content-between align-items-center mb-1">
                <label class="form-label small fw-bold text-secondary mb-0"><?= __('password_label', 'Password') ?></label>
            </div>
            <div class="input-group">
                <span class="input-group-text bg-white border-end-0 border" style="border-radius: 12px 0 0 12px; border-color: #e2e8f0 !important;">
                    <i class="bi bi-lock text-muted"></i>
                </span>
                <input type="password" id="passwordInput" name="password" class="form-control form-control-custom border-start-0 border-end-0" placeholder="<?= __('password_placeholder', '••••••••') ?>" value="" required>
                <button class="btn btn-outline-secondary border bg-white" style="border-radius: 0 12px 12px 0; border-color: #e2e8f0 !important;" type="button" onclick="togglePasswordVisibility()">
                    <i class="bi bi-eye" id="togglePassIcon"></i>
                </button>
            </div>
        </div>

        <button type="submit" class="btn btn-login w-100 mb-3 py-2.5">
            <i class="bi bi-shield-check me-2"></i> <?= __('sign_in_btn', 'Sign In to Admin Portal') ?>
        </button>

        <div class="text-center text-muted" style="font-size: 0.8rem;">
            &copy; <?= date('Y') ?> Flikata Social App. <?= __('all_rights_reserved', 'All rights reserved.') ?>
        </div>
    </form>
</div>

<script>
    function togglePasswordVisibility() {
        const passInput = document.getElementById('passwordInput');
        const passIcon = document.getElementById('togglePassIcon');
        if (passInput.type === 'password') {
            passInput.type = 'text';
            passIcon.classList.remove('bi-eye');
            passIcon.classList.add('bi-eye-slash');
        } else {
            passInput.type = 'password';
            passIcon.classList.remove('bi-eye-slash');
            passIcon.classList.add('bi-eye');
        }
    }
</script>

</body>
</html>
