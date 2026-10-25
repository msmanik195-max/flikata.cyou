<?php
/**
 * Localization and Multi-Language Engine
 * Flikata Admin Control Panel
 * Supports 100% Pure Bengali (বাংলা) and 100% Pure English (EN)
 */

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

// Handle Language Switch Request
if (isset($_GET['set_lang'])) {
    $requestedLang = strtolower(trim($_GET['set_lang']));
    if (in_array($requestedLang, ['bn', 'en'])) {
        $_SESSION['admin_lang'] = $requestedLang;
        setcookie('admin_lang', $requestedLang, time() + (86400 * 60), '/');
    }
    // Clean redirect to strip set_lang parameter
    $cleanUri = strtok($_SERVER['REQUEST_URI'], '?');
    $params = $_GET;
    unset($params['set_lang']);
    if (!empty($params)) {
        $cleanUri .= '?' . http_build_query($params);
    }
    header("Location: " . $cleanUri);
    exit;
}

// Current Language: default to 'bn' (Bengali)
$currentLang = $_SESSION['admin_lang'] ?? ($_COOKIE['admin_lang'] ?? 'bn');
if (!in_array($currentLang, ['bn', 'en'])) {
    $currentLang = 'bn';
}

$adminDictionary = [
    'bn' => [
        'app_name' => 'Flikata Admin',
        'control_panel' => 'কন্ট্রোল প্যানেল • লাইট',
        'main_menu' => 'প্রধান মেনু',
        'dashboard' => 'ড্যাশবোর্ড',
        'dashboard_heading' => 'এডমিন ড্যাশবোর্ড',
        'dashboard_subheading' => 'Flikata সোশ্যাল অ্যাপ ওভারভিউ ও পরিসংখ্যান',
        'users' => 'ব্যবহারকারী ব্যবস্থাপনা',
        'posts' => 'পোস্টসমূহ',
        'verification_wallet' => 'ভেরিফিকেশন ও ওয়ালেট',
        'verification_packages' => 'ভেরিফিকেশন প্যাকেজ',
        'verification_requests' => 'ভেরিফিকেশন অনুরোধ',
        'deposit_methods' => 'ডিপোজিট মাধ্যম',
        'deposit_requests' => 'ডিপোজিট অনুরোধ',
        'withdraw_methods' => 'উইথড্র মাধ্যম',
        'withdraw_requests' => 'উইথড্র অনুরোধ',
        'communication_notices' => 'যোগাযোগ ও বিজ্ঞপ্তি',
        'page_notifications' => 'পেজ নোটিফিকেশন',
        'system_settings' => 'সিস্টেম ও ক্লাউড',
        'feature_controls' => 'অ্যাপ ফিচার কন্ট্রোল',
        'app_update' => 'অ্যাপ্লিকেশন আপডেট',
        'cloudflare_r2' => 'ক্লাউডফ্লেয়ার R2 স্টোরেজ',
        'admin_settings' => 'এডমিন সেটিংস',
        'database_connected' => 'ডাটাবেজ সংযুক্ত',
        'database_disconnected' => 'ডাটাবেজ সংযোগ বিচ্ছিন্ন',
        'logged_in_as' => 'লগইন করা আছেন',
        'change_password' => 'পাসওয়ার্ড পরিবর্তন',
        'logout' => 'লগআউট',
        'confirm_logout' => 'আপনি কি নিশ্চিতভাবে লগআউট করতে চান?',
        'toggle_menu' => 'মেনু খুলুন/বন্ধ করুন',
        'language' => 'ভাষা',
        'lang_bn' => 'বাংলা (BN)',
        'lang_en' => 'English (EN)',
        'admin_portal' => 'অ্যাডমিনিস্ট্রেটর পোর্টাল - নিরাপদ প্রবেশ',
        'email_label' => 'এডমিন ইমেইল',
        'email_placeholder' => 'আপনার এডমিন ইমেইল লিখুন',
        'password_label' => 'পাসওয়ার্ড',
        'password_placeholder' => 'আপনার গোপন পাসওয়ার্ড লিখুন',
        'sign_in_btn' => 'অ্যাডমিন প্যানেলে প্রবেশ করুন',
        'all_rights_reserved' => 'সর্বস্বত্ব সংরক্ষিত।',
        'security_badge' => 'উচ্চ নিরাপত্তা সুরক্ষায় পরিচালিত',
        'save_changes' => 'পরিবর্তন সংরক্ষণ করুন',
        'cancel' => 'বাতিল',
        'status_active' => 'সক্রিয়',
        'status_inactive' => 'নিষ্ক্রিয়',
        'status_pending' => 'অপেক্ষমাণ',
        'status_approved' => 'অনুমোদিত',
        'status_rejected' => 'বাতিলকৃত',
        'delete' => 'মুছুন',
        'edit' => 'সম্পাদনা',
        'actions' => 'পদক্ষেপ',
        'total' => 'মোট',
        'search' => 'অনুসন্ধান করুন...',
        'success' => 'সফল',
        'error' => 'ত্রুটি'
    ],
    'en' => [
        'app_name' => 'Flikata Admin',
        'control_panel' => 'Control Panel • Light',
        'main_menu' => 'Main Menu',
        'dashboard' => 'Dashboard',
        'dashboard_heading' => 'Admin Dashboard',
        'dashboard_subheading' => 'Flikata Social Platform Overview & Analytics',
        'users' => 'User Management',
        'posts' => 'Posts Management',
        'verification_wallet' => 'Verification & Wallet',
        'verification_packages' => 'Verification Packages',
        'verification_requests' => 'Verification Requests',
        'deposit_methods' => 'Deposit Methods',
        'deposit_requests' => 'Deposit Requests',
        'withdraw_methods' => 'Withdraw Methods',
        'withdraw_requests' => 'Withdraw Requests',
        'communication_notices' => 'Communication & Notices',
        'page_notifications' => 'Page Notifications',
        'system_settings' => 'System & Cloud',
        'feature_controls' => 'Feature Controls',
        'app_update' => 'App Update Management',
        'cloudflare_r2' => 'Cloudflare R2 Storage',
        'admin_settings' => 'Admin Settings',
        'database_connected' => 'Database Connected',
        'database_disconnected' => 'Database Disconnected',
        'logged_in_as' => 'Logged in as',
        'change_password' => 'Change Password',
        'logout' => 'Log Out',
        'confirm_logout' => 'Are you sure you want to log out?',
        'toggle_menu' => 'Toggle Navigation Menu',
        'language' => 'Language',
        'lang_bn' => 'বাংলা (BN)',
        'lang_en' => 'English (EN)',
        'admin_portal' => 'Administrator Portal - Secure Login',
        'email_label' => 'Admin Email',
        'email_placeholder' => 'Enter your admin email',
        'password_label' => 'Password',
        'password_placeholder' => 'Enter your secret password',
        'sign_in_btn' => 'Sign In to Admin Portal',
        'all_rights_reserved' => 'All rights reserved.',
        'security_badge' => 'Protected by High-Grade Security',
        'save_changes' => 'Save Changes',
        'cancel' => 'Cancel',
        'status_active' => 'Active',
        'status_inactive' => 'Inactive',
        'status_pending' => 'Pending',
        'status_approved' => 'Approved',
        'status_rejected' => 'Rejected',
        'delete' => 'Delete',
        'edit' => 'Edit',
        'actions' => 'Actions',
        'total' => 'Total',
        'search' => 'Search here...',
        'success' => 'Success',
        'error' => 'Error'
    ]
];

function __($key, $fallback = '') {
    global $adminDictionary, $currentLang;
    if (isset($adminDictionary[$currentLang][$key])) {
        return $adminDictionary[$currentLang][$key];
    }
    return $fallback ?: $key;
}
