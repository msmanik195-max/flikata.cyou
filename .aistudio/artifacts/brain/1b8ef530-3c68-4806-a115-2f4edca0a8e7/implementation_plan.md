# Implementation Plan: In-App Auto-Update System with Admin Panel Management

A complete in-app software update delivery system that allows admins to publish updates directly from the cPanel Admin Panel (via Cloudflare R2 APK uploads or direct links) and enforces seamless, in-app downloads with colorful 3D UI on the Android client without redirecting to external browsers.

---

## 1. Database Architecture & Backend APIs

### Database Schema (`app_updates` Table)
Add `app_updates` table in `backend/config/schema_update.php` and `backend/database.sql`:
- `id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `version_code` (INT, NOT NULL) — e.g. 14
- `version_name` (VARCHAR, NOT NULL) — e.g. "6.1"
- `apk_url` (VARCHAR, 1000, NOT NULL) — Cloudflare R2 public URL or custom CDN link
- `apk_size_bytes` (BIGINT, DEFAULT 0)
- `release_notes` (TEXT) — List of features/bug fixes in this update
- `is_force_update` (TINYINT, DEFAULT 1) — 1 = Mandatory (blocks app until updated), 0 = Optional
- `is_active` (TINYINT, DEFAULT 1)
- `created_at`, `updated_at` (DATETIME)

### Backend Endpoints
1. **`backend/app/check_update.php` (Public Endpoint)**:
   - Client sends its current `version_code` (e.g. `GET ?current_version_code=13`).
   - Checks the latest active record from `app_updates`.
   - Returns JSON:
     ```json
     {
       "success": true,
       "update_available": true,
       "is_force": true,
       "latest_version_code": 14,
       "latest_version_name": "6.1",
       "apk_url": "https://pub-...r2.dev/apk/flikata_v6.1.apk",
       "apk_size_mb": "30.5 MB",
       "release_notes": "What's new in this release...",
       "message": "New update available"
     }
     ```
2. **`backend/admin/app_update.php` (Admin Panel UI & Handler)**:
   - Add new sidebar menu item: **App Update / অ্যাপ আপডেট** in `admin/includes/sidebar.php` and `admin/includes/header.php`.
   - Admin features:
     - Form to enter `version_code`, `version_name`, `release_notes`, and toggle `is_force_update`.
     - File upload picker for `.apk` files — streams file to Cloudflare R2 bucket under `apk/flikata_v{version}.apk` using existing `r2_helper.php` (with local `/uploads/apk/` fallback if R2 is not configured).
     - Alternative input field for direct external APK URL.
     - View currently active release and update history table with deactivate/delete options.

---

## 2. Android Manifest & Permissions

### Permissions & FileProvider Setup
1. **Permissions in `AndroidManifest.xml`**:
   - `<uses-permission android:name="android.permission.INTERNET" />`
   - `<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />` (Allows triggering in-app package installation on Android 8.0+)
2. **FileProvider Configuration**:
   - Ensure `androidx.core.content.FileProvider` is declared in `AndroidManifest.xml` with authority `${applicationId}.provider`.
   - Add `<external-cache-path name="apk_downloads" path="apk/" />` and `<cache-path name="cache_apk" path="apk/" />` in `res/xml/file_paths.xml`.

---

## 3. In-App Download & Installation Engine

### `AppUpdateManager.kt`
Create a helper utility to manage the update lifecycle:
- Check for updates via Retrofit `ApiClient.apiService.checkAppUpdate(BuildConfig.VERSION_CODE)`.
- Download APK into application cache directory using `OkHttp` or Android `DownloadManager` with streaming byte tracking for accurate 0-100% progress.
- Once downloaded:
  - Verify APK file integrity.
  - Generate content URI using `FileProvider.getUriForFile`.
  - Create and launch `Intent(Intent.ACTION_VIEW)` with MIME type `application/vnd.android.package-archive` and `FLAG_GRANT_READ_URI_PERMISSION`.
  - If `canRequestPackageInstalls()` is false on Android 8+, guide user to toggle "Install Unknown Apps" for Flikata.

---

## 4. UI Design: 3D Colorful Glowing Update Screen

### UI Components (`AppUpdateDialog.kt` / `SplashScreen.kt`)
On startup in `SplashScreen.kt`:
1. Call `checkAppUpdate()`.
2. If an update is required:
   - Halt transition to `HomeScreen`.
   - Display a 3D Neumorphic glowing modal/sheet:
     - **3D Animated Rocket / Update Badge** with vibrant gradient halo (Cyan, Violet, Gold lighting).
     - **Version Comparison Pill**: e.g., `Current: v6.0` ➔ `Latest: v6.1`.
     - **Title & Release Notes**: Highlight improvements, bug fixes, and performance notes in a clean card.
     - **Live Download Progress**:
       - Animated gradient progress bar (0% - 100%).
       - Downloaded speed and size display (e.g., `18.2 MB / 30.5 MB • 65%`).
     - **Action Button**:
       - 3D Glowing Button: **"Update Now (এখনই আপডেট করুন)"**.
       - During download: Shows "Downloading..." with circular loader or progress indicator.
       - When completed: Switches to "Install Update (ইনস্টল করুন)".

---

## 5. Verification & Deliverables

1. Compile the app with `compile_applet`.
2. Verify:
   - APK upload and saving in Admin Panel `app_update.php`.
   - API response in `check_update.php`.
   - In-app update detection logic when `version_code` > current `BuildConfig.VERSION_CODE`.
   - In-app download progress and package installer trigger without leaving the app.
3. Package updated backend files into `cpanel_package_v5.zip`.
4. Build `APK_DOWNLOAD/flikata v6.1.apk` for testing.
